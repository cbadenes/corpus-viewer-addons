package es.gob.minetad.doctopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopicIndexFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopicIndexFactory.class);

    public static DocTopicsIndex newFrom(Integer numTopics){
        float epsylon = 1f / numTopics;
        float multiplicationFactor = Double.valueOf(1 * Math.pow(10, String.valueOf(numTopics).length() + 1)).floatValue();
        CleanZeroEpsylonIndex docTopicIndexer = new CleanZeroEpsylonIndex(numTopics, multiplicationFactor, epsylon);
        return docTopicIndexer;
    }

}
