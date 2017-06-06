package com.mijack;

import java.io.File;

/**
 * Created by Mr.Yuan on 2017/3/21.
 */
public class Config {
    private static Config instance;
    private String input;
    private String xmlOutput;
    private String javaOutput;
    private boolean forceWrite = false;
    private String command;
    private static final String workspace = "workspace";

    public static String getWorkspacePath() {
        return new File(workspace).getAbsolutePath();
    }

    public static Config getInstance() {
        synchronized (Config.class) {
            if (instance == null) {
                instance = new Config();
            }
        }
        return instance;
    }

    public String getXmlOutput() {
        return xmlOutput;
    }

    public void setXmlOutput(String xmlOutput) {
        this.xmlOutput = xmlOutput;
    }

    public String getJavaOutput() {
        return javaOutput;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setJavaOutput(String javaOutput) {
        this.javaOutput = javaOutput;
    }

    public void setForceWrite(boolean forceWrite) {
        this.forceWrite = forceWrite;
    }

    public boolean isForceWrite() {
        return forceWrite;
    }

    public String getInput() {
        return input;
    }

    public String getInstallCommand() {
        return command;
    }

    public void setInstallCommand(String command) {
        this.command = command;
    }
}
