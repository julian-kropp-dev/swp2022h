package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * Request to update an username.
 *
 * @see de.uol.swp.common.user.User
 */
public class UpdateAvatarRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 6971210969044815206L;
  private final User toUpdate;
  private final int avatarId;

  /**
   * Constructor.
   *
   * @param user the current user to send to the Server
   * @param avatarId The new avatarId to send to the Server
   */
  public UpdateAvatarRequest(User user, int avatarId) {
    this.toUpdate = user;
    this.avatarId = avatarId;
  }

  /**
   * Getter for the updated user object.
   *
   * @return the updated user object
   */
  public User getUser() {
    return toUpdate;
  }

  /**
   * Getter for the Username.
   *
   * @return the new Username
   */
  public int getUpdateAvatar() {
    return avatarId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateAvatarRequest that = (UpdateAvatarRequest) o;
    return (Objects.equals(toUpdate, that.toUpdate) && (Objects.equals(avatarId, that.avatarId)));
  }

  @Override
  public int hashCode() {
    return Objects.hash(toUpdate);
  }
}
