package com.watchfreemovies.freehdcinema786.utils;

import static com.watchfreemovies.freehdcinema786.config.AppConfig.ADMIN_PANEL_URL;

public class Constant {

    public static final String REGISTER_URL = ADMIN_PANEL_URL + "/api/user_register/?user_type=normal&name=";
    public static final String NORMAL_LOGIN_URL = ADMIN_PANEL_URL + "/api/get_user_login/?email=";
    public static final String FORGET_PASSWORD_URL = ADMIN_PANEL_URL + "/api/forgot_password/?email=";
    public static final String CATEGORY_ARRAY_NAME = "result";
    public static int GET_SUCCESS_MSG;
    public static final String MSG = "msg";
    public static final String SUCCESS = "success";
    public static final String USER_NAME = "name";
    public static final String USER_ID = "user_id";
    public static final long DELAY_REFRESH = 1000;
    public static final int DELAY_PROGRESS_DIALOG = 2000;

    public static final long DELAY_TIME = 1000;
    public static final long DELAY_TIME_MEDIUM = 500;
    public static final String YOUTUBE_IMG_FRONT = "https://img.youtube.com/vi/";
    public static final String YOUTUBE_IMG_BACK = "/mqdefault.jpg";
    public static final int MAX_SEARCH_RESULT = 500;

    public static final String TOPIC_GLOBAL = "global";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String SHARED_PREF = "ah_firebase";
    public static final String NOTIFICATION_CHANNEL_NAME = "news_channel_01";
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    public static final String AD_STATUS_ON = "on";
    public static final String ADMOB = "admob";
    public static final String FAN = "fan";
    public static final String STARTAPP = "startapp";

    //startapp native ad image parameters
    public static final int STARTAPP_IMAGE_XSMALL = 1; //for image size 100px X 100px
    public static final int STARTAPP_IMAGE_SMALL = 2; //for image size 150px X 150px
    public static final int STARTAPP_IMAGE_MEDIUM = 3; //for image size 340px X 340px
    public static final int STARTAPP_IMAGE_LARGE = 4; //for image size 1200px X 628px

    public static final int MAX_NUMBER_OF_NATIVE_AD_DISPLAYED = 1000;

}