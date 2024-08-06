package de.uol.swp.server.usermanagement.store;

/**
 * Exception thrown in UserStore.
 *
 * <p>This exception is thrown if someone wants to register a with a username that is already taken
 * or someone tries to modify or remove a user that does not (yet) exist within the UserStore.
 *
 * @see UserStore
 */
class UserStoreException extends RuntimeException {

  private static final long serialVersionUID = -8507356424861316033L;

  /**
   * Constructor.
   *
   * @param s String containing the cause for the exception.
   */
  UserStoreException(String s) {
    super(s);
  }
}
