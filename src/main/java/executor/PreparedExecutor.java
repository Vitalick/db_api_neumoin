package executor;

import handlers.TResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;


public class PreparedExecutor {
    /*public static void execQuery(Connection connection, String query, String[] paramsArray)
            throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(query);
        ArrayList<String> params = new ArrayList<>(Arrays.asList(paramsArray));
        for (int pos = 1; pos <= params.size(); pos++) {
            stmt.setString(pos, params.get(pos));
        }
        stmt.close();
    }*/

    public static ResultSet execQueryAndGetGK(Connection connection, String query, String[] paramsArray)
            throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(query);
        ArrayList<String> params = new ArrayList<>(Arrays.asList(paramsArray));
        for (int pos = 1; pos <= params.size(); pos++) {
            stmt.setString(pos, params.get(pos));
        }
        stmt.close();
        return stmt.getGeneratedKeys();
    }

    public static <T> T execQuery(Connection connection, String query, String[] paramsArray, TResultHandler<T> handler)
            throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(query);
        ArrayList<String> params = new ArrayList<>(Arrays.asList(paramsArray));
        System.out.println(params.get(0));
        for (int pos = 1; pos <= params.size(); pos++) {
            stmt.setString(pos, params.get(pos-1));
        }
        ResultSet resultSet = stmt.executeQuery();
        T value = handler.handle(resultSet);
        stmt.close();
        return value;
    }
}
