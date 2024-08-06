package de.uol.swp.client;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.LogoutSuccessfulResponse;
import de.uol.swp.common.user.response.UpdateAvatarResponse;
import de.uol.swp.common.user.response.UpdateUserResponse;
import de.uol.swp.common.user.response.UpdateUsernameResponse;

/** The class saves and updates the logged-in user. */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class LoggedInUser {
  private UserDTO user;

  @Inject
  public LoggedInUser(EventBus eventBus) {
    eventBus.register(this);
  }

  /**
   * This method saves the user when he has logged in.
   *
   * @param msg Is the message from the server
   */
  @Subscribe
  public void onLoginSuccessfulResponse(LoginSuccessfulResponse msg) {
    this.user = (UserDTO) msg.getUser();
  }

  /**
   * This method deletes the saved user when he logs out.
   *
   * @param msg Is the message from the server
   */
  @Subscribe
  public void onLogoutSuccessfulResponse(LogoutSuccessfulResponse msg) {
    this.user = null;
  }

  /**
   * This method updates the user when their name is revised.
   *
   * @param msg Is the message from the server
   */
  @Subscribe
  public void onUpdateUserNameResponse(UpdateUsernameResponse msg) {
    this.user = msg.getUpdatedUser();
  }

  /**
   * This method will update the user when it is updated.
   *
   * @param msg Is the message from the server
   */
  @Subscribe
  public void onUpdateUserResponse(UpdateUserResponse msg) {
    this.user = msg.getUpdatedUser();
  }

  /**
   * This method will update the user when it is updated.
   *
   * @param msg Is the message from the server
   */
  @Subscribe
  public void onUpdateAvatarResponse(UpdateAvatarResponse msg) {
    this.user = msg.getUpdatedUser();
  }

  /**
   * Method for returning a user object. This method returns the user as an object. If no user is
   * logged in, it returns null.
   *
   * @return the user as UserDTO
   */
  public UserDTO getUser() {
    return user;
  }

  /**
   * The method is intended for testing.
   *
   * @param user is the test user
   */
  public void setUser(UserDTO user) {
    this.user = user;
  }

  /**
   * This methode returns the username of the logged-in user. If no user is logged in, it returns
   * "Nicht angemeldet" so that there are no runtime error messages when initializing the individual
   * components. "Nicht angemeldet" is not a valid username.
   *
   * @return the username as a String.
   */
  public String getUsername() {
    if (user == null) {
      return "Nicht angemeldet";
    }
    return user.getUsername();
  }
}
