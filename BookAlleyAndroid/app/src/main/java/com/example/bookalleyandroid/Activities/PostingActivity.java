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

import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.databinding.ActivityPostingBinding;

public class PostingActivity extends AppCompatActivity {

    ActivityPostingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Create a book posting");
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
                    .appendPath("Users")
                    .appendPath("SignOut")
                    .appendQueryParameter("sessionToken", sessionToken);
            String uri = builder.build().toString();
            Log.d("Uri", "handle create post: " + uri);
            finish();
        }
        return true;
    }
}