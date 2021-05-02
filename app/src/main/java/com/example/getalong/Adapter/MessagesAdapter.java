package com.example.getalong.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getalong.Activity.ChatActivity;
import com.example.getalong.Model.Messages;
import com.example.getalong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.getalong.Activity.ChatActivity.rImage;
import static com.example.getalong.Activity.ChatActivity.sImage;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    String recId;
    ArrayList<Messages> messagesArrayList;
    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, String recId, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
        this.recId = recId;
    }

    public MessagesAdapter( int ITEM_SEND, int ITEM_RECEIVE) {

        this.ITEM_SEND = ITEM_SEND;
        this.ITEM_RECEIVE = ITEM_RECEIVE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == ITEM_SEND)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout_item, parent, false);
            return new SenderViewHolder(view);
        } else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout_item, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages messages = messagesArrayList.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message ")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                database.getReference().child("chats").child(senderRoom)
                                        .child(messages.getMessageId())
                                        .setValue(null);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

                return false;
            }
        });

        if(holder.getClass() == SenderViewHolder.class)
        {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;

            if(messages.getMessage().equals("photo"))
            {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.txtMessage.setVisibility(View.GONE);
                Picasso.get().load(messages.getImageUrl()).placeholder(R.drawable.image_placeholder).into(viewHolder.image);
            }
            viewHolder.txtMessage.setText(messages.getMessage());
            Picasso.get().load(sImage).into(viewHolder.circleImageView);
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if(messages.getMessage().equals("photo"))
            {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.txtMessage.setVisibility(View.GONE);
                Picasso.get().load(messages.getImageUrl()).placeholder(R.drawable.image_placeholder).into(viewHolder.image);
            }
            viewHolder.txtMessage.setText(messages.getMessage());
            Picasso.get().load(rImage).into(viewHolder.circleImageView);
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {

        Messages messages = messagesArrayList.get(position);

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId()))
        {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView txtMessage;
        ImageView image;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profile_image);
            txtMessage = itemView.findViewById(R.id.txtMessages);
            image = itemView.findViewById(R.id.image);
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView txtMessage;
        ImageView image;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profile_image);
            txtMessage = itemView.findViewById(R.id.txtMessages);
            image = itemView.findViewById(R.id.image);
        }
    }
}
