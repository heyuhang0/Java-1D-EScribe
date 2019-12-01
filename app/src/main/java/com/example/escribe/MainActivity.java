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

public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> courseList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.course_list_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CourseListAdapter(courseList);
        recyclerView.setAdapter(mAdapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String courseName = dataSnapshot.getKey();
                assert courseName != null;

                // key starts with _ is used for other purpose
                if (courseName.startsWith("_")) {
                    return;
                }

                courseList.add(courseName);
                mAdapter.notifyItemInserted(courseList.size() - 1);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.MyViewHolder> {

        private List<String> courseList;

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.course_item_text);
                v.setOnClickListener(view -> {
                    Intent intent = new Intent(MainActivity.this, LessonListActivity.class);
                    String message = textView.getText().toString();
                    intent.putExtra(Util.MESSAGE_COURSE_NAME, message);
                    startActivity(intent);
                });
            }
        }

        CourseListAdapter(List<String> courseList) {
            this.courseList = courseList;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.course_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.textView.setText(courseList.get(position));
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }
    }

}