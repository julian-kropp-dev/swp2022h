package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for UpdateUserResponse. */
public class UpdateUserResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -4480650843458679083L;
  private final UserDTO updatedUser;

  public UpdateUserResponse(UserDTO updatedUser) {
    this.updatedUser = updatedUser;
  }

  public UserDTO getUpdatedUser() {
    return updatedUser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    UpdateUserResponse that = (UpdateUserResponse) o;
    return Objects.equals(updatedUser, that.updatedUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), updatedUser);
  }
}
