package es.gob.minetad.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class KL {

    private static final Logger LOG = LoggerFactory.getLogger(KL.class);

    public static final double log2 = Math.log(2.0D);

    public static double divergence(Map<String,Double> p1, double[] p2) {

        double klDiv = 0.0D;

        int index = 0;
        for(String key: p1.keySet()){
            if(p1.get(key) != 0.0D) {
                if(p2[index] == 0.0D) {
                    return 1.0D / 0.0;
                }

                klDiv += p1.get(key) * Math.log(p1.get(key) / p2[index++]);
            }
        }

        return klDiv / log2;
    }

}
