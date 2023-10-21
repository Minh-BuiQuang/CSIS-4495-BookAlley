package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivitySignInBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    private RequestQueue requestQueue;
    boolean isSignUp = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        showSignUpMenu(isSignUp);
        binding.modeSwitchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSignUp = !isSignUp;
                showSignUpMenu(isSignUp);
            }
        });
        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    handleSignUp();
                } else {
                    handleSignIn();
                }
            }
        });
    }

    private void handleSignUp() {
        //Validate input
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String passwordConfirm = binding.passwordConfirmEditText.getText().toString();
        String name = binding.nameEditText.getText().toString();

        if(email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() || name.isEmpty()){
            binding.errorMessageTextView.setText("Please fill in all fields");
        } else if(!password.equals(passwordConfirm)) {
            binding.errorMessageTextView.setText("Passwords do not match");
        } else if(password.length() < 6){
            binding.errorMessageTextView.setText("Password must be at least 6 characters");
        } else if(!email.contains("@")){
            binding.errorMessageTextView.setText("Invalid email");
        } else {
            binding.errorMessageTextView.setText("");
            //Send request
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(getString(R.string.book_alley_api))
                    .appendPath("api")
                    .appendPath("Users");
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            body.put("name", name);
            String uri = builder.build().toString();
            Log.d("Uri", "handleSignUp: " + uri);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, new JSONObject(body), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Sign Up Request", "onResponse: " + response.toString());
                    try {
                        //Print result message
                        String message = response.getString("message");
                        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();

                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.nameEditText.getWindowToken(), 0);
                        //Switch to sign in view
                        isSignUp = false;
                        showSignUpMenu(false);

                    } catch (JSONException e) {
                        Log.e("Sign Up Request", "onResponse: ", e);
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        String message = new String(error.networkResponse.data, "utf-8");
                        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
                            Log.d("Sign Up Request", "onErrorResponse: " + message);
                    } catch (Exception e) {
                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("Sign Up Request", "onErrorResponse: " + e.getMessage());
                    }
                }
            });
            requestQueue.add(request);
        }
    }
    private void handleSignIn() {
        //Validate input
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        if(!email.contains("@")) {
            binding.errorMessageTextView.setText("Invalid email");
        } else if(password.isEmpty()) {
            binding.errorMessageTextView.setText("Please enter password");
        }
        //Send request
            Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(getString(R.string.book_alley_api))
                .appendPath("api")
                .appendPath("Users")
                .appendPath("SignIn");
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        String uri = builder.build().toString();
        Log.d("Uri", "handleSignIn: " + uri);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, new JSONObject(body), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Sign In Request", "onResponse: " + response.toString());
                try {
                    //Store sessionToken
                    String sessionToken = response.getString("sessionToken");
                    SharedPreferences pref = getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("SESSION_TOKEN",sessionToken);
                    editor.apply();
                    //Go to main activity
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));

                } catch (JSONException e) {
                    Log.e("Sign In Request", "onResponse: ", e);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String message = new String(error.networkResponse.data, "utf-8");
                    JSONObject response = new JSONObject(message);
                    int status = response.getInt("status");
                    if(status == 404) {
                        Toast.makeText(SignInActivity.this, "Invalid user name or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Error when signing in", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Sign In Request", "onErrorResponse: " + message);
                } catch (Exception e) {
                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Sign In Request", "onErrorResponse: " + e.getMessage());
                }
            }
        });
        requestQueue.add(request);
    }


    void showSignUpMenu(boolean show){
        if(show){
            binding.nameEditText.setVisibility(View.VISIBLE);
            binding.nameEditText.setEnabled(true);
            binding.passwordConfirmEditText.setVisibility(View.VISIBLE);
            binding.passwordConfirmEditText.setEnabled(true);
            binding.confirmButton.setText("Sign Up");
            binding.modeSwitchTextView.setText("Already have an account? Sign In");

            binding.nameEditText.setText("");
            binding.passwordEditText.setText("");
            binding.passwordConfirmEditText.setText("");
            binding.emailEditText.setText("");
        }else{
            binding.nameEditText.setVisibility(View.GONE);
            binding.nameEditText.setEnabled(false);
            binding.passwordConfirmEditText.setVisibility(View.GONE);
            binding.passwordConfirmEditText.setEnabled(false);
            binding.confirmButton.setText("Sign In");
            binding.modeSwitchTextView.setText("Don't have an account? Sign Up");

            binding.nameEditText.setText("");
            binding.passwordEditText.setText("");
            binding.passwordConfirmEditText.setText("");
            binding.emailEditText.setText("");
        }

    }
}