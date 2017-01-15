package dao.impl;

import controllers.CustomResponse;
import dao.ForumDAO;
import dataSets.ForumDataSet;
import dataSets.PostDataSet;
import dataSets.ThreadDataSet;
import dataSets.UserDataSet;
import main.Main;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"OverlyComplexBooleanExpression", "OverlyComplexMethod", "JDBCResourceOpenedButNotSafelyClosed", "OverlyBroadCatchBlock"})
public class ForumDAOimpl implements ForumDAO{
    private final ObjectMapper mapper;

    public ForumDAOimpl() {
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE forum;");
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
            final int count = stmt.executeQuery("SELECT COUNT(*) FROM forum;").getInt(1);
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public CustomResponse create(String forumString) {
        try {
            final JsonNode json = mapper.readValue(forumString, JsonNode.class);
            try {
                final ForumDataSet forum = new ForumDataSet(
                        json.get("name").getTextValue(),
                        json.get("short_name").getTextValue(),
                        json.get("user").getTextValue() );
                try (Connection connection = Main.connection.getConnection()) {
                    final String query = "INSERT INTO forum (name, short_name, user) VALUES(?,?,?)";
                    final PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, forum.getName());
                    stmt.setString(2, forum.getShort_name());
                    stmt.setString(3, (String)forum.getUser());
                    stmt.execute();
                    final ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next())
                        forum.setId(generatedKeys.getInt(1));
                    stmt.close();

                    return new CustomResponse(forum, CustomResponse.OK);
                } catch (SQLException e) {
                    //noinspection MagicNumber
                    if (e.getErrorCode() == 1062) {
                        return details(json.get("short_name").getTextValue(), new ArrayList<>());
                    } else {
                        return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
                    }
                }
            } catch (Exception e) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }
    }

    @Override
    public CustomResponse details(String forumShortName, final List<String> related) {
        if (forumShortName == null || (!related.isEmpty() && !related.get(0).equals("user"))) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "SELECT * FROM forum WHERE short_name=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1,forumShortName);
            final ResultSet resultSet = stmt.executeQuery();
            final ForumDataSet forum;
            if (resultSet.next()) {
                forum = new ForumDataSet(resultSet);
            } else {
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            if (related.contains("user")) {
                forum.setUser(new UserDAOimpl().details((String)forum.getUser()).getResponse());
            }
            stmt.close();

            return new CustomResponse(forum, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse listPosts(String forumShortName,
                                    final List<String> related,
                                    String since,
                                    String limit,
                                    String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("thread") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM post WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");


            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final List<PostDataSet> posts = new ArrayList<>();
            final ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                final PostDataSet post = new PostDataSet(resultSet);

                if (related.contains("forum"))
                    post.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("thread")) {
                    final Integer thread = (Integer)post.getThread();
                    post.setThread(new ThreadDAOimpl().details(thread.toString(),
                            new ArrayList<>()).getResponse());
                }
                if (related.contains("user")) {
                    final String user = (String)post.getUser();
                    post.setUser(new UserDAOimpl().details(user).getResponse());
                }

                posts.add(post);
            }
            stmt.close();

            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse listThreads(String forumShortName, final List<String> related, String since,
                                      String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for(String str : related) {
            if (!str.equals("forum") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM thread WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                final ThreadDataSet thread = new ThreadDataSet(resultSet);
                threads.add(thread);

                if (related.contains("forum"))
                    thread.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("user")) {
                    final String user = (String)thread.getUser();
                    thread.setUser(new UserDAOimpl().details(user).getResponse());
                }
            }
            stmt.close();

            return new CustomResponse(threads, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public CustomResponse listUsers(String forumShortName, String since_id, String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final List<UserDataSet> users = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT u.* FROM user u");
            queryBuilder.append(" JOIN user_forum uf ON u.email=uf.user");
            queryBuilder.append(" WHERE uf.forum = ?");
            if (since_id != null) queryBuilder.append(" AND u.id >= ?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int param = 2;
            if (since_id != null) stmt.setString(param++, since_id);
            if (limit != null) stmt.setInt(param, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                final UserDataSet user = new UserDataSet(resultSet);
                new UserDAOimpl().setFollowers(connection, user);
                new UserDAOimpl().setFollowing(connection, user);
                new UserDAOimpl().setSubscriptions(connection, user);
                if ( since_id == null || user.getId() >= new Integer(since_id) )
                    users.add(user);
            }

            stmt.close();
            return new CustomResponse(users, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }
}
