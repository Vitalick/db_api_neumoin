package dataSets;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserDataSet {
    private final int id;
    private String email;
    private final String username;
    private final String name;
    private final String about;
    private final boolean isAnonymous;
    private final ArrayList<String> followers;
    private final ArrayList<String> following;
    private final ArrayList<String> subscriptions;

    public UserDataSet(int id, String email, String username, String name, String about,
                       boolean isAnonymous, ArrayList<String> followers,
                       ArrayList<String> following, ArrayList<String> subscriptions) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.about = about;
        this.isAnonymous = isAnonymous;
        this.followers = followers;
        this.following = following;
        this.subscriptions = subscriptions;
    }

    public UserDataSet(int id, String email, String username, String name, String about,
                       boolean isAnonymous) {
        this(id, email, username, name, about, isAnonymous, new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>());

    }

    public UserDataSet(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("email"),
                resultSet.getString("name"),
                resultSet.getString("username"),
                resultSet.getString("about"),
                resultSet.getBoolean("isAnonymous")
        );
    }

    public UserDataSet() {
        this.id = 1;
        this.email = "sda";
        this.username = "asd";
        this.name = "asd";
        this.about = "qwd";
        this.isAnonymous = false;
        this.followers = null;
        this.following = null;
        this.subscriptions = null;
    }

    public String getEmail() { return email; }
    public void setEmail(@NotNull String email) { this.email = email; }
}
