package de.uol.swp.server.usermanagement.store;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.util.List;
import java.util.Optional;

/** Interface to unify different kinds of UserStores in order to be able to exchange them easily. */
public interface UserStore {

  /**
   * Find a user by username and password.
   *
   * @param username username of the user to find
   * @param password password of the user to find
   * @return The User without password information, if found
   */
  Optional<User> findUser(String username, String password);

  /**
   * Find a user only by name.
   *
   * @param uuid ID of the user to find
   * @return The User without password information, if found
   */
  Optional<User> findUser(String uuid);

  /**
   * Update user. Update only given fields. Username cannot be changed
   *
   * @param username username of the user to be modified
   * @param password new password
   * @param mail new email address
   * @return The User without password information
   */
  User createUser(String uuid, String username, String password, String mail, UserData userData);

  User updateUser(String uuid, String username, String password, String mail, UserData userData);

  /**
   * Update only the Username.
   *
   * @param user the User to Update the Username
   * @param newUsername the new Username to update in User
   * @return new User
   */
  User updateUser(User user, String newUsername);

  /**
   * Remove user from store.
   *
   * @param uuid the ID of the user to remove
   */
  void removeUser(String uuid);

  /**
   * Retrieves the list of all users.
   *
   * @return A list of all users without password information
   */
  List<User> getAllUsers();

  /**
   * Get UserData from UserStore in a User.
   *
   * @param user the user to add the data
   * @return The User with new UserData
   */
  User getUserData(User user);

  /**
   * Set the AvatarId to the UserStore.
   *
   * @param user the user to set the avatarId
   * @param avatarId the avatarId to set to the user
   * @return The User with the AvatarId
   */
  User setUserAvatarId(User user, int avatarId);

  /**
   * Add UserStatistics to store.
   *
   * @param user the user to add statistic
   * @param userStatistic the statistic to add to the user
   * @return The User with new Statistics
   */
  User addUserStatistic(User user, UserStatistic userStatistic);

  /**
   * Remove UserStatistics from store.
   *
   * @param user the user with the UserStatistics to remove
   * @return The User with empty UserData
   */
  User removeUserStatistics(User user);

  /**
   * Find UUID in store.
   *
   * @param username the username of the searched UUID
   * @return The UUID of the User
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  String findUUID(String username);
}
