/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.podaci;

/**
 *
 * @author Zoran
 */
public class Dnevnik {
    private int id;
    private String korisnik;
    private String url;
    private String akcija;
    private String vrijeme;
    private int status;

    public Dnevnik(int id, String korisnik, String url, String akcija, String vrijeme, int status) {
        this.id = id;
        this.korisnik = korisnik;
        this.url = url;
        this.akcija = akcija;
        this.vrijeme = vrijeme;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(String korisnik) {
        this.korisnik = korisnik;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAkcija() {
        return akcija;
    }

    public void setAkcija(String akcija) {
        this.akcija = akcija;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
}
