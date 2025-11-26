package com.example.tralalero.test;

import android.util.Log;

import com.example.tralalero.data.remote.dto.task.TaskDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonTest {
    
    public static void testParsing() {
        String json = "{\n" +
            "  \"id\": \"e06383c4-c95f-4a53-95d5-7188a516fa27\",\n" +
            "  \"title\": \"Huhu\",\n" +
            "  \"task_labels\": [\n" +
            "    {\n" +
            "      \"task_id\": \"e06383c4-c95f-4a53-95d5-7188a516fa27\",\n" +
            "      \"label_id\": \"8d582be8-4fb4-482a-834e-407f773f7638\",\n" +
            "      \"labels\": {\n" +
            "        \"id\": \"8d582be8-4fb4-482a-834e-407f773f7638\",\n" +
            "        \"name\": \"todo\",\n" +
            "        \"color\": \"#F59E0B\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"task_assignees\": [\n" +
            "    {\n" +
            "      \"task_id\": \"e06383c4-c95f-4a53-95d5-7188a516fa27\",\n" +
            "      \"user_id\": \"357d584e-bb82-4021-b41d-c058fdeab458\",\n" +
            "      \"users\": {\n" +
            "        \"id\": \"357d584e-bb82-4021-b41d-c058fdeab458\",\n" +
            "        \"name\": \"sa sa ko\",\n" +
            "        \"email\": \"kuroyami166@gmail.com\",\n" +
            "        \"avatar_url\": \"https://lh3.googleusercontent.com/...\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        Gson gson = new GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create();
        
        Log.d("GsonTest", "Parsing JSON...");
        TaskDTO dto = gson.fromJson(json, TaskDTO.class);
        
        Log.d("GsonTest", "Title: " + dto.getTitle());
        Log.d("GsonTest", "task_labels: " + dto.getTaskLabels());
        Log.d("GsonTest", "task_assignees: " + dto.getTaskAssignees());
        
        if (dto.getTaskLabels() != null) {
            Log.d("GsonTest", "Labels count: " + dto.getTaskLabels().size());
        }
        if (dto.getTaskAssignees() != null) {
            Log.d("GsonTest", "Assignees count: " + dto.getTaskAssignees().size());
        }
    }
}
