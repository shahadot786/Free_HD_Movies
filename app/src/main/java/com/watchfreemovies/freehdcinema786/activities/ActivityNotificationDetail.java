package com.watchfreemovies.freehdcinema786.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSize;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.adapter.AdapterImage;
import com.watchfreemovies.freehdcinema786.adapter.AdapterRelated;
import com.watchfreemovies.freehdcinema786.callbacks.CallbackPostDetail;
import com.watchfreemovies.freehdcinema786.config.AppConfig;
import com.watchfreemovies.freehdcinema786.config.UiConfig;
import com.watchfreemovies.freehdcinema786.models.Images;
import com.watchfreemovies.freehdcinema786.models.News;
import com.watchfreemovies.freehdcinema786.rests.RestAdapter;
import com.watchfreemovies.freehdcinema786.utils.AdsPref;
import com.watchfreemovies.freehdcinema786.utils.AppBarLayoutBehavior;
import com.watchfreemovies.freehdcinema786.utils.DbHandler;
import com.watchfreemovies.freehdcinema786.utils.NativeTemplateStyle;
import com.watchfreemovies.freehdcinema786.utils.NetworkCheck;
import com.watchfreemovies.freehdcinema786.utils.TemplateView;
import com.watchfreemovies.freehdcinema786.utils.ThemePref;
import com.watchfreemovies.freehdcinema786.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.watchfreemovies.freehdcinema786.utils.Constant.ADMOB;
import static com.watchfreemovies.freehdcinema786.utils.Constant.AD_STATUS_ON;
import static com.watchfreemovies.freehdcinema786.utils.Constant.FAN;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP_IMAGE_LARGE;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP_IMAGE_XSMALL;

public class ActivityNotificationDetail extends AppCompatActivity {

