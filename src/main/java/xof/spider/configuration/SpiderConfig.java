package xof.spider.configuration;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiderConfig {
	private static final Logger logger = LoggerFactory.getLogger(SpiderConfig.class);
	private final String CONFIG_FILE = "/config.properties";
	
	private static SpiderConfig config = new SpiderConfig();	
	public String URL;
	public String FILE_DIR;
	public String DB_DIR;
	public int THREAD_NUM;
	
	public static SpiderConfig getInstance(){
		return config;
	}
	
	private SpiderConfig(){
		readConfig();
	}
	
	private void readConfig(){
		Properties prop = new Properties();
		try (InputStream in = SpiderConfig.class.getResourceAsStream(CONFIG_FILE)){
			prop.load(in);
			THREAD_NUM = Integer.parseInt(prop.getProperty("thread_num"));
			URL = prop.getProperty("url");
			FILE_DIR = prop.getProperty("file_dir");
			DB_DIR = prop.getProperty("db_dir");
			in.close();
		} catch (Exception e) {
			logger.error("SpiderConfig: errors occur when reading config file from {}",CONFIG_FILE,e);
		}
	}
	
}
