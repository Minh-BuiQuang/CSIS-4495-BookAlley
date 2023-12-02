package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.bookalleyandroid.Adapters.PostAdapter;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.Constance;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityStatisticBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.example.bookalleyandroid.Utilities.Utilities.GroupPostByISBN;
import static com.example.bookalleyandroid.Utilities.Utilities.SortPostByQuantity;

public class StatisticActivity extends AppCompatActivity {
    ActivityStatisticBinding binding;
    ArrayList<Post> groupedPosts = new ArrayList<>();
    ArrayList<Post> allTimePosts = new ArrayList<>();
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        binding.allTimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Set activity title
        getSupportActionBar().setTitle("Popular Books");


        //Get posts from backend and populate all time book list
        Uri.Builder allTimePostBuilder = new Uri.Builder();
        allTimePostBuilder.scheme("https")
                .authority(getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Posts")
                .appendQueryParameter("isStatisticRequest", "true");


        String allTimePostUri = allTimePostBuilder.build().toString();
        Log.d("Uri", "get book postings: " + allTimePostUri);
        JsonArrayRequest allTimePostRequest = new JsonArrayRequest(Request.Method.GET, allTimePostUri, null, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", "onResponse: " + response.toString());
                allTimePosts.clear();
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
                        allTimePosts.add(post);
                    } catch (JSONException e) {
                        Log.e("Parsing Post error", "onResponse: " + e.getMessage());
                    }
                }
                groupedPosts = GroupPostByISBN(allTimePosts);
                SortPostByQuantity(groupedPosts);
                PostAdapter adapter = new PostAdapter(StatisticActivity.this, groupedPosts, null);
                binding.allTimeRecyclerView.setAdapter(adapter);
                binding.totalPostTextView.setText(String.valueOf("Total Book Shared: " + allTimePosts.size()));
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String message = new String(error.networkResponse.data, "utf-8");
                    Toast.makeText(StatisticActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + message);
                } catch (Exception e) {
                    Toast.makeText(StatisticActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Get Book Posting", "onErrorResponse: " + e.getMessage());
                }
            }
        });

        requestQueue.add(allTimePostRequest);

        binding.locationTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(binding.locationTextView.getText().toString().isEmpty()) {
                    binding.totalLocalPostTextView.setText("Total Local Book Shared:");
                } else {
                    ArrayList<Post> localPosts = new ArrayList<>();
                    for(Post post : allTimePosts){
                        if(post.Location.equals(binding.locationTextView.getText().toString())){
                            localPosts.add(post);
                        }
                    }
                    binding.totalLocalPostTextView.setText(String.valueOf("Total Local Book Shared: " + localPosts.size()));
                }

            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, Constance.Cities);
        binding.locationTextView.setThreshold(1);
        binding.locationTextView.setAdapter(adapter);
    }

}