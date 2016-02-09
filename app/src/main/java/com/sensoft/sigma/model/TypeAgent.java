package com.sensoft.sigma.model;

import io.realm.RealmObject;

/**
 * Created by doulab on 24/01/16.
 */
public class TypeAgent extends RealmObject {

    private  String numeroTypeAgent;
    private String typeAgent;

    public String getNumeroTypeAgent() {
        return numeroTypeAgent;
    }

    public void setNumeroTypeAgent(String numeroTypeAgent) {
        this.numeroTypeAgent = numeroTypeAgent;
    }

    public String getTypeAgent() {
        return typeAgent;
    }

    public void setTypeAgent(String typeAgent) {
        this.typeAgent = typeAgent;
    }
}
