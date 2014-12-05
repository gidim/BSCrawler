package Crawler;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gideon on 11/24/14.
 */
public class Main {

    private static final int NTHREDS = 30;
    private static DAO DAO = null;


    public static void main(String[] args) throws InterruptedException {
        //setup main thread pool
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        DAO = DAO.getInstance();
        boolean testing = false;

        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(NTHREDS+1);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();



        //TESTING
        if(testing) {
            URLToVisit newTestBlog = new URLToVisit();
            newTestBlog.setUrl("http://nyconcertmeister.blogspot.com/");
            DAO.save(newTestBlog);
        }
        int numOfPages = 0;

        //queue of links waiting for inspection
        List<URLToVisit> urlQueue;

        urlQueue = DAO.dequeueListOfURLToVisit(150);
        while (!urlQueue.isEmpty()) {

            Iterator <URLToVisit> it = urlQueue.iterator();
            while(it.hasNext()){
                URLToVisit url = it.next();
                    Runnable worker = new Crawl(url,httpclient);

                    if(!testing) {
                        executor.execute(worker);
                    }
                    else {
                        worker.run(); //testing only!
                    }
                it.remove();
                }



            Thread.sleep(1000);

            //refill the queue
            if(urlQueue.size() < 10) {
                urlQueue = DAO.dequeueListOfURLToVisit(15);
            }
        }

        executor.shutdown();
        while(!executor.isTerminated());

        executor.awaitTermination(10l, TimeUnit.MINUTES);

        System.out.println("Finished all threads");



    }

}
