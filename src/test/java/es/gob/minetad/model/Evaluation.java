package es.gob.minetad.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Evaluation {

    private int num        = 0;

    private long truePositive    = 0;

    private long falsePositive   = 0;

    private long falseNegative   = 0;

    private Instant start;

    private Instant end;


    public Evaluation() {
    }

    public Evaluation(Instant start, Instant end, List<String> reference, List<String> values, int num) {
        this.start = start;
        this.end = end;
        this.num = num;
        addResult(reference, values);
    }


    public synchronized void addResult(List<String> reference, List<String> value, int num){
        this.num += num;
        addResult(reference, value);
    }

    public synchronized void addResult(List<String> reference, List<String> value){

        truePositive    += value.stream().filter( e -> reference.contains(e)).count();

        falsePositive   += value.stream().filter( e -> !reference.contains(e)).count();

        falseNegative   += reference.stream().filter( e -> !value.contains(e)).count();

    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public Double getPrecision(){

        double positive = (Double.valueOf(truePositive) + Double.valueOf(falsePositive));

        if (positive == 0.0) return 0.0;

        return Double.valueOf(truePositive) / positive;
    }


    public Double getRecall(){

        double positive = (Double.valueOf(truePositive)+ Double.valueOf(falseNegative));

        if (positive == 0.0) return 0.0;

        return Double.valueOf(truePositive) / positive;
    }

    public Double getFMeasure(){
        Double precision = getPrecision();
        Double recall = getRecall();
        if ((precision == 0) && (recall == 0)) return 0.0;
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
                ", num="            + num +
                ", elapsedTime="       + ((start!= null && end!=null)?  Time.print(start,end,"") : "")+
                '}';
    }
}
