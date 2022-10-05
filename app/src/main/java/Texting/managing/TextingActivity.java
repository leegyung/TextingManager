package Texting.managing;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class TextingActivity extends AppCompatActivity {

    private ArrayList<Contact> PeopleList = new ArrayList<>();
    private ArrayList<Contact> GroupList = new ArrayList<>();
    private ArrayList<String> CleanNumbers = new ArrayList<>();
    private ArrayList<Message> MessageList = new ArrayList<>();
    private boolean[] IndPrevSelection;
    private boolean[] GrPrevSelection;
    private ArrayList<ArrayList<String>> SendingList = new ArrayList<>();
    private CheckBox individual_check, group_check, instant_check, split_check;
    private Button showList, saveText, getText, sendText;
    private TextView selectList;
    private EditText messageText, Split_Number;

    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texting);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        messageText = (EditText)findViewById(R.id.message_text);
        selectList = (TextView)findViewById(R.id.select_List);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        addListenerOnButton();
        GetContacts();
        restoreMessages();

    }


    //
    // 개인 혹은 그룹 리스트 목록선택후 sendinglist 에 추가 dialog.
    // 개인 일시 사람들 이름 목록
    // 그룹일시 그룹 이름 목록
    //
    private void viewList(boolean individual, ArrayList<Contact> ContList){
        ArrayList<String> item = new ArrayList<>();

        for(Contact cont : ContList){
            item.add(cont.getName());
        }

        Collections.sort(item, String.CASE_INSENSITIVE_ORDER);

        final boolean[] selected = new boolean[ContList.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(TextingActivity.this);
        builder.setTitle("선택 목록");
        builder.setMultiChoiceItems(item.toArray(new String[item.size()]), selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){selected[which] = true;}
                else if(!isChecked){selected[which] = false;}
            }
        });
        
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(individual) {
                    SendingList.clear();
                    for (int i = 0; i < ContList.size(); i++){
                        if(selected[i]){
                            SendingList.add(GetPersonNumber(ContList.get(i).getID()));
                        }
                    }
                }

                else{
                    SendingList.clear();
                    for (int i = 0; i < ContList.size(); i++){
                        if(selected[i]){
                            SendingList.add(GetGroupNumber(ContList.get(i).getID()));
                        }
                    }
                }
                dialog.dismiss();
                CleanNumbers = CleanNumbers();
                selectList.setText(Integer.toString(CleanNumbers.size()) + " 명");
                }
            });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(TextingActivity.this, "취소됨", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }


    //
    // 저장된 메세지 리스트 다이얼로그 보여주기.
    //
    private void viewMessageList(){
        ArrayList<String> items = new ArrayList<>();
        for(Message msg : MessageList){
            items.add(msg.getTitle());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TextingActivity.this);
        builder.setTitle("문자 선택");
        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                messageText.setText(MessageList.get(id).getMessage());
            }
        });
        builder.show();
    }

    //
    // 지금 메세지 저장하기.
    //
    private void AddNewMessage(){
        String currentMsg = messageText.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("제목 입력:");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = input.getText().toString();
                MessageList.add(new Message(title, currentMsg));
            }
        });

        alert.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(TextingActivity.this, "취소됨", Toast.LENGTH_SHORT).show();
                    }
                });
        alert.show();


    }

    //
    // 바로 문자 보내기.
    //
    private void SendSMS(){
        ArrayList<String> numbers = CleanNumbers();
        if (numbers.size() > 200)
        {
            Toast.makeText(getApplicationContext(), "200명 한도 이상", Toast.LENGTH_SHORT).show();
        }
        else if (messageText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "문자 내용 없음", Toast.LENGTH_SHORT).show();
        }
        else{
            for(String num : numbers){
                try {
                    //전송
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(num, null, messageText.getText().toString(), null, null);
                    Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

        }
    }

    
    //
    // 예약문자시 alarmManager 만든후 예약
    //
    private void ReserveSMS(String numbers){
        if (CleanNumbers.size() > 200)
        {
            Toast.makeText(getApplicationContext(), "200명 한도 이상", Toast.LENGTH_SHORT).show();
        }
        else {
            SharedPreferences pref = getSharedPreferences("Sending", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("num", numbers);
            editor.putString("text", messageText.getText().toString());
            SaveList save = new SaveList();
            save.setStringArrayPref(getApplicationContext(), "List", CleanNumbers);
            editor.apply();


            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String getTime = dateFormat.format(date);

            int h =Integer.parseInt(getTime.substring(0, 2));
            int m = Integer.parseInt(getTime.substring(3, 5));
            int s =Integer.parseInt(getTime.substring(6, 8));


            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getTime.substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(getTime.substring(3, 5)));
            calendar.set(Calendar.SECOND, Integer.parseInt(getTime.substring(6, 8)));
            calendar.set(Calendar.MILLISECOND, 0);

            int temp = CleanNumbers.size();
            while (temp > 0) {
                Intent intent = new Intent(this, TextingReceiver.class);
                final int id = (int) System.currentTimeMillis();
                PendingIntent pIntent = PendingIntent.getBroadcast(this, id, intent, 0);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);

                temp -= Integer.parseInt(numbers);
                calendar.add(Calendar.MINUTE, 1);
            }
            Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_SHORT).show();
            split_check.setChecked(false);
            instant_check.setChecked(false);
            Split_Number.setText("");
        }
    }


    //
    // 모든 버튼 눌렀을시 실행 함수 설정.
    //
    private void addListenerOnButton(){
        individual_check = (CheckBox)findViewById(R.id.individual);
        group_check = (CheckBox)findViewById(R.id.group);
        showList = (Button)findViewById(R.id.choice);
        saveText = (Button)findViewById(R.id.save_text);
        getText = (Button)findViewById(R.id.get_text);
        instant_check = (CheckBox)findViewById(R.id.instant);
        split_check = (CheckBox)findViewById(R.id.split_check);
        sendText = (Button)findViewById(R.id.send);
        Split_Number = (EditText)findViewById(R.id.split_numbers);

        //문자저장 눌렀을시
        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewMessage();
            }
        });

        //불러오기 눌렀을시
        getText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMessageList();
            }
        });

        //목록 선택 눌렀을시
        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (individual_check.isChecked())
                {
                    viewList(true, PeopleList);
                }
                else if (group_check.isChecked())
                {
                    viewList(false, GroupList);
                }
            }
        });

        //전송 눌렀을 시
        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (instant_check.isChecked()) {
                    SendSMS();
                }
                else if (split_check.isChecked()) {
                    ReserveSMS(Split_Number.getText().toString());
                }



            }
        });

        //그룹선택 선택시
        group_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { individual_check.setChecked(false); }
            }
        });

        //개인선택 선택시
        individual_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { group_check.setChecked(false); }
            }
        });

        //즉시 발송 선택 시
        instant_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { split_check.setChecked(false); }
            }
        });
        //분활 선택 시
        split_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { instant_check.setChecked(false); }
            }
        });

    }


    //
    // 개인, 그룹 들 contact class로 만들고 저장.
    //
    private void GetContacts() {

        ContentResolver cr = getContentResolver();
        Uri groupsUri = ContactsContract.Groups.CONTENT_SUMMARY_URI;
        String where = "((" + ContactsContract.Groups.SUMMARY_WITH_PHONES + "!= 0))";
        Cursor cur = cr.query(groupsUri, null, where, null, null);


        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String ID = cur.getString(
                        cur.getColumnIndex(ContactsContract.Groups._ID));
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Groups.TITLE));
                GroupList.add(new Contact(name, ID));
            }
        }

        cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                PeopleList.add(new Contact(name, id));
            }
        }

