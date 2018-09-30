package edu.stanford.cse193a.animalgame;

import java.io.Serializable;

/**
 * This is a simple wrapper class around database 'node' rows.
 * You may want to use it with getValue(Class) if you are using Firebase.
 * See lecture slides.
 *
 * @author Marty Stepp
 * @version 2017/03/02
 * - added id/getID
 * @version 2017/02/22
 * - initial version
 */
public class Node implements Serializable {
    public String text;
    public String type;
    public int id = -1;
    public int yes = -1;
    public int no = -1;

    public Node() {}

    public int getID() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public int getYesID() {
        return yes;
    }

    public int getNoID() {
        return no;
    }

    public boolean isAnswer() {
        return type != null && type.equalsIgnoreCase("answer");
    }

    public boolean isQuestion() {
        return type != null && type.equalsIgnoreCase("question");
    }

    public String toString() {
        return "Node {id=" + id + ", text=\"" + text + "\", type=\"" + type + "\""
                + (isQuestion() ? (", yes=" + yes + ", no=" + no) : "")
                + "}";
    }
}
