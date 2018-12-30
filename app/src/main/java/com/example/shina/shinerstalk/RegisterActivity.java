package com.example.shina.shinerstalk;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shina.shinerstalk.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {


    private EditText email;
    private EditText name;
    private EditText password;
    private Button register;
    private String splash_background;

    private static final int PICK_FROM_ALBUM = 10;
    private ImageView profile;
    private Uri imageUri;


    String TAG = "RegisterActivity 액티비티";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 파이어베이스에서 원격 설정을 하기 위한 객체를 생성해준다.
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // 백그라운드 색상값을 파이어베이스 원격 설정으로부터 받아온다
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));

        // 사용자 프로필 이미지
        profile = findViewById(R.id.registerActivity_imageview_profile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,PICK_FROM_ALBUM);
            }
        });

        // XML 의 각 개체들을 연결해준다.
        email = findViewById(R.id.registerActivity_edittext_email);  // 이메일
        name = findViewById(R.id.registerActivity_edittext_name);   // 이름
        password = findViewById(R.id.registerActivity_edittext_password);   // 비밀번호
        register = findViewById(R.id.registerActivity_button_register);     // 회원가입 버튼

        // 회원가입 버튼에 색상을 입혀준다.
        register.setBackgroundColor(Color.parseColor(splash_background));


        // 회원가입 버튼의 온클릭리스너 설정
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 이메일이나, 이름이나, 패스워드값이 null 일  경우,
                if (email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null || imageUri ==null ) {
                    Toast.makeText(RegisterActivity.this, "회원정보를 모두 채워주세요. (프로필사진 포함) ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. 파이어베이스 인증
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {

                            @Override
                            // 회원가입이 완료되면 complete 로 넘어오게 된다.
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // 사용자의 Uid를 받아와서 스트링값 uid 에 저장한다. uid 는 각 계정에 부여되는 고유값으로 일종의 주민번호와 같다.
                                final String uid = task.getResult().getUser().getUid();
                                Log.i(TAG, "onComplete: uid값 : "+uid);

                                // 이미지를 파이어베이스 스토리지에 저장하기 위한 코드. userImages 경로 아래 저장된다.
                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        // 파일이 저장되면 저장된 경로를 보내준다.
                                        String imageUrl = task.getResult().getDownloadUrl().toString();

                                        //유저보멜 데이터 클래스 객체 생성
                                        UserModel userModel = new UserModel();
                                        // 사용자가 입력한 이름을 데이터 클래스 userNmae 변수에 넣어준다.
                                        userModel.userName = name.getText().toString();
                                        userModel.profileImageUrl = imageUrl;
                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        // 파이어베이스 DB에 사용자의 uid를 받아서 해당 userModel에 대한 값을 설정해주고, addOnSuccessListener로 로그인이 성공적인지 확인한다.
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener( new OnSuccessListener<Void>() {
                                            @Override
                                            // 로그인이 성공적인 경우
                                            public void onSuccess(Void aVoid) {

                                                RegisterActivity.this.finish(); // 이 액티비티는 종료한다.
                                            }//onSuccess 종료
                                        });//addOnSuccessListener 종료

                                    }//onComplete 종료
                                });//addOnCompleteListener 종료

                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: 계정이 이미 있거나 패스워드가 잘못된듯 ");
                        Toast.makeText(RegisterActivity.this, "회원가입이 이미 되있거나 비밀번호가 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            // 회원정보 사진을 저장해줌.
            profile.setImageURI(data.getData());
            imageUri = data.getData();  // 이미지 경로 원본
        }
    }
}
