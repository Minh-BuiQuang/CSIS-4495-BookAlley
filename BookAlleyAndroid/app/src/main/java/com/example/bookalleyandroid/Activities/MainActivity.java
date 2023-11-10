package com.example.bookalleyandroid.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.RecyclerViews.ConversationAdapter;
import com.example.bookalleyandroid.RecyclerViews.PostAdapter;
import com.example.bookalleyandroid.Utilities.Constance;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private RequestQueue requestQueue;
    ArrayList<Post> posts = new ArrayList<>();

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

        PostAdapter adapter = new PostAdapter(MainActivity.this, locationFilteredPosts);
        binding.postRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPosts();
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
}