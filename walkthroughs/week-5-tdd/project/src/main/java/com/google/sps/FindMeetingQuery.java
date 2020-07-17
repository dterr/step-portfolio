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

public final class FindMeetingQuery {

  /**
    Collect busy times
    Calculate free windows
    Choose best window
    Figure which time works best

    Collect optional busy times
    Calculate their free windows
    Calculate intersection

  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    ArrayList<TimeRange> candidates = new ArrayList<>();
    ArrayList<String> guests = new ArrayList<>(request.getAttendees());
    ArrayList<String> optionals = new ArrayList<>(request.getOptionalAttendees());
    long duration = request.getDuration();
    
    if ((int) request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return candidates; //empty
    } else if (events.isEmpty() || guests.isEmpty()) {
      candidates.add(TimeRange.WHOLE_DAY);
      return candidates; //empty
    } else if (guests.isEmpty()) {
      candidates.add(TimeRange.WHOLE_DAY);
      return candidates; //empty
    }

    ArrayList<TimeRange> sortedUnavailableTimes = (ArrayList<TimeRange>) collectBusyTimes(guests, events);
    ArrayList<TimeRange> freeWindows = (ArrayList<TimeRange>) findAppropiateFreeWindows(sortedUnavailableTimes, duration);

    if (!optionals.isEmpty()) {
      ArrayList<TimeRange> optionalWindows = (ArrayList<TimeRange>) considerOptionalAttendees(freeWindows, optionals, events, duration);
      if (freeWindows.isEmpty()) {
        return optionalWindows;
      }
      candidates = (ArrayList<TimeRange>) windowIntersections(optionalWindows, freeWindows, duration);
      if (!candidates.isEmpty()) {
        return candidates;
      }
    }
    if (freeWindows.isEmpty()) return Arrays.asList();
    return freeWindows;
  }


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

  private Collection<TimeRange> considerOptionalAttendees
      (Collection<TimeRange> mandatoryWindows, Collection<String> optionals, 
      Collection<Event> events, long duration) {

    ArrayList<TimeRange> sortedUnavailableTimes = 
      (ArrayList<TimeRange>) collectBusyTimes(optionals, events);
    if (sortedUnavailableTimes.isEmpty()) {
      return sortedUnavailableTimes;
    }

    ArrayList<TimeRange> freeWindows = 
      (ArrayList<TimeRange>) findAppropiateFreeWindows(sortedUnavailableTimes, duration);
    return freeWindows;
  }


  /* Assume sorted collections 
  * Returns intersection times.
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
