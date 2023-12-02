package com.example.bookalleyandroid.Utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.bookalleyandroid.Activities.ConversationActivity;
import com.example.bookalleyandroid.Activities.MessageActivity;
import com.example.bookalleyandroid.Adapters.ConversationAdapter;
import com.example.bookalleyandroid.Models.Book;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
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
    public static OffsetDateTime ToJavaTime(String cSharpDateTimeOffset) {
        // C# DateTimeOffset format is "yyyy-MM-ddTHH:mm:ss.ssszzz" (e.g., "2023-01-01T12:34:56.789+05:30")
        //Break the string into two parts: the date/time part and the offset part
        String[] parts = cSharpDateTimeOffset.split("\\+");
        String dateTimePart = parts[0];
        String offsetPart = "+" + parts[1];
        //Return the OffsetDateTime by parsing the two parts
        return OffsetDateTime.parse(dateTimePart + offsetPart);
    }
    public static void GetConversationFromBackEnd(Context context, OnGetConversationListener listener) {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.preference_key), Context.MODE_PRIVATE);
        String sessionToken = pref.getString("SESSION_TOKEN","");
        //Send start conversation request
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(context.getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Conversations")
                .appendQueryParameter("sessionToken", sessionToken);
        String uri = builder.build().toString();
        Log.d("Uri", "Handle getting conversations: " + uri);
        JsonArrayRequest request = new JsonArrayRequest( Request.Method.GET,uri,null,
                response -> {
                    Log.d("Response", "Handle getting conversations: " + response.toString());
                    ArrayList<Conversation> conversationResponse = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Conversation conversation = new Conversation();
                            conversation.Id = jsonObject.getLong("id");
                            conversation.PosterId = jsonObject.getLong("posterId");
                            conversation.PosterName = jsonObject.getString("posterName");
                            conversation.FinderId = jsonObject.getLong("finderId");
                            conversation.FinderName = jsonObject.getString("finderName");
                            JSONArray messages = jsonObject.getJSONArray("messages");
                            for (int j = 0; j < messages.length(); j++) {
                                JSONObject messageObject = messages.getJSONObject(j);
                                Message message = new Message();
                                message.Id = messageObject.getLong("id");
                                message.ConversationId = conversation.Id;
                                message.Content = messageObject.getString("content");
                                message.Source = messageObject.getString("source");
                                message.TimeStamp = ToJavaTime(messageObject.getString("timestamp"));
                                conversation.Messages.add(message);
                            }
                            conversationResponse.add(conversation);
                        } catch (JSONException e) {
                            Log.e("Parsing Conversation error", "onResponse: " + e.getMessage());
                        }
                    }
                    //Save conversations to database
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.AddOrUpdateConversations(conversationResponse);
                    listener.onGetConversation();
                },
                error -> {
                    Log.d("Error", "Handle getting conversations: " + error.toString());
                }
        );
        RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        requestQueue.add(request);
    }

    public interface OnGetConversationListener {
        void onGetConversation();
    }
}
