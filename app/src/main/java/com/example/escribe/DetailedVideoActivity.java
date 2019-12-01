package com.example.escribe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailedVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_video);

        Intent intent = getIntent();
        String courseName = intent.getStringExtra(Util.MESSAGE_COURSE_NAME);
        String lessonName = intent.getStringExtra(Util.MESSAGE_LESSON_NAME);
        int slideIndex = intent.getIntExtra(Util.MESSAGE_SLIDE_INDEX, 0);
        setTitle(lessonName);
        String slidePath = courseName + "/" + lessonName + "/processedVideos/" + slideIndex;

        VideoView video = findViewById(R.id.video);
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
                video.seekTo(1);
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
}
