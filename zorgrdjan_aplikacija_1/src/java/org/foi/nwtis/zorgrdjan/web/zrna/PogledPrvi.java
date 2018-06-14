/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.zrna;

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
import javax.servlet.http.HttpSession;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.kontrole.Util;
import org.foi.nwtis.zorgrdjan.web.podaci.Korisnici;
import org.foi.nwtis.zorgrdjan.web.podaci.Lokacija;
import org.foi.nwtis.zorgrdjan.web.podaci.ParkiralistaPojo;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Zoran
 */
@ManagedBean
@SessionScoped
public class PogledPrvi {
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
     * Creates a new instance of pogledPrvi
     */
    
    private List<Korisnici> popisKorisnika=new ArrayList<>();
    
    public PogledPrvi() {
    }
    @PostConstruct
    private void init() {
        popisKorisnika=getAllUsersFromDatabase();
    }

    public List<Korisnici> getPopisKorisnika() {
        return popisKorisnika;
    }

    public void setPopisKorisnika(List<Korisnici> popisKorisnika) {
        this.popisKorisnika = popisKorisnika;
    }

    public int getStranicenje() {
        return stranicenje;
    }

    public void setStranicenje(int stranicenje) {
        this.stranicenje = stranicenje;
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

    private List<Korisnici> getAllUsersFromDatabase() {
        dohvatiPodatke();
        List<Korisnici> popisKorisnika = new ArrayList<>();

        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici";
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
                String kor_ime = rs.getString("kor_ime");
                String ime = rs.getString("ime");
                String prezime = rs.getString("prezime");
                String lozinka = rs.getString("lozinka");
                String email = rs.getString("email");
                System.out.println("Korisnicko ime:"+kor_ime);
                Korisnici korisnik=new Korisnici(kor_ime, ime, prezime, lozinka, ime, id);
                popisKorisnika.add(korisnik);
            }

            rs.close();
            stm.close();
            con.close();
            return popisKorisnika;
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return null;
    }
    
}
