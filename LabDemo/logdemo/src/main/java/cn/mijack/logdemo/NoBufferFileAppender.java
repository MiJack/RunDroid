package cn.mijack.logdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.pqpo.librarylog4a.appender.AbsAppender;
import me.pqpo.librarylog4a.appender.Appender;

/**
 * @author Mi&Jack
 */
public class NoBufferFileAppender extends AbsAppender {

    private File logFile;
    private OutputStream outputStream;

    public NoBufferFileAppender(File logFile) {
        this.logFile = logFile;
        openFileOutputStream();
    }

    private void openFileOutputStream() {
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    return;
                }
                outputStream = new FileOutputStream(logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doAppend(int logLevel, String tag, String msg) {
        if (outputStream == null) {
            return;
        }
//        String logStr = String.format("%s/%s: %s\n", Level.getShortLevelName(logLevel), tag, msg);
        try {
            outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        super.release();
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}