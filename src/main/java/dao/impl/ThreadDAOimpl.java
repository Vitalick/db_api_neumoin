package dao.impl;

import controllers.CustomResponse;
import dao.ThreadDAO;
import dataSets.PostDataSet;
import dataSets.ThreadDataSet;
import executor.TExecutor;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.internal.inject.Custom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ThreadDAOimpl implements ThreadDAO {
    ObjectMapper mapper;

    public ThreadDAOimpl() {
        this.mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE thread;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE subscribed;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
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

    public CustomResponse create(String threadString) {
        CustomResponse response = new CustomResponse();
        ThreadDataSet thread;

        try {
            JsonNode json = mapper.readValue(threadString, JsonNode.class);
            if (!json.has("forum") || !json.has("title") || !json.has("isClosed") || !json.has("user")
                    || !json.has("date") || !json.has("message") || !json.has("slug")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
            try {
                thread = new ThreadDataSet(json);

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
            String query = "INSERT INTO thread (title, date, slug, message, forum, user, isClosed, isDeleted) VALUES(?,?,?,?,?,?,?,?);";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, thread.getTitle());
            stmt.setString(2, thread.getDate());
            stmt.setString(3, thread.getSlug());
            stmt.setString(4, thread.getMessage());
            stmt.setString(5, (String)thread.getForum());
            stmt.setString(6, (String)thread.getUser());
            stmt.setBoolean(7, thread.getIsClosed());
            stmt.setBoolean(8, thread.getIsDeleted());
            stmt.execute();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                thread.setId(generatedKeys.getInt(1));
            stmt.close();

            return new CustomResponse(thread, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse details(String threadId, final List<String> related) {
        CustomResponse response = new CustomResponse();

        if (threadId == null) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }

        for (String str : related) {
            if (!str.equals("user") && !str.equals("forum")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
        }

        ThreadDataSet thread;
        try (Connection connection = Main.connection.getConnection()) {
            String query = "SELECT * FROM thread WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, threadId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                thread = new ThreadDataSet(resultSet);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }
            if (related.contains("user")) {
                thread.setUser(new UserDAOimpl().details((String)thread.getUser()).getResponse());
            }
            if (related.contains("forum")) {
                thread.setForum(new ForumDAOimpl().details((String)thread.getForum(), new ArrayList<String>()).getResponse());
            }
            stmt.close();

            response.setResponse(thread);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse close(String threadString) {
        JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE thread SET isClosed=1 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse open(String threadString) {
        JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE thread SET isClosed=0 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse list(String forumShortName, String email, String since, String limit, String order) {
        if ((forumShortName == null && email == null) || (forumShortName != null && email != null)
                || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            String forumQuery = "SELECT * FROM forum WHERE short_name=?;";
            String userQuery = "SELECT * FROM user WHERE email=?";
            String existQuery = (forumShortName != null) ? forumQuery : userQuery;
            PreparedStatement existStmt = connection.prepareStatement(existQuery);
            existStmt.setString(1, (forumShortName != null) ? forumShortName : email);
            ResultSet existResultSet = existStmt.executeQuery();

            if (!existResultSet.next()) {
                existStmt.close();
                new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            existStmt.close();

            forumQuery = "SELECT * FROM thread WHERE forum=?";
            userQuery = "SELECT * FROM thread WHERE user=?";
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append((forumShortName != null) ? forumQuery : userQuery);
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, (forumShortName != null) ? forumShortName : email);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                ThreadDataSet thread = new ThreadDataSet(resultSet);
                threads.add(thread);
            }
            stmt.close();

            return new CustomResponse(threads, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse remove(String threadString) {
        JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE thread SET isDeleted=1, posts=0 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            String queryDeletePosts = "UPDATE post SET isDeleted=1 WHERE thread = ?";
            stmt = connection.prepareStatement(queryDeletePosts);
            stmt.setInt(1, threadId);
            stmt.execute();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse restore(String threadString) {
        JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int threadId = json.get("thread").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE thread SET isDeleted=0, posts=(SELECT COUNT(*) FROM post WHERE thread=?) WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, threadId);
            stmt.setInt(2, threadId);
            stmt.executeUpdate();
            stmt.close();

            String queryRestorePosts = "UPDATE post SET isDeleted=0 WHERE thread = ?";
            stmt = connection.prepareStatement(queryRestorePosts);
            stmt.setInt(1, threadId);
            stmt.execute();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse subscribe(String subscribeString) {
        JsonNode json;
        try {
            json = mapper.readValue(subscribeString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread") || !json.has("user"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int thread = json.get("thread").getIntValue();
        String user = json.get("user").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "INSERT IGNORE INTO subscribed (user, thread) VALUES (?,?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setInt(2, thread);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse unsubscribe(String unsubscribeString) {
        JsonNode json;
        try {
            json = mapper.readValue(unsubscribeString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("thread") || !json.has("user"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int thread = json.get("thread").getIntValue();
        String user = json.get("user").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "DELETE FROM subscribed WHERE user = ? AND thread = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setInt(2, thread);
            stmt.executeUpdate();
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse update(String threadString) {
        JsonNode json;
        try {
            json = mapper.readValue(threadString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("message") || !json.has("slug") || !json.has("thread"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        int threadId = json.get("thread").getIntValue();
        String slug = json.get("slug").getTextValue();
        String message = json.get("message").getTextValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE thread SET slug=?, message=? WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, slug);
            stmt.setString(2, message);
            stmt.setInt(3, threadId);
            stmt.executeUpdate();
            stmt.close();

            return details(new Integer(threadId).toString(), new ArrayList<>());
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse vote(String voteString) {
        JsonNode json;
        try {
            json = mapper.readValue(voteString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("vote") || !json.has("thread") ||
                json.get("vote").getIntValue() != 1 && json.get("vote").getIntValue() != -1) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        int vote = json.get("vote").getIntValue();
        int threadId = json.get("thread").getIntValue();

        try (Connection connection = Main.connection.getConnection()) {
            String queryLike = "UPDATE thread SET likes=likes+1, points=points+1 WHERE id=?";
            String queryDislike = "UPDATE thread SET dislikes=dislikes+1, points=points-1 WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement((vote == 1) ? queryLike : queryDislike);
            stmt.setInt(1, threadId);
            stmt.executeUpdate();
            stmt.close();

            return details(new Integer(threadId).toString(), new ArrayList<>());
        } catch (SQLException | NullPointerException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listPosts(String threadId, String sort, String since, String limit, String order) {
        if (threadId == null || (order != null && !order.equals("asc") && !order.equals("desc"))
                || (sort != null && !sort.equals("flat")) && !sort.equals("tree") && !sort.equals("parent_tree"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;
        sort = (sort == null) ? "flat" : sort;

        try (Connection connection = Main.connection.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder();
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

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, threadId);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null && !sort.equals("parent_tree")) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<PostDataSet> posts = new ArrayList<>();

            int parents = 0;
            int lastParent = -1;
            while (resultSet.next()) {
                PostDataSet post = new PostDataSet(resultSet);
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

//TODO delete posts with thread

