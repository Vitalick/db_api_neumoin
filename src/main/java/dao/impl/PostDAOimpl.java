package dao.impl;

import controllers.CustomResponse;
import dao.PostDAO;
import dataSets.PostDataSet;
import executor.TExecutor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PostDAOimpl implements PostDAO{
    Connection connection;
    ObjectMapper mapper;

    public PostDAOimpl(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE post;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
            int count = TExecutor.execQuery(connection, "SELECT COUNT(*) FROM post WHERE isDeleted=0;", resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public CustomResponse create(String postString) {
        CustomResponse response = new CustomResponse();
        PostDataSet post;

        try {
            JsonNode json = mapper.readValue(postString, JsonNode.class);
            if (!json.has("date") || !json.has("thread") || !json.has("message")
                    || !json.has("user") || !json.has("forum")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
            try {
                post = new PostDataSet(json);

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
            System.out.println(post.getMessage());
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

            response.setResponse(post);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
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
        try {
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
                post.setUser(new UserDAOimpl(connection).details((String)post.getUser()).getResponse());
            }
            if (related.contains("thread")) {
                post.setThread(new ThreadDAOimpl(connection).details(post.getThread().toString(), new ArrayList<>()).getResponse());
            }
            if (related.contains("forum")) {
                post.setForum(new ForumDAOimpl(connection).details((String)post.getForum(), new ArrayList<>()).getResponse());
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

        try {
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
            if (order != null) {
                queryBuilder.append(" ORDER BY date");
                if (order.equals("desc")) queryBuilder.append(" DESC");
            }
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

        try {
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

        int vote = json.get("vote").getIntValue();
        if ( vote != 1 && vote != -1) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }
        try {
            String query = "UPDATE "

        } catch (SQLException e) {

        }
    }
}
