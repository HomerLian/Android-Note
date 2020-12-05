## 具体实现插件的思想步骤
    在做插件化时，必须对类加载器有一定概念，具体可以百度，或者可查看一篇写的还可以的文章：[Android类加载](https://blog.csdn.net/yang_niuxxx/article/details/80012053)

#### 首先，我们普及一下jar和aar的区别：

jar：只有.class和清单文件，不包含资源文件</br>
aar：包含jar包和资源文件，如图片等所有的res中的文件</br>

**注意：同一个类，用不同的类加载器进行加载产生出来的对象是不同的，不能进行相互赋值，否则就会出现转化异常**</br>

所以，如果我们想要把某个功能做成插件，我们的思路就是通过类加载器去加载某个咱们指定路径下放置的一个类，加载进来之后，可以通过反射生成一个对象，然后就能用了，但是往往这样做不够优雅，这样代码显得臃肿以及扩展性很差，比如说你想要修改某个功能，你得在主工程中去查找一堆的逻辑然后再去对应插件工程中的类，那样很费劲。所以我用了另一个方式来做，避免了主工程代码中有很多的反射等让人头晕的代码。
    
我的做法是我将想要的功能做成一个lib工程，也就是sdk的概念，后续我们以sdk相称。这时候我在sdk中定义一些所需要的功能接口。然后将这个sdk打成jar包或者时aar，然后给主工程依赖，这时候主工程就具备了我们想要的能力，也就是说主工程就能持有sdk工程中的接口。然后我们再创建一个插件工程，插件工程也依赖sdk工程，这时候插件工程同样拥有了你想要的能力，也就是插件和主工程通过sdk为桥梁建立起了联系。然后在插件工程中做具体的实现，后续主工程执行相应功能的时候，在主工程中去加载插件工程中的类，就能具体获得插件工程中的具体实现了。

说的太多我们具体实现一下就知道了，假设我们需要有一个Dynamic接口，里面有init,showBanner等方法是用于具体在某个时机做某些操作。
所以总共涉及三个工程：**main，sdk，plugin**</br></br>
1.在sdk内部定义一个接口Dynamic,在接口中定义好要用的方法，并将sdk打成jar</br></br>2.main依赖sdk.jar,plugin也依赖sdk.jar</br></br>3.在plugin内部创建一个DynamicImpl，实现sdk中的Dynamic接口</br></br>4.将plugin打成jar，然后将plugin生成的jar通过dx工具转换成dex字节码的jar（通过java工程打成的jar，内部是.class字节码，Android虚拟机是无法识别的，所以必须转换成dex字节码的jar）</br></br>*mac系统下：cd到android sdk build-tools/xx版本/下，然后执行：
./dx --dex --output=/path_prefix/output.jar /path_prefix/origin.jar
其中/path_prefix/output.jar为要输出的新路径, /path_prefix/origin.jar为原始jar包*</br></br>
5.将转换完成的jar放在某个位置，然后主工程使用所需工程的时候，用DexClassLoader到该位置去加载，这时候声明的变量类型是Dynamic，但是具体加载进来的是DynamicImpl，所以调用到的也是DynamicImpl中的方法，这就是java的多态思想。所以这时候我们就能实现某个功能的使用，当后续想要替换某个功能的时候，只需把plugin.jar替换了就行。</br></br>
可能大家有疑问，既然加载的是插件中的类，为什么要在主工程中以来sdk，这个就是一个具体实现中我们能碰上的问题了，后续讲，所以代码就是：

sdk中声明接口：
```
package com.lianwenhong.sdk;

import android.app.Activity;

public interface Dynamic {
    void init(Activity var1);

    void showBanner();

    void showDialog();

    void destroy();
}

```
插件工程中实现Dynamic：

```
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
```
主工程中调用具体的方法来实现某些功能：

```
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
            //来回答上一个问题，如果这时候没有在主工程中依赖sdk工程，那么这时候主工程连编译都编不过
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
```
这时候功能就实现了。
在具体实现过程中，sdk工程如何打成jar包这个需要讲一下，我们需要在gradle文件的android平级下增加以下任务：

```
task clearJar(type: Delete) {
    delete "libs/sdk.jar" //sdk是你自己jar包的名字，随便命名
}
task makeJar(type:org.gradle.api.tasks.bundling.Jar) {
//指定生成的jar名
    baseName "sdk"
//指定想要打到jar里的class文件的位置，这里因为不同版本的androidstudio生成的class文件位置有所差异，所以具体要根据自身的版本来设置。
    from("build/intermediates/classes/lianwh/debug/com/lianwh/dynamic/impl/")
    from("build/intermediates/classes/lianwh/debug/android/")
//这里是指定某个类打包到jar中的时候的路径，例如我们将DynamicImpl打包到jar中，这时候DynamicImpl的全类名就是com.lianwh.dynamic.impl.DynamicImpl
    into("com/lianwh/dynamic/impl/")
//去掉不需要打包的目录和文件
    exclude("test/", "BuildConfig.class", "R.class")
//去掉R开头的文件
    exclude{ it.name.startsWith("R")}
}
makeJar.dependsOn(clearJar, build)
```
还有就是将class字节码的jar打成dex字节码的jar，这里上面有说明了mac系统下的转换方式，windows下可以自行百度。
