package de.uol.swp.client.lobby;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.chat.ClientChatService;
import de.uol.swp.client.game.events.ShowGameTabEvent;
import de.uol.swp.client.lobby.events.LobbyInfoEvent;
import de.uol.swp.client.main.events.ShowMainMenuTabEvent;
import de.uol.swp.client.textureatlas.TextureAtlas;
import de.uol.swp.client.textureatlas.TextureAtlasFloorPlans;
import de.uol.swp.client.textureatlas.TextureAtlasRobots;
import de.uol.swp.common.chat.ChatCommandResponse;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.message.GameStartMessage;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.helper.HelperMethods;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.message.ChangedSpectatorModeMessage;
import de.uol.swp.common.lobby.message.FloorPlansPreviewMessage;
import de.uol.swp.common.lobby.message.KickedUserMessage;
import de.uol.swp.common.lobby.message.SelectedRobotsMessage;
import de.uol.swp.common.lobby.message.UpdatedLobbyOptionsMessage;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.message.UserReadyMessage;
import de.uol.swp.common.lobby.response.LobbyLeaveSuccessfulResponse;
import de.uol.swp.common.lobby.response.LobbyOwnerResponse;
import de.uol.swp.common.lobby.response.ReadyCheckResponse;
import de.uol.swp.common.lobby.response.RetrieveLobbyOptionsResponse;
import de.uol.swp.common.lobby.response.RobotSelectedResponse;
import de.uol.swp.common.user.UserDTO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the LobbyView.
 *
 * @see de.uol.swp.client.AbstractPresenter
 */
