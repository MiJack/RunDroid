package com.mijack;

import com.mijack.meta.ClassMeta;
import com.mijack.meta.FunctionMeta;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.rmi.CORBA.Util;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Mr.Yuan on 2017/3/23.
 */
public class JavaFileObject {
    private String packageName;
    private String fileAbsolutePath;
    private Document document;
    private List<FunctionMeta> functionMetas;
    private List<ClassMeta> classMetas;
    private List<String> importClasses;
    //    private List<Element>
    public static final Pattern CLASS_PATTERN = Pattern.compile("^package\\s+(.*);$");

    public JavaFileObject(String fileAbsolutePath, Document document) {
        this.fileAbsolutePath = fileAbsolutePath;
        this.document = document;
        Element root = document.getRootElement();
        //查找package name
        List<Node> list = document.selectNodes("//*");
        Iterator<Node> iterator = list.iterator();
        while (iterator.hasNext()) {
            Node next = iterator.next();
            if (next.getName().equals("package") && next instanceof Element) {
                Matcher matcher = CLASS_PATTERN.matcher(Utils.elementToString((Element) next));
                if (matcher.matches()) {
                    packageName = matcher.group(1);
                }
                break;
            }
        }
        importClasses = new ArrayList<>();
        List<Element> importElements = Utils.findAllElement(root, "import");
        for (int i = 0; importElements != null && i < importElements.size(); i++) {
            Element importElement = importElements.get(i);
            Element name = importElement.element("name");
            importClasses.add(Utils.elementToString(name));
        }
        classMetas = new ArrayList();
        //获取所有的class
        findAllClasses(root, classMetas, packageName);
        //知道所有的class为null的
        //获取每个class下的所有function
        functionMetas = new ArrayList();
        for (ClassMeta classMeta : classMetas) {
            findAllFunctions(classMeta.getRoot(), classMeta, functionMetas);
        }
    }

    private void findAllClasses(Element root, List<ClassMeta> classMetas, String packageName) {
        Iterator<Element> iterator = root.elementIterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            if ("class".equals(element.getName())) {
                classMetas.add(new ClassMeta(element, packageName, this));
            }
            findAllClasses(element, classMetas, packageName);
        }
    }

    public void findAllFunctions(Element root, ClassMeta classMeta, List<FunctionMeta> functionMetas) {
        Iterator<Element> iterator = root.elementIterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            if ("function".equals(element.getName()) || "constructor".equals(element.getName())) {
                FunctionMeta functionMeta = new FunctionMeta(element);
                classMeta.addFunction(functionMeta);
                functionMetas.add(functionMeta);
                continue;
            } else if ("class".equals(element.getName())) {
                continue;
            }
            findAllFunctions(element, classMeta, functionMetas);
        }
    }

    public Document getDocument() {
        return document;
    }

    public static JavaFileObject create(String fileAbsolutePath, Document document) {
        return new JavaFileObject(fileAbsolutePath, document);
    }

    public List<FunctionMeta> getFunctionMetas() {
        return functionMetas;
    }

    public void processFunction() {
        //插桩
        Iterator<FunctionMeta> iterator = functionMetas.iterator();
        while (iterator.hasNext()) {
            FunctionMeta function = iterator.next();
            System.out.println(function.getJvmFunctionSign());
            //考虑比较简单的if语句
//            if () {        ---->               log if () { log
//            } else {       ---->               log } else { log
//            }              ---->               log } log
//            function.insertIfLog();
            function.insertExitLog();
            function.insertCatchLog();
            function.insertEntryLog();
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public List<ClassMeta> getClassMetas() {
        return classMetas;
    }

    public List<String> getImportClasses() {
        return importClasses;
    }

    public synchronized void addAnonymousClass(ClassMeta classMeta) {
//        System.out.println("#################################################################################");
// 查找父节点
        Element element = classMeta.getRoot();
        String className = "";
        List<Element> parents = new ArrayList<>();
        while (!element.isRootElement()) {
            if (element.getName().equals("interface") || element.getName().equals("class")) {
//                System.out.println("path:" + element.getPath());
                parents.add(0, element);
            }
            element = element.getParent();
        }
//        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        for (int i = 0; i < parents.size(); i++) {
            Element parent = parents.get(i);
            String current = Utils.elementToString(Utils.selectElement(parent, "name"));
            if (Utils.isEmpty(current)) {
//            fix index
                List<Element> result = new ArrayList<>();
//                System.out.println("------------------------------------------------------------------------------------");
                Element preParent = parents.get(i - 1);
//                System.out.println("PreParent:" + preParent.getPath());
                findAllElements(preParent, result);
//                System.out.println("result.size():" + result.size());
//                System.out.println("------------------------------------------------------------------------------------");
                int index = -1;
                for (int j = 0; j < result.size(); j++) {
                    Element e = result.get(j);
                    if (e == parent) {
                        index = j + 1;
                        break;
                    }
                }
                assert index != -1;
                current = String.valueOf(index);
            }
            className = Utils.isEmpty(className) ? current : (className + "$" + current);
        }
        classMeta.setClassName(className);
    }

    private void findAllElements(Element element, List<Element> result) {
//        System.out.println("path:" + element.getPath());
        List<Element> elements = element.elements();
        for (Element next : elements) {
            if (next.getName().equals("interface") || next.getName().equals("class")) {
                String name = Utils.elementToString(Utils.selectElement(next, "name"));
                if (Utils.isEmpty(name)) {
                    result.add(next);
                }
            } else {
                findAllElements(next, result);
            }
        }
    }

    public void collectIfUnit() {
        //插桩
        Iterator<FunctionMeta> iterator = functionMetas.iterator();
        while (iterator.hasNext()) {
            FunctionMeta function = iterator.next();
            function.collectIfUnit();
//            System.out.println(function.getJvmFunctionSign());
//            //考虑比较简单的if语句
////            if () {        ---->               log if () { log
////            } else {       ---->               log } else { log
////            }              ---->               log } log
////            function.insertIfLog();
////            function.insertExitLog();
////            function.insertCatchLog();
////            function.insertEntryLog();
        }
    }


//    private String getParentClassName(Element element) {
//        return null;
//    }
}
