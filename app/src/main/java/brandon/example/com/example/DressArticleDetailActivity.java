package brandon.example.com.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 2018/3/19.
 */

public class DressArticleDetailActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("Article","0");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        Log.i("Article","1");
        Intent intent = getIntent();
        Log.i("Article","2");
        String data = intent.getStringExtra("article");
        Log.i("Article","3");
        Log.i("Article",data);
        try{
            new MyTask().execute(data);
            Log.i("Article","4");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class MyTask extends AsyncTask<String,Integer,List<Object>> {
        @Override
        protected List<Object> doInBackground(String... params) {
            List<Object> objlist = new ArrayList<>();
            try{
                Document doc = Jsoup.connect(params[0]).get();
                doc = Jsoup.parse(doc.toString().replace("&nbsp;", ""));
                objlist.add(doc.select("h1").text());
                objlist.add(doc.select(".artInfo").text());
                objlist.add(doc.select(".artSum").text());
                Elements result = doc.select("p");
                for(Element pItem : result){
                    Log.i("Detail",""+pItem.childNodeSize());
                    Log.i("Detail",""+pItem.text());

                    if((pItem.childNodeSize()!=0)&&(pItem.hasAttr("style"))&&(pItem.child(0).is("img"))){
                        Bitmap bmp = null;
                        //Log.i("Dress",article.image);
                        URL url=new URL(pItem.child(0).attr("src"));
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        //从InputStream流中解析出图片
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                        objlist.add(bmp);
                    }else{
                        objlist.add(pItem.text());
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return objlist;
        }

        @Override
        protected void onPostExecute(List<Object> objlist) {
            super.onPostExecute(objlist);
            try{

                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.dress_detail_linearlayout);
                for(Object obj:objlist){
                    if(obj instanceof Bitmap){
                        ImageView imageView = new ImageView(DressArticleDetailActivity.this);
                        imageView.setImageBitmap((Bitmap)obj);
                        linearLayout.addView(imageView);
                    }else{
                        TextView textView = new TextView(DressArticleDetailActivity.this);
                        textView.setText((String)obj);
                        linearLayout.addView(textView);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
