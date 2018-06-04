package es.gob.minetad.solr.model;

import es.gob.minetad.model.RestResource;
import es.gob.minetad.model.Topic;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicCollection extends RestResource {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCollection.class);
    private final String name;
    private final String endpoint;
    private final SolrClient client;


    public TopicCollection(String endpoint, String name) {
        this.endpoint = endpoint;
        this.name = name;
        this.client = new CloudSolrClient.Builder(Arrays.asList(new String[]{endpoint}))
//        this.client = new HttpSolrClient.Builder(endpoint)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();

    }

    public boolean create(){
        if (!Collection.create(endpoint,name)) return false;
        LOG.info("Adding fields..");

        // adding fields
        FieldManager fieldManager = new FieldManager(endpoint,name);

        // field type for words
        String typeDefinition = "{\"tokenizer\":{\"class\":\"solr.WhitespaceTokenizerFactory\"}, \"filters\":[{ \"class\":\"solr.DelimitedTermFrequencyTokenFilterFactory\"}] }";

        String topicWordsType = "topicWordsField";
        fieldManager.addType(topicWordsType,"solr.TextField","100",typeDefinition);
        fieldManager.add("name","string",true);

        if (!fieldManager.add("description","text_general",true)
            || !fieldManager.add("words",topicWordsType,true)){
            destroy();
            fieldManager.removeType(topicWordsType);
            return false;
        }
        LOG.info("Fields added");
        return true;
    }

    public boolean destroy(){
        return Collection.remove(endpoint,name);
    }


    public boolean add(Topic topic, Integer normalizer){

        try{
            final SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", topic.getId());
            doc.addField("name", topic.getName());
            doc.addField("description", topic.getDescription());
            String topicWords = topic.getWords().stream().map(tw -> tw.getWord().getValue() + "|" + Double.valueOf(normalizer*tw.getScore()).intValue()).collect(Collectors.joining(" "));
            doc.addField("words", topicWords);

            UpdateResponse updateResponse = client.add(name, doc);
            // Indexed documents must be committed
            client.commit(name);
            LOG.info("Added " + topic + " to collection: " + name);
            return true;
        }catch (Exception e){
            LOG.error("Error adding topic " + topic+ " to collection: " + name,e);
            return false;
        }
    }
}
