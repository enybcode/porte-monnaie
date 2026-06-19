package com.portemonnaie.model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private String title;
    private double targetAmount;
    private double currentAmount;
    private LocalDate createdAt;

    public Goal(int id, String title, double targetAmount, double currentAmount, LocalDate createdAt) {
        this.id = id;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.createdAt = createdAt;
    }

    public Goal(String title, double targetAmount, double currentAmount) {
        this(0, title, targetAmount, currentAmount, LocalDate.now());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public double getProgress() {
        if (targetAmount <= 0) return 0;
        return Math.min(1, currentAmount / targetAmount);
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
}
