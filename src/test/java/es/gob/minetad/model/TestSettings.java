package es.gob.minetad.model;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TestSettings {

    private static final Logger LOG = LoggerFactory.getLogger(TestSettings.class);
    private final Properties properties;

    public TestSettings() {
        try {
            this.properties = new Properties();
            properties.load(new FileInputStream("src/test/resources/config.properties"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String property){
        return this.properties.getProperty(property);
    }

    public String getSolrUrl(){
        String url = properties.getProperty("solr.url");
        return (url.startsWith("http"))? url : "http://"+ url + "/solr";
    }

    public boolean isSolrUp(){
        try {
            Unirest.get(getSolrUrl()).asString();
            return true;
        } catch (UnirestException e) {
            return false;
        }
    }

    public List<Corpus> getCorpora(){
        Map<String,Corpus> corpora = new HashMap<>();
        for(Enumeration e=properties.propertyNames(); e.hasMoreElements();){
            String propName = (String) e.nextElement();
            if (propName.startsWith("corpus.")){
                String name         = StringUtils.substringBefore(StringUtils.substringAfter(propName, "corpus."),".");
                Corpus corpus       = corpora.containsKey(name)? corpora.get(name) : new Corpus(name);
                String property         = StringUtils.substringAfter(propName, "corpus."+name+".");
                switch (property.toLowerCase()){
                    case "doctopics": corpus.setDoctopicsPath(properties.getProperty(propName));
                        break;
                    case "model": corpus.setModelPath(properties.getProperty(propName));
                        break;
                }
                corpora.put(name, corpus);
            }
        }

        return corpora.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

}
