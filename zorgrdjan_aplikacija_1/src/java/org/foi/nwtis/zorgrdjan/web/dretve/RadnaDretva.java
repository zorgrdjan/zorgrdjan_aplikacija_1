/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.dretve;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;
import org.foi.nwtis.zorgrdjan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.zorgrdjan.web.podaci.Korisnici;
import org.foi.nwtis.zorgrdjan.web.podaci.KorisnikPojo;
import org.foi.nwtis.zorgrdjan.web.pojoKlase.KomandaJson;
import org.foi.nwtis.zorgrdjan.web.pojoKlase.PopisLjudiPojo;
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.zorgrdjan.ws.klijenti.GrupaWsKlijent;

/**
 *
 * @author Zoran
 */
public class RadnaDretva extends Thread {

    private boolean runThread = true;
    public boolean runServerSocket = true;
    public boolean serverPause = false;
    public boolean weatherDataPicker = false;
    public boolean postupakPrekida = false;

    private String korisnikSVN;
    private String lozinkaSVN;
    Connection con;
    Statement stm;

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
    private List<KorisnikPojo> sviKorisnici = new ArrayList<>();

    private ServletContext servletContext;
    private Socket socket;
    private Date vrijemePreuzimanja;
    private String odgovor;
    private int brojPoruke;

    public RadnaDretva(Socket socket, Date vrijeme, int brojPoruke) {
        this.socket = socket;
        this.servletContext = SlusacAplikacije.getSc();
        this.serverPause = ServerSocketThread.serverPause;
        this.weatherDataPicker = ServerSocketThread.weatherDataPicker;
        this.postupakPrekida = ServerSocketThread.postupakPrekida;
        this.korisnikSVN = SlusacAplikacije.getSc().getAttribute("korisnikSVN").toString();
        this.lozinkaSVN = SlusacAplikacije.getSc().getAttribute("lozinkaSVN").toString();
        this.vrijemePreuzimanja = vrijeme;
        this.brojPoruke = brojPoruke;

    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            StringBuffer buffer = new StringBuffer();
            int znak;
            while ((znak = is.read()) != -1) {
                buffer.append((char) znak);
            }
            String komanda = buffer.toString();
            System.out.println("Komanda je:" + komanda);
            if (provjeriSintaksuKomande(komanda)) {
                if (provjeriSintaksuAuthenticate(komanda)) {
                    odgovor = doCommandAuthenticateOnly(komanda);
                } else if (provjeriDodajNaredbu(komanda)) {
                    odgovor = doCommandDodaj(komanda);
                } else if (authenthicateUser(komanda)) {
                    provjeriKomandu(komanda);
                } else {
                    odgovor = korisnikNePostojiError;
                }
            } else {
                odgovor = "Neispravna sintaksa komande";
            }
            os.write(odgovor.getBytes());
            os.flush();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isWeatherDataPicker() {
        return weatherDataPicker;
    }

    public void setWeatherDataPicker(boolean weatherDataPicker) {
        ServerSocketThread.weatherDataPicker = weatherDataPicker;
    }

    public synchronized boolean isServerPause() {
        return serverPause;
    }

    public synchronized void setServerPause(boolean serverPause) {
        ServerSocketThread.serverPause = serverPause;
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
        if (komanda.contains("AZURIRAJ")) {
            odgovor = doCommandAzuriraj(komanda);
        } else if (komanda.contains("PAUZA")) {
            odgovor = doCommandPauza();
        } else if (komanda.contains("KRENI")) {
            odgovor = doCommandKreni();
        } else if (komanda.contains("PASIVNO")) {
            odgovor = doCommandPasivno();
        } else if (komanda.contains("AKTIVNO")) {
            odgovor = doCommandAktivno();
        } else if (komanda.contains("STANI")) {
            if (postupakPrekida) {
                odgovor = serverJeUStaniError;
            } else {
                ServerSocketThread.runServerSocket = false;
                postupakPrekida = true;
                odgovor = "OK 10;";
            }
        } else if (komanda.contains("STANJE")) {
            odgovor = doCommmandStanje();
        } else if (komanda.contains("LISTAJ")) {
            getAllUsersFromDatabase();
            odgovor = "OK 10;" + dajPopisLjudiUJsonu();
        } else if (komanda.contains("DOHVATIKORISNIKA")) {
            getAllUsersFromDatabaseKorisnik();
            odgovor = dajPopisLjudiUJsonuKorisnici();
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
                pisiUDnevnik(komanda);
                saljiMail(komanda);
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

    private String dajPopisLjudiUJsonuKorisnici() {
        Gson gson = new Gson();
        String jsonZapisKorisnika = gson.toJson(sviKorisnici);
        return jsonZapisKorisnika;
    }

    private void checkGroupCommand(String komanda) {
        if (komanda.contains("GRUPA DODAJ")) {
            odgovor = registrirajGrupu();
        } else if (komanda.contains("GRUPA PREKID")) {
            odgovor = deregistrirajGrupu();
        } else if (komanda.contains("GRUPA KRENI")) {
            odgovor = aktivirajGrupu();
        } else if (komanda.contains("GRUPA PAUZA")) {
            odgovor = pauzirajGrupu();
        } else if (komanda.contains("GRUPA STANJE")) {
            odgovor = dajStanjeGrupe();
        }
    }

    private String registrirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        grupa.autenticirajGrupu(korisnikSVN, lozinkaSVN);
        boolean statusRegistracija = grupa.registrirajGrupu(korisnikSVN, lozinkaSVN);
        System.out.println(statusRegistracija);
        if (statusRegistracija) {
            return "OK 20;";
        } else {
            return grupaVecRegistriranaError;
        }
    }

    private String deregistrirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        grupa.autenticirajGrupu(korisnikSVN, lozinkaSVN);
        boolean statusDeregistracija = grupa.deregistrirajGrupu(korisnikSVN, lozinkaSVN);
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNijeRegistriranaError;
        }
    }

