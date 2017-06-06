package com.mijack.logcatmodel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 以进程为单位的Object Pool
 *
 * @auhor Mr.Yuan
 * @date 2017/4/24
 */
public class ObjectInstancePool {
    //pid-->hashcode-->object instance list
    static Map<Long, Map<Long, List<ObjectInstance>>> pool = new HashMap<>();

    public enum Type {
        INSTANCE, PARAM, RETURN, THROWABLE
    }

    public static Map<Long, Map<Long, List<ObjectInstance>>> getPool() {
        return pool;
    }

    public static ObjectInstance obtain(ContainerNode containerNode, LogObject logObject, Type type) {
        // 获取pid
        long pid = logObject.getPid();
        if (containerNode instanceof ObjectNode) {
            // basic type
            if (containerNode.has("type")) {
                return newBasicType((ObjectNode) containerNode);
            }
            long hashcode = containerNode.get("hashcode").asLong();
            //处理null
            if (hashcode <= 0) {
                return obtainNull();
            }
            //获取objectInstanceList
            List<ObjectInstance> objectInstances = getObjectList(pid, hashcode);
            int size = objectInstances.size();
            //判断是否需要使用新版本的对象
            if (size == 0 || needNewVersionObjectInstance(containerNode, logObject, type)) {
                ObjectInstance objectInstance = newObjectInstance(containerNode);
                objectInstance.setPid(logObject.getPid());
                int version = size + 1;
                objectInstance.setVersion(version);
                objectInstances.add(objectInstance);
                return objectInstance;
            }
            return objectInstances.get(size - 1);
        } else if (containerNode instanceof ArrayNode) {

//
        }
        return null;
    }

    private static boolean needNewVersionObjectInstance(ContainerNode containerNode, LogObject logObject, Type type) {
        if (type.equals(Type.RETURN)) {
            if ("android.os.Message android.os.Message.obtain()".equals(logObject.getMethodSign())
                    || "android.os.Parcel android.os.Parcel.obtain()".equals(logObject.getMethodSign())
                    || "android.os.Parcel android.os.Parcel.obtain(int)".equals(logObject.getMethodSign())
                    || "android.os.Parcel android.os.Parcel.obtain(long)".equals(logObject.getMethodSign())
                    ) {
                return true;
            }
        }
        return false;
    }

    private static ObjectInstance newObjectInstance(ContainerNode jsonNode) {
        ObjectInstance objectInstance = new ObjectInstance(jsonNode);
        objectInstance.setClassName(jsonNode.get("class").asText());
        objectInstance.setObjectId(jsonNode.get("hashcode").asLong());
        return objectInstance;
    }

    private static List<ObjectInstance> getObjectList(long pid, long hashcode) {
        if (!pool.containsKey(pid)) {
            pool.put(pid, new HashMap<>());
        }
        Map<Long, List<ObjectInstance>> map = pool.get(pid);
        if (!map.containsKey(hashcode)) {
            map.put(hashcode, new ArrayList<>());
        }
        return map.get(hashcode);
    }

    public static ObjectInstance newBasicType(ObjectNode jsonNode) {
        ObjectInstance objectInstance = new ObjectInstance(jsonNode);
        objectInstance.setType(jsonNode.get("type").asText());
        objectInstance.setData(jsonNode.get("data").asText());
        return objectInstance;
    }

    public static ObjectInstance obtainNull(ContainerNode o) {
        return new ObjectInstance(o);
    }

    public static ObjectInstance obtainNull() {
        return new ObjectInstance(null);
    }
}
