package xof.spider.database;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public abstract class AbstractFrontier {
	private final static Logger logger = LoggerFactory.getLogger(AbstractFrontier.class);
	private Environment env;
	private static final String CLASS_CATALOG = "java_class_catalog";
	private static final String URL_NAME = "URL";
	protected StoredClassCatalog javaCatalog;
	protected Database catalogDatabase;
	protected Database database;
	
	public AbstractFrontier(String homeDirectory) throws DatabaseException{
		logger.debug("AbstractFrontier: open environment in {}",homeDirectory);
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		try {
			env = new Environment(new File(homeDirectory), envConfig);
		} catch (DatabaseException e) {
			logger.error("AbstractFrontier: failed to create Environment",e);
			throw e;
		}
		
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setTransactional(true);
		dbConfig.setAllowCreate(true);
		
		catalogDatabase = env.openDatabase(null, CLASS_CATALOG, dbConfig);
		javaCatalog = new StoredClassCatalog(catalogDatabase);
		
		DatabaseConfig dbConfig0 = new DatabaseConfig();
		dbConfig0.setTransactional(true);
		dbConfig0.setAllowCreate(true);
		database = env.openDatabase(null, URL_NAME, dbConfig0);
	}
	
	public void close() throws DatabaseException{
		database.close();
		javaCatalog.close();
		env.close();
	}
	
	public abstract void put(Object key,Object value);	
	public abstract Object get(Object key);
	public abstract Object delete(Object key);
}
