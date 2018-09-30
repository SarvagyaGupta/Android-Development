package edu.stanford.cse193a.animalgame;

/**
 * This is a simple wrapper class around database 'graph' rows.
 * You may want to use it with getValue if you are using Firebase.
 * See lecture slides.
 */
public class Graph {
    public int childid;
    public int graphid;
    public int parentid;
    public String type;

    public Graph() {}

    public int getChildId() {
        return childid;
    }

    public int getGraphId() {
        return graphid;
    }

    public int getParentId() {
        return parentid;
    }

    public String getType() {
        return type;
    }
}
