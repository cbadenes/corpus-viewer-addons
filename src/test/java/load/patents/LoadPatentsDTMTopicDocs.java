package load.patents;

import load.LoadDTMTopicDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LoadPatentsDTMTopicDocs extends LoadDTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadPatentsDTMTopicDocs.class);

    private static final String CORPUS  = "patents";
    private static final Integer DIM    = 15;


    public LoadPatentsDTMTopicDocs() {
        super(CORPUS, DIM);
    }
}
