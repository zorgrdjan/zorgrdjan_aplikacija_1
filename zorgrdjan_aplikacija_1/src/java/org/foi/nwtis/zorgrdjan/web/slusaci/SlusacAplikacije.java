/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.slusaci;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.zorgrdjan.konfiguracije.Konfiguracija;
import org.foi.nwtis.zorgrdjan.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.zorgrdjan.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.zorgrdjan.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.dretve.ServerThread;

/**
 * Web application lifecycle listener.
 *
 * @author Zoran
 */
public class SlusacAplikacije implements ServletContextListener {

    private ServerThread serverThread;
    private static ServletContext sc;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String datoteka = sc.getInitParameter("konfiguracija");
        String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
        BP_Konfiguracija bpk = new BP_Konfiguracija(putanja + datoteka);
        sc.setAttribute("BP_Konfig", bpk);
        sc.setAttribute("putanja", putanja);
        System.out.println("Podatak:" + bpk.getAdminDatabase());
        try {
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanja + datoteka);
            preuzmiKonfiguraciju(sc, konf);
            //       ObradaPoruka obrada=new ObradaPoruka(konf,bpk);
            //       obrada.start();
            serverThread = new ServerThread(sc);
            serverThread.start();
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            //  Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ServletContext getSc() {
        return sc;
    }

    public static void setSc(ServletContext sc) {
        SlusacAplikacije.sc = sc;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        serverThread.runThread = false;
        serverThread.serverSocketThread.runServerSocket = false;
        System.out.println("Dretve su ugasene!");
    }

    private void preuzmiKonfiguraciju(ServletContext sc, Konfiguracija konf) {
        sc.setAttribute("intervalDretveZaMeteoPodatke", konf.dajPostavku("intervalDretveZaMeteoPodatke"));
        sc.setAttribute("apikey", konf.dajPostavku("apikey"));
        sc.setAttribute("predmetporuke", konf.dajPostavku("predmetporuke"));
        sc.setAttribute("adresaprimatelja", konf.dajPostavku("adresaprimatelja"));
        sc.setAttribute("adresaposiljatenja", konf.dajPostavku("adresaposiljatenja"));
        sc.setAttribute("gmapikey", konf.dajPostavku("gmapikey"));
        sc.setAttribute("korisnikSVN", konf.dajPostavku("korisnikSVN"));
        sc.setAttribute("lozinkaSVN", konf.dajPostavku("lozinkaSVN"));
        
    }
}
