package com.example.bookalleyandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.bookalleyandroid.R;
import com.example.bookalleyandroid.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}