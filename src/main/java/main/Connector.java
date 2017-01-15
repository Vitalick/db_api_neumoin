package main;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Connector {
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    public static final String URL = "jdbc:mysql://localhost:3306/TPForum?autoreconnect=true&useUnicode=yes&characterEncoding=UTF-8";

    public static final String USER = "Alexandra";

    public static final String PASSWORD = "secret";

    @SuppressWarnings("UnusedAssignment")
    public DataSource createSource() throws Exception
    {
        Class.forName(DRIVER).newInstance();
        final GenericObjectPool connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(100);
        final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(URL, USER, PASSWORD);

        final PoolableConnectionFactory pcf = new PoolableConnectionFactory(
                connectionFactory,
                connectionPool,
                null,
                null,
                false,
                true);
        return new PoolingDataSource(connectionPool);
    }
}
