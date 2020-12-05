package com.lianwh.dynamic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import androidx.annotation.NonNull;

import com.lianwenhong.sdk.Dynamic;

import java.io.File;

import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

    public static final String TAG = "lianwenhong";

    private Button btn_init;
    private Button btn_showbanner;
    private Button btn_showdialog;
    private Button btn_destroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_init = findViewById(R.id.btn_init);
        btn_showbanner = findViewById(R.id.btn_showbanner);
        btn_showdialog = findViewById(R.id.btn_showdialog);
        btn_destroy = findViewById(R.id.btn_destroy);
        if (checkPermission()) {
            Log.e(TAG, " >>> checkPermission is true");
            proxyMethod();
        }
    }

    public void proxyMethod() {
        try {
            String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "plugin.jar";
            Log.e(TAG, " >>> dexPath:" + dexPath);
            String outPath = getFilesDir().getAbsolutePath();
            Log.e(TAG, " >>> outPath:" + outPath);

            /**
             * java.lang.BootClassLoader@1bc2fae4
             * java.lang.BootClassLoader@1bc2fae4
             */
            Log.e(TAG, Context.class.getClassLoader().toString());
            Log.e(TAG, ListView.class.getClassLoader().toString());
            /**
             * android中默认使用的类加载器是：PathClassLoader。
             * 如果要加载一个没有安装的apk，jar，dex则需要使用DexClassLoader
             */

            /**
             * DexClassLoader构造函数参数说明：
             * dexPath：dex压缩文件的路径（可以是apk，jar，dex文件格式）
             * outPath：dex解压缩释放后的目录
             * libraryPath：c/c++依赖的本地库文件目录，可以为null
             * classLoader:上一级的类加载器，就是父类加载器
             */
            DexClassLoader loader = new DexClassLoader(dexPath, outPath, null, getClassLoader());
            Class clazz2 = loader.loadClass("com.lianwenhong.sdk.Dynamic");
            Class clazz1 = Class.forName("com.lianwenhong.sdk.Dynamic");
            /**
             * 是否是同一个Class对象：true
             */
            Log.e(TAG, "是否是同一个Class对象：" + (clazz1 == clazz2));
            /**
             * Clazz1的类加载器是：dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.lianwh.mysourceapp-2/base.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             * Clazz2的类加载器是：dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.lianwh.mysourceapp-2/base.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             * getClassLoader的类加载器是：dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.lianwh.mysourceapp-2/base.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             * 系统类加载器是：dalvik.system.PathClassLoader[DexPathList[[directory "."],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             */
            Log.e(TAG, "Clazz1的类加载器是：" + clazz1.getClassLoader());
            Log.e(TAG, "Clazz2的类加载器是：" + clazz2.getClassLoader());
            Log.e(TAG, "getClassLoader的类加载器是：" + getClassLoader());
            Log.e(TAG, "系统类加载器是：" + getClassLoader().getSystemClassLoader());

            /**
             * dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.lianwh.mysourceapp-2/base.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             * ->java.lang.BootClassLoader@1bc2fae4
             */
            ClassLoader cl = MainActivity.class.getClassLoader();
            while (cl != null) {
                Log.e(TAG, cl + "->");
                cl = cl.getParent();
            }
            //加载具体的某个类
            Class<?> clazz = loader.loadClass("com.lianwenhong.plugin.DynamicImpl");
            /**
             * Clazz的类加载器是：dalvik.system.DexClassLoader[DexPathList[[zip file "/storage/emulated/0/dynamic_temp.jar"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]
             * 因为这个类在外部jar中，所以要用DexClassLoader来加载，PathClassLoader加载不了这个类
             */
            Log.e(TAG, "Clazz的类加载器是：" + clazz.getClassLoader());
            final Dynamic dynamic = (Dynamic) clazz.newInstance();
            btn_init.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dynamic.init(MainActivity.this);
                }
            });
            btn_showbanner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dynamic.showBanner();
                }
            });
            btn_showdialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dynamic.showDialog();
                }
            });
            btn_destroy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dynamic.destroy();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error:" + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Boolean checkPermission() {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            Log.i("读写权限获取", " ： " + isGranted);
            if (!isGranted) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }
        return isGranted;
    }
}