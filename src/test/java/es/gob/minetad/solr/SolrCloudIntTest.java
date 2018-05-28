package es.gob.minetad.solr;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.params.CollectionParams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrCloudIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(SolrCloudIntTest.class);


    @Test
    public void connect(){


        CloudSolrClient client = new CloudSolrClient.Builder(Arrays.asList(new String[]{"http://localhost:2181"}), Optional.empty())
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();

        client.connect();

        ClusterState clusterState = client.getZkStateReader().getClusterState();
        final DocCollection collection = clusterState.getCollection("topics");

        LOG.info("Collection: " + collection);





    }

}
