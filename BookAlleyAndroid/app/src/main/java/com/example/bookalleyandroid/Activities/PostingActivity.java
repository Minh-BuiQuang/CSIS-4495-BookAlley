package com.example.bookalleyandroid.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.Constance;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityPostingBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostingActivity extends AppCompatActivity {

    ActivityPostingBinding binding;
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Create a book posting");
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, Constance.Cities);
        binding.cityTextView.setThreshold(1);
        binding.cityTextView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_post_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.postButton){
            //Handle sending the post
            SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
            String sessionToken = pref.getString("SESSION_TOKEN","");

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(getString(R.string.book_alley_api))
                    .appendPath("api")
                    .appendPath("Posts")
                    .appendQueryParameter("sessionToken", sessionToken);

            Map<String, String> body = new HashMap<>();
            body.put("author", binding.authorEditText.getText().toString());
            body.put("isbn", binding.isbnEditText.getText().toString());
            body.put("bookTitle", binding.titleEditText.getText().toString());
//            body.put("image", image);
            body.put("location", binding.cityTextView.getText().toString());
            body.put("note", binding.noteEditText.getText().toString());
            String uri = builder.build().toString();
            Log.d("Uri", "handle create post: " + uri);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, new JSONObject(body), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Sign Up Request", "onResponse: " + response.toString());
                    try {
                        //Print result message
                        String message = response.getString("message");
                        Toast.makeText(PostingActivity.this, message, Toast.LENGTH_SHORT).show();
                        //Close activity
                        finish();
                    } catch (JSONException e) {
                        Log.e("Sign Up Request", "onResponse: ", e);
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        String message = new String(error.networkResponse.data, "utf-8");
                        Toast.makeText(PostingActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.d("Book Posting Request", "onErrorResponse: " + message);
                    } catch (Exception e) {
                        Toast.makeText(PostingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("Book Posting Request", "onErrorResponse: " + e.getMessage());
                    }
                }
            });
            requestQueue.add(request);
        }
        return true;
    }
}