package com.example.myfirebasechatexam;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends FirebaseRecyclerAdapter<ChatMessage, RecyclerAdapter.MessageViewHolder> {
    private FirebaseAuth mFirebaseAuth;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerAdapter(@NonNull FirebaseRecyclerOptions<ChatMessage> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser().getDisplayName().equals(model.getName())){
            // 내 채팅
            holder.otherChat.setVisibility(View.GONE);
            holder.myChat.setVisibility(View.VISIBLE);
            holder.tvMyMsg.setText(model.getText());
        } else {
            // 다른 사람 채팅
            holder.otherChat.setVisibility(View.VISIBLE);
            holder.myChat.setVisibility(View.GONE);
            holder.tvOtherMsg.setText(model.getText());
            holder.tvSender.setText(model.getName());
            if (model.getPhotoUrl() == null) {
                holder.imageSender.setImageDrawable(ContextCompat.getDrawable(holder.imageSender.getContext(),
                        R.drawable.ic_account_circle_black_24dp));
            } else {
                Glide.with(holder.imageSender.getContext())
                        .load(model.getPhotoUrl())
                        .into(holder.imageSender);
            }
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_message, viewGroup, false);
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout otherChat, myChat;
        CircleImageView imageSender;
        TextView tvSender;
        TextView tvMyMsg, tvOtherMsg;

        public MessageViewHolder(View v) {
            super(v);
            otherChat = itemView.findViewById(R.id.otherChat);
            imageSender = itemView.findViewById(R.id.imageSender);
            tvOtherMsg = itemView.findViewById(R.id.tvOtherMsg);
            tvSender = itemView.findViewById(R.id.tvSender);

            myChat = itemView.findViewById(R.id.myChat);
            tvMyMsg = itemView.findViewById(R.id.tvMyMsg);
        }
    }
}

