package com.example.shina.shinerstalk.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.shina.shinerstalk.R;
import com.example.shina.shinerstalk.chat.MessageActivity;
import com.example.shina.shinerstalk.model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;


public class PeopleFragment extends android.support.v4.app.Fragment {

    String TAG = "PeopleFragment 액티비티";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<UserModel> userModels;

        // 생성자 Constructor
        public PeopleFragmentRecyclerViewAdapter() {

            // userModels 리스트 초기화
            userModels = new ArrayList<>();

            // 파이어페이스의 "user"에 값 이벤트 리스너 추가함.
            FirebaseDatabase.getInstance()
                    .getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // 서버에서 넘어오는 데이터이다.
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //데이터 모델이 바뀌었을 때, 누적된 데이터들을 초기화 하여 꼬이지 않게 한다.
                    userModels.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //userModel 리스트에 친구목록을 받는데, 서버에서 Value를 get해오고, UserModel클래스 형태로 받겠다는 뜻
                        userModels.add(snapshot.getValue(UserModel.class));
                    }
                    // 새로고침 해주기 - 아답터에 아이템이 바뀌었음을 알려주는 메서드
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }// 생성자 종료

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend,viewGroup,false);
            return new CustomViewHolder(view);
        }




        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            // 글라이드 라이브러리를 이용하여 이미지를 바인딩해준다.
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())    // 어떻게 이미지를 줄건지
                    .into(((CustomViewHolder)holder).imageView);  // 이미지를 넣어준다.
            // 캐스팅을 해줘야 에러가 안난다고 한다.
            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            // 아이템을 클릭하면 채팅창으로 넘어가는 온클릭리스너이다.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //인텐트 객체 생성 - 현재 액티비티에서, 메세지 액티비티로 간다.
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    //인텐트에 추가정보를 담는다 : 사용자의 uid값.
                    intent.putExtra("destinationUid",userModels.get(position).uid);
                    Log.i(TAG, "onClick: 인텐트 엑스트라에 담긴 사용자 uid값 : " +userModels.get(position).uid );

                    // 애니메이션을 설정해주기 위해 ActivityOption 객체 생성.
                    ActivityOptions activityOptions = null;
                    // 안드로이드가 젤리빈 버전 이상일 경우에만 애니매이션 효과가 실행된다.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

                        // 애니매이션 효과를 설정해준다.
                        // 파라메터 설명 ( 컨텍스트, 들어오는 아이템은 오른쪽으로부터 들어온다, 나가는 아이템은 왼쪽으로 나간다)
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright,R.anim.toleft);
                        // 액티비티 시작하면서, 파라메터에 액티비티옵션도 넣어준다.
                        startActivity(intent,activityOptions.toBundle());
                    }
                }//온클릭 메서드 종료
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
            }
        }
    }

}
