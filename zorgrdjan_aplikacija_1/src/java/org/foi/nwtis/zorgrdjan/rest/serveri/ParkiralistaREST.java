/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.rest.serveri;

import java.sql.Connection;
import java.sql.Statement;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
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
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ParkiralistaREST
     */
    public ParkiralistaREST() {
    }

    /**
     * Retrieves representation of an instance of org.foi.nwtis.zorgrdjan.rest.serveri.ParkiralistaREST
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        //TODO return proper representation object
        return "bananananananna";
      //  throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of ParkiralistaREST
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
    
    
}
