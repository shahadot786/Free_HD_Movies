package com.watchfreemovies.freehdcinema786.Fragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Activities.FeedBackActivity;
import com.watchfreemovies.freehdcinema786.Activities.UserProfilesActivity;
import com.watchfreemovies.freehdcinema786.Categories.CategoryDetailsActivity;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.FragmentCategoriesBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class CategoriesFragment extends Fragment {

    FragmentCategoriesBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    TextView toastText;
    View toastLayout;
    Toast toast;

    public CategoriesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        binding = FragmentCategoriesBinding.inflate(inflater,container,false);
        setHasOptionsMenu(true);

        //find id
        TextView userName = binding.userName;
        ImageView profileImage = binding.profileImage;

        //custom toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //pro status
        ImageView proBadge = view.findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

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
        binding.userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserProfilesActivity.class);
                intent.putExtra("postedBy", auth.getUid());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //new
        binding.newMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2022");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.newMovies.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.newMovies.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Thriller movies
        binding.thriller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Thriller");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.thriller.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.thriller.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Romantic movies
        binding.romantic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Romance");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.romantic.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.romantic.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Drama movies
        binding.drama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Drama");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.drama.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.drama.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //adventure movies
        binding.adventure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Adventure");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.adventure.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.adventure.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //action
        binding.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Action");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.action.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.action.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //korean movies
        binding.korean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Korean");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.korean.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.korean.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //chinese movies
        binding.chinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Chinese");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.chinese.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.chinese.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Horror movies
        binding.horror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Horror");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.horror.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.horror.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //comedy movies
        binding.comedy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Comedy");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.comedy.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.comedy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //crime movies
        binding.crime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Crime");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.crime.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.crime.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //others movies
        binding.fantasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Fantasy");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.fantasy.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.fantasy.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //war movies
        binding.war.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","War");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.war.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.war.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //sci-fi movies
        binding.sciFi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Sci-Fi");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.sciFi.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.sciFi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //family movies
        binding.family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Family");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.family.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.family.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //animation movies
        binding.animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Animation");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.animation.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.animation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //history movies
        binding.history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","History");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.history.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.history.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Music movies
        binding.music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Music");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.music.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.music.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //Documentary movies
        binding.documentary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Documentary");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.documentary.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.documentary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //biography movies
        binding.biography.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Biography");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.biography.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.biography.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });
        //western movies
        binding.western.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Western");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.western.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.western.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //mystery movies
        binding.mystery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Mystery");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.mystery.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.mystery.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //old is gold movies
        binding.oldIsGold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Old Is Gold");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.oldIsGold.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.oldIsGold.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //more movies
        binding.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","More");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.more.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.more.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //all time hits
        binding.evergreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","All Time Hits");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.evergreen.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.evergreen.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //flex favorites
        binding.flexFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Flex Favorites");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.flexFav.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.flexFav.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //top rated
        binding.topRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Top Rated");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.topRated.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.topRated.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //latest updated
        binding.latestUpdated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Latest");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.latestUpdated.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.latestUpdated.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2021 movies
        binding.y2021.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2021");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2021.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2021.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2020 movies
        binding.y2020.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2020");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2020.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2020.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2019 movies
        binding.y2019.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2019");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2019.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2019.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2018 movies
        binding.y2018.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2018");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2018.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2018.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2017 movies
        binding.y2017.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2017");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2017.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2017.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2016 movies
        binding.y2016.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2016");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2016.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2016.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2015 movies
        binding.y2015.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2015");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2015.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2015.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2014 movies
        binding.y2014.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2014");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2014.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2014.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2013 movies
        binding.y2013.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2013");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2013.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2013.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2012 movies
        binding.y2012.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2012");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2012.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2012.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2011 movies
        binding.y2011.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2011");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2011.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2011.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        //2010 movies
        binding.y2010.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","2010");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                binding.y2010.setTextColor(getResources().getColor(R.color.colorWhite));
                binding.y2010.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
            }
        });

        return binding.getRoot();
    }

    //onCreate Option Menu
    //Hide menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setVisible(false);
        MenuItem notifications = menu.findItem(R.id.notification);
        notifications.setVisible(false);
    }
}