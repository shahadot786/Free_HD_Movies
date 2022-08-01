package com.watchfreemovies.freehdcinema786.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.FeedsModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.Utils.NetworkChecks;
import com.watchfreemovies.freehdcinema786.databinding.ActivityAddFeedsBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AddFeedsActivity extends AppCompatActivity {

    ActivityAddFeedsBinding binding;
    Uri imageUri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    EditText postDescription;
    ImageView postImage;
    ImageView proBadge;
    String description;
    Dialog postDialog;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFeedsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //find id
        postDescription = findViewById(R.id.postDescriptions);
        postImage = findViewById(R.id.postImage);
        proBadge = findViewById(R.id.proBadge);

        postDialog = new Dialog(this);
        adNetwork = new AdNetwork(this);
        NetworkChecks networkChecks = new NetworkChecks(this);
        //ad initialization
        MaxAdView mRecAd = findViewById(R.id.mRec);
        //banner
        LinearLayout unityBannerAd = findViewById(R.id.banner_ad);
        //adNetwork.loadBannerAd();
        adNetwork.loadUnityBannerAd();
        //check premium
        if (UiConfig.BANNER_AD_VISIBILITY) {
            unityBannerAd.setVisibility(View.VISIBLE);
        } else {
            unityBannerAd.setVisibility(View.GONE);
        }
        //check pro status
        ImageView proBadge = findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

        //instance
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //progress dialog
        dialog = new ProgressDialog(AddFeedsActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        AddFeedsActivity.this.setTitle("Create Post");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //get database value
        //fetch update image and username
        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            Picasso.get()
                                    .load(userModel.getProfile())
                                    .placeholder(R.drawable.ic_profile_default_image)
                                    .into(binding.profileImage);
                            binding.userName.setText(userModel.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //post descriptions
        binding.postDescriptions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                description = binding.postDescriptions.getText().toString();
                if (!description.isEmpty()){
                    binding.postBtn.setBackgroundResource(R.drawable.ic_post_btn_bg_active);
                    binding.postBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.postBtn.setEnabled(true);
                }else {
                    binding.postBtn.setBackgroundResource(R.drawable.ic_post_btn_bg);
                    binding.postBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                    binding.postBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //post image data
        binding.uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(AddMoviesActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //gallery open
                    ImagePicker.with(AddFeedsActivity.this)
                            .crop(1f,1f)	    			//Crop image(Optional), Check Customization for more option
                            .compress(512)			//Final image size will be less than 1 MB(Optional)
                            .maxResultSize(620, 620)	//Final image resolution will be less than 1080 x 1080(Optional)
                            .start();

                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });

        //post btn data
        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(AddMoviesActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    if (imageValidation()){
                        //showed conformation dialog
                        //find dialog
                        TextView btnYes,btnCancle;
                        postDialog.setContentView(R.layout.custom_add_post_dialog_layout);

                        btnYes = postDialog.findViewById(R.id.postYes);
                        btnCancle = postDialog.findViewById(R.id.postCancle);

                        btnCancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                postDialog.dismiss();
                            }
                        });

                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                postDialog.dismiss();
                                dialog.show();
                                if (imageUri != null){
                                    StorageReference reference = storage.getReference().child("post_images")
                                            .child(FirebaseAuth.getInstance().getUid() + UUID.randomUUID().toString());
                                    reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()){
                                                toastText.setText(R.string.UploadSuccessfully);
                                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        FeedsModel model = new FeedsModel();
                                                        model.setPostImage(uri.toString());
                                                        model.setPostDescription(binding.postDescriptions.getText().toString());
                                                        model.setPostedBy(FirebaseAuth.getInstance().getUid());
                                                        model.setPostedAt(new Date().getTime());

                                                        database.getReference().child("Feeds")
                                                                .push()
                                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(@NonNull Void unused) {
                                                                dialog.dismiss();
                                                                finish();
                                                            }
                                                        });

                                                    }
                                                });
                                            }else {
                                                toastText.setText(R.string.WrongImageUpload);
                                            }
                                            toast.show();
                                        }
                                    });
                                }
                            }
                        });

                        postDialog.setCancelable(false);
                        postDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postDialog.show();
                    }


                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });




    }//ends of onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        binding.postImage.setImageURI(uri);
        imageUri = uri;
        binding.postImage.setVisibility(View.VISIBLE);
        binding.postBtn.setBackgroundResource(R.drawable.ic_post_btn_bg_active);
        binding.postBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        binding.postBtn.setEnabled(true);
    }

    //post description validation
    public boolean imageValidation(){
        if (imageUri ==null){
            imageDialog();
            return false;
        }else {
            return true;
        }
    }

    //on Exit Dialog
    public void imageDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AddFeedsActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setIcon(R.drawable.ic_main_icon_round);
        dialog.setTitle("Oops!");
        dialog.setMessage("Image is required!!");
        dialog.setPositiveButton("OK", (dialogInterface, i) -> {

        });
        dialog.show();
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}