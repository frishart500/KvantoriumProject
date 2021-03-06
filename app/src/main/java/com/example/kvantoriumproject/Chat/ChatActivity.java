package com.example.kvantoriumproject.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ActivityNavigator;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kvantoriumproject.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ListView messageListView;
    private ProgressBar progressBar;
    private AwesomeMessageAdapter adapter;
    private Button sendBtn, addPhotoBtn;
    private EditText messageEditText;
    private DatabaseReference dr;
    private String nameTitle, phoneTitle;
    private String recipientUserId;
    private ChildEventListener childEventListener;

    private NotificationManager nm;
    private final int NOTIFICTION_ID = 100;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        init();
        onClick();

        nameTitle = getIntent().getStringExtra("name");

        getSupportActionBar().setTitle(nameTitle);
        List<AwesomeMessage> awesomeMessages = new ArrayList<>();
        adapter = new AwesomeMessageAdapter(this, R.layout.message_item, awesomeMessages);
        messageListView.setAdapter(adapter);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        recipientUserId = getIntent().getStringExtra("email");
        phoneTitle = getIntent().getStringExtra("phone");
        System.out.println(phoneTitle);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                AwesomeMessage message = dataSnapshot.getValue(AwesomeMessage.class);
                nameTitle = getIntent().getStringExtra("name");

                if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        && message.getRecipient().equals(recipientUserId)) {
                    message.setMine(true);

                    adapter.add(message);
                } else if (message.getRecipient().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        && message.getSender().equals(recipientUserId)) {
                    message.setMine(false);
                    adapter.add(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        dr.addChildEventListener(childEventListener);
    }

    private void notifiacation(){
        nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setTicker("Новое уведомление")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Вам пришло сообщение")
                .setContentText("Нажмите чтобы увидеть");
        Notification notification = builder.build();
        nm.notify(NOTIFICTION_ID, notification);
    }

    private void init() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        messageListView = findViewById(R.id.list);
        sendBtn = findViewById(R.id.sendMessage);
        addPhotoBtn = findViewById(R.id.addPhoto);
        progressBar = findViewById(R.id.progressBar);
        messageEditText = findViewById(R.id.messageEditText);
        dr = FirebaseDatabase.getInstance().getReference("YourMessages");
    }

    private void onClick() {

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    sendBtn.setEnabled(true);
                } else {
                    sendBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(500)
        });

        View.OnClickListener BTNs = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.sendMessage) {

                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    final DatabaseReference uidRefGetUid = rootRef.child("User").child(uid);

                    notifiacation();

                    ValueEventListener eventListener = new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.child("name").getValue(String.class);
                            System.out.println(name);
                            AwesomeMessage message = new AwesomeMessage();
                            message.setText(messageEditText.getText().toString());
                            message.setName(name);
                            message.setSender(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            message.setRecipient(recipientUserId);

                            dr.push().setValue(message)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

                            messageEditText.setText("");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    uidRefGetUid.addListenerForSingleValueEvent(eventListener);
                } else if (v.getId() == R.id.addPhoto) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Выберите картинку"), 123);
                }
            }
        };
        sendBtn.setOnClickListener(BTNs);
        addPhotoBtn.setOnClickListener(BTNs);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("MessagePhotos");
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedItem = data.getData();
            StorageReference imageReference = storageReference.child(selectedItem.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(selectedItem);

            uploadTask = imageReference.putFile(selectedItem);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        AwesomeMessage message = new AwesomeMessage();
                        message.setRecipient(nameTitle);
                        message.setImgUrl(downloadUri.toString());
                        message.setSender(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        message.setRecipient(recipientUserId);
                        System.out.println(message.getImgUrl());
                        dr.push().setValue(message);


                    } else {

                    }
                }
            });

        }
    }

    private void status(String status){
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        FirebaseDatabase.getInstance().getReference("User").updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_call:
                makePhoneCall();
                break;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall() {


        if (ContextCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            String dial = "tel:" + phoneTitle;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }


    }
}