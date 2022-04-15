package com.watchfreemovies.freehdcinema786.Model;

public class NotificationsModel {

    private String notificationBy;
    private long notificationAt;
    private String type;
    private String postId;
    private String notificationId;
    private String postedBy;
    private String commentedText;
    private boolean checkOpen;

    public NotificationsModel() {
    }

    public String getNotificationBy() {
        return notificationBy;
    }

    public void setNotificationBy(String notificationBy) {
        this.notificationBy = notificationBy;
    }

    public long getNotificationAt() {
        return notificationAt;
    }

    public void setNotificationAt(long notificationAt) {
        this.notificationAt = notificationAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public boolean isCheckOpen() {
        return checkOpen;
    }

    public void setCheckOpen(boolean checkOpen) {
        this.checkOpen = checkOpen;
    }

    public String getCommentedText() {
        return commentedText;
    }

    public void setCommentedText(String commentedText) {
        this.commentedText = commentedText;
    }
}
