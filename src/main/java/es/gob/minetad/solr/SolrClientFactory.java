package es.gob.minetad.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SolrClientFactory.class);


    public static SolrClient create(String url, String mode){
        switch (mode.toLowerCase()){
            case "cloud":
                LOG.info("Created a new connection to Solr Cloud");
                return new CloudSolrClient.Builder(Arrays.asList(url), Optional.empty())
                        .withConnectionTimeout(10000)
                        .withSocketTimeout(6000000)
                        .build();
            default:
                LOG.info("Created a new connection to Solr Standalon");
                String serverUrl = url.startsWith("http") ? url : "http://" + url;
                return new HttpSolrClient.Builder(serverUrl+"/solr").build();
        }
    }
}
