package de.uol.swp.client.lobby;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.game.events.ShowGameTabEvent;
import de.uol.swp.client.lobby.events.LobbyInfoEvent;
import de.uol.swp.client.lobby.events.ShowLobbyTabEvent;
import de.uol.swp.client.lobby.tableview.LobbyTableData;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.lobby.exception.LobbyDoesNotAllowSpectatorsExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyDoesNotExistExceptionMessage;
import de.uol.swp.common.lobby.message.LobbyCreatedMessage;
import de.uol.swp.common.lobby.message.LobbyDroppedMessage;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.response.AllLobbyIdResponse;
import de.uol.swp.common.lobby.response.LobbyJoinAsSpectatorResponse;
import de.uol.swp.common.lobby.response.LobbyJoinSuccessfulResponse;
import de.uol.swp.common.user.User;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages the LobbyListView. */
@SuppressWarnings("UnstableApiUsage")
public class LobbyListPresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/LobbyListTab.fxml";
  private static final Logger LOG = LogManager.getLogger(LobbyListPresenter.class);
  private ObservableList<LobbyTableData> lobbyTableData;
  @Inject private LobbyService lobbyService;
  @Inject private LoggedInUser loggedInUser;
  @FXML private TextField joinCodeField;
  @FXML private TableView<LobbyTableData> tableView;
  @FXML private TableColumn<LobbyTableData, String> tableName;
  @FXML private TableColumn<LobbyTableData, String> tableTitle;
  @FXML private TableColumn<LobbyTableData, String> tableOwner;
  @FXML private TableColumn<LobbyTableData, Integer> tableUser;
  @FXML private TableColumn<LobbyTableData, Integer> tableMaxUser;
  @FXML private TableColumn<LobbyTableData, String> tableStatus;
  @FXML private TableColumn<LobbyTableData, String> tableVariantOne;
  @FXML private TableColumn<LobbyTableData, String> tableVariantTwo;
  @FXML private TableColumn<LobbyTableData, String> tableVariantThree;

  private boolean tabClosed = false;

  /** This methode is to set up the table view to display information about lobbies. */
  @FXML
  public void initialize() {
    lobbyService.retrieveAllLobbys();
    tableName.setCellValueFactory(new PropertyValueFactory<>("lobbyId"));
    tableTitle.setCellValueFactory(new PropertyValueFactory<>("lobbyTitle"));
    tableOwner.setCellValueFactory(new PropertyValueFactory<>("lobbyOwner"));
    tableUser.setCellValueFactory(new PropertyValueFactory<>("lobbyAvailableSlots"));
    tableMaxUser.setCellValueFactory(new PropertyValueFactory<>("lobbyMaximalSlots"));
    tableStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    tableVariantOne.setCellValueFactory(new PropertyValueFactory<>("variantOne"));
    tableVariantTwo.setCellValueFactory(new PropertyValueFactory<>("variantTwo"));
    tableVariantThree.setCellValueFactory(new PropertyValueFactory<>("variantThree"));
  }

  /**
   * Handles newly created lobbies
   *
   * <p>If a new LobbyCreatedMessage object is posted to the EventBus the name of the newly created
   * lobby is appended to the lobby list in the LobbyListView. Only public lobbys get added.
   * Furthermore, if the LOG-Level is set to DEBUG the message "New Lobby {@literal <lobbyId>}
   * created." is displayed in the log.
   *
   * @param message the LobbyCreatedMessage object seen on the EventBus
   * @see de.uol.swp.common.lobby.message.LobbyCreatedMessage
   */
  @Subscribe
  public void onLobbyCreatedMessage(LobbyCreatedMessage message) {
    if (!tabClosed && !message.getLobbyOptions().isPrivateLobby()) {
      LOG.debug("New lobby {} created.", message.getLobbyId());
      Platform.runLater(
          () -> {
            if (lobbyTableData != null && loggedInUser.getUser() != null) {
              lobbyTableData.add(
                  new LobbyTableData(
                      message.getLobbyId(),
                      message.getLobbyOptions().getLobbyTitle(),
                      message.getUser().getUsername(),
                      1,
                      message.getLobbyOptions().getSlot(),
                      message.getLobbyOptions().getLobbyStatus().name(),
                      message.getLobbyOptions().isActiveLasers(),
                      message.getLobbyOptions().isSwitchOffRoboter(),
                      message.getLobbyOptions().isSwitchOffRoboter()));

              LOG.debug("Adding public lobby {} to list", message.getLobbyId());
            } else {
              lobbyTableData = FXCollections.observableArrayList();
              tableView.setItems(lobbyTableData);
              lobbyTableData.add(
                  new LobbyTableData(
                      message.getLobbyId(),
                      message.getLobbyOptions().getLobbyTitle(),
                      message.getUser().getUsername(),
                      1,
                      message.getLobbyOptions().getSlot(),
                      message.getLobbyOptions().getLobbyStatus().name(),
                      message.getLobbyOptions().isActiveLasers(),
                      message.getLobbyOptions().isSwitchOffRoboter(),
                      message.getLobbyOptions().isSwitchOffRoboter()));
            }
          });
    }
  }

  /**
   * Handles new list of lobbies
   *
   * <p>If a new AllLobbyIdResponse object is posted to the EventBus the names of all lobbies are
   * put onto the lobby list in the LobbyListView. Furthermore, if the LOG-Level is set to DEBUG the
   * message "Update of lobby list" with the names of all lobbies is displayed in the log.
   *
   * @param response the AllLobbyIdResponse object seen on the EventBus
   * @see AllLobbyIdResponse
   */
  @Subscribe
  public void onAllLobbyIdResponse(AllLobbyIdResponse response) {
    if (!tabClosed) {
      LOG.debug("Update of lobby list {}", response.getLobbies());
      updateLobbyList(response.getLobbies());
    }
  }

  /**
   * Handles a dropped Lobby
   *
   * <p>If a new LobbyDroppedMessage object is posted to the EventBus the name of the newly dropped
   * lobby is removed from the lobby list in the lobby view. Furthermore, if the LOG-Level is set to
   * DEBUG the message "Lobby {@literal <lobbyId>} dropped." is displayed in the log.
   *
   * @param message the LobbyDroppedMessage object seen on the EventBus
   * @see de.uol.swp.common.lobby.message.LobbyDroppedMessage
   */
  @Subscribe
  public void onLobbyDroppedMessage(LobbyDroppedMessage message) {
    if (!tabClosed) {
      LOG.debug("Lobby {} dropped.", message.getLobbyId());
      Optional<LobbyTableData> lobbyDisplay =
          lobbyTableData.stream()
              .filter(l -> l.getLobbyId().equals(message.getLobbyId()))
              .findFirst();
      int lobbyIndex;
      lobbyIndex = lobbyDisplay.map(l -> lobbyTableData.indexOf(l)).orElse(-1);
      if (lobbyIndex >= 0) {
        Platform.runLater(() -> lobbyTableData.remove(lobbyIndex));
      } else {
        LOG.error(
            "Lobby {} cannot be deleted. It was not found in LobbyTableData", message.getLobbyId());
      }
    }
  }

  /**
   * Updates the lobby list according to the given list.
   *
   * <p>This method clears the entire lobby list and then adds the name of each lobby in the list
   * given to the LobbyListView lobby list. If there ist no lobby list this it creates one. If the
   * lobby is private is does not get added to the lobby list.
   *
   * @implNote The code inside this Method has to run in the JavaFX-application thread. Therefore,
   *     it is crucial not to remove the {@code Platform.runLater()}
   * @param lobbyList A list of LobbyDTO objects including all lobbies
   * @see de.uol.swp.common.lobby.dto.LobbyDTO
   */
  private void updateLobbyList(List<LobbyDTO> lobbyList) {
    Platform.runLater(
        () -> {
          if (lobbyTableData == null) {
            lobbyTableData = FXCollections.observableArrayList();
            tableView.setItems(lobbyTableData);
          }

          lobbyTableData.clear();
          lobbyList.stream()
              .filter(l -> !l.getOptions().isPrivateLobby())
              .forEach(
                  l ->
                      lobbyTableData.add(
                          new LobbyTableData(
                              l.getLobbyId(),
                              l.getOptions().getLobbyTitle(),
                              l.getOwner().getUsername(),
                              l.getPlayerCount(),
                              l.getOptions().getSlot(),
                              l.getOptions().getLobbyStatus().name(),
                              l.getOptions().isActiveLasers(),
                              l.getOptions().isSwitchOffRoboter(),
                              l.getOptions().isWeakDuplicatedActive())));
        });
  }

  /**
   * Method called when the join lobby button is pressed.
   *
   * <p>If the join lobby button is pressed, this method first checks if the joinCodeField contains
   * any characters. In this case any selection in the LobbyList is overruled. If the joinCodeField
   * contains a valid JoinCode, e.g. 4 Characters, the LobbyService tries to join the Lobby. If the
   * joinCodeField does not contain any characters the selection in the LobbyList is used for
   * joining the lobby.
   *
   * @see de.uol.swp.client.lobby.LobbyService
   */
  @FXML
  void onJoinLobby() {
    if (!Strings.isNullOrEmpty(joinCodeField.getText())) {
      String joinCode = joinCodeField.getText().toUpperCase();
      LOG.debug("JoinCode {} entered.", joinCode);
      if (!joinCode.matches("^[A-Z]{4}$")) {
        // noinspection SpellCheckingInspection
        eventBus.post(new LobbyInfoEvent("JoinCodes sind 4 Buchstaben lang.\nZum Beispiel: ABCD"));
      } else {
        lobbyService.joinLobby(joinCode, loggedInUser.getUser());
      }
    } else {
      // uses selected Item to join Lobby
      ObservableList<LobbyTableData> selectedItems =
          tableView.getSelectionModel().getSelectedItems();
      if (selectedItems.isEmpty()) {
        eventBus.post(new LobbyInfoEvent("Keine Lobby ausgewählt!"));
      } else {
        String lobbyId = selectedItems.get(0).getLobbyId();
        LOG.debug("Lobby {} selected.", lobbyId);
        lobbyService.joinLobby(lobbyId, loggedInUser.getUser());
      }
    }
    joinCodeField.clear();
  }

  /**
   * Handles the LobbyDoesNotExistExceptionMessage found on the Eventbus. This Exception usually
   * means that the user entered a non-existing join-code.
   *
   * @param message LobbyDoesNotExistExceptionMessage
   */
  @Subscribe
  public void onLobbyDoesNotExistExceptionMessage(LobbyDoesNotExistExceptionMessage message) {
    if (!tabClosed) {
      LOG.debug("Received LobbyDoesNotExistExceptionMessage for Lobby {}", message.getMessage());
      eventBus.post(new LobbyInfoEvent(message + "\nHast du dich vielleicht vertippt?"));
    }
  }

  /**
   * Handles the LobbyDoesNotAllowSpectatorsExceptionMessage found on the Eventbus. It means that
   * the selected lobby does not allow spectators to join.
   *
   * @param message LobbyDoesNotAllowSpectatorsExceptionMessage
   */
  @Subscribe
  public void onLobbyDoesNotAllowSpectatorsExceptionMessage(
      LobbyDoesNotAllowSpectatorsExceptionMessage message) {
    if (!tabClosed) {
      LOG.debug("Reachieved LobbyDoesNotExistExceptionMessage for Lobby {}", message.getMessage());
      eventBus.post(new LobbyInfoEvent(message.toString()));
    }
  }

  /**
   * Handles new users in lobby
   *
   * <p>If a new UserJoinedLobbyMessage object is posted to the EventBus the name of the new joining
   * user is appended to the lobby user list (in the main menu).
   *
   * @param message the UserJoinedLobbyMessage object seen on the EventBus
   * @see de.uol.swp.common.lobby.message.UserJoinedLobbyMessage
   */
  @Subscribe
  public void onUserJoinedLobbyMessage(UserJoinedLobbyMessage message) {
    if (!tabClosed) {
      User newLobbyMember = message.getUser();
      if (!newLobbyMember.equals(loggedInUser.getUser())) {
        LOG.debug(
            "Another user {} has joined this lobby, you are {}",
            newLobbyMember.getUsername(),
            loggedInUser.getUsername());
      }
    }
  }

  /**
   * Handles successful join of user to lobby
   *
   * <p>If a new LobbyJoinSuccessfulResponse object is posted to the EventBus the joined lobby is
   * opened. Furthermore, if the LOG-Level is set to DEBUG the message "New user {@literal
   * <Username>} joined lobby {@literal <lobbyId>}." is displayed in the log.
   *
   * @param message the LobbyJoinSuccessfulResponse object seen on the EventBus
   * @see de.uol.swp.common.lobby.response.LobbyJoinSuccessfulResponse
   */
  @Subscribe
  public void onLobbyJoinSuccessfulResponse(LobbyJoinSuccessfulResponse message) {
    if (!tabClosed) {
      LOG.debug("You ({}) joined lobby {},", message.getUser().getUsername(), message.getLobbyId());
      eventBus.post(
          new ShowLobbyTabEvent(
              message.getLobbyId(), message.getLobbyOptions().getLobbyTitle(), null));
    }
  }

  /**
   * Response when user joins in lobby as spectator.
   *
   * @param message sent by server to spectator who wants to join
   */
  @Subscribe
  public void onLobbyJoinAsSpectatorResponse(LobbyJoinAsSpectatorResponse message) {
    if (!tabClosed) {
      LOG.debug("You ({}) joined lobby {},", message.getUser().getUsername(), message.getLobbyId());
      ShowLobbyTabEvent lobbyTabEvent =
          new ShowLobbyTabEvent(
              message.getLobbyId(), message.getLobbyOptions().getLobbyTitle(), null);
      if (message.getLobbyStatus() == LobbyStatus.INGAME) {
        eventBus.post(new ShowGameTabEvent(lobbyTabEvent.getTab(), message.getLobbyId()));
      } else {
        eventBus.post(lobbyTabEvent);
      }
    }
  }

  /**
   * Handels the UpdateLobbyListMessage found on the Eventbus. It updates the information for all
   * lobbys in the LobbyList.
   *
   * @param message UpdateLobbyListMessage
   */
  @Subscribe
  public void onUpdateLobbyListMessage(UpdateLobbyListMessage message) {
    LOG.debug("Lobbylist update received");
    // Search for the join-code and get the current displayed name of a Lobby
    Platform.runLater(
        () -> {
          Optional<LobbyTableData> lobbyDisplay =
              lobbyTableData.stream()
                  .filter(l -> l.getLobbyId().equals(message.getLobbyId()))
                  .findFirst();
          int lobbyIndex = -1;
          if (lobbyDisplay.isPresent()) {
            lobbyIndex = lobbyTableData.indexOf(lobbyDisplay.get());
          }
          if (lobbyIndex >= 0) {
            lobbyTableData.set(
                lobbyIndex,
                new LobbyTableData(
                    message.getLobbyId(),
                    message.getTitle(),
                    message.getOwnerName(),
                    message.getLoggedInUser(),
                    message.getSlots(),
                    message.getStatus().name(),
                    message.isVariantOne(),
                    message.isVariantTwo(),
                    message.isVariantThree()));
          }
        });
  }

  /**
   * Handles the closing of the tab and ensures that closed tabs do not react to events on the
   * EventBus.
   */
  @FXML
  public void onClose() {
    tabClosed = true;
  }

  /** Is called when pressed on button to change spectatorMode. */
  @FXML
  public void onJoinAsSpectator() {
    if (!Strings.isNullOrEmpty(joinCodeField.getText())) {
      String joinCode = joinCodeField.getText().toUpperCase();
      LOG.debug("JoinCode {} entered.", joinCode);
      if (!joinCode.matches("^[A-Z]{4}$")) {
        // noinspection SpellCheckingInspection
        eventBus.post(new LobbyInfoEvent("JoinCodes sind 4 Buchstaben lang.\nZum Beispiel: ABCD"));
      } else {
        lobbyService.joinAsSpectator(joinCode, loggedInUser.getUser());
      }
    } else {
      // uses selected Item to join Lobby
      ObservableList<LobbyTableData> selectedItems =
          tableView.getSelectionModel().getSelectedItems();
      if (selectedItems.isEmpty()) {
        eventBus.post(new LobbyInfoEvent("Keine Lobby ausgewählt!"));
      } else {
        String lobbyId = selectedItems.get(0).getLobbyId();
        LOG.debug("Lobby {} selected.", lobbyId);
        lobbyService.joinAsSpectator(lobbyId, loggedInUser.getUser());
      }
    }
    joinCodeField.clear();
  }
}
