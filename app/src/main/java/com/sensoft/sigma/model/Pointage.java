package com.sensoft.sigma.model;

import io.realm.RealmObject;

/**
 * Created by doulab on 24/01/16.
 */
public class Pointage extends RealmObject {

    private String numeroPointage;
    private  String datePointageEntre;
    private String datePointageSortit;
    private String numeroAgent;

    public String getNumeroPointage() {
        return numeroPointage;
    }

    public void setNumeroPointage(String numeroPointage) {
        this.numeroPointage = numeroPointage;
    }

    public String getDatePointageEntre() {
        return datePointageEntre;
    }

    public void setDatePointageEntre(String datePointageEntre) {
        this.datePointageEntre = datePointageEntre;
    }

    public String getDatePointageSortit() {
        return datePointageSortit;
    }

    public void setDatePointageSortit(String datePointageSortit) {
        this.datePointageSortit = datePointageSortit;
    }

    public String getNumeroAgent() {
        return numeroAgent;
    }

    public void setNumeroAgent(String numeroAgent) {
        this.numeroAgent = numeroAgent;
    }
}
