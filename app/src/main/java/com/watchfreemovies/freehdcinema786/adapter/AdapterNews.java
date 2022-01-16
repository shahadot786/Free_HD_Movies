package com.watchfreemovies.freehdcinema786.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.config.AppConfig;
import com.watchfreemovies.freehdcinema786.config.UiConfig;
import com.watchfreemovies.freehdcinema786.models.News;
import com.watchfreemovies.freehdcinema786.utils.AdsPref;
import com.watchfreemovies.freehdcinema786.utils.Constant;
import com.watchfreemovies.freehdcinema786.utils.NativeTemplateStyle;
import com.watchfreemovies.freehdcinema786.utils.TemplateView;
import com.watchfreemovies.freehdcinema786.utils.ThemePref;
import com.watchfreemovies.freehdcinema786.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.watchfreemovies.freehdcinema786.utils.Constant.ADMOB;
import static com.watchfreemovies.freehdcinema786.utils.Constant.AD_STATUS_ON;
import static com.watchfreemovies.freehdcinema786.utils.Constant.FAN;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP_IMAGE_MEDIUM;

public class AdapterNews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;

    private List<News> items;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context context;
    private OnItemClickListener mOnItemClickListener;

    private StartAppNativeAd startAppNativeAd;
    private NativeAdDetails nativeAdDetails = null;

    public interface OnItemClickListener {
        void onItemClick(View view, News obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterNews(Context context, RecyclerView view, List<News> items) {
        this.items = items;
        this.context = context;
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView ic_date;
        public TextView date;
        public TextView category;
        public TextView comment;
        public ImageView image;
        public ImageView thumbnail_video;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_comment;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            ic_date = v.findViewById(R.id.ic_date);
            date = v.findViewById(R.id.date);
            category = v.findViewById(R.id.category_name);
            comment = v.findViewById(R.id.comment);
            image = v.findViewById(R.id.image);
            thumbnail_video = v.findViewById(R.id.thumbnail_video);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            lyt_comment = v.findViewById(R.id.lyt_comment);
        }

    }

    public class AdViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public ImageView ic_date;
        public TextView date;
        public TextView category;
        public TextView comment;
        public ImageView image;
        public ImageView thumbnail_video;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_comment;

        //AdMob
        LinearLayout lyt_native_ad;
        TemplateView admob_native_template;
        MediaView admob_media_view;
        TextView primary;
        TextView secondary;

        //FAN
        private NativeAd nativeAd;
        RelativeLayout lyt_fan_native;
        private NativeAdLayout nativeAdLayout;
        private LinearLayout nativeAdView;

        //StartApp
        RelativeLayout lyt_startapp_native;
        ImageView startapp_native_image;
        TextView startapp_native_title;
        TextView startapp_native_description;
        Button startapp_native_button;

        public AdViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            ic_date = v.findViewById(R.id.ic_date);
            date = v.findViewById(R.id.date);
            category = v.findViewById(R.id.category_name);
            comment = v.findViewById(R.id.comment);
            image = v.findViewById(R.id.image);
            thumbnail_video = v.findViewById(R.id.thumbnail_video);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            lyt_comment = v.findViewById(R.id.lyt_comment);

            //admob native ad
            lyt_native_ad = v.findViewById(R.id.lyt_native_ad);
            admob_native_template = v.findViewById(R.id.native_template);
            admob_media_view = v.findViewById(R.id.media_view);
            primary = v.findViewById(R.id.primary);
            secondary = v.findViewById(R.id.secondary);

            //fan native ad
            lyt_fan_native = v.findViewById(R.id.lyt_fan_native);
            nativeAdLayout = v.findViewById(R.id.native_ad_container);

            //startapp native ad
            lyt_startapp_native = v.findViewById(R.id.lyt_startapp_native);
            startapp_native_image = v.findViewById(R.id.startapp_native_image);
            startapp_native_title = v.findViewById(R.id.startapp_native_title);
            startapp_native_description = v.findViewById(R.id.startapp_native_description);
            startapp_native_button = v.findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> itemView.performClick());
        }

        private void bindAdMobNativeAdView() {
            final ThemePref themePref = new ThemePref(context);
            final AdsPref adsPref = new AdsPref(context);
            AdLoader adLoader = new AdLoader.Builder(context, adsPref.getAdMobNativeId())
                    .forNativeAd(nativeAd -> {
                        if (themePref.getIsDarkTheme()) {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundDark));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            admob_native_template.setStyles(styles);
                            primary.setBackgroundResource(R.color.colorBackgroundDark);
                            secondary.setBackgroundResource(R.color.colorBackgroundDark);
                        } else {
                            ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundLight));
                            NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                            admob_native_template.setStyles(styles);
                            primary.setBackgroundResource(R.color.colorBackgroundLight);
                            secondary.setBackgroundResource(R.color.colorBackgroundLight);
                        }
                        admob_media_view.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                        admob_native_template.setNativeAd(nativeAd);
                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                                admob_native_template.setVisibility(View.VISIBLE);
                                lyt_native_ad.setVisibility(View.VISIBLE);
                            } else {
                                admob_native_template.setVisibility(View.GONE);
                                lyt_native_ad.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            admob_native_template.setVisibility(View.GONE);
                            lyt_native_ad.setVisibility(View.GONE);
                        }
                    })
                    .build();
            adLoader.loadAd(Tools.getAdRequest((Activity) context));
        }

        private void bindFanNativeAdView() {
            final AdsPref adsPref = new AdsPref(context);
            final ThemePref themePref = new ThemePref(context);
            if (BuildConfig.DEBUG) {
                nativeAd = new NativeAd(context, "IMG_16_9_APP_INSTALL#" + adsPref.getFanNativeUnitId());
            } else {
                nativeAd = new NativeAd(context, adsPref.getFanNativeUnitId());
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
                    if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                        lyt_fan_native.setVisibility(View.VISIBLE);
                        if (nativeAd == null || nativeAd != ad) {
                            return;
                        }
                        // Inflate Native Ad into Container
                        //inflateAd(nativeAd);
                        nativeAd.unregisterView();
                        // Add the Ad view into the ad container.
                        LayoutInflater inflater = LayoutInflater.from(context);
                        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.

                        nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_small_template, nativeAdLayout, false);

                        nativeAdLayout.addView(nativeAdView);

                        // Add the AdOptionsView
                        LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
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
                            nativeAdTitle.setBackgroundResource(R.color.colorBackgroundDark);
                        } else {
                            nativeAdTitle.setBackgroundResource(R.color.colorBackgroundLight);
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
                    } else {
                        lyt_fan_native.setVisibility(View.GONE);
                    }
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

        private void bindStartAppNativeAdView() {
            startAppNativeAd = new StartAppNativeAd(context);
            final AdsPref adsPref = new AdsPref(context);
            final ThemePref themePref = new ThemePref(context);
            NativeAdPreferences nativePrefs = new NativeAdPreferences()
                    .setAdsNumber(1)
                    .setAutoBitmapDownload(true)
                    .setPrimaryImageSize(STARTAPP_IMAGE_MEDIUM);
            AdEventListener adListener = new AdEventListener() {
                @Override
                public void onReceiveAd(Ad arg0) {
                    if (getAdapterPosition() % adsPref.getNativeAdInterval() == adsPref.getNativeAdIndex()) {
                        ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                        if (nativeAdsList.size() > 0) {
                            nativeAdDetails = nativeAdsList.get(0);
                        }
                        if (nativeAdDetails != null) {
                            startapp_native_image.setImageBitmap(nativeAdDetails.getImageBitmap());

                            if (themePref.getIsDarkTheme()) {
                                startapp_native_title.setBackgroundResource(R.color.colorBackgroundDark);
                                startapp_native_description.setBackgroundResource(R.color.colorBackgroundDark);
                            } else {
                                startapp_native_title.setBackgroundResource(R.color.colorBackgroundLight);
                                startapp_native_description.setBackgroundResource(R.color.colorBackgroundLight);
                            }

                            startapp_native_title.setText(nativeAdDetails.getTitle());
                            startapp_native_description.setText(nativeAdDetails.getDescription());
                            startapp_native_button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
                            nativeAdDetails.registerViewForInteraction(itemView);
                        }
                        lyt_startapp_native.setVisibility(View.VISIBLE);
                    } else {
                        lyt_startapp_native.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {
                    lyt_startapp_native.setVisibility(View.GONE);
                }
            };
            startAppNativeAd.loadAd(nativePrefs, adListener);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_news, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_native_ad, parent, false);
            vh = new AdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof OriginalViewHolder) {
            final News p = items.get(position);
            OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.title.setText(Html.fromHtml(p.news_title));

            if (UiConfig.ENABLE_DATE_DISPLAY) {
                vItem.date.setVisibility(View.VISIBLE);
                vItem.ic_date.setVisibility(View.VISIBLE);
            } else {
                vItem.date.setVisibility(View.GONE);
                vItem.ic_date.setVisibility(View.GONE);
            }

            if (UiConfig.DATE_DISPLAY_AS_TIME_AGO) {
                PrettyTime prettyTime = new PrettyTime();
                long timeAgo = Tools.timeStringtoMilis(p.news_date);
                vItem.date.setText(prettyTime.format(new Date(timeAgo)));
            } else {
                vItem.date.setText(Tools.getFormatedDateSimple(p.news_date));
            }

            if (UiConfig.DISABLE_COMMENT) {
                vItem.lyt_comment.setVisibility(View.GONE);
            }

            vItem.category.setText(Html.fromHtml(p.news_description));

            vItem.comment.setText(p.comments_count + "");

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnail_video.setVisibility(View.GONE);
            } else {
                vItem.thumbnail_video.setVisibility(View.VISIBLE);
            }

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            } else {
                Picasso.get()
                        .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            }

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });
        } else if (holder instanceof AdViewHolder) {
            final News p = items.get(position);
            AdViewHolder vItem = (AdViewHolder) holder;

            final AdsPref adsPref = new AdsPref(context);
            int LIMIT_NATIVE_AD = (Constant.MAX_NUMBER_OF_NATIVE_AD_DISPLAYED * adsPref.getNativeAdInterval()) + adsPref.getNativeAdIndex();
            if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
                for (int i = adsPref.getNativeAdIndex(); i < LIMIT_NATIVE_AD; i += adsPref.getNativeAdInterval()) {
                    if (position == i) {
                        switch (adsPref.getAdType()) {
                            case ADMOB:
                                if (!adsPref.getAdMobNativeId().equals("0")) {
                                    vItem.bindAdMobNativeAdView();
                                }
                                break;
                            case FAN:
                                if (!adsPref.getFanNativeUnitId().equals("0")) {
                                    vItem.bindFanNativeAdView();
                                }
                                break;
                            case STARTAPP:
                                if (!adsPref.getStartappAppID().equals("0")) {
                                    vItem.bindStartAppNativeAdView();
                                }
                                break;
                        }
                    }
                }
            }

            vItem.title.setText(Html.fromHtml(p.news_title));

            if (UiConfig.ENABLE_DATE_DISPLAY) {
                vItem.date.setVisibility(View.VISIBLE);
                vItem.ic_date.setVisibility(View.VISIBLE);
            } else {
                vItem.date.setVisibility(View.GONE);
                vItem.ic_date.setVisibility(View.GONE);
            }

            if (UiConfig.DATE_DISPLAY_AS_TIME_AGO) {
                PrettyTime prettyTime = new PrettyTime();
                long timeAgo = Tools.timeStringtoMilis(p.news_date);
                vItem.date.setText(prettyTime.format(new Date(timeAgo)));
            } else {
                vItem.date.setText(Tools.getFormatedDateSimple(p.news_date));
            }

            if (UiConfig.DISABLE_COMMENT) {
                vItem.lyt_comment.setVisibility(View.GONE);
            }

            vItem.category.setText(Html.fromHtml(p.news_description));

            vItem.comment.setText(p.comments_count + "");

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnail_video.setVisibility(View.GONE);
            } else {
                vItem.thumbnail_video.setVisibility(View.VISIBLE);
            }

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            } else {
                Picasso.get()
                        .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.image);
            }

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) != null) {
            final AdsPref adsPref = new AdsPref(context);
            int LIMIT_NATIVE_AD = (Constant.MAX_NUMBER_OF_NATIVE_AD_DISPLAYED * adsPref.getNativeAdInterval()) + adsPref.getNativeAdIndex();
            for (int i = adsPref.getNativeAdIndex(); i < LIMIT_NATIVE_AD; i += adsPref.getNativeAdInterval()) {
                if (position == i) {
                    return VIEW_AD;
                }
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertData(List<News> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        //this.items = new ArrayList<>();
        this.items.clear();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / UiConfig.LOAD_MORE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}