package es.gob.minetad.lucene;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BM25ByDTFEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BM25ByDTFEval.class);


    @Test
    public void bm25DTFQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BM25Similarity());
        queryByDTF("BM25", searcher);
    }

}
