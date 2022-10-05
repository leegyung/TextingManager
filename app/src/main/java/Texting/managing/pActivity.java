package Texting.managing;


import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

// 전화 받고 수동문자 선택시 실행할 팝업 엑티비티
public class pActivity extends AppCompatActivity {
    private String phoneNum = "";
    private ArrayList<Message> MSGList = new ArrayList<>();
    private String MSG = "";
    private Button okay;
    private Button cancel;
    private Button list;
    private TextView msgText;
    private TextView numtxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p);
        Intent passedIntent = getIntent();
        processCommand(passedIntent);
        AddListener();

    }

    //
    // 버튼들 리스너.
    //
    private void AddListener(){
        okay = (Button)findViewById(R.id.popokay);
        cancel = (Button)findViewById(R.id.popcancel);
        list = (Button)findViewById(R.id.poplist);
        msgText = (TextView)findViewById(R.id.txtText);

        //  취소 버튼 눌렀을시 꺼짐.
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndRemoveTask();
                System.exit(0);
            }
        });

        // 확인버튼 눌렀을시 문자 내용이 있으면 전송
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MSG != "" && phoneNum != ""){
                    SendSMS(phoneNum, MSG, getApplicationContext());
                    finishAndRemoveTask();
                    System.exit(0);
                }
                else if(MSG == ""){
                    Toast.makeText(getApplicationContext(), "문자 내용 없음.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 저장 문자 리스트 보여주기
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMessageList();
            }
        });
    }

    
    //
    // 메세지 리스트 다이얼로그 보여주기.
    //
    private void viewMessageList(){
        ArrayList<String> items = new ArrayList<>();
        for(Message msg : MSGList){
            items.add(msg.getTitle());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(pActivity.this);
        builder.setTitle("문자 선택");
        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                MSG = MSGList.get(id).getMessage();
                if(MSG.length() > 10){
                    String temp = MSG.substring(0,11) + "...";
                    msgText.setText(temp);
                }
                else{
                    msgText.setText(MSG);
                }

            }
        });
        builder.show();
    }


    //
    // intent 의 정보 restore
    // phoneNum 에 번호 저장
    // MSGList 에 모든 저장 문자들 저장
    //
    private void processCommand(Intent intent){
        if (intent != null){
            Bundle b = intent.getExtras();
            for(String key : b.keySet()){
                if (key.equals("PNumber")){
                    phoneNum = b.get(key).toString();
                    numtxt = (TextView)findViewById(R.id.numtext);
                    String temp = "(" + phoneNum + ")";
                    numtxt.setText(temp);
                }
                else{
                    Message m = new Message(key, b.get(key).toString());
                    MSGList.add(m);
                }
            }
        }
    }

    //
    // 바로 문자 보내기.
    //
    private void SendSMS(String num, String msg, Context cont){
        try {
            //전송
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(num, null, msg, null, null);
            Toast.makeText(cont, "전송 완료!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(cont, "전송 실패", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

}