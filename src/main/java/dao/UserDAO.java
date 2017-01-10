package dao;

import controllers.CustomResponse;


public interface UserDAO {
    void truncateTable();

    int count();

    CustomResponse details(String email);

    CustomResponse create(String userString);

    CustomResponse follow(String followString);

    CustomResponse unfollow(String followString);

    CustomResponse listFollowers(String email, String since_id, String limit, String order);

    CustomResponse listFollowing(String email, String since_id, String limit, String order);

    CustomResponse listPosts(String email, String since, String limit, String order);

    CustomResponse updateProfile(String userString);
}
