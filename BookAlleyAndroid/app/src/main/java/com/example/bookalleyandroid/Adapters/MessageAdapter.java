package com.example.bookalleyandroid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;
import com.example.bookalleyandroid.R;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    Conversation conversation;
    Context context;
    boolean isPoster;
    public MessageAdapter(Context context,  Conversation conversation, boolean isPoster) {
        this.conversation = conversation;
        this.context = context;
        this.isPoster = isPoster;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate outgoing message layout
        if(viewType == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_message_line, parent, false);
            return new ViewHolder(view);
        }
        //Inflate incoming message layout
        else if(viewType == 2) {
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_message_line, parent, false);
            return new ViewHolder(view);
        }
        //Inflate system message layout
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.system_message_line, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = conversation.Messages.get(position);
        holder.messageTextView.setText(message.Content);
        if(!message.Source.equals("system")) {
            //Convert timestamp to local time
            OffsetDateTime timestamp = message.TimeStamp;
            ZoneId localZone = ZoneId.systemDefault();
            ZonedDateTime localTime = timestamp.atZoneSameInstant(localZone);
            //Format and display timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm • MMM dd yyyy");
            String dateString = formatter.format(localTime);
            holder.timeStampTextView.setText(dateString);
        }
    }

    @Override
    public int getItemCount() {
        return conversation.Messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = conversation.Messages.get(position);
        if((message.Source.equals("poster") && isPoster)
                || (message.Source.equals("finder") && !isPoster))
            return 1; //Outgoing message
        else if((message.Source.equals("poster") && !isPoster)
                || (message.Source.equals("finder") && isPoster))
                return 2; //Incoming message
        else
            return 0; //System message
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeStampTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageLineTextView);
            timeStampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
