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

public class JensenShannonTest {

    private static final Logger LOG = LoggerFactory.getLogger(JensenShannonTest.class);

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

    @Test
    public void test4(){
        // doctopics
        List<Double> docA = Arrays.asList(0.9964499484004059, 9.494324045407538E-4, 7.843137254901879E-4, 9.494324045407534E-4, 8.66873065015471E-4);
        LOG.info("sum1 -> " + docA.stream().reduce((a,b) -> a+b).get());

        // inference
        List<Double> docB = Arrays.asList(0.9968627450980392, 7.843137254901962E-4, 7.843137254901962E-4, 7.843137254901962E-4, 7.843137254901962E-4);
        LOG.info("sum2 -> " + docB.stream().reduce((a,b) -> a+b).get());

        LOG.info("Sim=" + JensenShannon.similarity(docA,docB));
    }


    private void test(List<Double> docA,List<Double> docB,List<Double> docC){
        LOG.info("sim(A,B)=" + JensenShannon.similarity(docA,docB));
        LOG.info("sim(A,C)=" + JensenShannon.similarity(docA,docC));
        LOG.info("sim(B,C)=" + JensenShannon.similarity(docB,docC));
    }

}
