package com.example.shina.shinerstalk.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shina.shinerstalk.R;
import com.example.shina.shinerstalk.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MessageActivity extends AppCompatActivity {

    private String destinatonUid; // 목적지 Uid?
    private Button button;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        destinatonUid = getIntent().getStringExtra("destinationUid");  // People Fragment에서 받아온 인텐트값
        button =  findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);


        // 전송 버튼에 대한 클릭리스너 설정
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //챗 모델 객체 생성
                ChatModel chatModel = new ChatModel();
                // 현재 사용자에 대한 uid를 가져와서 챗모델 uid에 저장한다.
                chatModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();   // 대화를 신청한 사람
                // 상대방의 uid를 넣어준다.   // 대화를 신청 당한사람
                chatModel.destinationUid = destinatonUid;
                // 파이어베이스 DB에 채팅방을 생성해주는 코드. chatrooms라는 차일드 생성한다음에
                // push를 넣지 않으면 이름이 없기 때문에 push를 넣어줘야 이름이 임의적으로 생성되서 채팅방이 만들어지게 된다.
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);

            }
        });

    }
}
