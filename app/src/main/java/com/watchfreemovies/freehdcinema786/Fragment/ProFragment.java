package com.watchfreemovies.freehdcinema786.Fragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Activities.MainActivity;
import com.watchfreemovies.freehdcinema786.Activities.UserProfilesActivity;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.FragmentProBinding;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;


public class ProFragment extends Fragment {
    FragmentProBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    TextView toastText;
    View toastLayout;
    Toast toast;


    public ProFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pro, container, false);
        binding = FragmentProBinding.inflate(inflater, container, false);
        //Toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        binding.proVersion.setText(BuildConfig.VERSION_NAME);
        //catch the current year
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        binding.txtCopyrights.setText("© " + year + " All Rights Reserved");

        //fetch username
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
                                    .into(binding.profileImage);
                            binding.proUserName.setText(user.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //contact us
        binding.contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MainActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                } else {
                    noConnectionDialog();
                }
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


        return binding.getRoot();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setVisible(false);
        MenuItem notifications = menu.findItem(R.id.notification);
        notifications.setVisible(false);
    }

    private void noConnectionDialog() {
        //custom dialog
        Dialog noConnection;
        TextView btnClose;
        noConnection = new Dialog(getActivity());
        noConnection.setContentView(R.layout.custom_no_connections_layout);
        noConnection.setCancelable(false);
        noConnection.setCanceledOnTouchOutside(false);
        noConnection.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        noConnection.show();
        btnClose = noConnection.findViewById(R.id.closeBtn);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noConnection.dismiss();
            }
        });
    }
}