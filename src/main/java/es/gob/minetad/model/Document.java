package es.gob.minetad.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Document implements Serializable{

    private String id;
    private String name;
    private String type;
    private List<Double> shape;

    public Document() {
    }

    public Document(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Document(String id) {
        this.id = id;
    }

    public Document(String id, List<Double> shape) {
        this.id = id;
        this.shape = shape;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getShape() {
        return shape;
    }

    public void setShape(List<Double> shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
