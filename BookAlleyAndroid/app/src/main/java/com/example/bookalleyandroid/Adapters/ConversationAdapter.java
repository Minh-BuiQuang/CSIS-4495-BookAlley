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

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.prefs.PreferenceChangeEvent;

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
        long conversationId = conversation.Id;
        //Set conversation name
        //Get userid from shared preference
        long userId = context.getSharedPreferences(context.getString(R.string.preference_key), Context.MODE_PRIVATE).getLong("USER_ID", -1);
        if (userId == conversation.PosterId) {
            holder.nameTextView.setText(conversation.FinderName);
        } else {
            holder.nameTextView.setText(conversation.PosterName);
        }
        //Bind message content
        holder.lastMessageTextView.setText(conversation.Messages.get(0).Content);
        //Convert timestamp to local time
        OffsetDateTime timestamp = conversation.Messages.get(0).TimeStamp;
        ZoneId localZone = ZoneId.systemDefault();
        ZonedDateTime localTime = timestamp.atZoneSameInstant(localZone);
        //Format and display timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm • MMM dd yyyy");
        String dateString = formatter.format(localTime);
        holder.timeStampTextView.setText(dateString);

        //handle unread badge
        int unreadCount = 0;
        for (int i = 0; i < conversation.Messages.size(); i++) {
            if (!conversation.Messages.get(i).IsRead) {
                unreadCount++;
            }
        }
        if (unreadCount > 0) {
            holder.badgeTextView.setVisibility(View.VISIBLE);
            holder.badgeTextView.setText(String.valueOf(unreadCount));
        } else {
            holder.badgeTextView.setVisibility(View.GONE);
        }
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
        TextView nameTextView, lastMessageTextView, timeStampTextView, badgeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            nameTextView = view.findViewById(R.id.nameTextView);
            lastMessageTextView = view.findViewById(R.id.messageTextView);
            timeStampTextView = view.findViewById(R.id.timeStampTextView);
            badgeTextView = view.findViewById(R.id.badgeTextView);
        }
    }
}
