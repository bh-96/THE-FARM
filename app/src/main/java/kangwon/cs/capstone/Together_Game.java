package kangwon.cs.capstone;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.StringTokenizer;

public class Together_Game extends AppCompatActivity implements SensorEventListener {

    private Button start_btn, exit_btn;

    int mSec = 100;
    int sec = 50;
    int shake_count = 0;

    boolean start_flag = false;

    Thread t_thread;//timeout thread

    TextView timeRecord;//타이머 view
    TextView countRecord;//카운트 view

    String Msg;//게임 결과에 따른 메시지

    //진동감지
    SensorManager sensorManager;
    Sensor accelerormeterSensor;

    float speed, previousX, previousY, previousZ;
    float currentX, currentY, currentZ;
    long lastTime;
    private static final int SHAKE_THRESHOLD = 1100;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private static MediaPlayer mp;// bgm
    private SoundPool sound_pool;//사운드풀 생성
    private int bomb_bgm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_together_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new JSONTask().execute(Constants.IP_ADDRESS + "/together");  //wait=4

        timeRecord = (TextView) findViewById(R.id.record);
        countRecord = (TextView) findViewById(R.id.count);
        countRecord.setTextSize(80);
        countRecord.setText(Integer.toString(shake_count));

        start_btn = (Button) findViewById(R.id.btn_start);
        exit_btn = (Button) findViewById(R.id.btn_exit);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mp = MediaPlayer.create(Together_Game.this, R.raw.bgm);
        sound_pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        bomb_bgm = sound_pool.load(Together_Game.this, R.raw.bomb, 1);

        start_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new JSONTask().execute(Constants.IP_ADDRESS + "/medal");
                startGame();
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(start_flag == true)
                {//게임 끝나고 종료
                    t_thread.interrupt();
                    Toast.makeText(Together_Game.this, "종료", Toast.LENGTH_SHORT ).show();
                    new JSONTask().execute(Constants.IP_ADDRESS + "/record");
                }
                else{//게임 시작 전 종료
                    Toast.makeText(Together_Game.this, "시작 전 나가기", Toast.LENGTH_SHORT ).show();

                    mp.stop();
                    Intent intent = new Intent(getApplicationContext(), Together.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }

    public void onBack(View v){
        new JSONTask().execute(Constants.IP_ADDRESS + "/record");
    }

    void startGame()  {

        mp.start();
        t_thread = new Thread (new timeThread());//시간 측정
        start_flag = true;

        start_btn.setClickable(false);
        start_btn.setVisibility(View.INVISIBLE);
        exit_btn.setClickable(false);
        exit_btn.setVisibility(View.INVISIBLE);
        if(accelerormeterSensor!=null) {
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
        }


        t_thread.start();



    }//end startGame()

    Handler timeOut = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 시간 format :
            mSec = 99 - (msg.arg1 % 100);
            sec = 9 - ((msg.arg1 / 100) % 60);
            String result = String.format("%02d:%02d", sec, mSec);

            if(sec <= 0 && mSec == 0 || sec < 0)
            {
                t_thread.interrupt();
                mp.stop();
                sound_pool.play(bomb_bgm, 1.0f, 1.0f, 1, 0, 1.0f);

                if(sensorManager != null) {
                    sensorManager.unregisterListener(Together_Game.this);
                }

                timeRecord.setTextColor(Color.parseColor("#697061"));
                timeRecord.setTextSize(47);
                timeRecord.setText("TIME OVER");
                Msg = Integer.toString(shake_count);
                Result.getInstance().setSave_result_to(Integer.parseInt(Msg));
                exit_btn.setClickable(true);
                exit_btn.setVisibility(View.VISIBLE);
                new JSONTask().execute(Constants.IP_ADDRESS + "/together_result");

                return;
            }
            else {
                if(sec < 3)
                {
                    timeRecord.setTextSize(55);
                    timeRecord.setTextColor(Color.RED);
                }
                timeRecord.setText(result);
            }
        }
    };

    public void onBackPressed(){

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
// Sensor 정보가 변하면 실행됨.
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            //최근 측정한 시간과 현재 시간을 비교하여 0.1초 이상되었을 때 흔듬을 감지한다.
            if(gabOfTime > 130) {
                lastTime = currentTime;
                currentX = event.values[SensorManager.DATA_X];
                currentY = event.values[SensorManager.DATA_Y];
                currentZ = event.values[SensorManager.DATA_Z];

                speed = Math.abs(currentX + currentY + currentZ - previousX - previousY - previousZ)/gabOfTime * 10000;

                if(speed > SHAKE_THRESHOLD) {// 이벤트 발생
                    countRecord.setText(Integer.toString(++shake_count));
                }
                previousX = event.values[DATA_X];
                previousY = event.values[DATA_Y];
                previousZ = event.values[DATA_Z];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }//정확도 설정


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

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Global.getInstance().getGlobal_u());
                jsonObject.accumulate("other", Global.getInstance().getGlobal());
                jsonObject.accumulate("result", Result.getInstance().getSave_result_to());

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

            if(result.equals("together")){
                //wait=4
            } else if(result.equals("ok")) {
                Toast.makeText(getApplicationContext(),"기록 저장: " + Result.getInstance().getSave_result_to(), Toast.LENGTH_SHORT).show();
            }  else if(result.equals("fail")) {
                Toast.makeText(getApplicationContext(),"기록 실패", Toast.LENGTH_SHORT).show();
            } else if(result.equals("record")) {
                Toast.makeText(getApplicationContext(),"친구가 게임이 끝나기를 기다리세요!", Toast.LENGTH_SHORT).show();
            } else if(result.equals("end")) {
                Intent intent = new Intent(getApplicationContext(), Together_result.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish();
            } else{
                StringTokenizer st = new StringTokenizer(result, ",");
                String Medal = st.nextToken();

                Result.getInstance().setSave_medal(Integer.parseInt(Medal));
            }
        }
    }
}