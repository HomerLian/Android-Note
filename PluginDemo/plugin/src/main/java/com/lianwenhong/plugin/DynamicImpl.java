package com.lianwenhong.plugin;

import android.app.Activity;
import android.widget.Toast;

import com.lianwenhong.sdk.Dynamic;

public class DynamicImpl implements Dynamic {

    private Activity mActivity;

    @Override
    public void init(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void showBanner() {
        Toast.makeText(mActivity, "我是showBanner方法", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showDialog() {
        Toast.makeText(mActivity, "我是showDialog方法", Toast.LENGTH_LONG).show();
    }

    @Override
    public void destroy() {
        Toast.makeText(mActivity, "我是destroy方法", Toast.LENGTH_LONG).show();
    }
}