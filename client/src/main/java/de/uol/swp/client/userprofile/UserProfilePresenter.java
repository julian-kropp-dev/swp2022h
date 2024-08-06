package de.uol.swp.client.userprofile;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.textureatlas.TextureAtlas;
import de.uol.swp.client.userprofile.event.ChangeUserDataErrorEvent;
import de.uol.swp.client.userprofile.event.ShowUserProfileTabEvent;
import de.uol.swp.common.user.response.UpdateAvatarResponse;
import de.uol.swp.common.user.response.UpdateUserResponse;
import de.uol.swp.common.user.response.UpdateUserStatsResponse;
import de.uol.swp.common.user.response.UpdateUsernameResponse;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Managed the UserProfilePresenter. */
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class UserProfilePresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/UserProfileTab.fxml";
  static final String DIALOG_STYLE_SHEET = "css/confirmationBox.css";
  private static final Logger LOG = LogManager.getLogger(UserProfilePresenter.class);
  private boolean tabClosed = false;
  @Inject private LoggedInUser loggedInUser;
  @FXML private Text username;
  @FXML private TextField txtUsername;
  @FXML private Button btnChangeUsername;
  @FXML private PasswordField txtPassword;
  @FXML private PasswordField txtPasswordWdh;
  @FXML private Button btnPasswordChange;
  @FXML private ChoiceBox<Integer> avatarId;
  @FXML private ImageView avatar;
  @FXML private Button btnStats;
  @FXML private Text avatarTxt;
  @FXML private Button onDeleteUser;
  @FXML private ListView<String> userStatsList;
  @FXML private AnchorPane anchorPane;
  @FXML private Text statsAvg;
  private ObservableList<String> users;

  @FXML
  private void initialize() {
    username.setText(loggedInUser.getUsername());
    avatarId.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));
    avatarId.setValue(
        avatarId.getItems().get(loggedInUser.getUser().getUserData().getAvatarId() - 1));
    avatarId
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                userService.updateAvatar(loggedInUser.getUser(), newValue.intValue() + 1));
    updateStatsList();

    avatar.setImage(TextureAtlas.getAvatar(loggedInUser.getUser().getUserData().getAvatarId()));
  }

  /**
   * If the User clicks on the button "Benutzername ändern" the text-field is checked, when empty it
   * displays an ErrorMessage.
   *
   * @param event empty
   */
  @FXML
  void onChangeUsername(ActionEvent event) {
    if (!txtUsername.getText().equals("")) {
      String password = showPasswordInput("Benutzernamen ändern", "Bitte gib dein Passwort ein");
      userService.updateUsername(loggedInUser.getUser(), txtUsername.getText(), password);
      txtUsername.clear();
    } else {
      eventBus.post(new ChangeUserDataErrorEvent("Bitte gib was in das Feld Benutzer ein!"));
    }
    event.consume();
  }

  /**
   * Sets the style of the button to default, when the password field is clicked and resets the
   * username field.
   *
   * @param mouseEvent empty
   */
  @FXML
  void onUsernameTextFieldClicked(MouseEvent mouseEvent) {
    btnChangeUsername.setStyle("");
    txtUsername.clear();
    mouseEvent.consume();
  }

  /**
   * When the user presses the button, it is asked whether the passwords are empty and the same,
   * then the password is updated via the updateUser. if not an error message is displayed.
   *
   * @param event the event of the button press
   */
  @FXML
  public void onChangeUserPassword(ActionEvent event) {
    if (!txtPasswordWdh.getText().equals("") && !txtPassword.getText().equals("")) {
      if (txtPasswordWdh.getText().equals(txtPassword.getText())) {
        String oldPassword =
            showPasswordInput("Passwort ändern", "Bitte gib dein altes Passwort ein.");
        userService.updateUser(
            loggedInUser.getUser().getUUID(),
            loggedInUser.getUsername(),
            txtPassword.getText(),
            loggedInUser.getUser().getMail(),
            oldPassword,
            loggedInUser.getUser().getUserData());
        txtPassword.clear();
        txtPasswordWdh.clear();
      } else {
        eventBus.post(new ChangeUserDataErrorEvent("Die Passwörter stimmen nicht überein."));
      }
    } else {
      eventBus.post(
          new ChangeUserDataErrorEvent(
              "Du hast kein Passwort eingegeben!\nBitte fülle alle Felder aus!"));
    }
    event.consume();
  }

  /**
   * Sets the style of the button to default, when the password field is clicked.
   *
   * @param mouseEvent mouseEvent
   */
  @FXML
  public void onPasswordFieldClicked(MouseEvent mouseEvent) {
    btnPasswordChange.setStyle("");
    mouseEvent.consume();
  }

  /**
   * If the request was successful, the button changes to green.
   *
   * @param response UpdateUserResponse
   */
  @Subscribe
  public void onUpdateUserResponse(UpdateUserResponse response) {
    if (!tabClosed) {
      btnPasswordChange.setStyle("-fx-background-color: LightGreen;");
      Platform.runLater(() -> username.setText(loggedInUser.getUsername()));
    }
  }

  /**
   * Handels the UpdateUsernameResponse found on the EventBus. It updates the username globally.
   *
   * @param response UpdateUsernameResponse
   */
  @Subscribe
  public void onUpdateUsernameResponse(UpdateUsernameResponse response) {
    if (!tabClosed) {
      btnChangeUsername.setStyle("-fx-background-color: LightGreen;");
      Platform.runLater(() -> username.setText(loggedInUser.getUsername()));
    }
  }

  /**
   * Handles the ShowUserProfileTabEvent found on the Eventbus.
   *
   * @param event ShowUserProfileTabEvent
   */
  @Subscribe
  public void onShowUserProfileViewEvent(ShowUserProfileTabEvent event) {
    if (!tabClosed) {
      Platform.runLater(() -> username.setText(loggedInUser.getUsername()));
    }
  }

  private String showPasswordInput(String title, String message) {
    Dialog<String> dialog = new Dialog<>();

    dialog.setTitle(title);
    dialog.setHeaderText(message);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    PasswordField pwd = new PasswordField();
    HBox content = new HBox();
    content.setAlignment(Pos.CENTER_LEFT);
    content.setSpacing(10);
    content.getChildren().addAll(pwd);
    dialog.getDialogPane().setContent(content);
    dialog.setResultConverter(
        t -> {
          if (t == ButtonType.OK) {
            return pwd.getText();
          } else {
            return null;
          }
        });
    Optional<String> result = dialog.showAndWait();
    return result.orElse(null);
  }

  @FXML
  public void onClose() {
    tabClosed = true;
  }

  @FXML
  void onDeleteUser() {
    Platform.runLater(
        () -> {
          ButtonType buttonTypeYes = new ButtonType("Ja");
          ButtonType buttonTypeNo = new ButtonType("Nein");
          Alert alert =
              new Alert(
                  Alert.AlertType.CONFIRMATION,
                  "Möchtest du diesen Account wirklich löschen? "
                      + "Dieser Schritt kann nicht rückgängig gemacht werden!",
                  ButtonType.NO,
                  ButtonType.YES);
          alert.getButtonTypes().clear();
          alert.getButtonTypes().addAll(buttonTypeYes, buttonTypeNo);
          Node yesButton = alert.getDialogPane().lookupButton(buttonTypeYes);
          Node noButton = alert.getDialogPane().lookupButton(buttonTypeNo);
          yesButton.setId("yesButton");
          noButton.setId("noButton");
          alert.setTitle("Bitte bestätigen");
          DialogPane pane = alert.getDialogPane();
          pane.getStylesheets().add(DIALOG_STYLE_SHEET);
          alert.showAndWait();
          if (alert.getResult() == buttonTypeYes) {
            LOG.debug("Droppimg User {}", username);
            userService.dropUser(loggedInUser.getUser());
          } else {
            alert.close();
          }
        });
  }

  private void updateStatsList() {
    Platform.runLater(
        () -> {
          if (users == null) {
            users = FXCollections.observableArrayList();
            users.add(0, "Keine Daten vorhanden.");
            statsAvg.setText("Durchschnitt deiner Platzierungen: --");
          }
          userStatsList.setItems(users);
        });
  }

  /**
   * Updates the user statistics by processing the response from a user statistics update.
   *
   * @param userStatistics The response containing the updated user statistics.
   */
  @Subscribe
  public void onUpdateUserStats(UpdateUserStatsResponse userStatistics) {
    Platform.runLater(
        () -> {
          users.clear();
          int totalPlacement = 0;
          int totalCount = 0;
          if (userStatistics.getUserStatistics() != null) {
            for (UserStatistic u : userStatistics.getUserStatistics()) {
              users.add(
                  u.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))
                      + " Uhr: "
                      + u.getPlacement()
                      + ". Platz");

              totalPlacement += u.getPlacement();
              totalCount++;
            }

            if (totalCount > 0) {
              double averagePlacement = (double) totalPlacement / totalCount;
              statsAvg.setText("Durchschnittliche Platzierung: " + averagePlacement);
            }
          }
        });
  }

  /**
   * Handles the event when the view stats button is clicked. Toggles the visibility of the anchor
   * pane based on its current state.
   */
  @FXML
  public void onViewStats() {
    if (!anchorPane.isVisible()) {
      setVisibility(false);
      anchorPane.setVisible(true);
      userService.retrieveUserStats(loggedInUser.getUser());
      avatarTxt.setText("Benutzer Statistiken");
      updateStatsList();
    } else {
      setVisibility(true);
      avatarTxt.setText("Avatar");
      anchorPane.setVisible(false);
    }
  }

  private void setVisibility(boolean isVisible) {
    username.setVisible(isVisible);
    txtUsername.setVisible(isVisible);
    btnChangeUsername.setVisible(isVisible);
    txtPassword.setVisible(isVisible);
    txtPasswordWdh.setVisible(isVisible);
    btnPasswordChange.setVisible(isVisible);
    avatarId.setVisible(isVisible);
    avatar.setVisible(isVisible);
    btnStats.setVisible(isVisible);
    onDeleteUser.setVisible(isVisible);
  }

  /**
   * The method updates the user's avatar with the new avatar id from the message.
   *
   * @param msg the UpdateAvatarResponse message containing the new avatar image
   */
  @Subscribe
  public void onUpdateAvatarResponse(UpdateAvatarResponse msg) {
    if (!tabClosed) {
      avatar.setImage(TextureAtlas.getAvatar(loggedInUser.getUser().getUserData().getAvatarId()));
    }
  }

  private InputStream getImageInputStream(String name) throws IOException {
    String path = "client/src/main/resources/images/avatar/" + name + ".png";
    return new FileInputStream(path);
  }
}
