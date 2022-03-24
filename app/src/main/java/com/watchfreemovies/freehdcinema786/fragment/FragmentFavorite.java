package com.watchfreemovies.freehdcinema786.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.activities.ActivityPostDetail;
import com.watchfreemovies.freehdcinema786.activities.ActivityPostDetailOffline;
import com.watchfreemovies.freehdcinema786.activities.MainActivity;
import com.watchfreemovies.freehdcinema786.adapter.AdapterNews;
import com.watchfreemovies.freehdcinema786.database.sqlite.DbHandler;
import com.watchfreemovies.freehdcinema786.models.Post;
import com.watchfreemovies.freehdcinema786.utils.Constant;
import com.watchfreemovies.freehdcinema786.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    AdapterNews adapterNews;
    List<Post> posts = new ArrayList<>();
    DbHandler dbHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        dbHandler = new DbHandler(getActivity());
        adapterNews = new AdapterNews(getActivity(), recyclerView, posts);
        recyclerView.setAdapter(adapterNews);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(dbHandler.getAllData());
    }

    public void displayData(List<Post> posts) {
        List<Post> favorites = new ArrayList<>();
        if (posts != null && posts.size() > 0) {
            favorites.addAll(posts);
        }
        adapterNews.resetListData();
        adapterNews.insertDataWithNativeAd(favorites);
        showNoItemView(favorites.size() == 0);

        adapterNews.setOnItemClickListener((v, obj, position) -> {
            if (Tools.isConnect(getActivity())) {
                Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).showInterstitialAd();
                }
            } else {
                Intent intent = new Intent(getActivity(), ActivityPostDetailOffline.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item_later);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_favorite_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }
}
