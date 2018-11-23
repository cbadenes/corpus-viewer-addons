package es.gob.minetad.metric;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JensenShannonExt {


    static double thr      = 0.005; //0.005
    static double thr_v    = Math.sqrt(8*thr);


    public static double similarity(List<Double> v1, List<Double> v2){

        double l1 = 0.0;
        for(int i=0;i<v1.size();i++){
            l1 += Math.abs(v1.get(i)-v2.get(i));
        }

        if (l1 >= thr_v) return 0.0;

        return JensenShannon.similarity(v1,v2);
    }

}
