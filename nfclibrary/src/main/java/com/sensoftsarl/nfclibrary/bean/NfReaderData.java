package com.sensoftsarl.nfclibrary.bean;

/**
 * Created by doulab on 03/02/16.
 */
import java.io.Serializable;
/**
 * Données à encoder sur la Carte
 */
public class NfReaderData implements Serializable{
    // taille donnée
    private int  size;

    public NfReaderData(int size, String data, String dataType) {
        this.size = size;
        this.data = data;
        this.dataType = dataType;
    }


    public NfReaderData(int size, String data) {
        this.size = size;
        this.data = data;
    }

    // donnée
    private String  data;

    // type donnée
    private String dataType;

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return data != null ? data : "";
    }

}