package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mijack.Utils;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class ObjectInstance implements Comparable<ObjectInstance>{
    private long objectId;
    private int version;
    private JsonNode rawJsonNode;
    private boolean nullObject;
    private String type;
    private String className;
    private String data;
    private long pid;
    ObjectInstance(JsonNode jsonNode) {
        rawJsonNode = jsonNode;
        nullObject = (jsonNode == null) || (jsonNode instanceof NullNode);
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public long getPid() {
        return pid;
    }

    public JsonNode getRawJsonNode() {
        return rawJsonNode;
    }

    public void setRawJsonNode(JsonNode rawJsonNode) {
        this.rawJsonNode = rawJsonNode;
    }

    public void setNull(boolean aNull) {
        this.nullObject = aNull;
    }

    public boolean isNullObject() {
        return nullObject;
    }

    public void setNullObject(boolean nullObject) {
        this.nullObject = nullObject;
    }

    public boolean isBasicType() {
        return !Utils.isEmpty(type);
    }

    public static ObjectInstance obtainNull() {
        return ObjectInstancePool.obtainNull(null);
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ObjectInstance{" +
                "objectId=" + objectId +
                ", version=" + version +
                ", rawJsonNode=" + rawJsonNode +
                ", nullObject=" + nullObject +
                ", type='" + type + '\'' +
                ", className='" + className + '\'' +
                ", data='" + data + '\'' +
                ", pid=" + pid +
                '}';
    }

    @Override
    public int compareTo(ObjectInstance o2) {
            if (getPid() > o2.getPid()) {
                return 1;
            }
            if (getPid() < o2.getPid()) {
                return -1;
            }
            if (getObjectId() > o2.getObjectId()) {
                return 1;
            }
            if (getObjectId() < o2.getObjectId()) {
                return -1;
            }
            if (getVersion() > o2.getVersion()) {
                return 1;
            }
            if (getVersion() < o2.getVersion()) {
                return -1;
            }
            return 0;
        
    }
}
