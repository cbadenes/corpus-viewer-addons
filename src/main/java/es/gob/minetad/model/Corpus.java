package es.gob.minetad.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Corpus {

    private static final Logger LOG = LoggerFactory.getLogger(Corpus.class);

    private String name;

    private Double entropy;

    public Corpus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getEntropy() {
        return entropy;
    }

    public void setEntropy(Double entropy) {
        this.entropy = entropy;
    }

    @Override
    public String toString() {
        return "Corpus{" +
                "name='" + name + '\'' +
                ", entropy=" + entropy +
                '}';
    }
}
