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

/**
 * Fetches the current state of the game and builds the UI.
 */
function loadComments() {
  console.log("Loading Comments...");
  fetch('/data').then(response => response.json()).then((comments) => {
    const historyEl = document.getElementById("messageBoard");
    console.log("Building Message Board...");
    comments.forEach((line) => {
      console.log("Building comment from " + line);
      historyEl.appendChild(createCommentElement(line));
    });
  });
}

/** Creates an paragraph element containing text. */
function createCommentElement(entry) {
  const postElement = document.createElement("p");
  postElement.innerText = "[" + entry.timeStamp + "] " + entry.author + " wrote: " + entry.messageContent;
  return postElement;
}
