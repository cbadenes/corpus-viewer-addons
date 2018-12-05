package es.gob.minetad.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SolrUtils.class);


    public static void iterate(String collection, String query, SolrClient client, Executor executor) throws IOException, SolrServerException {

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(500);
        solrQuery.setQuery(query);
        solrQuery.addSort("id", SolrQuery.ORDER.asc);
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse rsp = client.query(collection,solrQuery);
            String nextCursorMark = rsp.getNextCursorMark();
            for (SolrDocument d : rsp.getResults()) {
                executor.handle(d);
            }
            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
    }

    public static void iterateBySimilar(String collection, String filterQuery, String docTopics, Double threshold, Double epsylon, Double multiplier, SolrClient client, Executor executor) throws IOException, SolrServerException {


        /**
         *
         */
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("qq", docTopics);
        solrQuery.set("q","{!frangeext}query($qq)");
        solrQuery.set("fq", filterQuery);
        solrQuery.set("multiplicationFactor", multiplier+"");
        solrQuery.set("threshold", threshold+""); //umbral
        solrQuery.set("modelSize", 70); //tamaño del modelo
        solrQuery.addField("jsWeight:[js],id,listaBM,name_s");
        solrQuery.set("epsylon", epsylon+"");
        solrQuery.setRows(Integer.MAX_VALUE);
        solrQuery.setSort("score", SolrQuery.ORDER.desc);
        /**
         *
         */
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse rsp = client.query(collection,solrQuery);
            String nextCursorMark = rsp.getNextCursorMark();
            for (SolrDocument d : rsp.getResults()) {
                executor.handle(d);
            }
            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
    }


    public interface Executor{

        void handle(SolrDocument d);
    }
}
