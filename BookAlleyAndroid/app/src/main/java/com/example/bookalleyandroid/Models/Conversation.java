package com.example.bookalleyandroid.Models;

import java.util.ArrayList;

public class Conversation {
    public String Id;
    public String PosterName;
    public String PosterId;
    public String FinderName;
    public String FinderId;
    public ArrayList<Message> Messages = new ArrayList<>();
}
