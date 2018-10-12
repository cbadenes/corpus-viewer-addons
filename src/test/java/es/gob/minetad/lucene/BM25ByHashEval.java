package es.gob.minetad.lucene;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BM25ByHashEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BM25ByHashEval.class);


    @Test
    public void bm25Hash1Query() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BM25Similarity());
        queryByHash("BM251", searcher);
    }

    @Test
    public void bm25Hash2Query() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BM25Similarity());
        queryByHash("BM252", searcher);
    }

}
