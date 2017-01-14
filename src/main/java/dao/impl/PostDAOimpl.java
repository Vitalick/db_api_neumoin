package dao.impl;

import controllers.CustomResponse;
import dao.PostDAO;
import dataSets.PostDataSet;
import executor.TExecutor;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PostDAOimpl implements PostDAO{
    ObjectMapper mapper;

    public PostDAOimpl() {
        mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE post;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            int count = 0;
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT posts FROM thread");
            while (resultSet.next()) {
                count += resultSet.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //TODO проверка на isDeleted в базе
    public CustomResponse create(String postString) {
        PostDataSet post;

        try {
            JsonNode json = mapper.readValue(postString, JsonNode.class);
            if (!json.has("date") || !json.has("thread") || !json.has("message")
                    || !json.has("user") || !json.has("forum")) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
            try {
                post = new PostDataSet(json);
            } catch (Exception e) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES(?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, post.getDate());
            stmt.setInt(2, (Integer)post.getThread());
            stmt.setString(3, post.getMessage());
            stmt.setString(4, (String)post.getUser());
            stmt.setString(5, (String)post.getForum());
            stmt.setObject(6, post.getParent());
            stmt.setBoolean(7, post.getIsApproved());
            stmt.setBoolean(8, post.getIsHighlighted());
            stmt.setBoolean(9, post.getIsEdited());
            stmt.setBoolean(10, post.getIsSpam());
            stmt.setBoolean(11, post.getIsDeleted());
            stmt.execute();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                post.setId(generatedKeys.getInt(1));
            stmt.close();

            byte code = (byte)post.getId();
            if (post.getParent() == null || post.getParent() == 0) {
                String querySetPaths = "UPDATE post SET first_path=? WHERE id=?";
                stmt = connection.prepareStatement(querySetPaths);
                stmt.setInt(1, post.getId());
                stmt.setInt(2, post.getId());
                stmt.execute();
                stmt.close();
            } else {
                String queryGetParent = "SELECT first_path,last_path FROM post WHERE id=?";
                stmt = connection.prepareStatement(queryGetParent);
                stmt.setObject(1, post.getParent());
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
                int parentFirstPath = resultSet.getInt("first_path");
                String parentLastPath = resultSet.getString("last_path");
                stmt.close();

                String querySetPaths = "UPDATE post SET first_path=?, last_path=? WHERE id=?";
                stmt = connection.prepareStatement(querySetPaths);
                stmt.setInt(1, parentFirstPath);
                if (parentLastPath != null) {
                    stmt.setString(2, parentLastPath + '.' + (char)code);
                } else {
                    stmt.setObject(2, (char)code, java.sql.Types.CHAR);
                }
                stmt.setObject(3, post.getId());
                stmt.execute();
                stmt.close();
            }

            if (post.getIsDeleted() == false) {
                String queryThreadPosts = "UPDATE thread SET posts=posts+1 WHERE id=?";
                stmt = connection.prepareStatement(queryThreadPosts);
                stmt.setInt(1, (Integer) post.getThread());
                stmt.execute();
                stmt.close();
            }

            return new CustomResponse(post, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse details(String postId, final List<String> related) {
        CustomResponse response = new CustomResponse();

        if (postId == null) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }

        for (String str : related) {
            if (!str.equals("user") && !str.equals("forum") & !str.equals("thread")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
        }

        PostDataSet post;
        try (Connection connection = Main.connection.getConnection()) {
            String query = "SELECT * FROM post WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, postId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                post = new PostDataSet(resultSet);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }
            if (related.contains("user")) {
                post.setUser(new UserDAOimpl().details((String)post.getUser()).getResponse());
            }
            if (related.contains("thread")) {
                post.setThread(new ThreadDAOimpl().details(post.getThread().toString(), new ArrayList<>()).getResponse());
            }
            if (related.contains("forum")) {
                post.setForum(new ForumDAOimpl().details((String)post.getForum(), new ArrayList<>()).getResponse());
            }
            stmt.close();

            response.setResponse(post);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse list(String forumShortName, String threadId, String since,
                               String limit, String order) {
        CustomResponse response = new CustomResponse();

        if ((forumShortName == null && threadId == null) || (forumShortName != null && threadId != null)
                || (order != null && !order.equals("asc") && !order.equals("desc"))) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            String forumQuery = "SELECT * FROM forum WHERE short_name=?;";
            String threadQuery = "SELECT * FROM thread WHERE id=?";
            String existQuery = (forumShortName != null) ? forumQuery : threadQuery;
            PreparedStatement existStmt = connection.prepareStatement(existQuery);
            existStmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            ResultSet existResultSet = existStmt.executeQuery();

            if (!existResultSet.next()) {
                existStmt.close();
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }
            existStmt.close();

            forumQuery = "SELECT * FROM post WHERE forum=?";
            threadQuery = "SELECT * FROM post WHERE thread=?";
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append((forumShortName != null) ? forumQuery : threadQuery);
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<PostDataSet> posts = new ArrayList<>();
            while (resultSet.next()) {
                PostDataSet post = new PostDataSet(resultSet);
                posts.add(post);
            }
            stmt.close();

            response.setResponse(posts);
            response.setCode(CustomResponse.OK);
            return response;

        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse removeOrRestore(String postString, String action) {
        CustomResponse response = new CustomResponse();

        JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            response.setResponse("INVALID REQUEST");
            response.setCode(CustomResponse.INVALID_REQUEST);
            return response;
        }

        try (Connection connection = Main.connection.getConnection()) {
            String queryPost = "SELECT thread, isDeleted FROM post WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(queryPost);
            stmt.setInt(1, json.get("post").getIntValue());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int threadId = resultSet.getInt("thread");
                boolean oldIsDeleted = resultSet.getBoolean("isDeleted");
                stmt.close();
                String queryRemove = "UPDATE post SET isDeleted=? WHERE id=?";
                stmt = connection.prepareStatement(queryRemove);
                stmt.setInt(1, (action.equals("remove"))? 1 : 0);
                stmt.setInt(2, json.get("post").getIntValue());
                stmt.executeUpdate();

                if (!oldIsDeleted && action.equals("remove") || oldIsDeleted && action.equals("restore")) {
                    stmt.close();
                    String queryThreadPostsDec = "UPDATE thread SET posts=posts-1 WHERE id=?";
                    String queryThreadPostsInc = "UPDATE thread SET posts=posts+1 WHERE id=?";
                    String queryThreadPosts = (action.equals("remove") ? queryThreadPostsDec : queryThreadPostsInc);
                    stmt = connection.prepareStatement(queryThreadPosts);
                    stmt.setInt(1, threadId);
                    stmt.executeUpdate();
                }
            }
            stmt.close();

            response.setResponse(json);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse update(String postString) {
        JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("post") || !json.has("message"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        String message = json.get("message").getTextValue();
        int postId = json.get("post").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            String query = "UPDATE post SET message=? WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, message);
            stmt.setInt(2, postId);
            stmt.executeUpdate();

            return details(new Integer(postId).toString(), new ArrayList<>());
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse vote(String voteString) {
        CustomResponse response = new CustomResponse();

        JsonNode json;
        try {
            json = mapper.readValue(voteString, JsonNode.class);
        } catch (IOException e) {
            response.setResponse("INVALID REQUEST");
            response.setCode(CustomResponse.INVALID_REQUEST);
            return response;
        }

        if (!json.has("vote") || !json.has("post") ||
                json.get("vote").getIntValue() != 1 && json.get("vote").getIntValue() != -1) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        int vote = json.get("vote").getIntValue();
        int postId = json.get("post").getIntValue();

        try (Connection connection = Main.connection.getConnection()) {
            String queryLike = "UPDATE post SET likes=likes+1, points=points+1 WHERE id=?";
            String queryDislike = "UPDATE post SET dislikes=dislikes+1, points=points-1 WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement((vote == 1) ? queryLike : queryDislike);
            stmt.setInt(1, postId);
            stmt.executeUpdate();
            stmt.close();

            return details(new Integer(postId).toString(), new ArrayList<>());
        } catch (SQLException | NullPointerException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }
}

