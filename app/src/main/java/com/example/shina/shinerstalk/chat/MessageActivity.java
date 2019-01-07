package com.example.shina.shinerstalk.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.shina.shinerstalk.R;
import com.example.shina.shinerstalk.model.ChatModel;
import com.example.shina.shinerstalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    String TAG = "Message 액티비티 ";

    private String destinationUid; // 목적지 Uid?
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();   // 대화를 신청한 사람
        destinationUid = getIntent().getStringExtra("destinationUid");  // People Fragment에서 받아온 인텐트값
        Log.i(TAG, "onCreate: 현재 사용자 uid : "+uid);
        Log.i(TAG, "onCreate: 상대방 destinationUDI : " + destinationUid);

        button =  findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);
        recyclerView = findViewById(R.id.messageActivity_recyclerview);


        // 전송 버튼에 대한 클릭리스너 설정
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //챗 모델 객체 생성
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid,true);
                chatModel.users.put(destinationUid,true);


                // orderByChild 에다가 ("users/"+uid) 를 넣어주게 되면 child 인 users 에 uid 대로 정렬 시켜서,
                FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid)

                        // equalTo 는 뭔지 모르겠다. 싱글밸류이벤트 리스너 달아준다.
                        .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot item : dataSnapshot.getChildren()){

                            // chatrooms 하위에 Uid 하위에  [user 이하로 받아온 것]
                            ChatModel  chatModel = item.getValue(ChatModel.class);

                            // destinationUid 즉 상대방이 있을 경우, dataSnapshot 으로부터 chatRoomUid 를 받아오게 된다.
                            if(chatModel.users.containsKey(destinationUid)){
                                chatRoomUid = item.getKey();  // chatRoomUid 받아온다.

                                Log.i(TAG, "onDataChange: chatModel.users 에 상대방 키값이 있는가 ??? "+ chatModel.users.containsKey(destinationUid) );
                                Log.i(TAG, " checkChatRoom 메서드 내부 onDataChange: chatRoom 의 udi : "+ chatRoomUid);

                                // comment 객체를 생성해서,
                                ChatModel.Comment comment = new ChatModel.Comment();
                                comment.uid = uid;  // 현재 사용자의 uid 를 넣어준다.
                                comment.message = editText.getText().toString();  // 메세지는 editText 에서 받아온다.

                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    // 메세지가 전송이 완료되었다는 콜백을 받고,
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // 에딧텍스트 부분을 초기화 시켜준다.
                                        editText.setText("");

                                    }//end Complete
                                });// end addOnCompleteListener()

                                //전송 버튼 활성화
                                button.setEnabled(true);

                                recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                                recyclerView.setAdapter(new RecyclerViewAdapter());
                            }

                            else {

                                // 만약 chatRoomUid 가 null 일 경우,
                                if (chatRoomUid == null) {
                                    Log.i(TAG, "onClick: chatRoomUid  이 null 이다.  ");

                                    button.setEnabled(false); // Firebase 서버로부터 응답받기 전까진 버튼을 비활성화시킨다

                                    //  push로 chatRoomUid 를 생성한다.
                                    /*  파이어베이스 DB에 채팅방을 생성해주는 코드. chatrooms라는 차일드 생성한다음에
                                        push를 넣지 않으면 이름이 없기 때문에 push를 넣어줘야 이름이 임의적으로 생성되서 채팅방이 만들어지게 된다.
                                        setValue 로 chatModel 객체를 넣어주고, onSuccess 콜백메서드를 달아서
                                        성공했을 경우에만 checkChatRoom 메서드 (챗룸의 중복을 체크해주는 메서드) 실행  */

                                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // 챗룸의 중복을 체크해주는 메서드 실행
                                                    // checkChatRoom();
                                                    Log.i(TAG, "onSuccess: checkChatRoom 메서드가 끝나고 다시 돌아옴 ");
                                                }
                                            });

                                    // 만약 이곳에 checkChatRoom 이 위치하게 된다면 인터넷 연결이 잠시 끊겨서
                                    // FirebaseDatabase 이하의 코드가 생성이 안되어 chatrooms 가 push 되지 않은 상태에서
                                    // checkChatRoom 메서드가 돌게 되는 오류가 생길 수 있다.  by 하울

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "checkChatRoom 메서드 내부 onCancelled: 왠지 모르지만 캔슬되었다. ");
                    }//end onCancelled()

                });// end addListenerForSingleValueEvent()





            }//end onClick
        }); //end button setOnClickListener




    }//end onCreate

