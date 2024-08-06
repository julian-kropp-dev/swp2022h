package de.uol.swp.server.usermanagement;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.util.List;

/** An interface for all methods of the server user service. */
public interface ServerUserService {

  /**
   * Login with username and password.
   *
   * @param username the name of the user
   * @param password the password of the user
   * @return a new user object
   */
  User login(String username, String password);

  /**
   * Test, if given user is logged in.
   *
   * @param user the user to check for
   * @return true if the User is logged in
   */
  boolean isLoggedIn(User user);

  /**
   * Log out from server.
   *
   * @implNote the User Object has to contain a unique identifier in order to remove the correct
   *     user
   */
  void logout(User user);

  /**
   * Create a new persistent user.
   *
   * @implNote the User Object has to contain a unique identifier in order to remove the correct
   *     user
   * @param user The user to create
   * @return the new created user
   */
  User createUser(User user);

  /**
   * Removes a user from the sore.
   *
   * <p>Remove the User specified by the User object.
   *
   * @implNote the User Object has to contain a unique identifier in order to remove the correct
   *     user
   * @param user The user to remove
   */
  void dropUser(User user);

  /**
   * Update a user.
   *
   * <p>Updates the User specified by the User object.
   *
   * @implNote the User Object has to contain a unique identifier in order to update the correct
   *     user
   * @param user the user object containing all infos to update, if some values are not set, (e.g.
   *     password is "") these fields are not updated
   * @return the updated user object
   */
  User updateUser(User user, String password);

  /**
   * Update a username.
   *
   * @param user The old User to Update
   * @param name the new Username to Update
   * @param password the password from the user.
   * @return Return the new User Object
   */
  User updateUsername(User user, String name, String password);

  /**
   * Retrieve the list of all current logged-in users.
   *
   * @return a list of users
   */
  List<User> retrieveAllUsers();

  /**
   * Updates the statistics of a user based on the provided user statistic.
   *
   * @param user The user object to update the statistics for.
   * @param userStatistic The user statistic object containing the updated statistics.
   */
  void updateUserStatistics(User user, UserStatistic userStatistic);

  /**
   * Retrieves the list of user statistics for the given user.
   *
   * @param user The user object for which to retrieve the statistics.
   * @return A list of user statistics associated with the given user.
   */
  List<UserStatistic> retrieveUserStatistics(User user);
}
