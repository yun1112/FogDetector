package com.leadstepapp;

import com.google.type.Date;
import com.google.type.DateTime;

import java.lang.reflect.Array;
import java.util.List;
import java.util.List;

public class User {
    private String name;
//    private Double left[] = new Double[89];
//    private Double right[] = new Double[89];

    private List<Double> left;
    private List<Double> right;
    private DateTime createAt;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name) {
        this.name = name;
    }
    public User(String name, List<Double> left, List<Double> right) {
        this.name = name;
        this.left = left;
        this.right = right;
//        this.createAt = DateTime.getDefaultInstance();
    }

    public List<Double> getLeft() {
        return left;
    }
    public void setLeft(List<Double> left) {
        this.left = left;
    }
    public List<Double> getRight() {
        return right;
    }
    public void setRight(List<Double> right) {
        this.right = right;
    }
//    public User(String name, Double[] left, Double[] right, DateTime createAt) {
//        this.name = name;
//        this.left = left;
//        this.right = right;
//        this.createAt = createAt;
//    }
//
//    public Double[] getLeft() {
//        return left;
//    }
//
//    public void setLeft(Double[] left) {
//        this.left = left;
//    }
//
//    public Double[] getRight() {
//        return right;
//    }
//
//    public void setRight(Double[] right) {
//        this.right = right;
//    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


//    public DateTime getCreateAt() {
//        return createAt;
//    }
//    public void setCreateAt() {
//        this.createAt = createAt;
//    }
}
