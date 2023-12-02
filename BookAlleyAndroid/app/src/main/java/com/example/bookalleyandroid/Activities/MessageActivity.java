package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Adapters.MessageAdapter;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.DatabaseHelper;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityMessageBinding;

import org.json.JSONObject;

import static com.example.bookalleyandroid.Utilities.Utilities.GetConversationFromBackEnd;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;
    Conversation conversation;
    private RequestQueue requestQueue;
    private Handler handler = new Handler();
    private Runnable periodicTask;
    private long conversationId;

    boolean isPoster = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        //Get conversation id from intent to load the current conversation
        Bundle extras = getIntent().getExtras();
        conversationId = extras.getLong("CONVERSATION_ID", -1);
        if(conversationId == -1) finish();

        DatabaseHelper db = new DatabaseHelper(this);
        conversation = db.GetConversationById(conversationId);
        //Get userId from shared preference and set title to the other user's name
        Long userId = getSharedPreferences(getString(R.string.preference_key), MODE_PRIVATE).getLong("USER_ID", -1);
        if(userId == conversation.PosterId) {
            getSupportActionBar().setTitle(conversation.FinderName);
            isPoster = true;
        } else {
            getSupportActionBar().setTitle(conversation.PosterName);
            isPoster = false;
        }
        //Populate message recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        binding.messageRecyclerView.setLayoutManager(layoutManager);
        MessageAdapter adapter = new MessageAdapter(this, conversation, isPoster);
        binding.messageRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.sendButton.setOnClickListener(v -> {
            String message = binding.messageEditText.getText().toString();
            if(message.isEmpty()) {
                return;
            }
            //Send message to server
            // Get session token
            SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
            String sessionToken = pref.getString("SESSION_TOKEN","");
            //Build api request
            //Send start conversation request
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(getString(R.string.book_alley_api))
                    .appendPath("api")
                    .appendPath("Messages")
                    .appendQueryParameter("sessionToken", sessionToken);
            String uri = builder.build().toString();
            Log.d("Uri", "Handle sending message: " + uri);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("conversationId", conversation.Id);
                jsonObject.put("content", message);
                if(isPoster){
                    jsonObject.put("source", "poster");
                } else {
                    jsonObject.put("source", "finder");
                }
            } catch (Exception e) {
                Log.d("Error", "Handle sending message: " + e.getMessage());
            }

            //Send request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Response", "Start Conversation onResponse: " + response.toString());
                    binding.messageEditText.setText("");
                    GetConversationFromBackEnd(MessageActivity.this, () -> {
                        DatabaseHelper db = new DatabaseHelper(MessageActivity.this);
                        conversation = db.GetConversationById(conversationId);
                        MessageAdapter adapter = new MessageAdapter(MessageActivity.this, conversation, isPoster);

                        //Populate message recycler view
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MessageActivity.this);
                        layoutManager.setReverseLayout(true);
                        binding.messageRecyclerView.setLayoutManager(layoutManager);
                        binding.messageRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Start Conversation Error", "onErrorResponse: " + error.getMessage());
                    Toast.makeText(MessageActivity.this, "Message could not be sent. Please check your connection!", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(request);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        //Start periodic task to get Conversations
        periodicTask = new Runnable() {
            @Override
            public void run() {
                GetConversationFromBackEnd(MessageActivity.this, () -> {
                    DatabaseHelper db = new DatabaseHelper(MessageActivity.this);
                    if(conversationId != -1) {
                        //Update messages read status
                        db.UpdateMessagesReadStatus(conversationId);
                        //Update the conversation
                        conversation = db.GetConversationById(conversationId);
                        MessageAdapter adapter = new MessageAdapter(MessageActivity.this, conversation, isPoster);
                        binding.messageRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
                // Call the same runnable again after a delay
                handler.postDelayed(this, 2000);
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
}