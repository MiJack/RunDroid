package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.mijack.Utils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/17
 */
public class HistoryBuilder {

    public static final String OBJECT = "OBJECT";
    public static final String METHOD = "METHOD";
    public static final String FRAMEWORK = "FRAMEWORK";
    public static final String NULL = "NULL";
    public static final String BASIC_TYPE = "BASIC_TYPE";


    public static final String INVOKE = "INVOKE";
    public static final String INSTANCE = "INSTANCE";
    public static final String PARAM = "PARAM";
    public static final String RESULT = "RESULT";
    //    @Deprecated
    public static final String TRIGGER = "TRIGGER";
    public static final String TRIGGER_HANDLER = "TRIGGER_HANDLER";
    public static final String TRIGGER_THREAD = "TRIGGER_THREAD";
    public static final String TRIGGER_VIEW_LISTENER = "TRIGGER_VIEW_LISTENER";
    public static final String APPLICATION_LIFE_CYCLE = "APPLICATION_LIFE_CYCLE";

    private static final String NEXT_ACTION = "NEXT_ACTION";
    public static final List<String> MESSAGE_METHOD_SET;

    public static final List<String> PARCEL_METHOD_SET;
    public static final String[] SERVICE_LIFECYCLE_METHODS = new String[]{
            "android.os.IBinder %s.onBind(android.content.Intent)",
            "void %s.onCreate()",
            "void %s.onDestroy()",
            "void %s.onRebind(android.content.Intent)",
            "void %s.onStart(android.content.Intent,int)",
            "int %s.onStartCommand(android.content.Intent,int,int)",
            "boolean %s.onUnbind(android.content.Intent)",
    };
    public static final String[] ACTIVITY_LIFECYCLE_METHODS = new String[]{
            "void %s.onCreate(android.os.Bundle)",
            "void %s.onStart()",
            "void %s.onResume()",
            "void %s.onPause()",
            "void %s.onStop()",
            "void %s.onDestroy()",
    };

    static {
        MESSAGE_METHOD_SET = new ArrayList<>();
        MESSAGE_METHOD_SET.add("android.os.Message android.os.Message.obtain()");
        MESSAGE_METHOD_SET.add("void android.os.Message.recycle()");
        PARCEL_METHOD_SET = new ArrayList<>();
        PARCEL_METHOD_SET.add("android.os.Parcel android.os.Parcel.obtain()");
        PARCEL_METHOD_SET.add("void android.os.Parcel.recycle()");
    }

    private String applicationName;
    private String applicationId;
    private List<String> activityClasses;
    private List<String> serviceClasses;
    private GraphDatabaseService database;
    private String logFile;
    private String manifestFile;
    private String apkFile;

    public static void main(String[] args) throws Exception {
        Config config = Config.getInstance();
        if (!config.readConfig(args)){
             return;
         }
        LogProcess logProcess = new LogProcess(config.getLogFile());
        logProcess.process();
        logProcess.printDatabaseXml();
        HistoryBuilder builder = new HistoryBuilder(config.getLogFile(), config.getManifestFile(),config.getApkFile());
        builder.obtainNeo4jDataBase();
        builder.printDatabaseInfo();
        //清空数据库
        builder.clearDataBase();
        builder.printDatabaseInfo();
        //创建neo4j
        builder.createObjectGraph(logProcess);
        builder.printDatabaseInfo();
        builder.createMethodGraph(logProcess);
        //过滤一部分的message
        builder.printDatabaseInfo();
        //获取所有的Handler 子类
        builder.filterSomeParcelNodes();
        builder.filterSomeHandlers();
        builder.filterSomeMessageNodes();
        builder.printDatabaseInfo();
        builder.patchClickListener();
        builder.patchActivityLifecycle();
        builder.patchApplicationLifecycle();
        builder.patchHandlerMessage();
        builder.patchThreads();
        builder.patchRunOnUiThreads();
//        最后过滤0度节点
        builder.clearZeroDegreeNode();
        builder.printDatabaseInfo();
        builder.makeFramework();
        builder.makeShortName();
    }

