package com.example.escribe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LessonListActivity extends AppCompatActivity {
    private String courseName;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        Intent intent = getIntent();
        courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        setTitle(courseName);

        List<String> lessonList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.lesson_list_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new LessonListAdapter(lessonList);
        recyclerView.setAdapter(mAdapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(courseName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String lessonName = dataSnapshot.getKey();
                assert lessonName != null;

                // key starts with _ is used for other purpose
                if (lessonName.startsWith("_")) {
                    return;
                }

                lessonList.add(lessonName);
                mAdapter.notifyItemInserted(lessonList.size() - 1);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.MyViewHolder> {

        private List<String> lessonList;

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.lesson_item_text);
                v.setOnClickListener(view -> {
                    String lessonName = textView.getText().toString();

                    Intent intent = new Intent(LessonListActivity.this, LessonActivity.class);
                    intent.putExtra(Util.MESSAGE_COURSE_NAME, courseName);
                    intent.putExtra(Util.MESSAGE_LESSON_NAME, lessonName);
                    startActivity(intent);
                });
            }
        }

        LessonListAdapter(List<String> lessonList) {
            this.lessonList = lessonList;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lesson_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.textView.setText(lessonList.get(position));
        }

        @Override
        public int getItemCount() {
            return lessonList.size();
        }
    }
}
