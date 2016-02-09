package com.sensoft.sigma.model;

import io.realm.RealmObject;

/**
 * Created by doulab on 24/01/16.
 */
public class TypePlat extends RealmObject {

    private  String numeroTypePlat;
    private  String typePlat;
    private long prixPlat;


    public String getNumeroTypePlat() {
        return numeroTypePlat;
    }

    public void setNumeroTypePlat(String numeroTypePlat) {
        this.numeroTypePlat = numeroTypePlat;
    }

    public String getTypePlat() {
        return typePlat;
    }

    public void setTypePlat(String typePlat) {
        this.typePlat = typePlat;
    }

    public long getPrixPlat() {
        return prixPlat;
    }

    public void setPrixPlat(long prixPlat) {
        this.prixPlat = prixPlat;
    }
}