    private void makeShortName() {
        try (Transaction transaction = database.beginTx()) {
            Consumer<Node> consumer = node -> {
                String methodSign = node.getProperty("methodSign").toString();
                String substring = methodSign.substring(0, methodSign.indexOf("("));
                int lastIndexOf = substring.lastIndexOf(".");
                String temp = substring.substring(0, lastIndexOf);
                lastIndexOf = temp.lastIndexOf(".");
                String shortName = substring.substring(lastIndexOf + 1) + "()";
                if (shortName.startsWith("DemoActivity.")) {
                    shortName = shortName.substring("DemoActivity.".length());
                }
                if (shortName.startsWith("Activity.")) {
                    shortName = shortName.substring("Activity.".length());
                }
                node.setProperty("shortName", shortName);
            };
            database.findNodes(Label.label(FRAMEWORK)).stream().forEach(consumer);
            database.findNodes(Label.label(METHOD)).stream().forEach(consumer);
            database.findNodes(Label.label(OBJECT)).stream().forEach(node -> {

                String className = node.getProperty("class").toString();
                int lastIndexOf = className.lastIndexOf(".");
                if (lastIndexOf >= 0) {
                    className = className.substring(lastIndexOf + 1);
                }
//                lastIndexOf =className.lastIndexOf("$");
//                if (lastIndexOf>=0){
//                    className=className.substring(lastIndexOf+1);
//                }
                node.setProperty("shortName", className);
            });
            transaction.success();
        }
    }

    private void makeFramework() {
        try (Transaction transaction = database.beginTx()) {
            database.execute("MATCH (n:FRAMEWORK) REMOVE n:METHOD");
            transaction.success();
        }
    }

    private void patchRunOnUiThreads() {
        try (Transaction transaction = database.beginTx()) {
            //考虑时间差
            String query =
                    "MATCH (m1:METHOD)-[:PARAM]->(o:OBJECT)<-[:INSTANCE]-(m2:METHOD)" +
                            " WHERE m1.methodSign ='void android.app.Activity.runOnUiThread(java.lang.Runnable)'" +
                            " CREATE (m1)-[:TRIGGER{type:'runOnUiThread'}]->(m2)";
            database.execute(query);
            transaction.success();
        }
    }

    private void patchHandlerMessage() {
        try (Transaction transaction = database.beginTx()) {
            Result result = database.execute("MATCH P=(SEND:METHOD)-[:PARAM]->(MSG:OBJECT)<-[:PARAM]-(HANDLE:METHOD)-[:INSTANCE]->(HANDLER:OBJECT)" +
                    " WHERE SEND.methodSign ='boolean android.os.Handler.enqueueMessage(android.os.MessageQueue,android.os.Message,long)'" +
                    " AND HANDLE.methodSign ='void android.os.Handler.dispatchMessage(android.os.Message)'" +
                    " RETURN SEND,HANDLE");
            while (result.hasNext()) {
                Map<String, Object> objectMap = result.next();
                Node send = (Node) objectMap.get("SEND");
                Node handle = (Node) objectMap.get("HANDLE");
                Relationship relationship = send.createRelationshipTo(handle, RelationshipType.withName(TRIGGER_HANDLER));
            }
            transaction.success();
        }
    }

