package software.amazonaws.example.order.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dsql.DsqlUtilities;

public class DsqlDataSourceConfig {
	
	private static Connection jdbConnection=null; 
	static long startTime=System.currentTimeMillis();
	 
	private static final String REGION = System.getenv("REGION");
	private static final DsqlUtilities utilities = DsqlUtilities.builder().region(Region.of(REGION.toLowerCase()))
			.credentialsProvider(DefaultCredentialsProvider.create()).build();
	
	private static final String AURORA_DSQL_CLUSTER_ENDPOINT = System.getenv("AURORA_DSQL_CLUSTER_ENDPOINT");
			
	private static final String JDBC_URL = "jdbc:postgresql://"
			+ AURORA_DSQL_CLUSTER_ENDPOINT
			+ ":5432/postgres?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory";

	
	private static HikariDataSource hds;
	static {
		
		System.out.println("region : " + REGION.toLowerCase());
		System.out.println("url : " + AURORA_DSQL_CLUSTER_ENDPOINT);
				
		final HikariConfig config = new HikariConfig();
		config.setUsername("admin");
		config.setJdbcUrl(JDBC_URL);
		config.setMaxLifetime(1500 * 1000); // pool connection expiration time in milli seconds, default 30
		config.setMaximumPoolSize(1); // default is 10

		String authToken = getAuthTokenForAdminUser();

		config.setPassword(authToken);
		hds = new HikariDataSource(config);
		long endTime=System.currentTimeMillis();
		System.out.println("time to create hikari data source in ms "+(endTime-startTime));
		 
	}
	
	

	/**
	 * creates jdbc connection backed by Hikari data source pool
	 * 
	 * @return jdbc connection backed by Hikari data source pool
	 * @throws SQLException
	 */
	
	public static Connection getPooledConnection() throws SQLException {
		// Use generateDbConnectAuthToken when connecting as `admin` user
		long startTime=System.currentTimeMillis();
		String authToken = getAuthTokenForAdminUser();
		hds.setPassword(authToken);
		Connection connection= hds.getConnection();
		long endTime=System.currentTimeMillis();
		System.out.println("time to create hikari pooled connection in ms "+(endTime-startTime)); 
		return connection;
		
	}
	

	/** creates a new jdbc connection
	 * 
	 * @return new jdbc connection 
	 * @throws SQLException
	 */
	public static Connection getJDBCConnection() throws SQLException {
		long startTime = System.currentTimeMillis();
		if (jdbConnection == null || jdbConnection.isClosed()) {
			Properties props = new Properties();
			props.setProperty("user", "admin");
			String authToken = getAuthTokenForAdminUser();
			props.setProperty("password", authToken);
			jdbConnection = DriverManager.getConnection(JDBC_URL, props);
		}
		long endTime=System.currentTimeMillis();
		System.out.println("time to create jdbc connection in ms "+(endTime-startTime)); 
		return jdbConnection;
	}

	/**
	 * creates auth token
	 * 
	 * @return auth token
	 */
	private static String getAuthTokenForAdminUser() {
		long startTimeAuthToken=System.currentTimeMillis();
		String authToken= utilities.generateDbConnectAdminAuthToken(builder -> builder.hostname(AURORA_DSQL_CLUSTER_ENDPOINT)
				.region(Region.of(REGION.toLowerCase())).expiresIn(Duration.ofMillis(90*60*1000))); // Token expiration, default is 900 seconds
		//System.out.println("authToken : " + authToken);
		long endTimeAuthToken=System.currentTimeMillis();
		System.out.println("time to create auth token for admin user in ms "+(endTimeAuthToken-startTimeAuthToken)); 
		return authToken;
																									 
	}
}