package kangwon.cs.capstone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    EditText user;
    String Name, chk_name;
    CheckBox checkBox;
    Boolean chk;

    private static MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = (EditText) findViewById(R.id.name);

        checkBox = (CheckBox) findViewById(R.id.chk);
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        chk_name = pref.getString("chk_name", "");
        chk = pref.getBoolean("chk", false);

        user.setText(chk_name);
        checkBox.setChecked(chk);

        mp = MediaPlayer.create(this, R.raw.main_bgm);
        mp.setLooping(true);
        mp.start();
    }

    public void CheckBoxClicked(View v) {
        boolean isChecked = ((CheckBox) v).isChecked();

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        user = (EditText) findViewById(R.id.name);
        CheckBox chk = (CheckBox) findViewById(R.id.chk);

        if (isChecked) {
            Toast.makeText(getApplicationContext(), "저장", Toast.LENGTH_SHORT).show();

            editor.putString("chk_name", user.getText().toString());
            editor.putBoolean("chk", checkBox.isChecked());
            editor.commit();
        } else {
            Toast.makeText(getApplicationContext(), "해제", Toast.LENGTH_SHORT).show();
            editor.clear();
            editor.commit();
        }
    }

    public void onStart(View v) {
        Name = user.getText().toString();
        Global.getInstance().setGlobal_u(Name);

        if (Name == null || Name.equals("")) {
            Toast t = Toast.makeText(this, "캐릭터 생성", Toast.LENGTH_SHORT);
            t.show();

            Intent intent = new Intent(getApplicationContext(), Section3.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            new JSONTask().execute(Constants.IP_ADDRESS + "/main");//AsyncTask 시작시킴
        }
    }

    public void onHow(View v) {
        Intent intent = new Intent(getApplicationContext(), Section2.class);

        startActivity(intent);
    }

    public void onDestroy(){
        mp.stop();
        super.onDestroy();
    }

    public void onBackPressed(){
        mp.stop();
        super.onBackPressed();
    }

    public void onStop(){
        mp.stop();
        super.onStop();
    }

    /*
    public void onStart(){
        mp.start();
        super.onStart();
    }*/

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Global.getInstance().getGlobal_u());

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
                    return buffer.toString();//서버로 부터 받은 값을 리턴

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

            if (result != null) {
                if (result.equals("delete")) {
                    Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    user.setText("");
                } else if (result.equals("0")){
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    user.setText("");
                } else{
                    StringTokenizer st = new StringTokenizer(result, ",");
                    String Hunger = st.nextToken();
                    String Happiness = st.nextToken();
                    String Health = st.nextToken();
                    String Active = st.nextToken();
                    String Stress = st.nextToken();
                    String Experience = st.nextToken();
                    String EndTime = st.nextToken();

                    State.getInstance().setStateHunger(Integer.parseInt(Hunger));
                    State.getInstance().setStateHappiness(Integer.parseInt(Happiness));
                    State.getInstance().setStateHealth(Integer.parseInt(Health));
                    State.getInstance().setStateActive(Integer.parseInt(Active));
                    State.getInstance().setStateStress(Integer.parseInt(Stress));
                    State.getInstance().setStateExperience(Integer.parseInt(Experience));
                    State.getInstance().setEndTime(Long.parseLong(EndTime));

                    SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    editor.putInt("stateHunger",Integer.parseInt(Hunger));
                    editor.putInt("stateHappiness",Integer.parseInt(Happiness));
                    editor.putInt("stateHealth",Integer.parseInt(Health));
                    editor.putInt("stateActive",Integer.parseInt(Active));
                    editor.putInt("stateStress",Integer.parseInt(Stress));
                    editor.putInt("stateExperience",Integer.parseInt(Experience));

                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), Section4.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                }
            }
        }
    }
}
