package com.mijack.meta;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @auhor Mr.Yuan
 * @date 2017/5/18
 */
public class IfUnit {
    private IfUnit parent;
    private Element rootElement;
    private Element thenElement;
    private Element elseElement;
    private List<IfElseUnit> ifElseUnits = new ArrayList();
    private String id;

    public IfUnit getParent() {
        return parent;
    }

    public void setParent(IfUnit parent) {
        this.parent = parent;
    }

    public Element getRootElement() {
        return rootElement;
    }

    public void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public Element getThenElement() {
        return thenElement;
    }

    public void setThenElement(Element thenElement) {
        this.thenElement = thenElement;
    }

    public Element getElseElement() {
        return elseElement;
    }

    public void setElseElement(Element elseElement) {
        this.elseElement = elseElement;
    }

    public List<IfElseUnit> getIfElseUnits() {
        return ifElseUnits;
    }

    public void setIfElseUnits(List<IfElseUnit> ifElseUnits) {
        this.ifElseUnits = ifElseUnits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRootPath() {
        return rootElement != null ? "" : rootElement.getPath();
    }

    public class IfElseUnit {
        private String id;
        List<IfUnit> ifUnits;
        Element ifElseElement;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<IfUnit> getIfUnits() {
            return ifUnits;
        }

        public void setIfUnits(List<IfUnit> ifUnits) {
            this.ifUnits = ifUnits;
        }

        public Element getIfElseElement() {
            return ifElseElement;
        }

        public void setIfElseElement(Element ifElseElement) {
            this.ifElseElement = ifElseElement;
        }
    }
}
