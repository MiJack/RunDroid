package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mijack.Utils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElement;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class LogProcess {
    ObjectMapper mapper = new ObjectMapper();
    Map<Long, ProcessLogDatabase> databaseMap = new HashMap<>();
    String logFile;

    public LogProcess(String logFile) {
        this.logFile = logFile;
    }

    public void process() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String line = null;
        while ((line = reader.readLine()) != null) {
            int indexOf = line.indexOf("{\"logType\":\"");
            if (indexOf >= 0) {
                line = line.substring(indexOf);
            }
            JsonNode tree = mapper.readTree(line);
            long pid = tree.get("pid").asLong();
            String threadName = tree.get("threadName").asText();
            putLog(pid, threadName, line);
        }
    }

    public void printDatabaseXml() {
        String xmlFile = logFile + ".xml";
        Document document = new DefaultDocument();
        Element element = new DefaultElement("history");
        document.add(element);
        Element processes = new DefaultElement("processes");
        Element logs = new DefaultElement("log-repo");
        List<LogObject> objects = new ArrayList<>();
        for (Map.Entry<Long, ProcessLogDatabase> entry : databaseMap.entrySet()) {
            ProcessLogDatabase logDatabase = entry.getValue();
            processes.add(logDatabase.toElement());
            objects.addAll(logDatabase.getLogObjects());
        }
        element.add(processes);
        //遍历所有的log并输出
        objects.sort((o1, o2) -> {
            long t1 = o1.getTime();
            long t2 = o2.getTime();
            if (t1 < t2) return -1;
            if (t1 > t2) return 1;
            return 0;
        });
        for (LogObject logObject : objects) {
            Element e = new DefaultElement("log-entity");
            DefaultElement startNode = new DefaultElement("startNode");
            startNode.add(new DefaultCDATA(logObject.getEnterNode().toString()));
            e.add(startNode);
            DefaultElement endNode = new DefaultElement("endNode");
            endNode.add(new DefaultCDATA(logObject.getEnterNode().toString()));
            e.add(endNode);
//            e.add(new DefaultCDATA(logObject.getExitNode().toString()));
            logs.add(e);
        }
        element.add(logs);
        // 创建object
        Element o = new DefaultElement("objects");
        ObjectInstancePool.getPool().values().stream().flatMap(longListMap -> longListMap.values().stream())
                .flatMap(list -> list.stream())
                .sorted()
                .forEach(objectInstance -> {
                    Element e = new DefaultElement("object");
                    e.addAttribute("pid", String.valueOf(objectInstance.getPid()));
                    e.addAttribute("hashcode", String.valueOf(objectInstance.getObjectId()));
                    e.addAttribute("version", String.valueOf(objectInstance.getVersion()));
                    o.add(e);

                });
        element.add(o);
        Utils.writeXml(document, xmlFile);
    }

    private synchronized void putLog(long pid, String threadName, String line) throws IOException {
        if (!databaseMap.containsKey(pid)) {
            databaseMap.put(pid, new ProcessLogDatabase(pid));
        }
        ProcessLogDatabase processLogDatabase = databaseMap.get(pid);
        processLogDatabase.addLog(threadName, line);
    }

    public Map<Long, ProcessLogDatabase> getDatabaseMap() {
        return databaseMap;
    }
}
