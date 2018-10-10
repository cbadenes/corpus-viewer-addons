package es.gob.minetad.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TimeUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUtils.class);


    public static void print(Instant start, Instant end, String id){
        LOG.info(id + " Time: " + + ChronoUnit.MINUTES.between(start, end) % 60 + "min "
                + (ChronoUnit.SECONDS.between(start, end) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(start, end) % 1000) + "msecs");
    }

}
