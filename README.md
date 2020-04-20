# 1. 导入说明

## 1.1 导入相关依赖
* 将jkad.aar拷贝至app目录下的libs目录；
* 在app目录下的build.gradle文件中添加如下代码：
```
    android {
        defaultConfig {
            ndk {
                //根据项目需要，选择需要支持的CPU架构；
                //还可以添加 'x86', 'x86_64', 'mips', 'mips64'；
                abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a'
            }
        }
        repositories {
            flatDir {
                dirs 'libs'
            }
        }
    }
    dependencies {
        implementation(name: 'jkad', ext: 'aar')
        implementation 'com.google.code.gson:gson:2.8.2'
        implementation 'com.android.support:viewpager:28.0.0'
    }
```

## 1.2 添加所需权限
* 在AndroidManifest.xml文件中已添加已下相关权限：
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```
注意：Android 6.0后部分权限需要动态申请，否则无法正常使用。

## 1.3 打包混淆规则
* 如果项目添加了混淆，请在app目录下的proguard-rules.pro文件中添加如下配置: 
```
   -keep class com.jk.ad.** { *; }
```

---

# 2. 接口文档

## 2.1 AdManage（广告全局管理类）

### AdManage getInstance()

方法备注：AdManage实例

方法名称：getInstance

方法参数：无

方法返回：AdManage实例

必须调用：是

### AdManage setScheduleUrl(String scheduleurl)

方法备注：设置预缓存广告素材API

方法名称：setScheduleUrl

方法参数：scheduleurl，预缓存广告素材API

方法返回：AdManage实例

必须调用：是

### AdManage setShowPositionUrl(String showPositionUrl)

方法备注：设置广告展示API

方法名称：setShowPositionUrl

方法参数：showPositionUrl，广告展示API

方法返回：AdManage实例

必须调用：是

### AdManage setPositionCodes(List<String> positionCodes)

方法备注：设置广告位ID列表

方法名称：setPositionCodes

方法参数：positionCodes，广告位ID列表

方法返回：AdManage实例

必须调用：是

### AdManage setTime(int time)

方法备注：设置预缓存广告素材轮询的时间间隔

方法名称：setTime

方法参数：time，预缓存广告素材轮询的时间间隔

方法返回：AdManage实例

必须调用：否（默认20分钟）


### AdManage setPath(String path)

方法备注：设置广告保持路径

方法名称：setPath

方法参数：path，广告保存路径

方法返回：AdManage实例

必须调用：否（默认保存再sdcard目录）

### AdManage setMedia(String media)

方法备注：设置媒体标识

方法名称：setMedia

方法参数：media，媒体标识

方法返回：AdManage实例

### AdManage setChannel(String channel)

方法备注：设置渠道标识

方法名称：setChannel

方法参数：channel，渠道标识

方法返回：AdManage实例

### AdManage setLocationOn(boolean locationOn)

方法备注：是否上报定位

方法名称：setLocationOn

方法参数：locationOn，是否上报定位

方法返回：AdManage实例

必须调用：否

### AdManage isSystemLog(boolean isLog) 

方法备注：设置是否打印日志

方法名称：isSystemLog

方法参数：isLog，是否打印日志

方法返回：AdManage实例

必须调用：否（默认不打印）

### AdManage init(Context context) 

方法备注：AdManage初始化

方法名称：init

方法参数：Context上下文对象

方法返回：AdManage实例

必须调用：是

###  AdManage initAds(Context context) 

方法备注：预加载广告资源

方法名称：initAds

方法参数：Context上下文对象

方法返回：AdManage实例

必须调用：是

###  void checkAds(String positionCode, CheckADListener checkADListener)

方法备注：检查广告

方法名称：checkAds

方法参数：
参数名称  | 参数说明 
---|---|---
positionCode | 广告位标识 
checkADListener | 检查广告监听 

方法返回：无

适用场景：通过SDK播放广告，播放广告之前调用

###  void getAds(String positionCode, GetADListener getADListener) 

方法备注：获取广告路径

方法名称：getAds

方法参数：
参数名称  | 参数说明 
---|---|---
positionCode | 广告位标识 
getADListener | 获取广告监听 

方法返回：无

适用场景：通过SDK获取广告路径，由外部APP自行播放，获取广告时调用


###  void void onClick(String positionCode) 

方法备注：广告点击监听

方法名称：onClick

方法参数：
参数名称  | 参数说明 
---|---|---
positionCode | 广告位标识 

方法返回：无

适用场景：通过SDK获取广告路径，由外部APP自行播放，点击广告时调用（由默认浏览器打开跳转链接）

###  onClickWithReturn(String positionCode)

方法备注：广告点击监听

方法名称：onClickWithReturn

方法参数：
参数名称  | 参数说明 
---|---|---
positionCode | 广告位标识 

方法返回：无

适用场景：通过SDK获取广告路径，由外部APP自行播放，点击广告时调用（SDK返回跳转链接，由外部APP自行处理）

---

## 2.2 CheckADListener（广告检查监听）

###  void ADCount(int count)

方法备注：广告数据检查回调

方法名称：ADCount

方法参数：
参数名称  | 参数说明
---|---|---
count | 可正常播放的广告数量

方法返回：无

---

## 2.3 GetADListener（广告获取监听）

### void ADSuccess(int count, ArrayList<String> paths)

方法备注：广告数据获取成功

方法名称：ADSuccess

方法参数：
参数名称  | 参数说明
---|---|---
count | 可正常播放的广告数量
paths | 可正常播放的广告路径列表

方法返回：无

###  void ADError(String message)

方法备注：广告数据获取失败

方法名称：ADError

方法参数：
参数名称  | 参数说明
---|---|---
message | 错误提示信息

方法返回：无

---

## 2.4 AdView（普通广告控件）

### void setPosition(String position)

方法备注：设置控件的广告位标识

方法名称：setPosition

方法参数：
参数名称  | 参数说明
---|---|---
position | 广告位标识

方法返回：无

必须调用：是

### void setSkip(boolean skip)

方法备注：设置是否显示关闭按钮

方法名称：setSkip

方法参数：
参数名称  | 参数说明 
---|---|---
skip | 是否显示关闭按钮 

方法返回：无

必须调用：否（默认不显示关闭按钮）

### void setLoopAhead(int loopAhead)

方法备注：设置是否循环连播

方法名称：setLoopAhead

方法参数：
参数名称  | 参数说明 | 参数备注
---|---|---
loopAhead | 循环参数 | 0和负数：不循环；正数：循环因子 

方法返回：无

必须调用：否（默认不循环）

### void setHasPoint(boolean setHasPoint)

方法备注：设置是否显示定位圆点

方法名称：setHasPoint

方法参数：
参数名称  | 参数说明 
---|---|---
setHasPoint | 是否显示定位圆点 

方法返回：无

必须调用：否（默认显示定位圆点）

### void setInnerSize(float width, float height)

方法备注：设置内部广告的尺寸

方法名称：setInnerSize

方法参数：
参数名称  | 参数说明  | 参数备注
---|---|---
width | 广告宽度 | 单位dp，0或负数时，与控件宽度相同
height | 广告高度 | 单位dp，0或负数时，与控件高度相同

方法返回：无

必须调用：否（默认内部广告尺寸与控件尺寸相同）

### void setScaleType(int scaleType)

方法备注：设置图片广告填充类型

方法名称：scaleType

方法参数：

参数名称：scaleType，填充类型

参数示例  | 参数说明
---|---|---
FixelView.FIT_XY | 拉伸铺满
FixelView.CENTER_CROP | 等比放大铺满
FixelView.FIT_CENTER |等比自适应居中

方法返回：无

必须调用：否（默认，FIT_CENTER）

### void setOnAdPlayListener(AdView.OnAdPlayListener onAdPlayListener)

方法备注：设置广告播放监听（用于监听广告播放完成和广告关闭事件）

方法名称：setOnAdPlayListener

方法参数：
参数名称  | 参数说明
---|---|---
onAdPlayListener | 广告播放监听

方法返回：无

必须调用：否

### void setOpenType(int openType)

方法备注：设置跳转链接的打开方式

方法名称：setOpenType

方法参数：
参数名称  | 参数说明
---|---|---
openType | 跳转链接打开方法

参数示例  | 参数说明
---|---|---
AdView.OPEN_TYPE_NONE | 点击广告，不打开跳转链接
AdView.OPEN_TYPE_APP | 点击广告，SDK将跳转链接返回给外部APP，由外部APP处理（与setOnAdClickListener配合使用）
AdView.OPEN_TYPE_BROWSER |点击广告，由默认浏览器打开跳转链接

方法返回：无

必须调用：否（默认由默认浏览器打开）


### void setOnAdClickListener(AdView.OnAdClickListener onAdClickListener)

方法备注：设置广告点击监听

方法名称：setOnAdClickListener

方法参数：
参数名称  | 参数说明
---|---|---
onAdClickListener | 广告点击监听

方法返回：无

必须调用：否（设置跳转链接的打开方式为APP打开之后，调用该方式时SDK将跳转链接返回给外部APP，由外部APP处理）

###  void show()

方法备注：播放广告

方法名称：show

方法参数：无

方法返回：无

必须调用：是（调用AdManager检查广告接口之后，再调用播放方法）

---

## 2.5 SplashView（开屏广告控件）

SplashView是AdView的子类，Splash的接口与AdView相同；

### void setTiming(boolean timing)

方法备注：设置是否显示倒计时

方法名称：setTiming

方法参数：
参数名称  | 参数说明 
---|---|---
timing | 是否显示倒计时 

方法返回：无

必须调用：否（默认显示倒计时）

## 2.6 AdView.OnAdPlayListene（广告播放监听）

### void onAdPlayCompleted()；

方法备注：广告播放完成

方法名称：onAdPlayCompleted

方法参数：无

方法返回：无

### void onAdPlayClosed()；

方法备注：广告关闭/跳过回调

方法名称：onAdPlayClosed

方法参数：无

方法返回：无

---

## 2.7 AdView.OnAdClickListener（广告点击监听）

### void onClick(String url)

方法备注：广告点击回调

方法名称：onClick

方法参数：
参数名称  | 参数说明
---|---|---
url | 点击广告需要跳转的链接

方法返回：无

# 3. 使用示例

以下提供简单得调用示例鹅，详情请查看Demo项目。

## 3.1 SDK初始化

在程序的Application中进行SDK的初始化：
```
    public class MyApplication extends Application{
        @Override
        public void onCreate() {
            super.onCreate();
            //初始化SDK
            AdManage.getInstance()//获取AdManage实例对象
                    .setMedia("media")//设置媒体标识
                    .setChannel("channel")//设置渠道标识
                    .setTime(time)//设置预缓存广告素材轮询的时间间隔
                    .setScheduleUrl(schedule)//设置预缓存广告素材API
                    .setShowPositionUrl(showposition)//设置广告展示API
                    .setPositionCodes(positions)//设置广告位ID列表
                    .setPath(path)//设置广告缓存的路径（路径需具备读写权限）
                    .setLocationOn(true)//是否上报定位
                    .isSystemLog(true)//设置是否打印日志
                    .init(this);//初始化
        }
    }
