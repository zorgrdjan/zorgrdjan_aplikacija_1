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
public class Korisnici {
    private String kor_ime;
    private String ime;
    private String prezime;
    private String lozinka;
    private String email;
    private int id;

    public Korisnici(String kor_ime, String ime, String prezime, String lozinka, String email, int id) {
        this.kor_ime = kor_ime;
        this.ime = ime;
        this.prezime = prezime;
        this.lozinka = lozinka;
        this.email = email;
        this.id = id;
    }

    public String getKor_ime() {
        return kor_ime;
    }

    public void setKor_ime(String kor_ime) {
        this.kor_ime = kor_ime;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
