package nanwang.pig.entity;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * This class sets the configuration of Hibernate and handles the interaction with db
 * @author wangnan
 * @since 2014-05-01
 */

public class DbHandler {
	
	private static DbHandler instance;
	private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    private final static String configPath = "nanwang/pig/hibernate/config/hibernate.cfg.xml";
    
    private DbHandler(){
    	sessionFactory = getSessionFactory();
    }
    
    /**
     * This method is used for obtaining the DbHandler instance
     * @return
     */
    public static DbHandler getInstance() {
        if(instance == null) {
           instance = new DbHandler();
        }
        return instance;
     }
    
    /**
     * This method is used for configuring session factory
     * @return
     * @throws HibernateException
     */
    private SessionFactory configureSessionFactory() throws HibernateException {
        Configuration configuration = new Configuration();
        configuration.configure(configPath);
        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
        return configuration.buildSessionFactory(serviceRegistry);
    }

    /**
     * This method is used for obtaining a sessionFactory object
     * @return
     */
    private SessionFactory getSessionFactory() {
        return configureSessionFactory();
    }
    
    /**
     * This method is used for saving object into db
     * @param object
     */
    public void insert(Object object) {
    	Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(object);
		session.getTransaction().commit();
		session.close();
	}
    
    /**
     * This method is used for deleting counter from db based on script_name
     * @param scriptName
     */
    public void deleteCounter(String scriptName){
    	Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		Query query = session.createQuery("delete from counter where script_name = :script_name");
		query.setParameter("script_name", scriptName);
		
		query.executeUpdate();
		session.getTransaction().commit();
		session.close();
    }
    
    /**
     * This method is used for retrieve data from JobCounter in db
     * @param tableName
     * @param scriptName
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<JobCounter> readJobCounter(String scriptName, int jobRankNum){
    	Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		Query query = session.createQuery("from JobCounter where script_name = :script_name and jobRankNum =:jobRankNum");
		query.setParameter("script_name", scriptName);
		query.setParameter("jobRankNum", jobRankNum);
		
		List<JobCounter> list = (List<JobCounter>) query.list();
		
		session.getTransaction().commit();
		session.close();
		
		return list;
    }
    
    /**
     * This method is used for retrieve data from AVGCounter in db
     * @param tableName
     * @param scriptName
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AVGCounter> readAVGCounter(String scriptName, int jobRankNum){
    	Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		Query query = session.createQuery("from AVGCounter where script_name = :script_name and jobRankNum =:jobRankNum");
		query.setParameter("script_name", scriptName);
		query.setParameter("jobRankNum", jobRankNum);
		
		List<AVGCounter> list = (List<AVGCounter>) query.list();
		
		session.getTransaction().commit();
		session.close();
		
		return list;
    }
    
    /**
     * This method is used for updating AVGCounter in the db
     * @param reduceNum
     * @param mapCPUTime
     * @param reduceCPUTime
     * @param totalCPUTime
     * @param mapElapsedTime
     * @param reduceElapsedTime
     * @param shufflePhaseTime
     * @param sortPhaseTime
     * @param reducePhaseTime
     * @param shuffleBytes
     */
    @SuppressWarnings("unchecked")
    public void updateAVGCounter(AVGCounter newCounter){
    	
    	Session session = sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.createQuery("from AVGCounter where script_name = :script_name and reduceNum = :reduceNum and jobRankNum =:jobRankNum");
		query.setParameter("script_name", newCounter.getName());
		query.setParameter("reduceNum", newCounter.getReduceNum());
		query.setParameter("jobRankNum", newCounter.getJobRankNum());
		
		List<AVGCounter> list = (List<AVGCounter>) query.list();
		
		if(list.isEmpty()){
			insert(newCounter);
		}else {
			AVGCounter counter = list.get(0);
			counter.setMapCPUTime((counter.getMapCPUTime() + newCounter.getMapCPUTime())/2);
        	counter.setReduceCPUTime((counter.getReduceCPUTime() + newCounter.getReduceCPUTime())/2);
        	counter.setTotalCPUTime(counter.getMapCPUTime() + counter.getReduceCPUTime());
        	counter.setMapElapsedTime((counter.getMapElapsedTime() + newCounter.getMapElapsedTime())/2);
        	counter.setReduceElapsedTime((counter.getReduceElapsedTime() + newCounter.getReduceElapsedTime())/2);
        	counter.setShufflePhaseTime((counter.getShufflePhaseTime() + newCounter.getShufflePhaseTime())/2);
        	counter.setSortPhaseTime((counter.getSortPhaseTime() + newCounter.getSortPhaseTime())/2);
        	counter.setReducePhaseTime((counter.getReducePhaseTime() + newCounter.getReducePhaseTime())/2);
        	counter.setShuffleBytes((counter.getShuffleBytes() + newCounter.getShuffleBytes())/2);
        	session.saveOrUpdate(counter);
        	session.getTransaction().commit();
		}
    	session.close();
    }
    
    /**
     * This method is used for closing sessionFactory
     */
    public static void close(){
    	sessionFactory.close();
    }
    
    /**
     * This method is used for inserting a new job object
     * @param uuid
     * @param operation
     */
    public void insertJob(String uuid, String operation){
    	insert(new Job(uuid, operation));
    }
    
    /**
     * This method is used for inserting a new output result
     * @param uuid
     * @param output
     * @param pigname
     * @param state
     * @param time
     */
    public void insertOutput(String uuid, StringBuilder output, String pigname, String state, String time){
    	insert(new Output(uuid, output, pigname, state, time));
    }
    
    /**
     * This method is used for inserting a new mrplan
     * @param uuid
     * @param plan
     */
    public void insertMRPlan(String uuid, String plan){
    	insert(new MRPlan(uuid, plan));
    }
}
