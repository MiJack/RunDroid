import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.mijack.Utils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
//import com.Demo;
/**
 * @auhor Mr.Yuan
 * @date 2017/4/10
 */
public class WebMagicDemo {
Demo demo;
    public static final String CLASS_SITE = "https://developer.android.google.cn/reference/classes.html";
    public static final String PACKAGE_SITE = "https://developer.android.google.cn/reference/packages.html";

    public static void main(String[] args) {
        try {
            fun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fun() {
//        Spider.create(new WebMagicDemo()).addUrl(CLASS_SITE).thread(1).run();
        //基本思路
        //1、爬去package页面下的package name
        PackagePageProcessor packagePageProcessor = new PackagePageProcessor();
        Spider.create(packagePageProcessor).addUrl(PACKAGE_SITE).runAsync();
        //2、爬去class页面下的class name
        ClassPageProcessor classPageProcessor = new ClassPageProcessor();
        Spider.create(classPageProcessor).addUrl(CLASS_SITE).runAsync();
        while (classPageProcessor.classes == null || packagePageProcessor.packageNames == null) {
        }
        List<String> packageNames = packagePageProcessor.getPackageNames();
        List<String> classes = classPageProcessor.getClasses();
        //class 和package 产生对应关系
        //规则，class所属的package是class的前缀中的最长的前缀和package 的交集，往往只有一个
        Map<String, List<String>> map = new HashMap<>();
        packageNames.stream().distinct().forEach(p -> map.put(p, new ArrayList<>()));
        map.put("", new ArrayList<>());
        for (String clazz : classes) {
            List<String> targets = new ArrayList();
            for (String packageName : packageNames) {
                if (clazz.startsWith(packageName)) {
                    targets.add(packageName);
                }
            }
            Optional<String> max = targets.stream().distinct().max((String o1, String o2) -> {
                int l1 = Utils.length(o1);
                int l2 = Utils.length(o2);
                return l1 == l2 ? 0 : (l1 < l2 ? -1 : 1);
            });
            if (!max.isPresent()) {
                System.err.println("find no package for class[" + clazz + "]");
                map.get("").add(clazz);
            } else {
                map.get(max.get()).add(clazz);
            }
        }
// 打印map
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer();
        try {
            SequenceWriter sequenceWriter = writer.writeValues(new File("class.json"));
            sequenceWriter.write(map);
            sequenceWriter.flush();
            sequenceWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClassPageProcessor implements PageProcessor {
        private List<String> classes ;

        @Override
        public void process(Page page) {
            classes = page.getHtml().$("table").css(".jd-linkcol").$("a").links().all().stream().distinct()
                    .map(s -> s.substring("https://developer.android.google.cn/reference/".length(), s.length() - ".html".length()).replace('/', '.'))
                    .collect(Collectors.toList());
        }

        private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

        @Override
        public Site getSite() {
            return site;
        }

        public List<String> getClasses() {
            return classes;
        }
    }

    public static class PackagePageProcessor implements PageProcessor {
        private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
        private List<String> packageNames;

        public List<String> getPackageNames() {
            return packageNames;
        }

        @Override
        public void process(Page page) {
            packageNames = page.getHtml().$("table").css(".jd-linkcol").$("a").xpath("//a/text()").all();
        }

        @Override
        public Site getSite() {
            return site;
        }
    }
}