package com.example.escribe;

public class ProcessedVideo {
    private String key;
    private String url;
    private String slidesRecognition;
    private String speechRecognition;
    private String thumbnail;

    public String getUrl() {
        return url;
    }

    public String getSlidesRecognition() {
        return slidesRecognition;
    }

    public String getSpeechRecognition() {
        return speechRecognition;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
