package de.uol.swp.server.usermanagement.store;

import com.google.common.base.Strings;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is a user store.
 *
 * <p>This is the user store that is used for the start of the software project. The user accounts
 * in this user store only reside within the RAM of your computer and only for as long as the server
 * is running. Therefore, the users have to be added every time the server is started.
 *
 * @implNote This store will never return the password of a user!
 * @see de.uol.swp.server.usermanagement.store.AbstractUserStore
 * @see de.uol.swp.server.usermanagement.store.UserStore
 */
public class MainMemoryBasedUserStore extends AbstractUserStore implements UserStore {

  private static final String ARGUMENT = "User does not exist!";
  private final Map<String, User> users = new HashMap<>();

  @Override
  public Optional<User> findUser(String username, String password) {
    User usr = users.get(username);
    if (usr != null && Objects.equals(usr.getPassword(), hash(password))) {
      return Optional.of(usr.getWithoutPassword());
    }
    return Optional.empty();
  }

  @Override
  public Optional<User> findUser(String username) {
    User usr = users.get(username);
    if (usr != null) {
      return Optional.of(usr.getWithoutPassword());
    }
    return Optional.empty();
  }

  @Override
  public User createUser(
      String uuid, String username, String password, String mail, UserData userData) {
    if (Strings.isNullOrEmpty(username)) {
      throw new IllegalArgumentException("Username must not be null");
    } else if (Strings.isNullOrEmpty(mail)) {
      throw new IllegalArgumentException("The email address must not be empty");
    } else if (!mail.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")) {
      throw new IllegalArgumentException("The entered email address is invalid");
    } else if (Objects.isNull(userData)) {
      throw new IllegalArgumentException("UserData must not be null");
    }
    if (Strings.isNullOrEmpty(uuid)) {
      uuid = UUID.randomUUID().toString();
    }
    User usr = new UserDTO(uuid, username, hash(password), mail, userData);
    users.put(username, usr);
    return usr;
  }

  @Override
  public User updateUser(
      String uuid, String username, String password, String mail, UserData userData) {
    if (users.get(username) == null) {
      throw new IllegalArgumentException("This User does not exist!");
    }
    return createUser(uuid, username, password, mail, userData);
  }

  @Override
  public User updateUser(User user, String newUsername) {
    String password = users.get(user.getUsername()).getPassword();
    users.remove(user.getUsername());
    users.put(
        newUsername,
        new UserDTO(user.getUUID(), newUsername, password, user.getMail(), user.getUserData()));
    return users.get(newUsername);
  }

  @Override
  public void removeUser(String username) {
    if (users.get(username) == null) {
      throw new IllegalArgumentException(ARGUMENT);
    }
    users.remove(username);
  }

  @Override
  public List<User> getAllUsers() {
    List<User> retUsers = new ArrayList<>();
    users.values().forEach(u -> retUsers.add(u.getWithoutPassword()));
    return retUsers;
  }

  @Override
  public User getUserData(User user) {
    Optional<User> user1 = findUser(user.getUsername());
    if (user1.isEmpty()) {
      throw new IllegalArgumentException(ARGUMENT);
    }
    return user1.get();
  }

  @Override
  public User setUserAvatarId(User user, int avatarId) {
    if (!users.containsKey(user.getUsername())) {
      throw new IllegalArgumentException(ARGUMENT);
    } else if (avatarId < 0) {
      throw new IllegalArgumentException("Invalid avatarId!");
    }
    String password = users.get(user.getUsername()).getPassword();
    User retUsr =
        new UserDTO(
            user.getUUID(),
            user.getUsername(),
            password,
            user.getMail(),
            new UserData(avatarId, user.getUserData().getUserStatistics()));
    users.put(user.getUsername(), retUsr);
    return retUsr;
  }

  @Override
  @SuppressWarnings("java:S3655")
  public User addUserStatistic(User user, UserStatistic userStatistic) {
    List<UserStatistic> retStats = user.getUserData().getUserStatistics();
    if (retStats.size() < 10) {
      retStats.add(userStatistic);
    } else {
      LocalDateTime minTime =
          retStats.stream().map(UserStatistic::getDateTime).min(LocalDateTime::compareTo).get();
      retStats.remove(
          retStats.stream()
              .filter(x -> x.getDateTime() == minTime)
              .limit(1)
              .collect(Collectors.toList())
              .get(0));
      retStats.add(userStatistic);
    }
    User retUsr =
        new UserDTO(
            user.getUUID(),
            user.getUsername(),
            "",
            user.getMail(),
            new UserData(user.getUserData().getAvatarId(), retStats));
    users.replace(user.getUsername(), retUsr);
    return retUsr;
  }

  @Override
  public User removeUserStatistics(User user) {
    User retUsr =
        new UserDTO(
            user.getUUID(),
            user.getUsername(),
            "",
            user.getMail(),
            new UserData(user.getUserData().getAvatarId()));
    users.replace(user.getUsername(), retUsr);
    return retUsr;
  }

  /**
   * find a user from the uuid.
   *
   * @param username the username of the searched UUID
   * @return the username because it needs to be implemented
   */
  @Override
  public String findUUID(String username) {
    return username;
  }
}
