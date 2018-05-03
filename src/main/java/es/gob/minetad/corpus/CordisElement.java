package es.gob.minetad.corpus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CordisElement {

    private static final Logger LOG = LoggerFactory.getLogger(CordisElement.class);


    private String id;

    private String title;

    private String objective;

    private String instrument;

    private String startDate;

    private String endDate;

    private Integer totalCost;

    private String area;

    private Integer topicWater;

    public CordisElement() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Integer totalCost) {
        this.totalCost = totalCost;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getTopicWater() {
        return topicWater;
    }

    public void setTopicWater(Integer topicWater) {
        this.topicWater = topicWater;
    }

    @Override
    public String toString() {
        return "CordisElement{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", objective='" + objective + '\'' +
                ", instrument='" + instrument + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", totalCost='" + totalCost + '\'' +
                ", area='" + area + '\'' +
                ", topicWater='" + topicWater + '\'' +
                '}';
    }
}
