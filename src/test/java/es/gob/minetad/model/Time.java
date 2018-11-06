package es.gob.minetad.model;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Time {

    private static SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ");

    public static String now(){
        return formatter.format(new Date());
    }

    public static String from(Long timestamp){
        return formatter.format(timestamp);
    }

    public static String print(Instant start, Instant end, String id){
        return (id + " Time: " + + ChronoUnit.MINUTES.between(start, end) % 60 + "min "
                + (ChronoUnit.SECONDS.between(start, end) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(start, end) % 1000) + "msecs");
    }
}
