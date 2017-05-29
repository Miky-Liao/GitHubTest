package com.google.mobilesafe.ui.activity;

import android.widget.GridView;
import android.widget.ImageView;

import com.google.mobilesafe.R;


/**
 * @author created by Miky
 * @date 2017/5/23 16:16
 * @desc
 * @editor edited by
 * @edit_date
 * @desc
 */

public class MainActivity extends BaseActivity {

    private ImageView mIvLogo;
    private ImageView mIvSetting;
    private GridView mGvConetent;

    @Override
    protected void initView() {
        mIvLogo = (ImageView) findViewById(R.id.iv_main_logo);
        mIvSetting = (ImageView) findViewById(R.id.iv_main_setting);
        mGvConetent = (GridView) findViewById(R.id.gv_main_content);
    }

    @Override
    protected void initData() {
        startAnim();
    }


    @Override
    protected void initListener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private void startAnim() {
    }


}
