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

    private String path;

    Map<Integer,Model> models = new HashMap<>();

    public Corpus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void add(Model model){
        this.models.put(model.getNumtopics(),model);
    }

    public Map<Integer, Model> getModels() {
        return models;
    }

    public static class Model{
        private String doctopics;
        private String api;
        private Integer numtopics;
        private Double entropy;

        public Model() {
        }

        public Double getEntropy() {
            return entropy;
        }

        public void setEntropy(Double entropy) {
            this.entropy = entropy;
        }

        public Integer getNumtopics() {
            return numtopics;
        }

        public void setNumtopics(Integer numtopics) {
            this.numtopics = numtopics;
        }

        public String getDoctopics() {
            return doctopics;
        }

        public void setDoctopics(String doctopics) {
            this.doctopics = doctopics;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

    }
}
