package com.mijack.meta;

import com.mijack.Utils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mr.Yuan
 * @since 2017/1/23.
 */
public class FunctionMeta {
    static final Pattern pattern = Pattern.compile("\\/\\*\\[name\\(\\)='(if|condition|then|else|elseif)'\\]");

    private String name;
    private String type;
    private String fullNameReturnType;
    private List<String> fullNameParameterList = new ArrayList<>();
    /**
     * 此处保留的是Element信息，方便回溯
     */
    private List<Element> rawParameterList;
    private Element functionRoot;
    private ClassMeta classMeta;
    public static final int IS_DEFAULT = 0;
    public static final int IS_PRIVATE = 1;
    public static final int IS_PUBLIC = 2;
    public static final int IS_PROTECTED = 3;
    private boolean isAbstract = false;
    private boolean isNative = false;
    private boolean isDefaultMethodImplementation = false;
    private boolean isFinal = false;
    private boolean isSynchronized = false;
    private boolean isTransient = false;
    private int methodRight = IS_DEFAULT;
    private boolean isStatic = false;
    private boolean isVolatile = false;
    private boolean isStrictfp = false;
    private List<String> genericArgs = new ArrayList<>();
    List<IfUnit> ifUnits;

    public FunctionMeta(Element functionRoot) {
        if (!functionRoot.getName().equals("function") && !functionRoot.getName().equals("constructor")) {
            throw new IllegalArgumentException();
        }
        this.functionRoot = functionRoot;
        rawParameterList = new ArrayList<>();
        ifUnits = new ArrayList<>();
        Iterator<Element> iterator = this.functionRoot.elementIterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            switch (element.getName()) {
                case "type":
                    assert type == null;
                    type = Utils.elementToString(element);
                    break;
                case "objectType":
                    assert type == null;
                    type = Utils.elementToString(element);
                    break;
                case "name":
                    name = Utils.elementToString(element);
                    break;
                case "specifier":
                    String specifier = Utils.elementToString(element);
                    setSpecifier(specifier);
                    break;
                case "parameter_list":
                    List<Element> parameters = Utils.selectElements(element, "parameter");
                    for (Element parameter : parameters) {
                        rawParameterList.add(parameter);
//                        //获取decl和name
//                        Element type = Utils.selectElement(parameter, "decl", "type");
//                        Element name = Utils.selectElement(parameter, "decl", "name");
//                        parameter_list.add(Utils.elementToString(type));
//                        parameter_list.add(Utils.elementToString(name));
                    }
                    break;
            }
        }
//        assert type != null;
//        assert Utils.isEmpty(type) == false;
    }

    public boolean isConstructor() {
        return functionRoot.getName().equals("constructor");
    }

    private void setSpecifier(String specifier) {
        switch (specifier) {
            case "abstract":
                isAbstract = true;
                break;
            case "default method implementation":
                isDefaultMethodImplementation = true;
                break;
            case "final":
                isFinal = true;
                break;
            case "synchronized":
                isSynchronized = true;
                break;
            case "transient":
                isTransient = true;
                break;
            case "native":
                isNative = true;
                break;
            case "private":
                methodRight = IS_PRIVATE;
                break;
            case "protected":
                methodRight = IS_PROTECTED;
                break;
            case "public":
                methodRight = IS_PUBLIC;
                break;
            case "static":
                isStatic = true;
                break;
            case "volatile":
                isVolatile = true;
                break;
            case "strictfp":
                isStrictfp = true;
                break;
        }
    }

    public Element getFunctionRoot() {
        return functionRoot;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Element> getRawParameterList() {
        return rawParameterList;
    }

    @Override
    public String toString() {
        return "com.mijack.meta.FunctionMeta{" + type + " " + name + " " + rawParameterList + '}';
    }


//	private void handleId(String pre, List<Unit> units) {
//		int i = 0;
//		for (Unit u : units) {
//			if (u instanceof IfUnit) {
//				i++;
//				String id = pre + "-if(" + i + ")-";
//				u.setId(id);
//			}
//		}
//	}

    public void setFullNameReturnType(String fullNameReturnType) {
        this.fullNameReturnType = fullNameReturnType;
    }

    public String getFullNameReturnType() {
        return fullNameReturnType;
    }


    public String functionSign() {
        //void android.app.Activity.onStart()
        StringBuilder sb = new StringBuilder();
        if (!isConstructor()) {
            sb.append(type);
        }
        sb.append(" ").append(name).append("(");
        for (int i = 0; rawParameterList != null && i < rawParameterList.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            Element parameter = rawParameterList.get(i);
            Element type = Utils.selectElement(parameter, "decl", "type");
            Element name = Utils.selectElement(parameter, "decl", "name");
            sb.append(Utils.elementToString(type)).append(" ").append(Utils.elementToString(name));
        }
        sb.append(")@").append(getClassMeta().getClassName());
        return sb.toString();
    }

    public String getJvmFunctionSign() {
        //void android.app.Activity.onStart()
        StringBuilder sb = new StringBuilder();
        if (!isConstructor()) {
            sb.append(fullNameReturnType).append(" ");
        }
        sb.append(getClassMeta().getClassName()).append(".")
                .append(name).append("(");
        for (int i = 0; fullNameParameterList != null && i < fullNameParameterList.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(fullNameParameterList.get(i));
        }
        sb.append(")");
        //append write sign
        return sb.toString();
    }


    public void setClassMeta(ClassMeta classMeta) {
        this.classMeta = classMeta;
    }

    public ClassMeta getClassMeta() {
        return classMeta;
    }

    public Element getBlockElement() {
        Iterator<Element> iterator = functionRoot.elementIterator();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            if ("block".equals(next.getName())) {
                return next;
            }
        }
        throw new IllegalStateException("找不到function节点下的block节点");
    }

    public void insertEntryLog() {
        if (isAbstract || isNative) {
            return;
        }
        //获取function节点下的一个block节点
        Element blockElement = getBlockElement();
        //block节点后面插入一条sout
        List elements = blockElement.elements();
        StringBuilder logStmt = new StringBuilder();
        if (blockElement.nodeCount() == 1) {
            //空函数调用Execute
            logStmt.append("com.mijack.Xlog.")
                    .append(isStatic ? "logStaticMethodExecute" : "logMethodExecute")
                    .append("(");
        } else {
            logStmt.append("com.mijack.Xlog.")
                    .append(isStatic ? "logStaticMethodEnter" : "logMethodEnter")
                    .append("(");
        }
        logStmt.append("\"").append(getJvmFunctionSign()).append("\"")
                .append(isStatic ? "" : ",this");
        for (int i = 0; rawParameterList != null && i < rawParameterList.size(); ) {
            Element element = rawParameterList.get(i);
            Element nameElement = Utils.selectElement(element, "decl", "name");
            logStmt.append(",").append(Utils.elementToString(nameElement));
            i = i + 1;
        }
        logStmt.append(");");
        //获取element的ContentList
        Element print = DocumentHelper.createElement("expr_stmt");
        print.setText(logStmt.toString());
        //判断是否是空函数
        if (blockElement.nodeCount() == 1) {
            Iterator nodeIterator = blockElement.nodeIterator();
            String block = Utils.elementToString(blockElement);
            int count = 0;
            for (int i = 0; i < block.length(); i++) {
                if (block.charAt(i) == '\n') {
                    count++;
                }
            }
            while (nodeIterator.hasNext()) {
                nodeIterator.next();
                nodeIterator.remove();
            }
            //如果count>1
            //将原来的串切割成两份
            if (count > 1) {
                int indexOf = block.indexOf("\n");
                String text1 = block.substring(0, indexOf + 1);
                String text2 = block.substring(indexOf + 1);
                blockElement.add(new DefaultText(text1));
                blockElement.add(print);
                blockElement.add(new DefaultText(text2));
            } else if (count == 1) {
                int indexOf = block.indexOf("\n");
                String text1 = block.substring(0, indexOf);
                String text2 = block.substring(indexOf);
                blockElement.add(new DefaultText(text1));
                blockElement.add(print);
                blockElement.add(new DefaultText(text2));
            } else {
//                count==0
                blockElement.add(new DefaultText("{"));
                blockElement.add(print);
                blockElement.add(new DefaultText("}"));
            }
        } else {
            elements.add(0, print);
        }
    }

    /**
     * 添加catch日志
     */
    public void insertCatchLog() {
        Element rootBlockElement = getBlockElement();
        int preNodeCount = rootBlockElement.nodeCount();
        if (preNodeCount == 0) {
            throw new IllegalStateException(getJvmFunctionSign() + "的xml异常");
        }
        if (rootBlockElement.nodeCount() == 1) {
            //空函数，无需实现
            return;
        }
        Element tryElement = new DefaultElement("try");
        tryElement.add(new DefaultText("try"));
        Element block = new DefaultElement("block");
        block.add(new DefaultText("{"));
        //Tip:从后往前删除列表，并将所有的element移至新的element,
        //!!!使用后项添加
        List<Node> list = new ArrayList();
        for (int i = preNodeCount - 1; i >= 0; i--) {
            if (i != 0 && i != preNodeCount - 1) {
                Node next = rootBlockElement.node(i);
                rootBlockElement.remove(next);
                list.add(0, next);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            block.add(list.get(i));
        }
        block.add(new DefaultText("}"));
        tryElement.add(block);
        //添加catch
        Element catchElement = new DefaultElement("catch");
        catchElement.add(new DefaultText("catch"));
        Element parameter_list = new DefaultElement("rawParameterList");
        parameter_list.add(new DefaultText("(Throwable throwable)"));
        catchElement.add(parameter_list);
        Element catchBlockElement = new DefaultElement("block");
        catchBlockElement.add(new DefaultText("{"));
        Element element = new DefaultElement("expr_stmt");
        element.add(new DefaultText(
                String.format("com.mijack.Xlog.%s(\"%s\"%s,throwable);throw throwable;",
                        isStatic ? "logStaticMethodExitWithThrowable" : "logMethodExitWithThrowable",
                        getJvmFunctionSign(),
                        isStatic ? "" : ",this")));
        catchBlockElement.add(element);
        catchBlockElement.add(new DefaultText("}"));
        catchElement.add(catchBlockElement);
        tryElement.add(catchElement);
        //将try添加到block中
        Node node = rootBlockElement.node(rootBlockElement.nodeCount() - 1);
        rootBlockElement.remove(node);
        rootBlockElement.add(tryElement);
        rootBlockElement.add(node);
    }

    /**
     * 添加退出日志
     */
    public void insertExitLog() {
        //找到当前方法中所有的return
        Element blockElement = getBlockElement();
        List<Element> list = Utils.findAllElement(blockElement, "return");
        Element logExit = new DefaultElement("expr_stmt");
        //判断是否为静态方法
        logExit.add(new DefaultText(
                String.format("com.mijack.Xlog.%s(\"%s\"%s);",
                        isStatic ? "logStaticMethodExit" : "logMethodExit",
                        getJvmFunctionSign(),
                        isStatic ? "" : ",this")));
        if (list.size() == 0) {
            //无return语句判断是否存在多个node
            if (blockElement.nodeCount() == 1) {
                //空方法不进行如何输出
                return;
            }
            //在最后一句前插入这条语句
            Node lastNode = null;
            Iterator<Node> nodeIterable = blockElement.nodeIterator();
            int i = 0;
            while (nodeIterable.hasNext()) {
                Node next = nodeIterable.next();
                if (i == blockElement.nodeCount() - 1) {
                    lastNode = next;
                    blockElement.remove(next);
                    break;
                }
                i++;
            }
            blockElement.add(logExit);
            blockElement.add(lastNode);
            return;
        }//特殊处理只有一个返回的方法
        if (list.size() == 1 && blockElement.elements().size() == 1) {
            //如果该方法是顺序结构，在程序的末尾插入日志
            blockElement.elements().add(0, logExit);
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Element returnElement = list.get(i);
            Element returnElementParent = returnElement.getParent();
            //获取在node list中的位置
            int indexOf = Utils.nodeIndex(returnElementParent, returnElement);
            // 将indexOf（含）以后的元素添加另一个列表中
            List<Node> nodes = new ArrayList<>();
            Iterator<Node> iterator = returnElementParent.nodeIterator();
            int temp = -1;
            while (iterator.hasNext()) {
                temp++;
                Node next = iterator.next();
                if (temp >= indexOf) {
                    iterator.remove();
                    if (!returnElementParent.remove(next)) {
                        next.detach();
                    }
                    if (temp != indexOf) {
                        nodes.add(next);
                    }
                }
            }
            if (!returnElementParent.remove(returnElement)) {
                returnElement.detach();
            }
            //创建block
            Element returnBlock = new DefaultElement("block");
            returnBlock.add(new DefaultText("{"));
            returnBlock.add(new DefaultText(
                    String.format("com.mijack.Xlog.%s(\"%s\",%s%d);",
                            isStatic ? "logStaticMethodExit" : "logMethodExit",
                            getJvmFunctionSign(), isStatic ? "" : "this,", (i + 1))));
            returnBlock.add(returnElement);
            returnBlock.add(new DefaultText("}"));
//            returnElementParent.elements().add(indexOf, returnBlock);
            returnElementParent.add(returnBlock);
            for (Node node : nodes) {
                returnElementParent.add(node);
            }
        }

    }

    public void addGenericIterator(String genericArg) {
        genericArgs.add(genericArg);
    }

    public boolean isGenericType(String type) {
        return genericArgs.contains(type);
    }

    public void setFullNameParameterList(List<String> fullNameParameterList) {
        this.fullNameParameterList = fullNameParameterList;
    }

    public List<String> getFullNameParameterList() {
        return fullNameParameterList;
    }

    public void insertIfLog() {
    }

    public void collectIfUnit() {
        String jvmFunctionSign = getJvmFunctionSign();
        List<Element> elements = new ArrayList<>();
        collectIfUnit(functionRoot, elements);
        if (elements.size() == 0) {
            return;
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------");
        System.out.println(jvmFunctionSign + ":" + elements.size());
        String functionPath = functionRoot.getPath();
        System.out.println("function path:" + functionPath);
        int c = 0;
        IfUnit ifUnit = null;
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            String path = element.getPath();
            String substring = path.substring(functionPath.length());
            IfType ifType = getIfElementType(element);

            if (ifType.equals(IfType.IF)) {
                IfUnit current= new IfUnit();
                current.setRootElement(element);
                //判断是否是嵌套的if

            }
            if (IfType.THEN.equals(ifType)) {
                //判断是否是当前的unit
                String rootPath = ifUnit.getRootPath();
                while (!path.startsWith(rootPath)) {
                    ifUnit = ifUnit.getParent();
                }
                ifUnit.setThenElement(element);
            }
            if (IfType.ELSE_IF.equals(ifType)) {
                //temp
            } else {

            }
            StringBuilder sb = new StringBuilder();
            if (ifType == IfType.THEN || ifType.equals(IfType.ELSE) || ifType.equals(IfType.ELSE_IF_THEN)) {
                for (int j = 0; j < c + 1; j++) {
                    sb.append("  ");
                }
            }
            sb.append(ifType);
            System.out.println(String.format("%-50s %s", sb.toString(), substring));
        }
    }

    enum IfType {
        ELSE, IF, ELSE_IF, THEN, ELSE_IF_THEN
    }

    private IfType getIfElementType(Element element) {
        if (element.getName() == "if") {
            if (element.getParent().getName() == "elseif") {
                return IfType.ELSE_IF;
            }
            return IfType.IF;
        }
        if (element.getName() == "then") {
            if (getIfElementType(element.getParent()).equals(IfType.ELSE_IF)) {
                return IfType.ELSE_IF_THEN;
            }
            return IfType.THEN;
        }
        if (element.getName() == "else") {
            return IfType.ELSE;
        } else {
            return null;
        }
    }

    private void collectIfUnit(Element functionRoot, List<Element> result) {
        Iterator<Element> iterator = functionRoot.elementIterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            if ("class".equals(element.getName())) {
                continue;
            }
            if ("if".equals(element.getName())) {
                result.add(element);
            }
            if ("else".equals(element.getName())) {
                result.add(element);
            }
            if ("then".equals(element.getName())) {
                result.add(element);
            }
            collectIfUnit(element, result);
        }
    }
}
