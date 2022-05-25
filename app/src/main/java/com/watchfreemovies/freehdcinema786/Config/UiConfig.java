package com.watchfreemovies.freehdcinema786.Config;

public class UiConfig {

    /*Pro and free version check code
     *if true free version
     *if false pro version
     */

    //Ad Related Static Variables
    //ads
    public static boolean BANNER_AD_VISIBILITY = true;//if true then show banner ad
    public static boolean INTERSTITIAL__AD_VISIBILITY = true;//if true then show interstitial ad
    public static int INTERSTITIAL_AD_INTERVAL = 2;//how many click for showed ads
    public static boolean REWARDED__AD_VISIBILITY = true;//if true then show rewarded ad
    //Premium User Check
    public static boolean PRO_VISIBILITY_STATUS_SHOW = true; // if ture then show all premium activities
    public static boolean SEND_EMAIL_VERIFICATION_STATUS = false; // if ture then send email verification
    //splash screen advertise check
    public static boolean SPLASH_SCREEN_PRO = true; // if ture then show the splash screen premium activity
    //app exit check
    public static boolean ENABLE_EXIT_DIALOG = true; //if true then show dialog

    //Extra UI Related Static Variables
    //Hide Movies And TV
    public static boolean HIDE_MOVIES_TV = true;//false every new update and true when upload movies.
    public static boolean EDIT_MOVIES_BUTTON = true;//false every new update and true when update movies.
    //share count 1 user 4:1 means 2 clicks 1 share
    public static int SHARE_CLICK_COUNT = 4;
    //video player orientation
    public static final boolean FORCE_VIDEO_PLAYER_TO_LANDSCAPE = false;



}


