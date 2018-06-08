/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.rest.klijenti.GMKlijent;
import org.foi.nwtis.zorgrdjan.web.podaci.KorisnikPrvaMetoda;
import org.foi.nwtis.zorgrdjan.web.podaci.ListPojo;
import org.foi.nwtis.zorgrdjan.web.podaci.Lokacija;
import org.foi.nwtis.zorgrdjan.web.podaci.ParkiralistaPojo;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;

/**
 * REST Web Service
 *
 * @author Zoran
 */
@Path("parkiralista")
public class ParkiralistaREST {

    String url;
    String korisnik;
    String lozinka;
    Connection con;
    Statement stm;
    BP_Konfiguracija bpk;
    String apikey;
    String gmapikey;

    String odgovorOk = "OK";
    String odgovorError = "ERR";
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ParkiralistaREST
     */
    public ParkiralistaREST() {
    }

    /**
     *
     * @param podaci salju se u json zapisu korisnik i lozinka
     * @return vraca podatke o svim parkiralistima
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postJson(String podaci) {

        Gson gson = new Gson();
        ListPojo lista = new ListPojo();
        String odgovor;
        if (provjeriPoslanePodatkePostParkiralista(podaci)) {
            if (provjeriKorisnickePodatke(podaci)) {
                JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
                String korisnickoIme = jsonObject.get("korisnik").toString().replace("\"", "");
                lista.setOdgovor(getAllParkingsFromDatabase());
                lista.setStatus(odgovorOk);
                odgovor = gson.toJson(lista);
                pisiUDnevnik(korisnickoIme,
                        "http://localhost:8084/zorgrdjan_aplikacija_1/webresources/parkiralista/",
                        "postJson", 1);
            } else {
                lista.setStatus(odgovorError);
                lista.setPoruka("Takav korisnik ne postoji!");
                odgovor = gson.toJson(lista);
            }

        } else {
            lista.setPoruka(odgovorError);
            lista.setStatus("Pogresno poslani parametri");
            odgovor = gson.toJson(lista);
        }
        return odgovor;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("korisnik/{korisnik}/lozinka/{lozinka}")
    public String getJson(@PathParam("korisnik") String korisnickoIme, @PathParam("lozinka") String lozinkaKorisnik) {

        System.out.println("Korisnik" + korisnickoIme);
        System.out.println("Lozinka" + lozinkaKorisnik);
        Gson gson = new Gson();
        ListPojo lista = new ListPojo();
        String odgovor;
        if (provjeriKorisnickePodatkeGet(korisnickoIme, lozinkaKorisnik)) {
            lista.setOdgovor(getAllParkingsFromDatabase());
            lista.setStatus(odgovorOk);
            odgovor = gson.toJson(lista);
            pisiUDnevnik(korisnickoIme,
                    "http://localhost:8084/zorgrdjan_aplikacija_1/webresources/parkiralista/korisnik/"+korisnickoIme+"/lozinka/"+lozinkaKorisnik,
                    "getJson", 1);
        } else {
            lista.setStatus(odgovorError);
            lista.setPoruka("Takav korisnik ne postoji!");
            odgovor = gson.toJson(lista);
        }

        return odgovor;
    }

    /**
     *
     * @param id id od parkiralista koje se preuzima
     * @param podaci salju se u json zapisu korisnik i lozinka
     * @return vraca podatke o jednom parkiralistu
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String postJson(@PathParam("id") String id, String podaci) {
        Gson gson = new Gson();
        ListPojo lista = new ListPojo();
        String odgovor;
        if (provjeriPoslanePodatkePostParkiralista(podaci)) {
            if (provjeriKorisnickePodatke(podaci)) {
                if (provjeriParkiraliste(Integer.parseInt(id))) {
                    JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
                    String korisnickoIme = jsonObject.get("korisnik").toString().replace("\"", "");
                    lista.setOdgovor(dohvatiParkiraliste(id));
                    lista.setStatus(odgovorOk);
                    odgovor = gson.toJson(lista);
                    pisiUDnevnik(korisnickoIme,
                            "http://localhost:8084/zorgrdjan_aplikacija_1/webresources/parkiralista/" + id,
                            "postJson", 1);
                } else {
                    lista.setStatus(odgovorError);
                    lista.setPoruka("Parkiriraliste ne postoji!");
                    odgovor = gson.toJson(lista);
                }

            } else {
                lista.setStatus(odgovorError);
                lista.setPoruka("Takav korisnik ne postoji!");
                odgovor = gson.toJson(lista);
            }

        } else {
            lista.setPoruka(odgovorError);
            lista.setStatus("Pogresno poslani parametri");
            odgovor = gson.toJson(lista);
        }
        return odgovor;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String putJson(@PathParam("id") String id, String podaci) {
        Gson gson = new Gson();
        ListPojo lista = new ListPojo();
        String odgovor;
        if (provjeriPoslanePodatkeUpisParkiralista(podaci)) {
            if (provjeriKorisnickePodatke(podaci)) {
                if (!provjeriParkiraliste(Integer.parseInt(id))) {
                    JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
                    String korisnickoIme = jsonObject.get("korisnik").toString().replace("\"", "");
                    dodajParkiraliste(Integer.parseInt(id), podaci);
                    odgovor = "{\"odgovor\": [],"
                            + "\"status\": \"OK\"} ";
                    pisiUDnevnik(korisnickoIme,
                            "http://localhost:8084/zorgrdjan_aplikacija_1/webresources/parkiralista/" + id,
                            "putJson", 1);
                } else {
                    lista.setStatus(odgovorError);
                    lista.setPoruka("Parkiriraliste vec postoji!");
                    odgovor = gson.toJson(lista);
                }

            } else {
                lista.setStatus(odgovorError);
                lista.setPoruka("Takav korisnik ne postoji!");
                odgovor = gson.toJson(lista);
            }

        } else {
            lista.setPoruka(odgovorError);
            lista.setStatus("Pogresno poslani parametri");
            odgovor = gson.toJson(lista);
        }
        return odgovor;
    }

    /**
     * PUT method for updating or creating an instance of ParkiralistaREST
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content) {
    }

    /**
     * Funkcija koja dohvaca podatke potrebne za rad
     */
    private void dohvatiPodatke() {

        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        apikey = SlusacAplikacije.getSc().getAttribute("apikey").toString();
        gmapikey = SlusacAplikacije.getSc().getAttribute("gmapikey").toString();
    }

