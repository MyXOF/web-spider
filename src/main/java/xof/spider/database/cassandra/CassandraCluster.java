package xof.spider.database.cassandra;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xof.spider.configuration.SpiderConfig;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class CassandraCluster {
	private static Logger logger = LoggerFactory.getLogger(CassandraCluster.class);
		
	public static CassandraCluster getInstance() {
		if (myCluster == null)
			myCluster = new CassandraCluster();
		return myCluster;
	}
	
	protected static CassandraCluster myCluster;
	
	private String[] nodes;
	// one cluster for per phisical
	private Cluster cluster;
	// one session for per ks
	public Session session;
	
	private static final  String createKsCql = "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'%s', 'replication_factor':%d};";
	
//	private static final String createHotCfCql = "CREATE TABLE IF NOT EXISTS %s.%s (" 
//											+ "document blob," + "weight double,"
//											+ "PRIMARY KEY(document)" + ");";
	
	private static final String createWordCfCql = "CREATE TABLE IF NOT EXISTS %s.%s (" 
											+ "DocID int," + "Weight double,"  + "Line int," + "File blob," + "Title blob,"
											+ "PRIMARY KEY(DocID)" + ");";
	
//	private static final String createDocCFCql = "CREATE TABLE IF NOT EXISTS %s.%s ("
//											+ "DocID int," + "File blob," + "Title blob,"
//											+ "PRIMARY KEY(DocID)" + ");";
	
//	private static final String DOC_CF = "doc_document";
		
	private static final String createSentenceCFCql = "CREATE TABLE IF NOT EXISTS %s.%s (" 
											+ "Line int," + "Content blob,"
											+ "PRIMARY KEY(Line)" + ");";
	
//	private static String insertCql = "insert into %s.%s(document,weight) values('%s',%f);";
	private static String selectCql = "select * from %s.%s";
	private static String selectCountCql = "select count(*) from %s.%s";
	
	private CassandraCluster() {
		SpiderConfig config = SpiderConfig.getInstance();
		nodes = config.cassandra_nodes.split(",");
		connect(nodes);
		createKs(config.cassandra_keyspace_word, config.cassandra_partition_strategy, config.cassandra_replica_factor);
		createKs(config.cassandra_keyspace_sentence, config.cassandra_partition_strategy, config.cassandra_replica_factor);
	}
	
	public Session getSession() {
		return session;
	}
	
	public void connect(String[] nodes) {
		if (nodes == null || nodes.length == 0){
			logger.warn("CassandraCluster : no ip node to init");
			return;

		}
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		for (String node : nodes) {
			try {
				addresses.add(InetAddress.getByName(node));
			}
			catch (UnknownHostException e) {
				logger.error(e.getMessage());
			}
		}
		cluster = Cluster.builder().addContactPoints(addresses).build();
		logger.info("CassandraCluster : Try connect cassandra");
		try {
			Metadata metadata = cluster.getMetadata();
			logger.info("CassandraCluster: Connected to cluster");
			for (Host host : metadata.getAllHosts()) {
				logger.info("Datatacenter: {}; Host: {}; Rack: {}",
						new Object[] { host.getDatacenter(), host.getAddress().toString(), host.getRack() });
			}
			session = cluster.connect();
		}
		catch (Exception e) {
			logger.error("CassandraCluster : failed to connect to cluster",e);
		}
	}
	
	public void close() {
		if (session != null) {
			session.close();
			session = null;
		}
		if (cluster != null) {
			cluster.close();
			cluster = null;
		}
		myCluster = null;
	}
	
	public void createKs(String ks, String strategy, int replication){
		try {
			session.execute(String.format(createKsCql, ks, strategy, replication));
		}
		catch (Exception e) {
			logger.error("CassandraCluster : failed to create keyspace {}",ks,e);
		}
	}
	
	public void createWordCf(String ks,String cf){
		try {
			session.execute(String.format(createWordCfCql, ks,cf));
		} catch (Exception e) {
			logger.error("CassandraCluster : fail to create cf {} from keyspace {}",cf,ks,e);
		}
	}
	
//	public void createDocCf(String ks) {
//		try {
//			session.execute(String.format(createDocCFCql, ks, DOC_CF));
//		}
//		catch (Exception e) {
//			logger.error("CassandraCluster : fail to create cf {} from keyspace {}",DOC_CF,ks,e);
//		}
//	}
	
	public void createSentenceCf(String ks,String cf){
		try {
			session.execute(String.format(createSentenceCFCql, ks,cf));
		} catch (Exception e) {
			logger.error("CassandraCluster : fail to create cf {} from keyspace {}",cf,ks,e);
		}
	}
	
	public ResultSet InsertWord(String ks,String cf,int docID,double weight,int line,ByteBuffer file,ByteBuffer title){
		Statement statement = QueryBuilder.insertInto(ks, cf).value("DocID", docID).value("Weight", weight).value("Line", line).value("File", file).value("Title", title);
		ResultSet rSet = session.execute(statement);
		return rSet;
	}
	
//	public ResultSet InsertDoc(String ks,int docID,ByteBuffer file,ByteBuffer title){
//		Statement statement = QueryBuilder.insertInto(ks,DOC_CF).value("DocID", docID).value("File", file).value("Title", title);
//		ResultSet rSet = session.execute(statement);
//		return rSet;
//	}
	
	public ResultSet InsertSentence(String ks,String cf,int line,ByteBuffer content){
		Statement statement = QueryBuilder.insertInto(ks, cf).value("Line", line).value("Content", content);
		ResultSet rSet = session.execute(statement);
		return rSet;
	}
	
//	public void createHotCf(String ks, String cf) {
//		try {
//			session.execute(String.format(createHotCfCql, ks, cf));
//		}
//		catch (Exception e) {
//			logger.error("CassandraCluster : fail to create hot cf {} from keyspace {}",cf,ks,e);
//		}
//	}
	
//	public ResultSet insert(String ks, String cf, ByteBuffer com, Double weight) {
//		Statement statement = QueryBuilder.insertInto(ks, cf).value("document", com).value("weight", weight);
//		ResultSet rSet = session.execute(statement);
//		return rSet;
//	}
	
//	public ResultSet batchInsert(String ks, String cf, ByteBuffer[] documents, Double[] weights) {
//		String sql = "insert into "+ks+"."+cf+"(document,weight) values('%s',%f);";
//		PreparedStatement statement = session.prepare(sql);
//		
//		List<ResultSetFuture> futures = new ArrayList<ResultSetFuture>();
//		for (int i = 0;i < documents.length;i++) {
//			BoundStatement bind = statement.bind(documents[i], weights[i]);
//			ResultSetFuture resultSetFuture = session.executeAsync(bind);
//			futures.add(resultSetFuture);
//		}
//		
//		for (ResultSetFuture future : futures)
//			future.getUninterruptibly();
//		
//		StringBuilder builder = new StringBuilder();
//		builder.append("BEGIN BATCH");
//		for (int i = 0;i < documents.length;i++) {
//			builder.append(String.format(insertCql, ks,cf,documents[i], weights[i]));
//		}
//		builder.append("APPLY BATCH;");
//		
//		ResultSet rSet = session.execute(builder.toString());
//		return rSet;
//	}
	
	public ResultSet select(String ks, String cf) {
		ResultSet rSet = null;
		
		logger.debug(String.format(selectCql, ks, cf));
		rSet = session.execute(String.format(selectCql, ks, cf));
		
		return rSet;
	}
	
	public boolean checkKs(String ks) {
		KeyspaceMetadata ksm = cluster.getMetadata().getKeyspace(ks);
		if (ksm == null)
			return false;
		return true;
	}
	
	public boolean checkCf(String ks, String cf) {
		KeyspaceMetadata ksm = cluster.getMetadata().getKeyspace(ks);
		if (ksm == null)
			return false;
			
		TableMetadata tm = ksm.getTable(cf);
		if (tm == null)
			return false;
			
		return true;
	}
	
	public ResultSet selectCount(String ks, String cf) {
		ResultSet rSet = null;
		
		logger.debug(String.format(selectCountCql, ks, cf));
		rSet = session.execute(String.format(selectCountCql, ks, cf));
		
		return rSet;
	}
	
	public static void main(String[] args) throws IOException {
//		CassandraCluster test = CassandraCluster.getInstance();
//		if(test.checkKs("test")){
//			System.out.println("test created");
//		}
//		
//		String name = "wikipedia_dada-_da4efds32234sf/tads.html";
//		double weight = 0.0312313;
//		
//		Double[] weights = new Double[10];
//		ByteBuffer[] documents = new ByteBuffer[10];
//		
//		for(int i = 0;i < 10;i++){
//			weights[i] = i+weight;
//			documents[i] = ByteBuffer.wrap(Gzip.compress(name+i));
//		}
		
//		if(!test.checkCf("test", "doge")){
//			test.createCf("test", "doge");
//		}
//		test.batchInsert("test1", "doge", documents, weights);
		
//		byte[] com = Gzip.compress(name);
//		test.insert("test", "doge", ByteBuffer.wrap(com), 0.003012311);
//		
//		ResultSet result = test.select("test", "doge");
//		
//		for(Row row : result.all()){
//			String document = Gzip.decompress(row.getBytes(0).array());
//			System.out.println("document name: "+document+" weight: "+row.getDouble(1));
//		}
//		
//		test.close();
	}
}
