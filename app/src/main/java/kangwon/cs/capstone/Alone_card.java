package kangwon.cs.capstone;

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

public class Alone_card extends AppCompatActivity {

    private static final int total = 16;//카드 개수
    private int[] cardId = {R.id.btn01, R.id.btn02, R.id.btn03, R.id.btn04, R.id.btn05, R.id.btn06, R.id.btn07, R.id.btn08,
            R.id.btn09, R.id.btn10, R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15, R.id.btn16};

    private Card[] cardArray = new Card[total];//16개 카드 배열

    private int click_cnt = 0; // 클릭 카운트
    private Card first, second; // 누른 버튼 두개
    private int success_cnt = 0; // 짝 맞추기 성공 카운트
    private Button start_btn, exit_btn;

    int mSec = 100;
    int sec = 50;
    Thread t_thread;//timeout thread
    TextView timeRecord;

    String Msg;
    Result rlt = new Result();//결과 전송 전역변수

    boolean start_flag = false;

    private static MediaPlayer mp;// bgm
    private SoundPool sound_pool;//사운드풀 생성
    private int bomb_bgm;

    class BtnOnClickListener implements ImageButton.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (click_cnt) {
                case 0: // 카드 하나만 뒤집었을 경우
                    for (int i = 0; i < total; i++) {
                        if (cardArray[i].card ==  v) {
                            first = cardArray[i];
                            break;
                        }
                    }
                    if (first.isBack) { // 이미 뒤집힌 카드는 처리 안함
                        first.onFront();
                        click_cnt = 1;
                    }
                    break;
                case 1: // 카드 두개 뒤집었을 경우

