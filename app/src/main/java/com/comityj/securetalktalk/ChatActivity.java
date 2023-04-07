package com.comityj.securetalktalk;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import javax.crypto.spec.IvParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import android.view.MenuItem;

public class ChatActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    public static String chatname, username, profileurl, useremail, aeskey, datetime, userUid;
    private boolean encryptox;
    private List<String> chatlist = new ArrayList<>();

    TextView titlename;
    ImageView back;
    ListView listmsg;
    EditText editmsg;
    Button send, aessend;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private ArrayList<ChatDTO> chatdto = new ArrayList<>();
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        mAuth = FirebaseAuth.getInstance() ;
        FirebaseUser user = mAuth.getCurrentUser();

        titlename = findViewById(R.id.toolbal_title);
        back = findViewById(R.id.toolbal_back);

        listmsg = findViewById(R.id.msglist);
        editmsg = findViewById(R.id.msget);
        send = findViewById(R.id.sendbtn);
        aessend = findViewById(R.id.aesbtn);

        adapter = new ChatAdapter(chatdto, getLayoutInflater());
        listmsg.setAdapter(adapter);

        Intent intent = getIntent();
        chatname = intent.getStringExtra("roomName");
        username = user.getDisplayName();
        profileurl = user.getPhotoUrl().toString();
        useremail = user.getEmail();
        userUid = user.getUid();

        titlename.setText(chatname);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        datetime = dateFormat.format(date);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptox = false;
                aeskey = "";
                ChatDTO chatDTO = new ChatDTO(username, editmsg.getText().toString(), encryptox, profileurl, useremail, aeskey, datetime);
                databaseReference.child("message").child(chatname).child("chat").push().setValue(chatDTO);
                editmsg.setText("");
            }
        });

        aessend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText key = new EditText(ChatActivity.this);
                key.setTextColor(Color.BLACK);
                final AlertDialog.Builder dekey = new AlertDialog.Builder(ChatActivity.this)
                        .setTitle("암호화")
                        .setMessage("KEY를 입력하세요.")
                        .setView(key)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String aesen;
                                    aesen = Encrypt(editmsg.getText().toString(), key.getText().toString());
                                    aeskey = key.getText().toString();
                                    encryptox = true;
                                    ChatDTO chatDTO = new ChatDTO(username, aesen, encryptox, profileurl, useremail, aeskey, datetime);
                                    databaseReference.child("message").child(chatname).child("chat").push().setValue(chatDTO);
                                    //databaseReference.child("message").child(chatname).child("lastchat").setValue(aesen);
                                    editmsg.setText("");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                dekey.show();
            }
        });

        listmsg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popup = new PopupMenu(ChatActivity.this, view);
                getMenuInflater().inflate(R.menu.chat_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.encryptmsg:
                                if(useremail.equals(chatdto.get(position).getUserEmail())) {
                                    if(chatdto.get(position).getEncrypt() == false) {
                                        final EditText key = new EditText(ChatActivity.this);
                                        key.setTextColor(Color.BLACK);
                                        final AlertDialog.Builder enkey = new AlertDialog.Builder(ChatActivity.this)
                                                .setTitle("암호화")
                                                .setView(key)
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            String enmsg = Encrypt(chatdto.get(position).getMsg(), key.getText().toString());
                                                            Map<String, Object> aesupdate = new HashMap<>();
                                                            aesupdate.put("aesKey", key.getText().toString());
                                                            aesupdate.put("encrypt", true);
                                                            aesupdate.put("msg", enmsg);

                                                            databaseReference.child("message").child(chatname).child("chat").child(chatlist.get(position)).updateChildren(aesupdate);
                                                            updatechat();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                        enkey.show();
                                    }else {
                                        Toast.makeText(ChatActivity.this,"이미 암호화된 메시지 입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(ChatActivity.this,"다른 사람의 메시지입니다." ,Toast.LENGTH_LONG).show();
                                }
                                break;

                            case R.id.decryptmsg:
                                if (chatdto.get(position).getEncrypt() == true) {
                                    final EditText key = new EditText(ChatActivity.this);
                                    key.setTextColor(Color.BLACK);
                                    final AlertDialog.Builder dekey = new AlertDialog.Builder(ChatActivity.this)
                                            .setTitle("복호화")
                                            .setView(key)
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        if (key.getText().toString().equals(chatdto.get(position).getAesKey())) {
                                                            String demsg = Decrypt(chatdto.get(position).getMsg(), key.getText().toString());
                                                            AlertDialog.Builder dem = new AlertDialog.Builder(ChatActivity.this)
                                                                .setTitle("복호화 메시지")
                                                                .setMessage(demsg)
                                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                    }
                                                                });
                                                            dem.show();
                                                        }else {
                                                            Toast.makeText(ChatActivity.this,"KEY 값이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                    dekey.show();
                                }else {
                                    Toast.makeText(ChatActivity.this,"암호화 메시지가 아닙니다.", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case R.id.copy:
                                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("Copy", chatdto.get(position).getMsg());
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(ChatActivity.this,"메시지를 복사했습니다.", Toast.LENGTH_SHORT).show();

                                break;

                            case R.id.delete:
                                if(useremail.equals(chatdto.get(position).getUserEmail())){
                                    deletechat(position);
                                    updatechat();
                                    Toast.makeText(ChatActivity.this,"삭제완료" ,Toast.LENGTH_LONG).show();
                                } else{
                                    Toast.makeText(ChatActivity.this,"다른 사람의 메시지입니다." ,Toast.LENGTH_LONG).show();
                                }
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RoomActivity.class);
                startActivity(intent);
            }
        });
        openChat(chatname);
    }

    public void onBackPressed(){
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
    }

    private void deletechat(int postion){
        databaseReference.child("message").child(chatname).child("chat").child(chatlist.get(postion)).removeValue();
    }

    private void updatechat(){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("roomName",chatname);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }


    //AES128 암호화
    public static String Encrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes= new byte[16];
        byte[] b= key.getBytes("UTF-8");
        int len= b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return Base64.encodeToString(results, 0);
    }
    //AES128 복호화
    public static String Decrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes= new byte[16];
        byte[] b= key.getBytes("UTF-8");
        int len= b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);

        byte [] results = cipher.doFinal(Base64.decode(text, 0));

        return new String(results,"UTF-8");
    }

    private void openChat(String roomName) {
        chatlist.clear();
        listmsg.setAdapter(adapter);

        databaseReference.child("message").child(roomName).child("chat").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chatlist.add(dataSnapshot.getKey());
                ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
                chatdto.add(chatDTO);
                adapter.notifyDataSetChanged();
                listmsg.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updatechat();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updatechat();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
}
