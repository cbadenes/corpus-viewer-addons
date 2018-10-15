package es.gob.minetad.metric;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SmartJensenShannonTest {

    private static final Logger LOG = LoggerFactory.getLogger(SmartJensenShannonTest.class);

    @Test
    public void test1(){

        List<Double> docA = Arrays.asList(0.6,0.1,0.1,0.1,0.1);
        List<Double> docB = Arrays.asList(0.1,0.1,0.1,0.1,0.6);
        List<Double> docC = Arrays.asList(0.2,0.2,0.2,0.2,0.2);

        test(docA,docB,docC);

    }

    @Test
    public void test2(){

        List<Double> docA = Arrays.asList(0.3,0.3,0.1,0.1,0.1);
        List<Double> docB = Arrays.asList(0.1,0.1,0.1,0.3,0.3);
        List<Double> docC = Arrays.asList(0.2,0.2,0.2,0.2,0.2);

        test(docA,docB,docC);


    }

    @Test
    public void test3(){

        int top = 2;
        int dim = 100;
        Double maxScore = 1.0/(dim-top);
        List<Double> incTopics = IntStream.range(0, top).mapToDouble(i -> maxScore).boxed().collect(Collectors.toList());
        List<Double> excTopics = IntStream.range(0, dim - top).mapToDouble(i -> (1.0 - (maxScore * top)) / (dim - top)).boxed().collect(Collectors.toList());

        List<Double> docA = new ArrayList<>(incTopics);
        docA.addAll(excTopics);

        List<Double> docB = new ArrayList<>(excTopics);
        docB.addAll(incTopics);

        List<Double> docC = IntStream.range(0,dim).mapToDouble(i -> 1.0/dim).boxed().collect(Collectors.toList());

        test(docA,docB,docC);


    }

    private void test(List<Double> docA,List<Double> docB,List<Double> docC){
        LOG.info("sim(A,B)=" + JensenShannon.smartSimilarity(docA,docB));
        LOG.info("sim(A,C)=" + JensenShannon.smartSimilarity(docA,docC));
        LOG.info("sim(B,C)=" + JensenShannon.smartSimilarity(docB,docC));
    }

}
