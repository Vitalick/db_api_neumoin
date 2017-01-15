package dao.impl;

import controllers.CustomResponse;
import dao.ThreadDAO;
import dataSets.PostDataSet;
import dataSets.ThreadDataSet;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"OverlyComplexBooleanExpression", "OverlyBroadCatchBlock", "OverlyComplexMethod", "JDBCResourceOpenedButNotSafelyClosed"})
public class ThreadDAOimpl implements ThreadDAO {
    final ObjectMapper mapper;

    public ThreadDAOimpl() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE thread;");
            stmt.execute("TRUNCATE TABLE subscribed;");
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
            final ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM thread;");
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
    public CustomResponse create(String threadString) {
        final ThreadDataSet thread;

        try {
            final JsonNode json = mapper.readValue(threadString, JsonNode.class);
            if (!json.has("forum") || !json.has("title") || !json.has("isClosed") || !json.has("user")
                    || !json.has("date") || !json.has("message") || !json.has("slug")) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
            try {
                thread = new ThreadDataSet(json);

            } catch (Exception e) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "INSERT INTO thread (title, date, slug, message, forum, user, isClosed, isDeleted) VALUES(?,?,?,?,?,?,?,?);";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, thread.getTitle());
            stmt.setString(2, thread.getDate());
            stmt.setString(3, thread.getSlug());
            stmt.setString(4, thread.getMessage());
            stmt.setString(5, (String)thread.getForum());
            stmt.setString(6, (String)thread.getUser());
            stmt.setBoolean(7, thread.getIsClosed());
            stmt.setBoolean(8, thread.getIsDeleted());
            stmt.execute();
            final ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                thread.setId(generatedKeys.getInt(1));
            stmt.close();

            return new CustomResponse(thread, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse details(String threadId, final List<String> related) {
        if (threadId == null) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }

        for (String str : related) {
            if (!str.equals("user") && !str.equals("forum")) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "SELECT * FROM thread WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, threadId);
            final ResultSet resultSet = stmt.executeQuery();
            final ThreadDataSet thread;
            if (resultSet.next()) {
                thread = new ThreadDataSet(resultSet);
            } else {
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            if (related.contains("user")) {
                thread.setUser(new UserDAOimpl().details((String)thread.getUser()).getResponse());
            }
            if (related.contains("forum")) {
                thread.setForum(new ForumDAOimpl().details((String)thread.getForum(), new ArrayList<>()).getResponse());
            }
            stmt.close();

            return new CustomResponse(thread, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse close(String threadString) {
        final JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE thread SET isClosed=1 WHERE id = ?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse open(String threadString) {
        final JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE thread SET isClosed=0 WHERE id = ?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse list(String forumShortName, String email, String since, String limit, String order) {
        if ((forumShortName == null && email == null) || (forumShortName != null && email != null)
                || (order != null && !order.equals("asc") && !order.equals("desc"))) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            String forumQuery = "SELECT * FROM forum WHERE short_name=?;";
            String userQuery = "SELECT * FROM user WHERE email=?";
            final String existQuery = (forumShortName != null) ? forumQuery : userQuery;
            final PreparedStatement existStmt = connection.prepareStatement(existQuery);
            existStmt.setString(1, (forumShortName != null) ? forumShortName : email);
            final ResultSet existResultSet = existStmt.executeQuery();

            if (!existResultSet.next()) {
                existStmt.close();
                new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            existStmt.close();

            forumQuery = "SELECT * FROM thread WHERE forum=?";
            userQuery = "SELECT * FROM thread WHERE user=?";
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append((forumShortName != null) ? forumQuery : userQuery);
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, (forumShortName != null) ? forumShortName : email);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                final ThreadDataSet thread = new ThreadDataSet(resultSet);
                threads.add(thread);
            }
            stmt.close();

            return new CustomResponse(threads, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse remove(String threadString) {
        final JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE thread SET isDeleted=1, posts=0 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            final String queryDeletePosts = "UPDATE post SET isDeleted=1 WHERE thread = ?";
            stmt = connection.prepareStatement(queryDeletePosts);
            stmt.setInt(1, threadId);
            stmt.execute();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse restore(String threadString) {
        final JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE thread SET isDeleted=0, posts=(SELECT COUNT(*) FROM post WHERE thread=?) WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.setInt(2, threadId);
            stmt.executeUpdate();
            stmt.close();

            final String queryRestorePosts = "UPDATE post SET isDeleted=0 WHERE thread = ?";
            stmt = connection.prepareStatement(queryRestorePosts);
            stmt.setInt(1, threadId);
            stmt.execute();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse subscribe(String subscribeString) {
        final JsonNode json;
        try {
            json = mapper.readValue(subscribeString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread") || !json.has("user"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int thread = json.get("thread").getIntValue();
        final String user = json.get("user").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "INSERT IGNORE INTO subscribed (user, thread) VALUES (?,?)";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setInt(2, thread);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse unsubscribe(String unsubscribeString) {
        final JsonNode json;
        try {
            json = mapper.readValue(unsubscribeString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread") || !json.has("user"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int thread = json.get("thread").getIntValue();
        final String user = json.get("user").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "DELETE FROM subscribed WHERE user = ? AND thread = ?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setInt(2, thread);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse update(String threadString) {
        final JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("message") || !json.has("slug") || !json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final int threadId = json.get("thread").getIntValue();
        final String slug = json.get("slug").getTextValue();
        final String message = json.get("message").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE thread SET slug=?, message=? WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, slug);
            stmt.setString(2, message);
            stmt.setInt(3, threadId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(threadId), new ArrayList<>());
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse vote(String voteString) {
        final JsonNode json;
        try {
            json = mapper.readValue(voteString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("vote") || !json.has("thread") ||
                json.get("vote").getIntValue() != 1 && json.get("vote").getIntValue() != -1) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        final int vote = json.get("vote").getIntValue();
        final int threadId = json.get("thread").getIntValue();

        try (Connection connection = Main.connection.getConnection()) {
            final String queryLike = "UPDATE thread SET likes=likes+1, points=points+1 WHERE id=?";
            final String queryDislike = "UPDATE thread SET dislikes=dislikes+1, points=points-1 WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement((vote == 1) ? queryLike : queryDislike);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(threadId), new ArrayList<>());
        } catch (SQLException | NullPointerException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse listPosts(String threadId, String sort, String since, String limit, String order) {
        if (threadId == null || (order != null && !order.equals("asc") && !order.equals("desc"))
                || (sort != null && !sort.equals("flat")) && !sort.equals("tree") && !sort.equals("parent_tree"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;
        sort = (sort == null) ? "flat" : sort;

        try (Connection connection = Main.connection.getConnection()) {
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM post");
            queryBuilder.append(" WHERE thread = ?");
            if (since != null) queryBuilder.append(" AND date >= ?");

            if (sort.equals("flat")) {
                queryBuilder.append(" ORDER BY date");
                if (order.equals("desc")) queryBuilder.append(" DESC");
            } else {
                queryBuilder.append(" ORDER BY first_path");
                if (order.equals("desc")) queryBuilder.append(" DESC");
                queryBuilder.append(", last_path");
            }

            if (limit != null && !sort.equals("parent_tree")) {
                    queryBuilder.append(" LIMIT ?");
            }

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, threadId);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null && !sort.equals("parent_tree")) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<PostDataSet> posts = new ArrayList<>();

            int parents = 0;
            int lastParent = -1;
            while (resultSet.next()) {
                final PostDataSet post = new PostDataSet(resultSet);
                if (sort.equals("parent_tree") && limit != null) {
                    if (post.getFirstPathValue()!=lastParent) {
                        parents++;
                        lastParent = post.getFirstPathValue();
                    }
                    if (parents == new Integer(limit) + 1) break;
                }
                posts.add(post);
            }
            stmt.close();

            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }
}

