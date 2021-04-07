package kangwon.cs.capstone;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class Sucess extends AppCompatActivity {

    ImageView imageView;
    ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sucess);

        imageView = (ImageView) findViewById(R.id.firework);
        imageView2 = (ImageView) findViewById(R.id.firework2);
        new UpdateImage().execute(R.drawable.firework);

    }

    //이미지 변경하기
    private class UpdateImage extends AsyncTask<Integer,Integer, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {

            //gif으로
            publishProgress(1,integers[0]);

//            try{
//                Thread.sleep(3000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//            //원상태로
//            publishProgress(2);

            return null;
        }

        @Override
        protected synchronized void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1) {
                GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(imageView);
                Glide.with(getApplicationContext()).load(values[1]).into(gifImage);
                GlideDrawableImageViewTarget gifImage2 = new GlideDrawableImageViewTarget(imageView2);
                Glide.with(getApplicationContext()).load(values[1]).into(gifImage2);

            }

        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void onBackClicked(View v){

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
