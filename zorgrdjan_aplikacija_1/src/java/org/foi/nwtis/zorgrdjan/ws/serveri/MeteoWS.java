/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.rest.klijenti.OWMKlijent;
import org.foi.nwtis.zorgrdjan.web.podaci.MeteoPodaci;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Zoran
 */
@WebService(serviceName = "MeteoWS")
public class MeteoWS {

    String url;
    String korisnik;
    String lozinka;
    Connection con;
    Statement stm;
    BP_Konfiguracija bpk;
    String apikey;
    String gmApiKey;
    @Resource
    private WebServiceContext context;

    @WebMethod(operationName = "dajVazeceMeteoPodatke")
    public MeteoPodaci dajVazeceMeteoPodatke(@WebParam(name = "id") int id) {
        dohvatiPodatke();

        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String query = "SELECT * FROM METEO where id=" + id;
            ResultSet rs1 = stm.executeQuery(query);
            String json = "";
            String latitude = "";
            String longitude = "";
            if (rs1.next()) {
                ResultSet rs = stm.executeQuery(query);
                while (rs.next()) {
                    latitude = rs.getString("latitude");
                    longitude = rs.getString("longitude");

                }
                OWMKlijent owm = new OWMKlijent(apikey);
                MeteoPodaci meteo = owm.getRealTimeWeather(latitude, longitude);
                rs1.close();
                rs.close();
                stm.close();
                con.close();
                return meteo;
            } else {
                rs1.close();
                stm.close();
                con.close();
                return null;

            }
//            }

        } catch (SQLException ex) {
            Logger.getLogger(MeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Web service operation Operacija koja vraca zadnje meteo podatke iz baze
     */
    @WebMethod(operationName = "dajZadnjeMeteoPodatke")
    public MeteoPodaci dajZadnjeMeteoPodatke(@WebParam(name = "id") int id) {
        dohvatiPodatke();
        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String query = "SELECT * FROM METEO where id=" + id;
            ResultSet rs1 = stm.executeQuery(query);
            String json = "";
            if (rs1.next()) {
                ResultSet rs = stm.executeQuery(query);
                while (rs.next()) {
                    sviMeteoPodaci.add(new MeteoPodaci(null, null,
                            rs.getFloat("temp"), rs.getFloat("tempmin"), rs.getFloat("tempmax"), null,
                            rs.getFloat("vlaga"), null,
                            rs.getFloat("tlak"), null,
                            rs.getFloat("vjetar"), null, null, null, null,
                            rs.getInt("vjetarsmjer"), rs.getString("vrijeme"),
                             null,
                            null, null, null,
                            0, rs.getString("vrijemeopis"), null, rs.getTimestamp("preuzeto")));
                }
                rs1.close();
                rs.close();
                stm.close();
                con.close();
                return sviMeteoPodaci.get(sviMeteoPodaci.size() - 1);
            } else {
                rs1.close();
                stm.close();
                con.close();
                return null;
            }
//            }

        } catch (SQLException ex) {
            Logger.getLogger(MeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @WebMethod(operationName = "dajZadnjihNMeteoPodataka")
    public List<MeteoPodaci> dajZadnjihNMeteoPodataka(@WebParam(name = "id") int id, @WebParam(name = "brojZapisa") int brojZapisa) {
        dohvatiPodatke();
        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String query = "SELECT * FROM METEO where id=" + id;
            ResultSet rs1 = stm.executeQuery(query);
            String json = "";
            if (rs1.next()) {
                ResultSet rs = stm.executeQuery(query);
                while (rs.next()) {
                    sviMeteoPodaci.add(new MeteoPodaci(null, null,
                            rs.getFloat("temp"), rs.getFloat("tempmin"), rs.getFloat("tempmax"), null,
                            rs.getFloat("vlaga"), null,
                            rs.getFloat("tlak"), null,
                            rs.getFloat("vjetar"), null, null, null, null,
                            rs.getInt("vjetarsmjer"), rs.getString("vrijeme"),
                             null,
                            null, null, null,
                            0, rs.getString("vrijemeopis"), null, rs.getTimestamp("preuzeto")));
                }
                rs1.close();
                rs.close();
                stm.close();
                con.close();
                //   return sviMeteoPodaci.get(sviMeteoPodaci.size()-1);
                if (sviMeteoPodaci.size() >= brojZapisa) {
                    return sviMeteoPodaci.subList(sviMeteoPodaci.size() - brojZapisa, sviMeteoPodaci.size());
                }
                //   myList.subList(myList.size()-3, myList().size());
            } else {
                rs1.close();
                stm.close();
                con.close();
                return null;
            }
//            }

        } catch (SQLException ex) {
            Logger.getLogger(MeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @WebMethod(operationName = "dajSveMeteoPodatke_1")
    @RequestWrapper(className = "org.dajSveMeteoPodatke_1")
    @ResponseWrapper(className = "org.dajSveMeteoPodatke_1Response")
    public java.util.List<MeteoPodaci> dajSveMeteoPodatke(@WebParam(name = "id") int id, @WebParam(name = "od") long vOd, @WebParam(name = "do") long vDo) {
        dohvatiPodatke();
        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();
        try {
            Class.forName(bpk.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            Timestamp vrijemeOd = new Timestamp(vOd * 1000);
            Timestamp vrijemeDo = new Timestamp(vDo * 1000);
            System.out.println("Vrijeme od " + vrijemeOd + " Vrijeme do:" + vrijemeDo);
            //    SELECT * FROM NWTIS_G3.METEO where id=1 and PREUZETO>'2018-05-04 19:00:00' and PREUZETO<'2018-05-04 19:52:00'
            String query = "SELECT * FROM METEO where id=" + id
                    + " and preuzeto>" + "'" + vrijemeOd + "'" + " and preuzeto<" + "'" + vrijemeDo + "'";
            ResultSet rs1 = stm.executeQuery(query);
            String json = "";
            if (rs1.next()) {
                ResultSet rs = stm.executeQuery(query);
                while (rs.next()) {
                    sviMeteoPodaci.add(new MeteoPodaci(null, null,
                            rs.getFloat("temp"), rs.getFloat("tempmin"), rs.getFloat("tempmax"), null,
                            rs.getFloat("vlaga"), null,
                            rs.getFloat("tlak"), null,
                            rs.getFloat("vjetar"), null, null, null, null,
                            rs.getInt("vjetarsmjer"), rs.getString("vrijeme"),
                             null,
                            null, null, null,
                            0, rs.getString("vrijemeopis"), null, rs.getTimestamp("preuzeto")));
                }
                rs1.close();
                rs.close();
                stm.close();
                con.close();
                return sviMeteoPodaci;
            } else {
                rs1.close();
                stm.close();
                con.close();
                return null;
            }
//            }

        } catch (SQLException ex) {
            Logger.getLogger(MeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void dohvatiPodatke() {

        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        apikey = SlusacAplikacije.getSc().getAttribute("apikey").toString();
        gmApiKey = SlusacAplikacije.getSc().getAttribute("gmapikey").toString();

    }
}
