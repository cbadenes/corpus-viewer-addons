/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Permutations<T> {

    public List<Pair<T,T>> between(List<T> el1,List<T> el2 ){
        if (el1.isEmpty() || el2.isEmpty()) return Collections.emptyList();
        return el1.stream().flatMap(el -> combine(el, el2)).collect(Collectors.toList());
    }

    private Stream<Pair<T,T>> combine(T el, List<T> list){
        return list.stream().map(r -> new Pair<T,T>(el,r));
    }


    public Set<Pair<T,T>> sorted(List<T> elements){

        if (elements.isEmpty() || elements.size() == 1) return Collections.emptySet();

        //Set<Pair<T>> result = sorted(elements.get(0), elements.subList(1, elements.size()));

        Set<Pair<T,T>> result = sorted(elements.get(0), elements);

        result.addAll(sorted(elements.subList(1,elements.size())));

        return result;

    }

    public Set<Pair<T,T>> sorted(T element, List<T> elements){
        return elements.stream().map(el -> new Pair<T,T>(element, el)).collect(Collectors.toCollection(HashSet::new));
    }
}