                    for (int i = 0; i < total; i++) {
                        findViewById(cardId[i]).setEnabled(false);
                        if (cardArray[i].card == v) {
                            second = cardArray[i];
                        }
                    }
                    if (second.isBack) { // 뒷면이 보이는 카드일 경우만 처리
                        second.onFront();
                        for(int i = 0; i < total; i++){
                            findViewById(cardId[i]).setEnabled(false);
                        }
                        if (first.value == second.value) { // 짝이 맞은 경우
                            success_cnt++;
                            if (success_cnt == total / 2) { // 모든 카드의 짝을 다 맞추었을 경우
                                Toast.makeText(Alone_card.this, "CLEAR!", Toast.LENGTH_SHORT ).show();
                            }
                        }
                        else{
                            Handler delay_Time2 = new Handler();
                            delay_Time2.postDelayed(new Runnable()  {
                                public void run() {
                                    first.onBack();
                                    second.onBack();
                                    for(int i = 0; i < total; i++){
                                        findViewById(cardId[i]).setEnabled(true);
                                    }
                                }
                            }, 120);
                        }
                        click_cnt = 0;
                    }
                    for(int i = 0; i < total; i++){
                        findViewById(cardId[i]).setEnabled(true);
                    }
                    break;
            }
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alone_card);
        new JSONTask().execute(Constants.IP_ADDRESS + "/alone");  //wait=3

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        timeRecord = (TextView)findViewById(R.id.record);
        timeRecord.setTextSize(47);

        for (int i = 0; i < total; i++) {
            cardArray[i] = new Card(i / 2); // 카드 16개 생성
            findViewById(cardId[i]).setEnabled(false);
            findViewById(cardId[i]).setOnClickListener(new BtnOnClickListener()); // 카드 클릭 리스너 설정
            cardArray[i].card = (ImageButton) findViewById(cardId[i]); // 카드 할당


            cardArray[i].onBack(); // 카드 뒤집어 놓음
        }

        mp = MediaPlayer.create(Alone_card.this, R.raw.bgm);
        sound_pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        bomb_bgm = sound_pool.load(Alone_card.this, R.raw.bomb, 1);

        start_btn = (Button) findViewById(R.id.btn_start);
        start_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(Alone_card.this, "시작버튼", Toast.LENGTH_SHORT ).show();
                startGame();
            }
        });

        exit_btn = (Button) findViewById(R.id.btn_exit);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(Alone_card.this, "닫기버튼", Toast.LENGTH_SHORT ).show();

                if(start_flag == false) { //시작 전
                    mp.stop();
                    Intent intent = new Intent(getApplicationContext(), Alone.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                }
                else {
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
        start_flag = true;
        int[] random = new int[total];//16개 랜덤배열
        int x;
        t_thread = new Thread (new timeThread());

        start_btn.setClickable(false);
        start_btn.setVisibility(View.INVISIBLE);
        exit_btn.setClickable(false);
        exit_btn.setVisibility(View.INVISIBLE);

        for (int i = 0; i < total; i++) { // 모든 카드의 뒷면이 보이게 함
            if (!cardArray[i].isBack) {//isBack == true이면 뒷면인 상태, !isBack ==true이면 앞면인 상태
                cardArray[i].onBack();
            }
        }

        boolean bool;

        for (int i = 0; i < total; i++) { // 0~15까지 랜덤한 순서로 random배열에 저장
            while (true) {
                bool = false;
                x = (int) (Math.random() * total);
                for (int j = 0; j < i; j++) {
                    if (random[j] == x) {
                        bool = true;
                        break;
                    }
                }
                if (!bool) break;
            }
            random[i] = x;
        }

        start_btn.setClickable(false);//start버튼을 한번만 누를 수 있도록 비활성화해줌

        for (int i = 0; i < total; i++) {
            cardArray[i].card = (ImageButton) findViewById(cardId[random[i]]);
            cardArray[i].onFront();
        }

        Handler delay_Time1 = new Handler();
        delay_Time1.postDelayed(new Runnable()  {
            public void run() {
                for(int i = 0; i < total; i++){
                    cardArray[i].onBack();
                    findViewById(cardId[i]).setEnabled(true);
                }

                t_thread.start();
            }
        }, 1800);


        Toast.makeText(Alone_card.this, "START!", Toast.LENGTH_SHORT ).show();
        success_cnt = 0;
        click_cnt = 0;

    }//end startGame()

    Handler timeOut = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mSec = 99 - (msg.arg1 % 100);
            sec = 14 - ((msg.arg1 / 100) % 60);
            String result = String.format("%02d:%02d", sec, mSec);

            if((sec <= 0 && mSec == 0  || sec < 0) && (success_cnt != total / 2))
            {
                t_thread.interrupt();
                mp.stop();
                sound_pool.play(bomb_bgm, 1.0f, 1.0f, 1, 0, 1.0f);//재생
                timeRecord.setTextColor(Color.parseColor("#697061"));
                timeRecord.setText("GAME OVER");
                Msg = "LOSE! 다시 도전하세요!";
                for (int j = 0; j < total; j++) {
                    findViewById(cardId[j]).setEnabled(false);
                }
                rlt.setSave_result("FALSE");
                new JSONTask().execute(Constants.IP_ADDRESS + "/result");  //기록 저장
                exit_btn.setClickable(true);
                exit_btn.setVisibility(View.VISIBLE);
                return;
            }
            else if(success_cnt == total / 2)
            {
                t_thread.interrupt();
                mp.stop();
                sound_pool.play(bomb_bgm, 1.0f, 1.0f, 1, 0, 1.0f);
                Msg = "WIN! 이겼어요!";
                rlt.setSave_result("TRUE");
                new JSONTask().execute(Constants.IP_ADDRESS + "/result");  //기록 저장
                exit_btn.setClickable(true);
                exit_btn.setVisibility(View.VISIBLE);
                return;

            }
            else {
                if(sec < 5)
                {
                    timeRecord.setTextColor(Color.RED);
                }
                timeRecord.setText(result);
            }
        }
    };

    public void onBackPressed(){

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

    public class timeThread implements Runnable{
        @Override
        public void run() {
            int i = 0;
            while (!(sec == 0 && mSec == 0)) {// 議곌굔 留뚯”?섎뒗?숈븞
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
                    return;
                }
            }
        }
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