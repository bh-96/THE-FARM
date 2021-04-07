package kangwon.cs.capstone;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by speak on 2018-03-08.
 */

public class ServiceThread extends Thread {
    Handler handler;
    int time = Section4.notificationTime;
    static int notificationOn = 1;


    public ServiceThread(Handler handler) {
        this.handler = handler;
    }


    public void run() {
        try {
            while (time > 0 && notificationOn == 1) {

                Log.d("ServiceTHread ", "notificaition = " + time);
                Thread.sleep(1000);
                time -= 1000;
            }
        } catch (Exception e) {
        }

        if (notificationOn == 1) {
            handler.sendEmptyMessage(0);
        } else {
        }

    }
}

//    public void run(){
//        try{
//            while( time > 0 ){
//                Log.d("ServiceThread","notificiationOn = "+Section4.notificationOn+",Time = "+time);
//                Thread.sleep(1000);
//                time -= 1000;
//            }
//
//        }catch (Exception e){}
//
//        //Section4.notificationOn == 1 &&
////        if(Section4.notificationOn == 1){
//            handler.sendEmptyMessage(0);
////        }else{}}
//
//
//
//
//
//}
