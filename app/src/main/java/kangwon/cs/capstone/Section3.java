package kangwon.cs.capstone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

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

public class Section3 extends AppCompatActivity {

    EditText name;
    ToggleButton c1, c2, c3;
    String Name, img = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section3);

        name = (EditText) findViewById(R.id.name);
        c1 = (ToggleButton) findViewById(R.id.crop1);
        c2 = (ToggleButton) findViewById(R.id.crop2);
        c3 = (ToggleButton) findViewById(R.id.crop3);

        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c1.isChecked()) {
                    c1.setBackgroundDrawable(getResources().getDrawable(R.drawable.s_1));
                    c2.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_2));
                    c3.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_3));
                    c2.setChecked(false);
                    c3.setChecked(false);

                    img = "1";
                } else {
                    c1.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_1));
                    img = "0";
                }
            }
        });
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c2.isChecked()) {
                    c1.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_1));
                    c2.setBackgroundDrawable(getResources().getDrawable(R.drawable.s_2));
                    c3.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_3));
                    c1.setChecked(false);
                    c3.setChecked(false);

                    img = "2";
                } else {
                    c2.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_2));
                    img = "0";
                }
            }
        });
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c3.isChecked()) {
                    c1.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_1));
                    c2.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_2));
                    c3.setBackgroundDrawable(getResources().getDrawable(R.drawable.s_3));
                    c1.setChecked(false);
                    c2.setChecked(false);

                    img = "3";
                } else {
                    c3.setBackgroundDrawable(getResources().getDrawable(R.drawable.n_3));
                    img = "0";
                }
            }
        });
    }

    public void onCreate(View v) {
        Name = name.getText().toString();
        Global.getInstance().setGlobal_u(Name);

        if (Name == null || Name.equals("")) {
            Toast t = Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT);
            t.show();
        } else if (img.equals("0")) {
            Toast t = Toast.makeText(this, "작물을 선택하세요.", Toast.LENGTH_SHORT);
            t.show();
        }else{
            new JSONTask().execute(Constants.IP_ADDRESS + "/sec3");//AsyncTask 시작시킴
        }
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Name);
                jsonObject.accumulate("num", img);

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

            if(result.equals("0")){
                Toast.makeText(getApplicationContext(), "사용할 수 없는 이름입니다.", Toast.LENGTH_SHORT).show();
            }else if(result.equals("1")){
                Toast.makeText(getApplicationContext(), "Welcome! " + Name, Toast.LENGTH_SHORT).show();

                SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putInt("stateHunger",100);
                editor.putInt("stateHappiness",100);
                editor.putInt("stateHealth",100);
                editor.putInt("stateActive",100);
                editor.putInt("stateStress",0);
                editor.putInt("stateExperience",0);

                State.getInstance().setEndTime(0);

                editor.commit();

                Intent intent = new Intent(getApplicationContext(), Section4.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish();
            }
        }
    }
}
