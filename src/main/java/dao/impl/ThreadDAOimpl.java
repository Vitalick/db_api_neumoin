package dao.impl;

import dao.ThreadDAO;
import executor.TExecutor;

import java.sql.Connection;
import java.sql.SQLException;


public class ThreadDAOimpl implements ThreadDAO {
    Connection connection;

    public ThreadDAOimpl(Connection connection) { this.connection = connection; }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE thread;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE subscribed;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
            int count = TExecutor.execQuery(connection, "SELECT COUNT(*) FROM thread WHERE isDeleted=0;",resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

