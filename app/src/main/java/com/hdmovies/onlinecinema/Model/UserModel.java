package com.hdmovies.onlinecinema.Model;

public class UserModel {
    private String userName, email, password;
    private String coverPhoto;
    private String profile;
    private String profession, fbLink, instaLink, githubLink, linkedinLink, twitterLink, userBio;

    public UserModel() {
    }

    public UserModel(String userName, String email, String password, String profession, String fbLink, String instaLink, String githubLink, String linkedinLink, String twitterLink, String userBio) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.profession = profession;
        this.fbLink = fbLink;
        this.instaLink = instaLink;
        this.githubLink = githubLink;
        this.linkedinLink = linkedinLink;
        this.twitterLink = twitterLink;
        this.userBio = userBio;
    }

    public UserModel(String userName, String email, String password, String coverPhoto, String profile, String profession, String fbLink, String instaLink, String githubLink, String linkedinLink, String twitterLink, String userBio) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.coverPhoto = coverPhoto;
        this.profile = profile;
        this.profession = profession;
        this.fbLink = fbLink;
        this.instaLink = instaLink;
        this.githubLink = githubLink;
        this.linkedinLink = linkedinLink;
        this.twitterLink = twitterLink;
        this.userBio = userBio;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getFbLink() {
        return fbLink;
    }

    public void setFbLink(String fbLink) {
        this.fbLink = fbLink;
    }

    public String getInstaLink() {
        return instaLink;
    }

    public void setInstaLink(String instaLink) {
        this.instaLink = instaLink;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }

    public String getLinkedinLink() {
        return linkedinLink;
    }

    public void setLinkedinLink(String linkedinLink) {
        this.linkedinLink = linkedinLink;
    }

    public String getTwitterLink() {
        return twitterLink;
    }

    public void setTwitterLink(String twitterLink) {
        this.twitterLink = twitterLink;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }
}
