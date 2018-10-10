package es.gob.minetad.lucene;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
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

public class BooleanByDTFEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BooleanByDTFEval.class);


    @Test
    public void booleanDTFQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BooleanSimilarity());
        queryByDTF("Boolean", searcher);
    }

}
