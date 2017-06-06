package com.mijack;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Mr.Yuan on 2017/4/6.
 */
public class Utils {
    public static String elementToString(Element element) {
        return elementToString(element, (e) -> true, new StringBuilder()).toString();
    }

    public static String elementToString(Element element, Predicate<Node> predicate) {
        return elementToString(element, predicate, new StringBuilder()).toString();
    }

    private static StringBuilder elementToString(Element elements, Predicate<Node> predicate, StringBuilder stringBuilder) {
        if (elements == null) {
            return stringBuilder;
        }
        Iterator<Node> iterator = elements.nodeIterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (!predicate.test(node)) {
                continue;
            }
            if (node instanceof Element) {
                stringBuilder = elementToString((Element) node, predicate, stringBuilder);
            } else {
                stringBuilder.append(node.getText());
            }
        }
        return stringBuilder;
    }


    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static <T> boolean isEmpty(T... collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static Element selectElement(Element element, String... names) {
        List<Element> elements = selectElements(element, names);
        return elements.isEmpty() ? null : elements.get(0);
    }

    public static List<Element> selectElements(Element element, String... names) {
        List<Element> result = new ArrayList<>();
        if (element != null && names != null && names.length > 0) {
            List<Element> input = Arrays.asList(element);
            for (String name : names) {
                result = new ArrayList<>();
                for (Element current : input) {
                    Iterator<Element> iterator = current.elementIterator();
                    while (iterator.hasNext()) {
                        Element next = iterator.next();
                        if (!Utils.isEmpty(next.getName()) && next.getName().matches(name)) {
                            result.add(next);
                        }
                    }
                }
                if (result.isEmpty()) {
                    return result;
                } else {
                    input = new ArrayList<>();
                    input.addAll(result);
                }
            }
        }
        return result;

    }

    public static void makeSureFile(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * todo 使用DFS进行遍历
     *
     * @param blockElement
     * @param name
     * @return
     */
    public static List<Element> findAllElement(Element blockElement, String name) {
        List<Element> result = new ArrayList<>();
        if (isEmpty(name)) {
            return result;
        }
        Stack<Element> toSearch = new Stack<>();
        Set<Element> hasSearch = new HashSet<>();
        if (name.equals(blockElement.getName())) {
            result.add(blockElement);
        }
        hasSearch.add(blockElement);
        toSearch.add(blockElement);
        while (!toSearch.isEmpty()) {
            //取出对应栈顶元素
            Element peek = toSearch.peek();
            List<Element> elements = peek.elements();
            int i = 0;
            for (; elements != null && i < elements.size(); i++) {
                Element element = elements.get(i);
                if (!hasSearch.contains(element)) {
                    if (name.equals(element.getName())) {
                        result.add(element);
                    }
                    toSearch.add(element);
                    hasSearch.add(element);
                    break;
                }
            }
            if (elements == null || i == elements.size()) {
                toSearch.pop();
            }
        }
        return result;
    }

    public static int nodeIndex(Element parent, Element target) {
        int index = -1;
        Iterator<Node> iterator = parent.nodeIterator();
        while (iterator.hasNext()) {
            index++;
            Node node = iterator.next();
            if (node == target) {
                return index;
            }
        }
        return -1;
    }

    public static void close(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static int length(String s) {
        return s == null ? 0 : s.length();
    }

    public static <T> int length(T... array) {
        return array == null ? 0 : array.length;
    }


    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) != -1;
    }

    public static <T> int indexOf(T[] array, T value) {
        if (isEmpty(array)) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }

    private static <T> boolean equals(T obj, T value) {
        if (obj == null && value == null) {
            return true;
        } else if (obj == null || value == null) {
            return false;
        } else {
            return obj.equals(value);
        }
    }

    public static int whichIsLonger(String s1, String s2) {
        int l1 = Utils.length(s1);
        int l2 = Utils.length(s2);
        return l1 == l2 ? 0 : (l1 < l2 ? -1 : 1);
    }

    public static String[] splitToArray(String source, String s) {
        return source.contains(s) ? source.split(s) : new String[]{source};
    }

    public static void writeXml(Document document, String xmlFile) {
        XMLWriter xmlWriter = null;
        try {
            FileWriter writer = new FileWriter(xmlFile, false);
            OutputFormat outputFormat = new OutputFormat();
            // 设置换行 为false时输出的xml不分行
            outputFormat.setNewlines(true);
            // 生成缩进
            outputFormat.setIndent(true);
            // 指定使用tab键缩进
            outputFormat.setIndent("\t");
            xmlWriter = new XMLWriter(writer, outputFormat);
            xmlWriter.write(document);
            xmlWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                xmlWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> boolean isSubSet(Collection<T> sub, Collection<T> target) {
        if (target == null) {
            return false;
        }
        if (sub == null) {
            return true;
        }
        Iterator<T> iterator = sub.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (!target.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public static int count(Iterator iterator) {
        int result = 0;
        while (iterator!=null&&iterator.hasNext()) {
            iterator.next();
            result++;
        }
        return result;
    }
}
