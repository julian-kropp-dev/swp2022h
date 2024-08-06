package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Class for UpdateUsernameResponse. */
public class UpdateUsernameResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 1177233649512115596L;
  private final UserDTO updatedUser;

  public UpdateUsernameResponse(UserDTO updatedUser) {
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
    UpdateUsernameResponse that = (UpdateUsernameResponse) o;
    return Objects.equals(updatedUser, that.updatedUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), updatedUser);
  }
}
