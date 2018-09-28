package es.gob.minetad.metric;

import cc.mallet.util.Maths;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JensenShannon {

    private static final Logger LOG = LoggerFactory.getLogger(JensenShannon.class);

    public static final double log2 = Math.log(2.0D);

    public static double similarity(List<Double> v1, List<Double> v2){
        return similarity(Doubles.toArray(v1), Doubles.toArray(v2));
    }

    public static double similarity(Map<String,Double> v1, Map<String,Double> v2){
        return 1 - divergence(v1,v2);
    }

    public static double similarity(double[] v1, double[] v2){
        return 1 - Maths.jensenShannonDivergence(v1,v2);
    }

    public static double divergence(List<Double> v1, List<Double> v2){
        return divergence(Doubles.toArray(v1),Doubles.toArray(v2));
    }

    public static double divergence(double[] v1, double[] v2){
        return Maths.jensenShannonDivergence(v1,v2);
    }

    public static double divergence(Map<String,Double> v1, Map<String,Double> v2){
        double[] average = new double[v1.size()];

        Map<String,Double> p2 = new HashMap<>();
        int index = 0;
        for(String key: v1.keySet()){
            if (!v2.containsKey(key)) continue;
            average[index++] += (v1.get(key) + v2.get(key)) / 2.0D;
            p2.put(key,v2.get(key));
        }

        return (klDivergence(v1, average) + klDivergence(p2, average)) / 2.0D;
    }

    public static double klDivergence(Map<String,Double> p1, double[] p2) {

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
