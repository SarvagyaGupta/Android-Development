package edu.stanford.cse193a.todolist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import java.util.Date;
import java.util.Objects;

public class NotificationService extends IntentService {
    static final String ACTION_NOTIFY_USER = "notifyUser";
    static final String ACTION_DONE = "done";
    static int ID = 1;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).equals(ACTION_NOTIFY_USER)) {
            String title = intent.getStringExtra("title");
            Date date = (Date) intent.getSerializableExtra("date");

            long time = (date.getTime() - System.currentTimeMillis()) / 1000;
            double hour = time / 3600.0;
            double mins = (time % 3600) / 60.0;
            String text = "Due in: " + (int) mins + " minute(s)";

            if (hour >= 1) {
                text = "Due in: 1 hour";
            }

            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, ToDoListActivity.class), 0);

            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("channel 1",
                        "my channel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);

                builder = new Notification.Builder(this, "channel 1");
            } else {
                builder = new Notification.Builder(this);
            }

            builder = builder.setContentTitle(title.toUpperCase())
                    .setContentText(text)
                    .setSmallIcon(R.drawable.notification)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            ID++;

            new Thread(() -> {
                if (hour >= 1) {
                    try {
                        Thread.sleep((int) ((hour - 1) * 3600 * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                manager.notify(ID, notification);
            }).start();

            Intent done = new Intent();
            done.setAction(ACTION_DONE);
            sendBroadcast(done);
        }
    }
}
