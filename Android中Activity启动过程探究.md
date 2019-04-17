## Android中Activity启动过程探究:
&ensp;&ensp;&ensp;&ensp;本篇笔记主要从视图的角度来探究启动过程，关于线程，Application等操作较为复杂待有空再研究。

&ensp;&ensp;&ensp;&ensp;首先，Android的语法就是java语言，而一个java项目的入口是public static void main(String[] args)，在Android项目中，如果也是同理：ActivityThread类中的public static void main(String[] args)。
```
public static void main(String[] args) {
        ...

        Looper.prepareMainLooper();

        ActivityThread thread = new ActivityThread();
        thread.attach(false);

        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        ...
}
```
&ensp;&ensp;&ensp;&ensp;可以看出，上面主要是给主线程创建了一个Looper消息队列，并且给创建了sMainThreadHandler这个handler对象，后续所有发往主线程的任务都会被这个handler执行。这里只是铺垫介绍一下，再来看一下handler的创建：
```
final Handler getHandler() {
    return mH;
}
```
&ensp;&ensp;&ensp;&ensp;仅仅是返回了一个mH对象，这个mH其实是ActivityThread定义的一个内部类H类型的对象。这个继承自Handler，发送到主线程的消息最终都是通过这个mH的handleMessage来处理的：

```
public void handleMessage(Message msg) {
            if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

                    r.packageInfo = getPackageInfoNoCheck(
                            r.activityInfo.applicationInfo, r.compatInfo);
                    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                } break;
                case RELAUNCH_ACTIVITY: {
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityRestart");
                    ActivityClientRecord r = (ActivityClientRecord)msg.obj;
                    handleRelaunchActivity(r);
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                } break;
                ...
                case RESUME_ACTIVITY:
                    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityResume");
                    SomeArgs args = (SomeArgs) msg.obj;
                    handleResumeActivity((IBinder) args.arg1, true, args.argi1 != 0, true,
                            args.argi3, "RESUME_ACTIVITY");
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    break;
                ...
            }
            Object obj = msg.obj;
            if (obj instanceof SomeArgs) {
                ((SomeArgs) obj).recycle();
            }
            if (DEBUG_MESSAGES) Slog.v(TAG, "<<< done: " + codeToString(msg.what));
        }
```
&ensp;&ensp;&ensp;&ensp;这里只截取几个关键case类型进行说明，这个handler主要处理各种对Activity，Service等的操作。我们看LAUNCH_ACTIVITY这个分支。它调用了handleLaunchActivity方法传进去的参数有runnable:

```
private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        ...

        Activity a = performLaunchActivity(r, customIntent);

        if (a != null) {
            r.createdConfig = new Configuration(mConfiguration);
            reportSizeConfigurations(r);
            Bundle oldState = r.state;
            handleResumeActivity(r.token, false, r.isForward,
                    !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);

            if (!r.activity.mFinished && r.startsNotResumed) {
                // The activity manager actually wants this one to start out paused, because it
                // needs to be visible but isn't in the foreground. We accomplish this by going
                // through the normal startup (because activities expect to go through onResume()
                // the first time they run, before their window is displayed), and then pausing it.
                // However, in this case we do -not- need to do the full pause cycle (of freezing
                // and such) because the activity manager assumes it can just retain the current
                // state it has.
                performPauseActivityIfNeeded(r, reason);

                // We need to keep around the original state, in case we need to be created again.
                // But we only do this for pre-Honeycomb apps, which always save their state when
                // pausing, so we can not have them save their state when restarting from a paused
                // state. For HC and later, we want to (and can) let the state be saved as the
                // normal part of stopping the activity.
                if (r.isPreHoneycomb()) {
                    r.state = oldState;
                }
            }
        } else {
            // If there was an error, for any reason, tell the activity manager to stop us.
            try {
                ActivityManager.getService()
                    .finishActivity(r.token, Activity.RESULT_CANCELED, null,
                            Activity.DONT_FINISH_TASK_WITH_ACTIVITY);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }
```
&ensp;&ensp;&ensp;&ensp;如上代码中，比较关键的两个步骤是 **performLaunchActivity()** 和 **handleResumeActivity()** 两个调用。我们先跟进去看看performLaunchActivity()。

```
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ...
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
            ...
        }

        try {
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
                ...
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);
                ...
                activity.mCalled = false;
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
                ...

        } catch (SuperNotCalledException e) {
            ...

        } catch (Exception e) {
            ...
        }

        return activity;
    }
```
&ensp;&ensp;&ensp;&ensp;首先通过Activity的类名创建了**activity**对象。然后调用**activity.attach()** 方法(这个方法里面做了很多比如绑定window，定义windowManager，以及一些其他的重要操作)。紧接着通过**Instrumentation** 对象执行了**callActivityOnCreate()** 方法，这个方法里面很关键。先看attach：

