package dao;

import controllers.CustomResponse;

import java.util.List;


public interface ThreadDAO {
    void truncateTable();

    int count();

    CustomResponse create(String threadString);

    CustomResponse details(String threadId, List<String> related);
}