    private List<ParkiralistaPojo> getAllParkingsFromDatabase() {
        dohvatiPodatke();
        List<ParkiralistaPojo> popisParkiralista = new ArrayList<>();

        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.parkiralista";
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
                String naziv = rs.getString("naziv");
                String adresa = rs.getString("adresa");
                String longitude = rs.getString("longitude");
                String latitude = rs.getString("latitude");
                int kapacitet = rs.getInt("kapacitet");
                Lokacija lok = new Lokacija(latitude, longitude);
                ParkiralistaPojo park = new ParkiralistaPojo(id, naziv, adresa, longitude, latitude, kapacitet);
                popisParkiralista.add(park);
            }

            rs.close();
            stm.close();
            con.close();
            return popisParkiralista;
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return null;
    }

    private boolean provjeriParkiraliste(int id) {
        dohvatiPodatke();
        boolean povratnaInformacija = false;
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            String upit = "Select * from parkiralista where id=" + id;
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            ResultSet rs = stm.executeQuery(upit);
            if (rs.next()) {
                povratnaInformacija = true;
            }

            rs.close();
            stm.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(ParkiralistaREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return povratnaInformacija;
    }

    private List<ParkiralistaPojo> dohvatiParkiraliste(String id) {
        dohvatiPodatke();
        List<ParkiralistaPojo> popisParkiralista = new ArrayList<>();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            String query = "Select * from parkiralista where id=" + id;
            System.out.println("Provjera parkiralista:" + provjeriParkiraliste(Integer.parseInt(id)));
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                int idPark = rs.getInt("id");
                String naziv = rs.getString("naziv");
                String adresa = rs.getString("adresa");
                String longitude = rs.getString("longitude");
                String latitude = rs.getString("latitude");
                int kapacitet = rs.getInt("kapacitet");
                Lokacija lok = new Lokacija(latitude, longitude);
                ParkiralistaPojo park = new ParkiralistaPojo(idPark, naziv, adresa, longitude, latitude, kapacitet);
                popisParkiralista.add(park);
            }
            stm.close();
            con.close();
            return popisParkiralista;
        } catch (SQLException ex) {
            Logger.getLogger(ParkiralistaREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean provjeriPoslanePodatkePostParkiralista(String podaci) {
        boolean rezultat = false;

        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            String korisnickoIme = jsonObject.get("korisnik").toString();
            String lozinka = jsonObject.get("lozinka").toString();
            rezultat = true;
            return rezultat;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean provjeriPoslanePodatkeUpisParkiralista(String podaci) {
        boolean rezultat = false;

        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            String korisnickoIme = jsonObject.get("korisnik").toString();
            String lozinka = jsonObject.get("lozinka").toString();
            String naziv = jsonObject.get("naziv").toString();
            String adresa = jsonObject.get("adresa").toString();
            String kapacitet = jsonObject.get("kapacitet").toString();
            rezultat = true;
            return rezultat;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean provjeriKorisnickePodatke(String podaci) {
        dohvatiPodatke();
        JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
        String korisnickoIme = jsonObject.get("korisnik").toString().replace("\"", "");
        String lozinkaKorisika = jsonObject.get("lozinka").toString().replace("\"", "");
        boolean povratnaInformacija = false;
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            String upit = "Select * from  korisnici where "
                    + "kor_ime=" + '"' + korisnickoIme + '"' + " and lozinka=" + '"' + lozinkaKorisika + '"';
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            ResultSet rs = stm.executeQuery(upit);
            if (rs.next()) {
                povratnaInformacija = true;
            }
            rs.close();
            stm.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(ParkiralistaREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return povratnaInformacija;
    }

    private void pisiUDnevnik(String korisnickoIme, String urlAdresa, String akcija, int status) {
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
            String upit1 = " INSERT INTO nwtis_zorgrdjan_bp_1.dnevnik (`korisnik`, `url`,`akcija`,`status`) values("
                    + "'" + korisnickoIme + "',"
                    + "'" + urlAdresa + "',"
                    + "'" + akcija + "',"
                    + status + ")";
            stm.execute(upit1);
            stm.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    private void dodajParkiraliste(int id, String podaci) {
        dohvatiPodatke();
        JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
        String naziv = jsonObject.get("naziv").toString().replace("\"", "");;
        String adresa = jsonObject.get("adresa").toString().replace("\"", "");;
        String kapacitet = jsonObject.get("kapacitet").toString().replace("\"", "");;
        GMKlijent klijent = new GMKlijent(gmapikey);
        Lokacija lok = klijent.getGeoLocation(adresa);
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String upit1 = " INSERT INTO nwtis_zorgrdjan_bp_1.parkiralista (`id`, `naziv`,`adresa`,`longitude`,`latitude`,`kapacitet`) values("
                    + id + ","
                    + "'" + naziv + "',"
                    + "'" + adresa + "',"
                    + Float.parseFloat(lok.getLongitude()) + ","
                    + Float.parseFloat(lok.getLatitude()) + ","
                    + Integer.parseInt(kapacitet) + ")";
            stm.execute(upit1);
            stm.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    private boolean provjeriKorisnickePodatkeGet(String korisnickoIme, String lozinkaKorisnik) {
        dohvatiPodatke();
        boolean povratnaInformacija = false;
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            String upit = "Select * from  korisnici where "
                    + "kor_ime=" + '"' + korisnickoIme + '"' + " and lozinka=" + '"' + lozinkaKorisnik + '"';
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            ResultSet rs = stm.executeQuery(upit);
            if (rs.next()) {
                povratnaInformacija = true;
            }
            rs.close();
            stm.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(ParkiralistaREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return povratnaInformacija;
    }

}
