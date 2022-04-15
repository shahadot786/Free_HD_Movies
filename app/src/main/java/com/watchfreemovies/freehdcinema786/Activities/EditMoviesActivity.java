package com.watchfreemovies.freehdcinema786.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Model.MoviesModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.ActivityEditMoviesBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;

public class EditMoviesActivity extends AppCompatActivity {

    ActivityEditMoviesBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProgressDialog dialog;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    Intent intent;
    String postId,movieImage,mName,mYear,mDes,mRating,mImdb,mTrailer,mG1,mG2,mG3,mKeywords;
    String server_1,server_2,server_3,server_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        intent = getIntent();
        postId = intent.getStringExtra("postId");
        mName = intent.getStringExtra("movieName");
        mYear = intent.getStringExtra("movieYear");
        mDes = intent.getStringExtra("movieDes");
        movieImage = intent.getStringExtra("movieImage");
        mRating = intent.getStringExtra("movieRating");
        mImdb = intent.getStringExtra("imdbLink");
        mTrailer = intent.getStringExtra("trailerLink");
        mKeywords = intent.getStringExtra("keywords");
        mG1 = intent.getStringExtra("g1");
        mG2 = intent.getStringExtra("g2");
        mG3 = intent.getStringExtra("g3");
        server_1 = intent.getStringExtra("server_1");
        server_2 = intent.getStringExtra("server_2");
        server_3 = intent.getStringExtra("server_3");
        server_4 = intent.getStringExtra("server_4");

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //progress dialog
        dialog = new ProgressDialog(EditMoviesActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Update Movies");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        EditMoviesActivity.this.setTitle("Update Movies");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        binding.movieNameEdit.setText(mName);
        binding.movieYearEdit.setText(mYear);
        binding.descriptionsEditText.setText(mDes);
        binding.moviesImagesEdit.setText(movieImage);
        binding.keywordsEditText.setText(mKeywords);
        binding.imdbLinkEditText.setText(mImdb);
        binding.playLinkEditText.setText(mTrailer);
        binding.ratingEditText.setText(mRating);
        binding.genreEditText.setText(mG1);
        binding.genre1EditText.setText(mG2);
        binding.genre2EditText.setText(mG3);
        binding.server1EditText.setText(server_1);
        binding.server2EditText.setText(server_2);
        binding.server3EditText.setText(server_3);
        binding.server4EditText.setText(server_4);


        //get database value
        //fetch update image
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


        binding.updateMoviesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                MoviesModel model = new MoviesModel();
                model.setPostedAt(new Date().getTime());
                model.setMovieImage(binding.moviesImagesEdit.getText().toString());
                model.setMovieName(Objects.requireNonNull(binding.movieNameEdit.getText()).toString());
                model.setMovieYear(Objects.requireNonNull(binding.movieYearEdit.getText()).toString());
                model.setType(Objects.requireNonNull(binding.typeEdit.getText()).toString());
                model.setDescription(Objects.requireNonNull(binding.descriptionsEditText.getText()).toString());
                model.setKeywords(Objects.requireNonNull(binding.keywordsEditText.getText()).toString());
                model.setImdbLink(Objects.requireNonNull(binding.imdbLinkEditText.getText()).toString());
                model.setTrailerLink(Objects.requireNonNull(binding.playLinkEditText.getText()).toString());
                model.setServer_1(Objects.requireNonNull(binding.server1EditText.getText()).toString());
                model.setServer_2(Objects.requireNonNull(binding.server2EditText.getText()).toString());
                model.setServer_3(Objects.requireNonNull(binding.server3EditText.getText()).toString());
                model.setServer_4(Objects.requireNonNull(binding.server4EditText.getText()).toString());
                //convert string to integer
                String rate = Objects.requireNonNull(binding.ratingEditText.getText()).toString();
                float rating = Float.parseFloat(rate);
                model.setRating(rating);

                //genres
                model.setGenre(Objects.requireNonNull(binding.genreEditText.getText()).toString());
                model.setGenre1(Objects.requireNonNull(binding.genre1EditText.getText()).toString());
                model.setGenre2(Objects.requireNonNull(binding.genre2EditText.getText()).toString());
                database.getReference()
                        .child("Movies")
                        .child(postId)
                        .setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                finish();
                            }
                        });
            }
        });

    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}