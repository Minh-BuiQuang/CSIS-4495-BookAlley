package com.example.bookalleyandroid.Utilities;

import com.example.bookalleyandroid.Models.Book;
import com.example.bookalleyandroid.Models.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Utilities {
    public static void SortPostByDate(ArrayList<Post> posts) {

        // Use Collections.sort() with a custom comparator
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                // Compare books based on their createdDate
                return post2.DatePosted.compareTo(post1.DatePosted);
            }
        });
    }
}
