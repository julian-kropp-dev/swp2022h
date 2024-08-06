package de.uol.swp.server.usermanagement;

import de.uol.swp.common.user.User;

abstract class AbstractUserManagement implements ServerUserService {
  public abstract User updateAvatar(User userToUpdate, int avatarid);
}
