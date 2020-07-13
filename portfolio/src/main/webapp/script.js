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


function loadMessageBoard() {
  console.log("Verifying login...");
  verifyLogin();
  console.log("Building Message Board...");
  loadComments();
}

/** Contacts Login servlet to verify if user is logged in. */
function verifyLogin() {
  console.log("Fetching login status...");
  fetch('/login').then(response => response.json()).then((arrStrings) => {
    const loginElement = document.getElementById("login-box");
    console.log("Displaying login status");
    arrStrings.forEach((line) => {
      loginElement.innerHTML += line;
    });
  });
}

/**
 * Fetches the current message board and fills UI with helper function.
 */
function loadComments() {
  console.log("Loading Comments...");
  fetch('/data').then(response => response.json()).then((messageBoard) => {
    const historyEl = document.getElementById("messageBoard");
    console.log("Building Message Board...");
    messageBoard.messages.forEach((line) => {
      historyEl.appendChild(createCommentElement(line));
    });
  });
}

/** Helper function to populate HTML comment element */
function createCommentElement(comment) {
  const postElement = document.createElement("p");
  postElement.innerText = "[" + comment.timeStamp + "] " + comment.author + " wrote: " + comment.messageContent;
  return postElement;
}
