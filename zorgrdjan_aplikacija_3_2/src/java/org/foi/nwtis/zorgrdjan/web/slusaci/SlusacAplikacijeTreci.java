/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.slusaci;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.zorgrdjan.konfiguracije.Konfiguracija;
import org.foi.nwtis.zorgrdjan.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.zorgrdjan.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.zorgrdjan.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;

/**
 * Web application lifecycle listener.
 *
 * @author Zoran
 */
public class SlusacAplikacijeTreci implements ServletContextListener {

    private static ServletContext sc;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            sc = sce.getServletContext();
            String datoteka = sc.getInitParameter("konfiguracija");
            String putanja = sc.getRealPath("/WEB-INF") + java.io.File.separator;
            BP_Konfiguracija bpk = new BP_Konfiguracija(putanja + datoteka);
            sc.setAttribute("BP_Konfig", bpk);
            sc.setAttribute("putanja", putanja);
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanja + datoteka);
            preuzmiKonfiguraciju(sc, konf);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(SlusacAplikacijeTreci.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacijeTreci.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
    }

    public static ServletContext getSc() {
        return sc;
    }

    public static void setSc(ServletContext sc) {
        SlusacAplikacijeTreci.sc = sc;
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
        sc.setAttribute("stranicenje", konf.dajPostavku("stranicenje"));
        sc.setAttribute("port", konf.dajPostavku("port"));
        sc.setAttribute("mail.server", konf.dajPostavku("mail.server"));
        sc.setAttribute("mail.attachmentFilename", konf.dajPostavku("mail.attachmentFilename"));

    }
}
