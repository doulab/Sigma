package com.sensoftsarl.nfclibrary.bean;

/**
 * Created by doulab on 27/01/16.
 */
public class TransmitProgress {


    public int controlCode;
    public byte[] command;
    public int commandLength;
    public byte[] response;
    public int responseLength;
    public Exception e;
}
