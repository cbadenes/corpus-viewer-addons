package es.gob.minetad.lucene;

import es.gob.minetad.doctopic.TopicSummary;
import es.gob.minetad.solr.model.TopicIndexFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class MLTByHashEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(MLTByHashEval.class);


    @Test
    public void moreLikeThisbyHash1Query() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);

        // Topic Hash
        mlt.setAnalyzer(new StandardAnalyzer());
        TopicSummary topicSummary = new TopicSummary(SAMPLE_VECTOR);
        Reader stringReaderPositive = new StringReader(topicSummary.byInclusion());
        Query mltQueryPositive = mlt.like(TopicIndexFactory.DOC_POSITIVE_HASH, stringReaderPositive);
        search("MoreLikeThis-byHash1", searcher, mltQueryPositive);
    }

    @Test
    public void moreLikeThisbyHash2Query() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);

        // Topic Hash
        mlt.setAnalyzer(new StandardAnalyzer());
        TopicSummary topicSummary = new TopicSummary(SAMPLE_VECTOR);
        Reader stringReaderPositive = new StringReader(topicSummary.byInclusion());
        Query mltQueryPositive = mlt.like(TopicIndexFactory.DOC_POSITIVE_HASH, stringReaderPositive);
        search("MoreLikeThis-byHash2", searcher, mltQueryPositive);
    }

}
