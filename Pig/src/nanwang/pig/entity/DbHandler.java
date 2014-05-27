package nanwang.pig.entity;

import org.hibernate.HibernateException;
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
	
	private static DbHandler instance = null;
	private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry = null;
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
	}
    
    /**
     * This method is used for closing sessionFactory
     */
    public static void close(){
    	sessionFactory.close();
    }
}
