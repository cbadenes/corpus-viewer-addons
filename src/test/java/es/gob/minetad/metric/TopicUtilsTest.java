package es.gob.minetad.metric;

import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicUtilsTest.class);

    @Test
    public void ten(){

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(10));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(20));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(30));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(40));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(50));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(60));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(70));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(80));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(90));

        Assert.assertEquals(Integer.valueOf(100000), TopicUtils.multiplier(99));

    }

    @Test
    public void hundred(){

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(100));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(200));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(300));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(400));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(500));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(600));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(700));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(800));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(900));

        Assert.assertEquals(Integer.valueOf(1000000), TopicUtils.multiplier(999));

    }

    @Test
    public void comparison(){

        Topic t1 = new Topic();
        t1.setWords(Arrays.asList(new TopicWord[]{ new TopicWord(new Word("A"),0.0), new TopicWord(new Word("B"),0.0), new TopicWord(new Word("C"),0.0)  }));

        Topic t2 = new Topic();
        t2.setWords(Arrays.asList(new TopicWord[]{ new TopicWord(new Word("D"),1.0), new TopicWord(new Word("B"),1.0), new TopicWord(new Word("C"),1.0), new TopicWord(new Word("E"),1.0)   }));

        TopicUtils.similarity(t1, t2, false);

    }

}
