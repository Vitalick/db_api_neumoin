package dao.impl;

import controllers.CustomResponse;
import dao.ThreadDAO;
import dataSets.ThreadDataSet;
import executor.TExecutor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ThreadDAOimpl implements ThreadDAO {
    Connection connection;
    ObjectMapper mapper;

    public ThreadDAOimpl(Connection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE thread;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE subscribed;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
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

        try {
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

            response.setResponse(thread);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
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
        try {
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
                thread.setUser(new UserDAOimpl(connection).details((String)thread.getUser()).getResponse());
            }
            if (related.contains("forum")) {
                thread.setForum(new ForumDAOimpl(connection).details((String)thread.getForum(), new ArrayList<String>()).getResponse());
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
}

