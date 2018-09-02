package com.lxq.baidutts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.lxq.baidutts.control.InitConfig;
import com.lxq.baidutts.control.MySyntherizer;
import com.lxq.baidutts.control.NonBlockSyntherizer;
import com.lxq.baidutts.listener.FileSaveListener;
import com.lxq.baidutts.listener.UiMessageListener;
import com.lxq.baidutts.util.AutoCheck;
import com.lxq.baidutts.util.FileUtil;
import com.lxq.baidutts.util.OfflineResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SpeechSynthesizerListener {


    private TextView mTv;
    private EditText mEt;
    private Button mBut_player;
    private Button mBut_jxu;
    private Button mBut_stop;
    protected String appId = "11737343";

    protected String appKey = "Ms8tkkeiAt190C6ZGazOrqRR";

    protected String secretKey = "dmx3EndGYkANnYY4UQAQqWKuRNmutjlq";

    private static final String TAG = "MainActivity";

    private SpeechSynthesizer mSpeechSynthesizer;//百度语音合成客户端

    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license_2016-04-05";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
    private String content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     /*   init();
        initialTts();
        initPermission();   initdata();*/

        initialEnv();
        initialTts();
        initview();


    }

    /**
     * 初始化语音合成客户端并启动
     */
    private void initialTts() {
        //获取语音合成对象实例
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        //设置Context
        this.mSpeechSynthesizer.setContext(this);
        //设置语音合成状态监听
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        //文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        //声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        //本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，
        //仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了离线授权，
        //不需要设置该参数，建议将该行代码删除（离线引擎）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
        //请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(appId);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(appKey, secretKey);
        //发音人（在线引擎），可用参数为0,1,2,3。。。
        //（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "1");
        // 设置Mix模式的合成策略
        //  this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, SpeechSynthesizer.MIX_MODE_DEFAULT);


        // 获取语音合成授权信息
        AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            Log.e("--------", "授权成功");
            // Toast.makeText(this, "授权成功", Toast.LENGTH_LONG).show();
            mSpeechSynthesizer.initTts(TtsMode.MIX);
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
            mSpeechSynthesizer.speak("授权成功");
        } else {
            // 授权失败
            Log.e("--------", "授权失败");
            // Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onSynthesizeStart(String s) {
        //监听到合成开始
        Log.e("--onSynthesizeStart---", ">>>onSynthesizeStart()<<< s: " + s);
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        //监听到有合成数据到达
        Log.e("--onSynthesizeDat---", ">>>onSynthesizeDataArrived()<<< s: " + s);
    }

    @Override
    public void onSynthesizeFinish(String s) {
        //监听到合成结束
        Log.e("--onSynthesizeFinish---", ">>>onSynthesizeFinish()<<< s: " + s);
    }

    @Override
    public void onSpeechStart(String s) {
        //监听到合成并开始播放
        Log.e("--onSpeechStart---", ">>>onSpeechStart()<<< s: " + s);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        //监听到播放进度有变化
        Log.e("--oressChanged---", ">>>onSpeechProgressChanged()<<< s: " + s);
    }

    @Override
    public void onSpeechFinish(String s) {
        //监听到播放结束
        Log.e("--onSpeechFinish---", ">>>onSpeechFinish()<<< s: " + s);
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        //监听到出错
        Log.e("--onError---", ">>>onError()<<< description: " + speechError.description + ", code: " + speechError.code);
    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        File file = new File(mSampleDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    /**
     * 将工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    public void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        this.mSpeechSynthesizer.release();//释放资源
        super.onDestroy();
    }


    private void initview() {
        mTv = (TextView) findViewById(R.id.main_tv);
        mEt = (EditText) findViewById(R.id.main_et);
        content = mEt.getText().toString();
        mBut_player = (Button) findViewById(R.id.main_bt_player);
        mBut_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("--------", "已经走这里了");
                mSpeechSynthesizer.speak("我不曾爱过你，明明觉得自己很冷静亲爱的爸爸斯诺怎么也没有想到，那个最爱她的爸爸突然之间消失不见了，随着一起消失的，还有家里很大一笔数目的钱。表面看起来，爸爸的精神状态一直很好，虽然他和单位闹得很僵，一直离职在家，可是单位并没有少给他发工资，他也乐得自在，每天在家给老婆和女儿做饭，每天的餐桌，都是他施展才艺的舞台。除此之外，那个面积不大的两居室，被他充分发挥想象，利用一楼的优势向外围墙扩展了一间书房，并把房间与书房之间连接起来，那个美丽的通道，抬起头就可以透过明亮的玻璃看到蓝天和白云而这个通道两边，放满了爸爸自己做的大鱼缸，里面游来游去的，是各种美丽的鱼儿，那个时候，斯诺最喜欢坐在鱼缸旁边的沙发上看书，累了抬头看看天，如果刚好外面下着雨，听着头顶滴滴答答的雨滴声，身边放着的，是爸爸为她切好的水果，那种感觉，是斯诺一辈子最珍贵的记忆斯诺学习成绩不是很好，为了能让她有机会在这所重点中学借读，爸爸妈妈托了很多关系。但是排名的落后一直让她倍感失落和自卑。她不知道父母这样的做法到底对不对，但是她可以确定的是，她过得十分不开心。");
                Log.e("--------", ">>>say: " + content);
            }
        });
        mBut_jxu = (Button) findViewById(R.id.main_bt_Jxu);
        mBut_jxu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeechSynthesizer.pause();
            }
        });
        mBut_stop = (Button) findViewById(R.id.main_stop);
        mBut_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeechSynthesizer.resume();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_tv:


                break;
            case R.id.main_et:
                break;
            case R.id.main_bt_player:
                Log.e("---合成并播放-----", "已经走这里了");
                String content = mEt.getText().toString();
                mSpeechSynthesizer.speak(content);
                Log.e("----合成并播放----", ">>>say: " + mEt.getText().toString());
                //合成播放
                // speak(); // 合成并播放
                break;
            case R.id.main_bt_Jxu:
                // resume(); // 播放恢复

                //继续
                break;
            case R.id.main_stop:
                //暂停
                // pause(); // 播放暂停

                break;
            default:
                break;
        }
    }

    
    
/*
    // ================== 初始化参数设置开始 ==========================
    *//**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     *//*
  

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    // protected MainActivity synthesizer;

    protected static String DESC = "请先看完说明。之后点击“合成并播放”按钮即可正常测试。\n"
            + "测试离线合成功能需要首次联网。\n"
            + "纯在线请修改代码里ttsMode为TtsMode.ONLINE， 没有纯离线。\n"
            + "本Demo的默认参数设置为wifi情况下在线合成, 其它网络（包括4G）使用离线合成。 在线普通女声发音，离线男声发音.\n"
            + "合成可以多次调用，SDK内部有缓存队列，会依次完成。\n\n";

    private static final String TAG = "MainActivity";

    *//**
     * 初始化的其它参数，用于setParam
     *//*
    private Map<String, String> params;

    private TextView mTv;
    private EditText mEt;
    private Button mBut_player;
    private Button mBut_jxu;
    private Button mBut_stop;
    private MySyntherizer synthesizer;
    private InitConfig initConfig;
    protected SpeechSynthesizer mSpeechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       init();
        initialTts();
        initPermission();

        initview();
        initdata();

    }

    *//**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     *
     * @return
     *//*
    protected boolean init() {
        Log.e("----MainActivity-----", "初始化开始");

        //    sendToUiThread("初始化开始");
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);
        // mSpeechSynthesizer.setSpeechSynthesizerListener(config.getListener());
        mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {

            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

            }

            @Override
            public void onSynthesizeFinish(String s) {

            }

            @Override
            public void onSpeechStart(String s) {

            }

            @Override
            public void onSpeechProgressChanged(String s, int i) {

            }

            @Override
            public void onSpeechFinish(String s) {

            }

            @Override
            public void onError(String s, SpeechError speechError) {

            }
        });

        // 请替换为语音开发者平台上注册应用得到的App ID ,AppKey ，Secret Key ，填写在SynthActivity的开始位置
        mSpeechSynthesizer.setAppId(appId);
        mSpeechSynthesizer.setApiKey(appKey, secretKey);

        if (isMix) {

            // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。选择纯在线可以不必调用auth方法。
            AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
            if (!authInfo.isSuccess()) {
                // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
                String errorMsg = authInfo.getTtsError().getDetailMessage();
                Log.e("----MainActivity-----", "鉴权失败 =" + errorMsg);
                //sendToUiThread("鉴权失败 =" + errorMsg);
                return false;
            } else {
                Log.e("----MainActivity-----", "验证通过，离线正式授权文件存在。");
                //sendToUiThread("验证通过，离线正式授权文件存在。");
            }
        }

        setParams(params);
        // 初始化tts
        int result = mSpeechSynthesizer.initTts(ttsMode);
        if (result != 0) {
            Log.e("----MainActivity-----", "【error】initTts 初始化失败 + errorCode：" + result);
            //sendToUiThread("【error】initTts 初始化失败 + errorCode：" + result);
            return false;
        }
        // 此时可以调用 speak和synthesize方法
        Log.e("----MainActivity-----", "合成引擎初始化成功");
        //sendToUiThread(INIT_SUCCESS, "合成引擎初始化成功");
        return true;
    }

    public void setParams(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                mSpeechSynthesizer.setParam(e.getKey(), e.getValue());
            }
        }
    }


    *//**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     *//*

     *//**
     * 与SynthActivity相比，修改listener为FileSaveListener 可实现保存录音功能。
     * 获取的音频内容同speak方法播出的声音
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     *//*
    protected void initialTts() {
        Log.e("-----initialTts();-----","");
        String tmpDir = FileUtil.createTmpDir(this);
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new FileSaveListener(tmpDir);
        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        // 此处可以改为MySyntherizer 了解调用过程
        synthesizer = new MySyntherizer(this, initConfig);
        Log.e("-----initialTts();-----","end");
    }

    *//**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     *//*
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.e("---MainActivity--", "【error】:copy files from assets failed." + e.getMessage());
            //toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    *//**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     *//*
    private void speak() {
        //  mShowText.setText("");
        String text = mEt.getText().toString();
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(mEt.getText())) {
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
            mEt.setText(text);
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);

        checkResult(result, "speak");
    }


    private void initdata() {

    }

    private void initview() {
        mTv = (TextView) findViewById(R.id.main_tv);
        mEt = (EditText) findViewById(R.id.main_et);
        mBut_player = (Button) findViewById(R.id.main_bt_player);
        mBut_jxu = (Button) findViewById(R.id.main_bt_Jxu);
        mBut_stop = (Button) findViewById(R.id.main_stop);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_tv:
                break;
            case R.id.main_et:
                break;
            case R.id.main_bt_player:
                //合成播放
                speak(); // 合成并播放
                break;
            case R.id.main_bt_Jxu:
                resume(); // 播放恢复

                //继续
                break;
            case R.id.main_stop:
                //暂停
                pause(); // 播放暂停

                break;
            default:
                break;
        }
    }

    *//**
     * 暂停播放。仅调用speak后生效
     *//*
    private void pause() {
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    *//**
     * 继续播放。仅调用speak后生效，调用pause生效
     *//*
    private void resume() {
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    *//*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     *//*
    private void stop() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }

    *//**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     *//*
    private void loadModel(String mode) {
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        Log.e("---MainActivity--", "切换离线语音：" + offlineResource.getModelFilename());
        //   toPrint("切换离线语音：" + offlineResource.getModelFilename());
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.e("---MainActivity--", "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            //  toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }


    @Override
    protected void onDestroy() {
        synthesizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

    *//**
     * android 6.0 以上需要动态申请权限
     *//*
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }


    private void startAct(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }*/
}
