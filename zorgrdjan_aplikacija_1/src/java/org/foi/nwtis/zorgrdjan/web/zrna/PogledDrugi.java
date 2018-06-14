/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.zrna;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.podaci.Dnevnik;
import org.foi.nwtis.zorgrdjan.web.podaci.Korisnici;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Zoran
 */
@ManagedBean
@SessionScoped
public class PogledDrugi {
    
    String url;
    String korisnik;
    String lozinka;
    Connection con;
    Statement stm;
    BP_Konfiguracija bpk;
    String apikey;
    String gmapikey;
    String korisnikSVN;
    String lozinkaSVN;
    int stranicenje;
    /**
     * Creates a new instance of PogledDrugi
     */
    List<Dnevnik> logDnevnika=new ArrayList<>();
    public PogledDrugi() {
    }
     @PostConstruct
    private void init() {
        logDnevnika=getAllDnevnikData();
    }

    public List<Dnevnik> getLogDnevnika() {
        return logDnevnika;
    }

    public int getStranicenje() {
        return stranicenje;
    }

    public void setStranicenje(int stranicenje) {
        this.stranicenje = stranicenje;
    }

    public void setLogDnevnika(List<Dnevnik> logDnevnika) {
        this.logDnevnika = logDnevnika;
    }
    
    private void dohvatiPodatke() {

        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        apikey = SlusacAplikacije.getSc().getAttribute("apikey").toString();
        gmapikey = SlusacAplikacije.getSc().getAttribute("gmapikey").toString();
        lozinkaSVN = SlusacAplikacije.getSc().getAttribute("lozinkaSVN").toString();
        korisnikSVN = SlusacAplikacije.getSc().getAttribute("korisnikSVN").toString();
        stranicenje=Integer.parseInt(SlusacAplikacije.getSc().getAttribute("stranicenje").toString());
    }

    private List<Dnevnik> getAllDnevnikData() {
        dohvatiPodatke();
        List<Dnevnik> dnevnikLog = new ArrayList<>();

        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.dnevnik";
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String kor_ime = rs.getString("korisnik");
                String url = rs.getString("url");
                String akcija = rs.getString("akcija");
                String vrijeme = rs.getString("vrijeme");
                int status = rs.getInt("status");
                Dnevnik logDnevnika=new Dnevnik(id, kor_ime, url, akcija, vrijeme, status);
                dnevnikLog.add(logDnevnika);
            }

            rs.close();
            stm.close();
            con.close();
            return dnevnikLog;
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return null;
    }
    }

