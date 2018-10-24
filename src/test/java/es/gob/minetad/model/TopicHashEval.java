package es.gob.minetad.model;

import es.gob.minetad.doctopic.TopicSummary;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicHashEval {

    private static final Logger LOG = LoggerFactory.getLogger(TopicHashEval.class);

    @Test
    public void patstat() throws IOException {

        BufferedReader doctopicsReader = ReaderUtils.from("https://delicias.dia.fi.upm.es/nextcloud/index.php/s/4FJtpLxM9qa7QiA/download");
        String line;
        ParallelExecutor executor = new ParallelExecutor();
        AtomicInteger counter = new AtomicInteger();
        int maxSize = 100000;
        ConcurrentLinkedQueue<Double> sizes = new ConcurrentLinkedQueue<Double>();
        while((line = doctopicsReader.readLine()) != null){
            if (counter.incrementAndGet() % 100 == 0 ) LOG.info(counter.get() + " topics hashed");
            final String row = line;
            executor.submit(() -> {
                String[] result = row.split(",");
                String id = result[0];
                List<Double> shape = new ArrayList<>();
                for (int i=1; i<result.length; i++){
                    shape.add(Double.valueOf(result[i]));
                }
                TopicSummary topicSummary = new TopicSummary(shape);
                sizes.add(Double.valueOf(StringUtils.countMatches(topicSummary.getHashTopicsQ1(),"_")+1));
            });
            if ((maxSize > 0) && (maxSize < counter.get())) break;
        }
        executor.awaitTermination(1, TimeUnit.HOURS);

        LOG.info("Topic Hash Statistics: " + new Stats(new ArrayList<Double>(sizes)));


    }

}
