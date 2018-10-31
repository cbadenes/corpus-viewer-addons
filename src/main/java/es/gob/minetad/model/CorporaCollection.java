package es.gob.minetad.model;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CorporaCollection extends SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(CorporaCollection.class);

    public CorporaCollection() throws IOException, SolrServerException {
        super("corpora");
    }


    public void add(Corpus corpus) throws IOException, SolrServerException {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",corpus.getName().hashCode());
        document.addField("name",corpus.getName());
        document.addField("entropy",corpus.getEntropy());

        add(document);

    }



}
