package load.patents;

import load.LoadCTMTopicDocs;
import load.LoadDTMTopicDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LoadPatentsCTMTopicDocs extends LoadCTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadPatentsCTMTopicDocs.class);

    private static final String CORPUS  = "patents";
    private static final Integer DIM    = 20;


    public LoadPatentsCTMTopicDocs() {
        super(CORPUS, DIM);
    }
}
