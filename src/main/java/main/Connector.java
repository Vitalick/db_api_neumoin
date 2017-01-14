package main;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Connector {
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String URL_DB = "jdbc:mysql://localhost:3306/TPForum?autoreconnect=true&useUnicode=yes&characterEncoding=UTF-8";
    public static final String USER_DB = "Alexandra";
    public static final String PASSWORD_DB = "secret";

    private GenericObjectPool connectionPool = null;

    public DataSource createSource() throws Exception
    {
        Class.forName(DRIVER).newInstance();
        connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(100);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(URL_DB, USER_DB, PASSWORD_DB);

        PoolableConnectionFactory pcf = new PoolableConnectionFactory(connectionFactory, connectionPool,
                        null, null, false, true);
        return new PoolingDataSource(connectionPool);
    }

    public GenericObjectPool getConnectionPool() {
        return connectionPool;
    }
}
