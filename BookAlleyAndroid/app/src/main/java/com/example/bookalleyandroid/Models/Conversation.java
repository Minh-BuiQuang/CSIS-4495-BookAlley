package com.example.bookalleyandroid.Models;

import java.util.ArrayList;

public class Conversation {
    public long Id;
    public String PosterName;
    public long PosterId;
    public String FinderName;
    public long FinderId;
    public ArrayList<Message> Messages = new ArrayList<>();
}