```

## 3.2 预缓存广告素材

根据项目需求，在适当时机，获取广告排期，并预缓存广告素材；
```
    AdManage.getInstance().initAds();
```


## 3.3 播放广告

根据场景，选择合适得集成方式：


### 3.3.1 SDK获取广告数据，并播放广告

* 图片支持类型：jpg、png、bmp；
* 视频支持类型：mp4、3gp；

备注：SplashView是AdView的子类，调用方式完全一致；

#### 3.3.1.1 控件引入

```
    <com.jk.ad.view.ad.AdView
        android:id="@+id/ad_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

#### 3.3.1.2 控件参数设置
```
    adView = findView(R.id.ad_view);

    //设置广告控件的广告位编码；
    adView.setPosition(POSITION_CODE);
    
    //设置是否显示定位圆点        
    adView.setHasPoint(false);
    
    //设置是否显示关闭按钮
    adView.setSkip(true);
        
    //设置广告循环因子（0不循环）
    adView.setLoopAhead(2);
    
    //设置遮罩广告尺寸
    adView.setInnerSize(240, 320);

    //设置图片广告的扩展样式；
    //默认为PixelView.FIT_CENTER（FIT_XY、CENTER_CROP、FIT_CENTER）
    adView.setScaleType(PixelView.FIT_XY);
    
    //设置跳转链接打开方式
    //AdView.OPEN_TYPE_NONE ：点击广告，不打开跳转链接
    //AdView.OPEN_TYPE_APP |：点击广告，SDK将跳转链接返回给外部APP，由外部APP处理（与setOnAdClickListener配合使用）
    //AdView.OPEN_TYPE_BROWSER ：点击广告，由默认浏览器打开跳转链接
    adView.setOpenType(AdView.OPEN_TYPE_APP);

    //设置广告点击监听（与setOpenType配合使用，仅OPEN_TYPE_APP时有效）
    adView.setOnAdClickListener(new AdView.OnAdClickListener() {
        @Override
        public void onClick(String url) {
            //url：广告跳转跳转链接，由APP自行处理
        }
    });
    
    //设置广告播放监听；
    adView.setOnAdPlayListener(new AdView.OnAdPlayListener() {
        
        @Override
        public void onAdPlayCompleted() {
            //广告播放结束（广告不循环，且最后一个广告播放完成）
        }

        @Override
        public void onAdPlayClosed() {
            //广告关闭或跳过
        }
    });
```

