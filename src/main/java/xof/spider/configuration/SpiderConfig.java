package xof.spider.configuration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiderConfig {
	private static final Logger logger = LoggerFactory.getLogger(SpiderConfig.class);
	private final String CONFIG_FILE = "/web-spider-config.properties";
	
	private static SpiderConfig config = new SpiderConfig();	
	public String URL;
	public String FILE_DIR;
	public String DB_WEB_TO_VISIT;
	public String DB_WEB_VISITED;
	public int THREAD_NUM;
	public String SOURCE_DATA_DIR;
	public int SOURCE_DATA_DEEP;
	public String SOURCE_DATA_FILTER_DIR;
	public String PARSE_DATA_DIR;
	// add by xinyi
	public int DB_WEB_NUM;
	
	public String cassandra_nodes = "127.0.0.1";
	public int sql_cassandra_port = 9042;
//	public String cassandra_keyspace = "test";
	public String cassandra_keyspace_word = "word";
	public String cassandra_keyspace_sentence = "sentence";
	public String cassandra_partition_strategy = "SimpleStrategy";
	public int cassandra_replica_factor = 1;
	public String storage_engine = "cassandra";
	public String hottable_status = "HotIndexStatus.f";
	
	public Set<String> stopWords;
	private final String STOP_WORDS_PATH = "/stopWordsIndex.txt";
	
	public static SpiderConfig getInstance(){
		return config;
	}
	
	private SpiderConfig(){
		readConfig();
	}
	
	private void readConfig(){
		stopWords = new HashSet<String>();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SpiderConfig.class.getResourceAsStream(STOP_WORDS_PATH)))){
			String line = "";
			while((line = bufferedReader.readLine()) != null){
				stopWords.add(line.toLowerCase());
			}
			bufferedReader.close();
		} catch (Exception e) {
			logger.error("SpiderConfig: errors occur when reading stop words file from {}",STOP_WORDS_PATH,e);
		}
		
		Properties prop = new Properties();
		try (InputStream in = SpiderConfig.class.getResourceAsStream(CONFIG_FILE)){
			prop.load(in);
			THREAD_NUM = Integer.parseInt(prop.getProperty("thread_num"));
			URL = prop.getProperty("url");
			FILE_DIR = prop.getProperty("file_dir");
			DB_WEB_TO_VISIT = prop.getProperty("db_web_to_visit");
			DB_WEB_VISITED = prop.getProperty("db_web_visited");
			
			SOURCE_DATA_DIR = prop.getProperty("source_data_dir");
			SOURCE_DATA_DEEP = Integer.parseInt(prop.getProperty("source_data_deep"));
			SOURCE_DATA_FILTER_DIR = prop.getProperty("source_data_filter_dir");
			PARSE_DATA_DIR = prop.getProperty("parse_data_dir");
			
			cassandra_nodes = prop.getProperty("cassandra_nodes");
			sql_cassandra_port = Integer.parseInt(prop.getProperty("sql_cassandra_port"));
//			cassandra_keyspace = prop.getProperty("cassandra_keyspace");
			cassandra_keyspace_word = prop.getProperty("cassandra_keyspace_word");
			cassandra_keyspace_sentence = prop.getProperty("cassandra_keyspace_sentence");
			cassandra_partition_strategy = prop.getProperty("cassandra_partition_strategy");
			cassandra_replica_factor = Integer.parseInt(prop.getProperty("cassandra_replica_factor"));
			storage_engine = prop.getProperty("storage_engine");
			hottable_status = prop.getProperty("hottable_status");
			in.close();
		} catch (Exception e) {
			logger.error("SpiderConfig: errors occur when reading config file from {}",CONFIG_FILE,e);
		}
	}
	
}
