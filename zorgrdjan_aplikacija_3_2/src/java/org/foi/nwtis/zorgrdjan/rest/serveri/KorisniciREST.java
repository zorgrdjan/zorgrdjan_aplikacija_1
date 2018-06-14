/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.xml.rpc.processor.modeler.j2ee.xml.emptyType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.podaci.ListPojo;
import org.foi.nwtis.zorgrdjan.web.podaci.Korisnici;
import org.foi.nwtis.zorgrdjan.web.podaci.KorisnikPojo;
import org.foi.nwtis.zorgrdjan.web.podaci.OdgovorSviKorisnici;
import org.foi.nwtis.zorgrdjan.web.podaci.ParkiralistaPojo;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacijeTreci;

/**
 * REST Web Service
 *
 * @author Zoran
 */
@Path("korisnici")
public class KorisniciREST {

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private String adresa;
    private int port;

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of KorisniciREST
     */
    public KorisniciREST() {
    }

    /**
     * preuzimanje svih korisnika - vraća odgovor u application/json formatu.
     * Struktura odgovora je sljedeća: {"odgovor": [{...},{...}...], "status":
     * "OK" | "ERR", <ako je "ERR" onda se dodaje
     * "poruka": poruka>}. Ne vraća se lozinka! KORISNIK korisnik; LOZINKA
     * lozinka; LISTAJ;
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/korisnik/{korisnik}/lozinka/{lozinka}")
    public String getJson(@PathParam("korisnik") String korisnickoIme, @PathParam("lozinka") String lozinkaKorisnik) {
        Gson gson = new Gson();
        OdgovorSviKorisnici lista = new OdgovorSviKorisnici();
        dohvatiPodatke();
        String komanda = "KORISNIK " + korisnickoIme + "; LOZINKA " + lozinkaKorisnik + "; LISTAJ;";
        String odgovor = saljiKomandu(komanda);
        String povratnaPoruka;
        if (odgovor.equals("ERR 11")) {
            povratnaPoruka = "{\"odgovor\": [],"
                    + "\"status\": \"ERR\","
                    + "\"poruka\": \"Korisnik ne postoji!!\" } ";
        } else {
            System.out.println(odgovor);
            String[] obrada = odgovor.split("OK 10;");
            List<Korisnici> listaKorisnicka = new ArrayList<>();
            Korisnici[] array = gson.fromJson(obrada[1], Korisnici[].class);
            listaKorisnicka = Arrays.asList(array);
            lista.setOdgovor(listaKorisnicka);
            lista.setStatus("OK");
            povratnaPoruka = gson.toJson(lista);
        }
        return povratnaPoruka;
    }

    /**
     *
     * preuzimanje jednog korisnika - vraća odgovor u application/json formatu.
     * Struktura odgovora je sljedeća: {"odgovor": [{...}], "status": "OK" |
     * "ERR", <ako je "ERR" onda se dodaje
     * "poruka": poruka>}. Ne vraća se lozinka!
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/korisnik/{korisnik}/lozinka/{lozinka}")
    public String getJson(@PathParam("korisnik") String korisnikZaPretragu, @PathParam("korisnik") String korisnickoIme, @PathParam("lozinka") String lozinkaKorisnik) {
        dohvatiPodatke();
        Gson gson = new Gson();
        OdgovorSviKorisnici lista = new OdgovorSviKorisnici();
        String povratnaPoruka = "";
        String komanda = "KORISNIK " + korisnickoIme + "; LOZINKA " + lozinkaKorisnik + "; DOHVATIKORISNIKA;";
        String odgovor = saljiKomandu(komanda);
        List<KorisnikPojo> listaKorisnicka = new ArrayList<>();
        KorisnikPojo[] array = gson.fromJson(odgovor, KorisnikPojo[].class);
        listaKorisnicka = Arrays.asList(array);
        for (KorisnikPojo korisnikPojo : array) {
            if (korisnikPojo.getKorisnickoIme().equalsIgnoreCase(korisnikZaPretragu)) {
                String podaciKorisnik = gson.toJson(korisnikPojo);
                povratnaPoruka = "{\"odgovor\": [" + podaciKorisnik + "],"
                        + "\"status\": \"OK\"} ";
                break;
            }
        }
        if (povratnaPoruka.equals("")) {
            povratnaPoruka = "{\"odgovor\": [],"
                    + "\"status\": \"ERR\","
                    + "\"poruka\": \"Korisnik ne postoji!!\" } ";
        }
        return povratnaPoruka;
    }

    /**
     * dodavanje jednog korisnika - šalju se podaci u application/json formatu.
     * Vraća odgovor u application/json formatu. Struktura odgovora je sljedeća:
     * {"odgovor": [], "status": "OK" | "ERR",
     * <ako je "ERR" onda se dodaje "poruka": poruka>}. Ne vraća se lozinka!
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String postJson(@PathParam("korisnickoIme") String korisnickoIme, String podaci) {
        dohvatiPodatke();
        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            String kor_ime = jsonObject.get("korisnik").toString().replace("\"", "");
            String lozinka = jsonObject.get("lozinka").toString().replace("\"", "");
            String ime = jsonObject.get("ime").toString().replace("\"", "");
            String prezime = jsonObject.get("prezime").toString().replace("\"", "");
            String naredba = "KORISNIK " + kor_ime + "; LOZINKA " + lozinka + "; DODAJ \"" + prezime + "\" \"" + ime + "\";";
            System.out.println("Naredba je:" + naredba);
            String odgovor = saljiKomandu(naredba);
            if (odgovor.equalsIgnoreCase("OK 10")) {
                return "{\"odgovor\": [],"
                        + "\"status\": \"OK\"} ";
            } else {
                return "{\"odgovor\": [],"
                        + "\"status\": \"ERR\","
                        + "\"poruka\": \"Korisnicko ime vec postoji u bazi!!!\" } ";
            }
        } catch (Exception e) {
            return "{\"odgovor\": [],"
                    + "\"status\": \"ERR\","
                    + "\"poruka\": \"Pogresna sintaksa json dokumenta!!\" } ";
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/autentikacija")
    public String postJson(String podaci) {
        dohvatiPodatke();
        System.out.println("Tu sam ");
        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            String kor_ime = jsonObject.get("korisnik").toString().replace("\"", "");
            String lozinka = jsonObject.get("lozinka").toString().replace("\"", "");
            String naredba = "KORISNIK " + kor_ime + "; LOZINKA " + lozinka+";";
            System.out.println("Naredba:"+naredba);
            String odgovor = saljiKomandu(naredba);
            if (odgovor.equalsIgnoreCase("OK 10;")) {
                return "{\"odgovor\": [],"
                        + "\"status\": \"OK\"} ";
            } else {
                return "{\"odgovor\": [],"
                        + "\"status\": \"ERR\","
                        + "\"poruka\": \"Takav korisnik ne postoji u bazi!!!\" } ";
            }
        } catch (Exception e) {
            return "{\"odgovor\": [],"
                    + "\"status\": \"ERR\","
                    + "\"poruka\": \"Pogresna sintaksa json dokumenta!!\" } ";
        }

    }

    /**
     * ažuriranje jednog korisnika - šalju se podaci u application/json formatu.
     * Vraća odgovor u application/json formatu. Struktura odgovora je sljedeća:
     * {"odgovor": [], "status": "OK" | "ERR",
     * <ako je "ERR" onda se dodaje "poruka": poruka>}. Ne vraća se lozinka!
     *
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String putJson(@PathParam("korisnickoIme") String id, String podaci) {
        dohvatiPodatke();
        try {
            JsonObject jsonObject = new JsonParser().parse(podaci).getAsJsonObject();
            String kor_ime = jsonObject.get("korisnik").toString().replace("\"", "");
            String lozinka = jsonObject.get("lozinka").toString().replace("\"", "");
            String ime = jsonObject.get("ime").toString().replace("\"", "");
            String prezime = jsonObject.get("prezime").toString().replace("\"", "");
            String naredba = "KORISNIK " + kor_ime + "; LOZINKA " + lozinka + "; AZURIRAJ \"" + prezime + "\" \"" + ime + "\";";
            System.out.println("Naredba je:" + naredba);
            String odgovor = saljiKomandu(naredba);
            if (odgovor.equalsIgnoreCase("OK 10")) {
                return "{\"odgovor\": [],"
                        + "\"status\": \"OK\"} ";
            } else {
                return "{\"odgovor\": [],"
                        + "\"status\": \"ERR\","
                        + "\"poruka\": \"Korisnicko ne postoji!!!\" } ";
            }
        } catch (Exception e) {
            return "{\"odgovor\": [],"
                    + "\"status\": \"ERR\","
                    + "\"poruka\": \"Pogresna sintaksa json dokumenta!!\" } ";
        }

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String deleteJson(@PathParam("id") String id, String podaci) {
        return "";
    }

    private void dohvatiPodatke() {

        adresa = SlusacAplikacijeTreci.getSc().getAttribute("mail.server").toString();
        port = Integer.parseInt(SlusacAplikacijeTreci.getSc().getAttribute("port").toString());

    }

    public String saljiKomandu(String komanda) {
        // String komanda = "KORISNIK korisnik; LOZINKA lozinka; DODAJ \"prezime\" \"ime\"";
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; PAUZA;";
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; KRENI;";   
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; PASIVNO;";   
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; AKTIVNO;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; STANI;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; STANJE;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; LISTAJ;"; 
        try {
            Socket socket = new Socket(adresa, port);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            os.write(komanda.getBytes());
            os.flush();
            socket.shutdownOutput();
            StringBuffer buffer = new StringBuffer();
            int znak;
            while ((znak = is.read()) != -1) {
                buffer.append((char) znak);
            }
            System.out.println("Odgovor: " + buffer.toString());
            return (buffer.toString());
        } catch (IOException ex) {
            Logger.getLogger(KorisniciREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{}";
    }
}
