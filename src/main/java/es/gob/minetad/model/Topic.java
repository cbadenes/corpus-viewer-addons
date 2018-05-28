package es.gob.minetad.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Topic {

    private static final Logger LOG = LoggerFactory.getLogger(Topic.class);

    private String id;

    private String name;

    private String description;

    private List<TopicWord> words;

    public Topic() {
    }

    public Topic(String id, String name, String description, List<TopicWord> words) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.words = words;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TopicWord> getWords() {
        return words;
    }

    public void setWords(List<TopicWord> words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
