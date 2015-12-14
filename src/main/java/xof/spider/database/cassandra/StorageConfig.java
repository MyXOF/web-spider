package xof.spider.database.cassandra;

public class StorageConfig {
	
	public static final String cassandra_nodes = "127.0.0.1";
	public static final int sql_cassandra_port = 9042;
	public static final String cassandra_keyspace = "test1";
	public static final String cassandra_partition_strategy = "SimpleStrategy";
	public static final int cassandra_replica_factor = 1;
	public static final String storage_engine = "cassandra";
	public static final String hottable_status = "HotIndexStatus.f";
	
}
