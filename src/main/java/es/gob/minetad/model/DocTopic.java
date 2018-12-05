package es.gob.minetad.model;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopic {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopic.class);

    private String id;

    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static DocTopic from(SolrDocument d1){
        DocTopic docTopic = new DocTopic();
        docTopic.setId((String) d1.getFieldValue("id"));
        if (d1.containsKey("hashcode1_i")) docTopic.setHash(String.valueOf(d1.getFieldValues("hashcode1_i").iterator().next()));
        if (d1.containsKey("listaBO")) docTopic.setTopics((String) d1.getFieldValues("listaBO").iterator().next());
        if (d1.containsKey("listaBM")) docTopic.setTopics((String) d1.getFieldValues("listaBM").iterator().next());
        if (d1.containsKey("name_s")) docTopic.setName((String) d1.getFieldValues("name_s").iterator().next());
        return docTopic;
    }


}
