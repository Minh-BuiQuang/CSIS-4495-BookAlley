package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.RecyclerViews.ConversationAdapter;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityConversationBinding;
import com.example.bookalleyandroid.databinding.ActivityMainBinding;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class ConversationActivity extends AppCompatActivity {
    ActivityConversationBinding binding;
    private RequestQueue requestQueue;
    ArrayList<Conversation> conversations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Conversations");

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        binding.conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Mock up conversation

        conversations.add(new Conversation(){
            {
                Id = UUID.randomUUID().toString();
                PosterName = "John";
                Messages.add(new Message(){
                    {
                        Content = "Hi. Is your book still available?";
                        TimeStamp = OffsetDateTime.parse("2017-07-15T10:52:59Z");
                    }
                });
            }
        });
        conversations.add(new Conversation(){
            {
                Id = UUID.randomUUID().toString();
                PosterName = "Mike";
                Messages.add(new Message(){
                    {
                        Content = "Hi. I saw your posting for a book I'm looking for.";
                        TimeStamp = OffsetDateTime.parse("2017-07-15T12:55:51Z");
                    }
                });
            }
        });
        ConversationAdapter adapter = new ConversationAdapter(ConversationActivity.this, conversations);
        binding.conversationRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}