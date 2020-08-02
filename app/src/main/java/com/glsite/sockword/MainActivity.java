package com.glsite.sockword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.TimedText;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.assetsbasedata.AssetsDatabaseManager;
import com.glsite.greendao.entity.greendao.CET4EntityDao;
import com.glsite.greendao.entity.greendao.DaoMaster;
import com.glsite.greendao.entity.greendao.DaoSession;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;
import com.iflytek.cloud.speech.SynthesizerListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener, SynthesizerListener, RadioGroup.OnCheckedChangeListener {

    public DaoMaster mDaoMaster;
    public DaoSession mDaoSession;
    public String mMonth;
    public String mDay;
    public String mWay;
    public String mHour;
    public String mMinute;
    public String mMinute1;
    public TextView mTimetext;
    public TextView mDatetext;
    public ImageView mPlayVioce;
    public TextView mWordText;
    public RadioGroup mChooseGroup;
    public RadioButton mChooseOne;
    public RadioButton mChooseTwo;
    public RadioButton mChooseThree;
    public SpeechSynthesizer mSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        setParam();

    }

    private void init() {
        //初始化时间控件
        mTimetext = (TextView) findViewById(R.id.time_text);
        mDatetext = (TextView) findViewById(R.id.date_text);

        //获取播放喇叭控件
        mPlayVioce = (ImageView) findViewById(R.id.playVioce);
        //设置播放按钮，点击监听
        mPlayVioce.setOnClickListener(this);

        //初始化单词内容
        mWordText = (TextView) findViewById(R.id.word_text);

        //初始化选项组
        mChooseGroup = (RadioGroup) findViewById(R.id.choose_Group);
        //选项设置三个选项的点击监听
        mChooseGroup.setOnCheckedChangeListener(this);
        //初始化选项一
        mChooseOne = (RadioButton) findViewById(R.id.chooseOne);
        //初始化选项二
        mChooseTwo = (RadioButton) findViewById(R.id.chooseTwo);
        //初始化选项三
        mChooseThree = (RadioButton) findViewById(R.id.chooseThree);

        //初始化播放语音

        SpeechUser.getUser().login(this, null, null, "appid=5f1840b0", listener);


        //初始化轻量级数据库
        SharedPreferences sp = getSharedPreferences("share", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();//初始化轻量级数据库编辑器
        //给播放单词语音的设置appid
        ArrayList<Integer> list = new ArrayList<>();//初始化list
        /**
         * 添加十个20以内的随机数
         */
        Random r = new Random();
        int i;
        while (list.size() < 10) {
            i = r.nextInt(20);
            if (!list.contains(i)) {
                list.add(i);
            }
        }

        /**
         * 得到键盘锁管理对象
         */
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unlock");


        AssetsDatabaseManager.initManager(this);//初始化，只需要调用一次
        AssetsDatabaseManager manager = AssetsDatabaseManager.getManager();//获取管理对象，因为数据库需要通过管理对象才能够获取
        SQLiteDatabase database = manager.getDatabase("word.db");//通过管理对象获取数据库

        //对数据库进行操作
        mDaoMaster = new DaoMaster(database);
        mDaoSession = mDaoMaster.newSession();
        CET4EntityDao cet4EntityDao = mDaoSession.getCET4EntityDao();

        /**
         * 此DevOpenHelper类继承自SQLiteOpenHelper,
         * 第一个参数Context,第二个参数数据库名字，第三个参数CursorFactory
         */
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wrong.db", null);

        /**
         * 初始化数据库
         */
        SQLiteDatabase database1 = helper.getWritableDatabase();

    }

    private void setParam() {
        mSynthesizer = SpeechSynthesizer.createSynthesizer(this);
        mSynthesizer.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");
        mSynthesizer.setParameter(SpeechConstant.SPEED,"50");
        mSynthesizer.setParameter(SpeechConstant.VOLUME,"50");
        mSynthesizer.setParameter(SpeechConstant.PITCH,"50");
    }

    @Override
    protected void onStart() {//onstart方法开始
        super.onStart();
        /**
         * 获取系统日期，并将其显示出来
         */
        Calendar calendar = Calendar.getInstance();
        mMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        mDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        mWay = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));


        /**
         * 如果小时是小于10，则前边加0
         */
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            mHour = String.valueOf("0"+ calendar.get(Calendar.HOUR_OF_DAY));
        } else {
            mHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }
        /**
         * 如果分钟是小于10，则前边加0
         */
        if (calendar.get(Calendar.MINUTE) < 10) {
            mMinute = String.valueOf("0"+ calendar.get(Calendar.MINUTE));
        } else {
            mMinute = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        /**
         * 将星期数字换成汉语
         */
        if ("0".equals(mWay)) {
            mWay = "六";
        } else if ("1".equals(mWay)) {
            mWay = "日";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        }

        /**
         * 显示日期和时间
         */
        mTimetext.setText(mHour + ":" + mMinute);
        mDatetext.setText(mMonth + "月" + mDay + "日" + "  " + "星期" + mWay);


    }//onstart方法结束

    @Override
    public  void onClick(View v) {
        switch (v.getId()) {
            case R.id.playVioce:
                String text = mWordText.getText().toString();
                mSynthesizer.startSpeaking(text, this);
                break;
        }
    }

    @Override
    public void onSpeakBegin() {

    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {

    }

    /**
     * 通用回调接口
     */
    private SpeechListener listener = new SpeechListener() {
        @Override
        //消息回调
        public void onEvent(int i, Bundle bundle) {

        }

        //数据回调
        @Override
        public void onData(byte[] bytes) {

        }

        //结束回调
        @Override
        public void onCompleted(SpeechError speechError) {

        }
    };

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    /**
     * 初始化语音播报
     */

}
