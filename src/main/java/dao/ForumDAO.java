package dao;

import controllers.CustomResponse;

import java.util.ArrayList;
import java.util.List;


public interface ForumDAO {
    void truncateTable();

    int count();

    CustomResponse create(String forumString);

    CustomResponse details(String forumShortName, final List<String> related);
}