    private void patchClickListener() {
        System.out.println("patchClickListener");
        try (Transaction transaction = database.beginTx()) {

            String delete = "MATCH p=(i:OBJECT)<-[:INSTANCE]-(n:FRAMEWORK)-[:PARAM]->(r:OBJECT) WHERE n.methodSign = 'void android.view.View.setOnClickListener(android.view.View$OnClickListener)' AND r.class IN ['com.android.internal.widget.ActionBarView$1','com.android.internal.widget.ActionBarView$2']   DETACH  DELETE p";
            database.execute(delete);
            //            List<String> listeners = SootPlugins.getViewClickListener();
//            for (String listener : listeners) {
            String queryWidget = "MATCH (listener)-[:PARAM]-(m:METHOD)-[:INSTANCE]->(widget) " +
                    "WHERE m.methodSign ='void android.view.View.setOnClickListener(android.view.View$OnClickListener)' " +
                    "RETURN DISTINCT(widget) AS widget";
//            Result result = database.execute(queryWidget);
            ResourceIterator<Node> iterator = database.execute(queryWidget).columnAs("widget");
            while (iterator.hasNext()) {
                Node widget = iterator.next();
                // 查询所有的void android.view.View.setOnClickListener(android.view.View$OnClickListener)
                String setViewListenerNodeQuery =
                        "MATCH (listener)<-[:PARAM]-(method:METHOD)-[:INSTANCE]->(widget) " +
                                "WHERE method.methodSign ='void android.view.View.setOnClickListener(android.view.View$OnClickListener)' " +
                                "   AND widget.hashcode='" + widget.getProperty("hashcode").toString() + "'" +
                                "   AND widget.version=" + widget.getProperty("version").toString() +
                                "   AND widget.pid=" + widget.getProperty("pid").toString() +
                                "   RETURN method,listener";
                System.out.println("setViewListenerNodeQuery:" + setViewListenerNodeQuery);
                Result result = database.execute(setViewListenerNodeQuery);
                List<Node> methods = new ArrayList<>();
                List<Node> listeners = new ArrayList<>();
                while (result.hasNext()) {
                    Map<String, Object> map = result.next();
                    listeners.add((Node) map.get("listener"));
                    methods.add((Node) map.get("method"));
                }
                assert methods.size() == listeners.size();
                //创建关系
                for (int index = 0; index < methods.size(); index++) {
                    Node method = methods.get(index);
                    Node listener = listeners.get(index);
                    if (listener.hasLabel(Label.label(NULL))) {
                        continue;
                    }
                    //获取对应的方法签名
                    String clazzName = listener.getProperty("class").toString();

                    String queryListener = null;
                    if (clazzName.equals("android.view.View$DeclaredOnClickListener")) {
                        //xml绑定的方法
//                        continue;
                        String queryMethodName =
                                "MATCH  (widget:OBJECT)<-[:INSTANCE]-(setMethod:METHOD)-[:PARAM]-(:OBJECT)-[:INSTANCE]-(resolveMethod:METHOD)-[:RESULT]-(result:OBJECT) " +
                                        "  WHERE setMethod.time=" + method.getProperty("time").toString() + " " +
                                        "   AND setMethod.methodSign = 'void android.view.View.setOnClickListener(android.view.View$OnClickListener)' " +
                                        "   AND resolveMethod.methodSign = 'java.lang.reflect.Method android.view.View$DeclaredOnClickListener.resolveMethod(android.content.Context,java.lang.String)' " +
                                        //widget 的限制
                                        "   AND widget.hashcode='" + widget.getProperty("hashcode").toString() + "'" +
                                        "   AND widget.version=" + widget.getProperty("version").toString() +
                                        "   AND widget.pid=" + widget.getProperty("pid").toString() +
                                        " RETURN result.method AS targetMethod";
                        System.out.println("queryMethodName:" + queryMethodName);
                        ResourceIterator<String> methodNameIterator = database.execute(queryMethodName).columnAs("targetMethod");
                        String methodName = null;
                        if (methodNameIterator.hasNext()) {
                            methodName = methodNameIterator.next();
                        }
                        assert Utils.isEmpty(methodName) != false;
                        assert methodNameIterator.hasNext() == false;

                        queryListener =
                                "MATCH (m1:METHOD)-[:INSTANCE]->(widget:OBJECT), (m2:METHOD)-[:PARAM]->(widget:OBJECT)" +
                                        " WHERE  m2.methodSign='" + methodName + "'";
                    } else {
                        queryListener =
                                "MATCH (m1:METHOD)-[:INSTANCE]->(widget:OBJECT)," +
                                        " (m1:METHOD)-[:PARAM]->(listener:OBJECT)," +
                                        " (m2:METHOD)-[:PARAM]->(widget:OBJECT)," +
                                        " (m2:METHOD)-[:INSTANCE]->(listener:OBJECT) " +
                                        " WHERE listener.class='" + clazzName + "'";
                    }

                    queryListener +=
                            "   and widget.hashcode='" + widget.getProperty("hashcode").toString() + "'" +
                                    "   and widget.version=" + widget.getProperty("version").toString() +
                                    "   and widget.pid=" + widget.getProperty("pid").toString();
                    queryListener += " and m2.time >  " + method.getProperty("time").toString();
                    //添加下一个时间的限制
                    if (index != methods.size() - 1) {
                        //需要限制时间上的维度
                        Node methodNext = methods.get(index + 1);
                        queryListener += " and m2.time < " + methodNext.getProperty("time").toString();
                    }
                    queryListener += " RETURN m1 , m2";
                    System.out.println("queryListener:" + queryListener);
                    Result methodResult = database.execute(queryListener);
                    while (methodResult.hasNext()) {
                        Map<String, Object> map = methodResult.next();
                        Node m1 = (Node) map.get("m1");
                        Node m2 = (Node) map.get("m2");
                        Relationship relationship = m1.createRelationshipTo(m2, RelationshipType.withName(TRIGGER_VIEW_LISTENER));
//                        relationship.setProperty("type", "setOnClickListener");
                    }
                }
//                // patch android.view.View$DeclaredOnClickListener
//                String query = "MATCH (instance:OBJECT)<-[:INSTANCE]-(method:METHOD)-[:PARAM]->(param:OBJECT)\n" +
//                        "WHERE param.class ='android.view.View$DeclaredOnClickListener'\n" +
//                        "RETURN method,instance,param";
//                Result queryResult = database.execute(query);

            }
            // 删除java.lang.reflect.Method android.view.View$DeclaredOnClickListener.resolveMethod(android.content.Context,java.lang.String)方法
            String deleteMethodStmt = "MATCH (m:METHOD)-[t:RESULT]->(object:OBJECT)" +
                    " WHERE m.methodSign='java.lang.reflect.Method android.view.View$DeclaredOnClickListener.resolveMethod(android.content.Context,java.lang.String)' " +
                    " DETACH DELETE m,object";
            System.out.println("deleteMethodStmt(resolveMethod):" + deleteMethodStmt);
            database.execute(deleteMethodStmt);
            transaction.success();
        }
    }

