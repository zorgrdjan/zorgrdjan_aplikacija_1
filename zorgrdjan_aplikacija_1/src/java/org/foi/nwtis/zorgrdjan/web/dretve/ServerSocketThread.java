/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.dretve;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.pojoKlase.PopisLjudiPojo;
import org.foi.nwtis.zorgrdjan.ws.klijenti.GrupaWsKlijent;

/**
 *
 * @author Zoran
 */
public class ServerSocketThread extends Thread {

    private boolean runThread = true;
    public boolean runServerSocket = true;
    public boolean serverPause = false;
    public boolean weatherDataPicker = false;
    public boolean postupakPrekida = false;
    private ServerSocket serverSocket;
    private Socket socket;
    private final String korisnikPostojiError = "ERR 10"; //error korisnik već postoji
    private final String korisnikNePostojiError = "ERR 11"; //error korisnik ne postoji
    private final String serverVecUPauziError = "ERR 12"; //eror PAUZA 
    private final String serverNijeBioUPauziError = "ERR 13"; //ERROR kreni
    private final String serverVecPasivanError = "ERR 14"; //ERROR pasivno
    private final String serverVecAktivanError = "ERR 15"; //ERROR aktivno
    private final String serverJeUStaniError = "ERR 16"; //ERROR stani
    private final String vratiSveKorisnikeError = "ERR 17"; //ERROR listaj
    private final String grupaVecRegistriranaError = "ERR 20"; //ERROR grupa dodaj
    private final String grupaNijeRegistriranaError = "ERR 21"; //ERROR grupa prekid
    private final String grupaNePostojiError = "ERR 22"; //ERROR grupa kreni
    private final String grupaNijeAktivanError = "ERR 23"; //ERROR grupa kreni

    private List<PopisLjudiPojo> popisLjudi = new ArrayList<>();

    private ServletContext servletContext;

    public ServerSocketThread(ServletContext sc) {
        this.servletContext = sc;
    }

    @Override
    public void interrupt() {
        setRunThread(false);
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        System.out.println(System.currentTimeMillis());
        System.out.println("Pokrecem dretvu koja prima podatke na server socket!");
        int port = 8000;
        int maksCekanje = 10;
        getAllUsersFromDatabase();
        String komanda = "KORISNIK ivicelig; LOZINKA 123456; GRUPA STANJE;";
        checkCommand(komanda);
        checkGroupCommand(komanda);
//        try {
//            serverSocket = new ServerSocket(port, maksCekanje);
//            while (runServerSocket) {
//                socket = serverSocket.accept();
//                System.out.println("Korisnik se spojio!");
//               
//                try {
//                    InputStream is = socket.getInputStream();
//                    OutputStream os = socket.getOutputStream();
//                    StringBuffer buffer = new StringBuffer();
//                    int znak;
//                    while ((znak = is.read()) != -1) {
//                        buffer.append((char) znak);
//                    }
//                //    String ispis = buffer.toString();
//                    
//                    String odgovor = "Ovo ti je odgovor na spajanje";
//                    os.write(odgovor.getBytes());
//                    os.flush();
//                    socket.shutdownOutput();
//                    System.out.println("Dretva se vrti sjelo vrijeme!");
//               //     System.out.println("Ispis"+ispis);
//                } catch (IOException ex) {
//                    //   Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                //       System.out.println("Ukupni broj dretvi u sustavu:" + ukupniBrojDretvi); //pomocni system out
//            }
//        } catch (IOException ex) {
//            System.out.println("Uhvatili smo exception!");
//        }

//        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isWeatherDataPicker() {
        return weatherDataPicker;
    }

    public void setWeatherDataPicker(boolean weatherDataPicker) {
        this.weatherDataPicker = weatherDataPicker;
    }

    public synchronized boolean isServerPause() {
        return serverPause;
    }

    public synchronized void setServerPause(boolean serverPause) {
        this.serverPause = serverPause;
    }

    public boolean isRunThread() {
        return runThread;
    }

    public void setRunThread(boolean runThread) {
        this.runThread = runThread;
    }

