package com.watchfreemovies.freehdcinema786.Model;

public class FeedbackModel {
    private String feedbackDescription;
    private String feedEmail;
    private String feedbackBy;
    private String userName;

    public FeedbackModel() {
    }

    public String getFeedbackDescription() {
        return feedbackDescription;
    }

    public void setFeedbackDescription(String feedbackDescription) {
        this.feedbackDescription = feedbackDescription;
    }

    public String getFeedEmail() {
        return feedEmail;
    }

    public void setFeedEmail(String feedEmail) {
        this.feedEmail = feedEmail;
    }

    public String getFeedbackBy() {
        return feedbackBy;
    }

    public void setFeedbackBy(String feedbackBy) {
        this.feedbackBy = feedbackBy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
