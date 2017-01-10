package dao.impl;

import controllers.CustomResponse;
import dao.ForumDAO;
import dataSets.ForumDataSet;
import dataSets.PostDataSet;
import dataSets.ThreadDataSet;
import dataSets.UserDataSet;
import executor.PreparedExecutor;
import executor.TExecutor;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//FIX PLACE WITH DETAILS

public class ForumDAOimpl implements ForumDAO{
    private Connection connection;
    private ObjectMapper mapper;

    public ForumDAOimpl(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE forum;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
            int count = TExecutor.execQuery(connection, "SELECT COUNT(*) FROM forum;", resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public CustomResponse create(String forumString) {
        CustomResponse response = new CustomResponse();
        try {
            JsonNode json = mapper.readValue(forumString, JsonNode.class);
            try {
                ForumDataSet forum = new ForumDataSet(json.get("name").getTextValue(),
                        json.get("short_name").getTextValue(), json.get("user").getTextValue() );
                try {
                    String query = "INSERT INTO forum (name, short_name, user) VALUES(?,?,?)";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, forum.getName());
                    System.out.println(json.get("short_name").getTextValue());
                    stmt.setString(2, forum.getShort_name());
                    stmt.setString(3, (String)forum.getUser());
                    stmt.execute();
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next())
                        forum.setId(generatedKeys.getInt(1));

                    response.setResponse(forum);
                    response.setCode(CustomResponse.OK);
                    return response;
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1062) {
                        return details(json.get("short_name").getTextValue(), new ArrayList<>());
                    } else {
                        response.setResponse("UNKNOWN ERROR");
                        response.setCode(CustomResponse.UNKNOWN_ERROR);
                        return response;
                    }
                }
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
    }

    public CustomResponse details(String forumShortName, final List<String> related) {
        CustomResponse response = new CustomResponse();
        if (forumShortName == null || (!related.isEmpty() && !related.get(0).equals("user"))) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }

        ForumDataSet forum;
        try {
            String query = "SELECT * FROM forum WHERE short_name=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1,forumShortName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                forum = new ForumDataSet(resultSet);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }
            if (related.contains("user")) {
                forum.setUser(new UserDAOimpl(connection).details((String)forum.getUser()).getResponse());
            }
            stmt.close();

            response.setResponse(forum);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }

    public CustomResponse listPosts(String forumShortName, final List<String> related, String since,
                             String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for(String str : related) {
            if (!str.equals("forum") && !str.equals("thread") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM post WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<PostDataSet> posts = new ArrayList<>();
            while (resultSet.next()) {
                PostDataSet post = new PostDataSet(resultSet);
                posts.add(post);

                if (related.contains("forum"))
                    post.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("thread")) {
                    Integer thread = (Integer)post.getThread();
                    post.setThread(new ThreadDAOimpl(connection).details(thread.toString(),
                            new ArrayList<>()).getResponse());
                }
                if (related.contains("user")) {
                    String user = (String)post.getUser();
                    post.setUser(new UserDAOimpl(connection).details(user).getResponse());
                }
            }
            stmt.close();

            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listThreads(String forumShortName, final List<String> related, String since,
                                    String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for(String str : related) {
            if (!str.equals("forum") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM thread WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                ThreadDataSet thread = new ThreadDataSet(resultSet);
                threads.add(thread);

                if (related.contains("forum"))
                    thread.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("user")) {
                    String user = (String)thread.getUser();
                    thread.setUser(new UserDAOimpl(connection).details(user).getResponse());
                }
            }
            stmt.close();

            return new CustomResponse(threads, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listUsers(String forumShortName, String since_id, String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try {
            List<UserDataSet> users = new ArrayList<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT u.* FROM post p");
            queryBuilder.append(" JOIN forum f ON p.forum=f.short_name");
            queryBuilder.append(" JOIN user u ON p.user=u.email");
            queryBuilder.append(" WHERE p.forum = ?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String email = resultSet.getString("email");
                UserDataSet user = (UserDataSet) new UserDAOimpl(connection).details(email).getResponse();
                if ( since_id == null || since_id != null && user.getId() >= new Integer(since_id) )
                    users.add(user);
            }

            stmt.close();
            return new CustomResponse(users, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    //TODO ворзврат объекта форума если уже существует
    //TODO проверить везде дефолт у сортировки
    //TODO проверить все на не найдено
}