    private void filterSomeParcelNodes() {
        try (Transaction transaction = database.beginTx()) {
//            //删除带有目标descriptor的BinderProxy
            //todo 提取Descriptor到文件
            database.execute(
                    "MATCH p=(m)-[r]->(o)" +
                            " WHERE type(r) IN ['INSTANCE','RESULT', 'PARAM']" +
                            " AND o.class='android.os.BinderProxy'" +
                            " AND o.descriptor IN [" +
                            "'miui.security.ISecurityManager'," +
                            "'android.view.IWindowManager'," +
                            "'android.view.IGraphicsStats'," +
                            "'android.content.IContentProvider'," +
                            "'android.app.INotificationManager'," +
                            "'android.view.accessibility.IAccessibilityManager'," +
                            "'android.hardware.input.IInputManager'," +
                            "'com.android.internal.view.IInputMethodManager'," +
                            "'IMountService'," +
                            "'android.view.IWindowSession'," +
                            "'android.net.IConnectivityManager'," +
                            "'android.app.IActivityManager'," +
                            "'android.content.pm.IPackageManager'," +
                            "'android.hardware.display.IDisplayManager'," +
                            "'com.miui.whetstone.IWhetstone',''" +
                            "  ] DELETE r,o");
            database.execute(
                    "MATCH (m:METHOD)" +
                            " WHERE m.methodSign='boolean android.os.BinderProxy.transact(int,android.os.Parcel,android.os.Parcel,int)'" +
                            " WITH m" +
                            " MATCH (m)-[r]-()" +
                            " WITH m ,collect(DISTINCT type(r)) AS types" +
                            " WHERE NOT any(x IN types WHERE x='INSTANCE')" +
                            " DETACH DELETE m");
            //所有的BinderProxy 作为
            Result result = database.execute(
                    "MATCH (m:METHOD)-[r]->(n:OBJECT)" +
                            "WHERE n.class='android.os.Parcel' AND type(r) IN ['INSTANCE','RESULT', 'PARAM']" +
                            "WITH n,collect(m) AS methods " +
                            "WHERE  ALL" +
                            "(x IN methods WHERE x.methodSign IN" +
                            "['android.os.Parcel android.os.Parcel.obtain()'," +
                            "'android.os.Parcel android.os.Parcel.obtain(int)'," +
                            "'android.os.Parcel android.os.Parcel.obtain(long)'," +
                            "'void android.os.Parcel.recycle()'])" +
                            "RETURN n  ,methods"
            );
            Set<Long> ids = new HashSet<>();
            while (result.hasNext()) {
                Map<String, Object> map = result.next();
                Node parcel = (Node) map.get("n");
                List<Node> methods = (List<Node>) map.get("methods");
                ids.add(parcel.getId());
                for (Node method : methods) {
                    ids.add(method.getId());
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("match (n) where id(n) in [");
            int index = 0;
            Iterator<Long> iterator = ids.iterator();
            while (iterator.hasNext()) {
                Long next = iterator.next();
                if (index != 0) {
                    sb.append(",");
                }
                sb.append(next);
                index++;
            }
            sb.append("] DETACH DELETE n");
            System.out.println(sb.toString());
            database.execute(sb.toString());
            transaction.success();
        }
    }

    private void filterSomeHandlers() {
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("filterSomeHandlers");
        try (Transaction transaction = database.beginTx()) {
            StringBuilder query = new StringBuilder();
            query.append("match (o:OBJECT),(m:METHOD)-[:INSTANCE]->(o) ")
                    .append("where m.methodSign in ").append("[\"void android.os.Handler.dispatchMessage(android.os.Message)\" ,")
                    .append("\"boolean android.os.Handler.enqueueMessage(android.os.MessageQueue,android.os.Message,long)\"] ")
                    .append("and not (o.class in [");
            List<String> handlers = SootPlugins.getHandlers();
            for (int i = 0; i < handlers.size(); i++) {
                if (i != 0) {
                    query.append(",");
                }
                query.append("\"").append(handlers.get(i)).append("\"");
            }
            query.append("]) return distinct(o.class) as handler");
            System.out.println(query.toString());
            Result result = database.execute(query.toString());
            ResourceIterator<String> iterator = result.columnAs("handler");
            List<String> systemHandler = new ArrayList<>();
//            systemHandler.addAll(Arrays.asList(HANDLER_CLASSES));
            while (iterator.hasNext()) {
//                Node handler = iterator.next();
                String handlerClass = iterator.next();//handler.getProperty("class").toString();
                systemHandler.add(handlerClass);
            }
            systemHandler.add("android.os.Handler");
            StringBuilder sb = new StringBuilder();
            StringBuilder classes = new StringBuilder();
            for (int i = 0; i < systemHandler.size(); i++) {
                if (i != 0) {
                    sb.append(",");
                    classes.append(",");
                }
                String handler = systemHandler.get(i);
                sb
                        .append("\"").append("void ").append(handler).append(".dispatchMessage(android.os.Message)").append("\",")
                        .append("\"").append("void ").append(handler).append(".handleMessage(android.os.Message)").append("\",")
                        .append("\"").append("boolean ").append(handler).append(".enqueueMessage(android.os.MessageQueue,android.os.Message,long)").append("\"");
                classes.append("\"").append(handler).append("\"");
            }
            System.out.println(sb.toString());
            System.out.println(classes.toString());
            String deleteStmt =
                    "MATCH (handler:OBJECT)<-[:INSTANCE]-(m1:METHOD)-[:PARAM]->(msg:OBJECT)<-[r]-(m2:METHOD)" +
                            "WHERE m1.methodSign IN     " +
                            " [" + sb.toString() + " ]" +
                            " AND handler.class IN [" + classes + "]" +
                            " AND (" +
                            "  (type(r)='INSTANCE' AND m2.methodSign= 'void android.os.Message.recycle()' )" +
                            "  OR " +
                            "  (type(r)='RESULT'  AND m2.methodSign='android.os.Message android.os.Message.obtain()')" +
                            " )" +
                            "DETACH DELETE m2";
            System.out.println("deleteStmt:\n" + deleteStmt);
            System.out.println();
            database.execute(deleteStmt);
            String deleteStmt2 =
                    "MATCH (handler:OBJECT)<-[:INSTANCE]-(m1:METHOD)-[:PARAM]->(msg:OBJECT)" +
                            " WHERE m1.methodSign IN " +
                            " [" + sb.toString() + " ]" +
                            " AND handler.class IN [" + classes + "]" +
                            "DETACH DELETE msg";
            System.out.println(deleteStmt2);
            database.execute(deleteStmt2);
            String deleteStmt3 =
                    "MATCH (handler:OBJECT)<-[:INSTANCE]-(m1:METHOD)" +
                            " WHERE m1.methodSign IN  " +
                            " [" + sb.toString() + " ]" +
                            " AND handler.class IN [" + classes + "]" +
                            "DETACH DELETE m1";
            System.out.println(deleteStmt3);
            database.execute(deleteStmt3);
            String deleteStmt4 =
                    "MATCH (handler:OBJECT)" +
                            " WHERE handler.class IN [" + classes + "]" +
                            " DETACH DELETE handler";
            System.out.println(deleteStmt4);
            database.execute(deleteStmt4);


            transaction.success();
        }
    }

    private void filterSomeMessageNodes() {
        //查询所有的object
        //查询所有的调用
        try (Transaction transaction = database.beginTx()) {
            //todo 修改删除逻辑如下：对于Message的调用，如recycle何obtain的调用者是否为应用方法
            String stmt1 = "MATCH (n:OBJECT) RETURN n, n.pid AS pid,n.hashcode AS hashcode,  n.version AS version";
            Result objectResult = database.execute(stmt1);
            while (objectResult.hasNext()) {
                Map<String, Object> next = objectResult.next();
                //获取所有的object的调用情况
                Node node = (Node) next.get("n");
                String pid = next.get("pid").toString();
                String hashcode = next.get("hashcode").toString();
                String version = next.get("version").toString();
                String stmt2 = "MATCH  (m:METHOD)-[r]->(n1:OBJECT)WHERE" +
                        " n1.pid =%s and n1.hashcode='%s' and n1.version =%s and n1.class='android.os.Message'" +
                        "RETURN m,r,n1";
                int countInstance = 0;
                int countParam = 0;
                int countResult = 0;
                Result result = database.execute(String.format(stmt2, pid, hashcode, version));
                while (result.hasNext()) {
                    Map<String, Object> objectMap = result.next();
                    Relationship relationshipType = (Relationship) objectMap.get("r");
                    String typename = relationshipType.getType().name();
                    if (INSTANCE.equals(typename)) {
                        countInstance++;
                    } else if (PARAM.equals(typename)) {
                        countParam++;
                    } else if (RESULT.equals(typename)) {
                        countResult++;
                    } else {
                        System.out.println("find illegal relationship in query " + typename);
                    }
                }
//                System.out.println(String.format(
//                        "object{pid:%s,hashcode:%s,version:%s}:countInstance(%d),countParam(%d),countResult(%d)", pid, hashcode, version, countInstance, countParam, countResult));
                //判断是不是message
//                if ("android.os.Message".equals(node.getProperty("class"))) {
                String query = String.format(
                        "MATCH p= (m:METHOD)-[r]->(i:OBJECT)" +
                                " WHERE i.hashcode='%s' and i.pid=%s and i.version=%s and type(r) in ['INSTANCE','RESULT', 'PARAM'] return m.methodSign as methodSign",
                        hashcode, pid, version);
                Result execute = database.execute(query);
                List<String> methods = new ArrayList<>();
                while (execute.hasNext()) {
                    String methodSign = execute.next().get("methodSign").toString();
                    methods.add(methodSign);
                }
                if (methods.size() <= 2 && Utils.isSubSet(methods, MESSAGE_METHOD_SET)) {
                    //判断是否是子集
                    //删除所有的边和点
                    deleteNode(node);
                }
//                }
            }
            // todo 删除没有message 作为result的Message.obtain()
            Result result = database.execute("MATCH (m:METHOD) WHERE m.methodSign ='android.os.Message android.os.Message.obtain()' RETURN m");
            ResourceIterator<Node> iterator = result.columnAs("m");
            while (iterator.hasNext()) {
                Node method = iterator.next();
                Iterable<Relationship> relationships = method.getRelationships(Direction.OUTGOING, RelationshipType.withName(RESULT));
                int count = relationships != null ? Utils.count(relationships.iterator()) : 0;
                if (count == 0) {
                    String delete = "MATCH (m:METHOD) WHERE id(m) =" + method.getId() + " DETACH DELETE m";
                    System.out.println("delete:" + delete);
                    database.execute(delete);
                }
            }
            transaction.success();
        }
    }

    private void deleteNode(Node node) {
        Iterator<Relationship> incomingIterator = node.getRelationships(Direction.INCOMING).iterator();
        while (incomingIterator.hasNext()) {
            Relationship relationship = incomingIterator.next();
            Node startNode = relationship.getStartNode();
            relationship.delete();
            if (startNode.getDegree() == 0) {
                startNode.delete();
            }
        }
        Iterator<Relationship> outgoingIterator = node.getRelationships(Direction.OUTGOING).iterator();
        while (outgoingIterator.hasNext()) {
            Relationship relationship = outgoingIterator.next();
            Node endNode = relationship.getEndNode();
            relationship.delete();
            if (endNode.getDegree() == 0) {
                endNode.delete();
            }
        }
    }

    private void printDatabaseInfo() {
        System.out.println("#########################################################################################");
        //node count
        try (Transaction transaction = database.beginTx()) {
            database.getAllLabels().stream()
                    .map(label -> label.name())
                    .forEach(name -> {
//                String name = label.name();
                        String query = "MATCH (n:" + name + ") RETURN count(n)";
                        Result result = database.execute(query);
                        while (result.hasNext()) {
                            Iterator<Map.Entry<String, Object>> iterator = result.next().entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, Object> next = iterator.next();
                                System.out.println("Label(" + name + ") count:" + next.getValue());
                            }
                        }
                    });
            database.getAllRelationshipTypes().stream().map(relationshipType -> relationshipType.name())
                    .forEach(name -> {
                        String query = "MATCH p=()-[r:" + name + "]->() RETURN count(p)";
                        Result result = database.execute(query);
                        while (result.hasNext()) {
                            Iterator<Map.Entry<String, Object>> iterator = result.next().entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, Object> next = iterator.next();
                                System.out.println("Relationship(" + name + ") count:" + next.getValue());
                            }
                        }
                    });
            transaction.success();
        }
        System.out.println("#########################################################################################");
    }

