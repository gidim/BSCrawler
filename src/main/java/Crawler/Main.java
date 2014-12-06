package Crawler;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.HttpParams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Gideon on 11/24/14.
 */
public class Main {

    private static final int NTHREDS = 1;
    private static final int TIMEOUT = 30;
    private static DAO DAO = null;


    public static void main(String[] args) throws InterruptedException {
        //setup main thread pool with timeout
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, NTHREDS, TIMEOUT, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        DAO = DAO.getInstance();
        boolean testing = false;

        // Create an HttpClient with the ThreadSafeClientConnManager.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(NTHREDS+1);
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT * 1000)
                .setConnectTimeout(TIMEOUT * 1000)
                .setConnectionRequestTimeout(TIMEOUT * 1000)
                .build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(config)
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



            Thread.sleep(1000 * 60); // wait for a minute so the queue will fill up again

            //refill the queue
            if(urlQueue.size() < 10) {
                urlQueue = DAO.dequeueListOfURLToVisit(150);
            }
        }

        System.out.println("Finished Submitting");
        executor.shutdown();
        while(!executor.isTerminated()){
            System.out.println("Waiting for all threads to finish");
        }

        executor.awaitTermination(10l, TimeUnit.MINUTES);

        System.out.println("Finished all threads");



    }

}