    private Call<CallbackPostDetail> callbackCall = null;
    private View lyt_main_content;
    private Runnable runnableCode = null;
    private Handler handler = new Handler();
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private News post;
    private Menu menu;
    TextView txt_title, txt_category, txt_date, txt_comment_count, txt_comment_text;
    ImageView img_thumb_video, img_date;
    LinearLayout btn_comment;
    private WebView webview;
    DbHandler databaseHandler;
    CoordinatorLayout parent_view;
    private ShimmerFrameLayout lyt_shimmer;
    RelativeLayout lyt_related;
    private SwipeRefreshLayout swipe_refresh;
    private String bg_paragraph;
    private FrameLayout adContainerView;
    com.facebook.ads.AdView fanAdView;
    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAd = null;
    private AdView adView;
    AdsPref adsPref;
    ThemePref themePref;
    private long nid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        if (UiConfig.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_post_detail_rtl);
        } else {
            setContentView(R.layout.activity_post_detail);
        }

        themePref = new ThemePref(this);
        adsPref = new AdsPref(this);

        if (UiConfig.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        Intent intent = getIntent();
        nid = intent.getLongExtra("id", 0);

        databaseHandler = new DbHandler(getApplicationContext());

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setRefreshing(false);

        lyt_main_content = findViewById(R.id.lyt_main_content);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        parent_view = findViewById(R.id.coordinatorLayout);
        webview = findViewById(R.id.news_description);
        txt_title = findViewById(R.id.title);
        txt_category = findViewById(R.id.category);
        txt_date = findViewById(R.id.date);
        img_date = findViewById(R.id.ic_date);
        txt_comment_count = findViewById(R.id.txt_comment_count);
        txt_comment_text = findViewById(R.id.txt_comment_text);
        btn_comment = findViewById(R.id.btn_comment);
        img_thumb_video = findViewById(R.id.thumbnail_video);

        lyt_related = findViewById(R.id.lyt_related);

        requestAction();

        swipe_refresh.setOnRefreshListener(() -> {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
            requestAction();
        });

        initToolbar();
        initAdNetwork();

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(this::requestPostData, 200);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI().getNewsDetail(nid);
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(Call<CallbackPostDetail> call, Response<CallbackPostDetail> response) {
                CallbackPostDetail responseHome = response.body();
                if (responseHome == null || !responseHome.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                post = responseHome.post;
                displayAllData(responseHome);
                swipeProgress(false);
                lyt_main_content.setVisibility(View.VISIBLE);
            }

            public void onFailure(Call<CallbackPostDetail> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lyt_main_content.setVisibility(View.GONE);
        if (NetworkCheck.isConnect(ActivityNotificationDetail.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
        });
    }

    private void displayAllData(CallbackPostDetail responseHome) {
        displayImages(responseHome.images);
        displayPostData();
        displayRelated(responseHome.related);
    }

    private void displayPostData() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(() -> {
            if (post.comments_count == 0) {
                txt_comment_text.setText(R.string.txt_no_comment);
            }
            if (post.comments_count == 1) {
                txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
            } else if (post.comments_count > 1) {
                txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
            }
        }, 1000);

        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);

        if (!UiConfig.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(v -> true);
            webview.setLongClickable(false);
        }

        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        if (themePref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String text_default = "<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        if (UiConfig.ENABLE_RTL_MODE) {
            webview.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            webview.loadDataWithBaseURL(null, text_default, mimeType, encoding, null);
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (UiConfig.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        if (UiConfig.ENABLE_DATE_DISPLAY) {
            txt_date.setVisibility(View.VISIBLE);
            findViewById(R.id.lyt_date).setVisibility(View.VISIBLE);
        } else {
            txt_date.setVisibility(View.GONE);
            findViewById(R.id.lyt_date).setVisibility(View.GONE);
        }
        txt_date.setText(Tools.getFormatedDate(post.news_date));

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

        new Handler().postDelayed(() -> lyt_related.setVisibility(View.VISIBLE), 1000);

        btn_comment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
        });

        txt_comment_text.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
        });

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(post.category_name);
        }

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (themePref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void displayImages(final List<Images> list) {
        TabLayout tabLayout = findViewById(R.id.tabDots);
        final AdapterImage adapter = new AdapterImage(ActivityNotificationDetail.this, list);

        if (UiConfig.ENABLE_RTL_MODE) {
            viewPagerRTL = findViewById(R.id.view_pager_image_rtl);
            viewPagerRTL.setAdapter(adapter);
            viewPagerRTL.setOffscreenPageLimit(list.size());
            viewPagerRTL.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            tabLayout.setupWithViewPager(viewPagerRTL, true);
        } else {
            viewPager = findViewById(R.id.view_pager_image);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(list.size());
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            tabLayout.setupWithViewPager(viewPager, true);
        }

        if (list.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        adapter.setOnItemClickListener((view, p, position) -> {
            switch (p.content_type) {
                case "youtube": {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", p.video_id);
                    startActivity(intent);
                    break;
                }
                case "Url": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);
                    break;
                }
                case "Upload": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageSlider.class);
                    intent.putExtra("position", position);
                    intent.putExtra("nid", post.nid);
                    startActivity(intent);
                    break;
                }
            }
        });

    }

    private void displayRelated(List<News> list) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_related);
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityNotificationDetail.this));
        AdapterRelated adapterNews = new AdapterRelated(ActivityNotificationDetail.this, recyclerView, list);
        recyclerView.setAdapter(adapterNews);
        recyclerView.setNestedScrollingEnabled(false);
        adapterNews.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
            intent.putExtra("id", obj.nid);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_detail, menu);
        this.menu = menu;
        addToFavorite();

        return true;
    }

    public void addToFavorite() {
        List<News> data = databaseHandler.getFavRow(nid);
        if (data.size() == 0) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
        } else {
            if (data.get(0).getNid() == nid) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_later:

                List<News> data = databaseHandler.getFavRow(nid);
                if (data.size() == 0) {
                    databaseHandler.AddtoFavorite(new News(
                            post.nid,
                            post.news_title,
                            post.category_name,
                            post.news_date,
                            post.news_image,
                            post.news_description,
                            post.content_type,
                            post.video_url,
                            post.video_id,
                            post.comments_count
                    ));
                    Snackbar.make(parent_view, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));

                } else {
                    if (data.get(0).getNid() == nid) {
                        databaseHandler.RemoveFav(new News(nid));
                        Snackbar.make(parent_view, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                        menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
                    }
                }

                break;

            case R.id.action_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.news_title + "\n\n" + getResources().getString(R.string.share_content) + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public void initAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            loadAdMobBannerAd();
            loadAdMobNativeAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            loadFanBannerAd();
            loadFanNativeAd();
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            loadStartAppBannerAd();
            loadStartAppNativeAd();
        }
    }

    public void loadAdMobBannerAd() {
        if (!adsPref.getAdMobBannerId().equals("0")) {
            adContainerView = findViewById(R.id.admob_banner_view_container);
            adContainerView.post(() -> {
                adView = new AdView(this);
                adView.setAdUnitId(adsPref.getAdMobBannerId());
                adContainerView.removeAllViews();
                adContainerView.addView(adView);
                adView.setAdSize(Tools.getAdSize(this));
                adView.loadAd(Tools.getAdRequest(this));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        adContainerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Code to be executed when an ad request fails.
                        adContainerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                    }

                    @Override
                    public void onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                    }

                    @Override
                    public void onAdClosed() {
                        // Code to be executed when the user is about to return
                        // to the app after tapping on an ad.
                    }
                });
            });
        }
    }

    private void loadAdMobNativeAd() {
        if (!adsPref.getAdMobNativeId().equals("0")) {
            final TemplateView native_template = findViewById(R.id.native_template);
            final MediaView mediaView = findViewById(R.id.media_view);
            AdLoader adLoader = new AdLoader.Builder(this, adsPref.getAdMobNativeId())
                    .forNativeAd(nativeAd -> {
                        if (themePref.getIsDarkTheme()) {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(this, R.color.colorBackgroundDark));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            native_template.setStyles(styles);
                        } else {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(this, R.color.colorBackgroundLight));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            native_template.setStyles(styles);
                        }
                        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                        native_template.setNativeAd(nativeAd);
                        native_template.setVisibility(View.VISIBLE);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            native_template.setVisibility(View.GONE);
                        }
                    })
                    .build();
            adLoader.loadAd(Tools.getAdRequest(this));
        }
    }

    private void loadFanBannerAd() {
        if (!adsPref.getFanBannerUnitId().equals("0")) {
            if (BuildConfig.DEBUG) {
                fanAdView = new com.facebook.ads.AdView(this, "IMG_16_9_APP_INSTALL#" + adsPref.getFanBannerUnitId(), AdSize.BANNER_HEIGHT_50);
            } else {
                fanAdView = new com.facebook.ads.AdView(this, adsPref.getFanBannerUnitId(), AdSize.BANNER_HEIGHT_50);
            }
            LinearLayout adContainer = findViewById(R.id.fan_banner_view_container);
            // Add the ad view to your activity layout
            adContainer.addView(fanAdView);
            com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                @Override
                public void onError(com.facebook.ads.Ad ad, AdError adError) {
                    adContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded(com.facebook.ads.Ad ad) {
                    adContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onLoggingImpression(com.facebook.ads.Ad ad) {

                }
            };
            com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);
        }
    }

    private void loadFanNativeAd() {
        if (!adsPref.getFanNativeUnitId().equals("0")) {
            final NativeAd nativeAd;
            final RelativeLayout lyt_fan_native = findViewById(R.id.lyt_fan_native);
            final NativeAdLayout nativeAdLayout = findViewById(R.id.native_ad_container);
            if (BuildConfig.DEBUG) {
                nativeAd = new NativeAd(this, "IMG_16_9_APP_INSTALL#" + adsPref.getFanNativeUnitId());
            } else {
                nativeAd = new NativeAd(this, adsPref.getFanNativeUnitId());
            }
            NativeAdListener nativeAdListener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onError(com.facebook.ads.Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(com.facebook.ads.Ad ad) {
                    // Race condition, load() called again before last ad was displayed
                    lyt_fan_native.setVisibility(View.VISIBLE);
                    if (nativeAd != ad) {
                        return;
                    }
                    // Inflate Native Ad into Container
                    //inflateAd(nativeAd);
                    nativeAd.unregisterView();
                    // Add the Ad view into the ad container.
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
                    LinearLayout nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_medium_template, nativeAdLayout, false);
                    nativeAdLayout.addView(nativeAdView);

                    // Add the AdOptionsView
                    LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                    AdOptionsView adOptionsView = new AdOptionsView(getApplicationContext(), nativeAd, nativeAdLayout);
                    adChoicesContainer.removeAllViews();
                    adChoicesContainer.addView(adOptionsView, 0);

                    // Create native UI using the ad metadata.
                    TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                    com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                    TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                    TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                    TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                    Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);
                    LinearLayout ad_unit = nativeAdView.findViewById(R.id.ad_unit);

                    if (themePref.getIsDarkTheme()) {
                        nativeAdTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        nativeAdSocialContext.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        sponsoredLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gnt_sub_text_color));
                        nativeAdBody.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gnt_sub_text_color));
                    }

                    // Set the Text.
                    nativeAdTitle.setText(nativeAd.getAdvertiserName());
                    nativeAdBody.setText(nativeAd.getAdBodyText());
                    nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                    nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                    nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                    sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                    // Create a list of clickable views
                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(nativeAdTitle);
                    clickableViews.add(ad_unit);
                    clickableViews.add(nativeAdCallToAction);

                    // Register the Title and CTA button to listen for clicks.
                    nativeAd.registerViewForInteraction(nativeAdView, nativeAdMedia, clickableViews);

                }

                @Override
                public void onAdClicked(com.facebook.ads.Ad ad) {

                }

                @Override
                public void onLoggingImpression(com.facebook.ads.Ad ad) {

                }
            };

            NativeAd.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
            nativeAd.loadAd(loadAdConfig);
        }
    }

    private void loadStartAppBannerAd() {
        if (!adsPref.getStartappAppID().equals("0")) {
            RelativeLayout bannerLayout = findViewById(R.id.startapp_banner_view_container);
            Banner banner = new Banner(this, new BannerListener() {
                @Override
                public void onReceiveAd(View banner) {
                    bannerLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailedToReceiveAd(View banner) {
                    bannerLayout.setVisibility(View.GONE);
                }

                @Override
                public void onImpression(View view) {

                }

                @Override
                public void onClick(View banner) {
                }
            });
            bannerLayout.addView(banner);
        }
    }

    private void loadStartAppNativeAd() {
        if (!adsPref.getStartappAppID().equals("0")) {
            View lyt_startapp_native = findViewById(R.id.startapp_native_template);
            ImageView startapp_native_image = findViewById(R.id.startapp_native_image);
            ImageView startapp_native_secondary_image = findViewById(R.id.startapp_native_secondary_image);
            TextView startapp_native_title = findViewById(R.id.startapp_native_title);
            TextView startapp_native_description = findViewById(R.id.startapp_native_description);
            Button startapp_native_button = findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> lyt_startapp_native.performClick());

            startAppNativeAd = new StartAppNativeAd(this);
            NativeAdPreferences nativePrefs = new NativeAdPreferences()
                    .setAdsNumber(1)
                    .setAutoBitmapDownload(true)
                    .setPrimaryImageSize(STARTAPP_IMAGE_LARGE)
                    .setSecondaryImageSize(STARTAPP_IMAGE_XSMALL);
            AdEventListener adListener = new AdEventListener() {
                @Override
                public void onReceiveAd(Ad arg0) {
                    ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                    if (nativeAdsList.size() > 0) {
                        nativeAd = nativeAdsList.get(0);
                    }
                    if (nativeAd != null) {;
                        startapp_native_image.setImageBitmap(nativeAd.getImageBitmap());
                        startapp_native_secondary_image.setImageBitmap(nativeAd.getSecondaryImageBitmap());
                        startapp_native_title.setText(nativeAd.getTitle());
                        startapp_native_description.setText(nativeAd.getDescription());
                        startapp_native_button.setText(nativeAd.isApp() ? "Install" : "Open");
                        nativeAd.registerViewForInteraction(lyt_startapp_native);
                    }
                    lyt_startapp_native.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {
                    lyt_startapp_native.setVisibility(View.GONE);
                }
            };
            startAppNativeAd.loadAd(nativePrefs, adListener);
        }
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
        super.onDestroy();
    }


}
