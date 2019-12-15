package com.example.escribe;

/**
 * Class used to map processed video data in Firebase
 * Instantiated by dataSnapshot.getValue in Firebase API
 */
public class ProcessedVideo {
    private String key;
    private String url;
    private String slidesRecognition;
    private String speechRecognition;
    private String thumbnail;

    /**
     * @return public URL of processed video file
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return slide recognition (OCR) result
     */
    public String getSlidesRecognition() {
        return slidesRecognition;
    }

    /**
     * @return speech recognition result of this video
     */
    public String getSpeechRecognition() {
        return speechRecognition;
    }

    /**
     * @return URL of this video's thumbnail image
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * @return corresponding key in Firebase
     */
    public String getKey() {
        return key;
    }

    /** Set video key
     * @param key key of the Firebase snapshot
     */
    public void setKey(String key) {
        this.key = key;
    }
}
