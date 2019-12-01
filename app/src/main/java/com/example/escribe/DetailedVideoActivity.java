package com.example.escribe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetailedVideoActivity extends AppCompatActivity {

    private VideoView video;

    private static final String STATE_VIDEO_PLAYING = "video_playing";
    private static final String STATE_VIDEO_POSITION = "video_position";
    private boolean videoPlaying = false;
    private int videoPosition = 1;

    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_video);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).show();
        }

        Intent intent = getIntent();
        String courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        String lessonName = intent.getStringExtra(Util.MESSAGE_LESSON_NAME);
        int slideIndex = intent.getIntExtra(Util.MESSAGE_SLIDE_INDEX, 0);
        setTitle(lessonName);
        String slidePath = courseName + "/" + lessonName + "/processedVideos/" + slideIndex;

        video = findViewById(R.id.video);
        TextView transcriptView = findViewById(R.id.transcript);
        TextView slideRecognitionView = findViewById(R.id.slide_recognition);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference(slidePath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProcessedVideo slide = dataSnapshot.getValue(ProcessedVideo.class);
                assert slide != null;

                transcriptView.setText(slide.getSpeechRecognition());
                slideRecognitionView.setText(slide.getSlidesRecognition());

                video.setVideoURI(Uri.parse(slide.getUrl()));

                if (savedInstanceState != null) {
                    video.seekTo(savedInstanceState.getInt(STATE_VIDEO_POSITION));
                    if (savedInstanceState.getBoolean(STATE_VIDEO_PLAYING)) {
                        video.start();
                    }
                } else {
                    video.seekTo(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        video.setOnPreparedListener(mp -> mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
            MediaController mc = new MediaController(DetailedVideoActivity.this);
            video.setMediaController(mc);
            mc.setAnchorView(video);
        }));

        List<String> comments = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.comments_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentsAdapter(comments);
        recyclerView.setAdapter(mAdapter);

        Button postCommentButton = findViewById(R.id.send_comment);
        EditText editCommentText = findViewById(R.id.edit_comment);

        DatabaseReference commentsRef = database.getReference(slidePath + "/comments");

        postCommentButton.setOnClickListener(v -> {
            String comment = editCommentText.getText().toString();

            DatabaseReference newCommentRef = commentsRef.push();
            newCommentRef.setValue(comment);
        });

        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String comment = (String) dataSnapshot.getValue();
                comments.add(comment);
                editCommentText.setText("");
                mAdapter.notifyItemInserted(comments.size() - 1);
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onPause() {
        videoPosition = video.getCurrentPosition();
        videoPlaying = video.isPlaying();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_VIDEO_PLAYING, videoPlaying);
        savedInstanceState.putInt(STATE_VIDEO_POSITION, videoPosition);
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

        private List<String> comments;

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.comment_item_text);
            }
        }

        CommentsAdapter(List<String> comments) {
            this.comments = comments;
        }


        @NonNull
        @Override
        public CommentsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsAdapter.MyViewHolder holder, int position) {
            holder.textView.setText(comments.get(position));
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }
    }
}
