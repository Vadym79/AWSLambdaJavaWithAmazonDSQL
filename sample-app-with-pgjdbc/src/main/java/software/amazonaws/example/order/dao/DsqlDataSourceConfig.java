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
		config.setMaximumPoolSize(5); // default is 10

		String authToken = getAuthToken();

		config.setPassword(authToken);
		hds = new HikariDataSource(config);

		// Set additional properties
		hds.setMaxLifetime(1500 * 1000); // pool connection expiration time in milli seconds
	

		System.out.println("before creating authToken");
	}

	/**
	 * creates jdbc connection
	 * 
	 * @return jdbc connection
	 * @throws SQLException
	 */
	public static Connection getPooledConnection() throws SQLException {
		// Use generateDbConnectAuthToken when connecting as `admin` user
		String authToken = getAuthToken();
		hds.setPassword(authToken);
		return hds.getConnection();
	}
	

	public static Connection getJDBCConnection() throws SQLException {
		Properties props = new Properties();
		props.setProperty("user", "admin");
		String authToken = getAuthToken();
		props.setProperty("password", authToken);
		return DriverManager.getConnection(JDBC_URL, props);
	}

	/**
	 * creates auth token
	 * 
	 * @return auth token
	 */
	private static String getAuthToken() {
		String authToken= utilities.generateDbConnectAdminAuthToken(builder -> builder.hostname(AURORA_DSQL_CLUSTER_ENDPOINT)
				.region(Region.of(REGION.toLowerCase())).expiresIn(Duration.ofMillis(900 * 1000))); // Token expiration, default is 900 seconds
		//System.out.println("authToken : " + authToken);
		return authToken;
																									 
	}
}