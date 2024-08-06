package de.uol.swp.client.user;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.common.user.Hashing;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.request.DeleteUserRequest;
import de.uol.swp.common.user.request.LoginRequest;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest;
import de.uol.swp.common.user.request.RetrieveUserStatsRequest;
import de.uol.swp.common.user.request.UpdateAvatarRequest;
import de.uol.swp.common.user.request.UpdateUserRequest;
import de.uol.swp.common.user.request.UpdateUsernameRequest;
import de.uol.swp.common.user.userdata.UserData;

/**
 * This class is used to hide the communication details implements de.uol.common.user.UserService
 *
 * @see ClientUserService
 */
@SuppressWarnings("UnstableApiUsage")
public class UserService implements ClientUserService {

  private final EventBus bus;

  /**
   * Constructor.
   *
   * @param bus The EventBus set in ClientModule
   * @see de.uol.swp.client.di.ClientModule
   */
  @Inject
  public UserService(EventBus bus) {
    this.bus = bus;
  }

  /**
   * Posts a login request to the EventBus.
   *
   * @param username the name of the user
   * @param password the password of the user
   */
  @Override
  public void login(String username, String password) {
    LoginRequest msg = new LoginRequest(username, Hashing.hashing(password));
    bus.post(msg);
  }

  @Override
  public void logout(User username) {
    LogoutRequest msg = new LogoutRequest();
    bus.post(msg);
  }

  @Override
  public void createUser(String username, String password, String email) {
    RegisterUserRequest request =
        new RegisterUserRequest(
            new UserDTO(null, username, Hashing.hashing(password), email, new UserData(1)));
    bus.post(request);
  }

  /**
   * Method to delete a users account.
   *
   * <p>This method sends a DeleteUserRequest to delete a users account.
   *
   * @param user The user to remove
   */
  public void dropUser(User user) {
    DeleteUserRequest request = new DeleteUserRequest();
    bus.post(request);
  }

  @Override
  public void updateUser(
      String uuid,
      String username,
      String newPassword,
      String email,
      String oldPassword,
      UserData userData) {
    UpdateUserRequest request =
        new UpdateUserRequest(
            new UserDTO(uuid, username, Hashing.hashing(newPassword), email, userData),
            Hashing.hashing(oldPassword));
    bus.post(request);
  }

  @Override
  public void updateUsername(User user, String name, String password) {
    UpdateUsernameRequest request =
        new UpdateUsernameRequest(user, name, Hashing.hashing(password));
    bus.post(request);
  }

  @Override
  public void updateAvatar(User user, int avatarId) {
    UpdateAvatarRequest request = new UpdateAvatarRequest(user, avatarId);
    bus.post(request);
  }

  @Override
  public void retrieveAllOnlineUsers() {
    RetrieveAllOnlineUsersRequest cmd = new RetrieveAllOnlineUsersRequest();
    bus.post(cmd);
  }

  @Override
  public void retrieveUserStats(User user) {
    RetrieveUserStatsRequest request = new RetrieveUserStatsRequest(user);
    bus.post(request);
  }
}
