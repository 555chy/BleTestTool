package com.newland.global;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.newland.activity.MainActivity;
import com.newland.bletesttool.R;
import com.newland.utils.LogUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashApplication extends Application {
    /** 异常处理器 */
    private CrashHandler crashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        crashHandler = new CrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }

    class CrashHandler implements UncaughtExceptionHandler {

        private Application app = null;

        public CrashHandler(Application app) {
            this.app = app;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            throwable.printStackTrace();
            LogUtils.logToFile(app, throwable);
            //重启应用
            Intent intent = new Intent(app, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);
            Process.killProcess(Process.myPid());
        }
    }
}