##### Activity：
```
final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {
        attachBaseContext(context);
        ...
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        ...
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
        ...
        mWindowManager = mWindow.getWindowManager();
    }
```
&ensp;&ensp;&ensp;&ensp;attach中对创建了Activity.mWindow对象，该对象的实例是PhoneWindow类型，PhoneWindow是Window类的实现类，内部定义了一个内部变量DecorView对象。这个DecorView对象就是整个窗口的根View，它继承自FragmentLayout。  
&ensp;&ensp;&ensp;&ensp;在attach一系列操作做完之后就走了**Instrumentation.callActivityOnCreate()** 。这个方法的内部实现：
##### Instrumentation：

```
public void callActivityOnCreate(Activity activity, Bundle icicle) {
        prePerformCreate(activity);
        activity.performCreate(icicle);
        postPerformCreate(activity);
    }
```
&ensp;&ensp;&ensp;&ensp;前后做的操作我们暂且不用关心，实际上它又调用了**activity.performCreate()** 方法,我们再回到activity中看看：
##### Activity：
```
final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        mCanEnterPictureInPicture = true;
        restoreHasCurrentPermissionRequest(icicle);
        if (persistentState != null) {
            onCreate(icicle, persistentState);
        } else {
            onCreate(icicle);
        }
        mActivityTransitionState.readState(icicle);

        mVisibleFromClient = !mWindow.getWindowStyle().getBoolean(
                com.android.internal.R.styleable.Window_windowNoDisplay, false);
        mFragments.dispatchActivityCreated();
        mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
    }
```
&ensp;&ensp;&ensp;&ensp;由此可见，开始走Activity的声明周期了，Activity.performCreate()方法调用了onCreate()，实际上当走到onCreate的时候往往我们这里就会开始对Activity设置布局，这时通常就会走setContent()方法，而看下Activity.setContentView():

```
public void setContentView(@LayoutRes int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
```
&ensp;&ensp;&ensp;&ensp;getWindow()返回的就是刚刚在attach()方法中创建的phoneWindow对象。所以真正调用的是phoneWindow.setContentView()方法。
##### PhoneWindow：

```
public void setContentView(View view,ViewGroup.LayoutParams params){
    if(mCOntentParent == null){
        installDecor();
    }else{
        mContentParent.removeAllViews();
    }
    mContentParent.addView(view,params);
    final Callback cb = getCallback();
    if(cb != null && !isDestoryed()){
        cb.onContentChanged();
    }
}
```
&ensp;&ensp;&ensp;&ensp;PhoneWindow主要有两个重要成员：一个是DecorView **mDecor** ,另一个是ViewGroup **mContentParent** 。

```
private void installDecor() {
    if (mDecor == null) {
        mDecor = generateDecor();
        ...
    }
    if (mContentParent == null) {
        mContentParent = generateLayout(mDecor);
        ...
    }
}
```
&ensp;&ensp;&ensp;&ensp;generateDecor()只是简单的new了一个DecorView，不展开，我们看看generateLayout()方法的实现：
```
protected ViewGroup generateLayout(DecorView decor) {

    //...以上省去，大致上是与样式，主题，版本相关的对视图的设置。
    
    //以下开始填充decor
    
    // Inflate the window decor.
    int layoutResource;    //这个是用来inflate的id
    
    ...    //这里省去，内容是根据Window的各种特性（feature）选择一个合适的视图id赋给layoutResource

    mDecor.startChanging();

    View in = mLayoutInflater.inflate(layoutResource, null);
    decor.addView(in, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

    ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);    //注意这个地方
    if (contentParent == null) {
        throw new RuntimeException("Window couldn't find content container view");
    }
    
    ... //省去，内容是设置背景，设置ActionBar的一些属性。

    mDecor.finishChanging();

    return contentParent;
}
```
&ensp;&ensp;&ensp;&ensp;这里最关键的一步，其实是上面注释中写的注意的那一步，可见其实是将DecorView中包含的那个id为content的LinearLayout赋值给contentParent对象。
###### PhoneWindow总结：  
&ensp;&ensp;&ensp;&ensp;1.一个Activity中包含这一个PhoneWindow对象，是一对一关系，Activity A跳转到Activity B，那么Activity B也会创建一个自身的PhoneWindow。  
&ensp;&ensp;&ensp;&ensp;2.PhoneWindow中有一个DecorView对象，该View对象是整个窗口的最根视图。内部包含了一个ActionBar视图对象和content视图对象。actionBar的id为title,content视图的id是content，并且该视图是LinearLayout类型。一般setContentView就是添加到该LinearLayout视图中。  
&ensp;&ensp;&ensp;&ensp;3.PhoneWindow自己并不是一个视图（View），它的成员变量mDecor才是整个界面的视图，mDecor是在generateLayout()的时候被创建出来的，而actionBar和contentParent两个视图都是通过findViewById()直接从mDecor中获取出来的。
  
&ensp;&ensp;&ensp;&ensp;回到setContentView中继续走，mContentParent.removeAllViews()可见每次新设置视图都会先清除DecorView中的所有视图。然后再添加，这时候调用了mContentParent.addView()方法，走的是ViewGroup.addView()方法。我们来看看：
##### ViewGroup：

