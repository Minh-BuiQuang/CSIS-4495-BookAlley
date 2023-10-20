package com.example.bookalleyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        validateSessionToken();
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
    }

    private void logout() {
        // Clear session token
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
        editor.apply();
        // Show login activity
        showSignInActivity();
    }
}