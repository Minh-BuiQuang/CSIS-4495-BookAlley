package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Adapters.PostAdapter;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityMyPostBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.example.bookalleyandroid.Utilities.Utilities.SortPostByDate;

public class MyPostActivity extends AppCompatActivity implements PostAdapter.OnItemClickListener {

    ActivityMyPostBinding binding;
    private RequestQueue requestQueue;
    ArrayList<Post> posts = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("My Posts");

        binding.myPostRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        getPosts();
    }

    private void getPosts() {
        // Get session token
        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
        String sessionToken = pref.getString("SESSION_TOKEN","");
        //Send sign out request
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Posts")
                .appendQueryParameter("sessionToken", sessionToken);

        String uri = builder.build().toString();
        Log.d("Uri", "get book postings: " + uri);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, uri, null, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", "onResponse: " + response.toString());

                posts.clear();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
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

                PostAdapter adapter = new PostAdapter(MyPostActivity.this, posts, MyPostActivity.this);
                binding.myPostRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String message = new String(error.networkResponse.data, "utf-8");
                    Toast.makeText(MyPostActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + message);
                } catch (Exception e) {
                    Toast.makeText(MyPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + e.getMessage());
                }
            }
        });

        requestQueue.add(request);
    }

    public void onItemClick(Post post) {
        //Show Popup to confirm post deletion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to delete this post?");

        // Add the Yes button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the Yes button click
                // Get session token
                SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
                String sessionToken = pref.getString("SESSION_TOKEN","");
                //Send delete post request
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority(getString(R.string.book_alley_api))
                        .appendPath("api")
                        .appendPath("Posts")
                        .appendPath(String.valueOf(post.Id))
                        .appendQueryParameter("sessionToken", sessionToken);
                String uri = builder.build().toString();
                Log.d("Uri", "handle delete post: " + uri);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, uri, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", "onResponse: " + response.toString());
                        Toast.makeText(MyPostActivity.this, "Delete post successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        getPosts();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Delete Post Error", "onErrorResponse: " + error.getMessage());
                        Toast.makeText(MyPostActivity.this, "Delete post failed. Please check your connection!", Toast.LENGTH_SHORT).show();
                    }
                });

                requestQueue.add(request);
            }
        });

        // Add the No button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the No button click
                dialog.dismiss(); // Dismiss the dialog

            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}