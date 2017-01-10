package dao.impl;

import dao.UserDAO;
import executor.TExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class UserDAOimpl implements UserDAO{
    Connection connection;

    public UserDAOimpl(Connection connection) { this.connection = connection; }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE user;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE follows;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
            int count = TExecutor.execQuery(connection, "SELECT COUNT(*) FROM user;", resultSet -> {
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
