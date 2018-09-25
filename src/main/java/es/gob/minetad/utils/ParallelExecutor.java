/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 26/04/16:
 *
 * @author cbadenes
 */
public class ParallelExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelExecutor.class);

    ExecutorService pool;

    public ParallelExecutor(){
        int cpus = Runtime.getRuntime().availableProcessors();
        int maxThreads = (cpus > 1)? cpus-1 : cpus;
        pool = new ThreadPoolExecutor(
                maxThreads, // core thread pool size
                maxThreads, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(maxThreads, true),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public ParallelExecutor(int size){
        pool = new ThreadPoolExecutor(
                size, // core thread pool size
                size, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(size, true),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void submit(Runnable task){
        pool.submit(task);
    }


    public void shutdown(){
        pool.shutdown();
    }

    public boolean awaitTermination(long time, TimeUnit unit) {
        pool.shutdown();
        try {
            return pool.awaitTermination(time,unit);
        } catch (InterruptedException e) {
            LOG.debug("Interruption", e);
        }
        return true;
    }

    public void waitFor() {
        try {
            pool.wait();
        } catch (InterruptedException e) {
            LOG.debug("Interruption", e);
        }
    }

    public void waitFor(long timeout) {
        try {
            pool.wait(timeout);
        } catch (InterruptedException e) {
            LOG.debug("Interruption", e);
        }
    }

    public void waitFor(long timeout, int nanos) {
        try {
            pool.wait(timeout,nanos);
        } catch (InterruptedException e) {
            LOG.debug("Interruption", e);
        }
    }


}
