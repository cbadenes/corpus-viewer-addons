package es.gob.minetad.doctopic;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CRDCIndexTest {

    private static final Logger LOG = LoggerFactory.getLogger(CRDCIndexTest.class);

    @Test
    public void checkString(){

        List<Double> vector = Arrays.asList(new Double[]{0.3,0.4,0.05,0.15});

        String expected        = "t1_0|40 t0_1|30 t3_2|15";

        CRDCIndex index     = new CRDCIndex(vector.size(),0.75,100);
        String actual       = index.toString(vector);

        Assert.assertEquals(expected, actual);

    }

    @Test
    public void checkVector(){

        List<Double> expected = Arrays.asList(new Double[]{0.3,0.4,0.0,0.15});

        String shape        = "t1_0|40 t0_1|30 t3_2|15";

        CRDCIndex index     = new CRDCIndex(expected.size(),0.75,100);
        List<Double> actual = index.toVector(shape);

        Assert.assertEquals(expected, actual);

    }

}
