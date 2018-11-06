package es.gob.minetad.model;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopic {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopic.class);

    private String id;

    private String hash;

    private String topics;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public static DocTopic from(SolrDocument d1){
        DocTopic docTopic = new DocTopic();
        docTopic.setId((String) d1.getFieldValue("id"));
        docTopic.setHash((String) d1.getFieldValues("hashexpr1").iterator().next());
        docTopic.setTopics((String) d1.getFieldValues("topics").iterator().next());
        return docTopic;
    }


}
