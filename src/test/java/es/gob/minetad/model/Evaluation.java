package es.gob.minetad.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Evaluation {

    private long truePositive    = 0;

    private long falsePositive   = 0;

    private long falseNegative   = 0;


    public synchronized void addResult(List<String> reference, List<String> value){

        truePositive    += value.stream().filter( e -> reference.contains(e)).count();

        falsePositive   += value.stream().filter( e -> !reference.contains(e)).count();

        falseNegative   += reference.stream().filter( e -> !value.contains(e)).count();

    }

    public Double getPrecision(){
        return Double.valueOf(truePositive) / (Double.valueOf(truePositive)+ Double.valueOf(falsePositive));
    }


    public Double getRecall(){
        return Double.valueOf(truePositive) / (Double.valueOf(truePositive)+ Double.valueOf(falseNegative));
    }

    public Double getFMeasure(){
        Double precision = getPrecision();
        Double recall = getRecall();
        return 2 * (precision*recall) / (precision+recall);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "truePositive="     + truePositive +
                ", falsePositive="  + falsePositive +
                ", falseNegative="  + falseNegative +
                ", precision="      + getPrecision()+
                ", recall="         + getRecall() +
                ", fMeasure="       + getFMeasure() +
                '}';
    }

    public static void main(String[] args){
        Evaluation evaluation = new Evaluation();

        List<String> ref = Arrays.asList(new String[]{"a","b","c","d","e"});
        List<String> sample = Arrays.asList(new String[]{"h","b","g","z","s"});

        evaluation.addResult(ref,sample);

        System.out.println(evaluation);



    }
}
