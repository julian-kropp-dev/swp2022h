package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** This Class handles the Response when the server successfully update the avatar id of a user. */
public class UpdateAvatarResponse extends AbstractResponseMessage {
  private static final long serialVersionUID = 1177233649572115596L;
  private final UserDTO updatedUser;

  public UpdateAvatarResponse(UserDTO updatedUser) {
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
    UpdateAvatarResponse that = (UpdateAvatarResponse) o;
    return (Objects.equals(updatedUser, that.updatedUser));
  }

  @Override
  public int hashCode() {
    return Objects.hash(updatedUser);
  }
}
