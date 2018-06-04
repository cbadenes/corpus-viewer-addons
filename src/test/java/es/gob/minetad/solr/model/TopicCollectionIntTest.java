package es.gob.minetad.solr.model;

import es.gob.minetad.librairy.ModelClient;
import es.gob.minetad.metric.TopicUtils;
import es.gob.minetad.model.Topic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicCollectionIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCollectionIntTest.class);


    private final String solr_endpoint  = "http://localhost:8081/solr";
    private final String model_endpoint = "http://localhost:8080/topics";

    private TopicCollection collection;

    @Before
    public void setup(){
        this.collection  = new TopicCollection(solr_endpoint, "topics");
    }


    @Test
    public void create(){
        Assert.assertTrue(collection.create());
    }

    @Test
    public void delete(){
        Assert.assertTrue(collection.destroy());
    }


    @Test
    public void index(){

        ModelClient modelClient = new ModelClient(model_endpoint);

        List<Topic> topics = modelClient.getTopics();

        for (Topic topic : topics){
            collection.add(topic, TopicUtils.multiplier(topics.size()));
        }

    }

}
