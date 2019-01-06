package com.example.shina.shinerstalk.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shina.shinerstalk.R;
import com.example.shina.shinerstalk.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageActivity extends AppCompatActivity {

    private String destinatonUid; // 목적지 Uid?
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();   // 대화를 신청한 사람
        destinatonUid = getIntent().getStringExtra("destinationUid");  // People Fragment에서 받아온 인텐트값

        button =  findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);


        // 전송 버튼에 대한 클릭리스너 설정
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //챗 모델 객체 생성
                ChatModel chatModel = new ChatModel();

                chatModel.users.put(uid,true);
                chatModel.users.put(destinatonUid,true);

                // 만약 chatRoomUid 가 null 일 경우,
                if (chatRoomUid == null) {
                    // push로 chatRoomUid 를 생성한다.
                    /*  파이어베이스 DB에 채팅방을 생성해주는 코드. chatrooms라는 차일드 생성한다음에
                        push를 넣지 않으면 이름이 없기 때문에 push를 넣어줘야 이름이 임의적으로 생성되서 채팅방이 만들어지게 된다.*/
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
                } else {
                    //null 이 아닐 경우 데이터를 넣어준다.
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;  // 현재 사용자의 uid 를 넣어준다.
                    comment.message = editText.getText().toString();  // 메세지는 editText 에서 받아온다.

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                }// end if-else

            }//end onClick
        }); //end button setOnClickListener


        // 챗룸의 중복을 체크해주는 메서드 실행
        checkChatRoom();

    }//end onCreate

    // 챗룸의 중복을 체크해주는 메서드
    void  checkChatRoom(){
         // orderByChild 에다가 ("users/"+uid) 를 넣어주게 되면 child 인 users 에 uid 대로 정렬이 되는 듯
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid)

                // equalTo 는 뭔지 모르겠다.
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot item : dataSnapshot.getChildren()){

                            // chatrooms 하위에 Uid 하위에  [user 이하로 받아온 것]
                            ChatModel  chatModel = item.getValue(ChatModel.class);

                            // destinationUid 즉 상대방이 있을 경우, 아이템의
                            if(chatModel.users.containsKey(destinatonUid)){
                                chatRoomUid = item.getKey();
                            }
                        }
                    }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
