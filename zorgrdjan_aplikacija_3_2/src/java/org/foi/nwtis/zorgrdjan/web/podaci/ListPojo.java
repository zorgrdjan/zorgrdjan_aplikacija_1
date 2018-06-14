/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.podaci;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.foi.nwtis.zorgrdjan.web.podaci.ParkiralistaPojo;

/**
 *Klasa koja sluzi za kreiranje json zapisa
 * @author Zoran
 */
public class ListPojo {

    @SerializedName("odgovor")
    @Expose
    private List<ParkiralistaPojo> odgovor = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("poruka")
    @Expose
    private String poruka;
    

    public List<ParkiralistaPojo> getOdgovor() {
        return odgovor;
    }

    public void setOdgovor(List<ParkiralistaPojo> odgovor) {
        this.odgovor = odgovor;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
}
