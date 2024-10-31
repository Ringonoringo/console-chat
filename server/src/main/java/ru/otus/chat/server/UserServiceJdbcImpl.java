package ru.otus.chat.server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
public class UserServiceJdbcImpl {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";

    private static final String USERS_QUERY = "SELECT * from users";

    private static final String USER_ROLES_QUERY = "select r.id as \"id\", r.name as \"name\" \n" +
            "from roles r\n" +
            "join users_to_roles utr\n" +
            "on r.id = utr.role_id\n" +
            "where utr.user_id = ?";

    private Connection connection;

    public UserServiceJdbcImpl() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL, "postgres", "postgres");
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try(Statement statement = connection.createStatement()) {
            try(ResultSet resultSet = statement.executeQuery(USERS_QUERY)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String password = resultSet.getString(2);
                    String email = resultSet.getString(3);
                    User user = new User(id, password, email);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        setRolesToUsers(users);
        return users;
    }

    private void setRolesToUsers(List<User> users) {
        try(PreparedStatement preparedStatement = connection.prepareStatement(USER_ROLES_QUERY)) {
            for (User user : users) {
                preparedStatement.setInt(1, user.getId());
                List<Role> roles = new ArrayList<>();
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString(2);
                        Role role = new Role(id, name);
                        roles.add(role);
                    }
                }
                user.setRoles(roles);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAdmin(int userId) {
        boolean result = getAll().stream().anyMatch(user -> user.getId() == userId);
        return result;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
