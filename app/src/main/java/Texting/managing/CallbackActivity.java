package Texting.managing;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Map;

//
// Callback 설정 Activity
//
public class CallbackActivity extends AppCompatActivity {

    private SwitchCompat SetAuto;
    private TextView message;
    private TextView callTitle;
    private TextView missTitle;
    private Button saveMessage;
    private Button getMessage;
    private Button callBackSet;
    private SwitchCompat unset;
    private Button setCallAuto;
    private Button setMissAuto;
    private ArrayList<Message> MessageList = new ArrayList<>();
    private BroadcastReceiver br;

    //
    // 저장된 메세지들 복원 후 버튼 리스너들 추가
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //여기서 오버레이 권한부여 받는거설정
        SharedPreferences prefs;
        prefs = getSharedPreferences("Pref", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun",true);
        if(isFirstRun)
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
            prefs.edit().putBoolean("isFirstRun",false).apply();
            Toast.makeText(getApplicationContext(), "이 앱의 권한 부여", Toast.LENGTH_LONG).show();
        }


        message = (TextView)findViewById(R.id.callback_message);
        callTitle = (TextView)findViewById(R.id.autoCallTitle);
        missTitle = (TextView)findViewById(R.id.autoMissTitle);
        restoreMessages();
        addListenerOnButton();

    }


    //
    // 모든 버튼 눌렀을시 실행 함수 설정.
    //
    private void addListenerOnButton(){
        SharedPreferences pref = getSharedPreferences("CallBack", Activity.MODE_PRIVATE);

        // 설정해놓은 auto 상태를 바꿈.
        SetAuto = (SwitchCompat)findViewById(R.id.Auto_message);
        unset = (SwitchCompat)findViewById(R.id.textSet);
        String set = pref.getString("send_Text", null);
        String auto = pref.getString("SetAuto", null);

        if(set != null){
            unset.setChecked(set.equals("true"));
        }
        else{
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("send_Text", "false");
            editor.apply();
        }

        if(auto != null){
            SetAuto.setChecked(auto.equals("true"));
        }
        else{
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("SetAuto", "false");
            editor.apply();
        }

        // 콜백문자 활성화 스위치
        unset.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // CallBack 의 send_Text 에 true 인지 false 인지 저장
                SharedPreferences.Editor editor = pref.edit();
                if(isChecked){
                    editor.putString("send_Text", "true");
                }
                else{
                    editor.putString("send_Text", "false");
                }
                editor.apply();
            }
        });

        // 콜백문자 오토 or 수동 스위치
        SetAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // CallBack 의 SetAuto 에 true 인지 false 인지 저장
                SharedPreferences.Editor editor = pref.edit();
                if(isChecked){
                    editor.putString("SetAuto", "true");
                }
                else{
                    editor.putString("SetAuto", "false");
                }
                editor.apply();
            }
        });

        // 문자 저장 버튼 눌렀을시.
        saveMessage = (Button)findViewById(R.id.callback_save);
        saveMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewMessage();
                saveMessages();
            }
        });

        // 저장된 문자 목록 불러오기
        getMessage = (Button)findViewById(R.id.callback_get);
        getMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMessageList("none");
            }
        });

        // 리시버 설정후 문자리스트와 써져있는 문자 저장.
        callBackSet = (Button)findViewById(R.id.callback_set);
        callBackSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(message.equals("") && SetAuto.isChecked())
                {
                    Toast.makeText(getApplicationContext(), "자동 문자 내용이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    br = new CallbackReceiver();
                    IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    registerReceiver(br, filter);
                    saveMessages();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("CurrentMSG", message.getText().toString());
                    editor.apply();
                    message.setText("");
                    Toast.makeText(getApplicationContext(), "설정 완료.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 통화 종료후 보낼 자동문자 선택.
        setCallAuto = (Button)findViewById(R.id.callAuto);
        String temp = "선택 문자 제목: " + pref.getString("m_Auto_Call_Title", null);
        callTitle.setText(temp);

        setCallAuto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                viewMessageList("call");
            }
        });


        // 부재중시 보낼 자동문자 선택.
        setMissAuto = (Button)findViewById(R.id.missAuto);
        temp = "선택 문자 제목: " + pref.getString("m_Miss_Call_Title", null);
        missTitle.setText(temp);

        setMissAuto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                viewMessageList("miss");
            }
        });


    }


    //
    // Dialog 띄운후 제목 입력 받은 후
    // Message class 만들어 MessageList 에 저장.
    //
    private void AddNewMessage(){
        String currentMsg = message.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("제목 입력:");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = input.getText().toString();
                MessageList.add(new Message(title, currentMsg));
                Toast.makeText(CallbackActivity.this, "저장 완료", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(CallbackActivity.this, "취소됨", Toast.LENGTH_SHORT).show();
                    }
                });
        alert.show();


    }


    //
    // 저장된 메세지 리스트 dialog 로 보여주고 보낼문자로 설정.
    // where(param)
    // -> miss 일시 부제중 오토 메세지로 설정
    // -> call 일시 전화 받은후 오토 메세지로 설정
    // -> 다른거 아무거나 일시는... 왜했지?... 필요 없는 문장.
    //
    private void viewMessageList(String where){
        ArrayList<String> items = new ArrayList<>();
        for(Message msg : MessageList){
            items.add(msg.getTitle());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CallbackActivity.this);
        builder.setTitle("문자 선택");
        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                if(where.equals("miss")){
                    SharedPreferences pref = getSharedPreferences("CallBack", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    // 리시버로 넘겨주기위해 문자 네용과 이름 저장.
                    editor.putString("m_Miss_Call", MessageList.get(id).getMessage());
                    editor.putString("m_Miss_Call_Title", MessageList.get(id).getTitle());
                    editor.apply();
                    String temp = "선택 문자 제목: " + MessageList.get(id).getTitle();
                    missTitle.setText(temp);
                }
                else if(where.equals("call")){
                    SharedPreferences pref = getSharedPreferences("CallBack", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    // 리시버로 넘겨주기위해 문자 네용과 이름 저장.
                    editor.putString("m_Auto_Call", MessageList.get(id).getMessage());
                    editor.putString("m_Auto_Call_Title", MessageList.get(id).getTitle());
                    editor.apply();
                    String temp = "선택 문자 제목: " + MessageList.get(id).getTitle();
                    callTitle.setText(temp);
                }
                else{
                    message.setText(MessageList.get(id).getMessage());
                }

            }
        });
        builder.show();
    }

    //
    // 메세지 리스트(MessageList) 에 저장된 것들 pref에 저장.
    //
    private void saveMessages(){
        SharedPreferences pref = getSharedPreferences("AutoMessage", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();

        for(Message msg : MessageList){
            editor.putString(msg.getTitle(), msg.getMessage());
        }
        editor.apply();
    }


    //
    // pref에 저장된 메세지리스트들 MessageList 로 가져오기.
    //
    private void restoreMessages(){
        SharedPreferences pref = getSharedPreferences("AutoMessage", Activity.MODE_PRIVATE);
        if(pref != null)
        {
            Map<String, ?> allEntries = pref.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Message msg = new Message(entry.getKey(), entry.getValue().toString());
                MessageList.add(msg);
            }
        }
    }

    //
    // 엡이 꺼지거나 멀티 윈도우로 갈때 메세지 리스트 저장.
    //
    @Override
    protected void onPause() {
        super.onPause();
        saveMessages();
    }


















}