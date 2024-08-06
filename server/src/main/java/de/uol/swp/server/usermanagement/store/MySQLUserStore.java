package de.uol.swp.server.usermanagement.store;

import com.google.common.base.Strings;
import com.mysql.cj.jdbc.MysqlDataSource;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.ServerConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The class connects to the database and queries sql. */
@SuppressWarnings({"java:S1192", "checkstyle:AbbreviationAsWordInName"})
public class MySQLUserStore extends AbstractUserStore implements UserStore {

  private static final Logger LOG = LogManager.getLogger(MySQLUserStore.class);
  private static final String SQL_ERROR = "SQL error: ";
  private MysqlDataSource dataSource = null;
  private Connection conn;

  /** Create a Connection to the Database. */
  public MySQLUserStore() {
    this(ServerConfig.getInstance());
  }

  /** Create a Connection to the Database by using serverConfig mock. */
  public MySQLUserStore(ServerConfig serverConfig) {
    String mySQLServer = serverConfig.getMySQLServerAddress();
    String mySQLPort = serverConfig.getMySQLServerPort();
    String mySQLDatabase = serverConfig.getMySQLServerDatabase();
    String mySQLUsername = serverConfig.getMySQLServerUsername();
    String mySQLPassword = serverConfig.getMySQLServerPassword();

    try {
      dataSource = new MysqlDataSource();
      dataSource.setUser(mySQLUsername);
      dataSource.setPassword(mySQLPassword);
      dataSource.setDatabaseName(mySQLDatabase);
      dataSource.setServerName(mySQLServer);
      dataSource.setPort(Integer.parseInt(mySQLPort));
      checkDatabase();

      LOG.info("Connected to Database: {}.", mySQLDatabase);
    } catch (NumberFormatException | SQLException e) {
      LOG.fatal("{}", e.getMessage());
    }
  }

