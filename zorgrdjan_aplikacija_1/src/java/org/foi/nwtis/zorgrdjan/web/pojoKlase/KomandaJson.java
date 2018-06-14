package org.foi.nwtis.zorgrdjan.web.pojoKlase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class KomandaJson {

@SerializedName("id")
@Expose
private Integer id;
@SerializedName("komanda")
@Expose
private String komanda;
@SerializedName("vrijeme")
@Expose
private String vrijeme;

public Integer getId() {
return id;
}

public void setId(Integer id) {
this.id = id;
}

public String getKomanda() {
return komanda;
}

public void setKomanda(String komanda) {
this.komanda = komanda;
}

public String getVrijeme() {
return vrijeme;
}

public void setVrijeme(String vrijeme) {
this.vrijeme = vrijeme;
}

}