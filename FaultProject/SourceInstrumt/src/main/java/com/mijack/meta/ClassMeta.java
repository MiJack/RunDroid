package com.mijack.meta;

import com.mijack.JavaFileObject;
import com.mijack.Utils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mr.Yuan on 2017/3/27.
 */
public class ClassMeta {
    private String packageName;
    /**
     * xxx.xxx.xxx.XXXX($SSSS)
     */
    private String className;
    private String fileName;
    private Element classRoot;
    private List<FunctionMeta> functionMetas = new ArrayList<>();
    private JavaFileObject javaFileObject;

    public ClassMeta(Element classRoot, String packageName, JavaFileObject javaFileObject) {
        this.classRoot = classRoot;
        this.packageName = packageName;
        setJavaFileObject(javaFileObject);
        Iterator<Element> iterator = classRoot.elementIterator();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            switch (next.getName()) {
                case "name":
                    //判断是否为根节点
//                     当前结点向上查找所有的interface和class节点
                    Element element = classRoot;
                    while (!element.isRootElement()) {
                        //判断是否为interface或者class
                        if (element.getName().equals("interface")) {
                            String s = Utils.elementToString(Utils.selectElement(element,  "name"));
                            className = s + (Utils.isEmpty(className) ? "" : ("$" + className));
                        } else if (element.getName().equals("class")) {
                            String s = Utils.elementToString(Utils.selectElement(element,  "name"));
                            className = s + (Utils.isEmpty(className) ? "" : ("$" + className));
                        }
                        element = element.getParent();
                    }
                    break;
            }
        }
        //class is null
        if (Utils.isEmpty(className)){
            //匿名的内部类
            javaFileObject.addAnonymousClass(this);
        }
    }

    public void setClassName(String className) {
        assert  className!=null;
        this.className = className;
    }

    public Element getRoot() {
        return classRoot;
    }

    public List<FunctionMeta> getFunctionMetas() {
        return functionMetas;
    }

    public void addFunction(FunctionMeta functionMeta) {
        functionMeta.setClassMeta(this);
        functionMetas.add(functionMeta);
    }

    @Override
    public String toString() {
        return "class[" + packageName + " " + className + "](function" + functionMetas.size() + ")";
    }

    public String getClassName() {
        return packageName + "." + className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setJavaFileObject(JavaFileObject javaFileObject) {
        this.javaFileObject = javaFileObject;
    }

    public JavaFileObject getJavaFileObject() {
        return javaFileObject;
    }
}
