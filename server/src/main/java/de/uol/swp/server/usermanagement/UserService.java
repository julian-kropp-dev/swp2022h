package de.uol.swp.server.usermanagement;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.exception.RegistrationExceptionMessage;
import de.uol.swp.common.user.exception.UpdateUserExceptionMessage;
import de.uol.swp.common.user.exception.UpdateUserStatsExceptionMessage;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.RetrieveUserStatsRequest;
import de.uol.swp.common.user.request.UpdateAvatarRequest;
import de.uol.swp.common.user.request.UpdateUserRequest;
import de.uol.swp.common.user.request.UpdateUsernameRequest;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import de.uol.swp.common.user.response.UpdateAvatarResponse;
import de.uol.swp.common.user.response.UpdateUserResponse;
import de.uol.swp.common.user.response.UpdateUserStatsResponse;
import de.uol.swp.common.user.response.UpdateUsernameResponse;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.message.UpdateUserStatsMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapping of event bus calls to user management calls.
 *
 * @see de.uol.swp.server.AbstractService
 */
@Singleton
@SuppressWarnings({"java:S1192", "UnstableApiUsage"})
public class UserService extends AbstractService {

  private static final Logger LOG = LogManager.getLogger(UserService.class);
  private final UserManagement userManagement;

  /**
   * Constructor.
   *
   * @param eventBus the EventBus used throughout the entire server (injected)
   * @param userManagement object of the UserManagement to use
   * @see de.uol.swp.server.usermanagement.UserManagement
   */
  @Inject
  public UserService(EventBus eventBus, UserManagement userManagement) {
    super(eventBus);
    LOG.debug("UserService created: {}", this);
    this.userManagement = userManagement;
  }

  /**
   * Handles RegisterUserRequests found on the EventBus
   *
   * <p>If a RegisterUserRequest is detected on the EventBus, this method is called. It tries to
   * create a new user via the UserManagement. If this succeeds a RegistrationSuccessfulResponse is
   * posted on the EventBus otherwise a RegistrationExceptionMessage gets posted there.
   *
   * @param msg The RegisterUserRequest found on the EventBus
   * @see de.uol.swp.server.usermanagement.UserManagement#createUser(User)
   * @see de.uol.swp.common.user.request.RegisterUserRequest
   * @see de.uol.swp.common.user.response.RegistrationSuccessfulResponse
   * @see de.uol.swp.common.user.exception.RegistrationExceptionMessage
   */
  @Subscribe
  private void onRegisterUserRequest(RegisterUserRequest msg) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got new registration message with {}", msg.getUser());
    }
    ResponseMessage returnMessage;
    try {
      userManagement.createUser(msg.getUser());
      returnMessage = new RegistrationSuccessfulResponse(msg.getUser());
    } catch (Exception e) {
      LOG.error(e);
      returnMessage =
          new RegistrationExceptionMessage(
              "Benutzer " + msg.getUser() + " konnte nicht erstellt werden!");
    }
    msg.getMessageContext().ifPresent(returnMessage::setMessageContext);
    post(returnMessage);
  }

  /**
   * Handels the onUpdateUserRequest found on the Eventbus.
   *
   * @param msg UpdateUserRequest
   */
  @Subscribe
  public void onUpdateUserRequest(UpdateUserRequest msg) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got new User to change password message with {}", msg.getUser());
    }
    ResponseMessage returnMessage;

    try {
      returnMessage =
          new UpdateUserResponse(
              (UserDTO) userManagement.updateUser(msg.getUser(), msg.getPassword()));
    } catch (Exception e) {
      LOG.error(e);
      if (e.getMessage().equals("Das Password ist Falsch!")) {
        returnMessage =
            new de.uol.swp.common.user.exception.UpdateUserExceptionMessage(e.getMessage());
      } else {
        returnMessage =
            new UpdateUserExceptionMessage(
                "Benutzer " + msg.getUser().getUsername() + " konnte nicht aktualisiert werden.");
      }
    }
    msg.getMessageContext().ifPresent(returnMessage::setMessageContext);

    post(returnMessage);
  }

  /**
   * Handles the UpdateUsernameRequest found on the Eventbus.
   *
   * @param msg UpdateUsernameRequest
   */
  @Subscribe
  public void onUpdateUsernameRequest(UpdateUsernameRequest msg) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got new username message with {}", msg.getUser());
    }
    ResponseMessage returnMessage;

    try {
      returnMessage =
          new UpdateUsernameResponse(
              (UserDTO)
                  userManagement.updateUsername(
                      msg.getUser(), msg.getUpdateName(), msg.getPassword()));
      Message message = new UserLoggedOutMessage(msg.getUser().getUsername());
      post(message);
      message = new UserLoggedInMessage(msg.getUpdateName());
      post(message);
    } catch (Exception e) {
      LOG.error(e);
      returnMessage =
          new UpdateUserExceptionMessage(
              "Benutzer " + msg.getUser().getUsername() + " konnte nicht aktualisiert werden.");
    }
    msg.getMessageContext().ifPresent(returnMessage::setMessageContext);

    post(returnMessage);
  }

  /**
   * Handles the UpdateAvatarRequest found on the Eventbus.
   *
   * @param msg UpdateAvatarRequest
   */
  @Subscribe
  public void onUpdateAvatarRequest(UpdateAvatarRequest msg) {
    LOG.debug("Got userdata message from {}", msg.getUser());
    ResponseMessage returnMessage;

    try {
      returnMessage =
          new UpdateAvatarResponse(
              (UserDTO) userManagement.updateAvatar(msg.getUser(), msg.getUpdateAvatar()));
    } catch (Exception e) {
      LOG.error(e);
      returnMessage =
          new UpdateUserExceptionMessage(
              "Benutzer " + msg.getUser().getUsername() + " konnte nicht aktualisiert werden.");
    }
    msg.getMessageContext().ifPresent(returnMessage::setMessageContext);

    post(returnMessage);
  }

  /**
   * Subscribes to the UpdateUserStatsMessage event and updates the user statistics accordingly.
   *
   * @param userStats The UpdateUserStatsMessage containing the user and user statistics.
   */
  @Subscribe
  public void onUpdateUserStatistics(UpdateUserStatsMessage userStats) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got userdata message from {}", userStats.getUser());
    }

    try {
      userManagement.updateUserStatistics(userStats.getUser(), userStats.getUserStatistic());
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  /**
   * Subscribes to the RetrieveUserStatsRequest event and retrieves the user statistics for the
   * requested user.
   *
   * @param request The RetrieveUserStatsRequest containing the user for which to retrieve
   *     statistics.
   */
  @Subscribe
  public void onRetrieveUserStatsRequest(RetrieveUserStatsRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got new user stats request from {}", request.getUser());
    }
    ResponseMessage returnMessage;

    try {
      returnMessage =
          new UpdateUserStatsResponse(userManagement.retrieveUserStatistics(request.getUser()));
    } catch (Exception e) {
      LOG.error(e);
      returnMessage =
          new UpdateUserStatsExceptionMessage(
              "User Statistiken vom Benutzer "
                  + request.getUser().getUsername()
                  + " konnten nicht abgerufen werden.");
    }
    request.getMessageContext().ifPresent(returnMessage::setMessageContext);

    post(returnMessage);
  }
}
