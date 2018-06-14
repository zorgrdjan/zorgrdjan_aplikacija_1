/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.zrna;

import org.foi.nwtis.zorgrdjan.ejb.eb.Dnevnik;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import org.foi.nwtis.zorgrdjan.ejb.sb.DnevnikFacade;

/**
 *
 * @author Zoran
 */
@Named(value = "upravljanjeNaredbama")
@SessionScoped
public class UpravljanjeNaredbama implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private String adresa = "127.0.0.1";
    private int port = 8000;
    String odgovorServera;

    /**
     * Creates a new instance of UpravljanjeNaredbama
     */
    public UpravljanjeNaredbama() {
    }

    public String getOdgovorServera() {
        return odgovorServera;
    }

    public void setOdgovorServera(String odgovorServera) {
        this.odgovorServera = odgovorServera;
    }
    public void korisnikDodaj() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; DODAJ \"prezime\" \"ime\";";
        saljiKomandu(naredba);
    }
    public void korisnikAzuriraj() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; AZURIRAJ \"poki\" \"zoki\";";
        saljiKomandu(naredba);
    }
    public void korisnikPauza() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; PAUZA;";
        saljiKomandu(naredba);
    }

    public void korisnikKreni() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; KRENI;";
        saljiKomandu(naredba);
    }

    public void korisnikPasivno() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; PASIVNO;";
        saljiKomandu(naredba);
    }

    public void korisnikAktivno() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; AKTIVNO;";

        saljiKomandu(naredba);
    }

    public void korisnikStani() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; STANI;";
        saljiKomandu(naredba);
    }

    public void korisnikStanje() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; STANJE;";
        saljiKomandu(naredba);
    }

    public void korisnikListaj() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; LISTAJ;";

        saljiKomandu(naredba);
    }

    public void grupaDodaj() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; GRUPA DODAJ;";

        saljiKomandu(naredba);
    }

    public void grupaPrekid() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; GRUPA PREKID;";

        saljiKomandu(naredba);
    }

    public void grupaKreni() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; GRUPA KRENI;";

        saljiKomandu(naredba);
    }

    public void grupaPauza() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; GRUPA PAUZA;";

        saljiKomandu(naredba);
    }

    public void grupaStanje() {
        String naredba = "KORISNIK zorgrdjan; LOZINKA 123456; GRUPA STANJE;";

        saljiKomandu(naredba);
    }
    public void pisiUDnevnik()
    {
        Dnevnik dnevnik= new Dnevnik();
        dnevnik.setId(1);
        dnevnik.setKorisnik("zoki");
        dnevnik.setStatus(1);
        dnevnik.setTrajanje(20);
        dnevnik.setUrl("neki url");
        dnevnik.setIpadresa("ip adresa");
        dnevnikFacade.create(dnevnik);
    }

    public void spojiSeNaServer() {

        try {
            socket = new Socket(adresa, port);

        } catch (IOException ex) {
            //     Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Spojio sam se na server");
    }

    public void saljiKomandu(String komanda) {
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
            setOdgovorServera(buffer.toString());
        } catch (IOException ex) {
            Logger.getLogger(UpravljanjeNaredbama.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
