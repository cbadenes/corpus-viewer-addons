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

public class TopicDocsCollection extends SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(TopicDocsCollection.class);
    private final float multiplicationFactor;
    private final DTFIndex indexer;

    public TopicDocsCollection(String name, Integer vocabSize) throws IOException, SolrServerException {
        super(name);
        super.interval              = 10;
        this.counter                = new AtomicInteger();
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(vocabSize).length()+1)).floatValue();
        this.indexer                = new DTFIndex(multiplicationFactor);

    }

    public void add(String id, String name, String description, Double entropy,  Map<String,Double> words) throws IOException, SolrServerException {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);
        document.addField("name_s",name);
        document.addField("description_txt",description);
        document.addField("entropy_f",entropy);

        String dtf = indexer.toString(words);
        document.addField("words_tfdl",dtf);

        add(document);
    }


}
