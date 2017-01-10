package dao.impl;

import controllers.CustomResponse;
import dao.UserDAO;
import dataSets.UserDataSet;
import executor.PreparedExecutor;
import executor.TExecutor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;


public class UserDAOimpl implements UserDAO{
    Connection connection;
    ObjectMapper mapper;

    public UserDAOimpl(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
    }

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

    public CustomResponse details(String email) {
        CustomResponse response = new CustomResponse();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user WHERE email = ?;");
            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();
            UserDataSet user = null;
            if (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                user = new UserDataSet(resultSet);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }

            setFollowers(user);
            setFollowing(user);
            setSubscriptions(user);

            response.setResponse(user);
            response.setCode(CustomResponse.OK);
            return response;

        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse create(String userString) {
        CustomResponse response = new CustomResponse();
        UserDataSet user;

        try {
            JsonNode json = mapper.readValue(userString, JsonNode.class);
            if (!json.has("email") || !json.has("username") || !json.has("name") || !json.has("about")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
            try {
                user = new UserDataSet(json);

            } catch (Exception e) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
        } catch (IOException e) {
            response.setResponse("INVALID REQUEST");
            response.setCode(CustomResponse.INVALID_REQUEST);
            return response;
        }

        try {
            String query = "INSERT INTO user (email, username, name, about, isAnonymous) VALUES(?,?,?,?,?);";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getAbout());
            stmt.setBoolean(5, user.getIsAnonymous());
            stmt.execute();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                user.setId(generatedKeys.getInt(1));
            stmt.close();

            response.setResponse(user);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                response.setCode(CustomResponse.ALREADY_EXIST);
                return response;
            } else {
                response.setResponse("UNKNOWN ERROR");
                response.setCode(CustomResponse.UNKNOWN_ERROR);
                return response;
            }
        }
    }

    public void setFollowers(UserDataSet user) throws SQLException {
        ArrayList<String> followers = new ArrayList<>();

        String query = "SELECT follower FROM follows WHERE followed = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            followers.add(resultSet.getString("follower"));
        }
        stmt.close();
        user.setFollowers(followers);
    }

    public void setFollowing(UserDataSet user) throws SQLException {
        ArrayList<String> following = new ArrayList<>();

        String query = "SELECT followed FROM follows WHERE follower = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            following.add(resultSet.getString("followed"));
        }
        stmt.close();
        user.setFollowing(following);
    }

    public void setSubscriptions(UserDataSet user) throws SQLException {
        ArrayList<Integer> subscriptions = new ArrayList<>();

        String query = "SELECT thread FROM subscribed WHERE user = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            subscriptions.add(resultSet.getInt("thread"));
        }
        stmt.close();
        user.setSubscriptions(subscriptions);
    }
}
