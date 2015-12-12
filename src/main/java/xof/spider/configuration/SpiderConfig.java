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
	public String DB_WEB_TO_VISIT;
	public String DB_WEB_VISITED;
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
			DB_WEB_TO_VISIT = prop.getProperty("db_web_to_visit");
			DB_WEB_VISITED = prop.getProperty("db_web_visited");
			in.close();
		} catch (Exception e) {
			logger.error("SpiderConfig: errors occur when reading config file from {}",CONFIG_FILE,e);
		}
	}
	
}
