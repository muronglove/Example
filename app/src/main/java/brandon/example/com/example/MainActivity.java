package brandon.example.com.example;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import brandon.example.com.example.helper.BookInfo;
import brandon.example.com.example.helper.DouBanBookInfoXmlParser;
import cn.qqtheme.framework.picker.DateTimePicker;


public class MainActivity extends Activity {
    private TextView sender;
    private TextView content;
    private IntentFilter receiveFilter;
    private MessageReceiver messageReceiver;
    private IntentFilter sendFilter;
    private SendStatusReceiver sendStatusReceiver;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sender = (TextView)findViewById(R.id.sender);
        content = (TextView)findViewById(R.id.content);
        receiveFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(1000);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver,receiveFilter);

//        sendFilter = new IntentFilter("SENT_SMS_ACTION");
//        sendStatusReceiver = new SendStatusReceiver();
//        registerReceiver(sendStatusReceiver,sendFilter);
        Button btn = (Button)findViewById(R.id.jump);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DressArticleActivity.class);
                try{startActivity(intent);}catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Button timeline_jump = (Button)findViewById(R.id.timeline_jump);
        timeline_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TimelineActivity.class);
                startActivity(intent);
            }
        });

        Button barcode_jump = (Button)findViewById(R.id.barcode_jump);
        barcode_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(MainActivity.this)
                        .setCaptureActivity(ScanActivity.class)
                        .setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)// 扫码的类型,可选：一维码，二维码，一/二维码
                        .setPrompt("请对准二维码")// 设置提示语
                        .setCameraId(0)// 选择摄像头,可使用前置或者后置
                        .setBeepEnabled(false)// 是否开启声音,扫完码之后会"哔"的一声
                        .setBarcodeImageEnabled(true)// 扫完码之后生成二维码的图片
                        .initiateScan();// 初始化扫码
            }
        });

        Button take_photo = (Button)findViewById(R.id.take_photo);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TakePhotoActivity.class);
                startActivity(intent);
            }
        });

        Button btn_webview = (Button)findViewById(R.id.btn_webview);
        btn_webview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
                startActivity(intent);
            }
        });

        Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
        t.setToNow(); // 取得系统时间。
        int year = t.year;
        int month = t.month+1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        //int second = t.second;

        final DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_24);//24小时值
        picker.setDateRangeStart(year, month, date);//日期起点
        picker.setDateRangeEnd(2020, 1,1);//日期终点
        picker.setTimeRangeStart(hour, minute);//时间范围起点
        picker.setTimeRangeEnd(23, 59);//时间范围终点
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                //year:年，month:月，day:日，hour:时，minute:分
                Toast.makeText(getApplicationContext(), year + "-" + month + "-" + day + " "
                        + hour + ":" + minute, Toast.LENGTH_LONG).show();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try{
                    Date d1  = df.parse(year+"-"+month+"-"+day+" "+hour+":"+minute+":00");
                    Date d2 = new Date(System.currentTimeMillis());
                    long diff = d1.getTime()-d2.getTime();
                    Intent intent = new Intent(MainActivity.this,AlarmReceiver.class);
                    intent.setAction("VIDEO_TIMER");
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
                    AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+diff,pendingIntent);
                }catch (Exception e){
                    e.printStackTrace();
                }



            }
        });
        Button btn_datereminder = (Button)findViewById(R.id.btn_datereminder);
        btn_datereminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                new GetBookTask().execute(result.getContents());

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(sendStatusReceiver);
    }

    class MessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            try{
                Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[])bundle.get("pdus");
            String format = intent.getStringExtra("format");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for(int i=0;i<pdus.length;i++){
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i],format);
            }
            String address = messages[0].getOriginatingAddress();
            String fullMessage = "";
            for(SmsMessage message:messages){
                fullMessage += message.getMessageBody();
            }
            sender.setText(address);
            content.setText(fullMessage);

//            SmsManager smsManager = SmsManager.getDefault();
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,new Intent("SENT_SMS_ACTION"),0);
//            smsManager.sendTextMessage(sender.getText().toString(),null,content.getText().toString(),pendingIntent,null);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class SendStatusReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode()==RESULT_OK){
                Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this,"not ok",Toast.LENGTH_LONG).show();
            }
        }
    }


    class GetBookTask extends AsyncTask<String,Integer,BookInfo>{
        @Override
        protected BookInfo doInBackground(String... params) {
            DouBanBookInfoXmlParser parser = new DouBanBookInfoXmlParser();
            BookInfo  info = null;
            try{
                 info = parser.fetchBookInfoByXML(params[0]);
            }catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }

        @Override
        protected void onPostExecute(BookInfo bookInfo) {
            super.onPostExecute(bookInfo);
            TextView textView = (TextView)findViewById(R.id.barcode_result);
            textView.setText(bookInfo.getAuthor()+" "+bookInfo.getPrice());
        }
    }

}
