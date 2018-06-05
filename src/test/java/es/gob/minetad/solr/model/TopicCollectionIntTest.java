package es.gob.minetad.solr.model;

import es.gob.minetad.librairy.ModelClient;
import es.gob.minetad.metric.TopicUtils;
import es.gob.minetad.model.Topic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicCollectionIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCollectionIntTest.class);


    private final String solr_endpoint      = "http://localhost:8081/solr";
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
            this.collection = new TopicCollection(solr_endpoint, COLLECTION_NAME, topics.size());
            LOG.info(topics.size() + " topics read");
        }catch (Exception e){
            LOG.error("Error reading topics from model", e);
            throw e;
        }
    }

    @Test
    public void delete(){
        Assert.assertTrue(collection.destroy());
    }


    @Test
    public void create(){
        Assert.assertTrue(collection.create());

        for (Topic topic : topics){
            collection.add(topic);
        }
    }

    @Test
    public void compare(){

        List<String> ids = topics.stream().map(t -> t.getId()).collect(Collectors.toList());

        for (String id1 : ids){

            List<String> tail = ids.subList(ids.indexOf(id1), ids.size());

            for(String id2: tail){
                LOG.info("Comparison between topic '"+ id1+ "' and topic '"+ id2 +"'");

                Instant start = Instant.now();
                Optional<Topic> t1 = collection.get(id1);
                Optional<Topic> t2 = collection.get(id2);

                if (t1.isPresent() && t2.isPresent()){

                    Double score = TopicUtils.similarity(t1.get(), t2.get());
                    LOG.info("Score=" + score);

                }
                Instant finish = Instant.now();
                LOG.info("elapsed time: " + Duration.between(start, finish).toMillis() + "msecs");


            }


        }

    }

}
