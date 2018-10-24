package es.gob.minetad.model;

import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.doctopic.TopicSummary;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocumentCollection extends SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentCollection.class);

    public DocumentCollection(String name) throws IOException, SolrServerException {
        super(name+"-docs");
    }


    public void add(String id, String name, String text) throws IOException, SolrServerException {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);
        document.addField("name",name);
        document.addField("text",text);

        add(document);

    }



}
