package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.List;

import static com.mijack.logcatmodel.HistoryBuilder.*;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class LogObject {
    private List<LogObject> invoke = new ArrayList<>();
    private JsonNode enterNode;
    private JsonNode exitNode;

    private long id;
    private long time;
    private long duration;
    private String processName;
    private String threadName;
    private long pid;
    private long hookId = -1;
    private int returnIndex = Integer.MIN_VALUE;
    private boolean isStatic;
    private String methodSign;
    private boolean end = true;
    private ObjectInstance objectInstance;
    private List<ObjectInstance> params;
    private ObjectInstance throwable;
    private ObjectInstance result;
    static int count = 0;

    public LogObject() {
        this.id = count++;
    }

    public void addInvoke(LogObject logObject) {
        invoke.add(logObject);
    }


    public void setTime(long time) {
        this.time = time;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setMethodSign(String methodSign) {
        this.methodSign = methodSign;
    }

    public void setHookId(long hookId) {
        this.hookId = hookId;
    }

    public void setReturnIndex(int returnIndex) {
        this.returnIndex = returnIndex;
    }

    public void setMethodType(String methodType) {
        isStatic = methodType.contains("static");
    }

    public boolean isStatic() {
        return isStatic;
    }

    public long getTime() {
        return time;
    }

    public void setEnterNode(JsonNode enterNode) {
        this.enterNode = enterNode;
        // 设置ObjectInstance
        if (!isStatic()) {
            setObjectInstance(ObjectInstancePool.obtain((ContainerNode) enterNode.get("instance"), this, ObjectInstancePool.Type.INSTANCE));
        }
        ArrayNode params = (ArrayNode) enterNode.get("params");
        List<ObjectInstance> array = new ArrayList<>();
        for (int i = 0; params != null && i < params.size(); i++) {
            JsonNode param = params.get(i);
            if (param == null || param instanceof NullNode) {
                array.add(ObjectInstancePool.obtainNull());
            } else {
                array.add(ObjectInstancePool.obtain((ContainerNode) param, this, ObjectInstancePool.Type.PARAM));
            }
        }
        setParams(array);
    }

    public JsonNode getEnterNode() {
        return enterNode;
    }

    public JsonNode getExitNode() {
        return exitNode;
    }

    public void setExitNode(JsonNode objectNode) {
        this.exitNode = objectNode;
        // 设置ObjectInstance
        setThrowable(ObjectInstancePool.obtain((ContainerNode) objectNode.get("throwable"), this, ObjectInstancePool.Type.THROWABLE));
        JsonNode result = objectNode.get("result");
        if (result == null || result instanceof NullNode) {
        } else if (result instanceof ContainerNode) {
            ContainerNode containerNode = (ContainerNode) result;
            setResult(ObjectInstancePool.obtain(containerNode, this, ObjectInstancePool.Type.RETURN));
        }
    }

    public Element toElement() {
        DefaultElement element = new DefaultElement("log");
        element.addAttribute("hashcode", String.valueOf(hashCode()));
        element.addAttribute("methodSign", methodSign);
        for (LogObject logObject : invoke) {
            element.add(logObject.toElement());
        }
        return element;
    }

    public Node toNode(HistoryBuilder historyBuilder) {
        LogObject log = this;
        GraphDatabaseService database = historyBuilder.getDatabase();
        Node method = database.createNode(Label.label(METHOD));
        method.setProperty("id", log.getId());
        method.setProperty("time", log.getTime());
        method.setProperty("processName", log.getProcessName());
        method.setProperty("threadName", log.getThreadName());
        method.setProperty("pid", log.getPid());
        if (log.getHookId() > 0) {
            method.setProperty("hookId", log.getHookId());
            method.addLabel(Label.label(FRAMEWORK));
        }
        method.setProperty("methodSign", log.getMethodSign());
        method.setProperty("isStatic", log.isStatic());
        if (log.getDuration() > 0) {
            method.setProperty("duration", log.getDuration());
        }
        if (log.getReturnIndex() >= 0) {
            method.setProperty("returnIndex", log.getReturnIndex());
        }
        if (!log.isStatic()) {
            ObjectInstance objectInstance = log.getObjectInstance();
            Node objectNode = historyBuilder.queryObjectNode(database, objectInstance.getPid(), objectInstance.getObjectId(), objectInstance.getVersion());
            if (objectNode == null) {
                System.out.println("findn't object node @" + log.toString());
            } else {
                method.createRelationshipTo(objectNode, RelationshipType.withName(INSTANCE));
            }
        }
        List<ObjectInstance> params = log.getParams();
        for (int i = 0; params != null && i < params.size(); i++) {
            ObjectInstance objectInstance = params.get(i);
            Node node = null;
            if (objectInstance.isBasicType()) {
                node = database.createNode(Label.label(BASIC_TYPE));
                node.setProperty("type", objectInstance.getType());
                node.setProperty("data", objectInstance.getData());
            } else if (objectInstance.isNullObject()) {
                node = database.createNode(Label.label(NULL));
            } else {
                node = historyBuilder.queryObjectNode(database, objectInstance.getPid(), objectInstance.getObjectId(), objectInstance.getVersion());
            }
            if (node == null) {
                System.out.println("find no node for instance " + objectInstance.toString());
            }
            Relationship relationship = method.createRelationshipTo(node, RelationshipType.withName(PARAM));
            relationship.setProperty("index", i);
        }
        if (log.getResult() != null) {
            ObjectInstance result = log.getResult();
            //判断是不是NULL
            Node resultNode;
            if (result == null || result.isNullObject()) {
                resultNode = database.createNode(Label.label(NULL));
            } else if (result.isBasicType()) {
                //判断是不是基本类型
                resultNode = database.createNode(Label.label(BASIC_TYPE));
                resultNode.setProperty("type", result.getType());
                resultNode.setProperty("data", result.getData());
            } else {
                resultNode = historyBuilder.queryObjectNode(database, result.getPid(), result.getObjectId(), result.getVersion());
            }
            method.createRelationshipTo(resultNode, RelationshipType.withName(RESULT));
        }
        for (int index = 0; invoke != null && index < invoke.size(); index++) {
            LogObject logObject = invoke.get(index);
            Node node = logObject.toNode(historyBuilder);
            Relationship relationshipTo = method.createRelationshipTo(node, RelationshipType.withName(INVOKE));
            relationshipTo.setProperty("index", index);
        }
        return method;

    }

    public List<LogObject> getInvoke() {
        return invoke;
    }

    public void setObjectInstance(ObjectInstance objectInstance) {
        this.objectInstance = objectInstance;
    }

    public ObjectInstance getObjectInstance() {
        return objectInstance;
    }

    public void setParams(List<ObjectInstance> params) {
        this.params = params;
    }

    public List<ObjectInstance> getParams() {
        return params;
    }

    public void setThrowable(ObjectInstance throwable) {
        this.throwable = throwable;
    }

    public ObjectInstance getThrowable() {
        return throwable;
    }

    public void setResult(ObjectInstance result) {
        this.result = result;
    }

    public ObjectInstance getResult() {
        return result;
    }

    public long getPid() {
        return pid;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public long getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getProcessName() {
        return processName;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getHookId() {
        return hookId;
    }

    public int getReturnIndex() {
        return returnIndex;
    }

    public boolean isEnd() {
        return end;
    }

}
