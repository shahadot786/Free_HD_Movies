package com.watchfreemovies.freehdcinema786.Fragment;

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
import com.watchfreemovies.freehdcinema786.Activities.AddFeedsActivity;
import com.watchfreemovies.freehdcinema786.Activities.FeedBackActivity;
import com.watchfreemovies.freehdcinema786.Activities.NotificationsActivity;
import com.watchfreemovies.freehdcinema786.Activities.UserProfilesActivity;
import com.watchfreemovies.freehdcinema786.Adapter.FeedsAdapter;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.FeedsModel;
import com.watchfreemovies.freehdcinema786.Model.NotificationsModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FeedsFragment extends Fragment {

    ShimmerRecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<FeedsModel> feedsList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    View empty;
    TextView emptyText,emptySubText;

    public FeedsFragment() {
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

        View view = inflater.inflate(R.layout.fragment_feeds,container,false);
        setHasOptionsMenu(true);

        //custom toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //empty view
        empty = view.findViewById(R.id.emptyList);
        emptyText = view.findViewById(R.id.empty);
        emptySubText = view.findViewById(R.id.emptyListText);

        //recyclerview
        recyclerView = view.findViewById(R.id.rv_feeds);
        recyclerView.showShimmerAdapter();

        //pro status
        ImageView proBadge = view.findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

        //write post click codes
        View feedsView = view.findViewById(R.id.feeds_view);
        feedsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddFeedsActivity.class));
            }
        });

        //firebase instance & user data code
        TextView userName = view.findViewById(R.id.userName);
        ImageView profileImage = view.findViewById(R.id.profileImage);
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

        //adapter data fetch
        feedsList = new ArrayList<>();
        FeedsAdapter adapter = new FeedsAdapter(feedsList,getContext());

        StaggeredGridLayoutManager feedsLayout = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        feedsLayout.setReverseLayout(false);
        recyclerView.setLayoutManager(feedsLayout);
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

        database.getReference().child("Feeds")
                .limitToLast(210)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        feedsList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FeedsModel model = dataSnapshot.getValue(FeedsModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(feedsList);
                            feedsList.add(model);
                        }
                        Collections.reverse(feedsList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.hideShimmerAdapter();
                        adapter.notifyDataSetChanged();

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
                database.getReference().child("Feeds")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                feedsList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    FeedsModel model = dataSnapshot.getValue(FeedsModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(feedsList);
                                    feedsList.add(model);
                                }
                                Collections.reverse(feedsList);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //for search menu
        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setVisible(false);
        /*SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search anything...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });*/

        //for Notifications menu
        MenuItem notifications = menu.findItem(R.id.notification);
        //notification icon change
        database.getReference()
                .child("Notifications")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NotificationsModel model = dataSnapshot.getValue(NotificationsModel.class);
                            assert model != null;
                            boolean checkOpens = model.isCheckOpen();
                            if (!checkOpens) {
                                notifications.setIcon(R.drawable.ic_notification_active_24);
                            } else {
                                notifications.setIcon(R.drawable.ic_notification_icon);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    //search method
   /* private void search(String str) {
        ArrayList<FeedsModel> myList = new ArrayList<>();
        for (FeedsModel object : feedsList){
            if (object.getPostDescription().toLowerCase().contains(str.toLowerCase())){
                myList.add(object);
            }
        }

        FeedsAdapter feedsAdapter = new FeedsAdapter(myList,getContext());
        recyclerView.setAdapter(feedsAdapter);

        if (myList.isEmpty()){
            emptyText.setVisibility(View.VISIBLE);
            emptySubText.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
        }else {
            emptyText.setVisibility(View.GONE);
            emptySubText.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.notification) {
            startActivity(new Intent(getContext(), NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}