/*
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);


        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Groups._ID));
                String temp = getGroupNameFor(Long.parseLong(id), cr);
                int i = 0;


                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                

                if (!IDList.contains(id)){
                    Changed = true;
                    IDList.add(id);
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));
                    ArrayList<String> groups = getGroupIdFor(Long.parseLong(id), cr);

                    ArrayList<String> numbers = new ArrayList<>();

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            numbers.add(phoneNo);
                        }
                        pCur.close();
                    }

                    Contact contact = new Contact(name, numbers, groups);
                    ContactList.add(contact);
                }

            }
        }*/
        if(cur!=null){
            cur.close();
        }
    }



    //
    // 특정 id 연락처의 모든 번호를 리턴 (한사람이 여러번호 가지고 있기도 함).
    //
    private ArrayList<String> GetPersonNumber (String id)
    {
        ArrayList<String> numbers = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{id}, null);

        while (pCur.moveToNext()) {
            String phoneNo = pCur.getString(pCur.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
            numbers.add(phoneNo);
        }
        pCur.close();

        return numbers;
    }


    //
    // 특정 id 그룹의 모든 번호를 리턴.
    //
    private ArrayList<String> GetGroupNumber (String id) {
        ArrayList<String> numbers = new ArrayList<>();

        Cursor groupCursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ?" + " AND "
                        + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                new String[] { String.valueOf(id) }, null);

        if (groupCursor != null && groupCursor.moveToFirst())
        {
            do
            {
                long contactId = groupCursor.getLong(groupCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                Cursor numberCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                        null, null);

                if (numberCursor.moveToFirst())
                {
                    int numberColumnIndex = numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    do
                    {
                        numbers.add(numberCursor.getString(numberColumnIndex));
                    } while (numberCursor.moveToNext());
                    numberCursor.close();
                }
            } while (groupCursor.moveToNext());
            groupCursor.close();
        }
        return numbers;
    }


    //
    // 전화번호들 중복제거, 단일리스트로 만들고, 불필요문자 제거
    //
    private ArrayList<String> CleanNumbers(){
        ArrayList<String> clean = new ArrayList<>();
        for(ArrayList<String> i : SendingList){
            for(String num : i){
                String cleanNum = num.replace("+82", "0").replaceAll("[^0-9]", "");
                if(!clean.contains(cleanNum)){ clean.add(cleanNum); }
            }
        }
        return clean;
    }



    //
    // 엡이 꺼지거나 멀티 윈도우로 갈때
    //
    @Override
    protected void onPause() {
        super.onPause();
        saveMessages();
    }
    

    //
    // pref 에 Messages list 저장.
    //
    private void saveMessages(){
        SharedPreferences pref = getSharedPreferences("SavedMessage", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();

        for(Message msg : MessageList){
            editor.putString(msg.getTitle(), msg.getMessage());
        }
        editor.apply();
    }


    //
    // pref 에 저장한 문자들 가져오기.
    //
    private void restoreMessages(){
        SharedPreferences pref = getSharedPreferences("SavedMessage", Activity.MODE_PRIVATE);
        if(pref != null)
        {
            Map<String, ?> allEntries = pref.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Message msg = new Message(entry.getKey(), entry.getValue().toString());
                MessageList.add(msg);
            }
        }
    }


}