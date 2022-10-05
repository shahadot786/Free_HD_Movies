package com.hdmovies.onlinecinema.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.MoviesModel;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.databinding.ActivityAddMoviesBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AddMoviesActivity extends AppCompatActivity {
    ActivityAddMoviesBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    ProgressDialog dialog;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    Uri imageUri;
    Intent intent;
    String mName,mYear,mDes,mRating,mImdb,mTrailer,mG1,mG2,mG3,mKeywords;
    String server_1,server_2,server_3,server_4;
    private TextInputLayout movieNameInput, movieYearInput, descriptionsInput, categoriesInput, ratingInput, imdbInput, downloadInput, trailerInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMoviesBinding.inflate(getLayoutInflater());
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
        mName = intent.getStringExtra("movieName");
        mYear = intent.getStringExtra("movieYear");
        mDes = intent.getStringExtra("movieDes");
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

        //find id
        movieNameInput = findViewById(R.id.text_input_movieName);
        movieYearInput = findViewById(R.id.text_input_movieYear);
        descriptionsInput = findViewById(R.id.text_input_descriptions);
        categoriesInput = findViewById(R.id.text_input_genre);
        ratingInput = findViewById(R.id.text_input_rating);
        imdbInput = findViewById(R.id.text_input_imdb_link);
        downloadInput = findViewById(R.id.text_input_server_1);
        trailerInput = findViewById(R.id.text_input_trailer_link);
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
        dialog = new ProgressDialog(AddMoviesActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Added Movies");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        AddMoviesActivity.this.setTitle("Add Movies");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //for edit movies
        binding.movieNameEdit.setText(mName);
        binding.movieYearEdit.setText(mName);

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
        //upload image
        binding.movieImageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(AddMoviesActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //gallery open
                    ImagePicker.with(AddMoviesActivity.this)
                            .galleryOnly()
                            .crop(3f,4f)	    			//Crop image(Optional), Check Customization for more option
                            .compress(256)			//Final image size will be less than 312kb (Optional)
                            .maxResultSize(550, 550)	//Final image resolution will be less than 1080 x 1080(Optional)
                            .start();
                } else {
                    toastText.setText(R.string.no_connection_text);
                    toast.show();
                }
            }
        });

        binding.addMoviesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(AddMoviesActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //send data to firebase
                    if (moviesNameValidation() && moviesYearValidation() && descriptionValidation() && categoriesValidation() && ratingValidation() && imdbValidation() && downloadValidation() && trailerValidation()) {
                        dialog.show();
                        if (imageUri != null) {
                            StorageReference reference = storage.getReference().child("movie_images")
                                    .child(FirebaseAuth.getInstance().getUid() + UUID.randomUUID().toString());
                            reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        toastText.setText(R.string.UploadSuccessfully);
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(@NonNull Uri uri) {
                                                MoviesModel moviesModel = new MoviesModel();
                                                moviesModel.setPostedAt(new Date().getTime());
                                                moviesModel.setMovieImage(uri.toString());
                                                moviesModel.setMovieName(Objects.requireNonNull(binding.movieNameEdit.getText()).toString());
                                                moviesModel.setMovieYear(Objects.requireNonNull(binding.movieYearEdit.getText()).toString());
                                                moviesModel.setType(Objects.requireNonNull(binding.typeEdit.getText()).toString());
                                                moviesModel.setDescription(Objects.requireNonNull(binding.descriptionsEditText.getText()).toString());
                                                moviesModel.setKeywords(Objects.requireNonNull(binding.keywordsEditText.getText()).toString());
                                                moviesModel.setImdbLink(Objects.requireNonNull(binding.imdbLinkEditText.getText()).toString());
                                                moviesModel.setTrailerLink(Objects.requireNonNull(binding.playLinkEditText.getText()).toString());
                                                moviesModel.setServer_1(Objects.requireNonNull(binding.server1EditText.getText()).toString());
                                                moviesModel.setServer_2(Objects.requireNonNull(binding.server2EditText.getText()).toString());
                                                moviesModel.setServer_3(Objects.requireNonNull(binding.server3EditText.getText()).toString());
                                                moviesModel.setServer_4(Objects.requireNonNull(binding.server4EditText.getText()).toString());
                                                //convert string to integer
                                                String rate = Objects.requireNonNull(binding.ratingEditText.getText()).toString();
                                                float rating = Float.parseFloat(rate);
                                                moviesModel.setRating(rating);

                                                //genres
                                                moviesModel.setGenre(Objects.requireNonNull(binding.genreEditText.getText()).toString());
                                                moviesModel.setGenre1(Objects.requireNonNull(binding.genre1EditText.getText()).toString());
                                                moviesModel.setGenre2(Objects.requireNonNull(binding.genre2EditText.getText()).toString());

                                                database.getReference().child("Movies")
                                                        .push()
                                                        .setValue(moviesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(@NonNull Void unused) {
                                                        dialog.dismiss();
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        toastText.setText(R.string.WrongImageUpload);
                                    }
                                    toast.show();
                                }
                            });
                        }
                    }

                } else {
                    toastText.setText(R.string.no_connection_text);
                    toast.show();
                }
            }
        });

    }//onCreate Ends

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        binding.moviesImages.setImageURI(uri);
        imageUri = uri;
    }

    public boolean moviesNameValidation() {
        String sendTextInput = Objects.requireNonNull(movieNameInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            movieNameInput.setError("Name is required");
            return false;
        } else {
            movieNameInput.setError(null);
            return true;
        }
    }

    public boolean moviesYearValidation() {
        String sendTextInput = Objects.requireNonNull(movieYearInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            movieYearInput.setError("Year is required");
            return false;
        } else {
            movieYearInput.setError(null);
            return true;
        }
    }

    public boolean descriptionValidation() {
        String sendTextInput = Objects.requireNonNull(descriptionsInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            descriptionsInput.setError("Descriptions is required");
            return false;
        } else {
            descriptionsInput.setError(null);
            return true;
        }
    }

    public boolean categoriesValidation() {
        String categories = Objects.requireNonNull(categoriesInput.getEditText()).getText().toString().trim();
        if (categories.isEmpty()) {
            categoriesInput.setError("Categories is required.");
            return false;
        } else {
            categoriesInput.setError(null);
            return true;
        }
    }

    public boolean ratingValidation() {
        String ratings = Objects.requireNonNull(ratingInput.getEditText()).getText().toString().trim();
        if (ratings.isEmpty()) {
            ratingInput.setError("Rating is required.");
            return false;
        } else {
            ratingInput.setError(null);
            return true;
        }
    }

    public boolean imdbValidation() {
        String imdb = Objects.requireNonNull(imdbInput.getEditText()).getText().toString().trim();
        if (imdb.isEmpty()) {
            imdbInput.setError("IMDB Link is required.");
            return false;
        } else {
            imdbInput.setError(null);
            return true;
        }
    }

    public boolean downloadValidation() {
        String download = Objects.requireNonNull(downloadInput.getEditText()).getText().toString().trim();
        if (download.isEmpty()) {
            downloadInput.setError("Download Link is required.");
            return false;
        } else {
            downloadInput.setError(null);
            return true;
        }
    }

    public boolean trailerValidation() {
        String trailer = Objects.requireNonNull(trailerInput.getEditText()).getText().toString().trim();
        if (trailer.isEmpty()) {
            trailerInput.setError("Trailer Link is required.");
            return false;
        } else {
            trailerInput.setError(null);
            return true;
        }
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}