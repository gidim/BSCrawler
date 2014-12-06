package Crawler;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import org.hibernate.Session;
import org.hibernate.internal.QueryImpl;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.spi.PersistenceProvider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gideon on 11/27/14.
 */
public class DAO {

    private static DAO singleton = null;
    private static final String PERSISTENCE_UNIT_NAME = "NewPersistenceUnit";
    private static EntityManagerFactory factory;


    protected DAO() {
        PersistenceProvider pp = new HibernatePersistenceProvider();
        factory = pp.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, new HashMap());
    }

    public synchronized static DAO getInstance() {
        if(singleton == null) {
            singleton = new DAO();
        }
        return singleton;
    }

    public void save(Object obj){

        boolean exists = false;
        if(obj instanceof Blog){
            Blog b = getBlogByURL(((Blog) obj).getUrl());
            if(b!= null)
                exists =true;
        }
        if(obj instanceof URLToVisit){
            URLToVisit b = getURLToVisitByURL(((URLToVisit) obj).getUrl());
            if(b!= null)
                exists =true;
        }

        if(!exists) {
            try {
                EntityManager em = factory.createEntityManager();
                em.getTransaction().begin();
                em.persist(obj);
                em.getTransaction().commit();
                em.close();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                System.out.println(timeStamp + "Saved:" + obj);
            } catch (javax.persistence.RollbackException Ex) {
                return;
            }
        }
    }

    public void delete(Object obj){

        EntityManager em = factory.createEntityManager();
        em.remove(em.contains(obj) ? obj : em.merge(obj));
        em.close();
    }

    public void deleteURLToVisit(URLToVisit url){
        URLToVisit ret = null;
        EntityManager em = factory.createEntityManager();
        Session session = em.unwrap(Session.class);
        String id = String.format("'%s'", url.getUrl());
        em.getTransaction().begin();
        org.hibernate.internal.QueryImpl q = (QueryImpl) session.createQuery("delete URLToVisit where id ="+id);
        q.executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();

    }

    public URLToVisit getURLToVisitByURL(String url){
        URLToVisit ret = null;
        EntityManager em = factory.createEntityManager();
        Session session = em.unwrap(Session.class);
        session.beginTransaction();
        ret = (URLToVisit) session.get(URLToVisit.class,url);
        session.close();
        return ret;
    }


    public Blog getBlogByURL(String url){
        Blog ret = null;
        EntityManager em = factory.createEntityManager();
        Session session = em.unwrap(Session.class);
        session.beginTransaction();
        ret = (Blog) session.get(Blog.class,url);
        session.close();
        return ret;
    }

    public List<URLToVisit> dequeueListOfURLToVisit(int limit){

            EntityManager em = factory.createEntityManager();
            Query query = em.createQuery("SELECT urltovisit FROM URLToVisit urltovisit", URLToVisit.class).setMaxResults(limit);
            List<URLToVisit> results = query.getResultList();
            em.close();
            for(URLToVisit url : results){
                this.deleteURLToVisit(url);
            }
            return results;
    }

    public List<URLToVisit> getListOfURLToVisit(String like){

            EntityManager em = factory.createEntityManager();
            TypedQuery<URLToVisit> query = em.createQuery("SELECT urlToVisit FROM URLToVisit urlToVisit where url like :searchKeyword" , URLToVisit.class);
            query.setParameter("searchKeyword",like);
            List<URLToVisit> found = query.getResultList();
            em.close();
            return found;

    }




}
