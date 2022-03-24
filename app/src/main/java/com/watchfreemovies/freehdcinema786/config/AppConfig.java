package com.watchfreemovies.freehdcinema786.config;

public class AppConfig {

    //your Server Key obtained from the admin panel
    public static final String SERVER_KEY = "WVVoU01HTkViM1pNTW1ocllsYzVNbUZYVm5wTWJrNXZZMjFPZVZwWFJqQmhWemwxVEcxT2RtSlRPWHBoU0VsMFdWZFNkR0ZYTldaWldFSjNZa2RzYWxsWVVuQmlNalZLV2tZNWFtSXlNSFZrTWtZd1dUSm9iV050Vm14aVZ6a3lZVmRXZWt4dFdubGFWMVp2V2tkT2NHSnRWblJaVkdNMFRtYzlQUT09";

    //your Rest API Key obtained from the admin panel
    public static final String REST_API_KEY = "cda11T2xcUyOLn04vdoJ1tDQ8MiEr6V9CbWRSXjkfg5FZAuqwh";

    //show one latest news as header view
    public static final boolean DISPLAY_HEADER_VIEW = true;

    //show short description in the news list
    public static final boolean ENABLE_EXCERPT_IN_POST_LIST = true;

    //show total news in each category
    public static final boolean ENABLE_POST_COUNT_IN_CATEGORY = false;

    //video player orientation
    public static final boolean FORCE_VIDEO_PLAYER_TO_LANDSCAPE = false;

    //date display configuration
    public static final boolean ENABLE_DATE_DISPLAY = true;
    public static final boolean DATE_DISPLAY_AS_TIME_AGO = true;

    //enable view count in the news description
    public static final boolean ENABLE_VIEW_COUNT = true;

    //display alert dialog when user want to close the app
    public static final boolean ENABLE_EXIT_DIALOG = true;

    //set false to disable copy text in the news description
    public static final boolean ENABLE_TEXT_SELECTION = true;

    //open link in the news description using external web browser
    public static final boolean OPEN_LINK_INSIDE_APP = true;

    //GDPR EU Consent
    public static final boolean USE_LEGACY_GDPR_EU_CONSENT = true;

    //load more for next news list
    public static final int LOAD_MORE = 10;

    public static final int DELAY_SPLASH_SCREEN = 2000;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

}