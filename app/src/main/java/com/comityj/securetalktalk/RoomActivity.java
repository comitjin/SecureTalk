package com.comityj.securetalktalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class RoomActivity extends AppCompatActivity {

    private long backpressedTime = 0;

    private FirebaseAuth mAuth;
    public static String useruid, useremail;
    private ArrayList<RoomDTO> roomdto = new ArrayList<>();
    FloatingActionButton roombtn, findbtn;
    ListView listroom;
    TabHost chattab;
    TextView tvname, tvemail, tvlogout, tvrevoke;
    CircleImageView imgprofile;
    RoomAdapter adapter;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Intent intent_L = new Intent(getApplicationContext(), LoginActivity.class);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        roombtn = findViewById(R.id.roomadd);
        findbtn = findViewById(R.id.roomfind);
        listroom = findViewById(R.id.roomlist);
        tvlogout =  findViewById(R.id.logout);
        tvrevoke = findViewById(R.id.revoke);
        tvname = findViewById(R.id.profile_name);
        tvemail = findViewById(R.id.profile_email);
        imgprofile = findViewById(R.id.profile_img);
        chattab = findViewById(R.id.chat_tab);

        chattab.setup();
        chattab.setOnTabChangedListener(this::onTabChanged);

        TabHost.TabSpec chatmenu = chattab.newTabSpec("Chatroom list");
        chatmenu.setContent(R.id.chatmenu)
                .setIndicator("채팅", getResources().getDrawable(R.drawable.round_chat_black_20));
        chattab.addTab(chatmenu);

        TabHost.TabSpec myInfo = chattab.newTabSpec("My info");
        myInfo.setContent(R.id.myinfo)
                .setIndicator("내 정보", getResources().getDrawable(R.drawable.round_perm_identity_black_20));
        chattab.addTab(myInfo);

        for (int i = 0; i < chattab.getTabWidget().getChildCount(); i++){
            chattab.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        chattab.setCurrentTab(0);
        chattab.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#81afef"));

        tvlogout.setPaintFlags(tvlogout.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvrevoke.setPaintFlags(tvrevoke.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tvemail.setText(user.getEmail());
        tvname.setText(user.getDisplayName());
        Picasso.get().load(user.getPhotoUrl().toString()).into(imgprofile);

        adapter = new RoomAdapter(roomdto, getLayoutInflater());
        listroom.setAdapter(adapter);

        useruid = user.getUid();
        useremail = user.getEmail();

        tvlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                startActivity(intent_L);
            }
        });

        tvrevoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeAccess();
                startActivity(intent_L);
            }
        });

        roombtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final
                final EditText roomname = new EditText(RoomActivity.this);
                roomname.setTextColor(Color.BLACK);
                AlertDialog.Builder roomadd = new AlertDialog.Builder(RoomActivity.this)
                        .setTitle("채팅방 생성")
                        .setMessage("생성할 채팅방 이름을 쓰세요.")
                        .setView(roomname)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("message").child(roomname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() == null){
                                            UserDTO userDTO = new UserDTO(useruid, useremail);
                                            databaseReference.child("message").child(roomname.getText().toString()).child("user").push().setValue(userDTO);

                                            Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
                                            intent.putExtra("roomName", roomname.getText().toString());
                                            startActivity(intent);
                                        }else {
                                            Toast.makeText(RoomActivity.this, "이미 존재하는 채팅방입니다.", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                roomadd.show();
            }
        });

        findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText roomfindname = new EditText(RoomActivity.this);
                roomfindname.setTextColor(Color.BLACK);
                AlertDialog.Builder roomfind = new AlertDialog.Builder(RoomActivity.this)
                        .setTitle("채팅방 찾기")
                        .setMessage("찾을 채팅방 이름을 쓰세요.")
                        .setView(roomfindname)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("message").child(roomfindname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null){
                                            Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
                                            intent.putExtra("roomName", roomfindname.getText().toString());
                                            startActivity(intent);

                                            databaseReference.child("message").child(dataSnapshot.getKey()).child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    String userget = snapshot.getValue().toString();
                                                    if (userget.matches(".*userUid=" + useruid + ".*") == true){
                                                        Log.i("채팅방찾기 - 유저확인", "TRUE");
                                                    }
                                                    else {
                                                        Log.i("채팅방찾기 - 유저확인", "FALSE 유저 추가");
                                                        UserDTO userDTO = new UserDTO(useruid, useremail);
                                                        databaseReference.child("message").child(roomfindname.getText().toString()).child("user").push().setValue(userDTO);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }else {
                                            Toast.makeText(RoomActivity.this, "해당 채팅방이 없습니다.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                roomfind.show();
            }
        });
        showChatList();
    }

    @Override
    public void onBackPressed(){
        if (System.currentTimeMillis() > backpressedTime + 2000){
            backpressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG).show();
        }else if (System.currentTimeMillis() <= backpressedTime + 2000){
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void revokeAccess() {
        mAuth.getCurrentUser().delete();
    }

    public void onTabChanged(String tabId){
        for (int i = 0; i < chattab.getTabWidget().getChildCount(); i++){
            chattab.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        chattab.getTabWidget().getChildAt(chattab.getCurrentTab()).setBackgroundColor(Color.parseColor("#81afef"));
    }

    private void showChatList() {
        listroom.setAdapter(adapter);

        databaseReference.child("message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i("채팅방 이름", dataSnapshot.getKey());

                databaseReference.child("message").child(dataSnapshot.getKey()).child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userget = snapshot.getValue().toString();

                        if (userget.matches(".*userUid=" + useruid + ".*") == true){
                            Log.i("채팅방 - 유저확인", "TRUE");
                            adapter.addItem(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sc_icon_x), dataSnapshot.getKey());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {            }

            @Override
            public void onCancelled(DatabaseError databaseError) {            }
        });

        listroom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomDTO item = (RoomDTO) parent.getItemAtPosition(position);

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("roomName", item.getRoomname());
                startActivity(intent);
            }
        });

        listroom.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                RoomDTO item = (RoomDTO) parent.getItemAtPosition(position);

                AlertDialog.Builder alertDialogBulder = new AlertDialog.Builder(RoomActivity.this);
                alertDialogBulder.setTitle("삭제")
                        .setMessage("삭제하시겠습니까?");
                alertDialogBulder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child("message").child(item.getRoomname()).child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                databaseReference.child("message").child(item.getRoomname()).child("user").orderByChild("userUid").equalTo(useruid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String userkey = snapshot.getValue().toString().substring(1, 21);

                                        if (snapshot.getValue().toString().length() == dataSnapshot.getValue().toString().length()){
                                            Log.i("삭제 유저X", "유저가 없는 채팅방");
                                            databaseReference.child("message").child(item.getRoomname()).removeValue();
                                        } else {
                                            databaseReference.child("message").child(item.getRoomname()).child("user").child(userkey).removeValue();
                                            Log.i("삭제 유저O", "유저가 남아있는 채팅방");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Toast.makeText(RoomActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });
                AlertDialog alertDialog = alertDialogBulder.create();
                alertDialog.show();
                return true;
            }
        });
    }
}