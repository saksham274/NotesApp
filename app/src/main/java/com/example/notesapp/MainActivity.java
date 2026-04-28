package com.example.notesapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText noteInput;
    Button saveBtn;
    ListView listView;

    DatabaseReference db;

    ArrayList<Note> noteList;
    ArrayList<String> displayList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteInput = findViewById(R.id.noteInput);
        saveBtn = findViewById(R.id.saveBtn);
        listView = findViewById(R.id.listView);

        db = FirebaseDatabase.getInstance().getReference("Notes");

        noteList = new ArrayList<>();
        displayList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        // SAVE NOTE
        saveBtn.setOnClickListener(v -> {
            String text = noteInput.getText().toString();
            if (!text.isEmpty()) {
                String id = db.push().getKey();
                Note note = new Note(id, text);
                db.child(id).setValue(note);
                noteInput.setText("");
            }
        });

        // READ NOTES
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                noteList.clear();
                displayList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Note note = data.getValue(Note.class);

                    if (note != null && note.text != null) {
                        noteList.add(note);
                        displayList.add(note.text);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // DELETE (LONG CLICK)
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Note note = noteList.get(position);
            db.child(note.id).removeValue();
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            return true;
        });

        // EDIT (CLICK)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Note note = noteList.get(position);

            EditText input = new EditText(this);
            input.setText(note.text);

            new AlertDialog.Builder(this)
                    .setTitle("Edit Note")
                    .setView(input)
                    .setPositiveButton("Update", (dialog, which) -> {
                        String newText = input.getText().toString();
                        db.child(note.id).child("text").setValue(newText);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button logoutBtn = findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }
}