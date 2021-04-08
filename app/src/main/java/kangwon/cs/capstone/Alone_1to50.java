package kangwon.cs.capstone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class Alone_1to50 extends AppCompatActivity {
    private static final int total = 25;//버튼 개수
    private int[] btnId = {R.id.btn01, R.id.btn02, R.id.btn03, R.id.btn04, R.id.btn05, R.id.btn06, R.id.btn07, R.id.btn08,
            R.id.btn09, R.id.btn10, R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15, R.id.btn16, R.id.btn17, R.id.btn18,
            R.id.btn19, R.id.btn20, R.id.btn21,R.id.btn22,R.id.btn23,R.id.btn24, R.id.btn25};


    private int num_Array1[] = new int[total];//버튼에 들어갈 숫자 배열(1-25)
    private int num_Array2[] = new int[total];//버튼에 들어갈 숫자 배열(26-50)
    private int step;//몇까지 선택했는지
    private boolean state = false;
    private Button start_btn, exit_btn;
    private  Button btn_Array[] = new Button[total];//버튼 배열
    int mSec = 100;
    int sec = 50;

    boolean start_flag = false;

    Thread t_thread;//timeout thread
    TextView timeRecord;//타이머 view

    ProgressDialog dialog;//게임 종료 후 3초간 대기
    String Msg;//게임 결과에 따른 메시지
    Thread e_thread;

    Result rlt = new Result();//결과 저장(TRUE, FALSE)

    private static MediaPlayer mp;// bgm
    private SoundPool sound_pool;//사운드풀 생성
    private int bomb_bgm;

    class BtnOnClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            int x = Integer.parseInt(((Button)v).getText().toString());

            if(x == step){
                if(step >=  26){
                    ((Button)v).setVisibility(View.INVISIBLE);
                }
                else{
                    ((Button)v).setText("" + num_Array2[25 - step]);
                }
                step++;
            }

            if(step == 51)
            {
                Toast.makeText(getApplicationContext(), "CLEAR!", Toast.LENGTH_SHORT ).show();
            }
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alone_1to50);
        new JSONTask().execute(Constants.IP_ADDRESS + "/alone");  //wait=3
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        timeRecord = (TextView) findViewById(R.id.record);

        mp = MediaPlayer.create(Alone_1to50.this, R.raw.bgm);
        sound_pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        bomb_bgm = sound_pool.load(Alone_1to50.this, R.raw.bomb, 1);

        step = 1;

        for (int i = 0; i < total; i++) {
            btn_Array[i] = (Button) findViewById(btnId[i]);
            findViewById(btnId[i]).setEnabled(false);
            btn_Array[i].setTextSize(23);
            btn_Array[i].setVisibility(View.VISIBLE);//INVISIBLE 로 안보이게 처리할 예정
            findViewById(btnId[i]).setOnClickListener(new BtnOnClickListener());// 리스너 설정
        }

        start_btn = (Button) findViewById(R.id.btn_start);
        start_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(Alone_1to50.this, "시작버튼", Toast.LENGTH_SHORT ).show();
                startGame();
            }
        });


        exit_btn = (Button) findViewById(R.id.btn_exit);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(start_flag == false) {//시작 전
                    Toast.makeText(Alone_1to50.this, " 시작 전 닫기", Toast.LENGTH_SHORT ).show();
                    mp.stop();
                    Intent intent = new Intent(getApplicationContext(), Alone.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                }
                else{
                    Toast.makeText(Alone_1to50.this, "끝", Toast.LENGTH_SHORT ).show();

                    t_thread.interrupt();
                    Intent intent = new Intent(getApplicationContext(), Alone.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }

    void startGame()  {
        mp.start();
        start_btn.setClickable(false);
        start_btn.setVisibility(View.INVISIBLE);
        exit_btn.setClickable(false);
        exit_btn.setVisibility(View.INVISIBLE);
        start_flag = true;

        for(int i = 0; i < total; i++){
            num_Array1[i] = i + 1;
        }

        shakeNum1();

        t_thread = new Thread (new timeThread());

        Handler shake_delay1 = new Handler();
        shake_delay1.postDelayed(new Runnable()  {
            public void run() {
                for(int i = 0; i < total; i++)
                {
                    findViewById(btnId[i]).setEnabled(true);
                    btn_Array[i].setText("" + num_Array1[i]);
                    btn_Array[i].setVisibility(View.VISIBLE);
                }
                t_thread.start();
            }
        }, 100);
        for(int i = 0; i < total; i++){
            num_Array2[i] = i + 26;
        }
        shakeNum2();

    }//end startGame()

    void shakeNum1(){
        int x = 0;
        int y = 0;
        int tmp = 0;

        Random rand = new Random();

        for(int i = 0; i < 100; i++){
            x = rand.nextInt(24);
            y = rand.nextInt(24);

            if(x == y) continue;

            tmp = num_Array1[x];
            num_Array1[x] = num_Array1[y];
            num_Array1[y] = tmp;
        }
    }

    void shakeNum2(){
        int x = 0;
        int y = 0;
        int tmp = 0;

        Random rand = new Random();

        for(int i = 0; i < 100; i++){
            x = rand.nextInt(24);
            y = rand.nextInt(24);

            if(x == y) continue;

            tmp = num_Array2[x];
            num_Array2[x] = num_Array2[y];
            num_Array2[y] = tmp;
        }
    }


    Handler timeOut = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 시간 format :
            mSec = 99 - (msg.arg1 % 100);
            sec = 49 - ((msg.arg1 / 100) % 60);
            String result = String.format("%02d:%02d", sec, mSec);

            if((sec <= 0 && mSec == 0  || sec < 0) && step != 51)
            {
                t_thread.interrupt();
                mp.stop();
                sound_pool.play(bomb_bgm, 1.0f, 1.0f, 1, 0, 1.0f);
                timeRecord.setTextColor(Color.parseColor("#697061"));
                timeRecord.setTextSize(47);
                timeRecord.setText("GAME OVER");
                Msg = "LOSE! 다시 도전하세요!";
                for (int j = 0; j < total; j++) {
                    findViewById(btnId[j]).setClickable(false);
                }
                rlt.setSave_result("FALSE");
                new JSONTask().execute(Constants.IP_ADDRESS + "/result");  //기록 저장
                exit_btn.setClickable(true);
                exit_btn.setVisibility(View.VISIBLE);
                //endThread();//progress dialog
                return;
            }
            else if(step == 51)
            {
                t_thread.interrupt();
                mp.stop();
                sound_pool.play(bomb_bgm, 1.0f, 1.0f, 1, 0, 1.0f);
                Msg = "WIN! 이겼어요!";
                rlt.setSave_result("TRUE");
                new JSONTask().execute(Constants.IP_ADDRESS + "/result");  //기록 저장
                exit_btn.setClickable(true);
                exit_btn.setVisibility(View.VISIBLE);
                //endThread();//progress dialog
                return;

            }
            else {
                if(sec < 5)
                {
                    timeRecord.setTextSize(55);
                    timeRecord.setTextColor(Color.RED);
                }
                timeRecord.setText(result);
            }
        }
    };


    public void endThread() {

        dialog = new ProgressDialog(this);
        dialog = new ProgressDialog(this);
        dialog.setTitle(Msg);
        dialog.setMessage("결과 저장중입니다.\n잠시만 기다려주세요");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
                //e_thread.interrupt();
                start_btn.setClickable(true);
                start_btn.setVisibility(View.VISIBLE);
            }
        }).start();
    }

    public void onBackPressed(){
        //백키 막기
    }

    public class timeThread implements Runnable{
        @Override
        public void run() {
            int i = 0;
            while (!(sec == 0 && mSec == 0)) {// 조건 만족하는동안
                if (sec <= 0 && mSec == 0 || sec < 0) {
                    sec = 0;
                    mSec = 0;
                    break;
                }
                Message msg = new Message();
                msg.arg1 = i++;
                timeOut.sendMessage(msg);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    return; // 인터럽트 받을 경우 return됨
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("onPause", "*** game over");

        Intent intent = new Intent(getApplicationContext(), Alone.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Global.getInstance().getGlobal_u());
                jsonObject.accumulate("result", rlt.getSave_result());

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);

                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();

                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("result")){
                Toast.makeText(getApplicationContext(), Global.getInstance().getGlobal_u() + "'s result: " + rlt.getSave_result(), Toast.LENGTH_SHORT).show();
            } else if(result.equals("alone")){
                //wait=3
            } else{
                //game_menu wait=2
            }
        }
    }
}
