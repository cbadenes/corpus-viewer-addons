package es.gob.minetad.solr.model;

import es.gob.minetad.metric.TopicUtils;
import es.gob.minetad.model.RestResource;
import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicCollection extends RestResource {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCollection.class);
    private final String name;
    private final String endpoint;
    private final CloudSolrClient client;
    private final Integer freqRatio;
    private final Integer numTopics;


    public TopicCollection(String endpoint, String name, Integer numTopics) {
        this.endpoint = endpoint;
        this.name = name;
        this.numTopics = numTopics;
        this.freqRatio = TopicUtils.multiplier(numTopics);
//        this.client = new CloudSolrClient.Builder(Arrays.asList(new String[]{endpoint}))
////        this.client = new HttpSolrClient.Builder(endpoint)
//                .withConnectionTimeout(10000)
//                .withSocketTimeout(60000)
//                .build();


        this.client = new CloudSolrClient.Builder(Arrays.asList(new String[]{endpoint}), Optional.empty())
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();

        client.connect();

    }


    public boolean create(){

        if (!Collection.create(client,name)) return false;
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


    public boolean add(Topic topic){

        try{
            final SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", topic.getId());
            doc.addField("name", topic.getName());
            doc.addField("description", topic.getDescription());
            String topicWords = topic.getWords().stream().map(tw -> tw.getWord().getValue() + "|" + normalizedScore(tw.getScore())).collect(Collectors.joining(" "));
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

    private Integer normalizedScore (Double score){
        Integer normScore = Double.valueOf(freqRatio*score).intValue();
        return (normScore < 1)? 1 : normScore;
    }

    private Double denormalizedScore(Integer freq){
        return Double.valueOf(freq)/Double.valueOf(freqRatio);
    }

    public Optional<Topic> get(String id){
        try{
            SolrDocument topicDoc = client.getById(name, id);
            Topic topic = new Topic();
            topic.setId(id);
            topic.setName((String)topicDoc.getFieldValue("name"));
            topic.setDescription((String)topicDoc.getFieldValue("description"));

            String words = (String) topicDoc.getFieldValue("words");
            List<TopicWord> topicWords = Arrays.stream(words.split(" ")).map(exp -> new TopicWord(new Word(StringUtils.substringBefore(exp, "|")), denormalizedScore(Integer.valueOf(StringUtils.substringAfter(exp, "|"))))).collect(Collectors.toList());
            topic.setWords(topicWords);
            return Optional.of(topic);

        }catch (Exception e){
            LOG.error("Error getting topic by id '" + id + "' in collection '" + name+"'");
            return Optional.empty();
        }

    }
}
