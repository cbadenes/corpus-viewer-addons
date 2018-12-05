package es.gob.minetad.doctopic;

import com.google.common.primitives.Doubles;
import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BHTSne;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;
import es.gob.minetad.metric.JensenShannon;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TSNEIndex implements DocTopicsIndex {

    private static final Logger LOG = LoggerFactory.getLogger(TSNEIndex.class);

    private EuclideanDistance distance;

    private final Integer multiplier;

    private Integer numTopics;

    private Map<List<Double>,String> shapesMap;

    public TSNEIndex(Integer numTopics, Integer multiplier, Double perplexity, String corpusPath) throws IOException {

        this.numTopics  = numTopics;
        this.multiplier = multiplier;
        this.distance   = new EuclideanDistance();

        InputStream inputStream = new GZIPInputStream(new FileInputStream(new File(corpusPath)));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        String line;
        List<List<Double>> shapes = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        while ((line = bufferedReader.readLine()) != null) {
            // comment line
            if(line.trim().startsWith("#")){
                continue;
            }
            String[] result = line.split("\\t");
            String id = result[1].replace("\"", "").replace("/","-").toUpperCase().trim();
            List<Double> shape = new ArrayList<>();
            for (int i=2; i<result.length; i++){
                shape.add(Double.valueOf(result[i]));
            }
            shapes.add(shape);
//            if (counter.getAndIncrement() == 1000) break;
        }
        bufferedReader.close();
        LOG.info("corpus initialized");

//        double [][] X = MatrixUtils.simpleRead2DMatrix(new File("src/main/resources/datasets/mnist2500_X.txt"), "   ");
        double[][] X = new double[shapes.size()][shapes.get(0).size()];
        for(int i=0; i<shapes.size(); i++){
            X[i] = Doubles.toArray(shapes.get(i));
        }
        LOG.info("X matrix initialized");

        //System.out.println(MatrixOps.doubleArrayToPrintString(X, ", ", 50,10));
        BarnesHutTSne tsne;
        boolean parallel = false;
        if(parallel) {
            tsne = new ParallelBHTsne();
        } else {
            tsne = new BHTSne();
        }
        TSneConfiguration config = TSneUtils.buildConfig(X, 2, numTopics, perplexity, 1000);
        LOG.info("initializing t-SNE ..");
        double [][] Y = tsne.tsne(config);
        LOG.info("t-SNE completed!");

        double[] xValues = new double[Y.length];
        double[] yValues = new double[Y.length];
        for(int i=0;i<Y.length;i++){
            xValues[i] = Y[i][0];
            yValues[i] = Y[i][1];
        }

        double[] minValues = new double[2];
        minValues[0] = Double.valueOf(StatUtils.min(xValues));
        minValues[1] = Double.valueOf(StatUtils.min(yValues));

        double[] maxValues = new double[2];
        maxValues[0] = Double.valueOf(StatUtils.max(xValues));
        maxValues[1] = Double.valueOf(StatUtils.max(yValues));


        double[][] normalizedY = new double[Y.length][2];
        for(int i=0;i<Y.length;i++){
            double x = Y[i][0];
            normalizedY[i][0] =  ((x - minValues[0]) / (maxValues[0] - minValues[0]))+1;

            double y = Y[i][1];
            normalizedY[i][1] =  ((y - minValues[1]) / (maxValues[1] - minValues[1]))+1;
        }

        Map<List<Double>,String> shapesMap = new HashMap<>();
        for(int i=0;i<X.length;i++){
            List<Double> key = Doubles.asList(X[i]);
            double[] value = normalizedY[i];
            StringBuilder shape = new StringBuilder();
            for(int j=0;j<value.length;j++){
                double x = value[j];
                int score = Double.valueOf(x * multiplier).intValue();
                shape.append("t").append(j).append("|").append(score).append(" ");
            }
            shapesMap.put(key,shape.toString());
        }

    }

    @Override
    public String toString(List<Double> vector) {
        return shapesMap.get(vector);
    }

    @Override
    public List<Double> toVector(String shape) {
        //List<Double> vector = shapesMap.entrySet().parallelStream().filter(entry -> entry.getValue().equalsIgnoreCase(topicRepresentation)).map(e -> e.getKey()).collect(Collectors.toList()).get(0);
        List<Double> vector = new ArrayList<Double>();
        String[] values = shape.split(" ");
        vector.add(Double.valueOf(StringUtils.substringAfter(values[0],"|"))/Double.valueOf(multiplier));
        vector.add(Double.valueOf(StringUtils.substringAfter(values[1],"|"))/Double.valueOf(multiplier));


        return vector;
    }

    @Override
    public String id() {
        return "t-SNE";
    }

    @Override
    public Similarity metric() {
        return new BooleanSimilarity();
    }

    @Override
    public Double getEpsylon() {
        return 0.0;
    }

    @Override
    public Double getPrecision() {
        return Double.valueOf(multiplier);
    }

    @Override
    public Double similarity(List<Double> v1, List<Double> v2) {
        return -distance.compute(Doubles.toArray(v1), Doubles.toArray(v2));
    }

}
