package de.uol.swp.common.user;

/**
 * Interface for different kinds of user objects.
 *
 * <p>This interface is for unifying different kinds of user objects throughout the project. With
 * this being the base project it is currently only used for the UUIDSession objects within the
 * server.
 */
public interface Session {

  /**
   * Getter for the SessionID.
   *
   * @return ID of the session as String
   */
  String getSessionId();

  /**
   * Getter for the user that uses the session.
   *
   * @return the user of the session as object implementing user
   * @see de.uol.swp.common.user.User
   */
  User getUser();
}
