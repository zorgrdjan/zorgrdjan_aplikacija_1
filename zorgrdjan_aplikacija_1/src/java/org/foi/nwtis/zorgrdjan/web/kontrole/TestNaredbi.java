/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.kontrole;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Zoran
 */
@ManagedBean
@SessionScoped
public class TestNaredbi {

    public Socket socket;
    public InputStream is;
    public OutputStream os;
    public String adresa = "127.0.0.1";
    public int port = 8000;

    /**
     * Creates a new instance of TestNaredbi
     */
    public TestNaredbi() {
    }

    @PostConstruct
    private void init() {
        spojiSeNaServer();
    }

    public void spojiSeNaServer() {

        try {
            socket = new Socket(adresa, port);

        } catch (IOException ex) {
            //     Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Spojio sam se na server");
    }

    public void saljiKomandu() {
        String komanda = "KORISNIK korisnik; LOZINKA lozinka; DODAJ \"prezime\" \"ime\"";
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; PAUZA;";
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; KRENI;";   
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; PASIVNO;";   
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; AKTIVNO;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; STANI;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; STANJE;"; 
//        String komanda="KORISNIK korisnik; LOZINKA lozinka; LISTAJ;"; 
        if(socket.isOutputShutdown())
        {
            spojiSeNaServer();
        }
        try {

            is = socket.getInputStream();

            os = socket.getOutputStream();
            ByteArrayOutputStream bajtStream = new ByteArrayOutputStream();
            os.write(komanda.getBytes());
            os.flush();
            socket.shutdownOutput();
            StringBuffer buffer=new StringBuffer();
            int znak;
            while((znak=is.read())!=-1)
            {
            buffer.append((char)znak);
            }
            System.out.println("Odgovor: "+buffer.toString() );
        } catch (IOException ex) {
            Logger.getLogger(TestNaredbi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
