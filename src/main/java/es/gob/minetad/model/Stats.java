package es.gob.minetad.model;

import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Stats {

    private final Double total;

    private Double popVariance;

    private Double min;

    private Double max;

    private Double dev;

    private Double mode;

    private Double mean;

    private Double median;

    private Double variance;


    public Stats(List<Double> values) {
        double[] valuesArray = Doubles.toArray(values);
        StandardDeviation stdDev = new StandardDeviation();
        min = Double.valueOf(StatUtils.min(valuesArray));
        max = Double.valueOf(StatUtils.max(valuesArray));
        dev = stdDev.evaluate(valuesArray);
        mode = StatUtils.mode(valuesArray)[0];
        mean = StatUtils.mean(valuesArray);
        median = StatUtils.geometricMean(valuesArray);
        variance = StatUtils.variance(valuesArray);
        popVariance = StatUtils.populationVariance(valuesArray);
        total = StatUtils.sum(valuesArray);
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public Double getDev() {
        return dev;
    }

    public Double getMode() {
        return mode;
    }

    public Double getMean() {
        return mean;
    }

    public Double getMedian() {
        return median;
    }

    public Double getVariance() {
        return variance;
    }

    public Double getPopVariance() {
        return popVariance;
    }

    public Double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "total=" + total +
                ", popVariance=" + popVariance +
                ", min=" + min +
                ", max=" + max +
                ", dev=" + dev +
                ", mode=" + mode +
                ", mean=" + mean +
                ", median=" + median +
                ", variance=" + variance +
                '}';
    }
}