//    // 챗룸의 중복을 체크해주는 메서드
//    void  checkChatRoom(){
//        Log.i(TAG, "checkChatRoom: checkChatRoom 메서드 시작");
//         // orderByChild 에다가 ("users/"+uid) 를 넣어주게 되면 child 인 users 에 uid 대로 정렬이 되는 듯
//        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid)
//
//                // equalTo 는 뭔지 모르겠다.
//                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
//
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for(DataSnapshot item : dataSnapshot.getChildren()){
//
//                            // chatrooms 하위에 Uid 하위에  [user 이하로 받아온 것]
//                            ChatModel  chatModel = item.getValue(ChatModel.class);
//
//                            // destinationUid 즉 상대방이 있을 경우, dataSnapshot 으로부터 chatRoomUid 를 받아오게 된다.
//                            if(chatModel.users.containsKey(destinationUid)){
//                                chatRoomUid = item.getKey();  // chatRoomUid 받아온다.
//                                Log.i(TAG, " checkChatRoom 메서드 내부 onDataChange: chatRoom 의 udi : "+ chatRoomUid);
//
//                                //전송 버튼 활성화
//                                button.setEnabled(true);
//
//                                recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
//                                recyclerView.setAdapter(new RecyclerViewAdapter());
//                            }
//                        }
//                    }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "checkChatRoom 메서드 내부 onCancelled: 왠지 모르지만 캔슬되었다. ");
//            }//end onCancelled()
//
//        });// end addListenerForSingleValueEvent()
//
//    }//end checkChatRoom ()




    // inner Class 로 리사이클러뷰 어댑터를 넣어준다.
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;
        UserModel userModel;

        //Constructor
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);

                   // 사용자 대화내용을 불러오는 메서드
                   getMessageList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }//end Constructor

        // 사용자 대화내용을 불러오는 메서드
        void getMessageList () {
            //chatRoomUid = 방이름
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();  // clear를 일단 넣어준다.

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        comments.add(item.getValue(ChatModel.Comment.class));

                    }
                    //데이터의 변경사항을 어답터에 알려주어 갱신한다.
                    notifyDataSetChanged();

                    //리사이클러뷰의 스크롤을 이동시켜준다.  / 몇 번째의 포지션으로 이동하겠다는 것인데, comments의 사이즈의 -1 로 하면 맨 마지막으로 이동할 수 있다.
                    recyclerView.scrollToPosition(comments.size() -1);

                }//end onDataChange

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }//end onCancelled
            });
        }//end getMessageList



        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            //item_message.xml 를 인플레이터에 넣어준다
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message,viewGroup,false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            MessageViewHolder messageViewHolder =  ((MessageViewHolder)viewHolder);

            // 내가보낸 메세지  / 만약 comments 에 저장되어있는 uid 가 내 uid 와 같을 경우
            if (comments.get(i).uid.equals(uid)) {
                /* 1. ViewHolder클래스에서 textview 이름을 고대로 갖고온다.
                 * 2. 그냥 갖고오면 찾아지지 않기 때문에 viewHolder를 입력하고, 이후에 MessageViewHolder를 입력하여 차례로 캐스팅해준다.  */
                messageViewHolder.textView_message.setText(comments.get(i).message);   // 메세지 담기
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.outmsgbackground);  // 챗버블
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);  // 내 프로필사진과 이름은 안보이게 처리
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);   // 오른쪽으로 정렬
            }

            // 상대방이 보낸 메세지  /
            else {
                Glide.with(viewHolder.itemView.getContext())
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(userModel.userName);    // 상대방 이름 담기
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE); // 프로필 보이게 처리
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.inmsgbackground);   // 챗버블
                messageViewHolder.textView_message.setText(comments.get(i).message);    // 메세지 내용 담기
                messageViewHolder.textView_message.setTextSize(25);     // 글씨 크기는 25로
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);  // 왼쪽으로 정렬

            }



        }

        @Override
        public int getItemCount() {

            // 여기 까먹지 말것
            return comments.size();
        }

        // inner class MessageViewHolder
        private class MessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout  linearLayout_destination;
            public LinearLayout linearLayout_main;

            public MessageViewHolder(View view) {
                super(view);

                // item_message.xml  있는 객체들을 각각 찾아준다.
                textView_message = view.findViewById(R.id.messageItem_textView_message);
                textView_name = view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = view.findViewById(R.id.messageItem_linearlayout_main); // 컨텐츠 방향 정렬할때 쓸것.

            }//end MessageViewHolder()
        }
    }


}
