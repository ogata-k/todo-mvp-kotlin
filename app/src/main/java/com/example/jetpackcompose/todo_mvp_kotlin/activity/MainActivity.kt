package com.example.jetpackcompose.todo_mvp_kotlin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpackcompose.todo_mvp_kotlin.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}