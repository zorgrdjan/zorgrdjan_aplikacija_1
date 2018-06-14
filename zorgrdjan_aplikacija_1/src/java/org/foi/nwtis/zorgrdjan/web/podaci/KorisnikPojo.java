/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.podaci;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KorisnikPojo {

@SerializedName("kid")
@Expose
private Integer kid;
@SerializedName("prezime")
@Expose
private String prezime;
@SerializedName("ime")
@Expose
private String ime;
@SerializedName("korisnicko_ime")
@Expose
private String korisnickoIme;

public Integer getKid() {
return kid;
}

public void setKid(Integer kid) {
this.kid = kid;
}

public String getPrezime() {
return prezime;
}

public void setPrezime(String prezime) {
this.prezime = prezime;
}

public String getIme() {
return ime;
}

public void setIme(String ime) {
this.ime = ime;
}

public String getKorisnickoIme() {
return korisnickoIme;
}

public void setKorisnickoIme(String korisnickoIme) {
this.korisnickoIme = korisnickoIme;
}

}