    private void obtainNeo4jDataBase() {
        GraphDatabaseFactory factory = new GraphDatabaseFactory();
        String folder = logFile.substring(0, logFile.indexOf("."));
        database = factory.newEmbeddedDatabase(new File(folder));
    }

    //
//    private void filterMessages() {
//        //统计每一个Message的使用情况
//        try (Transaction transaction = database.beginTx()) {
//            String query = "MATCH (n:OBJECT) where n.class='android.os.Message' RETURN n";
//            Result execute = database.execute(query);
//            while (execute.hasNext()){
//                Map<String, Object> next = execute.next();
//                Node n = (Node) next.get("n");
//                //查询这个node相关的relationship
////                │{"arg2":"0","what":"0","obj":"│
////│null","arg1":"0","hashcode":"1│
////│02404186","class":"android.os.│
////│Message","version":"1","target│
////│":"null"}
//            }
//            transaction.success();
//        }
//    }
//
    private void patchApplicationLifecycle() {
        try (Transaction transaction = database.beginTx()) {
            //筛选所有的pid 和ThreadName
            List pids = getPids();
            for (Object pid : pids) {
//                筛选所有的method
                String query = "MATCH (m:METHOD) WHERE m.pid = " + pid + " AND m.threadName =~'main.*' RETURN m";
                Result execute = database.execute(query);
                ResourceIterator<Object> iterator = execute.columnAs("m");
                Node pre = null;
                while (iterator.hasNext()) {
                    Node next = (Node) iterator.next();
                    if (pre == null) {
                        pre = next;
                        continue;
                    }
                    //检查是否在invoke的入度
                    int degree = next.getDegree(RelationshipType.withName(INVOKE), Direction.INCOMING);
                    if (degree != 0) {
                        continue;
                    }
                    //判断是否可以添加到NEXT_ACTION的事件
                    pre.createRelationshipTo(next, RelationshipType.withName(NEXT_ACTION));
                    pre = next;
                }
            }
            transaction.success();
        }
    }

