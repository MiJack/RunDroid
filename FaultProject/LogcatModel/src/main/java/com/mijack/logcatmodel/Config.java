package com.mijack.logcatmodel;

import com.mijack.Utils;

/**
 * @auhor Mr.Yuan
 * @date 2017/6/4
 */
public class Config {
    private String manifestFile;
    private String logFile;
    private String apkFile;
    private static final Config instance = new Config();
    private String androidJar;

    public boolean readConfig(String[] args) {
//java -jar xxx.jar -logFile  xxxx -manifestFile xxxxx -apkFile xxxx
        if (args == null || args.length % 2 != 0) {
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i++]) {
                case "-logFile":
                    setLogFile(args[i]);
                    break;
                case "-manifestFile":
                    setManifestFile(args[i]);
                    break;
                case "-apkFile":
                    setApkFile(args[i]);
                    break;
                case "-androidJar":
                    setAndroidJar(args[i]);
                    break;
            }
        }
        return !(Utils.isEmpty(apkFile) || Utils.isEmpty(logFile) || Utils.isEmpty(manifestFile)|| Utils.isEmpty(androidJar));
    }

    public static Config getInstance() {
        return instance;
    }

    public void setManifestFile(String manifestFile) {
        this.manifestFile = manifestFile;
    }

    public String getManifestFile() {
        return manifestFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setApkFile(String apkFile) {
        this.apkFile = apkFile;
    }

    public String getApkFile() {
        return apkFile;
    }

    public void setAndroidJar(String androidJar) {
        this.androidJar = androidJar;
    }

    public String getAndroidJar() {
        return androidJar;
    }
}
