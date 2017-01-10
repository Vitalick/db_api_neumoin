package dao.impl;

import controllers.CustomResponse;
import dao.ForumDAO;
import dataSets.ForumDataSet;
import executor.PreparedExecutor;
import executor.TExecutor;
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
                        ///return details(object.get("short_name").getAsString(), null);
                        response.setCode(CustomResponse.ALREADY_EXIST);
                        return response;
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
        System.out.println(related);
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

    //TODO ворзврат объекта форума если уже существует
}
