package es.gob.minetad.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Alarm {

    private static final Logger LOG = LoggerFactory.getLogger(Alarm.class);

    private Integer type;

    private Map<String,Long> groups;

    public Alarm(Integer type) {
        this.type = type;
        this.groups = new HashMap<>();
    }

    public void addGroup(String name, Long num){
        this.groups.put(name,num);
    }

    public Long getTotal(){
        return this.groups.entrySet().stream().map(e -> e.getValue()).reduce((a,b) -> a+b).get();
    }

    public Map<String,Long> getGroups(){
        return this.groups;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "type=" + type +
                ", groups=" + groups.size() +
                ", elements=" + getTotal() +
                '}';
    }
}
