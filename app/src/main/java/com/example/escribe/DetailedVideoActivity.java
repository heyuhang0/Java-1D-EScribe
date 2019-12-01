package com.example.escribe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DetailedVideoActivity extends AppCompatActivity {

    private VideoView video;

    private static final String STATE_VIDEO_PLAYING = "video_playing";
    private static final String STATE_VIDEO_POSITION = "video_position";
    private boolean videoPlaying = false;
    private int videoPosition = 1;

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

        FirebaseDatabase.getInstance().getReference(slidePath).addValueEventListener(new ValueEventListener() {
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
}
