package es.gob.minetad.doctopic;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import es.gob.minetad.model.Stats;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicSummary {

    private static final Logger LOG = LoggerFactory.getLogger(TopicSummary.class);

    private List<TopicPoint> fineGrainedGroups;
    private List<TopicPoint> coarseGrainedGroups;

    private final HashFunction hf = Hashing.murmur3_32();


    public TopicSummary(List<Double> topicDistribution) {
        Stats stats = new Stats(topicDistribution);
        DistanceMeasure distanceMeasure = new MonoDimensionalDistanceMeasure();
        //double eps      = stats.getVariance();
        double eps      = stats.getDev();
        int minPts      = 0;

        DBSCANClusterer<TopicPoint> fineGrainedClusterer    = new DBSCANClusterer<>(stats.getVariance(),minPts,distanceMeasure);
        DBSCANClusterer<TopicPoint> coarseGrainedClusterer  = new DBSCANClusterer<>(stats.getDev(),minPts,distanceMeasure);


        List<TopicPoint> points = IntStream.range(0, topicDistribution.size()).mapToObj(i -> new TopicPoint("" + i, topicDistribution.get(i))).collect(Collectors.toList());



        List<Cluster<TopicPoint>> fineGrainedClusterList    = fineGrainedClusterer.cluster(points);
        List<Cluster<TopicPoint>> coarseGrainedClusterList  = coarseGrainedClusterer.cluster(points);

        this.fineGrainedGroups      = createGroups(fineGrainedClusterList, points, topicDistribution);
        this.coarseGrainedGroups    = createGroups(coarseGrainedClusterList, points, topicDistribution);
    }

    private List<TopicPoint> createGroups(List<Cluster<TopicPoint>> list, List<TopicPoint> points, List<Double> topicDistribution){
        List<TopicPoint> groups = new ArrayList<>();
        int totalPoints = 0;

        for(Cluster<TopicPoint> cluster: list){
            Double score    = (cluster.getPoints().stream().map(p -> p.score).reduce((x,y) -> x+y).get()) / (cluster.getPoints().size());
            String label    = cluster.getPoints().stream().map(p -> "t"+p.id).sorted((x,y) -> -x.compareTo(y)).collect(Collectors.joining("_"));

            totalPoints += cluster.getPoints().size();

            groups.add(new TopicPoint(label,score));
        }
        if (totalPoints < topicDistribution.size()){
            List<TopicPoint> clusterPoints = list.stream().flatMap(l -> l.getPoints().stream()).collect(Collectors.toList());
            List<TopicPoint> isolatedTopics = points.stream().filter(p -> !clusterPoints.contains(p)).collect(Collectors.toList());
            Double score = (isolatedTopics.stream().map(p -> p.score).reduce((x,y) -> x+y).get()) / (isolatedTopics.size());
            String label = isolatedTopics.stream().map(p -> "t"+p.id).sorted((x,y) -> -x.compareTo(y)).collect(Collectors.joining("_"));
            groups.add(new TopicPoint(label,score));
        }
        Collections.sort(groups, (a,b) -> -a.score.compareTo(b.score));
        return groups;
    }

    public String getHashTopics(int top) {
        switch (top){
            case 0:return getHashTopicsFrom(fineGrainedGroups, top);
            default: return getHashTopicsFrom(coarseGrainedGroups, top);
        }
    }

    private String getHashTopicsFrom(List<TopicPoint> groups, int top){
        if (groups.size()<=top) return groups.subList(0,1).stream().map(tp -> tp.id).collect(Collectors.joining("|"));
        return groups.subList(0,groups.size()-top).stream().map(tp -> tp.id).collect(Collectors.joining("|"));
    }

    public Integer getHashCode(int top){
        switch (top){
            case 0:return getHashCodeFrom(fineGrainedGroups, top);
            default: return getHashCodeFrom(coarseGrainedGroups, top);
        }
    }

    private Integer getHashCodeFrom(List<TopicPoint> groups, int top){
        if (groups.size()<=top) return hf.hashString(groups.subList(0,1).stream().map(tp -> tp.id).collect(Collectors.joining("|")),Charset.defaultCharset()).asInt();
        return hf.hashString(groups.subList(0,groups.size()-top).stream().map(tp -> tp.id).collect(Collectors.joining("|")),Charset.defaultCharset()).asInt();
    }

    public String getHashExpression(){
        return getHashExpressionFrom(fineGrainedGroups);
    }

    private String getHashExpressionFrom(List<TopicPoint> groups){
        return groups.stream().map(tp -> tp.id).collect(Collectors.joining("\n"));
    }


    private class TopicPoint implements Clusterable{

        private final String id;
        private final Double score;

        public TopicPoint(String id, Double score) {
            this.id = id;
            this.score = score;
        }

        @Override
        public double[] getPoint() {
            return new double[]{score};
        }
    }

    private class MonoDimensionalDistanceMeasure implements DistanceMeasure{

        @Override
        public double compute(double[] p1, double[] p2) {
            return Math.abs(p1[0]-p2[0]);
        }
    }


    public static void main(String[] args) {

        //List<Double> vector = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);

        List<Double> vector = Arrays.asList(0.01833976833976834,
                0.011583011583011584,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.01833976833976834,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.03185328185328185,
                0.011583011583011584,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.011583011583011584,
                0.01833976833976834,
                0.01833976833976834,
                0.004826254826254826,
                0.0250965250965251,
                0.004826254826254826,
                0.004826254826254826,
                0.0250965250965251,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.011583011583011584,
                0.004826254826254826,
                0.01833976833976834,
                0.004826254826254826,
                0.004826254826254826,
                0.011583011583011584,
                0.01833976833976834,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.01833976833976834,
                0.01833976833976834,
                0.011583011583011584,
                0.011583011583011584,
                0.01833976833976834,
                0.01833976833976834,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.011583011583011584,
                0.011583011583011584,
                0.004826254826254826,
                0.011583011583011584,
                0.004826254826254826,
                0.004826254826254826,
                0.01833976833976834,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.3764478764478765,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.011583011583011584,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826,
                0.004826254826254826);

        TopicSummary topicSummary = new TopicSummary(vector);
        LOG.info("Hash Expression: \n" + topicSummary.getHashExpression());
        for(int i=0;i<6;i++){
            LOG.info("Hash Code "+i+": " + topicSummary.getHashCode(i));
            LOG.info("Hash Topics "+i+":" + topicSummary.getHashTopics(i));

        }

    }

}
