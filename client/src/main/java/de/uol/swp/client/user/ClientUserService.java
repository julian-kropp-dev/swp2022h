package de.uol.swp.client.user;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.userdata.UserData;

/**
 * An interface for all methods of the client user service.
 *
 * <p>As the communication with the server is based on events, the returns of the call must be
 * handled by events
 */
public interface ClientUserService {

  /**
   * Login with username and password.
   *
   * @param username the name of the user
   * @param password the password of the user
   */
  void login(String username, String password);

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
   * @param username the username of the new User
   * @param password the password of the new User
   * @param email the email of the new User
   */
  void createUser(String username, String password, String email);

  /**
   * Removes a user from the store.
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
   * @param uuid the uuid of the user to update
   * @param username the username containing the info to update, if some values are not set, (e.g.
   *     password is "") these fields are not updated
   * @param newPassword the newPassword containing the info to update, if some values are not set,
   *     (e.g. * password is "") these fields are not updated
   * @param email the email containing the info to update, if some values are not set, (e.g. *
   *     password is "") these fields are not updated
   * @param oldPassword the old password from the user.
   */
  void updateUser(
      String uuid,
      String username,
      String newPassword,
      String email,
      String oldPassword,
      UserData userData);

  /**
   * Updates a Username of a user.
   *
   * @param user the old User to get the password later.
   * @param name the new Username to Change
   * @param password the password from the user.
   */
  void updateUsername(User user, String name, String password);

  /**
   * Updates the avatar id of a user.
   *
   * @param user the User to set the id later
   * @param avatarId the new avatar id
   */
  void updateAvatar(User user, int avatarId);

  /** Retrieve the list of all current logged-in users. */
  void retrieveAllOnlineUsers();

  /**
   * Retrieves user stats of a user.
   *
   * @param user the User to get the stats from.
   */
  void retrieveUserStats(User user);
}
