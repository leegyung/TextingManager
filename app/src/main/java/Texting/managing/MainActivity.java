package Texting.managing;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button text_button;
    private Button callback_button;
    private final int PERMISSIONS_REQUEST_RESULT = 1;

    private ArrayList<String> perms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // 필요한 권한 묻기.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.SEND_SMS
                }, PERMISSIONS_REQUEST_RESULT);

        addListenerOnButton();
    }


    //
    // 모든 버튼 눌렀을시 실행 함수 설정.
    //
    private void addListenerOnButton() {
        text_button = (Button)findViewById(R.id.text);
        callback_button = (Button)findViewById(R.id.call);

        text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextingActivity.class);
                startActivity(intent);
            }
        });

        callback_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CallbackActivity.class);
                startActivity(intent);
            }
        });
    }

}