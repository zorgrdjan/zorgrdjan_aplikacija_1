/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.dretve;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.rest.klijenti.OWMKlijent;
import org.foi.nwtis.zorgrdjan.web.podaci.Lokacija;
import org.foi.nwtis.zorgrdjan.web.podaci.MeteoPodaci;
import org.foi.nwtis.zorgrdjan.web.podaci.Parkiraliste;

/**
 *
 * @author Zoran
 */
public class ServerThread extends Thread {

    public boolean runThread = true;
    public ServerSocketThread serverSocketThread;
    private ServletContext servletContext;
    private int intervalDretveZaMeteoPodatke;

    public ServerThread(ServletContext sc) {
        this.servletContext = sc;
    }

    @Override
    public void interrupt() {
        setRunThread(false);
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        System.out.println("Pokrecem pozadinsku dretvu za preuzimanje geo meteo podataka!");
        intervalDretveZaMeteoPodatke=Integer.parseInt(servletContext.getAttribute("intervalDretveZaMeteoPodatke").toString())*1000;
        System.out.println("Interval za dretvu:"+intervalDretveZaMeteoPodatke);
        while (runThread) {
//            if (serverSocketThread.weatherDataPicker)
//            {
//                System.out.println("Trenutno sam u pauzi i ne preuzimam weather podatke");
//                
//            }
//            else {
//               System.out.println("Preuzimam nove weather podatke");
//            }
            try {
                System.out.println("Dretva spava");
                preuzmiMeteoPodatkeZaParkiralista();
                sleep(intervalDretveZaMeteoPodatke);
                System.out.println(System.currentTimeMillis());
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isRunThread() {
        return runThread;
    }

    public void setRunThread(boolean runThread) {
        this.runThread = runThread;
    }

    /**
     * Metoda koja dohvaca sva parkiralista iz baze podataka
     *
     * @return vraca listu parkiralista sa svim podacima
     */
    public List<Parkiraliste> dohvatiParkiralista() {
        List<Parkiraliste> popisParkiralista = new ArrayList<>();
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.parkiralista";
        try {
            Class.forName(konfiguracija.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Parkiraliste parkiraliste = new Parkiraliste();
                parkiraliste.setId(rs.getInt("id"));
                parkiraliste.setNaziv(rs.getString("naziv"));
                parkiraliste.setAdresa(rs.getString("adresa"));
                Lokacija lok = new Lokacija(rs.getString("longitude"), rs.getString("latitude"));
                parkiraliste.setGeoloc(lok);
                parkiraliste.setKapacitet(rs.getInt("kapacitet"));
                popisParkiralista.add(parkiraliste);
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }

        return popisParkiralista;
    }

    private void preuzmiMeteoPodatkeZaParkiralista() {
        String apikey = servletContext.getAttribute("apikey").toString();
        OWMKlijent klijent = new OWMKlijent(apikey);
        List<Parkiraliste> popisParkiralista = new ArrayList<>();
        popisParkiralista = dohvatiParkiralista();
        for (Parkiraliste parkiraliste : popisParkiralista) {
            MeteoPodaci podaci = klijent.getRealTimeWeather(parkiraliste.
                    getGeoloc().getLatitude(),
                    parkiraliste.getGeoloc().getLongitude());
            spremiMeteoPodatkeUBazu(podaci, parkiraliste);
        }
    }
    private void spremiMeteoPodatkeUBazu(MeteoPodaci podatak, Parkiraliste parkiraliste)
    {
         BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "INSERT INTO nwtis_zorgrdjan_bp_1.meteo(`id`, `adresaStanice`, "
                + "`latitude`, `longitude`, `vrijeme`, `vrijemeOpis`, `temp`, "
                + "`tempMin`, `tempMax`, `vlaga`, `tlak`, `vjetar`, `vjetarSmjer`"
                + ") "
                + "VALUES ("+parkiraliste.getId()+",'"+parkiraliste.getAdresa()+"',"
                + "'"+parkiraliste.getGeoloc().getLatitude()+"','"+parkiraliste.getGeoloc().getLongitude()+"',"
                + "'"+podatak.getCloudsName()+"',"
                + "'"+podatak.getWeatherValue()+"',"+podatak.getTemperatureValue()+","
                + ""+podatak.getTemperatureMin()+","+podatak.getTemperatureMax()+","+podatak.getHumidityValue()+","
                + ""+podatak.getPressureValue()+","+podatak.getWindSpeedValue()+","+podatak.getWindDirectionValue()+")";
        try {
            Class.forName(konfiguracija.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stmt = con.createStatement();
            stmt.executeUpdate(query);

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

}
