package dao;

import controllers.CustomResponse;

import java.util.List;


public interface ThreadDAO {
    void truncateTable();

    int count();

    CustomResponse create(String threadString);

    CustomResponse details(String threadId, List<String> related);

    CustomResponse close(String threadString);

    CustomResponse open(String threadString);

    CustomResponse list(String forumShortName, String email, String since, String limit, String order);

    CustomResponse remove(String threadString);

    CustomResponse restore(String threadString);

    CustomResponse subscribe(String subscribeString);

    CustomResponse unsubscribe(String unsubscribeString);

    CustomResponse update(String threadString);

    CustomResponse vote(String voteString);

    CustomResponse listPosts(String threadId, String sort, String since, String limit, String order);
}
