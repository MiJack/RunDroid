import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * Created by Mr.Yuan on 2017/3/27.
 */
public class XPathDemo {
    public static final String XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "\n" +
            "<bookstore>\n" +
            "\n" +
            "<book>\n" +
            "  <title lang=\"eng\">Harry Potter</title>\n" +
            "  <price>29.99</price>\n" +
            "</book>\n" +
            "\n" +
            "<book>\n" +
            "  <title lang=\"eng\">Learning XML</title>\n" +
            "  <class>39.95</class>\n" +
            "</book>\n" +
            "\n" +
            "</bookstore>";

    public static void main(String[] args) throws Exception {
//        String file ="F:\\IdeaProject\\FaultProject\\input\\java\\com\\ecnu\\billliao\\eventstimer\\MainActivity.java.xml";
//        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(new File(file));
//        XPathFactory xpathFactory = XPathFactory.newInstance();
//        XPath xpath = xpathFactory.newXPath();
//        XPathExpression expression =xpath.compile("//class");
//        NodeList nodelist = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
//        System.out.println(nodelist.getLength());
//        Element documentElement = document.getDocumentElement();
        Object[] a = new Object[]{1, 3, 1, "4sdfasd"};
        fun(a);
        fun2(new Object[]{a});
        System.out.println(boolean.class.getName());
    }

    public static void fun(Object... objects) {
        for (Object o : objects)
            System.out.println(o.getClass().getName());
    }

    public static void fun2(Object[] objects) {
        for (Object o : objects)
            System.out.println(o.getClass().getName());
    }
}
