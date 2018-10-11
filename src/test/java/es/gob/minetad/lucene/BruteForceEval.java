package es.gob.minetad.lucene;

import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.model.TopicIndexFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BruteForceEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BruteForceEval.class);


    @Test
    public void bruteForceQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        search("BruteForce-positive", searcher, new MatchAllDocsQuery());

    }

}
