package kangwon.cs.capstone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.widget.RemoteViews;

public class MyService extends Service {
    NotificationManager Notifi_M;
    ServiceThread thread;
    Notification Notifi ;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();

        return START_STICKY;

    }

    class myServiceHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override

        public void handleMessage(android.os.Message msg) {

            RemoteViews contentView;

            Intent intent = new Intent(MyService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);

            Notifi = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.kong_mini)
                    .setContent(contentView)
                    .setContentIntent(pendingIntent)
                    .build();

            //소리추가
            Notifi.defaults = Notification.DEFAULT_SOUND;
            //알림 소리를 한번만 내도록
            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            //확인하면 자동으로 알림이 제거 되도록
            Notifi.flags = Notification.FLAG_AUTO_CANCEL;
            Notifi_M.notify(3452 , Notifi);

            //토스트 띄우기

        }
    }
}

//            Notifi = new Notification.Builder(getApplicationContext())
//                    .setContentTitle("강낭콩키우기")
//                    .setContentText("다시 접속해라")
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setTicker("뭐야...")
//                    .setContentIntent(pendingIntent)
//                    .build();

