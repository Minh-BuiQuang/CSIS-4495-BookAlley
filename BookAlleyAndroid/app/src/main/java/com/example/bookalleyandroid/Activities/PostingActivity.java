package com.example.bookalleyandroid.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.bookalleyandroid.Adapters.BookAdapter;
import com.example.bookalleyandroid.Models.Book;
import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.Utilities.Constance;
import com.example.bookalleyandroid.Utilities.VolleySingleton;
import com.example.bookalleyandroid.databinding.ActivityPostingBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostingActivity extends AppCompatActivity implements BookAdapter.OnItemClickListener{

    ActivityPostingBinding binding;
    private RequestQueue requestQueue;
    private Book selectedBook;
    private PopupWindow popupWindow;
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

        binding.searchBookButton.setOnClickListener(v -> {
            String keyword = binding.titleEditText.getText().toString();
            //Skip searching if keyword is too short to avoid wasting API quota
            if(keyword.length() < 4) return;

            //Send book search request
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(getString(R.string.google_book_api))
                    .appendPath("books")
                    .appendPath("v1")
                    .appendPath("volumes")
                    .appendQueryParameter("q", "intitle:" + keyword)
                    .appendQueryParameter("key",getString(R.string.google_book_api_key));
            String uri = builder.build().toString();
            Log.d("Uri", "search Google Book API by title: " + uri);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Book search response", "onResponse: " + response.toString());
                    try {
                        JSONArray items = response.getJSONArray("items");
                        ArrayList<Book> resultBooks = new ArrayList<>();
//                                books.clear();
                        for(int i = 0; i < items.length(); i++) {
                            //Retrieve book info from search result
                            JSONObject book = items.getJSONObject(i);
                            try {
                                String title = book.getJSONObject("volumeInfo").getString("title");
                                String isbn = book.getJSONObject("volumeInfo").getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
                                String author = book.getJSONObject("volumeInfo").getJSONArray("authors").getString(0);
                                String image = book.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
                                //Add book to result list
                                Book bookResult = new Book();
                                bookResult.Title = title;
                                bookResult.Isbn = isbn;
                                bookResult.Author = author;
                                bookResult.Image = image;
                                resultBooks.add(bookResult);
                            } catch (JSONException e) {
                                Log.e("Book Parsing error", "onResponse: ", e);
                            }
                        }
                        View popupView = LayoutInflater.from(PostingActivity.this).inflate(R.layout.book_popup_layout, null);
                        RecyclerView popupRecyclerView = popupView.findViewById(R.id.bookSearchResultRecyclerView);
                        popupRecyclerView.setLayoutManager(new LinearLayoutManager(PostingActivity.this));
                        BookAdapter adapter = new BookAdapter(PostingActivity.this, resultBooks, PostingActivity.this);
                        popupRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                        popupWindow.showAtLocation(binding.bookAdapterLayout, Gravity.CENTER, 0, 0);
                    } catch (JSONException e) {
                        Log.e("Book Search Request", "onResponse: ", e);
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        String message = new String(error.networkResponse.data, "utf-8");
                        Log.d("Book Search Request", "onErrorResponse: " + message);
                    } catch (Exception e) {
                        Log.d("Book Search Request", "onErrorResponse: " + e.getMessage());
                    }
                }
            });
            requestQueue.add(request);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_post_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.postButton){
            //Validate input
            if(binding.titleEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return true;
            }
            if(binding.cityTextView.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter your location", Toast.LENGTH_SHORT).show();
                return true;
            }

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
            //Prefer data from search result book
            if(selectedBook != null) {
                body.put("image", selectedBook.Image);
                body.put("author", selectedBook.Author);
                body.put("isbn", selectedBook.Isbn);
                body.put("bookTitle", selectedBook.Title);
            } else {
                body.put("author", binding.authorEditText.getText().toString());
                body.put("isbn", binding.isbnEditText.getText().toString());
                body.put("bookTitle", binding.titleEditText.getText().toString());
            }

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

    @Override
    public void onItemClick(Book selectedItem) {
        binding.titleEditText.setText(selectedItem.Title);
        binding.authorEditText.setText(selectedItem.Author);
        binding.isbnEditText.setText(selectedItem.Isbn);
        selectedBook = selectedItem;
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}