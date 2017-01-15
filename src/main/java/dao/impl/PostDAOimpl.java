package dao.impl;

import controllers.CustomResponse;
import dao.PostDAO;
import dataSets.PostDataSet;
import main.Main;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"OverlyComplexBooleanExpression", "JDBCResourceOpenedButNotSafelyClosed", "OverlyBroadCatchBlock", "OverlyComplexMethod"})
public class PostDAOimpl implements PostDAO{
    final ObjectMapper mapper;

    public PostDAOimpl() {
        mapper = new ObjectMapper();
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE post;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            int count = 0;
            final Statement stmt = connection.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT posts FROM thread");
            while (resultSet.next()) {
                count += resultSet.getInt(1);
            }
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    @SuppressWarnings("MagicNumber")
    public CustomResponse create(String postString) {
        final PostDataSet post;

        try {
            final JsonNode json = mapper.readValue(postString, JsonNode.class);
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
            final String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES(?,?,?,?,?,?,?,?,?,?,?);";
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
            final ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                post.setId(generatedKeys.getInt(1));
            stmt.close();

            final String queryCountUserForum = "SELECT COUNT(*) FROM user_forum where user=? AND forum=?;";
            final PreparedStatement stmt1 = connection.prepareStatement(queryCountUserForum);
            stmt1.setString(1, (String)post.getUser());
            stmt1.setString(2, (String)post.getForum());

            final ResultSet rs = stmt1.executeQuery();
            rs.next();
            final int countUserForum = rs.getInt(1);
            stmt1.close();
            if (countUserForum == 0) {
                final String queryInsertUserForum = "INSERT INTO user_forum (user, forum) VALUES(?,?)";
                final PreparedStatement stmt2 = connection.prepareStatement(queryInsertUserForum);
                stmt2.setString(1, (String)post.getUser());
                stmt2.setString(2, (String)post.getForum());
                stmt2.execute();
                stmt2.close();
            }

            final byte code = (byte)post.getId();
            if (post.getParent() == null || post.getParent() == 0) {
                final String querySetPaths = "UPDATE post SET first_path=? WHERE id=?";
                stmt = connection.prepareStatement(querySetPaths);
                stmt.setInt(1, post.getId());
                stmt.setInt(2, post.getId());
                stmt.execute();
                stmt.close();
            } else {
                final String queryGetParent = "SELECT first_path,last_path FROM post WHERE id=?";
                stmt = connection.prepareStatement(queryGetParent);
                stmt.setObject(1, post.getParent());
                final ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
                final int parentFirstPath = resultSet.getInt("first_path");
                final String parentLastPath = resultSet.getString("last_path");
                stmt.close();

                final String querySetPaths = "UPDATE post SET first_path=?, last_path=? WHERE id=?";
                stmt = connection.prepareStatement(querySetPaths);
                stmt.setInt(1, parentFirstPath);
                if (parentLastPath != null) {
                    stmt.setString(2, parentLastPath + '.' + (char)code);
                } else {
                    stmt.setObject(2, (char)code, Types.CHAR);
                }
                stmt.setObject(3, post.getId());
                stmt.execute();
                stmt.close();
            }

            if (!post.getIsDeleted()) {
                final String queryThreadPosts = "UPDATE thread SET posts=posts+1 WHERE id=?";
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

    @Override
    public CustomResponse details(String postId, final List<String> related) {
        if (postId == null)
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        for (String str : related) {
            if (!str.equals("user") && !str.equals("forum") && !str.equals("thread")) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "SELECT * FROM post WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, postId);
            final ResultSet resultSet = stmt.executeQuery();
            final PostDataSet post;
            if (resultSet.next()) {
                post = new PostDataSet(resultSet);
            } else {
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
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

            return new CustomResponse(post, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public CustomResponse list(String forumShortName, String threadId, String since,
                               String limit, String order) {
        if ((forumShortName == null && threadId == null) || (forumShortName != null && threadId != null)
                || (order != null && !order.equals("asc") && !order.equals("desc"))) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            String forumQuery = "SELECT * FROM forum WHERE short_name=?;";
            String threadQuery = "SELECT * FROM thread WHERE id=?";
            final String existQuery = (forumShortName != null) ? forumQuery : threadQuery;
            final PreparedStatement existStmt = connection.prepareStatement(existQuery);
            existStmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            final ResultSet existResultSet = existStmt.executeQuery();

            if (!existResultSet.next()) {
                existStmt.close();
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            existStmt.close();

            forumQuery = "SELECT * FROM post WHERE forum=?";
            threadQuery = "SELECT * FROM post WHERE thread=?";
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append((forumShortName != null) ? forumQuery : threadQuery);
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<PostDataSet> posts = new ArrayList<>();
            while (resultSet.next()) {
                final PostDataSet post = new PostDataSet(resultSet);
                posts.add(post);
            }
            stmt.close();

            return new CustomResponse(posts, CustomResponse.OK);

        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);

        }
    }

    @Override
    public CustomResponse removeOrRestore(String postString, String action) {
        final JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String queryPost = "SELECT thread, isDeleted FROM post WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(queryPost);
            stmt.setInt(1, json.get("post").getIntValue());
            final ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                final int threadId = resultSet.getInt("thread");
                final boolean oldIsDeleted = resultSet.getBoolean("isDeleted");
                stmt.close();
                final String queryRemove = "UPDATE post SET isDeleted=? WHERE id=?";
                stmt = connection.prepareStatement(queryRemove);
                stmt.setInt(1, (action.equals("remove"))? 1 : 0);
                stmt.setInt(2, json.get("post").getIntValue());
                stmt.executeUpdate();

                if (!oldIsDeleted && action.equals("remove") || oldIsDeleted && action.equals("restore")) {
                    stmt.close();
                    final String queryThreadPostsDec = "UPDATE thread SET posts=posts-1 WHERE id=?";
                    final String queryThreadPostsInc = "UPDATE thread SET posts=posts+1 WHERE id=?";
                    final String queryThreadPosts = (action.equals("remove") ? queryThreadPostsDec : queryThreadPostsInc);
                    stmt = connection.prepareStatement(queryThreadPosts);
                    stmt.setInt(1, threadId);
                    stmt.executeUpdate();
                }
            }
            stmt.close();

            return new CustomResponse(json, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);

        }
    }

    @Override
    public CustomResponse update(String postString) {
        final JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }

        if (!json.has("post") || !json.has("message"))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);

        final String message = json.get("message").getTextValue();
        final int postId = json.get("post").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE post SET message=? WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, message);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(postId), new ArrayList<>());
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

        if (!json.has("vote") || !json.has("post") ||
                json.get("vote").getIntValue() != 1 && json.get("vote").getIntValue() != -1) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        final int vote = json.get("vote").getIntValue();
        final int postId = json.get("post").getIntValue();

        try (Connection connection = Main.connection.getConnection()) {
            final String queryLike = "UPDATE post SET likes=likes+1, points=points+1 WHERE id=?";
            final String queryDislike = "UPDATE post SET dislikes=dislikes+1, points=points-1 WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement((vote == 1) ? queryLike : queryDislike);
            stmt.setInt(1, postId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(postId), new ArrayList<>());
        } catch (SQLException | NullPointerException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }
}

