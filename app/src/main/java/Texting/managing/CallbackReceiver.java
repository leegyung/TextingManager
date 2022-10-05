package Texting.managing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Map;

//
// 전화 왔을시 실행되는 리시버.
//
public class CallbackReceiver extends BroadcastReceiver {

    static String phoneNum;
    static boolean ring = false;
    static boolean callReceived = false;
    private static String mLastState;
    private ArrayList<Message> MSGList = new ArrayList<>();

    @Override
    public void onReceive(Context mContext, Intent intent)
    {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(state==null)
            return;

        // 리시버가 두번 실행될때가 있어서 그거 방지.
        String stateDouble = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(mLastState)) { return; }
        else { mLastState = state; }



        SharedPreferences pref = mContext.getSharedPreferences("CallBack", Activity.MODE_PRIVATE);
        String SEND = pref.getString("send_Text", null);

        TelephonyManager telephony = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);

        telephony.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if(state == TelephonyManager.CALL_STATE_RINGING){
                    SharedPreferences pref = mContext.getSharedPreferences("CallBack", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("send_number", incomingNumber);
                    editor.apply();
                }
            }
        },PhoneStateListener.LISTEN_CALL_STATE);

        // 전화밸이 울렸을시 실행
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            ring = true;
        }

        // 전화를 받았을시 실행
        if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            callReceived = true;
        }


        // Idle 상태일때 부제중인지 아닌지  callReceived 와 ring 으로 판별후 개별 실행.
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
        {
            // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
            // 부제중 전화시
            if(ring && !callReceived && SEND.equals("true"))
            {
                // 번호 가져오고
                phoneNum = pref.getString("send_number", null);
                //부제중 오토 문자 가져오고
                String MESSAGE = pref.getString("m_Miss_Call",null);
                if(MESSAGE != null){
                    SendSMS(phoneNum, MESSAGE, mContext);
                }
            }

            // 전화를 받았을시.
            else if (ring && callReceived && SEND.equals("true"))
            {
                //오토인지 확인
                String AUTO = pref.getString("SetAuto", null);

                pref = mContext.getSharedPreferences("CallBack", Activity.MODE_PRIVATE);
                //전화번호 가져오고
                phoneNum = pref.getString("send_number", null);

                //오토일때
                if(AUTO.equals("true")){
                    // 전화 받았을시 오토 문자 내용 가져오고.
                    String MESSAGE = pref.getString("m_Auto_Call",null);
                    if(MESSAGE != null){
                        SendSMS(phoneNum, MESSAGE, mContext);
                        callReceived = false;
                    }
                }

                //아닐때
                else{
                    sendToActivity(mContext);
                    callReceived = false;
                }
            }
        }


    }


    //
    // 바로 문자 보내기.
    // num -> 보낼 전화 번호
    // msg -> 문자 내용
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


    //
    // pref에 저장된 문자내용들 MSGList 로 가져오기.
    //
    private void restoreMessages(Context cont){
        MSGList.clear();
        SharedPreferences pref = cont.getSharedPreferences("AutoMessage", Activity.MODE_PRIVATE);
        if(pref != null)
        {
            Map<String, ?> allEntries = pref.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Message msg = new Message(entry.getKey(), entry.getValue().toString());
                MSGList.add(msg);
            }
        }
    }

    //
    // pActivity(팝업) 에 문자 보낼 전화번호와 저장한 문자리스트 패스한후 실행.
    //
    private void sendToActivity(Context context) {
        Intent intent = new Intent(context, pActivity.class);
        restoreMessages(context);
        intent.putExtra("PNumber", phoneNum);
        for (Message msg : MSGList){
            intent.putExtra(msg.getTitle(), msg.getMessage());
        }

        //화면이 없는곳에서 화면을 띄울 때 사용되는 플래그
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }






}
