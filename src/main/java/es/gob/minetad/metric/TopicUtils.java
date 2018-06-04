package es.gob.minetad.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TopicUtils.class);


    public static Integer multiplier(Integer numTopics){

        int length = String.valueOf(numTopics).length();

        double value = Math.pow(10.0, Double.valueOf(length+1));

        return Double.valueOf(value).intValue();
    }

}
