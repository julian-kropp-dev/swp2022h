package de.uol.swp.client.lobby;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.lobby.events.LobbyInfoEvent;
import de.uol.swp.client.lobby.events.ShowLobbyTabEvent;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.message.LobbyCreatedMessage;
import de.uol.swp.common.lobby.response.LobbyIdResponse;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the LobbyCreationView.
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
@SuppressWarnings("UnstableApiUsage")
public class LobbyCreationPresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/LobbyCreationTab.fxml";
  private static final Logger LOG = LogManager.getLogger(LobbyCreationPresenter.class);
  private static final int MAX_PLAYERS = 8;
  private final boolean[] aiSlidersVisibility = new boolean[MAX_PLAYERS];
  @FXML public CheckBox activeLasers;
  @FXML public CheckBox activeWeakDuplicate;
  @FXML public CheckBox switchOffRoboter;
  @FXML public GridPane mainPane;
  @FXML public AnchorPane aiPane;
  @FXML public Slider ai1;
  @FXML public Slider ai2;
  @FXML public Slider ai3;
  @FXML public Slider ai4;
  @FXML public Slider ai5;
  @FXML public Slider ai6;
  @FXML public HBox hbox0;
  @FXML public HBox hbox1;
  @FXML public HBox hbox2;
  @FXML public HBox hbox3;
  @FXML public HBox hbox4;
  @FXML public HBox hbox5;
  @FXML public HBox hbox6;
  @FXML public Slider aiMain;
  @Inject private LobbyService lobbyService;
  @Inject private LoggedInUser loggedInUser;
  @FXML private ChoiceBox<String> turnTime;
  @FXML private Slider slot;
  @FXML private TextField lobbyTitle;
  @FXML private Slider aiPlayerCount;
  @FXML private Tab lobbyCreationTab;
  @FXML private CheckBox privateLobby;
  @FXML private CheckBox spectatorMode;
  private int called = 0;
  private double previousSlot = 2;
  private double previousAiPlayerCount = 0.0;
  private LobbyOptions lobbyOptions;
  private int[] aiDifficulty = new int[] {1, 1, 1, 1, 1, 1};

  /** Initializes the GUI elements of the application. */
  @FXML
  public void initialize() {
    turnTime.setItems(FXCollections.observableArrayList("20s", "25s", "30s", "35s", "40s", "45s"));
    turnTime.setValue(turnTime.getItems().get(0));
    aiMain.setLabelFormatter(
        new StringConverter<>() {
          @Override
          public String toString(Double object) {
            if (object <= 1) {
              return "Einfach";
            }
            if (object <= 2) {
              return "Mittel";
            }
            if (object <= 3) {
              return "Schwer";
            }
            return "Göttlich";
          }

          @Override
          public Double fromString(String string) {
            switch (string) {
              case "Mittel":
                return 2d;
              case "Schwer":
                return 3d;
              case "Göttlich":
                return 4d;
              case "Einfach":

              default:
                return 1d;
            }
          }
        });
  }

  /**
   * Reacts to button press of the Create Lobby Button, creates new LobbyOptions and initiates a new
   * Lobby.
   *
   * @param actionEvent the event of the button press
   */
  @FXML
  public void onCreateLobby(ActionEvent actionEvent) {
    lobbyOptions = new LobbyOptions();
    lobbyOptions.setPrivateLobby(privateLobby.selectedProperty().getValue());
    lobbyOptions.setSpectatorModeActive(spectatorMode.isSelected());
    lobbyOptions.setActiveLasers(activeLasers.isSelected());
    lobbyOptions.setAiDifficulty(aiDifficulty);
    lobbyOptions.setWeakDuplicatedActive(activeWeakDuplicate.isSelected());
    lobbyOptions.setSwitchOffRoboter(switchOffRoboter.isSelected());
    if (!lobbyOptions.setSlot((int) previousSlot)) {
      eventBus.post(
          new UpdateLobbyOptionsExceptionMessage(
              "Slotbegrenzung muss zwischen 1 " + "und 8 liegen", null));
    } else {
      lobbyOptions.setLobbyTitle(lobbyTitle.getText());
      lobbyOptions.setTurnLimit(turnTime.getValue());
      lobbyOptions.setAiPlayerCount((int) previousAiPlayerCount);
    }
    lobbyService.initiateNewLobby();
    actionEvent.consume();
  }

  /**
   * Handels the LobbyIdResponse on the EventBus. The Method uses the name provided by the server to
   * create a new Lobby via the lobbyService.
   *
   * @param response LobbyIdResponse from the server.
   */
  @Subscribe
  public void onLobbyIdResponse(LobbyIdResponse response) {
    if (called == 0) {
      LOG.debug("Request to create a Lobby with the following options: {}", lobbyOptions);
      lobbyService.createNewLobby(response.getLobbyId(), loggedInUser.getUser(), lobbyOptions);
      called++;
    }
  }

  /**
   * Handels the LobbyCreatedMessage found on the Eventbus. This Method shows the newly created
   * lobby.
   *
   * @param response LobbyCreatedMessage
   */
  @Subscribe
  public void onLobbyCreatedMessage(LobbyCreatedMessage response) {
    if (called == 1) {
      if (loggedInUser.getUser() == null) {
        return;
      }
      if (response.getUser().hashCode() == loggedInUser.getUser().hashCode()) {
        eventBus.post(
            new ShowLobbyTabEvent(
                response.getLobbyId(),
                response.getLobbyOptions().getLobbyTitle(),
                lobbyCreationTab));
        called++;
      }
    }
  }

  /**
   * Reacts to changes on the slider and checks if the value has changed and is within legal bounds.
   */
  @FXML
  public void onSliderChanged() {
    if (slot.getValue() != previousSlot) {
      if (slot.getValue() + aiPlayerCount.getValue() < 2) {
        slot.setValue(previousSlot);
        eventBus.post(
            new LobbyInfoEvent(
                "Wenn du alleine spielen möchtest, musst du "
                    + "mindestens einen AI-Player hinzufügen."));
      }
      if (previousAiPlayerCount > MAX_PLAYERS - slot.getValue()) {
        slot.setValue(previousSlot);
        eventBus.post(
            new LobbyInfoEvent(
                "Die Anzahl der menschlichen und KI Spieler können zusammen "
                    + "nicht mehr als 8 sein."));
      } else {
        previousSlot = slot.getValue();
      }
    }
  }

  /**
   * Reacts to changes on the slider and checks if the selected AiPlayerCount is lower than the
   * lobby slots.
   */
  @FXML
  public void onAiPlayerCountSliderChanged() {
    if (aiPlayerCount.getValue() != previousAiPlayerCount) {
      if (slot.getValue() + aiPlayerCount.getValue() < 2) {
        aiPlayerCount.setValue(previousAiPlayerCount);
        eventBus.post(
            new LobbyInfoEvent(
                "Wenn du alleine spielen möchtest, musst du mindestens "
                    + "einen AI-Player hinzufügen."));
      }
      if (aiPlayerCount.getValue() > MAX_PLAYERS - previousSlot) {
        aiPlayerCount.setValue(previousAiPlayerCount);
        eventBus.post(
            new LobbyInfoEvent(
                "Die Anzahl der menschlichen und KI Spieler können zusammen "
                    + "nicht mehr als 8 sein."));
      } else {
        previousAiPlayerCount = aiPlayerCount.getValue();
      }
    }
  }

  /**
   * This method sets up the UI for configuring Ai players based on the selected numbers of Ai
   * players.
   */
  @FXML
  public void onAiConfigPress() {
    mainPane.setDisable(true);
    MotionBlur motionBlur = new MotionBlur();
    motionBlur.setRadius(12);
    mainPane.setEffect(motionBlur);
    aiPane.setVisible(true);
    aiPane.setDisable(false);

    for (int i = 0; i < MAX_PLAYERS; i++) {
      aiSlidersVisibility[i] = i < aiPlayerCount.getValue();
    }

    ai1.setVisible(aiSlidersVisibility[0]);
    ai2.setVisible(aiSlidersVisibility[1]);
    ai3.setVisible(aiSlidersVisibility[2]);
    ai4.setVisible(aiSlidersVisibility[3]);
    ai5.setVisible(aiSlidersVisibility[4]);
    ai6.setVisible(aiSlidersVisibility[5]);

    hbox1.setVisible(aiSlidersVisibility[0]);
    hbox2.setVisible(aiSlidersVisibility[1]);
    hbox3.setVisible(aiSlidersVisibility[2]);
    hbox4.setVisible(aiSlidersVisibility[3]);
    hbox5.setVisible(aiSlidersVisibility[4]);
    hbox6.setVisible(aiSlidersVisibility[5]);

    hbox0.setVisible(aiPlayerCount.getValue() >= 1);
  }

  /** The exit button for the Ai-Config-Pane. */
  @FXML
  public void onAiConfigExitPress() {
    aiDifficulty =
        new int[] {
          (int) ai1.getValue(),
          (int) ai2.getValue(),
          (int) ai3.getValue(),
          (int) ai4.getValue(),
          (int) ai5.getValue(),
          (int) ai6.getValue()
        };
    mainPane.setDisable(false);
    mainPane.setEffect(null);
    aiPane.setVisible(false);
    aiPane.setDisable(true);
  }

  /**
   * The Main Slider in the Ai-Option-Pane.
   *
   * <p>set the other Slider to the same value
   */
  @FXML
  public void onAiMainSliderChanged() {
    ai1.setValue(aiMain.getValue());
    ai2.setValue(aiMain.getValue());
    ai3.setValue(aiMain.getValue());
    ai4.setValue(aiMain.getValue());
    ai5.setValue(aiMain.getValue());
    ai6.setValue(aiMain.getValue());
  }
}
