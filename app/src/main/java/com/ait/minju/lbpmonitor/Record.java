package com.ait.minju.lbpmonitor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Record implements Serializable {

    ArrayList<MyEntry> left;
    ArrayList<MyEntry> right;
    int timeDiff;
    String startTime;
    String endTime;

    public Record(){}

    public Record(ArrayList<MyEntry> left, ArrayList<MyEntry> right, int timeDiff, String startTime, String endTime) {
        this.left = left;
        this.right = right;
        this.timeDiff = timeDiff;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ArrayList<MyEntry> getLeft() {
        return left;
    }

    public void setLeft(ArrayList<MyEntry> left) {
        this.left = left;
    }

    public ArrayList<MyEntry> getRight() {
        return right;
    }

    public void setRight(ArrayList<MyEntry> right) {
        this.right = right;
    }

    public int getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(int timeDiff) {
        this.timeDiff = timeDiff;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
