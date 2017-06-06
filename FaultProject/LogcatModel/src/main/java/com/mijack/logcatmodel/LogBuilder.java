package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class LogBuilder {

    public static LogObject obtainLogObject(JsonNode objectNode, ProcessLogDatabase processLogDatabase) {
        try {
            LogObject logEntity = new LogObject();
            logEntity.setTime(objectNode.get("time").asLong());
            logEntity.setProcessName(objectNode.get("processName").asText());
            logEntity.setThreadName(objectNode.get("threadName").asText());
            logEntity.setPid(objectNode.get("pid").asLong());
            JsonNode hookId = objectNode.get("hookId");
            if (hookId != null) logEntity.setHookId(hookId.asInt(-1));
            logEntity.setMethodSign(objectNode.get("methodSign").asText());
            logEntity.setMethodType(objectNode.get("methodType").asText());
            JsonNode returnIndex = objectNode.get("returnIndex");
            if (returnIndex != null) logEntity.setReturnIndex(returnIndex.asInt(-1));
//            logEntity.setObjectInstance(ObjectInstance.obtain((ContainerNode) objectNode.get("instance")));
//            logEntity.setThrowable(ObjectInstance.obtain((ContainerNode) objectNode.get("throwable")));

            //判断是否主要清楚对象
            return logEntity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
