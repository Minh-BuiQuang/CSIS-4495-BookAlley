package com.example.bookalleyandroid.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookalleyandroid.Activities.MessageActivity;
import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.R;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    ArrayList<Conversation> conversations;
    Context context;
    public ConversationAdapter(Context context, ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        this.context = context;
    }

    @NonNull
    @Override
    public ConversationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.conversation_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationAdapter.ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        String conversationId = conversation.Id;
        holder.nameTextView.setText(conversation.PosterName);
        holder.lastMessageTextView.setText(conversation.Messages.get(0).Content);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        String dateString = formatter.format(conversation.Messages.get(0).TimeStamp);
        holder.timeStampTextView.setText(dateString);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("CONVERSATION_ID", conversationId);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView nameTextView, lastMessageTextView, timeStampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameTextView = view.findViewById(R.id.nameTextView);
            lastMessageTextView = view.findViewById(R.id.messageTextView);
            timeStampTextView = view.findViewById(R.id.timeStampTextView);
        }
    }
}
