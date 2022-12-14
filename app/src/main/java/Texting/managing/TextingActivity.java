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
    // ?????? ?????? ?????? ????????? ??????????????? sendinglist ??? ?????? dialog.
    // ?????? ?????? ????????? ?????? ??????
    // ???????????? ?????? ?????? ??????
    //
    private void viewList(boolean individual, ArrayList<Contact> ContList){
        ArrayList<String> item = new ArrayList<>();

        for(Contact cont : ContList){
            item.add(cont.getName());
        }

        Collections.sort(item, String.CASE_INSENSITIVE_ORDER);

        final boolean[] selected = new boolean[ContList.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(TextingActivity.this);
        builder.setTitle("?????? ??????");
        builder.setMultiChoiceItems(item.toArray(new String[item.size()]), selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){selected[which] = true;}
                else if(!isChecked){selected[which] = false;}
            }
        });
        
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                selectList.setText(Integer.toString(CleanNumbers.size()) + " ???");
                }
            });

        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(TextingActivity.this, "?????????", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }


    //
    // ????????? ????????? ????????? ??????????????? ????????????.
    //
    private void viewMessageList(){
        ArrayList<String> items = new ArrayList<>();
        for(Message msg : MessageList){
            items.add(msg.getTitle());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TextingActivity.this);
        builder.setTitle("?????? ??????");
        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                messageText.setText(MessageList.get(id).getMessage());
            }
        });
        builder.show();
    }

    //
    // ?????? ????????? ????????????.
    //
    private void AddNewMessage(){
        String currentMsg = messageText.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("?????? ??????:");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = input.getText().toString();
                MessageList.add(new Message(title, currentMsg));
            }
        });

        alert.setNegativeButton("??????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(TextingActivity.this, "?????????", Toast.LENGTH_SHORT).show();
                    }
                });
        alert.show();


    }

    //
    // ?????? ?????? ?????????.
    //
    private void SendSMS(){
        ArrayList<String> numbers = CleanNumbers();
        if (numbers.size() > 200)
        {
            Toast.makeText(getApplicationContext(), "200??? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
        else if (messageText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
        else{
            for(String num : numbers){
                try {
                    //??????
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(num, null, messageText.getText().toString(), null, null);
                    Toast.makeText(getApplicationContext(), "?????? ??????!", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

        }
    }

    
    //
    // ??????????????? alarmManager ????????? ??????
    //
    private void ReserveSMS(String numbers){
        if (CleanNumbers.size() > 200)
        {
            Toast.makeText(getApplicationContext(), "200??? ?????? ??????", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
            split_check.setChecked(false);
            instant_check.setChecked(false);
            Split_Number.setText("");
        }
    }


    //
    // ?????? ?????? ???????????? ?????? ?????? ??????.
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

        //???????????? ????????????
        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewMessage();
            }
        });

        //???????????? ????????????
        getText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMessageList();
            }
        });

        //?????? ?????? ????????????
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

        //?????? ????????? ???
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

        //???????????? ?????????
        group_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { individual_check.setChecked(false); }
            }
        });

        //???????????? ?????????
        individual_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { group_check.setChecked(false); }
            }
        });

        //?????? ?????? ?????? ???
        instant_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { split_check.setChecked(false); }
            }
        });
        //?????? ?????? ???
        split_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked ) { instant_check.setChecked(false); }
            }
        });

    }


    //
    // ??????, ?????? ??? contact class??? ????????? ??????.
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
    // ?????? id ???????????? ?????? ????????? ?????? (???????????? ???????????? ????????? ????????? ???).
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
    // ?????? id ????????? ?????? ????????? ??????.
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
    // ??????????????? ????????????, ?????????????????? ?????????, ??????????????? ??????
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
    // ?????? ???????????? ?????? ???????????? ??????
    //
    @Override
    protected void onPause() {
        super.onPause();
        saveMessages();
    }
    

    //
    // pref ??? Messages list ??????.
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
    // pref ??? ????????? ????????? ????????????.
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