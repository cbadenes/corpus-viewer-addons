package es.gob.minetad.metric;

import cc.mallet.util.Maths;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JensenShannon {

    private static final Logger LOG = LoggerFactory.getLogger(JensenShannon.class);

    public static final double log2 = Math.log(2.0D);

    public static double smartSimilarity(List<Double> v1, List<Double> v2){

        Double threshold = 1.0/v1.size();

        Map<Integer, List<Duple>> m1 = IntStream.range(0, v1.size()).mapToObj(i -> new Duple(i, v1.get(i))).filter(d -> d.getScore() >= threshold).collect(Collectors.groupingBy(Duple::getId));
        Map<Integer, List<Duple>> m2 = IntStream.range(0, v2.size()).mapToObj(i -> new Duple(i, v2.get(i))).filter(d -> d.getScore() >= threshold).collect(Collectors.groupingBy(Duple::getId));

        List<Integer> aux = new ArrayList(m1.keySet());
        aux.addAll(m2.keySet());
        List<Integer> indexes = aux.stream().sorted((a, b) -> a.compareTo(b)).distinct().collect(Collectors.toList());

        List<Double> nv1 = new ArrayList<>();
        List<Double> nv2 = new ArrayList<>();
        for(Integer index: indexes){
            if (m1.containsKey(index)) nv1.add(m1.get(index).get(0).getScore());
            else nv1.add(0.0);

            if (m2.containsKey(index)) nv2.add(m2.get(index).get(0).getScore());
            else nv2.add(0.0);
        }

        List<Double> sv1 = nv1.stream().map(v -> (v/(1.0/Double.valueOf(v1.size())))*(1.0/Double.valueOf(nv1.size())) ).collect(Collectors.toList());
        List<Double> sv2 = nv2.stream().map(v -> (v/(1.0/Double.valueOf(v2.size())))*(1.0/Double.valueOf(nv2.size())) ).collect(Collectors.toList());

        double acc1 = (1.0 - sv1.stream().reduce((a, b) -> a + b).get())/sv1.stream().filter((v -> v!=0.0)).count();
        double acc2 = (1.0 - sv2.stream().reduce((a, b) -> a + b).get())/sv1.stream().filter((v -> v!=0.0)).count();

        List<Double> fv1 = sv1.stream().map(s -> (s!=0.0)? s + acc1 : s).collect(Collectors.toList());
        List<Double> fv2 = sv2.stream().map(s -> (s!=0.0)? s + acc2 : s).collect(Collectors.toList());


        return similarity(Doubles.toArray(fv1), Doubles.toArray(fv2));
    }

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

    public static class Duple{
        private final Integer id;
        private final Double score;

        public Duple(Integer id, Double score) {
            this.id = id;
            this.score = score;
        }

        public Integer getId() {
            return id;
        }

        public Double getScore() {
            return score;
        }
    }

}
