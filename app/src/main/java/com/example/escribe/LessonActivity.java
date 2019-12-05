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
import java.util.Objects;

public class LessonActivity extends AppCompatActivity {

    private SlideAdapter mAdapter;
    private String courseName;
    private String lessonName;
    List<ProcessedVideo> slideList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Intent intent = getIntent();
        courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        lessonName = intent.getStringExtra(Util.MESSAGE_LESSON_NAME);
        setTitle(lessonName);
        String coursePath = courseName + "/" + lessonName;

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
                ProcessedVideo video = dataSnapshot.getValue(ProcessedVideo.class);
                if (video != null) {
                    video.setKey(dataSnapshot.getKey());
                    slideList.add(video);
                    mAdapter.notifyItemInserted(slideList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                int index = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getKey()));
                ProcessedVideo updatedSlide = dataSnapshot.getValue(ProcessedVideo.class);
                slideList.set(index, updatedSlide);
                mAdapter.notifyItemChanged(index);
            }

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
            String index;

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

        void setSlideList(List<ProcessedVideo> slideList) {
            this.slideList = slideList;
            notifyDataSetChanged();
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

            holder.index = slide.getKey();
            holder.transcriptTextView.setText(
                    (slide.getSpeechRecognition().trim() + "\n" + slide.getSlidesRecognition()).trim());
            String videoThumbnailPath = slide.getThumbnail();
            if (videoThumbnailPath != null && !videoThumbnailPath.isEmpty()) {
                Picasso.get().load(videoThumbnailPath).into(holder.videoPreview);
            }
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

        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newQueryText) {
                if (newQueryText.isEmpty()) {
                    mAdapter.setSlideList(slideList);
                } else {
                    List<ProcessedVideo> filteredSlides = new ArrayList<>();
                    for (ProcessedVideo v: slideList) {
                        newQueryText = newQueryText.toLowerCase();
                        String transcript = v.getSpeechRecognition();
                        String notes = v.getSlidesRecognition();
                        if (transcript != null && transcript.toLowerCase().contains(newQueryText)) {
                            filteredSlides.add(v);
                        } else if (notes != null && notes.toLowerCase().contains(newQueryText)) {
                            filteredSlides.add(v);
                        }
                    }
                    mAdapter.setSlideList(filteredSlides);
                }
                return false;
            }
        });

        return true;
    }
}
