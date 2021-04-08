package kangwon.cs.capstone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

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
import java.util.StringTokenizer;

public class Section4 extends AppCompatActivity {

    static Typeface typeface;
    static Display display;

    //상태바
    ProgressBar barHunger ;
    ProgressBar barHappiness;
    ProgressBar barHealth;
    ProgressBar barActive;
    ProgressBar barStress;
    ProgressBar barExperience;

    String charNum ;

    SoundPool sound;
    int soundId;


    StateOperation stateOperation;

    //어플이 언제 꺼지고 켜지는 지
    long startTime;
    long endTime;


    ImageView produce;
    TextView name;


    //notification 시간
    static int notificationTime = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section4);

        //bar id 찾기
        barHunger = (ProgressBar) findViewById(R.id.barHunger);
        barHappiness = (ProgressBar) findViewById(R.id.barHappiness);
        barHealth = (ProgressBar) findViewById(R.id.barHealth);
        barActive = (ProgressBar) findViewById(R.id.barActive);
        barStress = (ProgressBar) findViewById(R.id.barStress);
        barExperience = (ProgressBar) findViewById(R.id.barExperience);
        produce = (ImageView) findViewById(R.id.produce);
        name = (TextView) findViewById(R.id.name);

        //progress최대치 설정
        barHunger.setMax(100);
        barHappiness.setMax(100);
        barHealth.setMax(100);
        barActive.setMax(100);
        barStress.setMax(100);
        barExperience.setMax(100);

        TextView textHunger = (TextView) findViewById(R.id.textHunger);
        TextView textActive = (TextView) findViewById(R.id.textActive);
        TextView textHappiness = (TextView) findViewById(R.id.textHappiness);
        TextView textHealth = (TextView) findViewById(R.id.textHealth);
        TextView textStress = (TextView) findViewById(R.id.textStress);
        TextView textExperience = (TextView) findViewById(R.id.textExperience);

        typeface = Typeface.createFromAsset(getAssets(),"fonts/DungGeunMo.otf");

        textHunger.setTypeface(typeface);
        textActive.setTypeface(typeface);
        textHappiness.setTypeface(typeface);
        textHealth.setTypeface(typeface);
        textStress.setTypeface(typeface);
        textExperience.setTypeface(typeface);
        name.setTypeface(typeface);

        display = getWindowManager().getDefaultDisplay();

        sound = new SoundPool(5,AudioManager.STREAM_MUSIC,0);
        soundId = sound.load(this,R.raw.buttonclick,1);


    }

    public void onGame(View v) {

        sound.play(soundId,1f,1f,0,0,1f);


        Intent intent = new Intent(getApplicationContext(),Game_menu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    public void saveState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("stateHunger",State.getInstance().getStateHunger());
        editor.putInt("stateHappiness",State.getInstance().getStateHappiness());
        editor.putInt("stateHealth",State.getInstance().getStateHealth());
        editor.putInt("stateActive",State.getInstance().getStateActive());
        editor.putInt("stateStress",State.getInstance().getStateStress());
        editor.putInt("stateExperience",State.getInstance().getStateExperience());

        State.getInstance().setEndTime(System.currentTimeMillis());

        editor.commit();


    }

    public void restoreState() {

        long diffTime;

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        //저장한 값 불러오기
        State.getInstance().setStateHealth(pref.getInt("stateHealth",100));
        State.getInstance().setStateHunger(pref.getInt("stateHunger",100));
        State.getInstance().setStateHappiness(pref.getInt("stateHappiness",100));
        State.getInstance().setStateActive(pref.getInt("stateActive",100));
        State.getInstance().setStateStress(pref.getInt("stateStress",10));
        State.getInstance().setStateExperience(pref.getInt("stateExperience",0));


        if(State.getInstance().getEndTime() == 0)
            return;

        //저장한 값을 시간에 따라 다시 계산하기
        diffTime = (startTime - State.getInstance().getEndTime())/1000;
        Log.d("restoreState","diffTime ="+diffTime);

        //시간에 따라 값계산해주기
        State.getInstance().setStateHealth(State.getInstance().getStateHealth()-State.getInstance().getDegreeHealth()*(int)diffTime);
        State.getInstance().setStateHunger(State.getInstance().getStateHunger()-State.getInstance().getDegreeHunger()*(int)diffTime);
        State.getInstance().setStateHappiness(State.getInstance().getStateHappiness()-State.getInstance().getDegreeHappiness()*(int)diffTime);
        State.getInstance().setStateActive(State.getInstance().getStateActive()-State.getInstance().getDegreeActive()*(int)diffTime);
        State.getInstance().setStateStress(State.getInstance().getStateStress()-State.getInstance().getDegreeStress()*(int)diffTime);

        changeState();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //디비에서 캐릭터 상태 불러오기
        new JSONTask().execute(Constants.IP_ADDRESS + "/sec4");

        //여기에 시간계산넣어주세요~
        startTime = System.currentTimeMillis();
        Log.d("onStart","startTime"+startTime);

        restoreState();

        // Operation 정의 time,value,
        stateOperation = new StateOperation();
        // Operation 실행
        stateOperation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //예약된 notification 취소
        ServiceThread.notificationOn = 0;

        Intent intent = new Intent(Section4.this,MyService.class);
        stopService(intent);

        Log.d("onStart","endTime"+State.getInstance().getEndTime());


    }

    @Override
    protected void onPause() {
        super.onPause();
        stateOperation.cancel(true);

        saveState();


        //notifiacation 생성
        ServiceThread.notificationOn = 1;
        Intent intent = new Intent(Section4.this,MyService.class);
        startService(intent);

        //디비에 캐릭터 상태 저장하기
        new JSONTask().execute(Constants.IP_ADDRESS + "/save");
        Log.d("onPause","** Save state **");
    }

    //상태 움직이는 중요한 쓰레드당
    private class StateOperation extends AsyncTask<Void,Void,Void> {

        int value = 1;
        Intent intent_death = new Intent(getApplicationContext(), Death.class);
        Intent intent_sucess = new Intent(getApplicationContext(), Sucess.class);
        @Override
        protected Void doInBackground(Void... params) {

            while(true) {
                if( State.getInstance().getStateHunger() >= 0 && State.getInstance().getStateHealth()>= 0 && State.getInstance().getStateHappiness() >= 0 && State.getInstance().getStateStress() <= 100 && State.getInstance().getDegreeActive() >= 0 && !isCancelled()) {
                    try {
                        State.getInstance().setStateHealth(State.getInstance().getStateHealth()-State.getInstance().getDegreeHealth());
                        State.getInstance().setStateHunger(State.getInstance().getStateHunger()-State.getInstance().getDegreeHunger());
                        State.getInstance().setStateHappiness(State.getInstance().getStateHappiness()-State.getInstance().getDegreeHappiness());
                        State.getInstance().setStateActive(State.getInstance().getStateActive()-State.getInstance().getDegreeActive());
                        State.getInstance().setStateStress(State.getInstance().getStateStress()+State.getInstance().getDegreeStress());
                        // 랜덤으로 똥 생성!
                        value = new Random().nextInt(4);
                        //경험치가 300일 때 그만
                        if(State.getInstance().getStateExperience() >= 300)
                            break;
                        publishProgress();
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {}
                }else break;
            }

//            try{
//                Thread.sleep(3000);
//            }catch (Exception e){}

            return null;

        }


        @Override
        protected synchronized void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            barHunger.setProgress(State.getInstance().getStateHunger());
            barActive.setProgress(State.getInstance().getStateActive());
            barHappiness.setProgress(State.getInstance().getStateHappiness());
            barHealth.setProgress(State.getInstance().getStateHealth());
            barStress.setProgress(State.getInstance().getStateStress());
            //똥생성
            if(value == 0)
                createActivity("poop");


        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //new JSONTask().execute(Constants.IP_ADDRESS + "/delete");

            if(State.getInstance().getStateExperience() >= 300) {

                intent_sucess.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent_sucess);
                finish();
            }else{
                intent_death.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent_death);
                finish();
            }



        }
    }

    //밥주기를 눌렀을 때
    public synchronized void onFoodClicked(View view){

        sound.play(soundId,1f,1f,0,0,1f);

        if(State.getInstance().getStateHunger() <= 80 ){
            State.getInstance().setStateHunger(State.getInstance().getStateHunger()+20); // 밥먹어서 증가
            increaseExperience();
            setCustomToast(getApplicationContext(),"냠냠");
            new UpdateImage().execute(R.drawable.yummy);
        }
        else {
            State.getInstance().setStateHunger(100); // 밥먹어서 증가
            setCustomToast(getApplicationContext()," 배불러요.");
        }
        createActivity("debris");

        changeState();
    }

    public void createActivity(String thing){

        int button[] = {R.id.btActivity1,R.id.btActivity2,R.id.btActivity3,R.id.btActivity4,
                R.id.btActivity5,R.id.btActivity6,R.id.btActivity7,R.id.btActivity8,
                R.id.btActivity9,R.id.btActivity10,R.id.btActivity11,R.id.btActivity12,
                R.id.btActivity13,R.id.btActivity14,R.id.btActivity15,R.id.btActivity16,};
        int value = new Random().nextInt(16);
        int turn = 0;
        if(thing.equals("debris")){
            while(true){
                Button bt = (Button)findViewById(button[value]);
                if(bt.getVisibility() == View.INVISIBLE || turn == 16){
                    bt.setBackgroundResource(R.drawable.debris);
                    bt.setText("debris");
                    bt.setVisibility(View.VISIBLE);
                    break;
                }

                turn++;
                value++;
                if(value == 16) value = 0;

            }


        }else if(thing.equals("poop")){
            while(true){
                Button bt = (Button)findViewById(button[value]);
                if(bt.getVisibility() == View.INVISIBLE || turn == 16){
                    bt.setBackgroundResource(R.drawable.poop);
                    bt.setText("poop");
                    bt.setVisibility(View.VISIBLE);
                    break;
                }

                turn++;
                value++;
                if(value == 16) value = 0;

            }
        }
    }

    //쓰담기 눌렀을 때
    public void onHandClicked(View v){

        sound.play(soundId,1f,1f,0,0,1f);


        if(State.getInstance().getStateStress() >= 10 ){
            State.getInstance().setStateStress(State.getInstance().getStateStress()-10);
            setCustomToast(getApplicationContext(),"쓰담기 좋아!");
            increaseExperience();
            new UpdateImage().execute(R.drawable.wavehand);
        }
        else {
            State.getInstance().setStateStress(0);
            setCustomToast(getApplicationContext(),"좋아좋아");
        }

        changeState();


    }

    //activity 눌렀을 때
    public void onActivityClicked(View v){
        Button button = (Button)findViewById(v.getId());

        button.setVisibility(View.INVISIBLE);

        if(button.getText().equals("debris")){ // 부스러기일 때
            if(State.getInstance().getStateHealth() <= 70) {
                State.getInstance().setStateHealth(State.getInstance().getStateHealth() + 30); //청소를 해주어 HELTH가 증가
                increaseExperience();
            }
            else
                State.getInstance().setStateHealth(100);

            if(State.getInstance().getDegreeStress() > 1)
                State.getInstance().setDegreeStress(State.getInstance().getDegreeStress()-1); //청소를 해주어 스트레스 감소정도가 감소


        }else if(button.getText().equals("poop")){ // 똥일 때
            if(State.getInstance().getStateHealth() <= 90) {
                State.getInstance().setStateHealth(State.getInstance().getStateHealth() + 10); //청소를 해주어 HELTH가 증가
                increaseExperience();
            }else
                State.getInstance().setStateHealth(100);

        }else{

        }

        button.setText("0");

        changeState();


    }

    //간식주기 - 행복증가
    public void onSnackClicked(View v){

        sound.play(soundId,1f,1f,0,0,1f);


        if(State.getInstance().getStateHappiness() <= 70){
            State.getInstance().setStateHappiness(State.getInstance().getStateHappiness()+30);
            increaseExperience();
            setCustomToast(getApplicationContext(),"간식 좋아♥");
            new UpdateImage().execute(R.drawable.snacklove);
        }
        else {
            State.getInstance().setStateHappiness(100);
            setCustomToast(getApplicationContext(),"간식 이렇게 많이 먹어도 되나요?");
        }
        changeState();


    }

    //이미지 변경하기
    private class UpdateImage extends AsyncTask<Integer,Integer, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {

            //gif으로
            publishProgress(1,integers[0]);

            try{
                Thread.sleep(3000);
            }catch (Exception e){
                e.printStackTrace();
            }

            //원상태로
            publishProgress(2);

            return null;
        }

        @Override
        protected synchronized void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if(values[0] == 1){
                GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(produce);
                Glide.with(getApplicationContext()).load(values[1]).into(gifImage);

            }else{
                if (charNum.equals("1")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop1);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop1_2);
                    else
                        produce.setImageResource(R.drawable.crop1_3);
                } else if (charNum.equals("2")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop2);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop2_2);
                    else
                        produce.setImageResource(R.drawable.crop2_3);
                } else if (charNum.equals("3")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop3);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop3_2);
                    else
                        produce.setImageResource(R.drawable.crop3_3);
                }

            }
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //경험치 올리기
    public void increaseExperience(){

        int increaseExperience = 25;

        State.getInstance().setStateExperience(State.getInstance().getStateExperience()+increaseExperience); //경험치증가
        if(State.getInstance().getStateExperience() == 100 || State.getInstance().getStateExperience() == 200 ){
            if (charNum.equals("1")) {
                if(State.getInstance().getStateExperience() < 100)
                    produce.setImageResource(R.drawable.crop1);
                else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                    produce.setImageResource(R.drawable.crop1_2);
                else
                    produce.setImageResource(R.drawable.crop1_3);
            } else if (charNum.equals("2")) {
                if(State.getInstance().getStateExperience() < 100)
                    produce.setImageResource(R.drawable.crop2);
                else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                    produce.setImageResource(R.drawable.crop2_2);
                else
                    produce.setImageResource(R.drawable.crop2_3);
            } else if (charNum.equals("3")) {
                if(State.getInstance().getStateExperience() < 100)
                    produce.setImageResource(R.drawable.crop3);
                else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                    produce.setImageResource(R.drawable.crop3_2);
                else
                    produce.setImageResource(R.drawable.crop3_3);
            }
        }else{return;}
        if(State.getInstance().getStateExperience() == 100 || State.getInstance().getStateExperience() == 200){
            new UpdateImage().execute(R.drawable.levelup);
            State.getInstance().setStateExperience(State.getInstance().getStateExperience()+increaseExperience);
        }

    }
    //상태변경하기
    public void changeState(){
        barHunger.setProgress(State.getInstance().getStateHunger());
        barActive.setProgress(State.getInstance().getStateActive());
        barHappiness.setProgress(State.getInstance().getStateHappiness());
        barHealth.setProgress(State.getInstance().getStateHealth());
        barStress.setProgress(State.getInstance().getStateStress());
        barExperience.setProgress(State.getInstance().getStateExperience()%100);
    }

    //커스텀 토스트메시지
    public static void setCustomToast(Context context, String msg) {

        TextView tvToastMsg = new TextView(context);

        Point size = new Point();
        display.getSize(size);

        tvToastMsg.setText("  "+msg+"  ");
        tvToastMsg.setTextSize(20);
        tvToastMsg.setTypeface(typeface);
        tvToastMsg.setTextColor(Color.DKGRAY);
        tvToastMsg.setBackgroundResource(R.drawable.toastcustom);


        final Toast toastMsg = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toastMsg.setView(tvToastMsg);
        toastMsg.setGravity(Gravity.CENTER,0,0-(size.x/3));

        toastMsg.show();

    }



    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Global.getInstance().getGlobal_u());
                jsonObject.accumulate("hunger", State.getInstance().getStateHunger());
                jsonObject.accumulate("happiness", State.getInstance().getStateHappiness());
                jsonObject.accumulate("health", State.getInstance().getStateHealth());
                jsonObject.accumulate("active", State.getInstance().getStateActive());
                jsonObject.accumulate("stress", State.getInstance().getStateStress());
                jsonObject.accumulate("experience", State.getInstance().getStateExperience());
                jsonObject.accumulate("end_time", State.getInstance().getEndTime());


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

            if(result.equals("save")){
                //상태 저장
            } else if(result.equals("delete")){
                //캐릭터 삭제
            } else {
                StringTokenizer st = new StringTokenizer(result, ",");
                String num = st.nextToken();
                String Name = st.nextToken();

                charNum = num;
                Log.d("*****,","**********"+charNum);

                //id와 레벨 사이로
                if (num.equals("1")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop1);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop1_2);
                    else
                        produce.setImageResource(R.drawable.crop1_3);
                } else if (num.equals("2")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop2);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop2_2);
                    else
                        produce.setImageResource(R.drawable.crop2_3);
                } else if (num.equals("3")) {
                    if(State.getInstance().getStateExperience() < 100)
                        produce.setImageResource(R.drawable.crop3);
                    else if(State.getInstance().getStateExperience() >= 100 && State.getInstance().getStateExperience() < 200)
                        produce.setImageResource(R.drawable.crop3_2);
                    else
                        produce.setImageResource(R.drawable.crop3_3);
                }
                name.setText(Name);
            }
        }
    }
}
