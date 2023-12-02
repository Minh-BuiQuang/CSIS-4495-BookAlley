package com.example.bookalleyandroid.Models;

import java.time.OffsetDateTime;

public class Message {
    public long Id;
    public long ConversationId;
    public OffsetDateTime TimeStamp;
    public String Content;
    public String Source;
    public boolean IsRead;
}
