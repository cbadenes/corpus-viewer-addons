package load.wikipedia;

import load.LoadDTMTopicDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LoadWikipediaDTMTopicDocs extends LoadDTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadWikipediaDTMTopicDocs.class);

    private static final String CORPUS  = "wikipedia";
    private static final Integer DIM    = 120;


    public LoadWikipediaDTMTopicDocs() {
        super(CORPUS, DIM);
    }
}
