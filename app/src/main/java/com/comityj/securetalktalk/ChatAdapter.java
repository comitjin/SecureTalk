package com.comityj.securetalktalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ChatAdapter extends BaseAdapter {
    ArrayList<ChatDTO> chatdto;
    LayoutInflater layoutInflater;

    public ChatAdapter(ArrayList<ChatDTO> chatdto, LayoutInflater layoutInflater){
        this.chatdto = chatdto;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return chatdto.size();
    }

    @Override
    public Object getItem(int position) {
        return chatdto.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ChatDTO item = chatdto.get(position);

        if(item.getUserEmail().equals(ChatActivity.useremail)){
            view = layoutInflater.inflate(R.layout.chat_me, viewGroup, false);
        }else{
            view = layoutInflater.inflate(R.layout.chat_you, viewGroup, false);
        }

        TextView chatname = view.findViewById(R.id.chat_name);
        TextView chatmsg = view.findViewById(R.id.chat_msg);
        TextView chattime = view.findViewById(R.id.chat_time);

        chatname.setText(item.getUsername());
        chatmsg.setText(item.getMsg());
        String temptime = item.getDateTime().substring(11, 16);
        char hour0 = temptime.charAt(0);
        if (hour0 == '0'){
            temptime.substring(1);
            chattime.setText(temptime);
        }else {
            chattime.setText(temptime);
        }
        return view;
    }
}
