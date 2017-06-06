package com.mijack.xposed;

import android.os.Process;

import com.mijack.Xlog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static com.mijack.XlogUtils.method2String;

/**
 * Created by Mr.Yuan on 2017/2/23.
 */
public class HookCallBack extends XC_MethodHook {
    public static final ThreadLocal<String> THREAD_INFO_LOCAL = new ThreadLocal<>();
    private String[] argsType;
    private boolean isStatic;
    public static final char LINE_SPLIT_CHAR = '\t';

    public HookCallBack(String[] argsType, boolean isStatic) {
        this.argsType = argsType;
        this.isStatic = isStatic;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        if (!isStatic) {
            Xlog.logMethodEnter(System.identityHashCode(param),method2String(param.method), param.thisObject, param.args);
        } else {
            Xlog.logStaticMethodEnter(System.identityHashCode(param),method2String(param.method), param.args);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//        super.afterHookedMethod(param);
        boolean hasThrowable = param.hasThrowable();
        if (isStatic && hasThrowable) {
            Xlog.logStaticMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.getThrowable());
        } else if (isStatic && !hasThrowable) {
            Xlog.logStaticMethodExitWithResult(System.identityHashCode(param),method2String(param.method),param.getResult());
        } else if (!isStatic && hasThrowable) {
            Xlog.logMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.thisObject, param.getThrowable());
        } else {
            Xlog.logMethodExitWithResult(System.identityHashCode(param),method2String(param.method), param.thisObject,param.getResult());
        }
    }


}
