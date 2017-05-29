package com.google.mobilesafe.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author created by Miky-PC
 * @date 2017/5/29 15:32
 * @desc
 * @editor edited by
 * @edit_date
 * @desc
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        init();
    }

    private void init() {
        initView();
        initData();
        initListener();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();

    /**
     * 跳转后结束本页面
     * @param clazz 需要跳转的activity类
     */
    public void startActivityWithFinish(Class clazz) {
        Intent intent = new Intent(getBaseContext(), clazz);
        startActivity(intent);
        finish();
    }

    /**
     * 跳转后不结束本页面
     * @param clazz 需要跳转的activity类
     */
    public void startActivityWithoutFinish(Class clazz) {
        Intent intent = new Intent(getBaseContext(), clazz);
        startActivity(intent);
    }
}
