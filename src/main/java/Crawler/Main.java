package Crawler;

import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.*;
import javax.persistence.spi.PersistenceProvider;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gideon on 11/24/14.
 */
public class Main {

    private static final int NTHREDS = 10;
    private static final String PERSISTENCE_UNIT_NAME = "NewPersistenceUnit";
    private static EntityManagerFactory factory;


    public static void main(String[] args) throws InterruptedException {

        PersistenceProvider pp = new HibernatePersistenceProvider();
        factory = pp.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, new HashMap());
        EntityManager em = factory.createEntityManager();
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);


        //TESTING
/*
        URLToVisit newTestBlog = new URLToVisit();
        newTestBlog.setUrl("http://nyconcertmeister.blogspot.com/");
        em.getTransaction().begin();
        em.persist(newTestBlog);
        em.getTransaction().commit();

*/
        int numOfPages = 0;

        //queue of links waiting for inspection
        List<URLToVisit> urlQueue;

            Query query = em.createQuery("SELECT urltovisit FROM URLToVisit urltovisit",URLToVisit.class).setMaxResults(15);
            urlQueue = query.getResultList();

        while (!urlQueue.isEmpty()) {

                for (URLToVisit url : urlQueue) {
                    Runnable worker = new Crawl(url,factory);
                    //executor.execute(worker);
                    worker.run(); //testing only!
                }

                //refill the queue
            query = em.createQuery("SELECT urltovisit FROM URLToVisit urltovisit",URLToVisit.class).setMaxResults(15);
            urlQueue = query.getResultList();
        }

        executor.shutdown();
        while(!executor.isTerminated());

        executor.awaitTermination(10l, TimeUnit.MINUTES);

        System.out.println("Finished all threads");



    }

}
