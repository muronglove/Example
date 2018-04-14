package brandon.example.com.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Brandon on 2018/3/19.
 */

public class DressArticleActivity extends Activity {
    private List<Article> articleList = new ArrayList<>();
    private Document doc;
    private List<Map<String,Object>> result;
    private ListView list;
    private Handler handler = new Handler() {
       public void handleMessage(Message msg){
           try{
               switch (msg.what){
                   case 0x9527:
                       SimpleAdapter adapt=new SimpleAdapter(DressArticleActivity.this,result,R.layout.dress_article_item,new String[]{"image","caption","time","detail","tag"},
                               new int[]{R.id.dress_article_image,R.id.dress_article_caption,R.id.dress_article_time,R.id.dress_article_details,R.id.dress_article_tag});
                       adapt.setViewBinder(new SimpleAdapter.ViewBinder(){

                           @Override
                           public boolean setViewValue(View view, Object data,
                                                       String textRepresentation) {
                               if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                                   ImageView iv = (ImageView) view;
                                   Bitmap bm = (Bitmap) data;
                                   iv.setImageBitmap(bm);
                                   return true;
                               }
                               return false;

                           }

                       });


                       list.setAdapter(adapt);
                       break;
                   default:
                       break;
               }
           }catch (Exception e){
               e.printStackTrace();
           }
       }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dress_article);
            list = (ListView)findViewById(R.id.activity_dress_article_list);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String data = articleList.get(position).article;
                    Intent intent = new Intent(DressArticleActivity.this,DressArticleDetailActivity.class);
                    intent.putExtra("article",data);
                    startActivity(intent);

                }
            });
            Thread myThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    InitArticleList();
                    result = getListData(articleList);
                    Message msg = new Message();
                    msg.what = 0x9527;
                    handler.sendMessage(msg);
                }
            };
            myThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    class Article{
        public String image;
        public  String caption;
        public String time;
        public String detail;
        public String tag;
        public String article;
        public Article(String image,String caption,String time,String detail,String tag,String article){
            this.image = image;
            this.caption = caption;
            this.time = time;
            this.detail = detail;
            this.tag = tag;
            this.article = article;
        }
    }
    private void InitArticleList(){
        try{
            doc = Jsoup.connect("http://dress.pclady.com.cn/style/").get();
            Elements iPic = doc.select(".iPic");
            Elements eTit = doc.select(".eTit");
            Elements eTime = doc.select(".eTime");
            Elements sDes = doc.select(".sDes");
            Elements sLab = doc.select(".sLab");
            //Log.i("Dress",listItems.html());
            for(int i=0;i< iPic.size();i++){
                String image = iPic.get(i).child(0).child(0).attr("src");
                String caption =eTit.get(i).text();
                String time = eTime.get(i).text();
                String detail = sDes.get(i).text();
                String tag = sLab.get(i).text();
                String article = eTit.get(i).child(0).attr("href");
                Article articleItem = new Article(image,caption,time,detail,tag,article);
                articleList.add(articleItem);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private List<Map<String,Object>> getListData(List<Article> articleList){
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        for(Article article:articleList){
            Map<String, Object> map = new HashMap<String, Object>();
            try{
                Bitmap bmp = null;
                //Log.i("Dress",article.image);
                URL url=new URL(article.image);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                //从InputStream流中解析出图片
                bmp = BitmapFactory.decodeStream(is);
                is.close();
                map.put("image",bmp);
                map.put("caption",article.caption);
                map.put("time",article.time);
                map.put("detail",article.detail);
                map.put("tag",article.tag);
                map.put("article",article.article);
                result.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }



}
