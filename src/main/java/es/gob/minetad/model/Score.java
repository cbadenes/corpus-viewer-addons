package es.gob.minetad.model;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Score {

    private Double value;
    private Document reference;
    private Document similar;

    public Score(Double value, Document reference, Document similar) {
        this.value = value;
        this.reference = reference;
        this.similar = similar;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Document getReference() {
        return reference;
    }

    public void setReference(Document reference) {
        this.reference = reference;
    }

    public Document getSimilar() {
        return similar;
    }

    public void setSimilar(Document similar) {
        this.similar = similar;
    }

    @Override
    public String toString() {
        return "Score{" +
                "value=" + value +
                ", reference=" + reference +
                ", similar=" + similar +
                '}';
    }
}
