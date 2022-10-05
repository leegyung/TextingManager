package Texting.managing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;
import java.util.ArrayList;

//
// 예약문자 설정시 특정 시간마다 실행하는 리시버
//
public class TextingReceiver extends BroadcastReceiver {
    private String numbers;
    private String text;
    private ArrayList<String> NumberList = new ArrayList<>();
    final private SaveList Save = new SaveList();


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("Sending", Activity.MODE_PRIVATE);
        numbers = pref.getString("num","");
        text = pref.getString("text", "");
        SendSMS(context);
        ChangeNumbers(context);
    }

    // 번호 리스트에서 보낼 수 만큼 문자보내고 
    // 보낸 번호는 리스트에서 지우기 ChangeNumbers 사용.
    private void SendSMS(Context context){
        NumberList = Save.getStringArrayPref(context, "List");
        for(int i = 0; i < Integer.parseInt(numbers); i++){
            try {
                //전송
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(NumberList.get(i), null, text, null, null);
            }
            catch (Exception e) {
                Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        Toast.makeText(context, "예약 문자 전송", Toast.LENGTH_SHORT).show();
    }

    private void ChangeNumbers(Context context){
        // NumberList 앞에 보낸사람 지우고 새거를 Save.setStringArrayPref(); 에 저장
        ArrayList<String> subList = new ArrayList<>();
        if(NumberList.size() > Integer.parseInt(numbers)){
            for (int i = Integer.parseInt(numbers); i < NumberList.size(); i++){
                subList.add(NumberList.get(i));
            }
            Save.setStringArrayPref(context, "List", subList);
        }
    }


}
