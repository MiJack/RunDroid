package com.mijack.logcatmodel;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.util.Chain;

import java.util.*;

/**
 * @auhor Mr.Yuan
 * @date 2017/5/5
 */
public class SootPlugins {
    private static final SootPlugins instance = new SootPlugins();
    private String apkFile;

    public static SootPlugins getInstance() {
        return instance;
    }

    public void analyzeApk(String apkFile) {
        this.apkFile = apkFile;
        run();
    }

    public static final String ANDROID_JAR = "D:\\Android\\SDK\\platforms";

    public static void run() {
        init();
        PackManager.v().runPacks();
        //
        System.out.println("---------------------------------------------------------");
        Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
        Iterator<SootClass> iterator = applicationClasses.iterator();
        while (iterator.hasNext()) {
            SootClass sootClass = iterator.next();
            System.out.println(sootClass.getName());
        }
        System.out.println("---------------------------------------------------------------------");
        getViewClickListener().stream().forEach(System.out::println);
        System.out.println("---------------------------------------------------------------------");

        Collection<SootClass> subclasses = Scene.v().getFastHierarchy().getSubclassesOf(Scene.v().getSootClass("android.os.Handler"));
        subclasses.stream().forEach(System.out::println);
        System.out.println("------------------------------------------------");
        List<String> handlers = getHandlers();
        handlers.forEach(System.out::println);
    }

    public static void main(String[] args) {
        run();
        //
        System.out.println("---------------------------------------------------------");
        Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
        Iterator<SootClass> iterator = applicationClasses.iterator();
        while (iterator.hasNext()) {
            SootClass sootClass = iterator.next();
            System.out.println(sootClass.getName());
        }
        System.out.println("---------------------------------------------------------------------");
        getViewClickListener().stream().forEach(System.out::println);
        System.out.println("---------------------------------------------------------------------");

        Collection<SootClass> subclasses = Scene.v().getFastHierarchy().getSubclassesOf(Scene.v().getSootClass("android.os.Handler"));
        subclasses.stream().forEach(System.out::println);
        System.out.println("------------------------------------------------");
        List<String> handlers = getHandlers();
        handlers.forEach(System.out::println);
    }

    public static List<String> getHandlers() {
        List<String> result = new ArrayList<>();
        Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
        Iterator<SootClass> iterator = applicationClasses.iterator();
        while (iterator.hasNext()) {
            SootClass sootClass = iterator.next();
            if (isHandler(sootClass)) {
                result.add(sootClass.getName());
            }
        }
        return result;
    }

    public static boolean isHandler(SootClass sootClass) {
        while (sootClass.hasSuperclass()) {
            SootClass superclass = sootClass.getSuperclass();
            if (superclass.getName().equals("android.os.Handler")) {
                return true;
            }
            sootClass = superclass;
        }
        return false;
    }

    public static void init() {
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_whole_program(true);
        Options.v().set_force_overwrite(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(Config.getInstance().getApkFile()));
        Options.v().set_android_jars(Config.getInstance().getAndroidJar());
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_soot_classpath(Config.getInstance().getAndroidJar());

        Scene.v().loadNecessaryClasses();
    }

    public static List<String> getViewClickListener() {
        List<String> result = new ArrayList<>();
        Collection<SootClass> subclasses = Scene.v().getFastHierarchy().getAllImplementersOfInterface(Scene.v().getSootClass("android.view.View$OnClickListener"));
        Iterator<SootClass> iterator = subclasses.iterator();
        while (iterator.hasNext()){
            SootClass sootClass = iterator.next();
            result.add(sootClass.getName());
        }
        return result;
    }
}
