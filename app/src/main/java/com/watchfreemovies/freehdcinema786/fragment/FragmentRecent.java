package com.watchfreemovies.freehdcinema786.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.activities.ActivityPostDetail;
import com.watchfreemovies.freehdcinema786.adapter.AdapterRecent;
import com.watchfreemovies.freehdcinema786.callbacks.CallbackRecent;
import com.watchfreemovies.freehdcinema786.config.AppConfig;
import com.watchfreemovies.freehdcinema786.config.UiConfig;
import com.watchfreemovies.freehdcinema786.models.News;
import com.watchfreemovies.freehdcinema786.rests.ApiInterface;
import com.watchfreemovies.freehdcinema786.rests.RestAdapter;
import com.watchfreemovies.freehdcinema786.utils.AdsPref;
import com.watchfreemovies.freehdcinema786.utils.Constant;
import com.watchfreemovies.freehdcinema786.utils.NetworkCheck;
import com.watchfreemovies.freehdcinema786.utils.Tools;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.startapp.sdk.adsbase.StartAppAd;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.watchfreemovies.freehdcinema786.utils.Constant.ADMOB;
import static com.watchfreemovies.freehdcinema786.utils.Constant.AD_STATUS_ON;
import static com.watchfreemovies.freehdcinema786.utils.Constant.FAN;
import static com.watchfreemovies.freehdcinema786.utils.Constant.STARTAPP;

public class FragmentRecent extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterRecent mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private Call<CallbackRecent> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private StartAppAd startAppAd;
    private AdsPref adsPref;
    int counter = 1;
    private ArrayList<Object> feedItems = new ArrayList<>();
    private ShimmerFrameLayout lyt_shimmer;
    View lyt_shimmer_head;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_home, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        adsPref = new AdsPref(getActivity());
        loadAdNetwork();

        lyt_shimmer = root_view.findViewById(R.id.shimmer_view_container);
        lyt_shimmer_head = root_view.findViewById(R.id.lyt_shimmer_head);
        if (!UiConfig.DISPLAY_HEADER_VIEW) {
            lyt_shimmer_head.setVisibility(View.GONE);
        }

        swipe_refresh = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterRecent(getActivity(), recyclerView, feedItems);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
            showInterstitialAdNetwork();
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(current_page -> {
            if (post_total > mAdapter.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                mAdapter.setLoaded();
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            mAdapter.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return root_view;
    }

    private void displayApiResult(final List<News> posts) {
        mAdapter.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getRecentPost(AppConfig.API_KEY, page_no, UiConfig.LOAD_MORE);
        callbackCall.enqueue(new Callback<CallbackRecent>() {
            @Override
            public void onResponse(Call<CallbackRecent> call, Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackRecent> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.msg_no_Movies);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
    }

    private void loadAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            if (!adsPref.getAdMobInterstitialId().equals("0")) {
                InterstitialAd.load(getActivity(), adsPref.getAdMobInterstitialId(), Tools.getAdRequest(getActivity()), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        adMobInterstitialAd = interstitialAd;
                        adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                loadAdNetwork();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                Log.d("Interstitial", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                adMobInterstitialAd = null;
                                Log.d("Interstitial", "The ad was shown.");
                            }
                        });
                        Log.i("Interstitial", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("Interstitial", loadAdError.getMessage());
                        adMobInterstitialAd = null;
                        Log.d("Interstitial", "Failed load AdMob Interstitial Ad");
                    }
                });
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            if (BuildConfig.DEBUG) {
                fanInterstitialAd = new com.facebook.ads.InterstitialAd(getActivity(), "IMG_16_9_APP_INSTALL#" + adsPref.getFanInterstitialUnitId());
            } else {
                fanInterstitialAd = new com.facebook.ads.InterstitialAd(getActivity(), adsPref.getFanInterstitialUnitId());
            }
            com.facebook.ads.InterstitialAdListener adListener = new InterstitialAdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }

                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    fanInterstitialAd.loadAd();
                }
            };

            com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = fanInterstitialAd.buildLoadAdConfig().withAdListener(adListener).build();
            fanInterstitialAd.loadAd(loadAdConfig);

        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            if (!adsPref.getStartappAppID().equals("0")) {
                startAppAd = new StartAppAd(getActivity());
            }
        }
    }

    private void showInterstitialAdNetwork() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            if (!adsPref.getAdMobInterstitialId().equals("0")) {
                if (adMobInterstitialAd != null) {
                    if (counter == adsPref.getInterstitialAdInterval()) {
                        adMobInterstitialAd.show(getActivity());
                        counter = 1;
                    } else {
                        counter++;
                    }
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(FAN)) {
            if (!adsPref.getFanInterstitialUnitId().equals("0")) {
                if (fanInterstitialAd != null && fanInterstitialAd.isAdLoaded()) {
                    if (counter == adsPref.getInterstitialAdInterval()) {
                        fanInterstitialAd.show();
                        counter = 1;
                    } else {
                        counter++;
                    }
                }
            }
        } else if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            if (!adsPref.getStartappAppID().equals("0")) {
                if (counter == adsPref.getInterstitialAdInterval()) {
                    startAppAd.showAd();
                    counter = 1;
                } else {
                    counter++;
                }
            }
        }
    }

}

