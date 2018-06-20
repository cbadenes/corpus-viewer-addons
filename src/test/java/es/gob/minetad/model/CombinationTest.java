package es.gob.minetad.model;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CombinationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CombinationTest.class);


    @Test
    public void recursive(){

        List<String> letters = Arrays.asList( new String[]{"A","B","C","D","E"});
        List<Combination<String>.Pair> pairs = new Combination<>(letters).getPairs();

        pairs.forEach( pair -> LOG.info("" + pair));

    }
}
