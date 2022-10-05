package com.hdmovies.onlinecinema.Adapter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdmovies.onlinecinema.Activities.MoviesDetailsActivity;
import com.hdmovies.onlinecinema.Model.MoviesModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.databinding.AddMoviesRvSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW = 0;//never changed
    ArrayList<MoviesModel> list;
    ArrayList<MoviesModel> listAll;
    Context context;
    AdNetwork adNetwork;

    public MoviesAdapter(ArrayList<MoviesModel> list, Context context) {
        this.list = list;
        this.listAll = new ArrayList<>(list);
        this.context = context;

        adNetwork = new AdNetwork((Activity) context);
        adNetwork.loadInterstitialAd();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == ITEM_VIEW) {
            View view = layoutInflater.inflate(R.layout.add_movies_rv_sample, parent, false);
            return new viewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == ITEM_VIEW) {
            ((viewHolder) holder).bindData(list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_VIEW;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        AddMoviesRvSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AddMoviesRvSampleBinding.bind(itemView);

        }

        @SuppressLint("SetTextI18n")
        public void bindData(MoviesModel model) {
            String movieName = model.getMovieName();
            String likes = model.getLikesCount() + "";
            String views = model.getPostViews() + "";
            String rating = String.valueOf(model.getRating());
            //se value
            binding.movieName.setText(movieName);
            binding.rating.setText(rating);
            //set movie image
            Picasso.get()
                    .load(model.getMovieImage())
                    .placeholder(R.drawable.ic_placeholder_dark)
                    .into(binding.movieImage);

            //1K and 1M views logic
            int view = Integer.parseInt(views);
            if (view >= 1000) {
                binding.views.setText((view / 1000) + "." + ((view % 1000) / 100) + "K");
            } else {
                binding.views.setText(views);
            }
            if (view >= 1000000) {
                binding.views.setText((view / 1000000) + "." + ((view % 1000000) / 10000) + "M");
            }

            //1K and 1M likes logic
            int like = Integer.parseInt(likes);
            if (like >= 1000) {
                binding.countLikes.setText((like / 1000) + "." + ((like % 1000) / 100) + "K");
            } else {
                binding.countLikes.setText(likes);
            }
            if (like >= 1000000) {
                binding.countLikes.setText((like / 1000000) + "." + ((like % 1000000) / 10000) + "M");
            }
            //check multiple views
            FirebaseDatabase.getInstance().getReference()
                    .child("Movies")
                    .child(model.getPostId())
                    .child("views")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //post watch code
                            binding.moviesRVSample.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //goto topics details
                                    //set value for discus details
                                    Intent intent = new Intent(context.getApplicationContext(), MoviesDetailsActivity.class);
                                    intent.putExtra("postId", model.getPostId());
                                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    context.getApplicationContext().startActivity(intent);
                                    adNetwork.showInterstitialAdCount();
                                    //fetch firebase database
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Movies")
                                            .child(model.getPostId())
                                            .child("views")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        binding.views.setText(views);
                                                    } else {
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Movies")
                                                                .child(model.getPostId())
                                                                .child("views")
                                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                                .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(@NonNull Void unused) {
                                                                FirebaseDatabase.getInstance().getReference()
                                                                        .child("Movies")
                                                                        .child(model.getPostId())
                                                                        .child("postViews")
                                                                        .setValue(model.getPostViews() + 1)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(@NonNull Void unused) {
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}
