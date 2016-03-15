package com.sensoft.sigma.model;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by doulab on 24/01/16.
 */

public class Agent extends RealmObject implements Serializable {

    @PrimaryKey
    private int id ;

    private String numeroAgent;
    private String prenom;
    private String nom;
    private String dateNaissance;
    private String adresse;
    private String telephone;
    private String email;
    private String login;
    private String password;
    private String poste;
    private String imageAgent;
    private String qrCodeAgent;


    private TypeAgent typeAgent;

    public Agent() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroAgent() {
        return numeroAgent;
    }

    public void setNumeroAgent(String numeroAgent) {
        this.numeroAgent = numeroAgent;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageAgent() {
        return imageAgent;
    }

    public void setImageAgent(String imageAgent) {
        this.imageAgent = imageAgent;
    }

    public String getQrCodeAgent() {
        return qrCodeAgent;
    }

    public void setQrCodeAgent(String qrCodeAgent) {
        this.qrCodeAgent = qrCodeAgent;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public TypeAgent getTypeAgent() {
        return typeAgent;
    }

    public void setTypeAgent(TypeAgent typeAgent) {
        this.typeAgent = typeAgent;
    }

}