    private String aktivirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        boolean statusDeregistracija = grupa.aktivirajGrupu(korisnikSVN, lozinkaSVN);
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNePostojiError;
        }
    }

    private String pauzirajGrupu() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        boolean statusDeregistracija = grupa.blokirajGrupu(korisnikSVN, lozinkaSVN);
        if (statusDeregistracija) {
            return "OK 20;";
        } else {
            return grupaNijeAktivanError;
        }
    }

    private String dajStanjeGrupe() {
        GrupaWsKlijent grupa = new GrupaWsKlijent();
        //     String status= (grupa.dajStatusGrupe(korisnikSVN, lozinkaSVN)).toString();
        System.out.println(grupa.dajStatusGrupe(korisnikSVN, lozinkaSVN));
        String status = "";
        if (status.equalsIgnoreCase("AKTIVAN")) {
            return "OK 21";
        } else if (status.equalsIgnoreCase("BLOKIRAN")) {
            return "OK 22;";
        } else if (status.equalsIgnoreCase("DEREGISTRIRAN")) {
            return grupaNijeRegistriranaError;
        } else if (status.equalsIgnoreCase("REGISTRIRAN")) {
            return "OK 21";
        } else {
            return grupaNijeRegistriranaError;
        }
    }

    private void saljiMail(String komanda) {
        String predmetPoruke = SlusacAplikacije.getSc().getAttribute("predmetporuke").toString();
        String adresaPrimatelja = SlusacAplikacije.getSc().getAttribute("adresaprimatelja").toString();
        String adresaPosiljatelja = SlusacAplikacije.getSc().getAttribute("adresaposiljatenja").toString();
        String posluzitelj = SlusacAplikacije.getSc().getAttribute("mail.server").toString();
        String porukaZaSlanje = pretvoriKomanduJson(komanda);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            Session session = Session.getInstance(properties, null);
            MimeMessage message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(adresaPosiljatelja);
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(adresaPrimatelja);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(predmetPoruke);
            message.setText(porukaZaSlanje);
            Multipart multipart = new MimeMultipart();
            String attachmentPoruka = porukaZaSlanje;
            MimeBodyPart attachment = new MimeBodyPart();
            DataSource poruka = new ByteArrayDataSource(attachmentPoruka.getBytes("UTF-8"), "application/json");
            attachment.setDataHandler(new DataHandler(poruka));
            attachment.setFileName(SlusacAplikacije.getSc().getAttribute("mail.attachmentFilename").toString());
            multipart.addBodyPart(attachment);
            message.setContent(multipart);
            Transport.send(message);

        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    

    private void provjeriKomandu(String komanda) {
        String sintaksa = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (PAUZA|KRENI|PASIVNO|AKTIVNO|STANI|STANJE|LISTAJ|DOHVATIKORISNIKA);$";
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(komanda);

        String sintaksa1 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (AZURIRAJ) \"([^\\s]+)\" \"([^\\s]+)\";$";
        Pattern patternDodajAzuriraj = Pattern.compile(sintaksa1);
        Matcher matcherDodajAzuriraj = patternDodajAzuriraj.matcher(komanda);

        boolean status = m.matches();
        boolean statusDodajAzuriraj = matcherDodajAzuriraj.matches();
        if (status) {
            checkCommand(komanda);
            saljiMail(komanda);
            pisiUDnevnik(komanda);
        } else if (statusDodajAzuriraj) {
            checkCommand(komanda);
            saljiMail(komanda);
            pisiUDnevnik(komanda);
            //dodaj korIme group(1) lozinka(group2) naredba(group3) azuriraj ime group(4) prezime(group(5)
        } else {
            provjeriKomanduGrupa(komanda);

        }
    }

    private void provjeriKomanduGrupa(String komanda) {
        String sintaksa = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); GRUPA (DODAJ|PREKID|KRENI|PAUZA|STANJE);$";
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(komanda);
        boolean status = m.matches();
        if (status && !serverPause) {
            checkGroupCommand(komanda);
            saljiMail(komanda);
            pisiUDnevnik(komanda);
        } else if (serverPause) {
            odgovor = "Server je u pauzi";
        } else {
            odgovor = "Pogresni format naredbe";
        }
    }

    private String doCommmandStanje() {
        if (!serverPause && !weatherDataPicker) {
            return "OK 11;";
        } else if (!serverPause && weatherDataPicker) {
            return "OK 12;";
        } else if (serverPause && !weatherDataPicker) {
            return "OK 13;";
        } else if (serverPause && weatherDataPicker) {
            return "OK 14;";
        }
        return "";
    }

    private String doCommandAzuriraj(String komanda) {
        String[] parts = komanda.split(";");
        String[] dijelovi = parts[2].split("\"");
        String[] korisnikUnos = parts[0].trim().split(" ");
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        try {
            Class.forName(konfiguracija.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            System.out.println(dijelovi[3] + dijelovi[1] + korisnikUnos[1]);
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stmt = con.createStatement();
            String upit1 = " UPDATE nwtis_zorgrdjan_bp_1.korisnici SET `ime`="
                    + "'" + dijelovi[3] + "',"
                    + " `prezime`= '" + dijelovi[1] + "'"
                    + " WHERE kor_ime=" + "\"" + korisnikUnos[1] + "\"";
            stmt.executeUpdate(upit1);
            stmt.close();
            con.close();
            return "OK 10";

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return "OK 10;";
    }

    private String pretvoriKomanduJson(String komanda) {
        String[] parts = komanda.split(";");
        String[] dijelovi = parts[2].split("\"");
        String[] korisnikUnos = parts[0].trim().split(" ");
        String[] lozinkaKorisnika = parts[1].trim().split(" ");
        String komandaBezLozinke = komanda.replace(" LOZINKA " + lozinkaKorisnika[1] + ";", "");
        System.out.println("Komanda broj: " + brojPoruke + " bez lozinke:" + komandaBezLozinke);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.zzz"); //yyyy.MM.dd hh:mm:ss.zzz
        String datum = dateFormat.format(vrijemePreuzimanja);
        KomandaJson novaKomanda = new KomandaJson();
        novaKomanda.setId(brojPoruke);
        novaKomanda.setKomanda(komandaBezLozinke);
        novaKomanda.setVrijeme(datum);
        Gson gson = new Gson();
        String povratnaKomanda = gson.toJson(novaKomanda);
        return povratnaKomanda;
    }

    private void pisiUDnevnik(String komanda) {
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        String[] parts = komanda.split(";");
        String[] dijelovi = parts[2].split("\"");
        String[] korisnikUnos = parts[0].trim().split(" ");
        Connection con;
        Statement stmt;
        try {
            Class.forName(konfiguracija.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String upit1 = " INSERT INTO nwtis_zorgrdjan_bp_1.dnevnik (`korisnik`, `url`,`akcija`,`status`) values("
                    + "'" + korisnikUnos[1] + "',"
                    + "'" + "',"
                    + "'" + komanda + "',"
                    + 2 + ")";
            stm.execute(upit1);
            stm.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    /**
     * Metoda dohvaca sve korisnike iz baze podataka
     */
    private void getAllUsersFromDatabaseKorisnik() {
        sviKorisnici.clear();
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
                String korIme = rs.getString("kor_ime");
                KorisnikPojo korisnikZapis = new KorisnikPojo();
                korisnikZapis.setKid(id);
                korisnikZapis.setIme(ime);
                korisnikZapis.setPrezime(prezime);
                korisnikZapis.setKorisnickoIme(korIme);
                sviKorisnici.add(korisnikZapis);
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    private boolean provjeriDodajNaredbu(String komanda) {
        String sintaksa = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (DODAJ) \"([^\\s]+)\" \"([^\\s]+)\";$";
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(komanda);
        boolean status = m.matches();
        return status;
    }

    private boolean provjeriSintaksuKomande(String komanda) {
        String sintaksa = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (PAUZA|KRENI|PASIVNO|AKTIVNO|STANI|STANJE|LISTAJ|DOHVATIKORISNIKA);$";
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(komanda);

        String sintaksa1 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); (DODAJ|AZURIRAJ) \"([^\\s]+)\" \"([^\\s]+)\";$";
        Pattern patternDodajAzuriraj = Pattern.compile(sintaksa1);
        Matcher matcherDodajAzuriraj = patternDodajAzuriraj.matcher(komanda);

        String sintaksa2 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+); GRUPA (DODAJ|PREKID|KRENI|PAUZA|STANJE);$";
        Pattern patternGrupa = Pattern.compile(sintaksa2);
        Matcher mathcherGrupa = patternGrupa.matcher(komanda);
        boolean statusPosluzitelj = m.matches();

        String sintaksa3 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+);$";

        Pattern patternAutentikacija = Pattern.compile(sintaksa3);
        Matcher mAutentikacija = patternAutentikacija.matcher(komanda);
        boolean statusAutentikacija = mAutentikacija.matches();

        boolean statusGrupa = mathcherGrupa.matches();
        boolean statusDodajAzuriraj = matcherDodajAzuriraj.matches();
        if (statusPosluzitelj || statusGrupa || statusDodajAzuriraj || statusAutentikacija) {
            return true;
        } else {
            return false;
        }
    }

    private boolean provjeriSintaksuAuthenticate(String komanda) {
        String sintaksa3 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+);$";

        Pattern patternAutentikacija = Pattern.compile(sintaksa3);
        Matcher mAutentikacija = patternAutentikacija.matcher(komanda);
        boolean statusAutentikacija = mAutentikacija.matches();

        return statusAutentikacija;
    }

    private String doCommandAuthenticateOnly(String komanda) {
        String sintaksa3 = "^KORISNIK ([^\\s]+); LOZINKA ([^\\s]+);$";

        Pattern patternAutentikacija = Pattern.compile(sintaksa3);
        Matcher mAutentikacija = patternAutentikacija.matcher(komanda);
        boolean statusAutentikacija = mAutentikacija.matches();
        if (authenthicateUserCommand(mAutentikacija.group(1), mAutentikacija.group(2))) {
            saljiMailAuthenthicate(komanda,mAutentikacija.group(2));
            pisiUDnevnikAuthenticate(komanda,mAutentikacija.group(1));
            return "OK 10;";
        } else {
            return "ERR 11;";
        }
    }

    private boolean authenthicateUserCommand(String kor_ime, String kor_lozinka) {
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        String query = "SELECT * FROM nwtis_zorgrdjan_bp_1.korisnici where "
                + "kor_ime=" + '"' + kor_ime + '"' + "and lozinka=" + "'" + kor_lozinka + "'";
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
      private String pretvoriKomanduJsonAuthenthicate(String komanda,String lozinka) {
        String komandaBezLozinke = komanda.replace(" LOZINKA " + lozinka + ";", "");
        System.out.println("Komanda broj: " + brojPoruke + " bez lozinke:" + komandaBezLozinke);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.zzz"); //yyyy.MM.dd hh:mm:ss.zzz
        String datum = dateFormat.format(vrijemePreuzimanja);
        KomandaJson novaKomanda = new KomandaJson();
        novaKomanda.setId(brojPoruke);
        novaKomanda.setKomanda(komandaBezLozinke);
        novaKomanda.setVrijeme(datum);
        Gson gson = new Gson();
        String povratnaKomanda = gson.toJson(novaKomanda);
        return povratnaKomanda;
    }
      private void saljiMailAuthenthicate(String komanda,String lozinkaKorisnika) {
        String predmetPoruke = SlusacAplikacije.getSc().getAttribute("predmetporuke").toString();
        String adresaPrimatelja = SlusacAplikacije.getSc().getAttribute("adresaprimatelja").toString();
        String adresaPosiljatelja = SlusacAplikacije.getSc().getAttribute("adresaposiljatenja").toString();
        String posluzitelj = SlusacAplikacije.getSc().getAttribute("mail.server").toString();
        String porukaZaSlanje = pretvoriKomanduJsonAuthenthicate(komanda,lozinkaKorisnika);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            Session session = Session.getInstance(properties, null);
            MimeMessage message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(adresaPosiljatelja);
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(adresaPrimatelja);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(predmetPoruke);
            message.setText(porukaZaSlanje);
            Multipart multipart = new MimeMultipart();
            String attachmentPoruka = porukaZaSlanje;
            MimeBodyPart attachment = new MimeBodyPart();
            DataSource poruka = new ByteArrayDataSource(attachmentPoruka.getBytes("UTF-8"), "application/json");
            attachment.setDataHandler(new DataHandler(poruka));
            attachment.setFileName(SlusacAplikacije.getSc().getAttribute("mail.attachmentFilename").toString());
            multipart.addBodyPart(attachment);
            message.setContent(multipart);
            Transport.send(message);

        } catch (IOException ex) {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
        private void pisiUDnevnikAuthenticate(String komanda,String korisnikSustava) {
        BP_Konfiguracija konfiguracija = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        String url = konfiguracija.getServerDatabase();
        String korisnik = konfiguracija.getAdminDatabase();
        String lozinka = konfiguracija.getAdminPassword();
        Connection con;
        Statement stmt;
        try {
            Class.forName(konfiguracija.getDriverDatabase());
        } catch (java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, korisnik, lozinka);
            stm = con.createStatement();
            String upit1 = " INSERT INTO nwtis_zorgrdjan_bp_1.dnevnik (`korisnik`, `url`,`akcija`,`status`) values("
                    + "'" + korisnikSustava + "',"
                    + "'" + "',"
                    + "'" + komanda + "',"
                    + 2 + ")";
            stm.execute(upit1);
            stm.close();
            con.close();
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }
}
