package es.gob.minetad.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Corpus {

    private static final Logger LOG = LoggerFactory.getLogger(Corpus.class);

    private Integer numTopics;

    private String name;

    private String fullName;

    private String doctopicsPath;

    private String documentsPath;

    private String modelPath;

    private Double entropy;


    public Corpus(String fullName) {
        this.fullName = fullName;
        this.name = StringUtils.substringBefore(fullName,"-");
        this.numTopics = Integer.valueOf(StringUtils.substringAfter(fullName,"-"));
    }


    public String getFullName() {
        return fullName;
    }

    public String getDoctopicsPath() {
        return doctopicsPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDoctopicsPath(String doctopicsPath) {
        this.doctopicsPath = doctopicsPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getName(){
        return this.name;
    }

    public Integer getNumTopics(){
        return this.numTopics;
    }

    public Double getEntropy() {
        return entropy;
    }

    public void setEntropy(Double entropy) {
        this.entropy = entropy;
    }

    public void setNumTopics(Integer numTopics) {
        this.numTopics = numTopics;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentsPath() {
        return documentsPath;
    }

    public void setDocumentsPath(String documentsPath) {
        this.documentsPath = documentsPath;
    }

    @Override
    public String toString() {
        return "Corpus{" +
                "numTopics=" + numTopics +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", doctopicsPath='" + doctopicsPath + '\'' +
                ", documentsPath='" + documentsPath + '\'' +
                ", modelPath='" + modelPath + '\'' +
                ", entropy=" + entropy +
                '}';
    }
}
