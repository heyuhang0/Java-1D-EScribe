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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

        // save previous video view states
        // and later will restore video in onResume()
        if (savedInstanceState != null) {
            videoPosition = savedInstanceState.getInt(STATE_VIDEO_POSITION);
            videoPlaying = savedInstanceState.getBoolean(STATE_VIDEO_PLAYING);
        } else {
            videoPosition = 1;  // used to show first frame as thumbnail
            videoPlaying = false;
        }

        // make video view full screen when landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).show();
        }

        // read Firebase path for this video from intent
        Intent intent = getIntent();
        String courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        String lessonName = intent.getStringExtra(Util.MESSAGE_LESSON_NAME);
        String slideIndex = intent.getStringExtra(Util.MESSAGE_SLIDE_INDEX);
        setTitle(lessonName);
        String slidePath = courseName + "/" + lessonName + "/processedVideos/" + slideIndex;

        // find views
        video = findViewById(R.id.video);
        TextView transcriptView = findViewById(R.id.transcript);
        TextView slideRecognitionView = findViewById(R.id.slide_recognition);

        // read Firebase and update views
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference(slidePath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProcessedVideo slide = dataSnapshot.getValue(ProcessedVideo.class);
                assert slide != null;

                transcriptView.setText(slide.getSpeechRecognition());
                slideRecognitionView.setText(slide.getSlidesRecognition());

                video.setVideoURI(Uri.parse(slide.getUrl()));

                resumeVideo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // setup media controller
        // media controller can not be set once video is ready and view size is confirmed
        // so listener is used to set the controller
        video.setOnPreparedListener(mp -> mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
            MediaController mc = new MediaController(DetailedVideoActivity.this);
            video.setMediaController(mc);
            mc.setAnchorView(video);
        }));

        // RecyclerView for comments
        List<String> comments = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.comments_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentsAdapter(comments);
        recyclerView.setAdapter(mAdapter);

        // Views to write and post comment
        TextView postCommentButton = findViewById(R.id.send_comment);
        EditText editCommentText = findViewById(R.id.edit_comment);

        DatabaseReference commentsRef = database.getReference(slidePath + "/comments");

        // Update "Post" button color to show whether the comment can be posted
        // (whether the comment editText is empty)
        editCommentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (editCommentText.getText().toString().isEmpty()) {
                    postCommentButton.setTextColor(getResources().getColor(R.color.colorUnfocused));
                } else {
                    postCommentButton.setTextColor(getResources().getColor(R.color.colorAccent));
                }
            }
        });

        // post comments -> push a new String to Firebase
        postCommentButton.setOnClickListener(v -> {
            String comment = editCommentText.getText().toString();

            if (!"".equals(comment)) {
                DatabaseReference newCommentRef = commentsRef.push();
                newCommentRef.setValue(comment);
            }
        });

        // insert new comments in Firebase to a list
        // and notify UI to update
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
    protected void onResume() {
        super.onResume();
        resumeVideo();
    }

    private void resumeVideo() {
        video.seekTo(videoPosition);
        if (videoPlaying) {
            video.start();
        }
    }

    @Override
    protected void onPause() {
        // video view states need to be saved before onPause()
        // otherwise the video will stop itself
        // so we save states in variables first, and later save them in onSaveInstanceState()
        saveVideoState();
        super.onPause();
    }

    private void saveVideoState() {
        videoPosition = video.getCurrentPosition();
        videoPlaying = video.isPlaying();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save video state so can continue playing after rotation or screen unlocking
        savedInstanceState.putBoolean(STATE_VIDEO_PLAYING, videoPlaying);
        savedInstanceState.putInt(STATE_VIDEO_POSITION, videoPosition);
    }

    // Adapter for comments recyclerView
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
