package com.mijack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @auhor Mr.Yuan
 * @date 2017/4/11
 */
public class ListHashMap<K, V> {
    private HashMap<K, List<V>> data = new HashMap<>();

    public void put(K k, V v) {
        if (!data.containsKey(k)) {
            data.put(k, new ArrayList<V>());
        }
        data.get(k).add(v);
    }

    public List<V> getList(K k) {
        if (data.containsKey(k)) {
            return data.get(k);
        }
        List<V> l = new ArrayList<>();
        data.put(k, l);
        return l;
    }
}
