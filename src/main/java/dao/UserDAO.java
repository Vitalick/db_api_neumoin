package dao;

import controllers.CustomResponse;


public interface UserDAO {
    void truncateTable();

    int count();

    CustomResponse details(String email);

    CustomResponse create(String userString);
}
