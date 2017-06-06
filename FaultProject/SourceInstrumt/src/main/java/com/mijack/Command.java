package com.mijack;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @auhor Mr.Yuan
 * @date 2017/5/12
 */
public class Command {

    public static String transformJavaFile(String file, String baseDir, String outputDir) {
        CommandLine cmdLine = new CommandLine("\"D:\\Program Files (x86)\\srcML 0.9.5\\bin\\srcml.exe\" ${file} -o ${target}.xml");
        String target = outputDir + (file.substring(baseDir.length()));
        Map<String, String> map = new HashMap<>();
        map.put("file", file);
        map.put("target", target);
//        map.put("encoding", "utf-8");
        cmdLine.setSubstitutionMap(map);
        String command = cmdLine.getExecutable();
        System.out.println("command:" + command);
        new File(target).getParentFile().mkdirs();
        execute(command);
        //        try {
//            Process p = Runtime.getRuntime().exec(command);
//            p.waitFor();
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        DefaultExecutor executor = new DefaultExecutor();
//        executor.setExitValue(1);
//        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
//        executor.setWatchdog(watchdog);
//        System.out.println(cmdLine.getExecutable());
//        executor.setWorkingDirectory(null);
//        try {
//            return executor.execute(cmdLine);
//        } catch (Throwable e) {
//            e.printStackTrace(System.out);
//            if (e instanceof ExecuteException) {
//                return ((ExecuteException) e).getExitValue();
//            } else {
//                return -1;
//            }
//        }
        return target + ".xml";
    }

    public static void main(String[] args) {
        String file = "F:\\AndroidProjects\\LabDemo\\demo\\src\\main\\java\\cn\\mijack\\multithreadandipcdemo\\HandlerActivity.java";
        CommandLine cmdLine = new CommandLine("srcml.exe ${file} -o ${file}.xml  --src-encoding ${encoding}");
//        %%i -o %%i.xml  --src-encoding utf-8
        Map<String, String> map = new HashMap<>();
        map.put("file", file);
        map.put("encoding", "utf-8");
        cmdLine.setSubstitutionMap(map);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        System.out.println(cmdLine.getExecutable());
        int exitValue = 0;
        try {
            exitValue = executor.execute(cmdLine);
        } catch (Throwable e) {
//            e.printStackTrace();
        }
        System.out.println("exitValue:" + exitValue);
    }

    public static void execute(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
