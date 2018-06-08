/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.ws.klijenti;

/**
 *
 * @author Zoran
 */
public class GrupaWsKlijent {

    public static Boolean autenticirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.autenticirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean registrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.registrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean aktivirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.aktivirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean deregistrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.deregistrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean blokirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.blokirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static StatusKorisnika dajStatusGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.zorgrdjan.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.dajStatusGrupe(korisnickoIme, korisnickaLozinka);
    }
    
    
}
