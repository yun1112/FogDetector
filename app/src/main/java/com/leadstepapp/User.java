package com.leadstepapp;

import com.google.type.Date;
import com.google.type.DateTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
//    private Double left[] = new Double[89];
//    private Double right[] = new Double[89];

    private ArrayList<Double> left;
    private ArrayList<Double> right;
    private DateTime createAt;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name) {
        this.name = name;
    }
    public User(String name, ArrayList<Double> left, ArrayList<Double> right) {
        this.name = name;
        this.left = (ArrayList<Double>) left.clone();
        this.right = (ArrayList<Double>) right.clone();
//        this.createAt = DateTime.getDefaultInstance();
    }

    public List<Double> getLeft() {
        return left;
    }
    public void setLeft(ArrayList<Double> left) {
        this.left = (ArrayList<Double>) left.clone();
    }
    public List<Double> getRight() {
        return right;
    }
    public void setRight(ArrayList<Double> right) {
        this.right = (ArrayList<Double>) right.clone();
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
