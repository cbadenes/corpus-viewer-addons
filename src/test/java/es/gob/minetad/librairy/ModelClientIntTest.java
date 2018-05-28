package es.gob.minetad.librairy;

import es.gob.minetad.model.Topic;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ModelClientIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(ModelClientIntTest.class);

    @Test
    public void topics(){
        ModelClient modelClient = new ModelClient("http://localhost:8080/topics");

        List<Topic> topics = modelClient.getTopics();
        for(Topic topic: topics){
            LOG.info(""+topic);
        }


        topics.get(0).getWords().stream().limit(50).forEach(w -> LOG.info("Word: " + w));

    }

}
