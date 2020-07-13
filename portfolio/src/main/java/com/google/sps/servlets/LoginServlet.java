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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.lang.String;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;

@WebServlet("/login")
public class LoginServlet extends HttpServlet{

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    boolean loginStatus = userService.isUserLoggedIn();

    ArrayList<String> arrStrings = new ArrayList();
    if (loginStatus) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/contact.html";
      String logoutURL = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      arrStrings.add("<p>Hello, " + userEmail + "! You are logged in.</p>");
      arrStrings.add("<p><a href=\""+ logoutURL + "\">Click here to log out.</a></p>");
    } else {
      String loginURL = userService.createLoginURL("/contact.html");
      arrStrings.add("<p><a href=\"" + loginURL + "\"> Login Here </a></p>");
    }

    Gson gson = new Gson();
    response.setContentType("text/json;");
    response.getWriter().println(gson.toJson(arrStrings));
  }
}