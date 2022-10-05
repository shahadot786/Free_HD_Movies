package com.hdmovies.onlinecinema.Fragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hdmovies.onlinecinema.Activities.EditProfileActivity;
import com.hdmovies.onlinecinema.Activities.FavoriteListActivity;
import com.hdmovies.onlinecinema.Activities.FeedBackActivity;
import com.hdmovies.onlinecinema.Activities.MoviesDetailsActivity;
import com.hdmovies.onlinecinema.Activities.MyPostActivity;
import com.hdmovies.onlinecinema.Activities.PremiumActivity;
import com.hdmovies.onlinecinema.Activities.SavedPostActivity;
import com.hdmovies.onlinecinema.Activities.SignInActivity;
import com.hdmovies.onlinecinema.Activities.WebviewActivity;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog dialog;
    TextView toastText;
    View toastLayout;
    Toast toast;
    //Start activity for image upload result
    //cover
    ActivityResultLauncher<String> coverLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        binding.coverPhoto.setImageURI(uri);
                        dialog.show();
                        final StorageReference reference = storage.getReference().child("cover_photo")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                dialog.dismiss();
                                toastText.setText(R.string.UploadSuccessfully);
                                toast.show();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(@NonNull Uri uri) {
                                        database.getReference()
                                                .child("UserData")
                                                .child(Objects.requireNonNull(auth.getUid()))
                                                .child("coverPhoto")
                                                .setValue(uri.toString());
                                    }
                                });
                            }
                        });
                    }
                }
            });
    //profile
    ActivityResultLauncher<String> profileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        binding.profileImage.setImageURI(uri);
                        dialog.show();
                        final StorageReference reference = storage.getReference().child("profile_image")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                dialog.dismiss();
                                toastText.setText(R.string.UploadSuccessfully);
                                toast.show();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(@NonNull Uri uri) {
                                        database.getReference()
                                                .child("UserData")
                                                .child(Objects.requireNonNull(auth.getUid()))
                                                .child("profile")
                                                .setValue(uri.toString());
                                    }
                                });
                            }
                        });
                    }
                }
            });

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        //Toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //dialog
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Image Uploading");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //pro status
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            binding.proBadge.setVisibility(View.GONE);
        } else {
            binding.proBadge.setVisibility(View.VISIBLE);
        }
        //promotion visibility
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            binding.promotion.setVisibility(View.VISIBLE);
        } else {
            binding.promotion.setVisibility(View.GONE);
        }

        /*check if user is sign in or sign out*/
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        } else {
            //google sign in data fetch with image
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
                                binding.userName.setText(user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            //cover and profile update images
            database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel user = snapshot.getValue(UserModel.class);
                                assert user != null;
                                Picasso.get()
                                        .load(user.getCoverPhoto())
                                        .placeholder(R.drawable.ic_placeholder_dark)
                                        .into(binding.coverPhoto);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            //other information data fetch
            //update profile data fetch
            database.getReference().child("UserData")
                    .child(Objects.requireNonNull(auth.getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel profile = snapshot.getValue(UserModel.class);
                                assert profile != null;
                                String userName = profile.getUserName();
                                String profession = profile.getProfession();
                                String bio = profile.getUserBio();
                                String fb = profile.getFbLink();
                                String insta = profile.getInstaLink();
                                String github = profile.getGithubLink();
                                String linkedin = profile.getLinkedinLink();
                                String twitter = profile.getTwitterLink();

                                //set value
                                binding.userName.setText(userName);
                                if (profession.isEmpty()){
                                    binding.profession.setText(getResources().getString(R.string.profession));
                                }else {
                                    binding.profession.setText(profession);
                                }
                                if (bio.isEmpty()){
                                    binding.userBioText.setText(getResources().getString(R.string.about_yourself));
                                }else {
                                    binding.userBioText.setText(bio);
                                }
                                //insert link data
                                binding.linkFacebook.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (fb.isEmpty()) {
                                                toastText.setText(R.string.Pleaseupdateyourprofilefirst);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                                                intent.putExtra("IMDbLink", fb);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            noConnectionDialog();
                                        }
                                    }
                                });//facebook
                                binding.linkInstagram.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (insta.isEmpty()) {
                                                toastText.setText(R.string.Pleaseupdateyourprofilefirst);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                                                intent.putExtra("IMDbLink", insta);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            noConnectionDialog();
                                        }
                                    }
                                });//instagram
                                binding.linkGithub.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (github.isEmpty()) {
                                                toastText.setText(R.string.Pleaseupdateyourprofilefirst);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                                                intent.putExtra("IMDbLink", github);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            noConnectionDialog();
                                        }
                                    }
                                });//github
                                binding.linkLinkedIn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (linkedin.isEmpty()) {
                                                toastText.setText(R.string.Pleaseupdateyourprofilefirst);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                                                intent.putExtra("IMDbLink", linkedin);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            noConnectionDialog();
                                        }
                                    }
                                });//linkedin
                                binding.linkTwitter.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (twitter.isEmpty()) {
                                                toastText.setText(R.string.Pleaseupdateyourprofilefirst);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                                                intent.putExtra("IMDbLink", twitter);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            noConnectionDialog();
                                        }
                                    }
                                });//twitter


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }


        //upload cover
        binding.uploadCoverImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(FeedBackActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    coverLauncher.launch("image/*");
                } else {
                    noConnectionDialog();
                }

            }
        });
        //upload profile image
        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(FeedBackActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    profileLauncher.launch("image/*");
                } else {
                    noConnectionDialog();
                }
            }
        });
        //edit profile
        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
        //promo start button
        binding.promotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PremiumActivity.class);
                startActivity(intent);
            }
        });
        //bookmarks
        binding.bookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FavoriteListActivity.class);
                startActivity(intent);
            }
        });

        //my post
        binding.myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyPostActivity.class);
                startActivity(intent);
            }
        });

        //save post
        binding.savedPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SavedPostActivity.class);
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }//ends of onCreate

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setVisible(false);
        MenuItem notifications = menu.findItem(R.id.notification);
        notifications.setVisible(false);
    }
}