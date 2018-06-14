/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.kontrole;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;

import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    static String url;
    static String korisnik;
    static String lozinka;
    static Connection con;
    static Statement stm;
    static BP_Konfiguracija bpk;
    static String apikey;
    static String gmApiKey;
    private static final long serialVersionUID = 1L;
    private String password;
    private String message, uname;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String loginProject() {
        boolean result = provjeriLogin();
        if (result) {

            // get Http Session and store username
            HttpSession session = Util.getSession();
            session.setAttribute("kor_ime", uname);
            String url = "/zorgrdjan_aplikacija_1/faces/pogled1.xhtml"; // Your URL here
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            } catch (IOException ex) {
                Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Invalid Login!",
                    "Please Try Again!"));

            // invalidate session, and redirect to other pages
            //message = "Invalid Login. Please Try Again!";
//            String url = "/zorgrdjan_aplikacija_1/faces/login.xhtml"; // Your URL here
//            try {
//                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
//            } catch (IOException ex) {
//                Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
//            }
            return "login";
        }
        return "";
    }

    public void logout() {
        HttpSession session = Util.getSession();
        session.invalidate();
        String url = "/zorgrdjan_aplikacija_1/faces/login.xhtml"; // Your URL here
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void dohvatiPodatke() {

        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        apikey = SlusacAplikacije.getSc().getAttribute("apikey").toString();
        gmApiKey = SlusacAplikacije.getSc().getAttribute("gmapikey").toString();

    }

    public boolean provjeriLogin() {
        dohvatiPodatke();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici where kor_ime=" + "\"" + uname + "\"" + " and lozinka=" + "\"" + password + "\"";
            ResultSet rs = stm.executeQuery(query);
            if (rs.next()) // found
            {
                System.out.println(rs.getString("kor_ime"));
                stm.close();
                con.close();
                return true;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Pokusaj prijave!",
                        "Pogresni podaci!"));
                stm.close();
                con.close();
                return false;
            }
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problem sa bazom",
                    "Nije se moguce logirati na bazu"));
            System.out.println("Error in login() -->" + ex.getMessage());
            return false;
        }

    }
}
