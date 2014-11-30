package Crawler;


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
        ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
        DAO = DAO.getInstance();

        //TESTING
        URLToVisit newTestBlog = new URLToVisit();
        newTestBlog.setUrl("http://nyconcertmeister.blogspot.com/");
        DAO.save(newTestBlog);

        int numOfPages = 0;

        //queue of links waiting for inspection
        List<URLToVisit> urlQueue;

        urlQueue = DAO.dequeueListOfURLToVisit(15);
        while (!urlQueue.isEmpty()) {

                for (URLToVisit url : urlQueue) {
                    Runnable worker = new Crawl(url);
                    //executor.execute(worker);
                    worker.run(); //testing only!
                }

                //refill the queue
            urlQueue = DAO.dequeueListOfURLToVisit(15);
        }

        executor.shutdown();
        while(!executor.isTerminated());

        executor.awaitTermination(10l, TimeUnit.MINUTES);

        System.out.println("Finished all threads");



    }

}
