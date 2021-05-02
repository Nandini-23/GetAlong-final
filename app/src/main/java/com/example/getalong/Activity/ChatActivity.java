package com.example.getalong.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getalong.Adapter.MessagesAdapter;
import com.example.getalong.Model.Messages;
import com.example.getalong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String ReceiverImage, ReceiverUid, ReceiverName, SenderUID;
    CircleImageView profileImage;
    TextView receiverName;
    ImageView attachment;

    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;

    public static String sImage;
    public static String rImage;

    CardView sendBtn;
    EditText editMessage;

    String senderRoom, receiverRoom;

    RecyclerView messageAdapter;
    ArrayList<Messages>  messagesArrayList;

    MessagesAdapter adapter;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        ReceiverName = getIntent().getStringExtra("name");
        ReceiverImage = getIntent().getStringExtra("ReceiverImage");
        ReceiverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        profileImage = findViewById(R.id.profile_image);
        receiverName = findViewById(R.id.receiver_name);
        attachment = findViewById(R.id.attachment);

        messageAdapter = findViewById(R.id.messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        adapter = new MessagesAdapter(ChatActivity.this, ReceiverUid,  messagesArrayList);
        messageAdapter.setAdapter(adapter);

        sendBtn = findViewById(R.id.sendBtn);
        editMessage = findViewById(R.id.editMessage);

        Picasso.get().load(ReceiverImage).into(profileImage);
        receiverName.setText(""+ReceiverName);

        SenderUID = firebaseAuth.getUid();

        senderRoom = SenderUID+ReceiverUid;
        receiverRoom = ReceiverUid+SenderUID;

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messagesArrayList.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messages.setMessageId(snapshot.getKey());
                    messagesArrayList.add(messages);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sImage = snapshot.child("imageUri").getValue().toString();
                rImage = ReceiverImage;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();

                if(message.isEmpty())
                {
                    Toast.makeText(ChatActivity.this, "Please enter valid message", Toast.LENGTH_SHORT).show();
                    return;
                }

                editMessage.setText("");
                Date date = new Date();

                Messages messages = new Messages(message, SenderUID, date.getTime());

                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
            }
        });

        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25)
        {
            if(data != null)
            {
                if(data.getData() != null)
                {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful())
                            {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String message = editMessage.getText().toString();

                                        editMessage.setText("");
                                        Date date = new Date();

                                        Messages messages = new Messages(message, SenderUID, date.getTime());
                                        messages.setMessage("photo");
                                        messages.setImageUrl(filePath);

                                        database = FirebaseDatabase.getInstance();
                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .push()
                                                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}