package com.example.escribe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LessonActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private String courseName;
    private String lessonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Intent intent = getIntent();
        courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        lessonName = intent.getStringExtra(Util.MESSAGE_LESSON_NAME);
        setTitle(lessonName);
        String coursePath = courseName + "/" + lessonName;

        List<ProcessedVideo> slideList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.slide_list_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new SlideAdapter(slideList);
        recyclerView.setAdapter(mAdapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(coursePath + "/processedVideos")
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                slideList.add(dataSnapshot.getValue(ProcessedVideo.class));
                mAdapter.notifyItemInserted(slideList.size() - 1);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


    class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.MyViewHolder> {

        private List<ProcessedVideo> slideList;

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView videoPreview;
            TextView transcriptTextView;
            int index;

            MyViewHolder(View view) {
                super(view);
                videoPreview = view.findViewById(R.id.videoPreview);
                transcriptTextView = view.findViewById(R.id.transcript);
                view.setOnClickListener(v -> {
                    Intent intent = new Intent(LessonActivity.this, DetailedVideoActivity.class);
                    intent.putExtra(Util.MESSAGE_COURSE_NAME, courseName);
                    intent.putExtra(Util.MESSAGE_LESSON_NAME, lessonName);
                    intent.putExtra(Util.MESSAGE_SLIDE_INDEX, index);
                    startActivity(intent);
                });
            }
        }

        SlideAdapter(List<ProcessedVideo> slideList) {
            this.slideList = slideList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.slide_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ProcessedVideo slide = slideList.get(position);

            holder.index = position;
            holder.transcriptTextView.setText(slide.getSpeechRecognition());
            Picasso.get().load(slide.getThumbnail()).into(holder.videoPreview);
        }

        @Override
        public int getItemCount() {
            return slideList.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Assumes current activity is the searchable activity
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i("LessonActivity", "query:" + query);
        }
    }
}
