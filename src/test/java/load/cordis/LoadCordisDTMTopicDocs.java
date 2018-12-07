package load.cordis;

import load.LoadDTMTopicDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LoadCordisDTMTopicDocs extends LoadDTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadCordisDTMTopicDocs.class);

    private static final String CORPUS  = "cordis";
    private static final Integer DIM    = 150;


    public LoadCordisDTMTopicDocs() {
        super(CORPUS, DIM);
    }
}