  /** Searches for the user in the database, that matches the given username and password. */
  @Override
  public Optional<User> findUser(String username, String password) {
    LOG.debug("Database: execute findUser (username, password)");

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT UserID, Username, Password, Mail, AvatarId "
                + "FROM User WHERE Username = ? && Password = ?")) {
      checkDatabase();
      stmt.setString(1, username);
      stmt.setString(2, hash(password));
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(
            new UserDTO(
                    rs.getString("UserID"),
                    rs.getString("Username"),
                    "",
                    rs.getString("Mail"),
                    new UserData(rs.getInt("AvatarId")))
                .getWithoutPassword());
      }
      rs.close();
      return Optional.empty();
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  /**
   * Searches for the userID, username, mail and avatarID in the database, that matches the given
   * userID.
   */
  @Override
  public Optional<User> findUser(String uuid) {
    LOG.debug("Database: execute findUser (uuid)");

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT UserID, Username, Mail, AvatarId FROM User WHERE UserID = ?")) {
      checkDatabase();
      stmt.setString(1, uuid);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(
            new UserDTO(
                    rs.getString("UserID"),
                    rs.getString("Username"),
                    "",
                    rs.getString("Mail"),
                    new UserData(rs.getInt("AvatarId")))
                .getWithoutPassword());
      }
      rs.close();
      return Optional.empty();
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  /** Searches for the userID in the database, that matches the given username. */
  @Override
  public String findUUID(String username) {
    LOG.debug("Database: execute findUUID (username)");

    try (PreparedStatement stmt =
        conn.prepareStatement("SELECT UserID FROM User WHERE Username = ?")) {
      checkDatabase();
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getString("UserID");
      }
      rs.close();
      return null;
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  /** Tries to insert a new user with userID, username, mail and avatarID into the database. */
  @Override
  @SuppressWarnings("java:S1226")
  public User createUser(
      String uuid, String username, String password, String mail, UserData userData) {
    LOG.debug("Database: execute createUser (username, password, mail)");

    if (Strings.isNullOrEmpty(username)) {
      throw new IllegalArgumentException("Username must not be null");
    }

    if (Strings.isNullOrEmpty(password)) {
      throw new IllegalArgumentException("Password must not be null");
    }

    if (Strings.isNullOrEmpty(mail)) {
      throw new IllegalArgumentException("The email address must not be empty");
    }

    if (!mail.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
      throw new IllegalArgumentException("The entered email address is invalid");
    }

    if (Objects.isNull(userData)) {
      throw new IllegalArgumentException("UserData must not be null");
    }

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO User (Username, Password, Mail, AvatarID) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      checkDatabase();
      stmt.setString(1, username);
      stmt.setString(2, hash(password));
      stmt.setString(3, mail);
      stmt.setInt(4, userData.getAvatarId());
      stmt.executeUpdate();
      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          uuid = generatedKeys.getString(1);
        } else {
          LOG.fatal(SQL_ERROR + "Creating user failed, no ID obtained.");
          throw new UserStoreException(SQL_ERROR + "Creating user failed, no ID obtained.");
        }
      }
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
    return new UserDTO(uuid, username, hash(password), mail, userData);
  }

  @Override
  public User updateUser(
      String uuid, String username, String password, String mail, UserData userData) {
    LOG.debug("Database: execute updateUser (username, password, mail)");

    if (findUser(findUUID(username)).isEmpty()) {
      throw new IllegalArgumentException("User does not exist!");
    }

    if (Strings.isNullOrEmpty(username)) {
      throw new IllegalArgumentException("Username must not be null");
    }

    if (Strings.isNullOrEmpty(password)) {
      throw new IllegalArgumentException("Password must not be null");
    }

    if (Strings.isNullOrEmpty(mail)) {
      throw new IllegalArgumentException("The email address must not be empty");
    }

    if (!mail.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
      throw new IllegalArgumentException("The entered email address is invalid");
    }

    if (Objects.isNull(userData)) {
      throw new IllegalArgumentException("UserData must not be null");
    }

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "UPDATE User SET Password = ?, Mail = ?, AvatarId = ? WHERE UserID = ?")) {
      checkDatabase();
      stmt.setString(1, hash(password));
      stmt.setString(2, mail);
      stmt.setInt(3, userData.getAvatarId());
      stmt.setString(4, uuid);
      stmt.executeUpdate();
      return new UserDTO(uuid, username, hash(password), mail, userData);
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public User updateUser(User user, String newUsername) {
    LOG.debug("Database: execute updateUser");

    if (findUser(user.getUUID()).isEmpty()) {
      throw new IllegalArgumentException("User does not exist!");
    }

    try (PreparedStatement stmt =
            conn.prepareStatement(
                "SELECT UserID, Password, Mail, AvatarId FROM User WHERE UserID = ?");
        PreparedStatement stmtUpdate =
            conn.prepareStatement("UPDATE User SET Username = ? WHERE UserID = ?")) {
      checkDatabase();
      stmt.setString(1, user.getUUID());
      stmtUpdate.setString(1, newUsername);
      stmtUpdate.setString(2, user.getUUID());
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        String uuid = rs.getString("UserID");
        String password = rs.getString("Password");
        String mail = rs.getString("Mail");
        int avatarId = rs.getInt("AvatarId");
        stmtUpdate.executeUpdate();
        return new UserDTO(uuid, newUsername, password, mail, new UserData(avatarId));
      }
      rs.close();
      return null;
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public void removeUser(String uuid) {
    LOG.debug("Database: execute removeUser");
    if (findUser(uuid).isEmpty()) {
      throw new IllegalArgumentException("User does not exist!");
    }
    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM User WHERE UserID = ?")) {
      checkDatabase();
      stmt.setString(1, uuid);
      stmt.executeUpdate();
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public List<User> getAllUsers() {
    LOG.debug("Database: execute getAllUsers");

    try (Statement stmt = conn.createStatement()) {
      checkDatabase();
      ResultSet rs = stmt.executeQuery("SELECT UserID, Username, Mail, AvatarId FROM User");
      List<User> retUsers = new ArrayList<>();
      while (rs.next()) {
        retUsers.add(
            new UserDTO(
                    rs.getString("UserID"),
                    rs.getString("Username"),
                    "",
                    rs.getString("Mail"),
                    new UserData(rs.getInt("AvatarId")))
                .getWithoutPassword());
      }
      rs.close();
      return retUsers;
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public User setUserAvatarId(User user, int avatarId) {
    LOG.debug("Database: execute setUserAvatarId");

    try (PreparedStatement stmtUpdate =
        conn.prepareStatement("UPDATE User SET AvatarId = ? WHERE UserID = ?")) {
      checkDatabase();
      stmtUpdate.setInt(1, avatarId);
      stmtUpdate.setString(2, user.getUUID());
      stmtUpdate.executeUpdate();
      return new UserDTO(
          user.getUUID(),
          user.getUsername(),
          "",
          user.getMail(),
          new UserData(avatarId, user.getUserData().getUserStatistics()));
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  @SuppressWarnings("lineLength")
  public User getUserData(User user) {
    LOG.debug("Database: execute getUserData");

    if (findUser(findUUID(user.getUsername())).isEmpty()) {
      throw new IllegalArgumentException("User does not exist!");
    }
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT placement, DateTime, AvatarId From User LEFT JOIN Statistic S on User.UserID = S.UserID WHERE User.UserID = ?")) {
      checkDatabase();
      stmt.setString(1, user.getUUID());
      ResultSet rs = stmt.executeQuery();
      List<UserStatistic> userStatistics = new ArrayList<>();
      Timestamp timestamp;
      int placement;
      Integer avatarId = null;
      while (rs.next()) {
        avatarId = rs.getInt("AvatarId");
        timestamp = rs.getTimestamp("DateTime");
        placement = rs.getInt("placement");
        if (!(placement == 0 || timestamp == null)) {
          userStatistics.add(new UserStatistic(placement, timestamp.toLocalDateTime()));
        }
      }
      rs.close();
      if (avatarId != null) {
        return new UserDTO(
            user.getUUID(),
            user.getUsername(),
            "",
            user.getMail(),
            new UserData(avatarId, userStatistics));
      }
      return null;
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public User addUserStatistic(User user, UserStatistic userStatistic) {
    LOG.info("Database: execute addUserStatistic");

    try (PreparedStatement stmt = conn.prepareStatement("call addOrUpdateStatistic(?, ?, ?)")) {
      checkDatabase();
      stmt.setString(1, user.getUsername());
      stmt.setInt(2, userStatistic.getPlacement());
      stmt.setTimestamp(3, Timestamp.valueOf(userStatistic.getDateTime()));
      stmt.executeUpdate();

      return getUserData(user);
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  @Override
  public User removeUserStatistics(User user) {
    LOG.info("Database: execute removeUserStatistics");

    try (PreparedStatement stmt =
        conn.prepareStatement(
            "DELETE FROM Statistic WHERE UserID = (SELECT UserID FROM User WHERE UserID = ?)")) {
      checkDatabase();
      stmt.setString(1, user.getUUID());
      stmt.executeUpdate();

      return getUserData(user);
    } catch (SQLException e) {
      LOG.fatal("{}{}", SQL_ERROR, e.getMessage());
      throw new UserStoreException(SQL_ERROR + e.getMessage());
    }
  }

  private void checkDatabase() throws SQLException {
    if (conn == null || conn.isClosed()) {
      conn = dataSource.getConnection();
    }
  }
}