```
public void addView(View child, int index, LayoutParams params) {
        if (DBG) {
            System.out.println(this + " addView");
        }

        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }

        // addViewInner() will call child.requestLayout() when setting the new LayoutParams
        // therefore, we call requestLayout() on ourselves before, so that the child's request
        // will be blocked at our level
        requestLayout();
        invalidate(true);
        addViewInner(child, index, params, false);
    }

```
&ensp;&ensp;&ensp;&ensp;（requestLayout只会执行measure和layout操作，invalidate只会执行draw操作）。实际上会调用到View.addView()中，view.addView()中做的操作暂且不管了吧，现在看不明白。  
&ensp;&ensp;&ensp;&ensp;到这里ActivityThread的handleLaunchActivity继续执行，接下来是**handleResumeActivity** :  
##### ActivityThread:

```
final void handleResumeActivity(IBinder token,
            boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
        ActivityClientRecord r = mActivities.get(token);
        ...
        r = performResumeActivity(token, clearHide, reason);

        if (r != null) {
        ...
            if (r.window == null && !a.mFinished && willBeVisible) {
                ViewManager wm = a.getWindowManager();
                ...
                if (r.mPreserveWindow) {
                    ...
                    ViewRootImpl impl = decor.getViewRootImpl();
                    if (impl != null) {
                        impl.notifyChildRebuilt();
                    }
                }
                if (a.mVisibleFromClient) {
                    if (!a.mWindowAdded) {
                        a.mWindowAdded = true;
                        wm.addView(decor, l);
                    } else {
                    ...
                    }
                }
                ...
        }
    }
```
&ensp;&ensp;&ensp;&ensp;performResumeActivity()其实也是通过Instrumentation最终会回调到Activity.onResume()。关键看下面的**wm.addView()** 。vm是ViewManager对象，**WindowManager继承自ViewManager** ，ViewManager主要有三个操作视图的方法：**addView(),updateViewLayout(),removeView()**。在前面的Activity中我们知道，attach()方法中给Activity创建了Window，WindowManager等对象，这里我们看一下这个wm是怎么来的： 
##### Activity：
    
```
public WindowManager getWindowManager() {
        return mWindowManager;
    }
```
&ensp;&ensp;&ensp;&ensp;再回到attach()方法查看mWindowManager的初始化情况：  

```
final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {
        ...
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        ...
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
        ...
        mWindowManager = mWindow.getWindowManager();
        mCurrentConfig = config;

        mWindow.setColorMode(info.colorMode);
    }
```
&ensp;&ensp;&ensp;&ensp;跟进**mWindow.setWindowManager()** 这一行进去：  
##### Window:
```
public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
            boolean hardwareAccelerated) {
        mAppToken = appToken;
        mAppName = appName;
        mHardwareAccelerated = hardwareAccelerated
                || SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
        if (wm == null) {
            wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
    }
```
&ensp;&ensp;&ensp;&ensp;实际上这个mWindowManager是**WindowManagerImpl**赋值的。**WindowManagerImpl是WindowManager的实现类**，并且WindowManagerImpl内部的操作基本使用代理的方式由**WindowManagerGlobal**来实现的，所以最终**ActivityThread.handleResumeActivity** 方法中的**vm.addView()** 最终的实现就在这里。跟进去会看到让人欣喜的东西： 
##### WindowManagerGlobal：
```
public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
        ...
        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
            ...
            root = new ViewRootImpl(view.getContext(), display);

            view.setLayoutParams(wparams);

            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);

            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
                // BadTokenException or InvalidDisplayException, clean up.
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
        }
    }
```
&ensp;&ensp;&ensp;&ensp;在WindowManagerGlobal.addView()中生成了一个ViewRootImpl对象，并且调用了这个对象的setView()方法：root.SetView();这时我们进SetView()看一下：
##### ViewRootImpl:
```
/**
 * We have one child
 */
public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
    synchronized (this) {
        if (mView == null) {
            mView = view;
            ...

            // Schedule the first layout -before- adding to the window
            // manager, to make sure we do the relayout before receiving
            // any other events from the system.
            requestLayout();
            
            ...

            view.assignParent(this);
            ...
        }
    }
}
```
&ensp;&ensp;&ensp;&ensp;在setView()方法中调用了**requestLayout()**方法：

```
public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread();
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }
```
&ensp;&ensp;&ensp;&ensp;紧接着就是**scheduleTraversals();** 方法，进去看一下：

```
void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true;
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
            if (!mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }
```
&ensp;&ensp;&ensp;&ensp;**mChoreographer.postCallback(Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);** Post了一个**mTraversalRunnable**对象，这个Runnable被执行时一步一步就走到了**performTraversals()** 方法：

```
final class TraversalRunnable implements Runnable {
        @Override
        public void run() {
            doTraversal();
        }
    }
    
void doTraversal() {
        if (mTraversalScheduled) {
            mTraversalScheduled = false;
            mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);

            if (mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }

            performTraversals();

            if (mProfile) {
                Debug.stopMethodTracing();
                mProfile = false;
            }
        }
    }

```
&ensp;&ensp;&ensp;&ensp;紧接着在**performTraversals()** 方法中开始了一系列的**host.dispatchAttachToWindow()，performMeasure(),performLayout(),performDraw()** 方法，这就是视图的测绘流程开始了。










