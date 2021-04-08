package kangwon.cs.capstone;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import android.widget.EditText;

public class Together extends AppCompatActivity {

    EditText other;
    String Other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_together);

        other = (EditText) findViewById(R.id.other);

        new JSONTask().execute(Constants.IP_ADDRESS + "/together");  //wait=4
    }

    @Override
    public void onPause() {
        super.onPause();

        new JSONTask().execute(Constants.IP_ADDRESS + "/main");  //wait=0
    }

    public void onSearch(View v){
        Other = other.getText().toString();
        Global.getInstance().setGlobal(Other);

        if(Other == null || Other.equals("")){
            Toast.makeText(this, "친구 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            new JSONTask().execute(Constants.IP_ADDRESS + "/other");
        }
    }

    public void onStart(View v){
        Other = other.getText().toString();
        Global.getInstance().setGlobal(Other);

        if(Other == null || Other.equals("")){
            Toast.makeText(this, "친구 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            new JSONTask().execute(Constants.IP_ADDRESS + "/go");
        }
    }

    public void onBack(View v){
        Intent intent = new Intent(getApplicationContext(), Game_menu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    public void onBackPressed(){
        //백키 막기
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //node.js로 data를 보냄, accumulate();
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", Global.getInstance().getGlobal_u());
                jsonObject.accumulate("other", Global.getInstance().getGlobal());

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
            } else if(result.equals("other")) {
                Toast.makeText(getApplicationContext(),"게임시작 버튼을 눌러 친구와 연결하세요!", Toast.LENGTH_SHORT).show();
            } else if(result.equals("no")) {
                Toast.makeText(getApplicationContext(),"친구 이름이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }else if(result.equals("go")) {
                Toast.makeText(getApplicationContext(),"만보기 게임으로!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), Together_Game.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish();
            }else if(result.equals("waiting")) {
                Toast.makeText(getApplicationContext(),"친구가 아직 같이놀기 상태가 아닙니다.", Toast.LENGTH_SHORT).show();
            }else {
                //
            }
        }
    }
}