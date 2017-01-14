package dao.impl;

import controllers.CustomResponse;
import dao.UserDAO;
import dataSets.PostDataSet;
import dataSets.UserDataSet;
import executor.PreparedExecutor;
import executor.TExecutor;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAOimpl implements UserDAO{
    ObjectMapper mapper;

    public UserDAOimpl() {
        mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE user;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE follows;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE user_forum;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
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
        try (Connection connection = Main.connection.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user WHERE email = ?;");
            stmt.setString(1, email);
            ResultSet resultSet = stmt.executeQuery();
            UserDataSet user = null;
            if (resultSet.next()) {
                user = new UserDataSet(resultSet);
                setFollowers(connection, user);
                setFollowing(connection, user);
                setSubscriptions(connection, user);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }

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

        try (Connection connection = Main.connection.getConnection()) {
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

    public CustomResponse follow(String followString) {
        JsonNode json;
        try {
            json = mapper.readValue(followString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        String follower = json.get("follower").getTextValue();
        String followed = json.get("followee").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "INSERT IGNORE INTO follows (follower, followed) VALUES (?,?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.executeUpdate();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse unfollow(String unfollowString) {
        JsonNode json;
        try {
            json = mapper.readValue(unfollowString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        String follower = json.get("follower").getTextValue();
        String followed = json.get("followee").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "DELETE FROM follows WHERE follower=? AND followed=?;";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.execute();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listFollowers(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            List<UserDataSet> followers = new ArrayList<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT follower FROM follows f JOIN user u ON f.follower = u.email");
            queryBuilder.append(" WHERE f.followed=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                UserDataSet follower = (UserDataSet)details(resultSet.getString(1)).getResponse();
                if ( since_id == null || since_id != null && follower.getId() >= new Integer(since_id) )
                    followers.add(follower);
            }

            stmt.close();
            return new CustomResponse(followers, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listFollowing(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            List<UserDataSet> following = new ArrayList<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT followed FROM follows f JOIN user u ON f.followed = u.email");
            queryBuilder.append(" WHERE f.follower=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                UserDataSet followed = (UserDataSet)details(resultSet.getString(1)).getResponse();
                if ( since_id == null || since_id != null && followed.getId() >= new Integer(since_id) )
                    following.add(followed);
            }

            stmt.close();
            return new CustomResponse(following, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listPosts(String email, String since, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            List<PostDataSet> posts = new ArrayList<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT id FROM post WHERE user = ?");
            if (since != null) queryBuilder.append( "AND date >= ?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                PostDataSet post = (PostDataSet)new PostDAOimpl().details(resultSet.getString(1), new ArrayList<>()).getResponse();
                posts.add(post);
            }

            stmt.close();
            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse updateProfile(String userString) {
        JsonNode json;
        try {
            json = mapper.readValue(userString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("user") || !json.has("name") || !json.has("about"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        String email = json.get("user").getTextValue();
        String name = json.get("name").getTextValue();
        String about = json.get("about").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE user SET name=?, about=? WHERE email=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, about);
            stmt.setString(3, email);
            stmt.executeUpdate();
            stmt.close();

            return details(email);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }


    public void setFollowers(Connection connection, UserDataSet user) throws SQLException {
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

    public void setFollowing(Connection connection, UserDataSet user) throws SQLException {
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

    public void setSubscriptions(Connection connection, UserDataSet user) throws SQLException {
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
