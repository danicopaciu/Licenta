package com.springapp.mvc.model.abc;

import java.util.List;

/**
 * Created by Daniel on 3/14/2015.
 */
public class FoodSource implements Comparable<FoodSource> {

    private List<Nectar> nectarList;

    private double fitness;

    private double probability;

    private int trialsNumber;

    private int conflictsNumber;


    private Bee bee;
    private OnlookerBee onlookerBee;

    public FoodSource(List<Nectar> nectarList) {
        this.nectarList = nectarList;
        this.fitness = 0.0;
        this.probability = 0.0;
        this.trialsNumber = 0;
        this.conflictsNumber = 0;
    }

    public Bee getBee() {
        return bee;
    }

    public void setBee(Bee bee) {
        this.bee = bee;
    }

    public OnlookerBee getOnlookerBee() {
        return onlookerBee;
    }

    public void setOnlookerBee(OnlookerBee onlookerBee) {
        this.onlookerBee = onlookerBee;
    }

    public List<Nectar> getNectarList() {
        return nectarList;
    }

    public void setNectarList(List<Nectar> nectarList) {
        this.nectarList = nectarList;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getTrialsNumber() {
        return trialsNumber;
    }

    public void setTrialsNumber(int trialsNumber) {
        this.trialsNumber = trialsNumber;
    }

    public int getConflictsNumber() {
        return conflictsNumber;
    }

    public void setConflictsNumber(int conflictsNumber) {
        this.conflictsNumber = conflictsNumber;
    }

    @Override
    public int compareTo(FoodSource o) {
        return this.conflictsNumber - o.getConflictsNumber();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FoodSource) {
            int equalElements = 0;
            for (Nectar index1 : nectarList) {
                FoodSource comparisonObj = (FoodSource) obj;
                for (Nectar index2 : comparisonObj.getNectarList()) {
                    if (index1.equals(index2)) {
                        equalElements++;
                    }
                }
            }
            if (equalElements >= 4) {
                return true;
            }
        }
        return false;
    }

    public void incrementTrialsNumber() {
        trialsNumber++;
    }
}
