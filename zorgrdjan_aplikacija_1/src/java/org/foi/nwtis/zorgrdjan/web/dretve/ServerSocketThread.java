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
import org.foi.nwtis.zorgrdjan.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.zorgrdjan.ws.klijenti.GrupaWsKlijent;

/**
 *
 * @author Zoran
 */
public class ServerSocketThread extends Thread {

    public static boolean runServerSocket = true;
    public ServerSocket serverSocket;
    private Socket socket;

    private static boolean runThread = true;
    public static boolean serverPause = false;
    public static boolean weatherDataPicker = false;
    public static boolean postupakPrekida = false;

    private ServletContext servletContext;
    private int brojPoruke = 1;

    public ServerSocketThread(ServletContext sc) {
        this.servletContext = sc;
    }

    @Override
    public void interrupt() {
        runServerSocket = false;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        System.out.println(System.currentTimeMillis());
        System.out.println("Pokrecem dretvu koja prima podatke na server socket!");
        int port = Integer.parseInt(SlusacAplikacije.getSc().getAttribute("port").toString());

        // String komanda = "KORISNIK ivicelig; LOZINKA 123456; GRUPA STANJE;";
        try {
            serverSocket = new ServerSocket(port);
            while (runServerSocket) {
                socket = serverSocket.accept();
                Date vrijeme = new Date();
                System.out.println("Korisnik se spojio!");
                RadnaDretva radnaDretva = new RadnaDretva(socket, vrijeme, brojPoruke);
                radnaDretva.start();
                brojPoruke++;
            }
        } catch (IOException ex) {
            System.out.println("Uhvatili smo exception!" + ex);
        }

    }

    public synchronized int getBrojPoruke() {
        return brojPoruke;
    }

    public synchronized void setBrojPoruke(int brojPoruke) {
        this.brojPoruke = brojPoruke;
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

}
