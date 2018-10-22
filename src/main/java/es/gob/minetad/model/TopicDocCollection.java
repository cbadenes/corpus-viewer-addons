package es.gob.minetad.model;

import es.gob.minetad.doctopic.DTFIndex;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicDocCollection extends SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(TopicDocCollection.class);
    private final AtomicInteger counter;
    private final Integer vocabSize;
    private final float multiplicationFactor;
    private final DTFIndex indexer;

    public TopicDocCollection(String name, Integer numTopics, Integer vocabSize) throws IOException, SolrServerException {
        super(name+"-topicdoc-"+numTopics);
        this.vocabSize              = vocabSize;
        this.counter                = new AtomicInteger();
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(vocabSize).length()+1)).floatValue();
        this.indexer                = new DTFIndex(multiplicationFactor);

    }

    public void add(String id, String name, String description, Double entropy,  Map<String,Double> words) throws IOException, SolrServerException {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);
        document.addField("name",name);
        document.addField("description",description);
        document.addField("entropy",entropy);

        String dtf = indexer.toString(words);
        document.addField("words",dtf);

        solrClient.add(collectionName, document);

        if (counter.incrementAndGet() % 100 == 0) commit();
    }


}
