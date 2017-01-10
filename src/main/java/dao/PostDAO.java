package dao;

import controllers.CustomResponse;

import java.util.List;


public interface PostDAO {
    void truncateTable();

    int count();

    CustomResponse create(String postString);

    CustomResponse details(String postId, List<String> related);

    CustomResponse list(String forumShortName, String threadId, String since,
                        String limit, String order);

    CustomResponse removeOrRestore(String postString, String action);

    CustomResponse update(String postString);

    CustomResponse vote(String voteString);
}
