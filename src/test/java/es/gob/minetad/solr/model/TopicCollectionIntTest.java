package es.gob.minetad.solr.model;

import es.gob.minetad.librairy.ModelClient;
import es.gob.minetad.metric.TopicUtils;
import es.gob.minetad.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicCollectionIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCollectionIntTest.class);


    private final String solr_endpoint      = "localhost:2181";
    private final String model_endpoint     = "http://localhost:8080/topics";

    private final String COLLECTION_NAME    = "topics";
    private List<Topic> topics;
    private TopicCollection collection;


    @Before
    public void setup(){
        LOG.info("loading topics from an existing model ..");
        try{
            ModelClient modelClient = new ModelClient(model_endpoint);
            this.topics = modelClient.getTopics();
            LOG.info(topics.size() + " topics read");
        }catch (Exception e){
            LOG.error("Error reading topics from model", e);
            throw e;
        }
    }

    @Test
    public void delete(){
        this.collection = new TopicCollection(solr_endpoint, COLLECTION_NAME, topics.size());
        Assert.assertTrue(collection.destroy());
    }


    @Test
    public void create(){
        this.collection = new TopicCollection(solr_endpoint, COLLECTION_NAME, topics.size());
        Assert.assertTrue(collection.create());

        for (Topic topic : topics){
            collection.add(topic);
        }
    }

    @Test
    public void compareCollections(){
        this.collection = new TopicCollection(solr_endpoint, COLLECTION_NAME, topics.size());
        List<String> ids = topics.stream().map(t -> t.getId()).collect(Collectors.toList());

        for (String id1 : ids){

            int index = ids.indexOf(id1);
            if (index == topics.size() -1) break;

            List<String> tail = ids.subList(index+1, ids.size());

            for(String id2: tail){
                LOG.info("Comparison between topic '"+ id1+ "' and topic '"+ id2 +"'");

                Instant start = Instant.now();
                Optional<Topic> t1 = collection.get(id1);
                Optional<Topic> t2 = collection.get(id2);

                if (t1.isPresent() && t2.isPresent()){
                    Double score = TopicUtils.similarity(t1.get(),t2.get(), false);
                    LOG.info("Score=" + score);

                }
                Instant finish = Instant.now();
                LOG.info("elapsed time: " + Duration.between(start, finish).toMillis() + "msecs");


            }


        }

    }

    @Test
    public void compareAllTopics(){
        compareTopics(topics, false);
    }

    @Test
    public void compareOnlySimilarTopics(){
        compareTopics(topics, true);
    }


    private void compareTopics(List<Topic> topics, Boolean validate){

        Map<String, Topic> topicsMap = topics.stream().collect(Collectors.toMap(t -> t.getId(), t -> t));

        List<Combination<String>.Pair> pairs = new Combination<String>(topics.stream().map(t -> t.getId()).collect(Collectors.toList())).getPairs();


        List<TopicWord> results = pairs.parallelStream().map(pair -> {

            Instant start = Instant.now();
            Double score = TopicUtils.similarity(topicsMap.get(pair.getT1()), topicsMap.get(pair.getT2()), false);
            Instant end = Instant.now();

            Word summary = new Word(pair.getT1() + " <-> " + pair.getT2() + " : " + score);

            return new TopicWord(summary, Double.valueOf(Duration.between(start, end).toMillis()));
        }).collect(Collectors.toList());

        results.forEach(result -> LOG.info("Result: " + result.getWord().getValue()));
        LOG.info("Pairs: " + results.size());

        Stats timeStats = new Stats(results.stream().map(t -> t.getScore()).collect(Collectors.toList()));
        LOG.info("Time Stats: " + timeStats);
    }

}
