package com.watchfreemovies.freehdcinema786.Adapter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Activities.FeedsDetailsActivity;
import com.watchfreemovies.freehdcinema786.Model.NotificationsModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.NotificationRvSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.viewHolder>{

    ArrayList<NotificationsModel> list;
    Context context;
    AdNetwork adNetwork;

    public NotificationsAdapter(ArrayList<NotificationsModel> list, Context context) {
        this.list = list;
        this.context = context;

        adNetwork = new AdNetwork((Activity) context);
        adNetwork.loadInterstitialAd();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        NotificationsModel notifications = list.get(position);
        String time = TimeAgo.using(notifications.getNotificationAt());
        String type = notifications.getType();
        //String commentedText = notifications.getCommentedText();

        FirebaseDatabase.getInstance().getReference()
                .child("UserData")
                .child(notifications.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        assert userModel != null;
                        Picasso.get()
                                .load(userModel.getProfile())
                                .placeholder(R.drawable.ic_profile_default_image)
                                .into(holder.binding.profileImage);
                        holder.binding.notificationTime.setText(time);

                        if (type.equals("likes")) {
                            holder.binding.notificationText.setText(Html.fromHtml("<span style=\"font-weight:bold; color:#15c55d\">"+
                                    userModel.getUserName()+" "+"</span>" + " liked your post"));
                        }
                        /*else if (type.equals("comments")){
                            holder.binding.notificationText.setText(Html.fromHtml("<span style=\"font-weight:bold; color:#15c55d\">"+
                                    userModel.getUserName()+" "+"</span>" + " comment on your post- " +commentedText));
                        }*/

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Notifications")
                        .child(notifications.getPostedBy())
                        .child(notifications.getNotificationId())
                        .child("checkOpen")
                        .setValue(true);
                holder.binding.openNotification.setBackgroundResource(R.drawable.ic_notification_open_bg);
                Intent intent = new Intent(context, FeedsDetailsActivity.class);
                intent.putExtra("postId", notifications.getPostId());
                intent.putExtra("postedBy", notifications.getPostedBy());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
                adNetwork.showInterstitialAdCount();
            }
        });

        boolean checkOpen = notifications.isCheckOpen();
        if (checkOpen) {
            holder.binding.openNotification.setBackgroundResource(R.drawable.ic_notification_open_bg);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        NotificationRvSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = NotificationRvSampleBinding.bind(itemView);
        }
    }
}
