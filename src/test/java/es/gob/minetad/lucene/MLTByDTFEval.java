package es.gob.minetad.lucene;

import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.model.TopicIndexFactory;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class MLTByDTFEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(MLTByDTFEval.class);


    @Test
    public void moreLikeThisbyDTFQuery() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);

        // Topic DTF
        mlt.setAnalyzer(new DocTopicAnalyzer());
        Reader stringReader = new StringReader(vector2String(SAMPLE_VECTOR));
        Query mltQuery = mlt.like(TopicIndexFactory.FIELD_NAME, stringReader);
        search("MoreLikeThis-byDTF", searcher, mltQuery);
    }
}
