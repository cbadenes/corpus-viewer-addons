package es.gob.minetad.lucene;

import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.TimeUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class GoldStandardEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(GoldStandardEval.class);

    @Test
    public void test1() throws IOException, ParseException {
        goldStandardQuery("gold-standard1");
    }

    @Test
    public void test2() throws IOException, ParseException {
        goldStandardQuery("gold-standard2");
    }


    public void goldStandardQuery(String label) throws IOException, ParseException {

        List<Double> v1 = SAMPLE_VECTOR;
//        Map<String, Double> m1 = string2map(vector2String(v1));
        String description = "Searching by " + label;

        Map<String,Double> scoreDocs = new ConcurrentHashMap<>();

        Instant s2 = Instant.now();
        ParallelExecutor executor = new ParallelExecutor();
        BufferedReader doctopicsReader = ReaderUtils.from(DOCTOPICS_PATH);
        String line;
        AtomicInteger counter = new AtomicInteger();
        while ((line = doctopicsReader.readLine()) != null) {
            final Integer index = counter.incrementAndGet();
            final String row = line;
            executor.submit(() -> {
                try{
                    String[] result = row.split(",");
                    String id = result[0];
                    List<Double> v2 = new ArrayList<>();
                    for (int i=1; i<result.length; i++){
                        v2.add(Double.valueOf(result[i]));
                    }

                    double score = JensenShannon.similarity(v1, v2);
                    if (score > 0.98){
                        scoreDocs.put(id,score);
                    }
                }catch (Exception e){
                    LOG.error("Unexpected error", e);
                }
            });
        }
        executor.awaitTermination(1, TimeUnit.HOURS);
        Instant e2 = Instant.now();
        TimeUtils.print(s2,e2,"JSD");

        Instant s3 = Instant.now();
        List<Score> topDocs = scoreDocs.entrySet().parallelStream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).map(entry -> new Score(entry.getValue(), new Document(), new Document(entry.getKey()))).collect(Collectors.toList());
        Instant e3 = Instant.now();
        TimeUtils.print(s3,e3,"Sort");


        TimeUtils.print(s2,e3,"Total");

        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));

    }

}
