package com.mijack.logcatmodel;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Mr.Yuan on 2017/4/5.
 */
public class LogMain {
    public static final String ADB = "D:\\Android\\sdk\\platform-tools\\adb.exe";
    public static final String APP_NAME = "";//"cn.mijack.multithreadandipcdemo";

    public static void main(String[] args) throws IOException {
        IDevice device;
        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                ADB, false);
        waitForDevice(bridge);
        IDevice devices[] = bridge.getDevices();
        device = devices[0];
        LogCatReceiverTask logCatReceiverTask = new LogCatReceiverTask(device);
        logCatReceiverTask.addLogCatListener(new LogCatListener() {
            public static final String FILTER_TAG = "Xlog";
            public final Pattern PATTERN_FILTER_TAG = Pattern.compile(FILTER_TAG);

            public void log(List<LogCatMessage> msgList) {
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter("logcat.json", true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (LogCatMessage msg : msgList) {
                    if (PATTERN_FILTER_TAG.matcher(msg.getTag()).matches()
                            && msg.getMessage().startsWith("{")) {
                        String log = msg.getMessage();
                        try {
                            fileWriter.write(log);
                            fileWriter.write(",\n");
                            fileWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        System.out.println(log);

                    }
                }
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("saddasd");
                }
            }

        });

        new Thread(logCatReceiverTask).start();
    }

    private static void waitForDevice(AndroidDebugBridge bridge) {
        int count = 0;
        while (!bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException ignored) {
            }
            if (count > 300) {
                System.err.print("Time out");
                break;
            }
        }
    }
}
