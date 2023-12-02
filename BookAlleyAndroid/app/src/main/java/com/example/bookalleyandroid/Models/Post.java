package com.example.bookalleyandroid.Models;

import java.time.OffsetDateTime;

public class Post {
    public long Id;
    public String PosterName;
    public long PosterId;
    public String BookTitle;
    public String Author;
    public String ISBN;
    public String Image;
    public String Location;
    public String Note;
    public OffsetDateTime DatePosted;
    public int Quantity;
}
