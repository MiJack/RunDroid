package com.mijack.meta;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @auhor Mr.Yuan
 * @date 2017/5/17
 */
public class TreeUnit<T> {
    private String name;
    private Element root;
    private List<TreeUnit<T>> list = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRoot(Element root) {
        this.root = root;
    }

    public Element getRoot() {
        return root;
    }
}
