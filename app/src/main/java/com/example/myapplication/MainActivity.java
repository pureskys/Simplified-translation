package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private String from = "auto";    // ************************************************************************************************************
//*******************************************************************************************************************
    //        Handler系统区域
    Handler handler = new Handler(Looper.myLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                String body = msg.obj.toString();
                TextView textView = findViewById(R.id.YoudaofanyiText);
                if (body.length() < 200) {
                    textView.setText("( ๑ŏ ﹏ ŏ๑ )出错啦！！");
                    Log.d("httptext", "有道翻译结果:" + body);
                } else {
                    String body1 = body.substring(body.indexOf("译文"));
                    String body2 = body1.substring(body1.indexOf("<ul id=\"translateResult\">"), body1.indexOf("</ul>"));
                    String body3 = body2.substring(body2.indexOf("<li>") + 4, body2.indexOf("</li>"));
                    Log.d("httptext", "有道翻译结果:" + body3);
                    textView.setText(body3);
                }

            }
            if (msg.what == 1) {
                String response = msg.obj.toString();
                TextView baidufanyitext = findViewById(R.id.BaiufanyiText);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    int changdu = jsonObject.length();
                    if (changdu <= 2) {
                        Log.d("httptext", "百度翻译结果:" + "出错啦");
                        baidufanyitext.setText("( ๑ŏ ﹏ ŏ๑ )出错啦！！");
                    } else {
                        JSONArray trans_result = jsonObject.getJSONArray("trans_result");
                        JSONObject obj = (JSONObject) trans_result.get(0);
                        String dst = obj.getString("dst");
                        baidufanyitext.setText(dst);
                        Log.d("httptext", "百度翻译结果:" + dst);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (msg.what == 2) {
                String response = msg.obj.toString();
                TextView meiriyiyan = findViewById(R.id.meiriyiyan);
                TextView origin_text = findViewById(R.id.origin);
                try {
                    JSONObject object = new JSONObject(response);
                    JSONObject data = object.getJSONObject("data");
                    String origin = data.getString("origin");
                    String content = data.getString("content");
                    meiriyiyan.setText("“" + content + "”");
                    origin_text.setText("————" + "《" + origin + "》");
                    Log.d("httptext", "每日一言:来源：" + origin + "，内容：" + content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //设置按钮可以点击
            if (msg.what == 3) {
                fanyi.setEnabled(true);
                anniu_dianji = true;
                handler.removeCallbacksAndMessages(null);
            }

        }
    };
    private String to = "auto";
    private String zhuanhuanleixing = "AUTO";
    private Boolean anniu_dianji = true;
    private TextView fanyi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //隐藏键盘1
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
//        点击事件绑定区
        fanyi = findViewById(R.id.fanyi_anniu);
        fanyi.setOnClickListener(view ->
                {
                    if (anniu_dianji) {
                        //设置按钮不可被点击开始
                        fanyi.setEnabled(false);
                        anniu_dianji = false;
                        //设置按钮不可被点击结束
                        youdaofanyi();
                        baidufanyi();
                        meiriyiyan();
                        //延时1.3秒设置按钮
                        handler.sendEmptyMessageDelayed(3, 1300);

                    }
                    //隐藏键盘2
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
        );


//        页面加载运行函数
        new Thread(new Runnable() {
            @Override
            public void run() {
                int cishu = Animation.INFINITE;
                int sudu = 3500;
                int zujian = R.id.tupian1;
                tupianXuanzhuan(cishu,sudu,zujian);
            }
        }).start();
        meiriyiyan();
    }

    //每日一言
    private void meiriyiyan() {
        String meiri_yiyan = "https://api.xygeng.cn/one";
        Request request = new Request.Builder()
                .url(meiri_yiyan)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    Message message = new Message();
                    message.what = 2;
                    message.obj = Objects.requireNonNull(response.body()).string();
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //    百度翻译方法
    private void baidufanyi() {
        TextView shurukuang = findViewById(R.id.shurukuang);
        String q = shurukuang.getText().toString();
        String q1 = q.trim();
        if (q1.length() <= 0) {
            fanyi.setEnabled(true);
            anniu_dianji = true;
        } else {
            String appid = "20210213000697057";
            String salt = "1435660288";
            String key = "EZ103WWUygv1ctLtU1d_";
            String url = "https://fanyi-api.baidu.com/api/trans/vip/translate?";
            String pingjie = appid + q1 + salt + key;
            String sign = "";
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(pingjie.getBytes());
                String qianming = new BigInteger(1, messageDigest.digest()).toString(16);
                sign = qianming;
                Log.d("httptext", "签名数据:" + qianming);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            String geturl = url + "q=" + q + "&" + "from=" + from + "&" + "to=" + to + "&" + "appid=" + appid + "&" + "salt=" + salt + "&" + "sign=" + sign;
            Log.d("httptext", "百度翻译请求地址：" + geturl);
            Request request = new Request.Builder()
                    .url(geturl)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        Message message = new Message();
                        message.what = 1;
                        message.obj = Objects.requireNonNull(response.body()).string();
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    //    有道翻译方法
    private void youdaofanyi() {
        TextView shurukuang = findViewById(R.id.shurukuang);
        String fanyineirong = shurukuang.getText().toString();
        String fanyineirong1 = fanyineirong.trim();
        if (fanyineirong1.length() > 0) {
            String geturl = "https://m.youdao.com/translate";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FormBody formBody = new FormBody.Builder()
                            .add("inputtext", fanyineirong1)
                            .add("type", zhuanhuanleixing)
                            .build();
                    Request request = new Request.Builder()
                            .url(geturl)
                            .post(formBody)
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d("TAG", "onFailure: " + e);

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String body = Objects.requireNonNull(response.body()).string();
                            Message message = new Message();
                            message.what = 0;
                            message.obj = body;
                            handler.sendMessage(message);
                        }


                    });

                }
            }).start();
        } else {
            Toast.makeText(MainActivity.this, "还没有输入文本哦！", Toast.LENGTH_SHORT).show();
        }


    }

    //图片旋转方法
    public void tupianXuanzhuan(int cishu,int sudu,int zujian) {
        ImageView tupian1 = findViewById(zujian);
        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(sudu);
        animation.setRepeatCount(cishu);
        animation.setRepeatMode(Animation.RESTART);
        tupian1.startAnimation(animation);

    }

    //Unicode转换为中文
    public String UnicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");

        Matcher matcher = pattern.matcher(str);

        char ch;

        while (matcher.find()) {

            ch = (char) Integer.parseInt(Objects.requireNonNull(matcher.group(2)), 16);

            str = str.replace(Objects.requireNonNull(matcher.group(1)), ch + "");

        }

        return str;

    }

    //翻译类型点击事件
    public void leixing(View view) {
        TextView fanyileixing = findViewById(R.id.fanyi_leixing0);
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.fanyi_leixing);
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.auto:
                        fanyileixing.setText("自动检测");
                        zhuanhuanleixing = "AUTO";
                        from = "auto";
                        to = "auto";
                        break;
                    case R.id.ZH_CN2EN:
                        fanyileixing.setText("中文翻译英文");
                        zhuanhuanleixing = "ZH_CN2EN";
                        from = "zh";
                        to = "en";
                        break;
                    case R.id.ZH_CN2JA:
                        fanyileixing.setText("中文翻译日文");
                        zhuanhuanleixing = "ZH_CN2JA";
                        from = "zh";
                        to = "jp";
                        break;
                    case R.id.ZH_CN2KR:
                        fanyileixing.setText("中文翻译韩语");
                        zhuanhuanleixing = "ZH_CN2KR";
                        from = "zh";
                        to = "kor";
                        break;
                    case R.id.ZH_CNFR:
                        fanyileixing.setText("中文翻译法语");
                        zhuanhuanleixing = "ZH_CN2FR";
                        from = "zh";
                        to = "fra";
                        break;
                    case R.id.ZH_CN2RU:
                        fanyileixing.setText("中文翻译俄语");
                        zhuanhuanleixing = "ZH_CN2RU";
                        from = "zh";
                        to = "ru";
                        break;
                    case R.id.ZH_CN2SP:
                        fanyileixing.setText("中文翻译西班牙语");
                        zhuanhuanleixing = "ZH_CN2SP";
                        from = "zh";
                        to = "spa";
                        break;
                    case R.id.EN2ZH_CN:
                        fanyileixing.setText("英语翻译为中文");
                        zhuanhuanleixing = "EN2ZH_CN";
                        from = "en";
                        to = "zh";
                        break;
                    case R.id.JA2ZH_CN:
                        fanyileixing.setText("日语翻译为中文");
                        zhuanhuanleixing = "JA2ZH_CN";
                        from = "jp";
                        to = "zh";
                        break;
                    case R.id.KR2ZH_CN:
                        fanyileixing.setText("韩语翻译为中文");
                        zhuanhuanleixing = "KR2ZH_CN";
                        from = "kor";
                        to = "zh";
                        break;
                    case R.id.FR2ZH_CN:
                        fanyileixing.setText("法语翻译为中文");
                        zhuanhuanleixing = "FR2ZH_CN";
                        from = "fra";
                        to = "zh";
                        break;
                    case R.id.RU2ZH_CN:
                        fanyileixing.setText("俄语翻译为中文");
                        zhuanhuanleixing = "RU2ZH_CN";
                        from = "ru";
                        to = "zh";
                        break;
                    case R.id.SP2ZH_CN:
                        fanyileixing.setText("西班牙语翻译为中文");
                        zhuanhuanleixing = "SP2ZH_CN";
                        from = "spa";
                        to = "zh";
                        break;


                    default:
                        Toast.makeText(MainActivity.this, "系统错误", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    //跳转二级翻译页面
    public void fanyi_erjiyemian(View view) {

    }

    public void meiriyiyan_genxing(View view) {
        meiriyiyan();
        tupianXuanzhuan(0,800,R.id.tupian2);
    }


}