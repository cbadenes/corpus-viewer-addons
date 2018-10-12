package es.gob.minetad.lucene;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BooleanByHashEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BooleanByHashEval.class);


    @Test
    public void booleanHash1Query() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BooleanSimilarity());
        queryByHash("Boolean1", searcher);
    }

    @Test
    public void booleanHash2Query() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BooleanSimilarity());
        queryByHash("Boolean2", searcher);
    }

}
