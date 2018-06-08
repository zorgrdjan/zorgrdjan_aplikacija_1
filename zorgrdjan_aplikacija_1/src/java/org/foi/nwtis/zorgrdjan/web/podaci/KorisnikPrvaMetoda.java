package org.foi.nwtis.zorgrdjan.web.podaci;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KorisnikPrvaMetoda {

@SerializedName("korisnickoIme")
@Expose
private String korisnickoIme;
@SerializedName("lozinka")
@Expose
private String lozinka;

public String getKorisnickoIme() {
return korisnickoIme;
}

public void setKorisnickoIme(String korisnickoIme) {
this.korisnickoIme = korisnickoIme;
}

public String getLozinka() {
return lozinka;
}

public void setLozinka(String lozinka) {
this.lozinka = lozinka;
}

}