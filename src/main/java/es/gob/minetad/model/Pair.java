/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.model;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Pair<T,K> implements Serializable {

    T i;
    K j;

    public Pair(T i, K j){
        this.i = i;
        this.j = j;
    }

    public T getI() {
        return i;
    }

    public void setI(T i) {
        this.i = i;
    }

    public K getJ() {
        return j;
    }

    public void setJ(K j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Pair.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Pair other = (Pair) obj;

        return (other.getI().equals(i) && other.getJ().equals(j))
                || (other.getI().equals(j) && other.getJ().equals(i));
    }

    @Override
    public int hashCode() {
        String value = Lists.newArrayList(i, j).stream().map(el -> el.toString()).sorted((o1, o2)->o1.compareTo(o2)).collect(Collectors.joining(","));

        int hash = 3;
        hash = 53 * hash + value.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "i="+i+"/j="+j;
    }
}
