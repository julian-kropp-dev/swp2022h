package de.uol.swp.server.usermanagement;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.usermanagement.store.UserStore;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles most user related issues e.g. login/logout
 *
 * @see de.uol.swp.server.usermanagement.AbstractUserManagement
 */
@Singleton
public class UserManagement extends AbstractUserManagement {

  private static final Logger LOG = LogManager.getLogger(UserManagement.class);
  private final UserStore userStore;
  private final SortedMap<String, User> loggedInUsers = new TreeMap<>();

  /**
   * Constructor.
   *
   * @param userStore object of the UserStore to be used
   * @see de.uol.swp.server.usermanagement.store.UserStore
   */
  @Inject
  public UserManagement(UserStore userStore) {
    LOG.debug("UserManagement created: {}", this);
    this.userStore = userStore;
  }

  @Override
  public User login(String username, String password) throws SecurityException {
    if (loggedInUsers.containsKey(username)) {
      throw new SecurityException(
          "Der Spieler " + username + " ist bereits an einem anderen Client angemeldet.");
    }
    Optional<User> user = userStore.findUser(username, password);
    if (user.isPresent()) {
      this.loggedInUsers.put(username, user.get());
      return user.get();
    } else {
      throw new SecurityException(
          "Einloggen von Nutzer nicht möglich.\n(Username oder Passwort falsch)");
    }
  }

  @Override
  public boolean isLoggedIn(User username) {
    return loggedInUsers.containsKey(username.getUsername());
  }

  @Override
  public User createUser(User userToCreate) {
    Optional<User> user = userStore.findUser(userStore.findUUID(userToCreate.getUsername()));
    if (user.isPresent()) {
      throw new UserManagementException("A user with this username already exists!");
    }
    return userStore.createUser(
        userToCreate.getUUID(),
        userToCreate.getUsername(),
        userToCreate.getPassword(),
        userToCreate.getMail(),
        userToCreate.getUserData());
  }

  @Override
  public User updateUser(User userToUpdate, String password) {
    Optional<User> user = userStore.findUser(userStore.findUUID(userToUpdate.getUsername()));
    if (user.isEmpty()) {
      throw new UserManagementException("There is no user with this name!");
    }
    user = userStore.findUser(userToUpdate.getUsername(), password);
    if (user.isEmpty()) {
      throw new UserManagementException("The password is wrong!");
    }
    // Only update if there are new values
    String newPassword = firstNotNull(userToUpdate.getPassword(), user.get().getPassword());
    String newMail = firstNotNull(userToUpdate.getMail(), user.get().getMail());
    return userStore.updateUser(
        userToUpdate.getUUID(),
        userToUpdate.getUsername(),
        newPassword,
        newMail,
        userToUpdate.getUserData());
  }

  /**
   * Method for updating a Username.
   *
   * @param userToUpdate The old User to Update
   * @param username the new Username to Update
   * @param password the password from the user
   * @return the new User
   */
  @Override
  public User updateUsername(User userToUpdate, String username, String password) {
    if (userToUpdate == null) {
      return null;
    } else {
      if (userStore.findUser(userStore.findUUID(userToUpdate.getUsername())).isEmpty()) {
        throw new UserManagementException("Unknown user!");
      }

      if (userStore.findUser(userToUpdate.getUsername(), password).isEmpty()) {
        throw new UserManagementException("Password is not correct!");
      }

      if (username.equals("")) {
        throw new UserManagementException("Input is incorrect!");
      }

      if (userStore.findUser(userStore.findUUID(username)).isPresent()) {
        throw new UserManagementException("The username is already taken!");
      }

      return userStore.updateUser(userToUpdate, username);
    }
  }

  /**
   * Method for updating a Username.
   *
   * @param userToUpdate The old User to Update
   * @param avatarId the new avatarId to Update
   * @return the new User
   */
  @Override
  public User updateAvatar(User userToUpdate, int avatarId) {
    if (userToUpdate == null) {
      return null;
    } else {
      if (userStore.findUser(userStore.findUUID(userToUpdate.getUsername())).isEmpty()) {
        throw new UserManagementException("Unknown user!");
      }
      if (avatarId < 0 || avatarId > 8) {
        throw new UserManagementException("Input is incorrect!");
      }
      return userStore.setUserAvatarId(userToUpdate, avatarId);
    }
  }

  @Override
  public void dropUser(User userToDrop) {
    Optional<User> user = userStore.findUser(userStore.findUUID(userToDrop.getUsername()));
    if (user.isEmpty()) {
      throw new UserManagementException("There is no user with this name!");
    }
    userStore.removeUser(userStore.findUUID(userToDrop.getUsername()));
    if (isLoggedIn(userToDrop)) {
      loggedInUsers.remove(userToDrop.getUsername());
    }
  }

  /**
   * Updates the statistics of a user based on the provided user statistic.
   *
   * @param userToUpdate The user object to update the statistics for.
   * @param userStatistic The user statistic object containing the updated statistics.
   * @throws UserManagementException If no user with the given username is found.
   */
  @Override
  public void updateUserStatistics(User userToUpdate, UserStatistic userStatistic) {
    Optional<User> user = userStore.findUser(userStore.findUUID(userToUpdate.getUsername()));
    if (user.isEmpty()) {
      throw new UserManagementException(
          "Could not update UserStatistics. There is no user with this name!");
    }
    userStore.addUserStatistic(userToUpdate, userStatistic);
  }

  /**
   * Retrieves the list of user statistics for the given user.
   *
   * @param user The user object for which to retrieve the statistics.
   * @return A list of user statistics associated with the given user.
   * @throws UserManagementException If no user with the given username is found.
   */
  @Override
  public List<UserStatistic> retrieveUserStatistics(User user) {
    Optional<User> optionalUser = userStore.findUser(userStore.findUUID(user.getUsername()));
    if (optionalUser.isEmpty()) {
      throw new UserManagementException(
          "Could not get UserStatistics. There is no optionalUser with this name!");
    }
    return userStore.getUserData(user).getUserData().getUserStatistics();
  }

  /**
   * Sub-function of update user
   *
   * <p>This method is used to set the new user values to the old ones if the values in the update
   * request were empty.
   *
   * @param firstValue value to update to, empty String or null
   * @param secondValue the old value
   * @return String containing the value to be used in the update command
   */
  private String firstNotNull(String firstValue, String secondValue) {
    return Strings.isNullOrEmpty(firstValue) ? secondValue : firstValue;
  }

  @Override
  public void logout(User user) {
    loggedInUsers.remove(user.getUsername());
  }

  @Override
  public List<User> retrieveAllUsers() {
    return userStore.getAllUsers();
  }
}
