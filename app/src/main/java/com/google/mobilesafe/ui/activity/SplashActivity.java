package com.google.mobilesafe.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mobilesafe.Contants;
import com.google.mobilesafe.R;
import com.google.mobilesafe.domain.VersionInfo;
import com.google.mobilesafe.utils.PackageUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private TextView mTvVersionName;
    private TextView mTvVersionCode;
    private RelativeLayout mRlRootView;
    private VersionInfo mInfo;
    private String mVersionName;
    private String mVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mTvVersionName = (TextView) findViewById(R.id.tv_version_name);
        mTvVersionCode = (TextView) findViewById(R.id.tv_version_code);
        mRlRootView = (RelativeLayout) findViewById(R.id.rl_splash_root_view);
    }

    private void initListener() {

    }

    private void initData() {
        // 获取到当前的版本号和版本名并更改开机动画时展示的版本号和版本名
        mVersionName = PackageUtil.getVersionName(getApplicationContext());
        mTvVersionName.setText(mVersionName);
        mVersionCode = PackageUtil.getVersionCode(getApplicationContext());
        mTvVersionCode.setText(mVersionCode);
        startAnimation();
    }

    private static final int MSG_SUCCESS = 0;
    private static final int MSG_ERROR = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:   // 网络请求成功
                    if (mInfo.getVersionCode().equals(mVersionCode)) {  // 比对版本号
                        Toast.makeText(SplashActivity.this, "版本号一致", Toast.LENGTH_SHORT).show();
                        loadMain();
                    } else {
                        Toast.makeText(SplashActivity.this, "版本号不一致", Toast.LENGTH_SHORT).show();
                        showUpdateDialog();
                    }
                    break;
                case MSG_ERROR:
                    switch (msg.arg1) {
                        case 1001:  // MalformedURLException
                            Toast.makeText(SplashActivity.this, "url错误", Toast.LENGTH_SHORT).show();
                            break;
                        case 1002:  // IOException
                            Toast.makeText(SplashActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                            break;
                        case 1003:  // JSONException
                            Toast.makeText(SplashActivity.this, "json解析错误", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SplashActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    loadMain();
                    break;
                default:
                    break;
            }
        }
    };

    private void startAnimation() {
        // 创建动画集
        AnimationSet animationSet = new AnimationSet(false);
        // 渐变动画
        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(2000);
        alphaAnim.setFillAfter(true);
        // 将渐变动画添加到动画集
        animationSet.addAnimation(alphaAnim);
        // 旋转动画
        RotateAnimation rotateAnim = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(2000);
        rotateAnim.setFillAfter(true);
        // 将旋转动画添加到动画集
        animationSet.addAnimation(rotateAnim);
        // 缩放动画
        ScaleAnimation scaleAnim = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(2000);
        scaleAnim.setFillAfter(true);
        // 将旋转动画添加到动画集
        animationSet.addAnimation(scaleAnim);
        // 开启动画
        mRlRootView.startAnimation(animationSet);
        // 监听动画
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                new Thread() {  // 开启一个线程去检测版本更新
                    @Override
                    public void run() {
                        checkVersion();
                    }
                }.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void checkVersion() {
        final Message msg = new Message();    // 设置消息
        msg.what = MSG_ERROR;
        long startTime = System.currentTimeMillis();
        // 获取网络请求
        try {
            URL url = new URL(Contants.URL_UPDATE_INFO);    // 设置要访问的网址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();    // 打开连接获取访问对象
            connection.setConnectTimeout(5000); // 设置超时时间
            connection.setRequestMethod("GET"); // 设置请求方式
            int responseCode = connection.getResponseCode();//获取响应码
            // 判断响应码
            if (responseCode == 200) {
                InputStream inputStream = connection.getInputStream();  //获取输入流
                String json = stream2String(inputStream);// 字节流转换为字符流
                // 解析Json并转换为VersionInfo对象返回
                mInfo = parserJson(json);
                msg.what = MSG_SUCCESS;
            } else {
                msg.arg1 = responseCode;
                System.out.println("失败");
            }
        } catch (MalformedURLException e) {
            msg.arg1 = 1001;
            e.printStackTrace();
        } catch (IOException e) {
            msg.arg1 = 1002;
            e.printStackTrace();
        } catch (JSONException e) {
            msg.arg1 = 1003;
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();  // 等动画结束后发送消息
            if ((endTime - startTime) < 2000) {
                SystemClock.sleep(2000 - (endTime - startTime)); // 动画没有结束，则等待动画结束为止
            }
            mHandler.sendMessage(msg);    // 发送消息
        }
    }

    /**
     * 弹窗提示用户更新
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this); // 利用Builder创建一个对话框
        builder.setTitle("提示"); // 对话框标题
        builder.setMessage(mInfo.getDesc());    // 对话框内容
        builder.setNegativeButton("暂时不", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadMain();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("马上更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadApk();  // 去下载安装包
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loadMain();
            }
        });
        builder.create().show();    // 创建完显示
    }


    /**
     * 下载Apk
     */
    private void downloadApk() {
        String fileName = mInfo.getDownloadUrl().substring(mInfo.getDownloadUrl().lastIndexOf("/") + 1);    // 获取文件名
        final File file = new File(Environment.getExternalStorageDirectory(), fileName);  // 根据文件名去SD卡拿取对应的安装包
        if (file.exists()) {    // 判断本地是否下载过
            Log.d(TAG, "存在");
            AskForInstallApk(file);   // 去安装
        } else {
            final ProgressDialog progressDialog = ProgressDialog.show(SplashActivity.this, "", "玩命下载中");// 显示下载框
            HttpUtils httpUtils = new HttpUtils(5000);  //在构造时设置超时时间
            httpUtils.download(mInfo.getDownloadUrl(), file.getAbsolutePath(), new RequestCallBack<File>() {    // 开启下载
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {    //下载成功
                    AskForInstallApk(file);   // 去安装
                    Log.d(TAG, "成功");
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(HttpException e, String s) {  // 下载失败
                    Log.d(TAG, mInfo.getDownloadUrl());
                    loadMain();
                    progressDialog.dismiss();
                }
            });
        }
    }

    /**
     * 弹出弹窗问用户是否安装Apk
     *
     * @param file 要安装的文件
     */
    private void AskForInstallApk(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setTitle("提示");
        builder.setMessage("是否安装");
        builder.setNegativeButton("暂时不", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadMain();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("安装", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*<intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <data android:scheme="content" />
                    <data android:scheme="file" />
                    <data android:mimeType="application/vnd.android.package-archive" />
                </intent-filter>*/
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                startActivityForResult(intent, 0);  //启动Activity需要知道返回返回码
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loadMain();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            loadMain();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 跳转到主界面
     */
    private void loadMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String stream2String(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private VersionInfo parserJson(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        VersionInfo info = new VersionInfo();
        info.setVersionName(object.getString("versionName"));
        info.setVersionCode(object.getString("versionCode"));
        info.setDesc(object.getString("desc"));
        info.setDownloadUrl(object.getString("downloadUrl"));
        return info;
    }

}
