package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.*;

/**
 * 以进程为单位的日志数据库
 * 支持按照时间顺序（线程、进程粒度）和调用关系查询日志
 *
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class ProcessLogDatabase {
    /**
     * 以进程为单位的ObjectPool
     */
    ObjectInstancePool instancePool = new ObjectInstancePool();
    private static ObjectMapper mapper = new ObjectMapper();
    private Map<String, Stack<LogObject>> threadStackMap = new HashMap<>();
    /**
     * 以next形式呈现的日志
     */
    private Map<String, List<LogObject>> eventLogObject = new HashMap<>();
    /**
     * 时间顺序呈现的日志
     */
    private List<LogObject> logObjects = new ArrayList<>();
    private long pid;

    public ProcessLogDatabase(long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }

    public void addLog(String threadName, String line) throws IOException {
        JsonNode tree = mapper.readTree(line);
        String logType = tree.get("logType").asText();
        if (!threadStackMap.containsKey(threadName)) {
            threadStackMap.put(threadName, new Stack<>());
        }
        Stack<LogObject> logStack = threadStackMap.get(threadName);
        if ("enter".equals(logType)) {
            //创建LogObject
            LogObject logObject = LogBuilder.obtainLogObject(tree, this);
            logObjects.add(logObject);
            //获取栈顶元素
            logObject.setEnterNode(tree);
            if (!logStack.isEmpty()) {
                // 创建调用关系
                LogObject top = logStack.peek();
                top.addInvoke(logObject);
            }
            //push到stack
            if (logStack.isEmpty()) {
                //添加到eventLogObject
                List<LogObject> logObjects = eventLogObject.get(threadName);
                if (logObjects == null) {
                    logObjects = new ArrayList<>();
                    eventLogObject.put(threadName, logObjects);
                }
                logObjects.add(logObject);
            }
            logStack.push(logObject);
            //获取ObjectInstance的信息
        }
        if ("exit".equals(logType)) {
            //弹出顶部的元素
            assert logStack.isEmpty() == false;
            LogObject log = logStack.pop();
            //check method sign
            String enterMethodSign = log.getEnterNode().get("methodSign").asText();
            String exitMethodSign = tree.get("methodSign").asText();
            assert enterMethodSign != null;
            assert enterMethodSign.equals(exitMethodSign);
            log.setExitNode(tree);
            //获取ObjectInstance的信息
            //获取result 的信息
            JsonNode result = tree.get("result");
            if (result != null) {

            }
        }

    }

    public Element toElement() {
        Element processElement = new DefaultElement("process");
        processElement.addAttribute("pid", String.valueOf(pid));
        //遍历next
        Set<Map.Entry<String, List<LogObject>>> entries = eventLogObject.entrySet();
        for (Map.Entry<String, List<LogObject>> entry : entries) {
            String threadName = entry.getKey();
            List<LogObject> value = entry.getValue();
            Element threadElement = new DefaultElement("thread");
            threadElement.addAttribute("name", threadName);
            for (LogObject o : value) {
                threadElement.add(o.toElement());
            }
            processElement.add(threadElement);
        }
        return processElement;
    }

    public void toNode(HistoryBuilder historyBuilder) {
        Set<Map.Entry<String, List<LogObject>>> entries = eventLogObject.entrySet();
        for (Map.Entry<String, List<LogObject>> entry : entries) {
            List<LogObject> value = entry.getValue();
            for (LogObject o : value) {
                o.toNode(historyBuilder);
            }
        }
    }

    public List<LogObject> getLogObjects() {
        return logObjects;
    }

    public Map<String, List<LogObject>> getEventLogObject() {
        return eventLogObject;
    }
}
