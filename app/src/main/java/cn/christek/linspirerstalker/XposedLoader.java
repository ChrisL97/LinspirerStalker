package cn.christek.linspirerstalker;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.XModuleResources;
import android.view.KeyEvent;

import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit , IXposedHookInitPackageResources {
    private static final String PKG_Launcher3 = "com.android.launcher3";
    private static final String PKG_UserLogin = "com.iflytek.userlogin";
    private static final String PKG_AppShop = "com.ndwill.swd.appstore";
    private static final String PKG_Settings = "com.android.settings";
    private static final String PKG_LinspirerStalker = "cn.christek.linspirerstalker";
    private XSharedPreferences sharedPreferences;
    private static String MODULE_PATH = null;
    private static int id_ic_sm,id_ic_lc,keycode;
    private static long eventTime;

    public XposedLoader(){
        sharedPreferences = new XSharedPreferences(PKG_LinspirerStalker);
        sharedPreferences.makeWorldReadable();
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        XposedBridge.hookAllMethods(ActivityThread.class, "systemMain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                /*Class<?> ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", classLoader);
                XposedHelpers.findAndHookConstructor(ams, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class<?> sm = XposedHelpers.findClass("android.os.ServiceManager", classLoader);
                        Context context = (Context) param.args[0];
                        smm = new StudyModeManagerService(context);
                        XposedHelpers.callStaticMethod(sm, "addService", "user.tbx.sm", smm, true);
                        mdm = new VirtualMDM(context);
                        XposedHelpers.callStaticMethod(sm, "addService", "user.tbx.mdm", mdm, true);;
                    }
                });
                XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        smm.systemReady();
                        mdm.systemReady();
                    }
                });*/
                Class<?> class_pwp = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", classLoader);
                XposedHelpers.findAndHookMethod(class_pwp, "interceptKeyBeforeQueueing",KeyEvent.class,int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //TODO:4返回，3主页，24音量上，25音量下，26电源
                        //TODO:>100长按
                        KeyEvent event = (KeyEvent)param.args[0];
                        int code = event.getKeyCode();
                        if (code == 321) {
                            return;
                        }
                        /*switch (code){
                            case 24:
                            case 25:
                                XposedBridge.log("音量键按下事件"+event.isLongPress());
                                break;
                            case 1:
                            case 3:
                                XposedBridge.log("导航栏事件"+event.isLongPress());
                                break;
                        }*/
                        if(event.getAction()==KeyEvent.ACTION_DOWN){
                            eventTime = event.getEventTime();
                            keycode = code;
                            return;
                        }
                        if(event.getAction()==KeyEvent.ACTION_UP&&code==keycode){
                            XposedBridge.log("按键："+code+" 持续时间："+(event.getEventTime()-eventTime));
                        }

                    }
                });
            }
        });
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final String pkg = lpparam.packageName;
        final ClassLoader classLoader = lpparam.classLoader;
        sharedPreferences.reload();
        /*if(pkg.equals(PKG_Launcher3)){
            if(sharedPreferences.getBoolean("switch_launcher_debug",false)){
                enableDebugMode();
            }
            //hookLauncher3(lpparam);
            return;
        }
        if(pkg.equals(PKG_UserLogin)){
            if(sharedPreferences.getBoolean("switch_userlogin_debug",false)){
                enableDebugMode();
            }
            return;
        }
        if(pkg.equals(PKG_AppShop)){
            if(sharedPreferences.getBoolean("switch_appshop_debug",false)){
                enableDebugMode();
            }
            return;
        }*/
        if(pkg.equals(PKG_Settings)){
            XposedHelpers.findAndHookMethod("com.android.settings.SettingsActivity", classLoader, "addExternalTiles", List.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    List<Object> categoryList = (List<Object>) param.args[0];
                    Object category = categoryList.get(1);
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("cn.christek.linspirerstalker", "cn.christek.linspirerstalker.AboutActivity");
                    intent.setComponent(componentName);
                    XposedHelpers.callMethod(category,"addTile",createSettingsTile(classLoader,"学习模式",null,id_ic_sm,intent));
                    componentName = new ComponentName("cn.christek.linspirerstalker", "cn.christek.linspirerstalker.AboutActivity");
                    intent.setComponent(componentName);
                    XposedHelpers.callMethod(category,"addTile",createSettingsTile(classLoader,"领创系列软件","包括领创平板管理、用户登录和应用商店",id_ic_lc,intent));
                    param.args[0] = categoryList;
                }
            });
        }

    }

    private void enableDebugMode(){
        XposedBridge.hookAllMethods(android.os.Process.class, "start", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[5] = (Integer) param.args[5] | 1;
            }
        });
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        String pkg = resparam.packageName;
        if (pkg.equals(PKG_Settings)) {
            XModuleResources res = XModuleResources.createInstance(MODULE_PATH,resparam.res);
            id_ic_lc = resparam.res.addResource(res, R.drawable.ic_iflytek);
            id_ic_sm = resparam.res.addResource(res, R.drawable.ic_studymode);
        }
    }

    private Object createSettingsTile(ClassLoader classLoader,String title,String summary,int iconRes,Intent intent){
        Object tile = XposedHelpers.newInstance(XposedHelpers.findClass("com.android.settings.dashboard.DashboardTile",classLoader));
        XposedHelpers.setIntField(tile,"id",iconRes);
        XposedHelpers.setIntField(tile,"iconRes",iconRes);
        XposedHelpers.setObjectField(tile,"title",title);
        XposedHelpers.setObjectField(tile,"summary",summary);
        XposedHelpers.setObjectField(tile, "intent", intent);
        return tile;
    }
}
