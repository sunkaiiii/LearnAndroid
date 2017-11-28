package com.example.user.testproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btnTest,btnCancel,btnCancelOne;
    EditText idEditText;
    NotificationManager notificationManager;
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTest = (Button) findViewById(R.id.btn_test_btn);
        btnCancel=(Button)findViewById(R.id.btn_cancel);
        btnCancelOne=(Button)findViewById(R.id.btn_cancel_one);
        idEditText=(EditText)findViewById(R.id.editText_id);

        btnTest.setOnClickListener((view) -> {
            Random random=new Random();
            count++;
            sendNotification(count,"message Test");
        });


        btnCancel.setOnClickListener((view -> {
            cancelNotification();
        }));

        btnCancelOne.setOnClickListener((view -> {
            int id=Integer.parseInt(idEditText.getText().toString());
            cancelNotification(id);
        }));
    }

    private void cancelNotification(int id){
        notificationManager.cancel(id);
    }
    private void cancelNotification(){
        if(notificationManager!=null){
            notificationManager.cancelAll();
        }
    }

    private void sendNotification(int id,String content) {
        Intent mainIntent = new Intent(this, MainActivity.class);

        //pendingIntent用于在某个事件结束后执行的特定的Action
        //在这里应用为用户点击通知，才会执行
        PendingIntent mainPendingIntent=PendingIntent.getActivity(this,1,mainIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Resources res=getResources();
        Bitmap bit=BitmapFactory.decodeResource(res,R.drawable.ic_launcher_background);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) {
            Notification builder = new Notification.Builder(this, "tag")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setLargeIcon(bit)
                    .setContentTitle(content)
                    .setContentText(content + content + content + content)
                    .setContentIntent(mainPendingIntent)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(id, builder);
        }
        else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "tag")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setLargeIcon(bit)
                    .setContentTitle(content)
                    .setContentText(content + content + content + content)
                    .setContentIntent(mainPendingIntent).setAutoCancel(true)
            //This method was deprecated in API level 26. use enableVibration(boolean)
                    // and enableLights(boolean) and setSound(Uri, AudioAttributes) instead.
                    .setDefaults(Notification.DEFAULT_ALL);
            notificationManager.notify(id, builder.build());
        }
    }
}
