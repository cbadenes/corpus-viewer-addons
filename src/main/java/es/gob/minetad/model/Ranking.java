/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Ranking<T> implements Serializable{

    Map<T,Integer> positions   = new ConcurrentHashMap<>();

    Map<T,Double> weights      = new ConcurrentHashMap<>();

    AtomicInteger counter = new AtomicInteger(1);

    private String id;

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public Ranking add(T element, Double weight){
        if (!positions.containsKey(element)){
            positions.put(element,counter.getAndIncrement());
            weights.put(element,weight);
        }
        return this;
    }

    public Integer getPosition(T element){
        if (!positions.containsKey(element)) return positions.size()+1;
        return positions.get(element);
    }

    public Double getWeight(T element){
        if (!weights.containsKey(element)) return 0.0;
        return weights.get(element);
    }

    public boolean exist(T element){
        return positions.containsKey(element);
    }

    public List<T> getElements(){
        return new ArrayList(positions.keySet());
    }


    public List<Pair<T, Double>> getPairs(){
        return this.weights.entrySet().stream().map(e -> new Pair<T,Double>(e.getKey(),e.getValue())).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "["+positions.entrySet().stream().sorted((o1,o2) -> o1.getValue().compareTo(o2.getValue())).map(el ->
                "("+el.getKey().toString()+","+weights.get(el.getKey())+")").collect(Collectors.joining(","))+"]";
    }
}