    private void patchThreads() {
        //找到所有的Thread
//        MATCH (n:METHOD)-[r:]->(p)where n.methodSign="void java.lang.Thread.start()" RETURN p
        try (Transaction transaction = database.beginTx()) {
            String stmt1 = String.format(
                    "MATCH (n:METHOD)-[r:INSTANCE]->(p) WHERE n.methodSign=\"%s\" RETURN distinct(p) as thread", "void java.lang.Thread.start()");
            System.out.println(stmt1);
            Result result = database.execute(stmt1);
            String queryTemplate = "MATCH (m:METHOD)-[:INSTANCE]->(i:OBJECT)" +
                    " WHERE i.hashcode ='%s' and i.version = %s and i.pid = %s" +
                    " and m.methodSign in ['%s','%s'] return m order by m.id limit 1";
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node thread = (Node) row.get("thread");
                String hashcode = thread.getProperty("hashcode").toString();
                String className = thread.getProperty("class").toString();
                String version = thread.getProperty("version").toString();
                String pid = thread.getProperty("pid").toString();
                //查询最早的start()方法
                String nativeStartMethod = "void java.lang.Thread.start()";
                String classStartMethod = "void " + className + ".start()";
                String startMethodQuery = String.format(queryTemplate, hashcode, version, pid, nativeStartMethod, classStartMethod);
                System.out.println(startMethodQuery);
                Result startQuery = database.execute(startMethodQuery);
                Node startNode = null;
                if (startQuery.hasNext()) {
                    Map<String, Object> next = startQuery.next();
                    startNode = (Node) next.get("m");
                }
                assert startNode != null;
                String nativeRunMethod = "void java.lang.Thread.run()";
                String classRunMethod = "void " + className + ".run()";
                String runMethodQuery = String.format(queryTemplate, hashcode, version, pid, nativeRunMethod, classRunMethod);
                System.out.println(runMethodQuery);
                Result runQuery = database.execute(runMethodQuery);
                Node runNode = null;
                if (runQuery.hasNext()) {
                    Map<String, Object> next = runQuery.next();
                    runNode = (Node) next.get("m");
                }
                if (runNode != null) {
                    //创建trigger
                    Relationship relationship = startNode.createRelationshipTo(runNode, RelationshipType.withName(TRIGGER_THREAD));
                }

            }
            database.execute("MATCH p=(m:FRAMEWORK)-[:INSTANCE]->(o:OBJECT) WHERE o.class='de.robv.android.xposed.XSharedPreferences$1' DETACH DELETE p");
            transaction.success();
        }
    }

    private void clearZeroDegreeNode() {
        try (Transaction transaction = database.beginTx()) {
            database.getAllNodes().stream().filter(node -> node.getDegree() == 0).forEach(Node::delete);
            transaction.success();
        }
    }

    private void createObjectGraph(LogProcess logProcess) {
        try (Transaction transaction = database.beginTx()) {
            ObjectInstancePool.getPool()
                    .values().stream()
                    .flatMap(longListMap -> longListMap.values().stream())
                    .flatMap(List::stream)
                    .sorted()
                    .forEach(objectInstance -> {
//                        //  创建节点
//                        System.out.println("create node:"
//                                + " pid:" + objectInstance.getPid()
//                                + " hashcode:" + objectInstance.getObjectId()
//                                + " version:" + objectInstance.getVersion());
                        Node node = database.createNode(Label.label(OBJECT));
                        node.setProperty("class", objectInstance.getClassName());
                        node.setProperty("version", objectInstance.getVersion());
                        node.setProperty("pid", objectInstance.getPid());
                        ContainerNode rawJsonNode = (ContainerNode) objectInstance.getRawJsonNode();
                        if (rawJsonNode.isObject()) {
                            Iterator<Map.Entry<String, JsonNode>> fields = rawJsonNode.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> entry = fields.next();
                                node.setProperty(entry.getKey(), entry.getValue().asText());
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                    });
            transaction.success();
        }

    }


    private void patchActivityLifecycle() {
        System.out.println("patchActivityLifecycle");
        try (Transaction transaction = database.beginTx()) {
            //获取所有的pid
            List pids = getPids();
            for (Object pid : pids) {
                StringBuilder sb = new StringBuilder("MATCH (m:METHOD) where m.methodSign in [");
                List<String> classes = new ArrayList<>();
                classes.addAll(activityClasses);
                classes.add("android.app.Activity");

                boolean first = true;

                for (int i = 0; i < classes.size(); i++) {
                    String clazz = classes.get(i);
                    for (String method : ACTIVITY_LIFECYCLE_METHODS) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append("'").append(String.format(method, clazz)).append("'");
                    }
                }
                sb.append("] AND m.pid=").append(pid).append(" return m");
                System.out.println(sb.toString());
                Result result = database.execute(sb.toString());
                ResourceIterator<Node> methods = result.columnAs("m");
                Node pre = null;
                while (methods.hasNext()) {
                    Node next = methods.next();
                    if (pre == null) {
                        pre = next;
                        continue;
                    }
                    String sizeShortestPath = "MATCH (m) ,(n)" +
                            " where id(m)=%d and id(n)=%d return size(shortestPath((m)-[:INVOKE]->(n))) as size";
                    Result execute = database.execute(String.format(sizeShortestPath, pre.getId(), next.getId()));
                    if (execute.hasNext()) {
                        ResourceIterator<Object> resourceIterator = execute.columnAs("size");
                        if (resourceIterator.hasNext()) {
                            Object o = resourceIterator.next();
                            System.out.println(o);
                            if (o == null) {
                                pre.createRelationshipTo(next, RelationshipType.withName(APPLICATION_LIFE_CYCLE));
                                pre = next;
                            }
                        }
                        assert execute.hasNext() == false;
                    }
                }
            }
            transaction.success();
        }
    }

    private List getPids() {
        String queryPid = "MATCH (o) WHERE exists (o.pid)RETURN DISTINCT(o.pid) AS pid";
        Result pidResult = database.execute(queryPid);
        ResourceIterator<Object> iterator = pidResult.columnAs("pid");
        List pids = new ArrayList<>();
        while (iterator.hasNext()) {
            pids.add(iterator.next());
        }
        return pids;
    }

    public void clearDataBase() {
        try (Transaction transaction = database.beginTx()) {
            //删除关系
            database.execute("MATCH (n) DETACH DELETE n");
            //删除node
            transaction.success();
        }
    }

    public HistoryBuilder(String logFile, String manifestFile, String apkFile) {
        this.logFile = logFile;
        this.manifestFile = manifestFile;
        this.apkFile = apkFile;

        SootPlugins.getInstance().analyzeApk(apkFile);
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new File(manifestFile));
            Element manifest = document.getRootElement();
            applicationId = manifest.attributeValue("package");
            Element application = manifest.element("application");
            applicationName = fixClassName(application.attributeValue("name"), applicationId);

            activityClasses = new ArrayList<>();
            List<Element> activityElements = application.elements("activity");
            for (Element activityElement : activityElements) {
                activityClasses.add(fixClassName(activityElement.attributeValue("name"), applicationId));
            }
            serviceClasses = new ArrayList<>();
            List<Element> serviceElements = application.elements("service");
            for (Element serviceElement : serviceElements) {
                serviceClasses.add(fixClassName(serviceElement.attributeValue("name"), applicationId));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private String fixClassName(String className, String applicationId) {
        if (className == null) {
            return "";
        }
        return className.startsWith(".") ? (applicationId + className) : className;
    }

    public void createMethodGraph(LogProcess logProcess) {
        try (Transaction transaction = database.beginTx()) {
            for (Map.Entry<Long, ProcessLogDatabase> entry : logProcess.getDatabaseMap().entrySet()) {
                ProcessLogDatabase database = entry.getValue();
                Long pid = entry.getKey();
                database.toNode(this);
            }
            transaction.success();
        }
    }


    public Node queryObjectNode(GraphDatabaseService database, long pid, long objectId, int version) {
        String query = "MATCH (n:OBJECT) WHERE n.pid=%d  AND n.hashcode='%d' AND n.version = %d RETURN n";
        String queryStmt = String.format(query, pid, objectId, version);
//        System.out.println(queryStmt);
        Result result = database.execute(queryStmt);
        while (result.hasNext()) {
            Map<String, Object> objectMap = result.next();
            return (Node) objectMap.get("n");
        }
        return null;
    }

    public GraphDatabaseService getDatabase() {
        return database;
    }

}
