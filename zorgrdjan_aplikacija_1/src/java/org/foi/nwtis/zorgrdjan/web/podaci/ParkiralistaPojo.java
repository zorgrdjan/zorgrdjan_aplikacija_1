
package org.foi.nwtis.zorgrdjan.web.podaci;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Klasa sluzi iskljucivo samo za kreiranje json-zapisa
 * @author Zoran
 */
public class ParkiralistaPojo {

    @SerializedName("Id")
    @Expose
    private Integer id;
    @SerializedName("Naziv")
    @Expose
    private String naziv;
    @SerializedName("Adresa")
    @Expose
    private String adresa;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("Longitude")
    @Expose
    private String longitude;

    public ParkiralistaPojo(Integer id, String naziv, String adresa, String latitude, String longitude) {
        this.id = id;
        this.naziv = naziv;
        this.adresa = adresa;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
