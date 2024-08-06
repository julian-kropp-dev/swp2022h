package de.uol.swp.server.usermanagement;

/**
 * Exception thrown in UserManagement.
 *
 * <p>This exception is thrown if someone wants to register a with a username that is already taken
 * or someone tries to modify or remove a user that does not (yet) exist within the UserStore.
 *
 * @see de.uol.swp.server.usermanagement.UserManagement
 */
class UserManagementException extends RuntimeException {

  private static final long serialVersionUID = -5657881148659505370L;

  /**
   * Constructor.
   *
   * @param s String containing the cause for the exception.
   */
  UserManagementException(String s) {
    super(s);
  }
}
