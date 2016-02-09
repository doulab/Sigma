package com.sensoft.sigma.model;

import io.realm.RealmObject;

/**
 * Created by doulab on 24/01/16.
 */
public class Commande extends RealmObject {

    private String numeroCommande;
    private String dateCommande;
    private String numeroAgent;
    private TypePlat typePlat;

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }


    public String getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(String dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getNumeroAgent() {
        return numeroAgent;
    }

    public void setNumeroAgent(String numeroAgent) {
        this.numeroAgent = numeroAgent;
    }

    public TypePlat getTypePlat() {
        return typePlat;
    }

    public void setTypePlat(TypePlat typePlat) {
        this.typePlat = typePlat;
    }
}
