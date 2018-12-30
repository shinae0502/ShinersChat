package com.example.shina.shinerstalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.shina.shinerstalk.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //v4 프래그먼트를 썼기 때문에 하울 강의와는 다른 getSupportFragmentManager 사용했다.
        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new PeopleFragment()).commit();

    }
}
