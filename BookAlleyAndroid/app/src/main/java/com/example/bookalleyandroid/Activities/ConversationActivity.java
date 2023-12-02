package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.Adapters.ConversationAdapter;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.DatabaseHelper;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityConversationBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.bookalleyandroid.Utilities.Utilities.GetConversationFromBackEnd;
import static com.example.bookalleyandroid.Utilities.Utilities.ToJavaTime;

public class ConversationActivity extends AppCompatActivity {
    ActivityConversationBinding binding;
    private RequestQueue requestQueue;
    ArrayList<Conversation> conversations = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable periodicTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Conversations");

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        binding.conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Get conversation from server
        // Get session token
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        String sessionToken = pref.getString("SESSION_TOKEN","");
        //Send start conversation request
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(getString(R.string.book_alley_api))
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
                    DatabaseHelper db = new DatabaseHelper(this);
                    db.AddOrUpdateConversations(conversationResponse);
                    LoadConversations();

                    ConversationAdapter adapter = new ConversationAdapter(ConversationActivity.this, conversations);
                    binding.conversationRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    Bundle extras = getIntent().getExtras();
                    //Check if there is a conversation id in the intent and load the message activity
                    if(extras != null) {
                        Long conversationId = extras.getLong("id", -1);
                        if (conversationId != -1) {
                            for (Conversation conversation : conversations) {
                                if (conversation.Id == conversationId) {
                                    Intent i = new Intent(this, MessageActivity.class);
                                    i.putExtra("CONVERSATION_ID", conversationId);
                                    startActivity(i);
                                    break;
                                }
                            }
                        }
                    }
                },
                error -> {
                    Log.d("Error", "Handle getting conversations: " + error.toString());
                }
        );
        requestQueue.add(request);

        ConversationAdapter adapter = new ConversationAdapter(ConversationActivity.this, conversations);
        binding.conversationRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //Start periodic task to get Conversations
        periodicTask = new Runnable() {
            @Override
            public void run() {
                GetConversationFromBackEnd(ConversationActivity.this, () -> {
                    LoadConversations();
                    ConversationAdapter adapter = new ConversationAdapter(ConversationActivity.this, conversations);
                    binding.conversationRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                });
                // Call the same runnable again after a delay
                handler.postDelayed(this, 5000);
            }
        };

        // Start the periodic task
        handler.postDelayed(periodicTask, 1000); // Start after 1 second
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop periodic task
        handler.removeCallbacks(periodicTask);
    }

    private void LoadConversations() {
        DatabaseHelper db = new DatabaseHelper(this);
        //Get UserId from Preferences
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        Long userId = pref.getLong("USER_ID", -1);
        conversations = db.GetConversationsByUserId(userId);
    }
}