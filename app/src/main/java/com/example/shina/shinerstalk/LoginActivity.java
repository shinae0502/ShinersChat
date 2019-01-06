package com.example.shina.shinerstalk;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    String TAG = "LoginActivity 액티비티";

    private EditText id;
    private EditText password;

    private Button login;
    private Button register;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;  // 로그인 성공여부를 알려주는 역할
    private FirebaseAuth.AuthStateListener authStateListener;   // 로그인 성공시 다음 화면으로 넘겨주는 역할
    private String splash_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 하울이 이 코드를 작성하면서 "singleton 패턴으로 선언을 해줄게요." 라고 했다. 무슨말이지?
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // 파이어베이스 원격 설정으로부터 화면의 백그라운드 색상을 적용해주는 부분.
        splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));
        Log.i(TAG, "onCreate:  splash_background 값 : "+splash_background);  // 색상값이 로그로 찍힌다.

        // 파이어베이스로부터 받은 설정값을 실제 StatusBarColor에 적용해준다.
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        // 파이어베이스 인증 객체 초기화. ("싱글톤 패턴으로 가져온다"고 하울이 말함. 무슨말이지?)
        firebaseAuth = FirebaseAuth.getInstance();
        // 로그아웃
        firebaseAuth.signOut();

        // xml객체들과 연결
        id = findViewById(R.id.loginActivity_edittext_id); // 아이디 입력하는 ET
        password = findViewById(R.id.loginActivity_edittext_password);  // PW 입력하는 ET

        login = findViewById(R.id.loginActivity_button_login);  // 로그인 버튼
        register = findViewById(R.id.loginActivity_button_register);    // 회원가입 버튼

        // 버튼에 색상입히기
        login.setBackgroundColor(Color.parseColor(splash_background));
        register.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 이벤트 메서드 실행 - 사용자 로그인값 검증하여 알맞으면 메인 액티비티 시작
                loginEvent();
            }
        });

        // 회원가입 버튼 클릭 리스너
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: 호원가입 버튼 클릭");
                //로그인 화면에서 회원가입 화면으로 넘어가는 인텐트 생성 및 액티비티 시작
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        // 로그인 인터페이스 리스너  -
        //              클래스에 붙여줘야 (implement) 실행이 된다. --> onStart() 단계에 붙여줌.
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            // 상태(State)가 바뀌었을 때 (Changed) 알려줌 - 로그인 또는 로그아웃 될 때
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // 파이어베이스에서 현재 사용자를 받아서 user 객체에 저장
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(TAG, "onAuthStateChanged: 로그인 성공");
                    // 로그인이 성공되어 메인 화면으로 넘어간다.
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }else {
                    Log.i(TAG, "onAuthStateChanged: 로그인 실패");
                    // 로그 아웃
                }//if-else문 종료
            }//onAuthStateChange 메서드 종료
        };


    }//onCreate 메서드 종료



    void loginEvent () {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    // 로그인이 성공적으로 되었는지만 알려주는 역할 (화면을 넘겨주지는 않는다)
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //로그인이 실패했을 시에만 작동
                        if (!task.isSuccessful()) {
                            Log.i(TAG, "onComplete: 로그인이 실패했다. ");
                            // 어떤 에러로 로그인이 실패했는지 토스트 메세지가 뜬다. (영어로)
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }//onComplete 메서드 종료
                });


    }//loginEvent 메서드 종료

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: 상태");
        // 로그인 상태 리스너를 여기에 붙여줬다.
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: 상태");
        // 로그인 상태 리스너를 떼어준다
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
