package com.google.sps.data;

import java.util.Date;

public class Comment {

  private final String author;
  private final String messageContent;
  private final Date timeStamp;

  public Comment(String author, String messageContent, Date timeStamp) {
    this.author = author;
    this.messageContent = messageContent;
    this.timeStamp = timeStamp;
  }
}
