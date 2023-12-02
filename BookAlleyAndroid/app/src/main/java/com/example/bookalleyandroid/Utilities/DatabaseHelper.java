package com.example.bookalleyandroid.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.bookalleyandroid.Models.Conversation;
import com.example.bookalleyandroid.Models.Message;

import java.time.OffsetDateTime;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookAlley.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_CONVERSATION_TABLE =
            "CREATE TABLE Conversation ("
                    + "conversation_id LONG PRIMARY KEY, "
                    + "poster_name TEXT, "
                    + "poster_id LONG, "
                    + "finder_name TEXT, "
                    + "finder_id LONG)";

    private static final String CREATE_MESSAGE_TABLE =
            "CREATE TABLE Message ("
                    + "id LONG PRIMARY KEY, "
                    + "conversation_id LONG, "
                    + "content TEXT, "
                    + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "source TEXT, "
                    + "is_read BOOLEAN, "
                    + "FOREIGN KEY(conversation_id) REFERENCES Conversation(conversation_id))";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table
        db.execSQL(CREATE_CONVERSATION_TABLE);
        db.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Conversation");
        db.execSQL("DROP TABLE IF EXISTS Message");
        onCreate(db);
    }

    public boolean AddOrUpdateConversations(ArrayList<Conversation>conversations) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Conversation conversation : conversations) {
            db.execSQL("INSERT OR IGNORE INTO Conversation (conversation_id, poster_name, poster_id, finder_name, finder_id) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{conversation.Id, conversation.PosterName, conversation.PosterId, conversation.FinderName, conversation.FinderId});
            for (Message message : conversation.Messages) {
                db.execSQL("INSERT OR IGNORE INTO Message (id, conversation_id, content, timestamp, source, is_read) VALUES (?, ?, ?, ?, ?, ?)",
                        new Object[]{message.Id, message.ConversationId, message.Content, message.TimeStamp, message.Source, message.IsRead});
            }
        }
        return true;
    }

    public ArrayList<Conversation> GetConversationsByUserId(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Conversation> conversations = new ArrayList<>();
        String query = "SELECT * FROM Conversation WHERE poster_id = ? OR finder_id = ?";
        String[] args = new String[]{String.valueOf(userId), String.valueOf(userId)};
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                Conversation conversation = new Conversation();
                conversation.Id = cursor.getLong(0);
                conversation.PosterName = cursor.getString(1);
                conversation.PosterId = cursor.getLong(2);
                conversation.FinderName = cursor.getString(3);
                conversation.FinderId = cursor.getLong(4);
                conversation.Messages = GetMessagesByConversationId(conversation.Id);
                //Get Messages by conversation id
                conversations.add(conversation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return conversations;
    }

    public Conversation GetConversationById(long conversationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Conversation conversation = new Conversation();
        String query = "SELECT * FROM Conversation WHERE conversation_id = ?";
        String[] args = new String[]{String.valueOf(conversationId)};
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                conversation.Id = cursor.getLong(0);
                conversation.PosterName = cursor.getString(1);
                conversation.PosterId = cursor.getLong(2);
                conversation.FinderName = cursor.getString(3);
                conversation.FinderId = cursor.getLong(4);
                conversation.Messages = GetMessagesByConversationId(conversation.Id);
                //Get Messages by conversation id
            } while (cursor.moveToNext());
        }
        cursor.close();
        return conversation;
    }

    //Update messages read status
    public boolean UpdateMessagesReadStatus(long conversationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE Message SET is_read = 1 WHERE conversation_id = ?", new Object[]{conversationId});
        return true;
    }

    //Get all messages by conversation id
    private ArrayList<Message> GetMessagesByConversationId(long conversationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM Message WHERE conversation_id = ? ORDER BY timestamp DESC";
        String[] args = new String[]{String.valueOf(conversationId)};
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.Id = cursor.getLong(0);
                message.ConversationId = cursor.getLong(1);
                message.Content = cursor.getString(2);
                message.TimeStamp = OffsetDateTime.parse(cursor.getString(3));
                message.Source = cursor.getString(4);
                message.IsRead = cursor.getInt(5) == 1;
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }
}
