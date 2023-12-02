package com.example.bookalleyandroid.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Adapters.PostAdapter;
import com.example.bookalleyandroid.Utilities.Constance;
import com.example.bookalleyandroid.Utilities.DatabaseHelper;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.example.bookalleyandroid.Utilities.Utilities.GetConversationFromBackEnd;
import static com.example.bookalleyandroid.Utilities.Utilities.SortPostByDate;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnItemClickListener {

    ActivityMainBinding binding;
    private RequestQueue requestQueue;
    ArrayList<Post> posts = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable periodicTask;
    TextView badgeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        validateSessionToken();

        binding.imageButton.setOnClickListener(v -> {
            startActivity(new Intent(this, PostingActivity.class));
        });
        binding.postRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.bookSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterPosts();
            }
        });
        binding.locationTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filterPosts();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, Constance.Cities);
        binding.locationTextView.setThreshold(1);
        binding.locationTextView.setAdapter(adapter);
    }

    private void filterPosts() {
        String searchText = binding.bookSearchEditText.getText().toString();
        ArrayList<Post> keywordFilteredPosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.BookTitle.toLowerCase().contains(searchText.toLowerCase()) ||
                    post.Author.toLowerCase().contains(searchText.toLowerCase()) ||
                    post.ISBN.toLowerCase().contains(searchText.toLowerCase())) {
                keywordFilteredPosts.add(post);
            }
        }
        ArrayList<Post> locationFilteredPosts = new ArrayList<>();
        String location = binding.locationTextView.getText().toString();
        if(!location.isEmpty()) {
            for (Post post : keywordFilteredPosts) {
                if (post.Location.toLowerCase().contains(location.toLowerCase())) {
                    locationFilteredPosts.add(post);
                }
            }
        } else {
            locationFilteredPosts = keywordFilteredPosts;
        }

        PostAdapter adapter = new PostAdapter(MainActivity.this, locationFilteredPosts, MainActivity.this);
        binding.postRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPosts();

        //Start periodic task to get Conversations
        periodicTask = new Runnable() {
            @Override
            public void run() {
                GetConversationFromBackEnd(MainActivity.this, () -> {
                    //Get conversation by user id from db
                    SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
                    Long userId = pref.getLong("USER_ID",0);
                    DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                    ArrayList<com.example.bookalleyandroid.Models.Conversation> conversations = db.GetConversationsByUserId(userId);
                    int unreadCount = 0;
                    for (com.example.bookalleyandroid.Models.Conversation conversation : conversations) {
                        for (Message message : conversation.Messages) {
                             if(message.IsRead == false) unreadCount++;
                        }
                    }
                    if(badgeTextView != null) {
                        if (unreadCount > 0) {
                            badgeTextView.setVisibility(View.VISIBLE);
                            badgeTextView.setText(String.valueOf(unreadCount));
                        } else {
                            badgeTextView.setVisibility(View.GONE);
                        }
                    }
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

    private void getPosts() {
        //Send sign out request
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Posts");

        String uri = builder.build().toString();
        Log.d("Uri", "get book postings: " + uri);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, uri, null, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", "onResponse: " + response.toString());
                // Get user id
                SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
                Long userId = pref.getLong("USER_ID",0);

                posts.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        //Skip posts created by current user
                        if(jsonObject.getLong("posterId") == userId) continue;

                        Post post = new Post();
                        post.Id = jsonObject.getLong("id");
                        post.PosterName = jsonObject.getString("posterName");
                        post.PosterId = jsonObject.getLong("posterId");
                        post.BookTitle = jsonObject.getString("bookTitle");
                        post.Author = jsonObject.getString("author");
                        post.ISBN = jsonObject.getString("isbn");
                        post.Image = jsonObject.getString("image");
                        post.Location = jsonObject.getString("location");
                        post.Note = jsonObject.getString("note");
                        post.DatePosted = OffsetDateTime.parse(jsonObject.getString("datePosted"));
                        posts.add(post);
                        SortPostByDate(posts);
                    } catch (JSONException e) {
                        Log.e("Parsing Post error", "onResponse: " + e.getMessage());
                    }
                }

                filterPosts();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String message = new String(error.networkResponse.data, "utf-8");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + message);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + e.getMessage());
                }
            }
        });

        requestQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        MenuItem chatMenuItem = menu.findItem(R.id.chat);
        View actionView = chatMenuItem.getActionView();
        if(actionView!= null) {
            ImageView chatIconImageView = actionView.findViewById(R.id.chatIconImageView);
            chatIconImageView.setOnClickListener(v -> onOptionsItemSelected(chatMenuItem));
            badgeTextView = actionView.findViewById(R.id.badgeTextView);
        } else {
            actionView = LayoutInflater.from(this).inflate(R.layout.chat_icon_with_badge, null);
            chatMenuItem.setActionView(actionView);
            ImageView chatIconImageView = actionView.findViewById(R.id.chatIconImageView);
            chatIconImageView.setOnClickListener(v -> onOptionsItemSelected(chatMenuItem));
            badgeTextView = actionView.findViewById(R.id.badgeTextView);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout_option) {
            logout();
        } else if (item.getItemId() == R.id.chat) {
            startActivity(new Intent(this, ConversationActivity.class));
        } else if(item.getItemId() == R.id.profile) {
            startActivity(new Intent(this, MyPostActivity.class));
        }
        return true;
    }
    private void validateSessionToken() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        String sessionToken = pref.getString("SESSION_TOKEN","");
        if(TextUtils.isEmpty(sessionToken)) showSignInActivity();
    }

    private void showSignInActivity() {
        startActivity(new Intent(this,SignInActivity.class));
        finish();
    }

    private void logout() {
        // Get session token
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        String sessionToken = pref.getString("SESSION_TOKEN","");
        //Send sign out request
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Users")
                .appendPath("SignOut")
                .appendQueryParameter("sessionToken", sessionToken);
        String uri = builder.build().toString();
        Log.d("Uri", "handleSignIn: " + uri);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", "onResponse: " + response.toString());
            }
        },null);

        requestQueue.add(request);

        // Clear session token and logout
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("SESSION_TOKEN",null);
        editor.putLong("USER_ID",0);
        editor.apply();
        // Show login activity
        showSignInActivity();
    }
    public void onItemClick(Post post) {
        //Show Popup to confirm starting a conversation
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Confirmation");
        dialogBuilder.setMessage("Do you want to start a conversation with " + post.PosterName + "?");
        dialogBuilder.setPositiveButton("Yes", (dialog, which) -> {
            // Handle the Yes button click
            // Get session token
            SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
            String sessionToken = pref.getString("SESSION_TOKEN","");
            //Send start conversation request
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(getString(R.string.book_alley_api))
                    .appendPath("api")
                    .appendPath("Conversations")
                    .appendQueryParameter("sessionToken", sessionToken)
                    .appendQueryParameter("isStatisticRequest", "false");

            String uri = builder.build().toString();
            Log.d("Uri", "Handle starting new conversation: " + uri);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("posterId", post.PosterId);
                jsonObject.put("postId", post.Id);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Start Conversation Error", "onErrorResponse: " + e.getMessage());
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Response", "Start Conversation onResponse: " + response.toString());
                    try {
                        long id = response.getLong("conversationId");
                        Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    } catch (JSONException e) {
                        Log.e("Start Conversation Error", "onResponse: " + e.getMessage());
                    }
                    dialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Start Conversation Error", "onErrorResponse: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Start conversation failed. Please check your connection!", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(request);
        });
        dialogBuilder.setNegativeButton("No", (dialog, which) -> {
            // Handle the No button click
            dialog.dismiss(); // Dismiss the dialog
        });
        dialogBuilder.create().show();
    }
}