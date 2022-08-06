package com.example.findthestatue.interfaces

import com.example.findthestatue.models.Statue

interface FirebaseCallback {
    fun onResponse(statue: Statue?)
}