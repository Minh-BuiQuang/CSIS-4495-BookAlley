package com.example.bookalleyandroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookalleyandroid.Models.Book;
import com.example.bookalleyandroid.R;
import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    ArrayList<Book> books;
    Context context;
    private OnItemClickListener onItemClickListener;
    public BookAdapter(Context context, ArrayList<Book> books, OnItemClickListener onItemClickListener) {
        this.books = books;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdapter.ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleTextView.setText("Title: " + book.Title);
        holder.authorTextView.setText("Author: " + book.Author);
        holder.isbnTextView.setText("ISBN: " + book.Isbn);
        String imagePath = book.Image;
        if(!imagePath.contains("https")) {
            imagePath = imagePath.replace("http", "https");
        }
        Picasso.get().load(imagePath).resize(400, 400)
                .centerInside().into(holder.coverImageView);

        holder.posterTextView.setVisibility(View.GONE);
        holder.locationTextView.setVisibility(View.GONE);
        holder.timeTextView.setVisibility(View.GONE);
        holder.noteTextView.setVisibility(View.GONE);

        holder.view.setOnClickListener(v -> {
            onItemClickListener.onItemClick(book);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView titleTextView, authorTextView, noteTextView, isbnTextView, locationTextView, posterTextView, timeTextView;
        ImageView coverImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            posterTextView = view.findViewById(R.id.posterTextView);
            locationTextView = view.findViewById(R.id.bookLocationTextView);
            timeTextView = view.findViewById(R.id.postTimeTextView);
            titleTextView = view.findViewById(R.id.titleTextView);
            authorTextView = view.findViewById(R.id.authorTextView);
            isbnTextView = view.findViewById(R.id.isbnTextView);
            noteTextView = view.findViewById(R.id.noteTextView);
            coverImageView = view.findViewById(R.id.coverImageView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Book selectedItem);
    }
}
