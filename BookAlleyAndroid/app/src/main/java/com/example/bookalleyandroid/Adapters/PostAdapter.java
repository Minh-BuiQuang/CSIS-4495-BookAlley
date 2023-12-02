package com.example.bookalleyandroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookalleyandroid.Models.Book;
import com.example.bookalleyandroid.Models.Post;
import com.example.bookalleyandroid.R;
import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<Post> posts;
    Context context;
    private PostAdapter.OnItemClickListener onItemClickListener;

    public PostAdapter(Context context, ArrayList<Post> posts, OnItemClickListener onItemClickListener) {
        this.posts = posts;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.titleTextView.setText("Title: " + post.BookTitle);
        holder.authorTextView.setText("Author: " + post.Author);
        holder.isbnTextView.setText("ISBN: " + post.ISBN);
        holder.noteTextView.setText("\'" + post.Note + "\'");
        holder.locationTextView.setText(post.Location);
        holder.posterTextView.setText(post.PosterName);

        ZonedDateTime zonedDateTime = post.DatePosted.atZoneSameInstant(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("• HH:mm • dd MMMM yyyy", Locale.getDefault());
        String formattedLocalDateTime = zonedDateTime.format(formatter);
        holder.timeTextView.setText(formattedLocalDateTime);

        String imagePath = post.Image;
        if(!imagePath.contains("https")) {
            imagePath = imagePath.replace("http", "https");
        }
        Picasso.get().load(imagePath ).resize(400, 400)
                .centerInside().into(holder.coverImageView);
        holder.view.setOnClickListener(v -> {
            onItemClickListener.onItemClick(post);
        });

        if(post.Quantity > 0) {
            holder.posterTextView.setText("Shared Times: " + post.Quantity);
            holder.locationTextView.setVisibility(View.GONE);
            holder.timeTextView.setVisibility(View.GONE);
            holder.noteTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
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
        void onItemClick(Post selectedItem);
    }
}
