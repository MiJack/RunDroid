package cn.mijack.logdemo;

import android.content.Context;

import java.io.File;

/**
 * @author Mi&Jack
 */
public class FileUtils {

    public static File getLogDir(Context context) {
        File log = context.getExternalFilesDir("log");
        if (log == null) {
            log = new File(context.getFilesDir(), "log");
        }
        if (!log.exists()) {
            log.mkdir();
        }
        return log;
    }

}

