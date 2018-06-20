package es.gob.minetad.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Combination<T extends Comparable> {

    private static final Logger LOG = LoggerFactory.getLogger(Combination.class);

    List<Pair> pairs;

    public Combination(List<T> elements) {
        this.pairs = permute(elements);
    }

    private List<Pair> permute(List<T> list){
        if (list.isEmpty()) return Collections.emptyList();
        if (list.size() == 1) return Collections.emptyList();
        List<Pair> pairs = combine(list.get(0), list.subList(1, list.size()));
        pairs.addAll(permute(list.subList(1,list.size())));
        return pairs;
    }

    private List<Pair> combine(T x, List<T> list){
        return list.stream().map( e -> new Pair(x,e)).collect(Collectors.toList());
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    public class Pair{

        private final T t1;
        private final T t2;

        public Pair(T t1, T t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public T getT1() {
            return t1;
        }

        public T getT2() {
            return t2;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "t1=" + t1 +
                    ", t2=" + t2 +
                    '}';
        }
    }

}
