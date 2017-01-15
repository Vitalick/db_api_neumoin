package dao.impl;

import controllers.CustomResponse;
import dao.UserDAO;
import dataSets.PostDataSet;
import dataSets.UserDataSet;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"OverlyComplexBooleanExpression", "OverlyComplexMethod", "JDBCResourceOpenedButNotSafelyClosed", "OverlyBroadCatchBlock"})
public class UserDAOimpl implements UserDAO{
    final ObjectMapper mapper;

    public UserDAOimpl() {
        mapper = new ObjectMapper();
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE user;");
            stmt.execute("TRUNCATE TABLE follows;");
            stmt.execute("TRUNCATE TABLE user_forum;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM user;");
            resultSet.next();
            final int count = resultSet.getInt(1);
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public CustomResponse details(String email) {
        try (Connection connection = Main.connection.getConnection()) {
            final PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user WHERE email = ?;");
            stmt.setString(1, email);
            final ResultSet resultSet = stmt.executeQuery();
            final UserDataSet user;
            if (resultSet.next()) {
                user = new UserDataSet(resultSet);
                setFollowers(connection, user);
                setFollowing(connection, user);
                setSubscriptions(connection, user);
                stmt.close();
            } else {
                stmt.close();
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }

            return new CustomResponse(user, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse create(String userString) {
        final UserDataSet user;

        try {
            final JsonNode json = mapper.readValue(userString, JsonNode.class);
            if (!json.has("email") || !json.has("username") || !json.has("name") || !json.has("about")) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
            try {
                user = new UserDataSet(json);

            } catch (Exception e) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "INSERT INTO user (email, username, name, about, isAnonymous) VALUES(?,?,?,?,?);";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getAbout());
            stmt.setBoolean(5, user.getIsAnonymous());
            stmt.execute();
            final ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                user.setId(generatedKeys.getInt(1));
            stmt.close();

            return new CustomResponse(user, CustomResponse.OK);
        } catch (SQLException e) {
            //noinspection MagicNumber
            if (e.getErrorCode() == 1062) {
                return new CustomResponse("ALREADY EXISTS", CustomResponse.ALREADY_EXIST);
            } else {
                return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
            }
        }
    }

    @Override
    public CustomResponse follow(String followString) {
        final JsonNode json;
        try {
            json = mapper.readValue(followString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final String follower = json.get("follower").getTextValue();
        final String followed = json.get("followee").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "INSERT IGNORE INTO follows (follower, followed) VALUES (?,?)";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.executeUpdate();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse unfollow(String unfollowString) {
        final JsonNode json;
        try {
            json = mapper.readValue(unfollowString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final String follower = json.get("follower").getTextValue();
        final String followed = json.get("followee").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "DELETE FROM follows WHERE follower=? AND followed=?;";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.execute();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public CustomResponse listFollowers(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final List<UserDataSet> followers = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT follower FROM follows f JOIN user u ON f.follower = u.email");
            queryBuilder.append(" WHERE f.followed=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final UserDataSet follower = (UserDataSet)details(resultSet.getString(1)).getResponse();
                if ( since_id == null || follower.getId() >= new Integer(since_id) )
                    followers.add(follower);
            }

            stmt.close();
            return new CustomResponse(followers, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public CustomResponse listFollowing(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final List<UserDataSet> following = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT followed FROM follows f JOIN user u ON f.followed = u.email");
            queryBuilder.append(" WHERE f.follower=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final UserDataSet followed = (UserDataSet)details(resultSet.getString(1)).getResponse();
                if ( since_id == null || followed.getId() >= new Integer(since_id) )
                    following.add(followed);
            }

            stmt.close();
            return new CustomResponse(following, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse listPosts(String email, String since, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final List<PostDataSet> posts = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT id FROM post WHERE user = ?");
            if (since != null) queryBuilder.append( "AND date >= ?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final PostDataSet post = (PostDataSet)new PostDAOimpl().details(resultSet.getString(1), new ArrayList<>()).getResponse();
                posts.add(post);
            }

            stmt.close();
            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse updateProfile(String userString) {
        final JsonNode json;
        try {
            json = mapper.readValue(userString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("user") || !json.has("name") || !json.has("about"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final String email = json.get("user").getTextValue();
        final String name = json.get("name").getTextValue();
        final String about = json.get("about").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE user SET name=?, about=? WHERE email=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
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
        final ArrayList<String> followers = new ArrayList<>();

        final String query = "SELECT follower FROM follows WHERE followed = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            followers.add(resultSet.getString("follower"));
        }
        stmt.close();
        user.setFollowers(followers);
    }

    public void setFollowing(Connection connection, UserDataSet user) throws SQLException {
        final ArrayList<String> following = new ArrayList<>();

        final String query = "SELECT followed FROM follows WHERE follower = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            following.add(resultSet.getString("followed"));
        }
        stmt.close();
        user.setFollowing(following);
    }

    public void setSubscriptions(Connection connection, UserDataSet user) throws SQLException {
        final ArrayList<Integer> subscriptions = new ArrayList<>();

        final String query = "SELECT thread FROM subscribed WHERE user = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            subscriptions.add(resultSet.getInt("thread"));
        }
        stmt.close();
        user.setSubscriptions(subscriptions);
    }
}