#### 3.3.1.3 播放广告

播放广告分两种调用方式：

1.实时检查广告数据，然后播放；

```
    // 检查广告数据
    AdManage.getInstance().checkAds(POSITION_CODE, new CheckADListener() {

        @Override
        public void ADCount(int count) {
            //count:可展示的广告数量
            if (count > 0) {
                //播放广告
                adView.show();
            }
        }
    });

```

2.预先检查广告数据，并播放广告；

加载视图前，可以首先检查广告数据是否可以播放；
检查广告可放置在其他类中执行，根据业务需要，自行选择；
```
    // 检查广告数据
    AdManage.getInstance().checkAds(POSITION_CODE, new CheckADListener() {
        @Override
        public void ADCount(int count) {
            //count:可展示的广告数量
        }
    });
```
播放广告
```
    //播放广告
    adView.show();
```



### 3.3.2 SDK获取广告数据，APP播放广告

#### 3.3.2.1 播放广告：
```
    AdManage.getInstance().getAds(positionCode, new GetADListener() {

        @Override
        public void ADSuccess(int count, ArrayList<String> paths) {
            //count：可以播放的广告数量
            //paths：可以播放的广告路径列表
            //APP自行播放paths路径列表的广告内容
        }

        @Override
        public void ADError(String msg) {
            //msg：广告获取错误信息
        }
    });
```

#### 3.3.2.2 点击广告，调用方式1（由默认浏览器打开广告跳转链接）：
```
    //点击APP的广告后，APP主动调用点击该方法，上报给SDK
    AdManage.getInstance().onClick(positionCode);
```


#### 3.3.2.3 点击广告，调用方式2（SDK返回跳转链接，由APP自行处理）：
```
    //点击APP的广告后，APP主动调用点击该方法，上报给SDK
    //返回值为广告跳转链接列表，APP取到后，自行处理
    List<String> urls = AdManage.getInstance().onClickWithReturn(positionCode);
```