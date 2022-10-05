package com.hdmovies.onlinecinema.Model;

public class ReportsModel {
    String userName,userEmail,postId,selectIssue;
    String movieName;
    String reportedBy;

    public ReportsModel() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getSelectIssue() {
        return selectIssue;
    }

    public void setSelectIssue(String selectIssue) {
        this.selectIssue = selectIssue;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
}
