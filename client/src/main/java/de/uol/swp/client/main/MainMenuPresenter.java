package de.uol.swp.client.main;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.audio.PlaySound;
import de.uol.swp.client.chat.ClientChatService;
import de.uol.swp.client.instruction.event.ShowInstructionTabEvent;
import de.uol.swp.client.lobby.events.ShowLobbyCreationTabEvent;
import de.uol.swp.client.lobby.events.ShowLobbyListTabEvent;
import de.uol.swp.client.textureatlas.TextureAtlas;
import de.uol.swp.client.userprofile.event.ShowUserProfileTabEvent;
import de.uol.swp.common.chat.ChatCommandResponse;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.response.AllOnlineUsersResponse;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.LogoutSuccessfulResponse;
import de.uol.swp.common.user.response.UpdateAvatarResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the main menu.
 *
 * @author Marco Grawunder
 * @see de.uol.swp.client.AbstractPresenter
 */
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class MainMenuPresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/MainMenuTab.fxml";
  public static final String DIALOG_STYLE_SHEET = "css/alertBox.css";
  private static final String LOBBY_ID = "GLOBAL";
  private static final Logger LOG = LogManager.getLogger(MainMenuPresenter.class);
  private final PlaySound playSound = new PlaySound();
  private ObservableList<String> users;
  private ObservableList<Text> chat;
  private boolean mouseOnChat = false;
  private boolean isMusikStarted;
  @Inject private ClientChatService clientChatService;
  @Inject private LoggedInUser loggedInUser;
  @FXML private ListView<String> usersView;
  @FXML private ListView<Text> globalChat;
  @FXML private TextField messageField;
  @FXML private ImageView avatar;
  @FXML private Slider sliderMusic;

  @FXML
  private void onVolume() {
    playSound.setVolume((float) (sliderMusic.getValue() / 100));
  }

  private InputStream getImageInputStream(String name) throws IOException {
    String path = "/images/avatar/" + name + ".png";
    return new FileInputStream(path);
  }

  /**
   * Handles successful login.
   *
   * <p>If a LoginSuccessfulResponse is posted to the EventBus the loggedInUser of this client is
   * set to the one in the message received and the full list of users currently logged in is
   * requested.
   *
   * @param message the LoginSuccessfulResponse object seen on the EventBus
   * @see de.uol.swp.common.user.response.LoginSuccessfulResponse
   */
  @Subscribe
  public void onLoginSuccessfulResponse(LoginSuccessfulResponse message) {
    if (!isMusikStarted && message.getUser().getUUID().equals(loggedInUser.getUser().getUUID())) {
      isMusikStarted = true;
      PlaySound.stopAll();
      playSound.playSound("/sounds/background_musik.wav", true);
      playSound.setVolume((float) sliderMusic.getValue() / 100);
    }
    userService.retrieveAllOnlineUsers();
    updateChatList();
  }

  /**
   * Handles user list clear when logged out.
   *
   * <p>If a LogoutSuccessfulResponse is posted to the EventBus the list of users currently logged
   * in is cleared. If the user was marked for deletion it gives the user service a request to
   * delete the user of this client.
   *
   * @param message the LogoutSuccessfulResponse object seen on the EventBus
   * @see de.uol.swp.common.user.response.LogoutSuccessfulResponse
   */
  @Subscribe
  public void onLogoutSuccessfulResponse(LogoutSuccessfulResponse message) {
    Platform.runLater(() -> globalChat.getItems().clear());
  }

  /**
   * Handles new logged-in users.
   *
   * <p>If a new UserLoggedInMessage object is posted to the EventBus the name of the newly
   * logged-in user is appended to the user list in the main menu. Furthermore, if the LOG-Level is
   * set to DEBUG the message "New user {@literal <Username>} logged in." is displayed in the log.
   *
   * <p>A system message is inserted in the chat indicating that a new user is logged in.
   *
   * @param message the UserLoggedInMessage object seen on the EventBus
   * @see de.uol.swp.common.user.message.UserLoggedInMessage
   */
  @Subscribe
  public void onUserLoggedInMessage(UserLoggedInMessage message) {
    LOG.debug("New user {} logged in.", message.getUsername());
    Platform.runLater(
        () -> {
          if (loggedInUser.getUser() != null
              && !loggedInUser.getUsername().equals(message.getUsername())) {
            if (users == null) {
              updateUsersList(new ArrayList<>());
            }
            users.add(message.getUsername());
          }
        });
  }

  /**
   * Handles new logged-out users.
   *
   * <p>If a new UserLoggedOutMessage object is posted to the EventBus the name of the newly
   * logged-out user is removed from the user list in the main menu. Furthermore, if the LOG-Level
   * is set to DEBUG the message "User {@literal <Username>} logged out." is displayed in the log.
   *
   * <p>A system message is inserted in the chat indicating that a user is logged out.
   *
   * @param message the UserLoggedOutMessage object seen on the EventBus
   * @see de.uol.swp.common.user.message.UserLoggedOutMessage
   */
  @Subscribe
  public void onUserLoggedOutMessage(UserLoggedOutMessage message) {
    if (users == null) {
      updateUsersList(new ArrayList<>());
    }
    LOG.debug("User {} logged out.", message.getUsername());
    Platform.runLater(() -> users.remove(message.getUsername()));
  }

  /**
   * Handles new list of users.
   *
   * <p>If a new AllOnlineUsersResponse object is posted to the EventBus the names of currently
   * logged-in users are put onto the user list in the main menu. Furthermore, if the LOG-Level is
   * set to DEBUG the message "Update of user list" with the names of all currently logged-in users
   * is displayed in the log.
   *
   * @param allUsersResponse the AllOnlineUsersResponse object seen on the EventBus
   * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
   */
  @Subscribe
  public void onAllOnlineUsersResponse(AllOnlineUsersResponse allUsersResponse) {
    LOG.debug("Update of user list {}", allUsersResponse.getUsers());
    updateUsersList(allUsersResponse.getUsers());
    showAvatar();
  }

  /** This method is called by the eventBus and update the Avatar in the MainMenu. */
  @Subscribe
  public void onUpdateAvatarResponse(UpdateAvatarResponse updateAvatarResponse) {
    showAvatar();
  }

  private void showAvatar() {
    int number = loggedInUser.getUser().getUserData().getAvatarId();
    if (number != 0) {
      avatar.setImage(TextureAtlas.getAvatar(number));
    }
  }

  /**
   * Method to handle chatMessageMessage found on the Eventbus.
   *
   * @param chatMessageMessage chatMessageMessage
   */
  @Subscribe
  public void onChatMessageMessage(ChatMessageMessage chatMessageMessage) {
    if (loggedInUser.getUser() != null
        && chatMessageMessage.getChatMessage().getLobbyId().equals(LOBBY_ID)) {
      if (chat == null) {
        updateChatList();
      }
      Text text = new Text(chatMessageMessage.getChatMessage().toString());
      text.setWrappingWidth(globalChat.getWidth() - 20);
      Platform.runLater(
          () -> {
            chat.add(text);
            if (!mouseOnChat) {
              globalChat.scrollTo(chat.size() - 1);
            }
          });
    }
  }

  /**
   * Responds to replies from commands.
   *
   * @param msg The message from server.
   */
  @Subscribe
  public void onChatCommandResponse(ChatCommandResponse msg) {
    if (msg.getChatMassage().getLobbyId().equals(LOBBY_ID) && loggedInUser.getUser() != null) {
      if (chat == null) {
        updateChatList();
      }
      Text text = new Text(msg.getChatMassage().toString());
      text.setWrappingWidth(globalChat.getWidth() - 20);
      Platform.runLater(
          () -> {
            chat.add(text);
            if (!mouseOnChat) {
              globalChat.scrollTo(chat.size() - 1);
            }
          });
    }
  }

  /**
   * Updates the main menus user list according to the list given.
   *
   * <p>This method clears the entire user list and then adds the name of each user in the list
   * given to the main menus user list. If there ist no user list this it creates one.
   *
   * @implNote The code inside this Method has to run in the JavaFX-application thread. Therefore,
   *     it is crucial not to remove the {@code Platform.runLater()}
   * @param userList A list of UserDTO objects including all currently logged-in users
   * @see de.uol.swp.common.user.UserDTO
   */
  private void updateUsersList(List<UserDTO> userList) {
    Platform.runLater(
        () -> {
          if (users == null) {
            users = FXCollections.observableArrayList();
            usersView.setItems(users);
          }
          users.clear();
          userList.forEach(u -> users.add(u.getUsername()));
        });
  }

  /** The method is called at the beginning to initialize the chat display. */
  private void updateChatList() {
    Platform.runLater(
        () -> {
          if (chat == null) {
            chat = FXCollections.observableArrayList();
            globalChat.setItems(chat);
            globalChat.setCellFactory(
                stringListView -> {
                  ChatCell textListCell = new ChatCell();
                  textListCell.setMaxHeight(1000);
                  textListCell.setMaxWidth(globalChat.getWidth());
                  textListCell.setWrapText(true);
                  return textListCell;
                });
            LOG.debug("The chat was null and now it is {}", chat);
          } else {
            globalChat.getItems().clear();
          }
        });
  }

  /**
   * Method called when the create lobby button is pressed.
   *
   * <p>If the create lobby button is pressed, a ShowLobbyTabEvent is posted
   *
   * @see ShowLobbyCreationTabEvent
   */
  @FXML
  void onCreateLobbyView() {
    eventBus.post(new ShowLobbyCreationTabEvent());
  }

  /**
   * Method called when the join lobby button is pressed If the join lobby button is pressed, this
   * method sends a ShowLobbyListTabEvent.
   *
   * @see ShowLobbyListTabEvent
   */
  @FXML
  void onJoinLobby() {
    eventBus.post(new ShowLobbyListTabEvent());
  }

  /**
   * Method called when the my profile button is pressed.
   *
   * <p>If the button is pressed, this method requests the user profile service to post a new
   * ShowUserProfileTabEvent on the event bus.
   *
   * @see ShowUserProfileTabEvent
   */
  @FXML
  void onMyProfile() {
    eventBus.post(new ShowUserProfileTabEvent());
  }

  /**
   * Method called when the logout button is pressed.
   *
   * <p>This method is called when the logout button is pressed.
   *
   * @see de.uol.swp.client.user.UserService
   */
  @FXML
  void onUserLogout() {
    userService.logout(loggedInUser.getUser());
  }

  /**
   * Method called when the send button is pressed
   *
   * <p>If the send button is pressed, this method request the ClientChatService to send the server
   * a new message request. If there is no text in the text field, no message will be written.
   *
   * @see de.uol.swp.client.chat.ClientChatService
   */
  @FXML
  void onSend() {
    try {
      if (!messageField.getText().isBlank()) {
        clientChatService.sendMessage(messageField.getText(), LOBBY_ID);
        messageField.clear();
      }
    } catch (Exception e) {
      showError("Fehler beim Senden der Nachricht");
    }
  }

  /**
   * The method is called when the Enter key is pressed in the text field.
   *
   * <p>When the Enter key is pressed, the same thing is done as with onSend.
   *
   * @see de.uol.swp.client.chat.ClientChatService
   */
  @FXML
  void onEnter() {
    try {
      if (!messageField.getText().isBlank()) {
        clientChatService.sendMessage(messageField.getText(), LOBBY_ID);
        messageField.clear();
      }
    } catch (Exception e) {
      showError("Fehler beim Senden der Nachricht");
    }
  }

  /**
   * Shows a window with the message.
   *
   * @param message The message displayed to the user.
   */
  public void showError(String message) {
    LOG.error("New Error Message: {}", message);
    Platform.runLater(
        () -> {
          Alert a = new Alert(Alert.AlertType.ERROR, message);
          DialogPane pane = a.getDialogPane();
          pane.getStylesheets().add(DIALOG_STYLE_SHEET);
          a.showAndWait();
        });
  }

  /** The method sets mouseOnChat to False if the mouse is not on the chat. */
  @FXML
  public void onMouseEnteredTarget() {
    mouseOnChat = true;
  }

  /** The method sets mouseOnChat to True when the mouse is over the chat. */
  @FXML
  public void onMouseExitTarget() {
    mouseOnChat = false;
    globalChat.scrollTo(globalChat.getItems().size() - 1);
  }

  /** Exits the JavaFX application. */
  @FXML
  public void onExit() {
    Platform.exit();
  }

  /** When pressed, it opens the instruction tab. */
  @FXML
  public void onInstructionPress() {
    eventBus.post(new ShowInstructionTabEvent());
  }

  @SuppressWarnings("java:S110")
  static class ChatCell extends ListCell<Text> {
    @Override
    public void updateItem(Text item, boolean empty) {
      setGraphic(item);
    }
  }
}
