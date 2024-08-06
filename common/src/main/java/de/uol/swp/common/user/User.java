package de.uol.swp.common.user;

import de.uol.swp.common.user.userdata.UserData;
import java.io.Serializable;

/**
 * Interface for different kinds of user objects.
 *
 * <p>This interface is for unifying different kinds of user objects throughout the project. With
 * this being the base project it is currently only used for the UserDTO objects.
 *
 * @see de.uol.swp.common.user.UserDTO
 */
public interface User extends Serializable, Comparable<User> {

  /**
   * Getter for the uuid variable.
   *
   * @return uuid of the user as String
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  String getUUID();

  /**
   * Getter for the username variable.
   *
   * @return username of the user as String
   */
  String getUsername();

  /**
   * Getter for the password variable.
   *
   * @return password of the user as String
   */
  String getPassword();

  /**
   * Getter for the email variable.
   *
   * @return email address of the user as String
   */
  String getMail();

  /**
   * Creates a duplicate of this object leaving its password empty.
   *
   * @return Copy of this with empty password field
   */
  User getWithoutPassword();

  /**
   * Getter for the UserData.
   *
   * @return userdata of the user as UserData
   */
  UserData getUserData();
}
