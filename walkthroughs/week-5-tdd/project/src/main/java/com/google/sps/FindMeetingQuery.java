// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

/**
* FindMeetingQuery Class - Uses Event, Meeting Request, and Time Range objects
* to schedule a meeting time given participants and unique schedules.
*
* A query is fielded in the following methodology:
*      Collect busy times
*      Calculate free windows
*      Choose best window
*      Figure which time works best
*   
*     If optional attendees
*      Collect optional busy times
*      Calculate those free windows
*      Calculate intersection
*/
public final class FindMeetingQuery {

  /**
  * Function query
  * This method searches for common free windows in the schedules of the attendees needed. 
  * Optional attendees for the meeting are also considered and accommodated if possible.
  * @param request The object containing the requesting event and details
  * @param events A list of all of the events and attendees for the day
  * @return A list of possible time frames that satisfy the request.
  *
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    ArrayList<TimeRange> candidates = new ArrayList<>();
    ArrayList<String> guests = new ArrayList<>(request.getAttendees());
    ArrayList<String> optionals = new ArrayList<>(request.getOptionalAttendees());
    long duration = request.getDuration();
    
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    } else if (events.isEmpty() || guests.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    if (!optionals.isEmpty()) {
      return meetingWithOptionals(guests, optionals, duration, events);
    } else {
      return communalFreeWindows(guests, duration, events);
    }
  }

  /**
  * Method meetingWithOptionals
  * This method handles the case that there are optional attendees for this meeting.
  * Accommodation of the optional attendees is all or nothing.
  * @param mandatory The required meeting attendees.
  * @param optionals The optional meeting attendees.
  * @param duration The desired length of the meeting.
  * @param events A list of all the events occuring during that day.
  * @return A list of the time slots that attempts to accommodate all attendees.
  */
  private Collection<TimeRange> meetingWithOptionals
      (Collection<String> mandatory, Collection<String> optionals, 
      long duration, Collection<Event> events) {
        
    ArrayList<TimeRange> mandatoryWindows = (ArrayList<TimeRange>) communalFreeWindows(mandatory, duration, events);
    ArrayList<TimeRange> optionalWindows = (ArrayList<TimeRange>) communalFreeWindows(optionals, duration, events);

    if (mandatoryWindows.isEmpty()) {
      return optionalWindows;
    }
    ArrayList<TimeRange> accommodatingWindows = windowIntersections(mandatoryWindows, optionalWindows, duration);
    if (accommodatingWindows.isEmpty()) {
      return mandatoryWindows;
    }
    return accommodatingWindows;
  }

  /**
  * Method communalFreeWindows
  * This method finds the free time throughout the day that 
  * a desired group of people have in common.
  *
  * @param attendees The desired group of people to find free time for.
  * @param duration The minimum desired length of free time sought after
  * @param events A list of all the events ocurring during one day.
  * @return A list of the free spaces the desired people have in common.
  */
  private Collection<TimeRange> communalFreeWindows 
      (Collection<String> attendees, long duration, Collection<Event> events) {
    
    ArrayList<TimeRange> sortedUnavailableTimes = (ArrayList<TimeRange>) collectBusyTimes(attendees, events);
    ArrayList<TimeRange> freeWindows = (ArrayList<TimeRange>) findAppropiateFreeWindows(sortedUnavailableTimes, duration);
    return freeWindows;
  }

  /**
  * Method collectBusyTimes
  * This helper method takes a list of people and generates their schedules from a list of events.
  *
  * @param attendees The desired people whose schedules are considered.
  * @param events A list of all of the events occuring.
  * @return a sorted list of the busy times of the provided people.
  */
  private Collection<TimeRange> collectBusyTimes(Collection<String> attendees, Collection<Event> events) {
    HashSet<TimeRange> unavailableTimes = new HashSet<TimeRange>();
    for (Event cur : events) {
      if (!Collections.disjoint(cur.getAttendees(), attendees)) {
        unavailableTimes.add(cur.getWhen());
      }
    }

    ArrayList<TimeRange> sortedUnavailableTimes = new ArrayList<>(unavailableTimes);
    Collections.sort(sortedUnavailableTimes, TimeRange.ORDER_BY_START);
    return sortedUnavailableTimes;
  }

  /**
  * Method findAppropiateFreeWindows
  * This helper method determines free times within a communal schedule that are sufficiently long.
  *
  * @param sortedUnavailableTimes A list of busy times ordered for the day.
  * @param duration The desired length of the meeting.
  * @return A list of communal free times that are sufficiently long.
  */
  private Collection<TimeRange> findAppropiateFreeWindows(Collection<TimeRange> sortedUnavailableTimes, long duration) {
    List<TimeRange> freeWindows = new ArrayList<>();

    int previousEventEnd = 0;
    for (TimeRange curWhen : sortedUnavailableTimes) {
      if (curWhen.start() <= previousEventEnd) { //Overlapping events
        if (!(curWhen.end() < previousEventEnd)) { //confirm no encapsulation
          previousEventEnd = curWhen.end(); 
        }
        continue;
      } else if (duration <= curWhen.start() - previousEventEnd) {
        freeWindows.add(TimeRange.fromStartEnd(previousEventEnd, curWhen.start(), false));
      }
      previousEventEnd = curWhen.end();
    }

    if (TimeRange.WHOLE_DAY.end() - previousEventEnd >= duration) {
      freeWindows.add(TimeRange.fromStartEnd(previousEventEnd, TimeRange.WHOLE_DAY.end(), false));
    }
    return freeWindows;
  }

  /**
  * Method windowIntersections
  * This method finds the times in common between two schedules that fit a time constraint.
  * Assumes that each schedule is sorted from start to end of day.
  * @param primary A schedule to consider.
  * @param secondary A schedule to consider its intersection with the other.
  * @param minWindowSize The desired minimum length for free times in common.
  * @return A list of the times these schedules have in common.
  */
  private ArrayList<TimeRange> windowIntersections
      (ArrayList<TimeRange> primary, ArrayList<TimeRange> secondary, long minWindowSize) {
    
    ArrayList<TimeRange> scheduleIntersxns = new ArrayList<TimeRange>();
    for (int i = 0; i < primary.size(); i++) {
      TimeRange a = primary.get(i);
      for (int j = 0; j < secondary.size(); j++) {
        TimeRange b = secondary.get(j);
        if (a.overlaps(b)) {
          TimeRange window = a.getOverlap(b);
          if (window.duration() >= minWindowSize) {
            scheduleIntersxns.add(window);
          }
        }
      }
    }
    return scheduleIntersxns;
  }
}
