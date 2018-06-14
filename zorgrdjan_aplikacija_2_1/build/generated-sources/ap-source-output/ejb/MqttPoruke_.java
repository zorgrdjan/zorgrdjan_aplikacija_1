package ejb;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2018-06-12T16:53:43")
@StaticMetamodel(MqttPoruke.class)
public class MqttPoruke_ { 

    public static volatile SingularAttribute<MqttPoruke, Date> vrijeme;
    public static volatile SingularAttribute<MqttPoruke, String> parkiralisteId;
    public static volatile SingularAttribute<MqttPoruke, String> vozilo;
    public static volatile SingularAttribute<MqttPoruke, Integer> id;
    public static volatile SingularAttribute<MqttPoruke, String> akcija;

}