@SuppressWarnings("UnstableApiUsage")
public class LobbyPresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/LobbyTab.fxml";
  static final String DIALOG_STYLE_SHEET = "css/confirmationBox.css";
  private static final Logger LOG = LogManager.getLogger(LobbyPresenter.class);
  private static final int MAX_CHECKPOINTS = 6;
  private static final int[] VALID_IDS = new int[] {0, 1, 2, 66, 67, 68, 69};
  private static final int MAX_PLAYERS = 8;
  private final Robots[] robot =
      new Robots[] {
        Robots.BOB,
        Robots.DUSTY,
        Robots.GANDALF,
        Robots.GREGOR,
        Robots.OCEAN,
        Robots.OLIVER,
        Robots.ROCKY,
        Robots.ROSE
      };
  private final EnumMap<Robots, Boolean> robotSelected = new EnumMap<>(Robots.class);
  private final boolean[] aiSlidersVisibility = new boolean[8];
  private final boolean[] checkpointMapVisibility = new boolean[] {false, false, false, false};
  @FXML public VBox checkpointSelection;
  @FXML public Button btnCheckpoint1;
  @FXML public Button btnCheckpoint2;
  @FXML public Button btnCheckpoint3;
  @FXML public Button btnCheckpoint4;
  @FXML public Button btnCheckpoint5;
  @FXML public Button btnCheckpoint6;
  @FXML public Button btnHelpCheckpoints;
  @FXML public Button kickButton;
  @FXML public GridPane gridPaneCheckpoint3;
  @FXML public GridPane gridPaneCheckpoint2;
  @FXML public GridPane gridPaneCheckpoint1;
  @FXML public GridPane gridPaneCheckpoint;
  @FXML public Button btnSwitchMode;
  @FXML public Button btnCheckpointDelete;
  @FXML public StackPane stackPaneCheckpoint;
  @FXML public StackPane stackPaneCheckpoint1;
  @FXML public StackPane stackPaneCheckpoint2;
  @FXML public StackPane stackPaneCheckpoint3;
  LobbyOptions options = new LobbyOptions();
  @Inject private LobbyService lobbyService;
  @Inject private LoggedInUser loggedInUser;
  @Inject private ClientChatService clientChatService;
  @FXML private GridPane mainGrid;
  @FXML private AnchorPane aiPane;
  @FXML private ChoiceBox<String> turnTime;
  @FXML private CheckBox activeLasers;
  @FXML private CheckBox activeWeakDuplicate;
  @FXML private CheckBox switchOffRoboter;
  @FXML private Button btnStart;
  @FXML private Button btnReady;
  @FXML private Slider slot;
  @FXML private Slider aiPlayerCount;
  @FXML private Tab lobbyTab;
  @FXML private ListView<Text> lobbyChat;
  @FXML private TextField messageFieldLobby;
  @FXML private ListView<ColoredUser> userList;
  @FXML private Label lobbyTitleDisplay;
  @FXML private TextField changeLobbyTitle;
  @FXML private Button arrowRight;
  @FXML private Button arrowLeft;
  @FXML private ImageView robotImage;
  @FXML private Button robotSelect;
  @FXML private Text robotName;
  @FXML private VBox configBox;
  @FXML private GridPane configPane;
  @FXML private Slider ai1;
  @FXML private Slider ai2;
  @FXML private Slider ai3;
  @FXML private Slider ai4;
  @FXML private Slider ai5;
  @FXML private Slider ai6;
  @FXML private HBox hbox0;
  @FXML private HBox hbox1;
  @FXML private HBox hbox2;
  @FXML private HBox hbox3;
  @FXML private HBox hbox4;
  @FXML private HBox hbox5;
  @FXML private HBox hbox6;
  @FXML private Slider aiMain;
  @FXML private AnchorPane mapPreviewPane;
  @FXML private ImageView mapChangerImage;
  @FXML private ScrollPane scrollPaneMaps;
  @FXML private Button btnConfirmMap;
  @FXML private GridPane gridMapDepot;
  @FXML private GridPane gridMapSelect;
  @FXML private Button rotateMapUpLeft;
  @FXML private Button rotateMapUpRight;
  @FXML private Button rotateMapDownLeft;
  @FXML private Button rotateMapDownRight;
  @FXML private ImageView planUpLeft;
  @FXML private ImageView planUpRight;
  @FXML private ImageView planDownLeft;
  @FXML private ImageView planDownRight;
  @FXML private Button spectatorModeActive;
  @FXML private HBox spectatorProperties;
  private String lobbyId;
  private boolean tabClosed = false;
  private boolean userReadyStatus = false;
  private boolean mouseOnChat = false;
  private ObservableList<Text> chatLobby;
  private ObservableList<ColoredUser> usernameList;
  private double previousSlot;
  private double previousAiPlayerCount;
  private String previousLobbyTitle;
  private String previousTurnLimit;
  private String previousLobbyOwner;
  private boolean previousLaserActive;
  private int robotCounter;
  private boolean selected = false;
  private int[] previousAiDifficulty = new int[6];
  private int[] aiDifficulty = new int[] {1, 1, 1, 1, 1, 1};
  private boolean initialized = false;
  private boolean isSwitchOffRoboter;
  private boolean isWeakDuplicatedActive;
  private FloorPlanSetting[] floorPlansDepotArray =
      new FloorPlanSetting[] {
        new FloorPlanSetting(FloorPlans.CROSS),
        new FloorPlanSetting(FloorPlans.EXCHANGE),
        new FloorPlanSetting(FloorPlans.ISLAND),
        new FloorPlanSetting(FloorPlans.MAELSTROM),
        new FloorPlanSetting(FloorPlans.CANNERY_ROW),
        new FloorPlanSetting(FloorPlans.PIT_MAZE)
      };
  private FloorPlanSetting[] floorPlansSelectionArray =
      new FloorPlanSetting[] {
        new FloorPlanSetting(FloorPlans.EMPTY),
        new FloorPlanSetting(FloorPlans.EMPTY),
        new FloorPlanSetting(FloorPlans.EMPTY),
        new FloorPlanSetting(FloorPlans.EMPTY)
      };
  private FloorPlanSetting[] floorPlansCommittedArray = floorPlansSelectionArray;
  private boolean floorPlansValid = false;
  private EnumMap<FloorPlans, BufferedImage> floorPlansImageHashMap;
  private GridPane[] gridPaneCheckpointArray;
  private boolean activateEditCheckpoint = false;
  private int activeCheckpointMarker = 1;
  private Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition = new HashMap<>();
  private boolean switchMode = false;
  private List<Button> checkpointButtons = new ArrayList<>();
  private boolean isOwner;
  private List<Robots> selectedRobots = new ArrayList<>();

  /** This method is to set up the view for one lobby. */
  @FXML
  public void initialize() {
    gridPaneCheckpointArray =
        new GridPane[] {
          gridPaneCheckpoint, gridPaneCheckpoint1, gridPaneCheckpoint2, gridPaneCheckpoint3
        };
    checkpointButtons =
        Arrays.asList(
            btnCheckpoint1,
            btnCheckpoint2,
            btnCheckpoint3,
            btnCheckpoint4,
            btnCheckpoint5,
            btnCheckpoint6,
            btnCheckpointDelete);
    initCheckPointsPaneWithButtons();

    scrollPaneMaps
        .prefHeightProperty()
        .bind(mapPreviewPane.heightProperty().subtract(210).divide(2));
    mapPreviewPane
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              scrollPaneMaps.prefHeightProperty().unbind();
              scrollPaneMaps.setPrefHeight((newVal.doubleValue() - 210) / 2);
              scrollPaneMaps
                  .prefHeightProperty()
                  .bind(mapPreviewPane.heightProperty().subtract(210).divide(2));
            });

    for (Node node : gridMapSelect.getChildren()) {
      try {
        if (node instanceof ImageView) {
          ImageView imageView = ((ImageView) node);
          DoubleBinding floorImageSize = mapPreviewPane.heightProperty().subtract(180).divide(2);
          imageView.fitHeightProperty().bind(floorImageSize);
          imageView.fitWidthProperty().bind(floorImageSize);

          mapPreviewPane
              .heightProperty()
              .addListener(
                  (obs, oldVal, newVal) -> {
                    imageView.fitWidthProperty().unbind();
                    imageView.setFitWidth((newVal.doubleValue() - 180) / 2);
                    imageView
                        .fitWidthProperty()
                        .bind(mapPreviewPane.heightProperty().subtract(180).divide(2));
                  });
          mapPreviewPane
              .heightProperty()
              .addListener(
                  (obs, oldVal, newVal) -> {
                    imageView.fitHeightProperty().unbind();
                    imageView.setFitHeight((newVal.doubleValue() - 180) / 2);
                    imageView
                        .fitHeightProperty()
                        .bind(mapPreviewPane.heightProperty().subtract(180).divide(2));
                  });
        }
      } catch (NullPointerException e) {
        LOG.error(e.getMessage());
      }
    }

    floorPlansImageHashMap =
        initializeFloorPlans(
            new FloorPlans[] {
              FloorPlans.CROSS,
              FloorPlans.EXCHANGE,
              FloorPlans.ISLAND,
              FloorPlans.MAELSTROM,
              FloorPlans.CANNERY_ROW,
              FloorPlans.PIT_MAZE
            });
    switchRobotImage(Robots.BOB);
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
    Platform.runLater(this::showMapPreview);
  }

  /** Extracted from initialize. */
  private void initCheckPointsPaneWithButtons() {
    for (GridPane gridPane : gridPaneCheckpointArray) {
      for (int i = 0; i < FloorPlanSetting.FLOOR_PLAN_SIZE; i++) {
        for (int j = 0; j < FloorPlanSetting.FLOOR_PLAN_SIZE; j++) {
          Button button = new Button();
          button.setOnMouseClicked(this::onClickedOnCheckpointMap);
          GridPane.setRowIndex(button, i);
          GridPane.setColumnIndex(button, j);

          ImageView imageViewBlank = new ImageView(TextureAtlas.getAssets(41, 0));
          imageViewBlank.setPreserveRatio(true);

          imageViewBlank.fitHeightProperty().bind(button.heightProperty());
          imageViewBlank.fitWidthProperty().bind(button.heightProperty());
          button.setGraphic(imageViewBlank);
          button.setStyle("-fx-background-color: transparent");

          DoubleBinding buttonSize =
              mapPreviewPane
                  .heightProperty()
                  .subtract(180)
                  .divide(2 * FloorPlanSetting.FLOOR_PLAN_SIZE);
          button.maxHeightProperty().bind(buttonSize);
          button.maxWidthProperty().bind(buttonSize);
          button.minHeightProperty().bind(buttonSize);
          button.minWidthProperty().bind(buttonSize);

          gridPane.getChildren().add(button);
        }
      }
    }
  }

  /**
   * Handels the GameStart found on the Eventbus. It will close the LobbyTab and presents the new
   * GameTab.
   *
   * @param msg GameStartMessage
   */
  @Subscribe
  public void onGameStartMessage(GameStartMessage msg) {
    if (!tabClosed && msg.getGameId().equals(lobbyId)) {
      tabClosed = true;
      eventBus.post(new ShowGameTabEvent(lobbyTab, lobbyId));
    }
  }

  @FXML
  public void onClose() {
    tabClosed = true;
    lobbyService.leaveLobby(lobbyId, loggedInUser.getUser());
  }

  @FXML
  public void onLeave() {
    lobbyService.leaveLobby(lobbyId, loggedInUser.getUser());
    btnReady.setStyle("-fx-background-color: rgba(216,1,0,1);");
  }

  /**
   * Handles successful leave lobby
   *
   * <p>If an LobbyLeaveSuccessfulResponse object is detected on the EventBus this method is called.
   * It posts a new CancelEvent on the eventBus to show the LobbyCreation View and resets the
   * logged-in user of the lobby to null. If the loglevel is set to DEBUG or higher "user left lobby
   * successfully " and the username of the user who left the lobby are written to the log.
   *
   * @param response The LobbyLeaveSuccessfulResponse object detected on the EventBus
   * @see de.uol.swp.client.lobby.LobbyPresenter
   */
  @Subscribe
  public void onLobbyLeaveSuccessfulResponse(LobbyLeaveSuccessfulResponse response) {
    if (!tabClosed && lobbyId.equals(response.getLobbyId())) {
      LOG.debug("User {} left lobby successfully", response.getUser().getUsername());
      if (response.getUser().equals(loggedInUser.getUser())) {
        Platform.runLater(() -> usernameList.clear());
      }
      tabClosed = true;
      eventBus.post(new ShowMainMenuTabEvent(lobbyTab));
    }
  }

  /**
   * Handels the KickedUserMessage found on the EventBus.
   *
   * <p>If the kicked User from the message equals the currently logged-in user in userStore, the
   * view changes to the MainMenu and a warning gets show.
   *
   * @param message KickedUserMessage on the EventBus.
   */
  @Subscribe
  public void onKickedUserMessage(KickedUserMessage message) {
    if (!tabClosed) {
      LOG.debug("User {} was kicked!", message.getUser().getUsername());
      if (message.getUser().equals(loggedInUser.getUser())) {
        LOG.debug("You ({}) were kicked!", message.getUser().getUsername());

        eventBus.post(new ShowMainMenuTabEvent(lobbyTab));
        eventBus.post(new LobbyInfoEvent("Du wurdest aus der Lobby gekickt!"));
      }
    }
  }

  /**
   * This Method handels the kick button.
   *
   * <p>It gets the selected User from the userList. Before kicking the selected User the Owner
   * confirms that he actually wants to kick that User.
   */
  public void kick() {
    ObservableList<ColoredUser> auswahl = userList.getSelectionModel().getSelectedItems();
    if (auswahl.isEmpty()) {
      eventBus.post(
          new LobbyInfoEvent("Um einen Spieler zu kicken, musst du ihn in der Liste auswählen!"));
    } else {
      String username = auswahl.get(0).getUsername();
      if (loggedInUser.getUser().getUsername().equals(username)) {
        eventBus.post(new LobbyInfoEvent("Du kannst dich nicht selber kicken"));
        userList.getSelectionModel().clearSelection();
      } else {
        Platform.runLater(
            () -> {
              ButtonType buttonTypeYes = new ButtonType("Ja");
              ButtonType buttonTypeNo = new ButtonType("Nein");
              Alert alert =
                  new Alert(
                      Alert.AlertType.CONFIRMATION,
                      "Möchtest du "
                          + username
                          + " wirklich kicken? \n"
                          + "Dieser Schritt kann nicht rückgängig gemacht werden!");
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
                LOG.debug("Kicking User {}", username);
                lobbyService.kickUser(lobbyId, username);
                userList.getSelectionModel().clearSelection();
              } else {
                alert.close();
                userList.getSelectionModel().clearSelection();
              }
            });
      }
    }
  }

  /**
   * Method called when the ready button is pressed
   *
   * <p>if the ready button is pressed, this method requests the lobby service to set ready status.
   * Therefore, it uses the name of the lobby one is in and the loggedInUser.
   */
  @FXML
  public void onReadyButtonPressed() {
    lobbyService.sendReadySignal(lobbyId, loggedInUser.getUser());
    if (userReadyStatus) {
      LOG.debug("User {} set to not ready", loggedInUser.getUsername());
    } else {
      LOG.debug("User {} set to ready", loggedInUser.getUsername());
    }
  }

  /**
   * Catches UserReadyMessages to change the color of the readyButton equivalent to the readyStatus.
   *
   * @param message message after the readyStatus is successfully changed
   * @see UserReadyMessage
   */
  @Subscribe
  @SuppressWarnings("java:S1192")
  public void onUserReadyMessage(UserReadyMessage message) {
    if (!tabClosed && (message.getLobbyId().equals(lobbyId))) {
      if (message.getUser().equals(loggedInUser.getUser())) {
        if (message.getReady()) {
          btnReady.setStyle("-fx-background-color: #00C108;");
          robotSelect.setVisible(false);
        } else {
          btnReady.setStyle("-fx-background-color: #D80100;");
          robotSelect.setVisible(true);
        }
      }

      Color color;
      if (message.getReady()) {
        color = Color.GREEN;
      } else {
        color = Color.BLACK;
      }
      setUserColor(message.getUser().getUsername(), color);
      userReadyStatus = message.getReady();
      onSendReadyCheck(lobbyId, loggedInUser.getUser());
    }
  }

  private void setUserColor(String username, Color color) {
    for (ColoredUser user : usernameList) {
      if (user.getUsername().equals(username)) {
        Platform.runLater(
            () -> {
              usernameList.remove(user);
              usernameList.add(new ColoredUser(username, color));
            });
        break;
      }
    }
  }

  private void deleteUserColor(String username) {
    for (ColoredUser user : usernameList) {
      if (user.getUsername().equals(username)) {
        Platform.runLater(() -> usernameList.remove(user));
        break;
      }
    }
  }

  private boolean searchUserInColonList(String username) {
    for (ColoredUser user : usernameList) {
      if (user.getUsername().equals(username)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sends a Signal to check if all players in the lobby are ready.
   *
   * @param lobbyId the lobby in which should be checked.
   * @param user who wants it to be checked.
   */
  public void onSendReadyCheck(String lobbyId, UserDTO user) {
    lobbyService.sendReadyCheck(lobbyId, user);
  }

  /**
   * Catches the ReadyCheckResponse so the Start button can be turned visible or invisible, if all
   * players are ready or not.
   *
   * @param response ReadyCheckResponse
   */
  @Subscribe
  public void onReadyCheckResponse(ReadyCheckResponse response) {
    if (!tabClosed
        && response.getLobbyId().equals(lobbyId)
        && response.getOwner().equals(loggedInUser.getUser())) {
      btnStart.setVisible(response.getCheck());
    }
  }

  /**
   * This Method syncs the changes from the robot selection from the server to clients.
   *
   * @param msg SelectedRobotsMessage
   */
  @Subscribe
  public void onSelectedRobotsMessage(SelectedRobotsMessage msg) {
    if (!tabClosed && msg.getLobbyId().equals(lobbyId)) {
      selectedRobots = msg.getRobotsSelected();
      if (!selected) {
        switchRobotImage(robot[robotCounter]);
      }
    }
  }

  /**
   * This Method syncs the changes from the stored LobbyOption from the server to clients. It is the
   * first method called in a new lobby after creation. The previous value for one option get set to
   * the value from lobbyoption.
   *
   * @param response RetrieveLobbyOptionsResponse
   */
  @Subscribe
  @SuppressWarnings("java:S3776")
  public void onRetrieveLobbyOptionsResponse(RetrieveLobbyOptionsResponse response) {
    if (!tabClosed) {
      lobbyId = lobbyTab.getText().replace("Lobby ", "");
      if (lobbyId.equals(response.getLobbyId())) {
        options = response.getOptions();
        // lower left settings
        previousAiPlayerCount = options.getAiPlayerCount();
        spectatorProperties.setVisible(options.isSpectatorModeActive());
        spectatorProperties.setDisable(!options.isSpectatorModeActive());
        setSpectatorVisibility(response.getSpectators().contains(loggedInUser.getUser()));
        aiPlayerCount.setValue(previousAiPlayerCount);
        previousSlot = options.getSlot();
        slot.setValue(previousSlot);
        if (turnTime.getItems().isEmpty()) {
          turnTime.setItems(
              FXCollections.observableArrayList("20s", "25s", "30s", "35s", "40s", "45s"));
        }
        // right upper settings
        final String title;
        if (options.getLobbyTitle() == null || options.getLobbyTitle().isEmpty()) {
          title = lobbyId;
        } else {
          title = options.getLobbyTitle();
        }

        Platform.runLater(
            () -> {
              lobbyTitleDisplay.setText(title);
              turnTime.setValue(options.getTurnLimitString());
            });

        previousTurnLimit = options.getTurnLimitString();
        previousLobbyTitle = options.getLobbyTitle();
        previousLaserActive = options.isActiveLasers();
        activeLasers.setSelected(previousLaserActive);
        activeWeakDuplicate.setSelected(options.isWeakDuplicatedActive());
        isWeakDuplicatedActive = options.isWeakDuplicatedActive();
        switchOffRoboter.setSelected(options.isSwitchOffRoboter());
        isSwitchOffRoboter = options.isSwitchOffRoboter();
        // AI settings
        previousAiDifficulty = options.getAiDifficulty();
        aiDifficulty = options.getAiDifficulty();
        ai1.setValue(aiDifficulty[0]);
        ai2.setValue(aiDifficulty[1]);
        ai3.setValue(aiDifficulty[2]);
        ai4.setValue(aiDifficulty[3]);
        ai5.setValue(aiDifficulty[4]);
        ai6.setValue(aiDifficulty[5]);
        lobbyService.sendLobbyOwnerRequest(this.lobbyId, this.loggedInUser.getUser());
        updateChatList();
        initializeUserList();
        Platform.runLater(
            () ->
                response.getUsers().parallelStream()
                    .filter(t -> !t.getUUID().equals(loggedInUser.getUser().getUUID()))
                    .filter(t -> !response.getSpectators().contains(t))
                    .forEach(
                        t -> {
                          Color color = Color.BLACK;
                          if (Boolean.TRUE.equals(response.getReady().get(t))) {
                            color = Color.GREEN;
                          }
                          usernameList.add(new ColoredUser(t.getUsername(), color));
                        }));

        for (Robots robo : Robots.values()) {
          robotSelected.put(robo, false);
        }
        initialized = true;
        checkpointsPosition = response.getOptions().getCheckpointsPosition();

        if (response.getSpectators().contains(loggedInUser.getUser())) {
          setSpectatorVisibility(response.getSpectators().contains(loggedInUser.getUser()));
        }
        floorPlansCommittedArray = response.getOptions().getFloorPlanSettings();
        floorPlansSelectionArray = floorPlansCommittedArray;
        Platform.runLater(this::showMapPreview);
        lobbyService.sendSelectedRobotsRequest(this.lobbyId);
      }
    }
  }

  /**
   * This method handles the UpdatedLobbyOptionsMessage found on the eventbus. It syncs all changes.
   * And updates the GUI. The previous value for one option get set to the new value.
   *
   * @param msg UpdatedLobbyOptionsMessage
   */
  @Subscribe
  public void onUpdatedLobbyOptionsMessage(UpdatedLobbyOptionsMessage msg) {
    if (!tabClosed && msg.getLobbyId().equals(lobbyId)) {
      options = msg.getOptions();
      // lower left settings
      previousAiPlayerCount = options.getAiPlayerCount();
      aiPlayerCount.setValue(previousAiPlayerCount);
      previousSlot = options.getSlot();
      slot.setValue(previousSlot);
      Platform.runLater(() -> turnTime.setValue(options.getTurnLimitString()));
      previousTurnLimit = options.getTurnLimitString();
      // right upper settings
      Platform.runLater(() -> lobbyTitleDisplay.setText(options.getLobbyTitle()));
      previousLobbyTitle = options.getLobbyTitle();
      previousLaserActive = options.isActiveLasers();
      activeLasers.setSelected(previousLaserActive);
      isSwitchOffRoboter = options.isSwitchOffRoboter();
      switchOffRoboter.setSelected(isSwitchOffRoboter);
      isWeakDuplicatedActive = options.isWeakDuplicatedActive();
      activeWeakDuplicate.setSelected(isWeakDuplicatedActive);
      // AI settings
      previousAiDifficulty = options.getAiDifficulty();
      aiDifficulty = options.getAiDifficulty();
      ai1.setValue(aiDifficulty[0]);
      ai2.setValue(aiDifficulty[1]);
      ai3.setValue(aiDifficulty[2]);
      ai4.setValue(aiDifficulty[3]);
      ai5.setValue(aiDifficulty[4]);
      ai6.setValue(aiDifficulty[5]);
      floorPlansCommittedArray = msg.getOptions().getFloorPlanSettings();
      checkpointsPosition = msg.getOptions().getCheckpointsPosition();
      if (LOG.isDebugEnabled()) {
        LOG.debug("New LobbyOptions {}", options.toText());
      }
      if (!activateEditCheckpoint) {
        refreshCheckpointGui();
      }
    }
  }

  /**
   * This method handles the UpdateLobbyOptionsExceptionMessage it rolls back the potential invalid
   * inputs from the user to the last valid one.
   *
   * @param msg UpdateLobbyOptionsExceptionMessage
   */
  @Subscribe
  public void onUpdateLobbyOptionsExceptionMessage(UpdateLobbyOptionsExceptionMessage msg) {
    if (!tabClosed && msg.getLobbyId().equals(lobbyId)) {
      // reset lower left settings
      aiPlayerCount.setValue(previousAiPlayerCount);
      slot.setValue(previousSlot);
      Platform.runLater(() -> turnTime.setValue(previousTurnLimit));
      // reset right upper settings
      Platform.runLater(() -> lobbyTitleDisplay.setText(previousLobbyTitle));
      activeLasers.setSelected(previousLaserActive);
      switchOffRoboter.setSelected(isSwitchOffRoboter);
      activeWeakDuplicate.setSelected(isWeakDuplicatedActive);
      // reset AI settings
      System.arraycopy(previousAiDifficulty, 0, aiDifficulty, 0, 6);
      ai1.setValue(aiDifficulty[0]);
      ai2.setValue(aiDifficulty[1]);
      ai3.setValue(aiDifficulty[2]);
      ai4.setValue(aiDifficulty[3]);
      ai5.setValue(aiDifficulty[4]);
      ai6.setValue(aiDifficulty[5]);
    }
  }

  /** This Methods shall be called whenever a valid User Input happened. */
  private void updateLobbyOption(
      int newSlot,
      String newLobbyTitle,
      String newTurnLimit,
      boolean activeLasers,
      int newAiPlayerCount,
      boolean isWeakDuplicatedActive,
      boolean isSwitchOffRoboter) {
    lobbyService.updateLobbyOptions(
        options,
        lobbyId,
        loggedInUser.getUser(),
        newSlot,
        newLobbyTitle,
        newTurnLimit,
        activeLasers,
        newAiPlayerCount,
        aiDifficulty,
        isWeakDuplicatedActive,
        isSwitchOffRoboter,
        floorPlansSelectionArray,
        checkpointsPosition);
  }

  // GUI Buttons for Setting

  /** Reacts to changes of the Checkbox for activeLasers. */
  @FXML
  public void checkBoxActiveLasers() {
    updateLobbyOption(
        (int) previousSlot,
        previousLobbyTitle,
        previousTurnLimit,
        activeLasers.isSelected(),
        (int) previousAiPlayerCount,
        isWeakDuplicatedActive,
        isSwitchOffRoboter);
  }

  /**
   * Reacts to changes of the slider and checks if the value changed and updates the Options of the
   * current lobby.
   */
  @FXML
  public void onSliderChanged() {
    if (slot.getValue() != previousSlot) {
      if (slot.getValue() + aiPlayerCount.getValue() < 2) {
        slot.setValue(previousSlot);
        eventBus.post(
            new LobbyInfoEvent(
                "Wenn du alleine spielen möchtest, musst du mindestens einen "
                    + "AI-Player hinzufügen."));
      }
      if (slot.getValue() > MAX_PLAYERS - previousAiPlayerCount) {
        slot.setValue(previousSlot);
        eventBus.post(
            new LobbyInfoEvent(
                "Die Anzahl der menschlichen und KI Spieler können zusammen "
                    + "nicht mehr als 8 sein."));
      } else {
        updateLobbyOption(
            (int) slot.getValue(),
            previousLobbyTitle,
            previousTurnLimit,
            previousLaserActive,
            (int) previousAiPlayerCount,
            isWeakDuplicatedActive,
            isSwitchOffRoboter);
      }
    }
  }

  /** Reacts to changes of the AiPlayerCount slider and updates the Options of the current lobby. */
  @FXML
  public void onAiPlayerCountSliderChanged() {
    if (aiPlayerCount.getValue() != options.getAiPlayerCount()) {
      if (slot.getValue() + aiPlayerCount.getValue() < 2) {
        aiPlayerCount.setValue(previousSlot);
        eventBus.post(
            new LobbyInfoEvent(
                "Wenn du alleine spielen möchtest, musst du mindestens einen "
                    + "AI-Player hinzufügen."));
      }
      if (aiPlayerCount.getValue() > MAX_PLAYERS - previousSlot) {
        aiPlayerCount.setValue(previousAiPlayerCount);
        eventBus.post(
            new LobbyInfoEvent(
                "Die Anzahl der menschlichen und KI Spieler können zusammen "
                    + "nicht mehr als 8 sein."));
      } else {
        updateLobbyOption(
            (int) previousSlot,
            previousLobbyTitle,
            previousTurnLimit,
            previousLaserActive,
            (int) aiPlayerCount.getValue(),
            isWeakDuplicatedActive,
            isSwitchOffRoboter);
      }
    }
  }

  /** This method is called when I press the button to change the lobby title. */
  @FXML
  public void onLobbyTitleChange() {
    if (!(changeLobbyTitle.getText().equals(""))) {
      updateLobbyOption(
          (int) previousSlot,
          changeLobbyTitle.getText(),
          previousTurnLimit,
          previousLaserActive,
          (int) previousAiPlayerCount,
          isWeakDuplicatedActive,
          isSwitchOffRoboter);
      changeLobbyTitle.clear();
    }
  }

  /**
   * Reacts to changes in the ChoiceBox turnTime. It checks if the value changed and updated the
   * lobbyOptions.
   */
  @FXML
  public void onTurnTimeChange() {
    if ((turnTime.getItems() != null || !turnTime.getValue().isEmpty())
        && initialized
        && !turnTime.getValue().equals(previousTurnLimit)) {
      updateLobbyOption(
          (int) previousSlot,
          previousLobbyTitle,
          turnTime.getValue(),
          previousLaserActive,
          (int) previousAiPlayerCount,
          isWeakDuplicatedActive,
          isSwitchOffRoboter);
    }
  }

  /**
   * Shows the lobby configurations that only the lobby owner is supposed to see and isOwner is
   * 'true' when current user is owner, otherwise 'false'. Then the visibility is set according to
   * the boolean value isOwner.
   *
   * <p>Set visibility for newer functionalities for LobbyOwner here!
   *
   * @param lobbyId lobbyId is the name of the selected lobby
   * @param owner the owner will be compared with the loggedInUser
   */
  public void setOwnerVisibility(String lobbyId, UserDTO owner) {
    if (this.lobbyId.equals(lobbyId)) {
      isOwner = loggedInUser.getUser().getUsername().equals(owner.getUsername());
      activateEditCheckpoint = isOwner;
      configBox.setDisable(!isOwner);
      kickButton.setVisible(isOwner);
      configPane.setDisable(!isOwner);
      btnConfirmMap.setVisible(isOwner);
      scrollPaneMaps.setVisible(isOwner);
      stackPaneCheckpoint.setVisible(!isOwner);
      stackPaneCheckpoint.setDisable(!isOwner);
      stackPaneCheckpoint1.setVisible(!isOwner);
      stackPaneCheckpoint1.setDisable(!isOwner);
      stackPaneCheckpoint2.setVisible(!isOwner);
      stackPaneCheckpoint2.setDisable(!isOwner);
      stackPaneCheckpoint3.setVisible(!isOwner);
      stackPaneCheckpoint3.setDisable(!isOwner);
      spectatorModeActive.setVisible(!isOwner);
      spectatorModeActive.setDisable(isOwner);
      btnSwitchMode.setVisible(isOwner);
      if (!activateEditCheckpoint) {
        refreshCheckpointGui();
      }
    }
  }

  /**
   * Uses the method setOwnerVisibility.
   *
   * <p>Checks if there was a different previous LobbyOwner. If there is, the new LobbyOwner
   * receives a LobbyInfoEvent that he is the new LobbyOwner. The attribute previousLobbyOwner is
   * needed, because without it the LobbyOwner will always get a Warning/Info when a User leaves the
   * Lobby, even though the User wasn't the LobbyOwner before leaving. So the name of the LobbyOwner
   * needs to be stored in previousLobbyOwner.
   *
   * @param response response from the LobbyOwnerRequest
   */
  @Subscribe
  public void onLobbyOwnerResponse(LobbyOwnerResponse response) {
    if (!tabClosed) {
      lobbyOwnerChange(response.getLobbyId(), response.getOwner());
    }
  }

  /**
   * Uses the method setOwnerVisibility.
   *
   * <p>Checks if there was a different previous LobbyOwner. If there is, the new LobbyOwner
   * receives a LobbyInfoEvent that he is the new LobbyOwner. The attribute previousLobbyOwner is
   * needed, because without it the LobbyOwner will always get a Warning/Info when a User leaves the
   * Lobby, even though the User wasn't the LobbyOwner before leaving. So the name of the LobbyOwner
   * needs to be stored in previousLobbyOwner.
   */
  private void lobbyOwnerChange(String lobbyId, UserDTO owner) {
    setOwnerVisibility(lobbyId, owner);
    String currentLobbyOwner = owner.getUsername();
    if (previousLobbyOwner == null) {
      previousLobbyOwner = currentLobbyOwner;
    } else if (!currentLobbyOwner.equals(previousLobbyOwner)
        && this.lobbyId.equals(lobbyId)
        && loggedInUser.getUser().getUsername().equals(currentLobbyOwner)) {
      floorPlansCommittedArray = floorPlansSelectionArray;
      String joinCode = " [" + lobbyId + "]";
      if (joinCode.equals(" [" + lobbyTitleDisplay.getText() + "]")) {
        joinCode = "";
      }
      eventBus.post(
          new LobbyInfoEvent(
              "Du wurdest in der Lobby "
                  + "'"
                  + lobbyTitleDisplay.getText()
                  + "'"
                  + joinCode
                  + " zum neuen Besitzer ernannt!"));
      floorPlansDepotArray =
          new FloorPlanSetting[] {
            new FloorPlanSetting(FloorPlans.CROSS),
            new FloorPlanSetting(FloorPlans.EXCHANGE),
            new FloorPlanSetting(FloorPlans.ISLAND),
            new FloorPlanSetting(FloorPlans.MAELSTROM),
            new FloorPlanSetting(FloorPlans.CANNERY_ROW),
            new FloorPlanSetting(FloorPlans.PIT_MAZE)
          };
      floorPlansSelectionArray =
          new FloorPlanSetting[] {
            new FloorPlanSetting(FloorPlans.EMPTY),
            new FloorPlanSetting(FloorPlans.EMPTY),
            new FloorPlanSetting(FloorPlans.EMPTY),
            new FloorPlanSetting(FloorPlans.EMPTY)
          };
      floorPlansValid = false;
      previousLobbyOwner = currentLobbyOwner;
    }
  }

  // User List
  @SuppressWarnings({"checkstyle:Indentation", "java:S110"})
  private void initializeUserList() {
    Platform.runLater(
        () -> {
          usernameList = FXCollections.observableArrayList();
          userList.setItems(usernameList);

          String username = loggedInUser.getUser().getUsername();
          Color userColor = Color.BLACK;

          ColoredUser coloredUser = new ColoredUser(username, userColor);
          usernameList.add(coloredUser);

          //noinspection CheckStyle
          userList.setCellFactory(
              listView ->
                  new ListCell<>() {
                    @Override
                    protected void updateItem(ColoredUser user, boolean empty) {
                      super.updateItem(user, empty);
                      if (empty || user == null) {
                        setText(null);
                        setStyle(null);
                      } else {
                        setText(user.getUsername());
                        setStyle("-fx-text-fill: " + toHex(user.getColor()));
                      }
                    }
                  });
        });
  }

  private String toHex(Color color) {
    String red = Integer.toHexString((int) (color.getRed() * 255));
    String green = Integer.toHexString((int) (color.getGreen() * 255));
    String blue = Integer.toHexString((int) (color.getBlue() * 255));
    return "#" + padZero(red) + padZero(green) + padZero(blue);
  }

  private String padZero(String hex) {
    return hex.length() == 1 ? "0" + hex : hex;
  }

  /**
   * Removes the User who leaves from the userList in the LobbyView.
   *
   * @param msg the UserLeftLobbyMessage object seen on the EventBus
   * @see de.uol.swp.common.lobby.message.UserLeftLobbyMessage
   */
  @Subscribe
  public void onUserLeftLobbyMessage(UserLeftLobbyMessage msg) {
    if (!tabClosed) {
      if (msg.getLobbyId().equals(lobbyId)) {
        deleteUserColor(msg.getUser().getUsername());
      }
      onSendReadyCheck(lobbyId, loggedInUser.getUser());
      lobbyService.sendLobbyOwnerRequest(this.lobbyId, this.loggedInUser.getUser());
      lobbyService.sendSelectedRobotsRequest(this.lobbyId);
    }
  }

  // Chat

  private void updateChatList() {
    Platform.runLater(
        () -> {
          if (chatLobby == null) {
            chatLobby = FXCollections.observableArrayList();
            lobbyChat.setItems(chatLobby);
            lobbyChat.setCellFactory(
                stringListView -> {
                  LobbyChatCell textListCell = new LobbyChatCell();
                  textListCell.setMaxHeight(1000);
                  textListCell.setMaxWidth(lobbyChat.getMinWidth() - 15);
                  textListCell.setWrapText(true);
                  return textListCell;
                });
          }
        });
  }

  /**
   * This method updates the lobby chat when a new message appears from the server.
   *
   * @param chatMessageMessage The message from server.
   */
  @Subscribe
  private void onChatMessageMessage(ChatMessageMessage chatMessageMessage) {
    if (!tabClosed && chatMessageMessage.getChatMessage().getLobbyId().equals(lobbyId)) {
      Text text = new Text(chatMessageMessage.getChatMessage().toString());
      text.setWrappingWidth(lobbyChat.getMinWidth() - 15);
      Platform.runLater(
          () -> {
            chatLobby.add(text);
            if (!mouseOnChat) {
              lobbyChat.scrollTo(lobbyChat.getItems().size() - 1);
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
    if (!tabClosed
        && msg.getChatMassage().getLobbyId().equals(lobbyId)
        && loggedInUser.getUser() != null) {
      Text text = new Text(msg.getChatMassage().toString());
      text.setWrappingWidth(lobbyChat.getMinWidth() - 15);
      Platform.runLater(
          () -> {
            chatLobby.add(text);
            if (!mouseOnChat) {
              lobbyChat.scrollTo(lobbyChat.getItems().size() - 1);
            }
          });
    }
  }

  /**
   * Method called when the send button is pressed
   *
   * <p>If the send button is pressed, this method request the ClientChatService to send the server
   * a new Message request. If there is no text in the text field, no Message will be written.
   *
   * @see de.uol.swp.client.chat.ClientChatService
   * @since 2022-12-06
   */
  @FXML
  public void onSend() {
    if (!messageFieldLobby.getText().isBlank()) {
      clientChatService.sendMessage(messageFieldLobby.getText(), lobbyId);
      messageFieldLobby.clear();
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
  public void onEnter() {
    if (!messageFieldLobby.getText().isBlank()) {
      clientChatService.sendMessage(messageFieldLobby.getText(), lobbyId);
      messageFieldLobby.clear();
    }
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
    lobbyChat.scrollTo(lobbyChat.getItems().size() - 1);
  }

  /**
   * Reacts to changes in the CheckBox switchOffRoboter. It checks if the value changed and updated
   * the lobbyOptions.
   */
  @FXML
  public void checkBoxSwitchOffRoboter(ActionEvent event) {
    updateLobbyOption(
        (int) previousSlot,
        previousLobbyTitle,
        previousTurnLimit,
        previousLaserActive,
        (int) previousAiPlayerCount,
        isWeakDuplicatedActive,
        switchOffRoboter.isSelected());
    event.consume();
  }

  /**
   * Reacts to changes in the CheckBox activeWeakDuplicate. It checks if the value changed and
   * updated the lobbyOptions.
   */
  @FXML
  public void checkBoxActiveWeakDuplicate(ActionEvent event) {
    updateLobbyOption(
        (int) previousSlot,
        previousLobbyTitle,
        previousTurnLimit,
        previousLaserActive,
        (int) previousAiPlayerCount,
        activeWeakDuplicate.isSelected(),
        isSwitchOffRoboter);
    event.consume();
  }

  /** Sends a Start Signal after the Start button is pressed. */
  @FXML
  public void onStartSignal() {
    lobbyService.sendStartSignal(lobbyId);
  }

  /**
   * Method to handle UserJoinedLobbyMessage found on the Eventbus. If a UserJoinedLobbyMessage is
   * seen on the EventBus the Username gets added to the ObservableArrayList users, which should
   * have the Consequence that the ListView in the GUI also gets updated
   *
   * @param msg the UserJoinedMessage seen on the Eventbus
   */
  @Subscribe
  public void onUserJoinedLobbyMessage(UserJoinedLobbyMessage msg) {
    if (!tabClosed) {
      Platform.runLater(
          () -> {
            if (usernameList != null
                && !searchUserInColonList(msg.getUser().getUsername())
                && msg.getLobbyId().equals(lobbyId)) {
              usernameList.add(new ColoredUser(msg.getUser().getUsername(), Color.BLACK));
            }
          });
      if (msg.getLobbyId().equals(lobbyId)) {
        btnStart.setVisible(false);
      }
    }
  }

  /**
   * Method to change Robotimage so the player can go through all possible Robots scrolls forward.
   */
  @FXML
  public void onArrowRight() {
    this.robotCounter++;
    if (robotCounter > 7) {
      robotCounter = 0;
    }
    switchRobotImage(robot[robotCounter]);
    if (Boolean.TRUE.equals(robotSelected.get(robot[robotCounter]))) {
      this.robotCounter++;
    }
  }

  /**
   * Method to change Robotimage so the player can go through all possible Robots scrolls backward.
   */
  @FXML
  public void onArrowLeft() {
    this.robotCounter--;
    if (robotCounter < 0) {
      robotCounter = 7;
    }
    switchRobotImage(robot[robotCounter]);
    if (Boolean.TRUE.equals(robotSelected.get(robot[robotCounter]))) {
      this.robotCounter--;
    }
  }

  /** Method to change robot image. */
  private void switchRobotImage(Robots robot) {
    robotName.setText(
        robot.name().substring(0, 1).toUpperCase() + robot.name().substring(1).toLowerCase());
    if (selectedRobots.stream().anyMatch(t -> t.name().equals(robot.name()))) {
      Image image = TextureAtlasRobots.getRobotImage(robot.name());
      PixelReader pixelReader;
      if (image != null) {
        pixelReader = image.getPixelReader();
        WritableImage writableImage =
            new WritableImage(pixelReader, (int) image.getWidth(), (int) image.getHeight());
        robotImage.setImage(TextureAtlas.toGrayscale(writableImage));
      }
    } else {
      robotImage.setImage(TextureAtlasRobots.getRobotImage(robot.name()));
    }
  }

  @FXML
  public void onSelect() {
    lobbyService.sendRobotSelectionRequest(lobbyId, robot[robotCounter], loggedInUser.getUser());
  }

  /**
   * Catches RobotSelectedResponse so the view can be updated and the robot cant be claimed by other
   * players.
   */
  @Subscribe
  public void onRobotSelectedResponse(RobotSelectedResponse response) {
    if (!tabClosed
        && response.getLobbyId().equals(lobbyId)
        && response.getUser().equals(loggedInUser.getUser())) {
      if (Boolean.FALSE.equals(response.getAlreadyClaimed())) {
        if (Boolean.FALSE.equals(selected)) {
          if (getNumberMaps() != 0 && isOwner) {
            btnReady.setVisible(true);
          } else {
            btnReady.setVisible(!isOwner);
          }
          robotSelect.setStyle("-fx-background-color: Green");
          robotSelected.put(response.getStyle(), true);
          arrowLeft.setVisible(false);
          arrowRight.setVisible(false);
          selected = true;
        } else {
          robotSelected.put(response.getStyle(), false);
          robotSelect.setStyle("-fx-background-color: rgba(216,1,0,1)");
          arrowLeft.setVisible(true);
          arrowRight.setVisible(true);
          btnReady.setVisible(false);
          selected = false;
        }
      } else {
        eventBus.post(new LobbyInfoEvent("Dieser Roboter gehört schon einem anderen Spieler!"));
      }
    }
  }

  /**
   * This method sets up the UI for configuring AI players based on the selected numbers of Ai
   * players.
   */
  @FXML
  public void onAiConfigPress() {
    mainGrid.setDisable(true);
    MotionBlur motionBlur = new MotionBlur();
    motionBlur.setRadius(12);
    mainGrid.setEffect(motionBlur);
    aiPane.setVisible(true);
    aiPane.setDisable(false);

    for (int i = 0; i < MAX_PLAYERS; i++) {
      aiSlidersVisibility[i] = i < aiPlayerCount.getValue();
    }

    boolean mainSlider = true;

    for (int value : aiDifficulty) {
      if (value != aiDifficulty[0]) {
        mainSlider = false;
        break;
      }
    }

    if (mainSlider) {
      aiMain.setValue(aiDifficulty[0]);
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

  /** The Exitbutton for the Ai-Config-Pane. */
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
    mainGrid.setDisable(false);
    mainGrid.setEffect(null);
    aiPane.setVisible(false);
    aiPane.setDisable(true);

    updateLobbyOption(
        (int) previousSlot,
        previousLobbyTitle,
        previousTurnLimit,
        previousLaserActive,
        (int) previousAiPlayerCount,
        isWeakDuplicatedActive,
        isSwitchOffRoboter);
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

  /** When clicked on the map, this method is called and shows a map editor. */
  @FXML
  public void onMapChange() {
    mainGrid.setDisable(true);
    MotionBlur motionBlur = new MotionBlur();
    motionBlur.setRadius(12);
    mainGrid.setEffect(motionBlur);
    mapPreviewPane.setVisible(true);
    mapPreviewPane.setDisable(false);
  }

  /**
   * Called when there is a drag action on the element.
   *
   * @param mouseEvent for the drag
   */
  @FXML
  public void onDragDetectedMap(MouseEvent mouseEvent) {
    ImageView view = (ImageView) mouseEvent.getSource();
    int index;

    if (GridPane.getColumnIndex(view) == null && GridPane.getRowIndex(view) == null) {
      index = 0;
    } else if (GridPane.getColumnIndex(view) == null) {
      index = GridPane.getRowIndex(view);
    } else if (GridPane.getRowIndex(view) == null) {
      index = GridPane.getColumnIndex(view);
    } else {
      index = GridPane.getColumnIndex(view) + GridPane.getRowIndex(view) + 1;
    }

    if ((!isInGridPaneDepot(view) || index < floorPlansDepotArray.length)
        && !switchMode
        && isOwner) {
      WritableImage newImg = view.snapshot(new SnapshotParameters(), null);
      Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
      db.setDragView(newImg);
      ClipboardContent content = new ClipboardContent();
      content.putString(view.getId());
      db.setContent(content);
      mouseEvent.consume();
    }
  }

  /**
   * Called when there is a drag hover action on the item.
   *
   * <p>Check if the Droppable Item a Valid item from the two GridPanes.
   */
  @FXML
  public void onDragOverMap(DragEvent dragEvent) {
    ImageView source = (ImageView) dragEvent.getSource();
    int index;
    if (GridPane.getColumnIndex(source) == null && GridPane.getRowIndex(source) == null) {
      index = 0;
    } else if (GridPane.getColumnIndex(source) == null) {
      index = GridPane.getRowIndex(source);
    } else if (GridPane.getRowIndex(source) == null) {
      index = GridPane.getColumnIndex(source);
    } else {
      index = GridPane.getColumnIndex(source) + GridPane.getRowIndex(source) + 1;
    }
    if (dragEvent.getDragboard().hasString()
        && (isInGridPaneSelect(source) || isInGridPaneDepot(source))
        && (!isInGridPaneDepot(source) || index < floorPlansDepotArray.length)) {
      dragEvent.acceptTransferModes(TransferMode.MOVE);
    }
    dragEvent.consume();
  }

  /**
   * is triggered when object is being dropped on area.
   *
   * @param dragEvent that needs to be dropped on an item.
   */
  @FXML
  @SuppressWarnings({"java:S6541", "java:S3776"})
  public void onDragDroppedMap(DragEvent dragEvent) {
    Dragboard db = dragEvent.getDragboard();
    boolean success = false;
    if (db.hasString()) {
      ImageView targetView = (ImageView) dragEvent.getGestureTarget();
      ImageView sourceView = (ImageView) dragEvent.getGestureSource();
      int posSource;
      if (GridPane.getColumnIndex(sourceView) == null && GridPane.getRowIndex(sourceView) == null) {
        posSource = 0;
      } else if (GridPane.getColumnIndex(sourceView) == null) {
        posSource = GridPane.getRowIndex(sourceView) + 1;
      } else if (GridPane.getRowIndex(sourceView) == null) {
        posSource = GridPane.getColumnIndex(sourceView);
      } else {
        posSource = GridPane.getColumnIndex(sourceView) + GridPane.getRowIndex(sourceView) + 1;
      }
      int posTarget;
      if (GridPane.getColumnIndex(targetView) == null && GridPane.getRowIndex(targetView) == null) {
        posTarget = 0;
      } else if (GridPane.getColumnIndex(targetView) == null) {
        posTarget = GridPane.getRowIndex(targetView) + 1;
      } else if (GridPane.getRowIndex(targetView) == null) {
        posTarget = GridPane.getColumnIndex(targetView);
      } else {
        posTarget = GridPane.getColumnIndex(targetView) + GridPane.getRowIndex(targetView) + 1;
      }

      FloorPlanSetting mapSource;
      FloorPlanSetting mapTarget;

      if (isInGridPaneSelect(sourceView) && isInGridPaneSelect(targetView)) {
        // Drop Item from and to GridPaneSelect
        mapSource = floorPlansSelectionArray[posSource];
        mapTarget = floorPlansSelectionArray[posTarget];
        floorPlansSelectionArray[posTarget] = mapSource;
        floorPlansSelectionArray[posSource] = mapTarget;
        setRotateMapButtonVisibility(posSource);
        setRotateMapButtonVisibility(posTarget);
        switchImage(targetView, sourceView);
        targetView.setRotate(
            (double) floorPlansSelectionArray[posTarget].getDirection().getNumber() * 90);
        sourceView.setRotate(
            (double) floorPlansSelectionArray[posSource].getDirection().getNumber() * 90);
      } else if (isInGridPaneDepot(sourceView) && isInGridPaneDepot(targetView)) {
        // Drop Item from and to GridPaneDepot
        mapSource = floorPlansDepotArray[posSource - 1];
        mapTarget = floorPlansDepotArray[posTarget - 1];
        floorPlansDepotArray[posTarget - 1] = mapSource;
        floorPlansDepotArray[posSource - 1] = mapTarget;
        switchImage(targetView, sourceView);
      } else if (isInGridPaneDepot(sourceView) && isInGridPaneSelect(targetView)) {
        // Drop Item from GridPaneDepot to GridPaneSelect
        mapSource = floorPlansDepotArray[posSource - 1];
        mapTarget = floorPlansSelectionArray[posTarget];
        floorPlansDepotArray[posSource - 1] = floorPlansSelectionArray[posTarget];
        floorPlansSelectionArray[posTarget] = mapSource;
        setRotateMapButtonVisibility(posTarget);
        switchImage(targetView, sourceView);
        targetView.setRotate(
            (double) floorPlansSelectionArray[posTarget].getDirection().getNumber() * 90);
        for (int i = 1; i <= MAX_CHECKPOINTS; i++) {
          if (checkpointsPosition.containsKey(i)
              && checkpointsPosition.get(i).containsKey(mapTarget.getFloorPlansEnum())) {
            checkpointsPosition.remove(i);
          }
        }
      } else if (isInGridPaneDepot(targetView) && isInGridPaneSelect(sourceView)) {
        // Drop Item from GridPaneSelect to GridPaneDepot
        mapSource = floorPlansSelectionArray[posSource];
        mapTarget = floorPlansDepotArray[posTarget - 1];
        mapSource.setDirection(Direction.NORTH);
        floorPlansSelectionArray[posSource] = mapTarget;
        floorPlansDepotArray[posTarget - 1] = mapSource;
        setRotateMapButtonVisibility(posSource);
        switchImage(targetView, sourceView);
        sourceView.setRotate(0);
        mapTarget.setDirection(Direction.NORTH);
        for (int i = 1; i <= MAX_CHECKPOINTS; i++) {
          if (checkpointsPosition.containsKey(i)
              && checkpointsPosition.get(i).containsKey(mapSource.getFloorPlansEnum())) {
            checkpointsPosition.remove(i);
          }
        }
      }
      success = true;
    }
    dragEvent.setDropCompleted(success);
    dragEvent.consume();
  }

  /** doesn't send message to server and returns to regular LobbyView (mainPane). */
  @FXML
  public void onDiscardMapChange() {
    mainGrid.setDisable(false);
    mainGrid.setEffect(null);
    mapPreviewPane.setVisible(false);
    mapPreviewPane.setDisable(true);
  }

  /**
   * This method is called when the confirm button is pressed after selecting the maps. It sends a
   * message to the server and returns to regular LobbyView.
   */
  @FXML
  public void onConfirmMapChange() {
    if (selectedMapIsInvalid()) {
      if (getNumberMaps() == 3) {
        eventBus.post(new LobbyInfoEvent("Drei Baupläne sind nicht erlaubt."));
      } else if (getNumberMaps() == 0) {
        eventBus.post(new LobbyInfoEvent("Das gesamte Feld darf nicht leer sein."));
      } else if (getCombinationIdTwoMaps() == -1) {
        eventBus.post(new LobbyInfoEvent("Die Baupläne müssen nebeneinander sein."));
      }

    } else {
      if (isCheckpointSelectionIsValid()) {
        mainGrid.setDisable(false);
        mainGrid.setEffect(null);
        mapPreviewPane.setVisible(false);
        mapPreviewPane.setDisable(true);
        updateLobbyOption(
            (int) previousSlot,
            previousLobbyTitle,
            previousTurnLimit,
            activeLasers.isSelected(),
            (int) previousAiPlayerCount,
            isWeakDuplicatedActive,
            isSwitchOffRoboter);
        lobbyService.sendFloorPlansPreviewRequest(lobbyId);
        if (Boolean.TRUE.equals(selected)) {
          btnReady.setVisible(true);
        }
      }
    }
  }

  private boolean isCheckpointSelectionIsValid() {
    if (!hasCheckpoints()) {
      eventBus.post(new LobbyInfoEvent("Es muss mindestens zwei Checkpoints geben."));
      return false;
    } else {
      if (!areCheckpointsInOrder()) {
        eventBus.post(
            new LobbyInfoEvent("Die Checkpoints sind nicht in der richtigen Reihenfolge gesetzt."));
        return false;
      }
    }
    return true;
  }

  private boolean areCheckpointsInOrder() {
    if (!(checkpointsPosition.containsKey(1) && checkpointsPosition.containsKey(2))) {
      return false;
    }
    boolean inOrder = true;
    for (int i = 1; i <= 6; i++) {
      if (checkpointsPosition.containsKey(i)
          && (i != 1 && !checkpointsPosition.containsKey(i - 1))) {
        inOrder = false;
        break;
      }
    }
    return inOrder;
  }

  private boolean hasCheckpoints() {
    for (int i = 1; i <= 6; i++) {
      if (checkpointsPosition.containsKey(i)) {
        return true;
      }
    }
    return false;
  }

  /**
   * updates the FloorPlanPreview for all users, besides the owner, because it has been already
   * updated in onConfirmMapChange() for the LobbyOwner.
   */
  @Subscribe
  public void onFloorPlansPreviewMessage(FloorPlansPreviewMessage msg) {
    if (!tabClosed && this.lobbyId.equals(msg.getLobbyId())) {
      Platform.runLater(this::showMapPreview);
    }
  }

  /** Switch the Images from the Target and the Source Drag and Drop Event. */
  private void switchImage(ImageView targetView, ImageView sourceView) {
    Image targetImage = targetView.getImage();
    targetView.setImage(sourceView.getImage());
    sourceView.setImage(targetImage);
  }

  /** Check if the ImageView is in GridPaneSelect. */
  private boolean isInGridPaneSelect(ImageView source) {
    for (Node child : gridMapSelect.getChildren()) {
      if (child.getClass() == ImageView.class && child.getId().equals(source.getId())) {
        return true;
      }
    }
    return false;
  }

  /** Check if the ImageView is in GridPaneDepot. */
  private boolean isInGridPaneDepot(ImageView source) {
    for (Node child : gridMapDepot.getChildren()) {
      if (child.getClass() == ImageView.class && child.getId().equals(source.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the visibility of the rotation-buttons when there is a map on the GridPane child.
   *
   * @param index is the index of the map in the selection GridPane.
   */
  public void setRotateMapButtonVisibility(int index) {
    boolean visible = floorPlansSelectionArray[index].getFloorPlansEnum() != FloorPlans.EMPTY;
    switch (index) {
      case 0:
        rotateMapUpLeft.setVisible(visible);
        checkpointMapVisibility[0] = visible;
        break;
      case 1:
        rotateMapUpRight.setVisible(visible);
        checkpointMapVisibility[1] = visible;
        break;
      case 2:
        rotateMapDownLeft.setVisible(visible);
        checkpointMapVisibility[2] = visible;
        break;
      case 3:
        rotateMapDownRight.setVisible(visible);
        checkpointMapVisibility[3] = visible;
        break;
      default:
        LOG.debug("Index out of bounds for rotate buttons");
        break;
    }
  }

  /** Rotates map in GridPane on the up left position. */
  @FXML
  public void onRotateMapUpLeft() {
    final int mapIndex = 0; // index of map
    rotateMap(mapIndex);
    planUpLeft.setRotate(
        (double) floorPlansSelectionArray[mapIndex].getDirection().getNumber() * 90);
    refreshCheckpointGui();
  }

  /** Rotates map in GridPane on the up right position. */
  @FXML
  public void onRotateMapUpRight() {
    final int mapIndex = 1; // index of map
    rotateMap(mapIndex);
    planUpRight.setRotate(
        (double) floorPlansSelectionArray[mapIndex].getDirection().getNumber() * 90);
    refreshCheckpointGui();
  }

  /** Rotates map in GridPane on the down left position. */
  @FXML
  public void onRotateMapDownLeft() {
    final int mapIndex = 2; // index of map
    rotateMap(mapIndex);
    planDownLeft.setRotate(
        (double) floorPlansSelectionArray[mapIndex].getDirection().getNumber() * 90);
    refreshCheckpointGui();
  }

  /** Rotates map in GridPane on the down right position. */
  @FXML
  public void onRotateMapDownRight() {
    final int mapIndex = 3; // index of map
    rotateMap(mapIndex);
    planDownRight.setRotate(
        (double) floorPlansSelectionArray[mapIndex].getDirection().getNumber() * 90);
    refreshCheckpointGui();
  }

  /**
   * This method reduces code duplicates and is called in the 4 rotate map methods. It makes non-GUI
   * changes when a map is being rotated.
   *
   * @param i is an index for the FloorPlans array.
   */
  @SuppressWarnings("SuspiciousNameCombination")
  private void rotateMap(int i) {
    checkpointsPosition.forEach(
        (integer, enumMap) ->
            enumMap.forEach(
                (floorPlans, point) -> {
                  if (floorPlans.equals(floorPlansSelectionArray[i].getFloorPlansEnum())) {
                    int oldRow = point.x;

                    int newColumn = FloorPlanSetting.FLOOR_PLAN_SIZE - oldRow - 1;

                    checkpointsPosition
                        .get(integer)
                        .get(floorPlans)
                        .setLocation(point.y, newColumn);
                  }
                }));

    floorPlansSelectionArray[i].setDirection(
        floorPlansSelectionArray[i].getDirection().rotate(Direction.EAST));

    setButtonDisableOnCheckpointMap(floorPlansSelectionArray[i], gridPaneCheckpointArray[i]);
  }

  /** Lets the checkPoints, which were set by lobbyOwner, appear on the GUI. */
  private void refreshCheckpointGui() {
    for (GridPane grid : gridPaneCheckpointArray) {
      grid.getChildren().forEach(node -> ((ImageView) ((Button) node).getGraphic()).setImage(null));
    }

    checkpointsPosition.forEach(
        (integer, enumMap) ->
            enumMap.forEach(
                (floorPlans, point) -> {
                  for (int i = 0; i < gridPaneCheckpointArray.length; i++) {
                    if (floorPlans.equals(floorPlansSelectionArray[i].getFloorPlansEnum())
                        || (floorPlans.equals(floorPlansCommittedArray[i].getFloorPlansEnum())
                            && !isOwner)) {
                      Button imageView =
                          (Button)
                              getElementFromGridPane(point.x, point.y, gridPaneCheckpointArray[i]);
                      if (imageView != null) {
                        ((ImageView) imageView.getGraphic())
                            .setImage(TextureAtlas.getAssets(integer + 7, 0));
                      }
                    }
                  }
                }));
  }

  /** Checks if the selected combination of the map is valid. */
  public boolean selectedMapIsInvalid() {
    int numberMaps = 0;
    for (FloorPlanSetting item : floorPlansSelectionArray) {
      if (item.getFloorPlansEnum() != FloorPlans.EMPTY) {
        numberMaps++;
      }
    }
    if (numberMaps == 0
        || numberMaps == 3
        || (floorPlansSelectionArray[0].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSelectionArray[1].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSelectionArray[2].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSelectionArray[3].getFloorPlansEnum() != FloorPlans.EMPTY)
        || (floorPlansSelectionArray[0].getFloorPlansEnum() == FloorPlans.EMPTY
            && floorPlansSelectionArray[1].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSelectionArray[2].getFloorPlansEnum() != FloorPlans.EMPTY
            && floorPlansSelectionArray[3].getFloorPlansEnum() == FloorPlans.EMPTY)) {
      floorPlansValid = false;
      return true;
    }
    floorPlansValid = true;
    return false;
  }

  /**
   * counts how many maps are in the committed FloorPlan.
   *
   * @return the number of maps (that are not EMPTY as an enum).
   */
  private int getNumberMaps() {
    int numberMaps = 0;
    for (FloorPlanSetting item : floorPlansCommittedArray) {
      if (item.getFloorPlansEnum() != FloorPlans.EMPTY) {
        numberMaps++;
      }
    }
    return numberMaps;
  }

  /**
   * Returns an ID for the combination of maps with 2 floorPlans.
   *
   * @return Combination ID's: -1: error 0: Up Left and Up Right (horizontal) 1: Down Left and Down
   *     Right (horizontal) 2: Up Left and Down Left (vertical) 3: Up Right and Down Right
   *     (vertical)
   */
  private int getCombinationIdTwoMaps() {
    if (!floorPlansValid) {
      return -1;
    } else if (floorPlansSelectionArray[0].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlansSelectionArray[1].getFloorPlansEnum() != FloorPlans.EMPTY) {
      return 0;
    } else if (floorPlansSelectionArray[2].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlansSelectionArray[3].getFloorPlansEnum() != FloorPlans.EMPTY) {
      return 1;
    } else if (floorPlansSelectionArray[0].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlansSelectionArray[2].getFloorPlansEnum() != FloorPlans.EMPTY) {
      return 2;
    } else if (floorPlansSelectionArray[1].getFloorPlansEnum() != FloorPlans.EMPTY
        && floorPlansSelectionArray[3].getFloorPlansEnum() != FloorPlans.EMPTY) {
      return 3;
    }
    return -1;
  }

  /**
   * When only one image/map selected: this method will put the image on the preview. If 2 or 4 maps
   * selected: The images are merged together and then put on the preview. Uses static methods from
   * ImageMerger class.
   */
  @SuppressWarnings("SuspiciousNameCombination")
  private void showMapPreview() {
    if (!isOwner) {
      planUpLeft.setImage(getImageForFloorPlan(floorPlansCommittedArray[0].getFloorPlansEnum()));
      planUpRight.setImage(
          getImageForFloorPlan(floorPlansCommittedArray[1].getFloorPlansEnum())); // 1
      planDownLeft.setImage(
          getImageForFloorPlan(floorPlansCommittedArray[2].getFloorPlansEnum())); // 2
      planDownRight.setImage(
          getImageForFloorPlan(floorPlansCommittedArray[3].getFloorPlansEnum())); // 3
      planUpLeft.setRotate((double) floorPlansCommittedArray[0].getDirection().getNumber() * 90);
      planUpRight.setRotate((double) floorPlansCommittedArray[1].getDirection().getNumber() * 90);
      planDownLeft.setRotate((double) floorPlansCommittedArray[2].getDirection().getNumber() * 90);
      planDownRight.setRotate((double) floorPlansCommittedArray[3].getDirection().getNumber() * 90);
    }
    int mergeWidth = 1;
    switch (getNumberMaps()) {
      case 1:
        for (FloorPlanSetting item : floorPlansCommittedArray) {
          if (item.getFloorPlansEnum() != FloorPlans.EMPTY) {
            mapChangerImage.setImage(
                SwingFXUtils.toFXImage(
                    ImageMerger.rotate(
                        floorPlansImageHashMap.get(item.getFloorPlansEnum()), item.getDirection()),
                    null));
            return;
          }
        }
        LOG.debug("No FloorPlan found?");
        break;
      case 2:
        if (selectedMapIsInvalid()) {
          LOG.debug("Current map selection is forbidden.");
          return;
        }
        switch (getCombinationIdTwoMaps()) {
          case 0:
            mapChangerImage.setImage(
                SwingFXUtils.toFXImage(
                    ImageMerger.joinHorizontal(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[0].getFloorPlansEnum()),
                            floorPlansCommittedArray[0].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[1].getFloorPlansEnum()),
                            floorPlansCommittedArray[2].getDirection()),
                        mergeWidth),
                    null));
            break;
          case 1:
            mapChangerImage.setImage(
                SwingFXUtils.toFXImage(
                    ImageMerger.joinHorizontal(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[2].getFloorPlansEnum()),
                            floorPlansCommittedArray[0].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[3].getFloorPlansEnum()),
                            floorPlansCommittedArray[2].getDirection()),
                        mergeWidth),
                    null));
            break;
          case 2:
            mapChangerImage.setImage(
                SwingFXUtils.toFXImage(
                    ImageMerger.joinVertical(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[0].getFloorPlansEnum()),
                            floorPlansCommittedArray[0].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[2].getFloorPlansEnum()),
                            floorPlansCommittedArray[2].getDirection()),
                        mergeWidth),
                    null));
            break;
          case 3:
            mapChangerImage.setImage(
                SwingFXUtils.toFXImage(
                    ImageMerger.joinVertical(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[1].getFloorPlansEnum()),
                            floorPlansCommittedArray[1].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[3].getFloorPlansEnum()),
                            floorPlansCommittedArray[3].getDirection()),
                        mergeWidth),
                    null));
            break;
          default:
            LOG.debug("Something is wrong with the FloorPlans combination of two FloorPlans.");
            break;
        }
        break;
      case 4:
        mapChangerImage.setImage(
            SwingFXUtils.toFXImage(
                ImageMerger.joinVertical(
                    ImageMerger.joinHorizontal(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[0].getFloorPlansEnum()),
                            floorPlansCommittedArray[0].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[1].getFloorPlansEnum()),
                            floorPlansCommittedArray[1].getDirection()),
                        mergeWidth),
                    ImageMerger.joinHorizontal(
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[2].getFloorPlansEnum()),
                            floorPlansCommittedArray[2].getDirection()),
                        ImageMerger.rotate(
                            floorPlansImageHashMap.get(
                                floorPlansCommittedArray[3].getFloorPlansEnum()),
                            floorPlansCommittedArray[3].getDirection()),
                        mergeWidth),
                    mergeWidth),
                null));
        break;
      default:
        LOG.debug("Current map selection is forbidden or no plan has been selected yet.");
        break;
    }
  }

  private Image getImageForFloorPlan(FloorPlans floorPlan) {
    return TextureAtlasFloorPlans.getFloorPlanImage(floorPlan);
  }

  private BufferedImage getBufferedImageForFloorPlan(FloorPlans floorPlan) {
    return TextureAtlasFloorPlans.getFloorPlanBufferedImage(floorPlan);
  }

  /**
   * loads the imageInputStream of the basic floorPlans in a HashMap, so that showMapPreview()
   * doesn't always have to load the images for each action.
   *
   * <p>DON'T CHANGE THIS TO EnumMap without testing! The FXML won't load for some reason.
   */
  private EnumMap<FloorPlans, BufferedImage> initializeFloorPlans(FloorPlans[] basicFloorPlans) {
    EnumMap<FloorPlans, BufferedImage> floorPlansBufferedImageEnumMap =
        new EnumMap<>(FloorPlans.class);
    for (int i = 0; i < basicFloorPlans.length; i++) {
      ImageView imageView = new ImageView(getImageForFloorPlan(basicFloorPlans[i]));
      imageView.setFitWidth(200);
      imageView.setFitHeight(200);
      imageView.setId("plan" + (i));
      imageView.setPickOnBounds(true);
      imageView.setPreserveRatio(true);

      imageView.setOnDragDetected(this::onDragDetectedMap);
      imageView.setOnDragDropped(this::onDragDroppedMap);
      imageView.setOnDragOver(this::onDragOverMap);

      GridPane.setRowIndex(imageView, i);
      gridMapDepot.getChildren().add(imageView);

      floorPlansBufferedImageEnumMap.put(
          basicFloorPlans[i], getBufferedImageForFloorPlan(basicFloorPlans[i]));
      gridMapDepot.getRowConstraints().add(new RowConstraints(200));
    }
    return floorPlansBufferedImageEnumMap;
  }

  /**
   * is called by the first Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint1(ActionEvent event) {
    setActiveCheckpointButton(1);
    activeCheckpointMarker = 1;
    event.consume();
  }

  /**
   * is called by the second Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint2(ActionEvent event) {
    setActiveCheckpointButton(2);
    activeCheckpointMarker = 2;
    event.consume();
  }

  /**
   * is called by the third Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint3(ActionEvent event) {
    setActiveCheckpointButton(3);
    activeCheckpointMarker = 3;
    event.consume();
  }

  /**
   * is called by the fourth Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint4(ActionEvent event) {
    setActiveCheckpointButton(4);
    activeCheckpointMarker = 4;
    event.consume();
  }

  /**
   * is called by the fifths Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint5(ActionEvent event) {
    setActiveCheckpointButton(5);
    activeCheckpointMarker = 5;
    event.consume();
  }

  /**
   * is called by the six's Checkpoint button in the fieldBuilder.
   *
   * @param event the event from fx
   */
  @FXML
  public void onCheckpoint6(ActionEvent event) {
    setActiveCheckpointButton(6);
    activeCheckpointMarker = 6;
    event.consume();
  }

  private void setActiveCheckpointButton(int id) {
    checkpointButtons.forEach(button -> button.setId(""));
    if (id > 0 && id <= checkpointButtons.size()) {
      checkpointButtons.get(id - 1).setId("cp-active");
    } else {
      LOG.warn("setActiveCheckpointButton is out of range");
    }
  }

  /**
   * Handles Click on CheckPoint Map. Removes already existing checkpoint at selected position and
   * sets new checkpoint at this position. Displays the changes.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @FXML
  public void onClickedOnCheckpointMap(MouseEvent event) {
    Button imageView = (Button) event.getTarget();
    int column = GridPane.getColumnIndex(imageView);
    int row = GridPane.getRowIndex(imageView);

    for (int cpMap = 0; cpMap < gridPaneCheckpointArray.length; cpMap++) {
      if (imageView.equals(getElementFromGridPane(row, column, gridPaneCheckpointArray[cpMap]))) {
        // clicked position found on this map
        FloorPlans floorPlanID = floorPlansSelectionArray[cpMap].getFloorPlansEnum();
        // remove checkpoint already at this position
        for (int cpNum = 1; cpNum <= MAX_CHECKPOINTS; cpNum++) {
          if (checkpointsPosition.containsKey(cpNum)
              && checkpointsPosition.get(cpNum).containsKey(floorPlanID)
              && checkpointsPosition.get(cpNum).get(floorPlanID).x == row
              && checkpointsPosition.get(cpNum).get(floorPlanID).y == column) {
            checkpointsPosition.remove(cpNum);
          }
        }
        if (activeCheckpointMarker != -1) {
          // set new checkpoint at this position
          checkpointsPosition.put(
              activeCheckpointMarker, new EnumMap<>(Map.of(floorPlanID, new Point(row, column))));

          ((ImageView) imageView.getGraphic())
              .setImage(TextureAtlas.getAssets(activeCheckpointMarker + 7, 0));
        }
        refreshCheckpointGui();
        break;
      }
    }
    event.consume();
  }

  private Node getElementFromGridPane(int x, int y, GridPane gridPane) {
    for (Node child : gridPane.getChildren()) {
      if (GridPane.getRowIndex(child) == x && GridPane.getColumnIndex(child) == y) {
        return child;
      }
    }
    return null;
  }

  /**
   * "Switch Mode" button press event.
   *
   * <p>Toggles between "Map Edit Mode" and "Checkpoint Edit Mode".
   *
   * @param event the ActionEvent triggered by the button press
   */
  @FXML
  public void onSwitchMode(ActionEvent event) {
    switchMode = !switchMode;

    btnSwitchMode.setText(switchMode ? "Map Bearbeiten" : "Checkpoint Bearbeiten");

    stackPaneCheckpoint.setVisible(switchMode && checkpointMapVisibility[0]);
    stackPaneCheckpoint1.setVisible(switchMode && checkpointMapVisibility[1]);
    stackPaneCheckpoint2.setVisible(switchMode && checkpointMapVisibility[2]);
    stackPaneCheckpoint3.setVisible(switchMode && checkpointMapVisibility[3]);
    checkpointSelection.setVisible(switchMode);
    scrollPaneMaps.setVisible(!switchMode);

    for (int i = 0; i < gridPaneCheckpointArray.length; i++) {
      if (!floorPlansSelectionArray[i].getFloorPlansEnum().equals(FloorPlans.EMPTY)) {
        setButtonDisableOnCheckpointMap(floorPlansSelectionArray[i], gridPaneCheckpointArray[i]);
      }
    }

    refreshCheckpointGui();
    event.consume();
  }

  private void setButtonDisableOnCheckpointMap(
      FloorPlanSetting floorPlanSetting, GridPane gridPane) {
    final int[][] planId =
        HelperMethods.csvToIntArrayId(
            HelperMethods.convertFloorPlan(new FloorPlanSetting[] {floorPlanSetting}, null));

    for (int i = 0; i < FloorPlanSetting.FLOOR_PLAN_SIZE; i++) {
      for (int j = 0; j < FloorPlanSetting.FLOOR_PLAN_SIZE; j++) {
        Button button = (Button) getElementFromGridPane(i, j, gridPane);
        int id = planId[i][j];
        if (button != null && Arrays.stream(VALID_IDS).anyMatch(integer -> integer == id)) {
          button.setDisable(false);
          button.setStyle("-fx-background-color: transparent");

        } else if (button != null) {
          button.setDisable(true);
          button.setStyle("-fx-background-color: rgba(201,0,0,0.43)");
        } else {
          LOG.error("Element in Checkpoint GridPane is Null");
        }
      }
    }
  }

  /**
   * Resets the active checkpoint to the default state, effectively deleting the current checkpoint
   * selection.
   *
   * @param event the ActionEvent triggered by the button press
   */
  @FXML
  public void onCheckpointDelete(ActionEvent event) {
    setActiveCheckpointButton(MAX_CHECKPOINTS + 1);
    activeCheckpointMarker = -1;
    event.consume();
  }

  /**
   * Displays a help message with instructions on how to set up game boards and checkpoints.
   *
   * @param event the ActionEvent triggered by the button press
   */
  @FXML
  public void onCheckpointHelpPress(ActionEvent event) {
    eventBus.post(
        new LobbyInfoEvent(
            "Um Spielpläne zusammen zu setzen"
                + " kannst du sie aus der Auswahl in die leeren Flächen ziehen. "
                + System.lineSeparator()
                + System.lineSeparator()
                + "- Spielfelder müssen nebeneinander "
                + "sein und du kannst einen, zwei oder vier Stück auswählen."
                + System.lineSeparator()
                + System.lineSeparator()
                + System.lineSeparator()
                + "Du musst danach mindestens einen Checkpoint"
                + " auf den Spielfeldern, die du ausgewählt hast platzieren."
                + System.lineSeparator()
                + System.lineSeparator()
                + "- Dafür drückst du auf Checkpoints bearbeiten"
                + " und wählst dann den Checkpoint aus, den du auf das Feld setzen möchtest."
                + System.lineSeparator()
                + System.lineSeparator()
                + "- Am Besten beginnst du beim ersten Checkpoint "
                + "und klickst danach auf ein nicht rot markiertes "
                + " Feld um den Checkpoint zu platzieren."
                + System.lineSeparator()
                + System.lineSeparator()
                + " So kannst du alle weiteren Checkpoints setzen, "
                + "umsetzen oder wieder entfernen. Beachte, dass alle gewählten"
                + " Checkpoint-Nummern zusammenhängend sein sollten"
                + " und keine zahl dazwischen fehlt"));
    event.consume();
  }

  /** Method called when pressed on the button spectatorModeActive. */
  @FXML
  public void onSpectatorModeActive(ActionEvent event) {
    if (!tabClosed) {
      lobbyService.sendChangeSpectatorModeRequest(
          lobbyId, loggedInUser.getUser(), robot[robotCounter], selected);
    }
    event.consume();
  }

  /**
   * Changes for spectatorMode or playerMode.
   *
   * @param message sent to all in Lobby.
   */
  @Subscribe
  @SuppressWarnings("java:S3776")
  public void onChangedSpectatorModeMessage(ChangedSpectatorModeMessage message) {
    if (!tabClosed && message.getLobbyId().equals(lobbyId)) {
      if (loggedInUser.getUser().getUsername().equals(message.getUser().getUsername())) {
        for (Robots robotToReset : Robots.values()) {
          robotSelected.put(robotToReset, false);
        }
        Platform.runLater(() -> setSpectatorVisibility(message.isSpectatorMode()));
      } else {
        lobbyOwnerChange(message.getLobbyId(), (UserDTO) message.getOwner());
        if (message.isSpectatorMode()) {
          Platform.runLater(() -> deleteUserColor(message.getUser().getUsername()));
        } else {
          Platform.runLater(
              () -> {
                if (usernameList != null
                    && usernameList.stream()
                        .map(ColoredUser::getUsername)
                        .noneMatch(x -> x.equals(message.getUser().getUsername()))
                    && message.getLobbyId().equals(lobbyId)) {
                  usernameList.add(new ColoredUser(message.getUser().getUsername(), Color.BLACK));
                }
              });
        }
        if (isOwner) {
          btnStart.setVisible(false);
        }
      }
      lobbyService.sendSelectedRobotsRequest(lobbyId);
    }
  }

  /** Changes the colour of the spectator button after joining lobby as spectator. */
  private void setSpectatorVisibility(boolean isSpectator) {
    if (isSpectator) {
      Platform.runLater(
          () -> {
            spectatorModeActive.setStyle("-fx-background-color: #00C108;");
            spectatorModeActive.setText("Mitspielen");
            robotSelect.setStyle("-fx-background-color: rgba(216,1,0,1)");
            robotSelect.setVisible(true);
            arrowLeft.setVisible(true);
            arrowRight.setVisible(true);
            btnReady.setVisible(false);
            btnReady.setStyle("-fx-background-color: #D80100;");
          });
      selected = false;
    } else {
      Platform.runLater(
          () -> {
            spectatorModeActive.setStyle("-fx-background-color: #D80100;");
            spectatorModeActive.setText("Zuschauen");
          });
    }
  }

  @SuppressWarnings("java:S110")
  static class LobbyChatCell extends ListCell<Text> {
    @Override
    public void updateItem(Text item, boolean empty) {
      setGraphic(item);
    }
  }
}
