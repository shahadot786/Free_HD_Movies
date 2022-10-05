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

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdmovies.onlinecinema.Activities.FeedsDetailsActivity;
import com.hdmovies.onlinecinema.Model.FeedsModel;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.databinding.AddFeedsRvSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class FeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int ITEM_VIEW = 0;//never changed
    ArrayList<FeedsModel> list;
    ArrayList<FeedsModel> listAll;
    Context context;
    AdNetwork adNetwork;

    public FeedsAdapter(ArrayList<FeedsModel> list, Context context) {
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
            View view = layoutInflater.inflate(R.layout.add_feeds_rv_sample,parent,false);
            return new viewHolder(view);
        }else {
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

    public class viewHolder extends RecyclerView.ViewHolder{

        AddFeedsRvSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AddFeedsRvSampleBinding.bind(itemView);
        }

        @SuppressLint("SetTextI18n")
        public void bindData (FeedsModel model){
            String time = TimeAgo.using(model.getPostedAt());
            String likes = model.getPostLikes() +"";
            String views = model.getPostViews() + "";
            String comments = model.getCommentCount() + "";
            String shares = model.getShareCount() + "";
            //set post image
            Picasso.get()
                    .load(model.getPostImage())
                    .placeholder(R.drawable.ic_placeholder_dark)
                    .into(binding.posImage);

            //1K and 1M likes logic
            int like = Integer.parseInt(likes);
            if (like >= 1000) {
                binding.countLikes2.setText((like / 1000) + "." + ((like % 1000) / 100) + "K");
            } else {
                binding.countLikes2.setText(likes);
            }
            if (like >= 1000000) {
                binding.countLikes2.setText((like / 1000000) + "." + ((like % 1000000) / 10000) + "M");
            }

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

            //1K and 1M comments logic
            int comment = Integer.parseInt(comments);
            if (comment >= 1000) {
                binding.comment.setText((comment / 1000) + "." + ((comment % 1000) / 100) + "K");
            } else {
                binding.comment.setText(comments);
            }
            if (comment >= 1000000) {
                binding.comment.setText((comment / 1000000) + "." + ((comment % 1000000) / 10000) + "M");
            }

            //1K and 1M shares logic
            int share = Integer.parseInt(shares);
            if (share >= 1000) {
                binding.share.setText((share / 1000) + "." + ((share % 1000) / 100) + "K");
            } else {
                binding.share.setText(shares);
            }
            if (share >= 1000000) {
                binding.share.setText((share / 1000000) + "." + ((share % 1000000) / 10000) + "M");
            }

            //fetch user name and profile
             FirebaseDatabase.getInstance().getReference().child("UserData").child(model.getPostedBy())
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
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //check multiple views
            FirebaseDatabase.getInstance().getReference()
                    .child("Feeds")
                    .child(model.getPostId())
                    .child("views")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //post watch code
                            binding.feedsRVSample.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //goto topics details
                                    //set value for discus details
                                    Intent intent = new Intent(context.getApplicationContext(), FeedsDetailsActivity.class);
                                    intent.putExtra("postId", model.getPostId());
                                    intent.putExtra("postedBy", model.getPostedBy());
                                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    context.getApplicationContext().startActivity(intent);
                                    adNetwork.showInterstitialAdCount();

                                    //fetch firebase database
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Feeds")
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
                                                                .child("Feeds")
                                                                .child(model.getPostId())
                                                                .child("views")
                                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                                .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(@NonNull Void unused) {
                                                                FirebaseDatabase.getInstance().getReference()
                                                                        .child("Feeds")
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
