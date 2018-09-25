package es.gob.minetad.metric;

import cc.mallet.util.Maths;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JensenShannon {

    private static final Logger LOG = LoggerFactory.getLogger(JensenShannon.class);


    public static double similarity(List<Double> v1, List<Double> v2){
        double score = similarity(Doubles.toArray(v1), Doubles.toArray(v2));
        if (score < 0 ){
            LOG.warn("Invalid JSD score between: [1] -> " + v1 + " and [2] -> " + v2);
        }
        return score;
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

}
