package com.hdmovies.onlinecinema.Fragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdmovies.onlinecinema.Activities.FeedBackActivity;
import com.hdmovies.onlinecinema.Activities.SearchActivity;
import com.hdmovies.onlinecinema.Activities.UserProfilesActivity;
import com.hdmovies.onlinecinema.Adapter.MoviesAdapter;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.MoviesModel;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TvSeriesFragment extends Fragment {

    ShimmerRecyclerView recyclerView;
    ArrayList<MoviesModel> tvList;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseAuth auth;
    FirebaseDatabase database;
    TextView toastText;
    View toastLayout;
    Toast toast;
    View empty;
    TextView emptyText,emptySubText;

    public TvSeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tv_series, container, false);
        setHasOptionsMenu(true);
        //find id
        TextView userName = view.findViewById(R.id.userName);
        ImageView profileImage = view.findViewById(R.id.profileImage);
        empty = view.findViewById(R.id.emptyList);
        emptyText = view.findViewById(R.id.empty);
        emptySubText = view.findViewById(R.id.emptyListText);
        //custom toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //recyclerview
        recyclerView = view.findViewById(R.id.rv_tv);
        recyclerView.showShimmerAdapter();
        //pro status
        ImageView proBadge = view.findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            assert user != null;
                            Picasso.get()
                                    .load(user.getProfile())
                                    .placeholder(R.drawable.ic_profile_default_image)
                                    .into(profileImage);
                            userName.setText(user.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //userProfileView click data
        View view1 = view.findViewById(R.id.userProfileView);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserProfilesActivity.class);
                intent.putExtra("postedBy", auth.getUid());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        tvList = new ArrayList<>();
        MoviesAdapter tvSeriesAdapter = new MoviesAdapter(tvList, getContext());
        //reverse layout
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3,LinearLayoutManager.VERTICAL);
        gridLayoutManager.setReverseLayout(false);
        //set value to recyclerView
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(FeedBackActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            toastText.setText(R.string.network_connected);
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }

        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(tvList);
                            String type = model.getType();
                            Collections.shuffle(tvList);
                            if (type.equals("TV")){
                                tvList.add(model);
                            }

                        }
                        recyclerView.setAdapter(tvSeriesAdapter);
                        recyclerView.hideShimmerAdapter();
                        tvSeriesAdapter.notifyDataSetChanged();

                        //check if list is empty
                        if (tvList.isEmpty()){
                            emptyText.setVisibility(View.VISIBLE);
                            emptySubText.setVisibility(View.VISIBLE);
                            empty.setVisibility(View.VISIBLE);
                        }else {
                            emptyText.setVisibility(View.GONE);
                            emptySubText.setVisibility(View.GONE);
                            empty.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //find swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshDiscuss);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                recyclerView.showShimmerAdapter();
                recyclerView.setLayoutManager(gridLayoutManager);
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                tvList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(tvList);
                                    String type = model.getType();
                                    Collections.shuffle(tvList);
                                    if (type.equals("TV")){
                                        tvList.add(model);
                                    }
                                }
                                recyclerView.setAdapter(tvSeriesAdapter);
                                tvSeriesAdapter.notifyDataSetChanged();

                                //check if list is empty
                                if (tvList.isEmpty()){
                                    emptyText.setVisibility(View.VISIBLE);
                                    emptySubText.setVisibility(View.VISIBLE);
                                    empty.setVisibility(View.VISIBLE);
                                }else {
                                    emptyText.setVisibility(View.GONE);
                                    emptySubText.setVisibility(View.GONE);
                                    empty.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                tvSeriesAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    //onCreate Option Menu
    //Hide menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem notifications = menu.findItem(R.id.notification);
        notifications.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}