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

  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> candidates = new ArrayList<>();
    ArrayList<String> guests = new ArrayList<>(request.getAttendees());
    
    if ((int) request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      candidates.add(TimeRange.WHOLE_DAY);
      return candidates; //empty
    } else if (events.isEmpty()) {
      candidates.add(TimeRange.WHOLE_DAY);
      return candidates; //empty
    } else if (guests.size() == 0) {
      candidates.add(TimeRange.WHOLE_DAY);
      return candidates; //empty
    }

    HashSet<TimeRange> unavailableTimes = new HashSet<TimeRange>();

    for (Event cur : events) {
      if (!Collections.disjoint(cur.getAttendees(), guests)) {
        unavailableTimes.add(cur.getWhen());
      }
    }

    ArrayList<TimeRange> sortedUnavalableTimes = new ArrayList<>(unavailableTimes);
    Collections.sort(sortedUnavalableTimes, TimeRange.ORDER_BY_START);

    List<TimeRange> freeWindows = new ArrayList<>();
    TimeRange prev = sortedUnavalableTimes.get(0);
    
    if (sortedUnavalableTimes.isEmpty()) {
      freeWindows.add(TimeRange.WHOLE_DAY);
      return freeWindows;
    }

    int windowStart = 0;
    int previousEventEnd = 0;
    for (TimeRange curWhen : sortedUnavalableTimes) {
      if (curWhen.start() <= previousEventEnd) { //Overlapping events
        previousEventEnd = curWhen.end();
        continue;
      } else if (request.getDuration() >= curWhen.start() - previousEventEnd ) {
        freeWindows.add(TimeRange.fromStartEnd(previousEventEnd, curWhen.start(), false));
      }
      previousEventEnd = curWhen.end();
    }
    if (TimeRange.WHOLE_DAY.end() - previousEventEnd >= request.getDuration()) {
      freeWindows.add(TimeRange.fromStartEnd(previousEventEnd, TimeRange.WHOLE_DAY.end(), false));
    }
    return freeWindows;
  }
}
