package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;

/**
 * Request to delete an existing user.
 *
 * @see User
 */
public class DeleteUserRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 1069699382561973506L;

  public DeleteUserRequest() {
    super();
  }
}
