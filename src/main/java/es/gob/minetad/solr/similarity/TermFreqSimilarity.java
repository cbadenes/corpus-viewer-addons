package es.gob.minetad.solr.similarity;

import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class TermFreqSimilarity extends TFIDFSimilarity {

    public float lengthNorm(int numTerms) {
        //return (float)(1.0D / Math.sqrt((double)numTerms));
        return 1.0F;
    }

    public float tf(float freq) {
        return (float)Math.sqrt((double)freq);
    }

    public float sloppyFreq(int distance) {
        return 1.0F / (float)(distance + 1);
    }

    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1.0F;
    }

    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
        long df = termStats.docFreq();
        long docCount = collectionStats.docCount() == -1L?collectionStats.maxDoc():collectionStats.docCount();
        float idf = this.idf(df, docCount);
        return Explanation.match(idf, "idf, computed as log((docCount+1)/(docFreq+1)) + 1 from:", new Explanation[]{Explanation.match((float)df, "docFreq", new Explanation[0]), Explanation.match((float)docCount, "docCount", new Explanation[0])});
    }

    public float idf(long docFreq, long docCount) {
        //return (float)(Math.log((double)(docCount + 1L) / (double)(docFreq + 1L)) + 1.0D);
        return 1.0F;
    }

    public String toString() {
        return "TermFreqSimilarity";
    }

}