    /**
     * Metoda dohvaca sve korisnike iz baze podataka
     */
    private void getAllUsersFromDatabase() {
        System.out.println("Ulazim u metodu");
        popisLjudi.clear();
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");

        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici";
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
                int id = rs.getInt("id");
                String ime = rs.getString("ime");
                String prezime = rs.getString("prezime");
                PopisLjudiPojo covjek = new PopisLjudiPojo();
                covjek.setKi(id);
                covjek.setIme(ime);
                covjek.setPrezime(prezime);
                popisLjudi.add(covjek);
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    private void checkCommand(String komanda) {
        if (komanda.contains("DODAJ")) {
            System.out.println("Komanda dodaj");
            String odgovor = doCommandDodaj(komanda);
            System.out.println("Odgovor komande dodaj:" + odgovor);
        } else if (komanda.contains("PAUZA")) {
            System.out.println("Komanda PAUZA");
            if (authenthicateUser(komanda)) {
                String odgovor = doCommandPauza();
                System.out.println(odgovor);
            } else {
                System.out.println("Odgovor:" + korisnikNePostojiError);
            }

        } else if (komanda.contains("KRENI")) {
            System.out.println("Komanda KRENI");
            if (authenthicateUser(komanda)) {
                String odgovor = doCommandKreni();
                System.out.println(odgovor);
            } else {
                System.out.println("Odgovor:" + korisnikNePostojiError);
            }
        } else if (komanda.contains("PASIVNO")) {
            System.out.println("Komanda PASIVNO");
            if (authenthicateUser(komanda)) {
                String odgovor = doCommandPasivno();
                System.out.println(odgovor);
            } else {
                System.out.println("Odgovor:" + korisnikNePostojiError);
            }
        } else if (komanda.contains("AKTIVNO")) {
            System.out.println("Komanda AKTIVNO");
            if (authenthicateUser(komanda)) {
                String odgovor = doCommandAktivno();
                System.out.println(odgovor);
            } else {
                System.out.println("Odgovor:" + korisnikNePostojiError);
            }
        } else if (komanda.contains("STANI")) {
            if (authenthicateUser(komanda)) {
                if (postupakPrekida) {
                    System.out.println(serverJeUStaniError);
                } else {
                    System.out.println("OK 10;");
                    System.out.println("Sad idem u sleep i gasim se!!!");
                    try {
                        sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerSocketThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    postupakPrekida = true;
                    System.exit(0);
                }
            } else {
                System.out.println("Odgovor:" + korisnikNePostojiError);
            }
        } else if (komanda.contains("STANJE")) {
            System.out.println("Komanda STANJE");
            //TODO dovršiti kad se slozi baratanje sa svim komandama , onda bu to ok :)
        } else if (komanda.contains("LISTAJ")) {
            System.out.println("Komanda LISTAJ");
            getAllUsersFromDatabase();
            String odgovor = dajPopisLjudiUJsonu();
            System.out.println("OK 10; " + odgovor);
        }
    }

    private String doCommandDodaj(String komanda) {
        String[] parts = komanda.split(";");
        String[] dijelovi = parts[2].split("\"");
        String[] korisnikUnos = parts[0].trim().split(" ");
        String[] lozinkaKorisnika = parts[1].trim().split(" ");

        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici where "
                + "kor_ime=" + '"' + korisnikUnos[1] + '"';
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
            if (!rs.isBeforeFirst()) {
                String upit1 = " INSERT INTO nwtis_zorgrdjan_bp_1.korisnici (`ime`, `prezime`,`kor_ime`,`lozinka`) values("
                        + "'" + dijelovi[3] + "',"
                        + "'" + dijelovi[1] + "',"
                        + "'" + korisnikUnos[1] + "',"
                        + "'" + lozinkaKorisnika[1] + "')";
                stmt.execute(upit1);
                rs.close();
                stmt.close();
                con.close();
                return "OK 10";
            } else {
                rs.close();
                stmt.close();
                con.close();
                return korisnikPostojiError;
            }

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return "";
    }

    private String doCommandPauza() {
        if (isServerPause()) {
            return serverVecUPauziError;
        } else {
            setServerPause(true);
            return "OK 10;";
        }

    }

    private boolean authenthicateUser(String komanda) {
        String[] parts = komanda.split(";");
        String[] dijelovi = parts[2].split("\"");
        String[] korisnikUnos = parts[0].trim().split(" ");
        String[] lozinkaKorisnika = parts[1].trim().split(" ");
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici where "
                + "kor_ime=" + '"' + korisnikUnos[1] + '"' + "and lozinka=" + "'" + lozinkaKorisnika[1] + "'";
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
            if (!rs.isBeforeFirst()) {
                rs.close();
                stmt.close();
                con.close();
                return false;
            } else {
                rs.close();
                stmt.close();
                con.close();
                return true;
            }

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return false;
    }

    private String doCommandKreni() {
        if (isServerPause()) {
            setServerPause(false);
            return "OK 10";
        } else {

            return serverNijeBioUPauziError;
        }
    }

    private String doCommandPasivno() {
        if (isWeatherDataPicker()) {
            return serverVecPasivanError;
        } else {
            setWeatherDataPicker(true);
            return "OK 10;";
        }
    }

    private String doCommandAktivno() {
        if (isWeatherDataPicker()) {
            setWeatherDataPicker(false);
            return "OK 10;";
        } else {
            return serverVecAktivanError;
        }
    }

    private String dajPopisLjudiUJsonu() {
        Gson gson = new Gson();
        String jsonZapisLjudi = gson.toJson(popisLjudi);
        return jsonZapisLjudi;
    }

    private void checkGroupCommand(String komanda) {
        if (authenthicateUser(komanda)) {
            if (komanda.contains("GRUPA DODAJ")) {
                System.out.println(registrirajGrupu());
            } else if (komanda.contains("GRUPA PREKID")) {
                System.out.println(deregistrirajGrupu());
            } else if (komanda.contains("GRUPA KRENI")) {
                System.out.println(aktivirajGrupu());
            } else if (komanda.contains("GRUPA PAUZA")) {
                System.out.println(pauzirajGrupu());
            } else if (komanda.contains("GRUPA STANJE")) {
                System.out.println(dajStanjeGrupe());
            }
        } else {
            System.out.println(korisnikNePostojiError);
        }
    }

    private String registrirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        grupa.autenticirajGrupu("zorgrdjan", "b5pp4rkn");
        boolean statusRegistracija = grupa.registrirajGrupu("zorgrdjan", "b5pp4rkn");
        System.out.println(statusRegistracija);
        if (statusRegistracija) {
            return "OK 20;";
        } else {
            return grupaVecRegistriranaError;
        }
    }

    private String deregistrirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        grupa.autenticirajGrupu("zorgrdjan", "b5pp4rkn");
        boolean statusDeregistracija = grupa.deregistrirajGrupu("zorgrdjan", "b5pp4rkn");
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNijeRegistriranaError;
        }
    }

    private String aktivirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        boolean statusDeregistracija = grupa.aktivirajGrupu("zorgrdjan", "b5pp4rkn");
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNePostojiError;
        }
    }

    private String pauzirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        boolean statusDeregistracija = grupa.blokirajGrupu("zorgrdjan", "b5pp4rkn");
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNijeAktivanError;
        }
    }

    private String dajStanjeGrupe() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        String status = grupa.dajStatusGrupe("zorgrdjan", "b5pp4rkn").toString();
        if (status.equalsIgnoreCase("AKTIVAN")) {
            return "OK 21";
        } else if (status.equalsIgnoreCase("BLOKIRAN")) {
            return "OK 22;";
        } else {
            return grupaNijeRegistriranaError;
        }
    }

    private void saljiMail() {
        String predmetPoruke = servletContext.getAttribute("predmetporuke").toString();
        String adresaPrimatelja = servletContext.getAttribute("adresaprimatelja").toString();
        String adresaPosiljatelja = servletContext.getAttribute("adresaposiljatenja").toString();
        
    }
}
