package de.uol.swp.client.game;

import static de.uol.swp.client.textureatlas.TextureAtlas.getCard;
import static de.uol.swp.client.textureatlas.TextureAtlas.toGrayscale;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.client.audio.PlaySound;
import de.uol.swp.client.chat.ClientChatService;
import de.uol.swp.client.game.animator.AnimationInformation;
import de.uol.swp.client.game.animator.FloorFieldAnimator;
import de.uol.swp.client.game.animator.GameAnimatorFactory;
import de.uol.swp.client.game.events.ShowWinnerTabEvent;
import de.uol.swp.client.game.events.ValidateCardSelectionResponseEvent;
import de.uol.swp.client.lobby.LobbyService;
import de.uol.swp.client.textureatlas.TextureAtlas;
import de.uol.swp.common.chat.ChatCommandResponse;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.message.ProgrammingCardMessage;
import de.uol.swp.common.game.message.RespawnDirectionInteractionMessage;
import de.uol.swp.common.game.message.RespawnInteractionMessage;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.message.RoundFinishMessage;
import de.uol.swp.common.game.message.StopTimerMessage;
import de.uol.swp.common.game.message.UnblockCardInteractionMessage;
import de.uol.swp.common.game.message.ValidateCardsMessage;
import de.uol.swp.common.game.message.WaitForNextRoundMessage;
import de.uol.swp.common.game.message.WinMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.request.NextRoundStartForceRequest;
import de.uol.swp.common.game.request.NextRoundStartRequest;
import de.uol.swp.common.game.request.RespawnDirectionInteractionRequest;
import de.uol.swp.common.game.request.RespawnInteractionRequest;
import de.uol.swp.common.game.request.ShutdownRobotRequest;
import de.uol.swp.common.game.response.GameDTOResponse;
import de.uol.swp.common.game.response.UnblockCardResponse;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.helper.HelperMethods;
import de.uol.swp.common.lobby.message.ChangedSpectatorModeMessage;
import de.uol.swp.common.lobby.response.LobbyLeaveSuccessfulResponse;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages the GamePresenter. */
@SuppressWarnings("UnstableApiUsage")
public class GamePresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/GameTab.fxml";
  public static final String DIALOG_STYLE_SHEET = "css/confirmationBox.css";
  private static final Logger LOG = LogManager.getLogger(GamePresenter.class);
  private static final String NAME_OF_TIMER_LABEL = "Timer: ";
  private static final int BLUR_RADIUS_VALUE = 12;
  private static final String GAME_DTO_IS_NOT_PRESENT = "GameDTO is not Present";
  private static final String BTN_CLOSE_CARD_SELECT_ID = "gamePane__cardSelect__button--ready";
  @FXML public Text txtRespawnTimer;
  @FXML public Pane checkpointsPane;
  @Inject ClientChatService clientChatService;
  @Inject LobbyService lobbyService;
  @Inject LoggedInUser loggedInUser;
  @Inject GameService gameService;
  @FXML Tab gameTab;
  @FXML private GridPane respawnButtonsGridPane;
  @FXML private GridPane respawnFieldGridPane;
  @FXML private AnchorPane robotRespawnPane;
  @FXML private Button btnNextRound;
  @FXML private Button btnForceNextRound;
  @FXML private BorderPane mainPane;

  @SuppressWarnings("checkstyle:MemberName")
  @FXML
  private VBox vBoxShowRobotInfos;

  @FXML private GridPane floorField;
  @FXML private Pane robotField;
  @FXML private GridPane gridCardDepot;
  @FXML private GridPane gridCardSelect;
  @FXML private AnchorPane gameCardPane;
  @FXML private Button btnCloseCardSelect;
  @FXML private Button btnOpenCardSelect;
  @FXML private AnchorPane gameChatPane;
  @FXML private ListView<Text> gameChat;
  @FXML private TextField messageFieldGame;
  @FXML private Button closeChatButton;
  @FXML private Button openChatButton;

  @SuppressWarnings("java:S1171")
  private final Animation newMessageAnimation =
      new Transition() {
        {
          setCycleDuration(Duration.seconds(2));
        }

        @Override
        protected void interpolate(double frac) {
          openChatButton.setBackground(
              new Background(
                  new BackgroundFill(
                      new Color(1, 0.9, 0.7, 1 - frac),
                      openChatButton.backgroundProperty().getValue().getFills().get(0).getRadii(),
                      openChatButton
                          .backgroundProperty()
                          .getValue()
                          .getFills()
                          .get(0)
                          .getInsets())));
        }
      };

  @FXML private Text gamePhase;
  @FXML private Text countdown;
  @FXML private CheckBox robotShutdown;
  @FXML private Text txtShutOff;
  private String gameId;
  private boolean tabClosed;
  private boolean mouseOnChat = false;
  private boolean chatOpen = false;
  private boolean floorFieldInitialized = false;
  private boolean isRobotFieldInitialized = false;
  private int numberOfNewMessages = 0;
  private ObservableList<Text> gameChatMessages;
  private int unlockedCardSlots;
  private boolean robotFieldInitialized = false;
  private int canUnblock = 0;
  private boolean[] blockedCardSlots = new boolean[] {false, false, false, false, false};
  private FadeTransition fadeUnblock;
  private boolean unlockingProcess = false;
  private FloorFieldAnimator floorFieldAnimator;

  @SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "OptionalUsedAsFieldOrParameterType"})
  private Optional<GameDTO> gameDTO;
  // Depot in which up to nine cards can be stored
  private ProgrammingCard[] programmingCardDepotArray;
  // SelectionDepot in which up to five final cards can be stored
  private ProgrammingCard[] programmingCardSelectionArray;
  private int[][] checkpoints = new int[1][2];
  private int timeInSeconds = 99;
  private int unblockTimeInSeconds = 99;
  private boolean isOnlyDirection;
  private Direction robotDirection;
  private int newRespawnSelection;
  private int tileSize;
  private GameAnimatorFactory gameAnimator;
  private final Duration duration = Duration.seconds(1);
  private final HashMap<String, ImageView> imageViewHashMapRobotInfo = new HashMap<>();
  private boolean firstRoundfirstCard = true;

  /** Constructor. */
  public GamePresenter() {
    newMessageAnimation.setCycleCount(Animation.INDEFINITE);
    newMessageAnimation.setAutoReverse(true);
  }

  /**
   * Initializes the 3x3 playground by creating and setting the buttons and images. It also defines
   * the event handling for the buttons to switch the picture and set the active new spawn field.
   * This method is automatically called by JavaFX after the FXML file is loaded and the controller
   * is initialized.
   */
  @FXML
  @SuppressWarnings("java:S3776")
  public void initialize() {
    // create and set the button for the 3x3 playground
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        int preferenceSize = 150;
        Button button = new Button();
        button.setPrefSize(preferenceSize, preferenceSize);
        button.setOnAction(
            e -> {
              // set the event for the button to switch the picture and set the active new spawn
              // field
              Button sourceButton = (Button) e.getSource();
              int counter = 0;
              for (int k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                  Button gridButton =
                      (Button) getNodeByRowColumnIndex(k, l, respawnButtonsGridPane);
                  gridButton.setGraphic(null);
                  if (gridButton.equals(sourceButton)) {
                    newRespawnSelection = counter;
                    gameDTO.ifPresentOrElse(
                        dto ->
                            Platform.runLater(
                                () ->
                                    sourceButton.setGraphic(
                                        new ImageView(
                                            TextureAtlas.getAssets(
                                                dto.getPlayer(loggedInUser.getUser())
                                                    .getRobot()
                                                    .getType()
                                                    .ordinal(),
                                                0)))),
                        () -> LOG.error(GAME_DTO_IS_NOT_PRESENT));
                  }
                  counter++;
                }
              }
            });

        // add the button/images to the grid panes
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(preferenceSize);
        imageView.setFitWidth(preferenceSize);

        respawnFieldGridPane.add(imageView, y, x);
        respawnButtonsGridPane.add(button, y, x);
      }
    }
  }

  /**
   * Handels the GameDTOResponse found on the EventBus.
   *
   * @param response GameDTOResponse
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onGameDTOResponse(GameDTOResponse response) {
    if (gameId != null) {
      return;
    }
    if (!tabClosed) {
      if (response.getGameDTO() != null) {
        gameDTO = Optional.of(response.getGameDTO());
        gameId = response.getGameDTO().getGameId();
        Platform.runLater(
            () -> {
              initializeFloorField();
              initializeRobotsPosition(gameDTO.get());
              initializeRobotInfoField(gameDTO.get());
            });
      } else {
        gameDTO = Optional.empty();
      }
    }
  }

  private void initializeFloorField() {
    if (!floorFieldInitialized && gameDTO.isPresent()) {
      FloorPlan plan = gameDTO.get().getFloorPlan();
      int[][] floorPlanFieldId = HelperMethods.csvToIntArrayId(plan.getFloorPlanAsString());
      int[][] floorPlanFieldRotation =
          HelperMethods.csvToIntArrayRotation(plan.getFloorPlanAsString());

      final int rows = floorPlanFieldId.length;
      final int cols = floorPlanFieldId[0].length;

      // Calculating ImageSize in order to fit to screen
      tileSize = calculateImageSize(rows, cols);

      floorFieldAnimator = new FloorFieldAnimator();
      gameAnimator = new GameAnimatorFactory(this);
      for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
          int id = floorPlanFieldId[row][col];
          int rotation = floorPlanFieldRotation[row][col] * 90;
          ImageView imageView = floorFieldAnimator.getImageView(id, rotation);
          imageView.setFitWidth(tileSize);
          imageView.setFitHeight(tileSize);
          floorField.add(imageView, col, row);
        }
      }
      floorFieldAnimator.start();
      initializeCheckPoints(gameDTO.get());

      btnForceNextRound.setVisible(
          gameDTO.get().getLobby().getOwner().equals(loggedInUser.getUser()));
      floorFieldInitialized = true;
    }
  }

  /** This method adds the checkpoints images to the checkpointsPane. */
  @SuppressWarnings({
    "checkstyle:AbbreviationAsWordInName",
    "checkstyle:VariableDeclarationUsageDistance"
  })
  private void initializeCheckPoints(GameDTO gameDTO) {
    gameDTO
        .getFloorPlan()
        .getCheckpoints()
        .forEach(
            (i, c) -> {
              try {
                ImageView imageView = new ImageView();
                Image image = (TextureAtlas.getAssets(i + 7, 0));

                int[][] newArray = new int[checkpoints.length + 1][2];

                System.arraycopy(checkpoints, 0, newArray, 0, checkpoints.length);

                newArray[newArray.length - 1][0] = c.getX();
                newArray[newArray.length - 1][1] = c.getY();
                checkpoints = newArray;

                imageView.setImage(image);
                imageView.setFitWidth(tileSize);
                imageView.setFitHeight(tileSize);
                Platform.runLater(
                    () -> {
                      robotField.getChildren().add(imageView);
                      imageView.setX(c.getX() * (double) tileSize);
                      imageView.setY(c.getY() * (double) tileSize);
                    });
              } catch (NullPointerException e) {
                // Field has no Checkpoint
              }
            });
  }

  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  private void initializeRobotInfoField(GameDTO game) {
    if (isRobotFieldInitialized) {
      return;
    }
    // Show RobotAssets
    for (int i = 0; i < game.getPlayers().size(); i++) {
      final double assetsSize = 50;
      Player player = (Player) game.getPlayers().toArray()[i];
      HBox hbox = new HBox();
      hbox.setMaxHeight(70);
      hbox.setId("hBox1");

      // Player
      Text text = new Text(player.getUser().getUsername());
      text.setWrappingWidth(70);
      text.setId("usernametext-" + player.getUser().getUsername());
      hbox.getChildren().add(text);

      // Roboter
      ImageView imageView =
          new ImageView(TextureAtlas.getAssets(player.getRobot().getType().ordinal(), 0));
      imageView.setFitHeight(assetsSize);
      imageView.setPreserveRatio(true);
      imageView.setId("roboterImage-" + player.getRobot().getType());
      hbox.getChildren().add(imageView);

      // Player lives
      ImageView livesImage =
          new ImageView(TextureAtlas.getAssets(28 + player.getRobot().getLives(), 0));
      livesImage.setFitHeight(assetsSize);
      livesImage.setPreserveRatio(true);
      livesImage.setId("playerLives-" + player.getRobot().getType());
      ImageView livesBackgroundimage = new ImageView(TextureAtlas.getAssets(26, 0));
      livesBackgroundimage.setFitHeight(assetsSize);
      livesBackgroundimage.setPreserveRatio(true);
      StackPane spLives = new StackPane(livesBackgroundimage, livesImage);
      spLives.setPrefSize(0, 0);
      spLives.setMaxSize(assetsSize, assetsSize);
      hbox.getChildren().add(spLives);

      // Robot lives
      ImageView livesRobotImage =
          new ImageView(TextureAtlas.getAssets(28 + player.getRobot().getDamage(), 0));
      livesRobotImage.setFitHeight(assetsSize);
      livesRobotImage.setPreserveRatio(true);
      livesRobotImage.setId("robotDamagePoints-" + player.getRobot().getType());
      ImageView robotLivesBackgroundimage = new ImageView(TextureAtlas.getAssets(27, 0));
      robotLivesBackgroundimage.setFitHeight(assetsSize);
      robotLivesBackgroundimage.setPreserveRatio(true);
      StackPane spRoboLives = new StackPane(robotLivesBackgroundimage, livesRobotImage);
      spRoboLives.setPrefSize(0, 0);
      spRoboLives.setMaxSize(assetsSize, assetsSize);
      spRoboLives.setId("spRoboLives");
      hbox.getChildren().add(spRoboLives);

      // Current Checkpoints
      int checkpoint =
          player.getRobot().getCurrentCheckpoint() == 0
              ? 39
              : 7 + player.getRobot().getCurrentCheckpoint();
      ImageView currentCheckpoints = new ImageView(TextureAtlas.getAssets(checkpoint, 0));
      currentCheckpoints.setFitHeight(assetsSize);
      currentCheckpoints.setPreserveRatio(true);
      currentCheckpoints.setId("checkpoint-" + player.getRobot().getType());
      hbox.getChildren().add(currentCheckpoints);

      VBox vbox = new VBox();
      vbox.getChildren().add(new Text("Nächste Karte"));
      vbox.setId("vBox");

      HBox hbox1 = new HBox();

      // Movement
      ImageView nextCardNumber = new ImageView();
      nextCardNumber.setFitHeight(assetsSize);
      nextCardNumber.setPreserveRatio(true);
      nextCardNumber.setId("nextCardNumber-" + player.getRobot().getType());
      hbox1.getChildren().add(nextCardNumber);
      hbox1.setId("hBOX");

      // MovementCard
      ImageView nextCard = new ImageView();
      nextCard.setFitHeight(assetsSize);
      nextCard.setPreserveRatio(true);
      nextCard.setId("nextCard-" + player.getRobot().getType());
      hbox1.getChildren().add(nextCard);
      vbox.getChildren().add(hbox1);
      hbox.getChildren().add(vbox);

      vBoxShowRobotInfos.getChildren().add(hbox);
      vBoxShowRobotInfos.setSpacing(20);
      if (player.getUser().equals(loggedInUser.getUser())) {
        hbox.setStyle(
            "-fx-background-color: rgba(176,176,176,0.51); "
                + "-fx-border-color: rgba(256,220,4,0.70); -fx-border-width: 5;");
      } else {
        hbox.setStyle("-fx-background-color: rgba(176,176,176,0.51)");
      }
    }
    isRobotFieldInitialized = true;
  }

  /**
   * Updates the graphical user interface (GUI) with the information of a robot.
   *
   * @param msg the RobotInformationMessage received
   */
  @Subscribe
  public void onRobotInformationMessage(RobotInformationMessage msg) {
    if (!msg.getGameId().equals(gameId)) {
      return;
    }
    gameAnimator.updateGui(msg.getRobotType(), msg.getRobotInformation());
  }

  @SuppressWarnings("checkstyle:LocalVariableName")
  private void initializeRobotsPosition(GameDTO game) {
    if (robotFieldInitialized) {
      return;
    }
    // Display Markers
    game.getPlayers()
        .forEach(
            p -> {
              Robot robot = p.getRobot();
              int xCord = robot.getPosition().getX() * tileSize;
              int yCord = robot.getPosition().getY() * tileSize;
              ImageView imageView = new ImageView();
              imageView.setImage(TextureAtlas.getMarker(robot.getType()));
              imageView.setId("marker-" + robot.getType().ordinal());
              imageView.setFitHeight(0.9 * tileSize);
              imageView.setFitWidth(0.9 * tileSize);
              imageView.setTranslateX(xCord);
              imageView.setTranslateY(yCord);
              Platform.runLater(() -> robotField.getChildren().add(imageView));
            });

    // Display Robots
    game.getPlayers()
        .forEach(
            p -> {
              Robot robot = p.getRobot();
              int xCord = robot.getPosition().getX() * tileSize;
              int yCord = robot.getPosition().getY() * tileSize;
              ImageView imageView = new ImageView();
              imageView.setImage(
                  TextureAtlas.getAssets(
                      robot.getType().ordinal(), robot.getDirection().ordinal() * 90));
              imageView.setId("robot-" + robot.getType().ordinal());
              imageView.setFitHeight(0.9 * tileSize);
              imageView.setFitWidth(0.9 * tileSize);
              imageView.setTranslateX(xCord + 0.05 * tileSize);
              imageView.setTranslateY(yCord + 0.05 * tileSize);
              Platform.runLater(() -> robotField.getChildren().add(imageView));
            });

    robotFieldInitialized = true;
  }

  private ImageView getRobotImageView(Robots robot) {
    String robotId = "robot-" + robot.ordinal();
    ImageView robotImageView = null;

    for (Node node : robotField.getChildren()) {
      if (node instanceof ImageView && robotId.equals(node.getId())) {
        robotImageView = (ImageView) node;
        break;
      }
    }
    return robotImageView;
  }

  private ImageView getMarkerImageView(Robots robot) {
    String robotId = "marker-" + robot.ordinal();
    ImageView markerImageView = null;

    for (Node node : robotField.getChildren()) {
      if (node instanceof ImageView && robotId.equals(node.getId())) {
        markerImageView = (ImageView) node;
        break;
      }
    }
    return markerImageView;
  }

  public Pane getRobotField() {
    return robotField;
  }

  /**
   * Getter for the ImageView of an information field for a specific robot.
   *
   * @param prefix the type of information field: playerLives, robotDamagePoints, checkpoint,
   *     nextCardNumber, nextCard
   * @param robot the robot
   * @return the ImageView
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private ImageView getInfoImageView(String prefix, Robots robot) {
    String imageViewID = prefix + "-" + robot;
    if (imageViewHashMapRobotInfo.containsKey(imageViewID)) {
      return imageViewHashMapRobotInfo.get(imageViewID);
    }
    try {
      ImageView imageView = (ImageView) iterateChildren(vBoxShowRobotInfos, imageViewID);
      imageViewHashMapRobotInfo.put(imageViewID, imageView);
      return imageView;
    } catch (ClassCastException e) {
      LOG.error("ImageView for ID {} could not be found", imageViewID);
    }
    return null;
  }

  /**
   * Getter for the ImageViews of an information field for all possible robots.
   *
   * @param prefix the type of information field: playerLives, robotDamagePoints, checkpoint,
   *     nextCardNumber, nextCard
   * @return the ImageViews
   */
  private ImageView[] getInfoImageView(String prefix) {
    ImageView[] imageViews = new ImageView[Robots.values().length];
    for (Robots robot : Robots.values()) {
      imageViews[robot.ordinal()] = getInfoImageView(prefix, robot);
    }
    return imageViews;
  }

  /** The method iterates over all the children of the given container. */
  private Node iterateChildren(Parent parent, String id) {
    for (Node child : parent.getChildrenUnmodifiable()) {
      if (child.getId() != null && child.getId().equals(id)) {
        return child;
      }

      if (child instanceof javafx.scene.Parent) {
        Node result = iterateChildren((Parent) child, id);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  // Animation

  /**
   * Method for animation: Rotating.
   *
   * @param info AnimationInformation
   */
  public void rotation(AnimationInformation info) {
    RotateTransition rotateTransition =
        new RotateTransition(duration, getRobotImageView(info.getRobotType()));
    rotateTransition.setByAngle(info.getDegrees());
    rotateTransition.play();
  }

  /**
   * Method for animation: Repair.
   *
   * @param info AnimationInformation
   */
  public void repairRobot(AnimationInformation info) {
    ImageView imageView = getRobotImageView(info.getRobotType());
    if (imageView == null) {
      LOG.error("ImageView is Null");
      return;
    }
    ScaleTransition scaleTransition = new ScaleTransition(duration.divide(2), imageView);
    scaleTransition.setByX(1.3);
    scaleTransition.setByY(1.3);
    scaleTransition.setAutoReverse(true);
    scaleTransition.setCycleCount(2);

    ColorAdjust colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(0.5);
    colorAdjust.setContrast(0.5);
    colorAdjust.setHue(0.3);
    colorAdjust.setSaturation(0.5);

    imageView.setEffect(colorAdjust);

    PauseTransition pauseTransition = new PauseTransition(duration);
    pauseTransition.setOnFinished(event -> imageView.setEffect(null));

    ParallelTransition parallelTransition = new ParallelTransition();
    parallelTransition.getChildren().addAll(scaleTransition, pauseTransition);
    parallelTransition.play();
  }

  /**
   * Method for animation: Moving.
   *
   * @param info AnimationInformation
   */
  public void move(AnimationInformation info) {
    TranslateTransition translateTransition =
        new TranslateTransition(duration, getRobotImageView(info.getRobotType()));
    translateTransition.setByX((double) info.getTranslationXFactor() * tileSize);
    translateTransition.setByY((double) info.getTranslationYFactor() * tileSize);
    translateTransition.play();
  }

  /**
   * Method for animation: Hitting a wall.
   *
   * @param info AnimationInformation
   */
  public void wall(AnimationInformation info) {
    Direction direction = info.getDirection();
    ImageView robotImageView = getRobotImageView(info.getRobotType());
    TranslateTransition towardsWallTransition =
        new TranslateTransition((duration.divide(2)), robotImageView);
    TranslateTransition awayFromWallTransition =
        new TranslateTransition((duration.divide(2)), robotImageView);

    switch (direction) {
      case NORTH:
        towardsWallTransition.setByY(-0.3 * tileSize);
        awayFromWallTransition.setByY(0.3 * tileSize);
        break;
      case EAST:
        towardsWallTransition.setByX(0.3 * tileSize);
        awayFromWallTransition.setByX(-0.3 * tileSize);
        break;
      case SOUTH:
        towardsWallTransition.setByY(0.3 * tileSize);
        awayFromWallTransition.setByY(-0.3 * tileSize);
        break;
      case WEST:
        towardsWallTransition.setByX(-0.3 * tileSize);
        awayFromWallTransition.setByX(0.3 * tileSize);
        break;
      default:
        break;
    }
    SequentialTransition sequentialTransition = new SequentialTransition();
    sequentialTransition.getChildren().addAll(towardsWallTransition, awayFromWallTransition);
    sequentialTransition.play();
  }

  /**
   * Method for animation: Falling into pit.
   *
   * @param info AnimationInformation
   */
  public void fallIntoPit(AnimationInformation info) {
    Robots robot = info.getRobotType();
    ImageView robotImageView = getRobotImageView(robot);

    TranslateTransition translateTransition = new TranslateTransition(duration, robotImageView);
    translateTransition.setByX((double) info.getTranslationXFactor() * tileSize);
    translateTransition.setByY((double) info.getTranslationYFactor() * tileSize);

    ScaleTransition scaleTransition = new ScaleTransition(duration.divide(2), robotImageView);
    scaleTransition.setFromX(1.0);
    scaleTransition.setFromY(1.0);
    scaleTransition.setToX(0.0);
    scaleTransition.setToY(0.0);

    SequentialTransition sequentialTransition = new SequentialTransition();
    sequentialTransition.getChildren().addAll(translateTransition, scaleTransition);
    sequentialTransition.play();
  }

  /**
   * Method for animation: Laser Damage.
   *
   * @param info AnimationInformation
   */
  public void laserDamage(AnimationInformation info) {
    Robots robot = info.getRobotType();
    ImageView robotImageView = getRobotImageView(robot);
    applyRedOverlay(robotImageView);
    Transition shakeTransition = createShakeTransition(robotImageView);

    PauseTransition pauseTransition = new PauseTransition(Duration.seconds(3));
    pauseTransition.setOnFinished(event -> removeRedOverlay(robotImageView));

    ParallelTransition parallelTransition = new ParallelTransition();
    parallelTransition.getChildren().addAll(shakeTransition, pauseTransition);
    parallelTransition.play();
  }

  /**
   * Method for animation: Press.
   *
   * @param info the AnimationInformation
   */
  public void press(AnimationInformation info) {
    Robots robot = info.getRobotType();
    ImageView robotImageView = getRobotImageView(robot);

    ScaleTransition scaleTransition = new ScaleTransition(duration.divide(4), robotImageView);
    scaleTransition.setFromX(1.0);
    scaleTransition.setFromY(1.0);
    scaleTransition.setToX(1.2);
    scaleTransition.setToY(0.8);
    applyRedOverlay(robotImageView);

    ScaleTransition reverseScaleTransition =
        new ScaleTransition(duration.divide(4), robotImageView);
    reverseScaleTransition.setFromX(1.2);
    reverseScaleTransition.setFromY(0.8);
    reverseScaleTransition.setToX(1.0);
    reverseScaleTransition.setToY(1.0);

    PauseTransition pauseTransition = new PauseTransition(duration.divide(4));
    pauseTransition.setOnFinished(event -> removeRedOverlay(robotImageView));

    SequentialTransition sequentialTransition = new SequentialTransition();
    sequentialTransition
        .getChildren()
        .addAll(scaleTransition, reverseScaleTransition, pauseTransition);
    sequentialTransition.play();
  }

  /**
   * Method for animation: Displaying next card.
   *
   * @param cardType The type of the card
   */
  public void displayNextCard(CardType cardType, Robots robot) {
    updateGamePhaseText("Programmablauf");

    ImageView imageViewCard = getInfoImageView("nextCard", robot);
    ImageView imageViewCount = getInfoImageView("nextCardNumber", robot);
    if (CardType.numberForCardNeeded(cardType)) {
      Platform.runLater(
          () -> {
            if (imageViewCount != null) {
              imageViewCount.setImage(TextureAtlas.getNumberForCard(cardType));
            }
          });
    } else {
      Platform.runLater(
          () -> {
            if (imageViewCard != null) {
              imageViewCard.setImage(TextureAtlas.getAssets(41, 0));
            }
          });
    }
    Platform.runLater(
        () -> {
          if (imageViewCard != null) {
            imageViewCard.setImage(TextureAtlas.getSimpleCardImage(cardType));
          }
        });
  }

  /**
   * Clears the display of the next card.
   *
   * <p>This method sets the images of the next card numbers and next cards to a default image.
   */
  public void clearDisplayNextCard() {
    ImageView[] nextCardNumbers = getInfoImageView("nextCardNumber");
    ImageView[] nextCard = getInfoImageView("nextCard");
    Platform.runLater(
        () -> {
          for (ImageView imageView : nextCardNumbers) {
            if (imageView != null) {
              Platform.runLater(() -> imageView.setImage(TextureAtlas.getAssets(41, 0)));
            }
          }
          for (ImageView imageView : nextCard) {
            if (imageView != null) {
              Platform.runLater(() -> imageView.setImage(TextureAtlas.getAssets(41, 0)));
            }
          }
        });
  }

  /**
   * Updates the damage points of the specified robot.
   *
   * @param count The new damage point count.
   * @param robot The robot to update the damage points for.
   */
  public void updateDamagePoints(int count, Robots robot) {
    ImageView imageView = getInfoImageView("robotDamagePoints", robot);
    Platform.runLater(
        () -> {
          if (imageView != null) {
            imageView.setImage(TextureAtlas.getAssets(count + 28, 0));
          }
        });
  }

  /**
   * Updates the display of the player lives for a specific robot.
   *
   * @param count the number of lives to display
   * @param robot the robot to update the lives for
   */
  public void updatePlayerLives(int count, Robots robot) {
    ImageView imageView = getInfoImageView("playerLives", robot);
    Platform.runLater(
        () -> {
          if (imageView != null) {
            imageView.setImage(TextureAtlas.getAssets(41, 0));
            imageView.setImage(TextureAtlas.getAssets(count + 28, 0));
          }
        });
  }

  /**
   * Updates the display of the checkpoint for a specific robot.
   *
   * @param count the number of checkpoints to display
   * @param robot the robot to update the checkpoint for
   */
  public void updateCheckpoint(int count, Robots robot) {
    ImageView imageView = getInfoImageView("checkpoint", robot);
    if (count == 0) {
      Platform.runLater(
          () -> {
            if (imageView != null) {
              imageView.setImage(TextureAtlas.getAssets(39, 0));
            }
          });
    } else {
      Platform.runLater(
          () -> {
            if (imageView != null) {
              imageView.setImage(TextureAtlas.getAssets(count + 7, 0));
            }
          });
    }
  }

  /**
   * Updates the position of the archive marker for a specific robot.
   *
   * @param robot the robot to update the archive marker for
   * @param xCordTile the x-coordinate of the tile where the marker should be moved to
   * @param yCordTile the y-coordinate of the tile where the marker should be moved to
   */
  @SuppressWarnings({"checkstyle:LocalVariableName", "checkstyle:ParameterName"})
  public void updateArchiveMarker(Robots robot, int xCordTile, int yCordTile) {
    int xCord = xCordTile * tileSize;
    int yCord = yCordTile * tileSize;
    ImageView imageView = getMarkerImageView(robot);
    TranslateTransition translateTransition = new TranslateTransition(duration, imageView);
    translateTransition.setToX(xCord);
    translateTransition.setToY(yCord);
    translateTransition.play();
  }

  /**
   * Method for animation: Updating the game phase text.
   *
   * @param text the text to display
   */
  public void updateGamePhaseText(String text) {
    Platform.runLater(() -> gamePhase.setText(text));
  }

  private void applyRedOverlay(ImageView robotImageView) {
    ColorAdjust colorAdjust = new ColorAdjust();
    colorAdjust.setBrightness(0.5);
    colorAdjust.setContrast(0.5);
    colorAdjust.setHue(-0.05);
    colorAdjust.setSaturation(0.5);
    if (robotImageView != null) {
      robotImageView.setEffect(colorAdjust);
    }
  }

  private void removeRedOverlay(ImageView robotImageView) {
    robotImageView.setEffect(null);
  }

  private Transition createShakeTransition(ImageView robotImageView) {
    RotateTransition shakeTransition = new RotateTransition(Duration.millis(100), robotImageView);
    shakeTransition.setCycleCount(4);
    shakeTransition.setAutoReverse(true);
    shakeTransition.setByAngle(10);
    return shakeTransition;
  }

  // Animations End

  public int getTileSize() {
    return tileSize;
  }

  /**
   * Open the respawn window for the direction and position selection in the respawn process. The
   * methode start the local timer and show the 3x3 playground on the gui.
   *
   * @param message from the server.
   */
  @Subscribe
  @SuppressWarnings({"java:S3776", "java:S3776"})
  public void onRespawnInteractionMessage(RespawnInteractionMessage message) {
    if (!message.getGameId().equals(gameId)) {
      return;
    }
    if (message.getUser().getUUID().equals(loggedInUser.getUser().getUUID()) && !tabClosed) {
      // Blur effect for the background
      MotionBlur motionBlur = new MotionBlur();
      motionBlur.setRadius(BLUR_RADIUS_VALUE);
      mainPane.setEffect(motionBlur);
      robotDirection = message.getDirection();
      isOnlyDirection = false;
      robotRespawnPane.setVisible(true);

      // Set the local visible timer for the user
      timeInSeconds = message.getTime() / 1000;
      txtRespawnTimer.setText(NAME_OF_TIMER_LABEL + timeInSeconds);

      Timeline timeline = new Timeline();
      timeline.setCycleCount(Animation.INDEFINITE);
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.seconds(1),
              event -> {
                timeInSeconds--;
                txtRespawnTimer.setText(NAME_OF_TIMER_LABEL + timeInSeconds);
                if (isCountdownFinished()) {
                  timeline.stop();
                  robotRespawnPane.setVisible(false);
                  mainPane.setEffect(null);
                }
              });

      timeline.getKeyFrames().add(keyFrame);
      timeline.playFromStart();

      newRespawnSelection = 4;

      // Show the 3x3 playground for interaction wih the user
      int counter = 0;
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          ImageView imageView = getElementFromGridPane(j, i, respawnFieldGridPane);
          if (imageView != null) {
            imageView.setImage(
                TextureAtlas.getTileJavaFx(
                    message.getFields()[counter][0], message.getFields()[counter][1] * 90));
          } else {
            LOG.error("The ImageView is null!!");
          }
          if (counter != 4 && message.getFields()[counter][0] != -1) {
            Button button = (Button) getNodeByRowColumnIndex(i, j, respawnButtonsGridPane);
            button.setDisable(false);
            button.setGraphic(null);
            button.setRotate((double) message.getDirection().getNumber() * 90);
            button.setStyle("-fx-background-color: transparent");
          } else {
            Button button = (Button) getNodeByRowColumnIndex(i, j, respawnButtonsGridPane);
            button.setRotate((double) message.getDirection().getNumber() * 90);
            button.setGraphic(null);
            button.setDisable(true);
            button.setStyle("-fx-background-color: rgba(201,0,0,0.48)");
          }
          counter++;
        }
      }

      // set the robo from the user on the 3x3 playground
      Button button = (Button) getNodeByRowColumnIndex(1, 1, respawnButtonsGridPane);
      gameDTO.ifPresentOrElse(
          dto ->
              Platform.runLater(
                  () -> {
                    button.setGraphic(
                        new ImageView(
                            TextureAtlas.getAssets(
                                dto.getPlayer(loggedInUser.getUser())
                                    .getRobot()
                                    .getType()
                                    .ordinal(),
                                0)));
                    button.setRotate((double) message.getDirection().getNumber() * 90);
                  }),
          () -> LOG.error(GAME_DTO_IS_NOT_PRESENT));

      LOG.debug("The Array from the Server: {}", (Object) message.getFields());
    }
  }

  /**
   * When the server is finish with the GameLogic, the User can request a next round. The button a
   * show here.
   *
   * @param message from the server.
   */
  @Subscribe
  public void onWaitForNextRoundMessage(WaitForNextRoundMessage message) {
    if (!message.getGameId().equals(gameId)) {
      return;
    }
    if (!tabClosed) {
      gameDTO.ifPresentOrElse(
          dto -> {
            btnForceNextRound.setVisible(dto.getLobby().getOwner().equals(loggedInUser.getUser()));
            btnNextRound.setVisible(true);
          },
          () -> LOG.error(GAME_DTO_IS_NOT_PRESENT));
    }
  }

  /**
   * Open the respawn window for the direction selection in the respawn process.
   *
   * @param message from the server.
   */
  @Subscribe
  @SuppressWarnings("java:S3776")
  public void onRespawnDirectionInteractionMessage(RespawnDirectionInteractionMessage message) {
    if (!message.getGameId().equals(gameId)) {
      return;
    }
    if (message.getUser().getUUID().equals(loggedInUser.getUser().getUUID()) && !tabClosed) {
      LOG.debug("The Array from the Server: {}", (Object) message.getFields());
      MotionBlur motionBlur = new MotionBlur();
      motionBlur.setRadius(BLUR_RADIUS_VALUE);
      mainPane.setEffect(motionBlur);
      robotDirection = message.getDirection();
      robotRespawnPane.setVisible(true);

      timeInSeconds = message.getTime() / 1000;
      txtRespawnTimer.setText(NAME_OF_TIMER_LABEL + timeInSeconds);

      Timeline timeline = new Timeline();
      timeline.setCycleCount(Animation.INDEFINITE);
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.seconds(1),
              event -> {
                timeInSeconds--;
                txtRespawnTimer.setText(NAME_OF_TIMER_LABEL + timeInSeconds);
                if (isCountdownFinished()) {
                  timeline.stop();
                  robotRespawnPane.setVisible(false);
                  mainPane.setEffect(null);
                }
              });

      timeline.getKeyFrames().add(keyFrame);
      timeline.playFromStart();

      isOnlyDirection = true;
      int counter = 0;
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          ImageView imageView = getElementFromGridPane(j, i, respawnFieldGridPane);
          if (message.getFields()[counter][0] != -1) {
            if (imageView != null) {
              imageView.setImage(
                  TextureAtlas.getTileJavaFx(
                      message.getFields()[counter][0], message.getFields()[counter][1] * 90));
            } else {
              LOG.error("The ImageView is Null!");
            }
          } else {
            if (imageView != null) {
              imageView.setImage(TextureAtlas.getTileJavaFx(97, 0));
            } else {
              LOG.error("The ImageView is Null!");
            }
          }

          counter++;
          Button button = (Button) getNodeByRowColumnIndex(j, i, respawnButtonsGridPane);
          button.setDisable(true);
          button.setStyle("-fx-background-color: transparent");
          button.setRotate((double) robotDirection.getNumber() * 90);
        }
      }
      gameDTO.ifPresentOrElse(
          dto ->
              Platform.runLater(
                  () ->
                      ((Button) getNodeByRowColumnIndex(1, 1, respawnButtonsGridPane))
                          .setGraphic(
                              new ImageView(
                                  TextureAtlas.getAssets(
                                      dto.getPlayer(loggedInUser.getUser())
                                          .getRobot()
                                          .getType()
                                          .ordinal(),
                                      0)))),
          () -> LOG.error(GAME_DTO_IS_NOT_PRESENT));
    }
  }

  private ImageView getElementFromGridPane(int x, int y, GridPane gridPane) {
    Node node = null;
    ObservableList<Node> children = gridPane.getChildren();

    for (Node child : children) {
      if (GridPane.getColumnIndex(child) == x && GridPane.getRowIndex(child) == y) {
        node = child;
        break;
      }
    }

    if (node != null) {
      return (ImageView) node;
    } else {
      return null;
    }
  }

  /**
   * Private Method to calculate the size in px of one tile of the floor-field.
   *
   * @param rows Number of rows
   * @param cols Number of cols
   * @return the size in px of one Image
   */
  private int calculateImageSize(int rows, int cols) {
    final int imageSize = TextureAtlas.getResolution();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = (int) (screenSize.getWidth() * 0.75);
    int screenHeight = (int) (screenSize.getHeight() * 0.75);
    int boardSize;
    if (screenWidth > screenHeight) { // horizontal Monitor
      boardSize = screenHeight / rows;
    } else { // vertical
      boardSize = screenWidth / cols;
    }
    if (boardSize > imageSize) {
      boardSize = imageSize;
    }
    while (rows * boardSize > screenHeight) {
      boardSize--;
    }
    while (cols * boardSize > screenWidth) {
      boardSize--;
    }
    return boardSize;
  }

  /**
   * The method is called when the Enter key is pressed in the text field.
   *
   * <p>When the Enter key is pressed, the same thing is done as with onSend.
   *
   * @param actionEvent The ActionEvent created by pressing the enter key.
   * @see de.uol.swp.client.chat.ClientChatService
   */
  @FXML
  public void onEnter(ActionEvent actionEvent) {
    onSend(actionEvent);
  }

  /**
   * Method called when the send button is pressed
   *
   * <p>If the send button is pressed, this method request the ClientChatService to send the server
   * a new Message request. If there is no text in the text field, no Message will be written.
   *
   * @param actionEvent The ActionEvent created by pressing the send button
   * @see de.uol.swp.client.chat.ClientChatService
   */
  @FXML
  public void onSend(ActionEvent actionEvent) {
    initializeGameChat();
    if (!messageFieldGame.getText().isBlank()) {
      clientChatService.sendMessage(messageFieldGame.getText(), gameId);
      messageFieldGame.clear();
    }
    actionEvent.consume();
  }

  private void initializeGameChat() {
    Platform.runLater(
        () -> {
          if (gameChatMessages == null) {
            gameChatMessages = FXCollections.observableArrayList();
            gameChat.setItems(gameChatMessages);
            gameChat.setCellFactory(
                stringListView -> {
                  GameChatCell textListCell = new GameChatCell();
                  textListCell.setMaxHeight(1000);
                  textListCell.setMaxWidth(gameChat.getWidth());
                  textListCell.setWrapText(true);
                  return textListCell;
                });
          }
        });
  }

  /**
   * This method is triggered when a new ChatMessageMessage is received.
   *
   * <p>It initializes the game chat and displays the message in the chat interface if it is meant
   * for the current game. It also updates the number of new messages if the chat is currently
   * closed and updates the open chat button text to display the number of new messages.
   *
   * @param chatMessageMessage - The ChatMessageMessage that triggered the method
   */
  @Subscribe
  private void onNewMessage(ChatMessageMessage chatMessageMessage) {
    if (!chatMessageMessage.getChatMessage().getLobbyId().equals(gameId)) {
      return;
    }
    if (!tabClosed) {
      initializeGameChat();
      if (chatMessageMessage.getChatMessage().getLobbyId().equals(gameId)) {
        Text text = new Text(chatMessageMessage.getChatMessage().toString());
        text.setWrappingWidth(gameChat.getWidth() - 20);
        Platform.runLater(
            () -> {
              gameChatMessages.add(text);
              if (!mouseOnChat) {
                gameChat.scrollTo(gameChat.getItems().size() - 1);
              }
            });
        if (!chatOpen) {
          numberOfNewMessages++;
          newMessageAnimation(true);
          Platform.runLater(() -> openChatButton.setText("Chat (" + numberOfNewMessages + ")"));
        }
      }
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
    gameChat.scrollTo(gameChat.getItems().size() - 1);
  }

  /**
   * Handles the LobbyLeaveSuccessfulResponse found on the Eventbus.
   *
   * <p>If a User Leaves the Lobby of the Game the GameView is also left
   *
   * @param rsp the LobbyLeaveSuccessfulResponse found on the Eventbus
   */
  @Subscribe
  private void onLobbyLeaveSuccessfulResponse(LobbyLeaveSuccessfulResponse rsp) {
    if (rsp.getUser().getUUID().equals(loggedInUser.getUser().getUUID())
        && rsp.getLobbyId().equals(gameId)
        && !tabClosed) {
      Platform.runLater(() -> gameTab.getTabPane().getTabs().remove(gameTab));
      tabClosed = true;
    }
  }

  /**
   * Method is called when the leaveGame Button is pressed.
   *
   * @param event fired when the leave Button is pressed
   */
  @FXML
  public void onLeave(ActionEvent event) {
    if (closeConfirmed()) {
      gameTab.getTabPane().getTabs().remove(gameTab);
      onClose();
    } else {
      event.consume();
    }
  }

  /**
   * Method that is called when the close button is pressed.
   *
   * <p>It leaves the current lobby and stops the new message animation.
   */
  @FXML
  public void onClose() {
    lobbyService.leaveLobby(gameId, loggedInUser.getUser());
    newMessageAnimation(false);
    tabClosed = true;
  }

  /**
   * Event-handler method that is called when the user activates the close button of the game tab. A
   * confirmation alert is displayed to ensure that the user really wants to exit the game. Progress
   * will be lost. The user has the option to continue the game or exit it.
   *
   * @param event The event triggered by closing the game tab
   */
  @FXML
  public void onCloseRequest(Event event) {
    if (closeConfirmed()) {
      gameTab.getTabPane().getTabs().remove(gameTab);
    } else {
      event.consume();
    }
  }

  /**
   * Displays a confirmation alert to ensure that the user really wants to exit the game. Progress
   * will be lost. The user has the option to continue the game or exit it.
   *
   * @return a boolean value indicating whether the user wants to exit or not
   */
  private boolean closeConfirmed() {
    Alert alert =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Möchtest du das Spiel wirklich verlassen?"
                + " Der Spielstand geht dabei verloren."
                + " Klicke auf 'Abbrechen', um das Spiel fortzusetzen"
                + " oder 'OK', um das Spiel zu beenden");
    alert.setTitle("Bitte bestätigen");
    DialogPane pane = alert.getDialogPane();
    pane.getStylesheets().add(DIALOG_STYLE_SHEET);
    Optional<ButtonType> result = alert.showAndWait();
    return (result.isPresent() && result.get() == ButtonType.OK);
  }

  /**
   * This method is triggered when a ChatCommandResponse message is received.
   *
   * <p>It displays the message in the chat interface if it is meant for the current game and the
   * user is logged in. It also updates the number of new messages if the chat is currently closed
   * and updates the open chat button text to display the number of new messages.
   *
   * @param msg - The ChatCommandResponse message that triggered the method
   */
  @Subscribe
  public void onChatCommandResponse(ChatCommandResponse msg) {
    if (msg.getChatMassage().getLobbyId().equals(gameId)
        && loggedInUser.getUser() != null
        && !tabClosed) {
      Text text = new Text(msg.getChatMassage().toString());
      text.setWrappingWidth(gameChat.getWidth());
      Platform.runLater(() -> gameChatMessages.add(text));
      if (!chatOpen) {
        numberOfNewMessages++;
        newMessageAnimation(true);
        Platform.runLater(() -> openChatButton.setText("Chat (" + numberOfNewMessages + ")"));
      }
    }
  }

  /**
   * This method is triggered when the "close chat" button is clicked.
   *
   * <p>It animates the closing of the chat interface and hides the game chat pane, while showing
   * the open chat button. The text of the open chat button is set to "Chat" and the number of new
   * messages is reset to 0.
   *
   * @param event - The ActionEvent that triggered the method
   */
  @FXML
  public void onChatClose(ActionEvent event) {
    closeChatButton.setText("");
    animateSlide(
        gameChatPane,
        closeChatButton,
        109,
        -39,
        26,
        (event1 -> {
          gameChatPane.setVisible(false);
          openChatButton.setVisible(true);
          closeChatButton.setText("Chat");
        }),
        0,
        0.5);
    chatOpen = false;
    numberOfNewMessages = 0;
    event.consume();
  }

  /**
   * This method is triggered when the "open chat" button is clicked.
   *
   * <p>It makes the game chat pane visible, hides the open chat button, and animates the opening of
   * the chat interface. It also sets the chatOpen variable to true, and sets the text of the close
   * chat button to "Schließen" when the animation is finished.
   *
   * @param event - The ActionEvent that triggered the method
   */
  @FXML
  @SuppressWarnings("java:S1192")
  public void onChatOpen(ActionEvent event) {
    newMessageAnimation(false);
    closeChatButton.setText("");
    gameChatPane.setVisible(true);
    openChatButton.setVisible(false);
    chatOpen = true;
    animateSlide(
        gameChatPane,
        closeChatButton,
        235,
        -105,
        26,
        (event1 -> closeChatButton.setText("Schließen")),
        -(gameChatPane.getWidth() - closeChatButton.getHeight()),
        0.5);
    event.consume();
  }

  /** Called when a card gets clicked. * */
  @FXML
  @SuppressWarnings("java:S1192")
  public void onMouseClick(MouseEvent mouseEvent) {
    ImageView view = (ImageView) mouseEvent.getSource();
    if (!isInGridPaneSelect(view)) {
      return;
    }
    int index;
    boolean blockerCard = true;
    if (GridPane.getColumnIndex(view) == null) {
      index = 0;
    } else {
      index = GridPane.getColumnIndex(view);
    }
    if (isInGridPaneSelect(view) && !blockedCardSlots[index]) {
      blockerCard = false;
    }
    if (blockerCard && isInGridPaneSelect(view) && unlockingProcess) {
      gameService.sendUnblockCardRequest(loggedInUser.getUser().getUsername(), gameId, index);
      canUnblock--;
      if (canUnblock <= 0) {
        canUnblock = 0;
        unlockingProcess = false;
        resetUnblockButton();
      } else {
        btnCloseCardSelect.setText("WÄHLE " + canUnblock + " KARTE ZUM ENTSPERREN!");
      }
    }
    mouseEvent.consume();
  }

  /** Called when there is a drag action on the element. * */
  @FXML
  public void onDragDetectedCard(MouseEvent mouseEvent) {
    ImageView view = (ImageView) mouseEvent.getSource();
    if (!isInGridPaneDepot(view) && !isInGridPaneSelect(view)) {
      return;
    }
    int index;
    boolean blockerCard = true;
    if (GridPane.getColumnIndex(view) == null) {
      index = 0;
    } else {
      index = GridPane.getColumnIndex(view);
    }
    if (isInGridPaneDepot(view) && index < unlockedCardSlots) {
      blockerCard = false;
    }
    if (isInGridPaneSelect(view) && !blockedCardSlots[index]) {
      blockerCard = false;
    }
    if (GridPane.getRowIndex(view) == null && (index < programmingCardDepotArray.length)) {
      blockerCard = false;
    }
    if (!blockerCard) {
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
   * <p>Check if the droppable item is a valid item from the two GridPanes.
   */
  @FXML
  public void onDragOverView(DragEvent dragEvent) {
    ImageView source = (ImageView) dragEvent.getSource();
    int index;
    if (GridPane.getColumnIndex(source) == null) {
      index = 0;
    } else {
      index = GridPane.getColumnIndex(source);
    }
    if (dragEvent.getDragboard().hasString()
        && (isInGridPaneSelect(source) || isInGridPaneDepot(source))
        && (!isInGridPaneDepot(source) || index < programmingCardDepotArray.length)
        && (!isInGridPaneSelect(source) || !blockedCardSlots[index])) {
      dragEvent.acceptTransferModes(TransferMode.MOVE);
    }
    dragEvent.consume();
  }

  /** Open the CardSelectPane, when the button "Karten" is pressed. */
  @FXML
  public void onOpenCardSelect(ActionEvent event) {
    openCardSelection(true);
    event.consume();
  }

  private void openCardSelection(boolean setButton) {
    btnCloseCardSelect.setText("");
    gameCardPane.setVisible(true);
    btnOpenCardSelect.setVisible(false);
    animateSlide(
        gameCardPane,
        btnCloseCardSelect,
        360,
        -147,
        51.2,
        (event1 -> {
          if (setButton) {
            btnCloseCardSelect.setStyle("-fx-font-size: 20px;");
            btnCloseCardSelect.setText("Schließen");
          }
        }),
        -(gameCardPane.getWidth() - btnCloseCardSelect.getHeight()),
        1);
    btnCloseCardSelect.setOnAction(
        event1 -> {
          validateAndSendCardSelection();
          onCloseCardSelect(event1);
        });
  }

  /** Closed the CardSelectPane, when the Button "Schließen" from CardPane is pressed. */
  @FXML
  public void onCloseCardSelect(ActionEvent event) {
    btnCloseCardSelect.setText("");
    animateSlide(
        gameCardPane,
        btnCloseCardSelect,
        94.4,
        -9,
        38.4,
        (event1 -> {
          gameCardPane.setVisible(false);
          btnOpenCardSelect.setVisible(true);
          btnCloseCardSelect.setStyle("-fx-font-size: 14px;");
          btnCloseCardSelect.setText("Karten");
        }),
        0,
        1);
    event.consume();
  }

  /**
   * Called when there is a drop action on the item.
   *
   * <p>Switch the Images and the Array with Source and Target Element/Number
   */
  @SuppressWarnings({"java:S6541", "java:S3776"})
  @FXML
  public void onDragDroppedView(DragEvent dragEvent) {
    Dragboard db = dragEvent.getDragboard();
    boolean success = false;
    if (db.hasString()) {
      ImageView targetView = (ImageView) dragEvent.getGestureTarget();
      ImageView sourceView = (ImageView) dragEvent.getGestureSource();

      int posSource;
      if (GridPane.getColumnIndex(sourceView) == null) {
        posSource = 0;
      } else {
        posSource = GridPane.getColumnIndex(sourceView);
      }
      int posTarget;
      if (GridPane.getColumnIndex(targetView) == null) {
        posTarget = 0;
      } else {
        posTarget = GridPane.getColumnIndex(targetView);
      }
      ProgrammingCard cardSource;
      ProgrammingCard cardTarget;

      if (isInGridPaneSelect(sourceView) && isInGridPaneSelect(targetView)) {
        // Drop Item is from and to GridPaneSelect
        cardSource = programmingCardSelectionArray[posSource];
        cardTarget = programmingCardSelectionArray[posTarget];
        if (cardTarget == null
            || cardSource == null
            || !(firstRoundfirstCard
                && ((!cardSource.isMoveCard() && posTarget == 0)
                    || (!cardTarget.isMoveCard() && posSource == 0)))) {
          programmingCardSelectionArray[posTarget] = cardSource;
          programmingCardSelectionArray[posSource] = cardTarget;
          switchImage(targetView, sourceView);
        }
      } else if (isInGridPaneDepot(sourceView) && isInGridPaneDepot(targetView)) {
        // Drop Item is from and to GridPaneDepot
        cardSource = programmingCardDepotArray[posSource];
        cardTarget = programmingCardDepotArray[posTarget];
        programmingCardDepotArray[posTarget] = cardSource;
        programmingCardDepotArray[posSource] = cardTarget;
        switchImage(targetView, sourceView);
      } else if (isInGridPaneDepot(sourceView) && isInGridPaneSelect(targetView)) {
        // Drop Item is from GridPaneDepot to GridPaneSelect
        cardSource = programmingCardDepotArray[posSource];
        cardTarget = programmingCardSelectionArray[posTarget];
        if (!(firstRoundfirstCard
            && ((cardSource == null || !cardSource.isMoveCard()) && posTarget == 0))) {
          programmingCardDepotArray[posSource] = cardTarget;
          programmingCardSelectionArray[posTarget] = cardSource;
          switchImage(targetView, sourceView);
        }
      } else if (isInGridPaneDepot(targetView) && isInGridPaneSelect(sourceView)) {
        // Drop Item is from GridPaneSelect to GridPaneDepot
        cardSource = programmingCardSelectionArray[posSource];
        cardTarget = programmingCardDepotArray[posTarget];
        if (cardTarget == null
            || !(firstRoundfirstCard && (!cardTarget.isMoveCard() && posSource == 0))) {
          programmingCardSelectionArray[posSource] = cardTarget;
          programmingCardDepotArray[posTarget] = cardSource;
          switchImage(targetView, sourceView);
        }
      }

      if (checkCardSelectDone()) {
        btnOpenCardSelect.setId(BTN_CLOSE_CARD_SELECT_ID);
        btnCloseCardSelect.setId(BTN_CLOSE_CARD_SELECT_ID);
      } else {
        btnOpenCardSelect.setId("");
        btnCloseCardSelect.setId("");
      }
      success = true;
    }
    dragEvent.setDropCompleted(success);
    dragEvent.consume();
  }

  /**
   * Send a request to start a next round.
   *
   * @param event action event
   */
  @FXML
  public void nextRoundStartRequest(ActionEvent event) {
    gameDTO.ifPresentOrElse(
        dto -> eventBus.post(new NextRoundStartRequest(dto.getGameId(), loggedInUser.getUser())),
        () -> LOG.error(GAME_DTO_IS_NOT_PRESENT));
    event.consume();
  }

  /**
   * Send a request to force a next round. Only for LobbyOwner.
   *
   * @param event action event
   */
  @FXML
  public void nextRoundStartForceRequest(ActionEvent event) {
    if (gameDTO.isPresent()) {
      btnForceNextRound.setVisible(false);
      btnForceNextRound.setText("Nächste Runde jetzt erzwingen");
      eventBus.post(
          new NextRoundStartForceRequest(gameDTO.get().getGameId(), loggedInUser.getUser()));
    }
    event.consume();
  }

  /**
   * Rotate the robot on the respawn window to the left.
   *
   * @param event action event
   */
  @FXML
  public void robotOnLeftRotation(ActionEvent event) {
    robotDirection = robotDirection.rotate(Direction.WEST);
    Platform.runLater(
        () ->
            respawnButtonsGridPane
                .getChildren()
                .forEach(node -> node.setRotate((double) robotDirection.getNumber() * 90)));
    event.consume();
  }

  /**
   * Rotate the robot on the respawn window to the right.
   *
   * @param event action event
   */
  @FXML
  public void robotOnRightRotation(ActionEvent event) {
    robotDirection = robotDirection.rotate(Direction.EAST);
    Platform.runLater(
        () ->
            respawnButtonsGridPane
                .getChildren()
                .forEach(node -> node.setRotate(((double) robotDirection.getNumber()) * 90)));
    event.consume();
  }

  /**
   * This Methode send the user input from the respawn process to the server.
   *
   * @param event action event
   */
  @FXML
  public void onSendRobotSelection(ActionEvent event) {
    if (gameDTO.isPresent()) {
      if (isOnlyDirection) {
        eventBus.post(
            new RespawnDirectionInteractionRequest(
                gameDTO.get().getGameId(), loggedInUser.getUser(), robotDirection));
        robotRespawnPane.setVisible(false);
        mainPane.setEffect(null);
      } else {
        if (newRespawnSelection >= 0 && newRespawnSelection <= 8) {
          eventBus.post(
              new RespawnInteractionRequest(
                  gameDTO.get().getGameId(),
                  loggedInUser.getUser(),
                  newRespawnSelection,
                  robotDirection));
          LOG.debug("{}  # {}", robotDirection, newRespawnSelection);
          robotRespawnPane.setVisible(false);
          mainPane.setEffect(null);
        } else {
          Alert alert =
              new Alert(
                  AlertType.INFORMATION,
                  "Du hast nicht das richtige Feld ausgewählt. Bitte ändern");
          alert.setTitle("Bitte bestätigen");
          DialogPane pane = alert.getDialogPane();
          pane.getStylesheets().add(DIALOG_STYLE_SHEET);
          alert.showAndWait();
        }
      }
    } else {
      LOG.error(GAME_DTO_IS_NOT_PRESENT);
    }
    event.consume();
  }

  private void newMessageAnimation(boolean value) {
    if (value) {
      newMessageAnimation.play();
    } else {
      newMessageAnimation.stop();
      openChatButton.setBackground(closeChatButton.getBackground());
    }
  }

  /**
   * Slide IN and OUT Animation from Panes.
   *
   * @param pane the selected Pane
   * @param button the close Button from Pane
   * @param widthProperty change the Height (Button is rotated at -90 Degree)
   * @param layoutPropertyX move the button to the new x Value of the Pane after resize the Height
   * @param heightProperty change value, to fit in pane after resize the Height
   * @param onFinishedEvent triggered after finishes the Animation
   * @param valueX the new x Value from the Pane on the MainView
   */
  @SuppressWarnings("java:S107")
  private void animateSlide(
      AnchorPane pane,
      Button button,
      double widthProperty,
      double layoutPropertyX,
      double heightProperty,
      EventHandler<ActionEvent> onFinishedEvent,
      double valueX,
      double duration) {
    // Create a TranslateTransition object to animate the opening of the chat interface
    TranslateTransition slide = new TranslateTransition(Duration.seconds(duration), pane);
    slide.setToX(valueX);

    // Create a Timeline object to change the width and layout of the close chat button
    Timeline timeline = new Timeline();
    timeline
        .getKeyFrames()
        .add(
            new KeyFrame(
                Duration.seconds(duration),
                new KeyValue(button.prefWidthProperty(), widthProperty),
                new KeyValue(button.prefHeightProperty(), heightProperty),
                new KeyValue(button.layoutXProperty(), layoutPropertyX)));
    timeline.play();
    slide.play();
    timeline.setOnFinished(onFinishedEvent);
  }

  /**
   * Handles the ProgrammingCardMessage found on the Eventbus.
   *
   * @param msg ProgrammingCardMessage
   */
  @Subscribe
  public void onProgrammingCardMessage(ProgrammingCardMessage msg) {
    if (gameDTO.isPresent() && !tabClosed && msg.getGameId().equals(gameId)) {
      btnForceNextRound.setVisible(false);
      btnNextRound.setVisible(false);
      btnOpenCardSelect.setId("");
      btnCloseCardSelect.setId("");
      if (gameDTO.get().getLobby().getOptions().isSwitchOffRoboter()) {
        robotShutdown.setVisible(true);
        txtShutOff.setVisible(true);
      }
      Player playerMsg;
      Player playerLocal;
      gameService.retrieveGameDTO(gameId);
      countDown();
      try {
        playerMsg = msg.getPlayer(loggedInUser.getUser());
        playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      } catch (NoSuchElementException e) {
        return;
      }
      if (playerMsg.equals(playerLocal)) {
        LOG.debug("Received new ProgrammingCardMessage. Calling refreshShownCards()");
        playerLocal.getRobot().setHandCards(msg.getCards(playerMsg));
        playerLocal.getRobot().setBlocked(msg.getBlocked(playerMsg));
        blockedCardSlots = playerLocal.getRobot().getBlocked();
        for (int i = 0; i < playerLocal.getRobot().getSelectedCards().length; i++) {
          if (!playerLocal.getRobot().isBlocked(i)) {
            playerLocal.getRobot().getSelectedCards()[i] = null;
          }
        }
        refreshShownCards(msg.getCards(playerMsg));
      }
    }
  }

  /**
   * Handles the unblock cards response.
   *
   * @param msg UnblockCardResponse
   */
  @Subscribe
  public void onUnblockCardResponse(UnblockCardResponse msg) {
    if (gameDTO.isPresent() && !tabClosed && msg.getGameId().equals(gameId)) {
      Player playerMsg;
      Player playerLocal;
      try {
        playerMsg = gameDTO.get().getPlayer(msg.getUser());
        playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      } catch (NoSuchElementException e) {
        return;
      }
      if (playerMsg.equals(playerLocal)) {
        LOG.debug("Received new UnblockCardSlotResponse. Setting Blocked Card Slots");
        playerLocal.getRobot().setBlocked(msg.getBlocked());
        blockedCardSlots = playerLocal.getRobot().getBlocked();
        refreshBlockedCards();
      }
    }
  }

  /**
   * Handles the ProgrammingCardMessage found on the Eventbus.
   *
   * @param msg ProgrammingCardMessage
   */
  @Subscribe
  public void onUnblockCardInteractionMessage(UnblockCardInteractionMessage msg) {
    if (gameDTO.isPresent() && !tabClosed && msg.getGameId().equals(gameId)) {
      Player playerMsg;
      Player playerLocal;
      gameService.retrieveGameDTO(gameId);
      try {
        playerMsg = gameDTO.get().getPlayer(msg.getUser());
        playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      } catch (NoSuchElementException e) {
        return;
      }
      if (playerMsg.equals(playerLocal)) {
        LOG.debug("Received new UnblockCardSlotRequest. Calling selectCardToUnblock()");
        playerLocal.getRobot().setBlocked(msg.getBlocked());
        blockedCardSlots = playerLocal.getRobot().getBlocked();
        refreshBlockedCards();
        selectCardToUnblock(msg.getAmount());
      }
    }
  }

  private void selectCardToUnblock(int amount) {
    unlockingProcess = true;
    Platform.runLater(
        () -> {
          openCardSelection(false);
          canUnblock = amount;
          if (amount > 1) {
            btnCloseCardSelect.setText(
                "WÄHLE "
                    + amount
                    + " KARTEN ZUM ENTSPERREN ("
                    + getTimelimitInSecondsFromOptions()
                    + ")");
          } else {
            btnCloseCardSelect.setText(
                "WÄHLE "
                    + amount
                    + " KARTE ZUM ENTSPERREN ("
                    + getTimelimitInSecondsFromOptions()
                    + ")");
          }
          btnCloseCardSelect.setStyle("-fx-background-color: #d18100;");
          btnCloseCardSelect.setDisable(true);
          blinkButton();
        });
    unblockTimer();
  }

  private void blinkButton() {
    fadeUnblock = new FadeTransition(Duration.millis(1000), btnCloseCardSelect);
    fadeUnblock.setFromValue(1.0);
    fadeUnblock.setToValue(0.5);
    fadeUnblock.setCycleCount(20); // 20 Sekunden
    fadeUnblock.setAutoReverse(true);
    fadeUnblock.play();
  }

  private void refreshShownCards(ProgrammingCard[] programmingCardDepotArray) {
    LOG.debug("Refreshing {} card(s)", programmingCardDepotArray.length);
    unlockedCardSlots = 9;
    programmingCardSelectionArray = new ProgrammingCard[5];
    this.programmingCardDepotArray = programmingCardDepotArray;
    for (int i = 0; i < gridCardSelect.getColumnCount(); i++) {
      if (programmingCardSelectionArray[i] != null) {
        ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
            .setImage(
                getCard(
                    programmingCardSelectionArray[i].getCardType().ordinal(),
                    programmingCardSelectionArray[i].getCount()));
      } else {
        ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
            .setImage(getCard(8, 0)); // Empty
      }
    }

    refreshBlockedCards();

    if (programmingCardDepotArray.length > 5) {
      LOG.debug("Received {} Cards. No locked Registers", programmingCardDepotArray.length);
    }

    for (int i = 0; i < gridCardDepot.getColumnCount(); i++) {
      if (programmingCardDepotArray.length > i) {
        ((ImageView) getNodeByRowColumnIndex(0, i, gridCardDepot))
            .setImage(
                getCard(
                    programmingCardDepotArray[i].getCardType().ordinal(),
                    programmingCardDepotArray[i].getCount()));
      } else {
        ((ImageView) getNodeByRowColumnIndex(0, i, gridCardDepot))
            .setImage(getCard(7, 0)); // Blocker
        unlockedCardSlots--;
      }
    }
  }

  @SuppressWarnings({"java:S2629", "java:S1192", "java:S3776"})
  private void refreshBlockedCards() {
    if (gameDTO.isPresent()) {
      Player playerLocal;
      try {
        playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      } catch (NoSuchElementException e) {
        LOG.error("Player is not present. Can't Block Card Slots.");
        return;
      }
      StringBuilder blocked = new StringBuilder();
      for (int i = 0; i < 5; i++) {
        ProgrammingCard slotted = playerLocal.getRobot().getSelectedCards()[i];
        if (playerLocal.getRobot().isBlocked(i)) {
          if (slotted != null) {
            ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
                .setImage(
                    toGrayscale(
                        Objects.requireNonNull(
                            getCard(slotted.getCardType().ordinal(), slotted.getCount()))));
          } else {
            ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
                .setImage(toGrayscale(Objects.requireNonNull(getCard(7, 0)))); // Blocker
          }
          blocked.append(i + 1).append(",");
        } else {
          if (slotted != null) {
            ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
                .setImage(getCard(slotted.getCardType().ordinal(), slotted.getCount())); // slotted
          } else {
            ((ImageView) getNodeByRowColumnIndex(1, i, gridCardSelect))
                .setImage(getCard(8, 0)); // Empty
          }
        }
      }
      if (!blocked.toString().equals("")) {
        LOG.debug(
            "Robot is Damaged. Blocking the slot(s) {}.",
            blocked.substring(0, blocked.length() - 1));
      }
      if (unlockedCardSlots == 0) {
        btnOpenCardSelect.setId("gamePane__cardSelect__button--ready");
        btnCloseCardSelect.setId("gamePane__cardSelect__button--ready");
      }
    }
  }

  private Node getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
    Node result = new ImageView();
    ObservableList<Node> children = gridPane.getChildren();

    for (Node node : children) {
      int r = GridPane.getRowIndex(node) == null ? 0 : GridPane.getRowIndex(node);
      int c = GridPane.getColumnIndex(node) == null ? 0 : GridPane.getColumnIndex(node);

      if (r == row && c == column) {
        result = node;
        break;
      }
    }

    return result;
  }

  /** Switch the Images from the Target and the Source Drag and Drop Event. */
  private void switchImage(ImageView targetView, ImageView sourceView) {
    Image targetImage = targetView.getImage();
    targetView.setImage(sourceView.getImage());
    sourceView.setImage(targetImage);
  }

  /** Check if the ImageView is in GridPaneSelect. */
  private boolean isInGridPaneSelect(ImageView source) {
    for (Node child : gridCardSelect.getChildren()) {
      if (child.getClass() == ImageView.class && child.getId().equals(source.getId())) {
        return true;
      }
    }
    return false;
  }

  /** Check if the ImageView is in GridPaneDepot. */
  private boolean isInGridPaneDepot(ImageView source) {
    for (Node child : gridCardDepot.getChildren()) {
      if (child.getClass() == ImageView.class && child.getId().equals(source.getId())) {
        return true;
      }
    }
    return false;
  }

  private boolean checkCardSelectDone() {
    for (int i = 0; i < programmingCardSelectionArray.length; i++) {
      if (programmingCardSelectionArray[i] == null && !blockedCardSlots[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Annotated with "@Subscribe", this method is triggered by the "RoundFinishMessage" event. It
   * compares the current game name with the game name in the message object to determine if the
   * round has finished in the current game. If the names match, it returns true. Processing of new
   * data to display the new game round is not implemented in this method.
   *
   * @param roundFinishMessage the message object that contains the game name
   * @return a boolean value indicating whether new data needs to be processed or not
   */
  @Subscribe
  public boolean onRoundFinish(RoundFinishMessage roundFinishMessage) {
    if (!roundFinishMessage.getStep().getGameId().equals(gameId)) {
      return false;
    }
    if (!tabClosed) {
      firstRoundfirstCard = false;
      gameAnimator.playAnimation(roundFinishMessage.getStep());
      return true;
    }
    return false;
  }

  /**
   * Method which sends the programmingCardSelectionArray to the server to validate by creating a
   * sendValidateCardSelectionRequest.
   */
  public void validateAndSendCardSelection() {
    if (robotShutdown.isVisible()) {
      if (robotShutdown.isSelected() && gameDTO.isPresent()) {
        eventBus.post(new ShutdownRobotRequest(gameDTO.get().getGameId(), loggedInUser.getUser()));
        robotShutdown.setSelected(false);
      }
      robotShutdown.setVisible(false);
      txtShutOff.setVisible(false);
    }
    if (isCardSelectionValid(programmingCardSelectionArray) && gameDTO.isPresent()) {
      gameService.sendValidateCardSelectionRequest(
          loggedInUser.getUser().getUsername(), gameId, programmingCardSelectionArray);
      try {
        Player playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
        playerLocal.getRobot().setSelectedCards(programmingCardSelectionArray);
      } catch (NoSuchElementException e) {
        LOG.error("Player is not present. Set Card selection.");
      }
    } else {
      LOG.error("Card selection is not valid!");
    }
  }

  /**
   * Method that fixes the user card selection if he messed up.
   *
   * @param selectedCards the Array that consists of the programmingCardSelectionArray
   */
  @SuppressWarnings("java:S3776")
  private void fixFirstCardMove(ProgrammingCard[] selectedCards) {
    if (selectedCards[0] == null || !selectedCards[0].isMoveCard()) {
      for (int i = 0; i < selectedCards.length; i++) {
        if (selectedCards[i] != null && selectedCards[i].isMoveCard()) {
          ProgrammingCard first = selectedCards[0];
          selectedCards[0] = selectedCards[i];
          selectedCards[i] = first;
          break;
        }
      }
    }
    if (selectedCards[0] == null || !selectedCards[0].isMoveCard()) {
      for (int i = 0; i < programmingCardDepotArray.length; i++) {
        if (programmingCardDepotArray[0] != null && programmingCardDepotArray[i].isMoveCard()) {
          selectedCards[0] = programmingCardDepotArray[i];
          ProgrammingCard first = programmingCardSelectionArray[0];
          programmingCardSelectionArray[0] = programmingCardDepotArray[i];
          programmingCardDepotArray[i] = first;
          switchImage(
              ((ImageView) getNodeByRowColumnIndex(0, i, gridCardDepot)),
              ((ImageView) getNodeByRowColumnIndex(1, 0, gridCardSelect)));
          break;
        }
      }
    }
    refreshBlockedCards();
  }

  /**
   * Method that performs some basic validation on the selected programming cards array.
   *
   * @param selectedCards the Array that consists of the programmingCardSelectionArray
   * @return a boolean value if the given selectedCards Array is in range.
   */
  private boolean isCardSelectionValid(ProgrammingCard[] selectedCards) {
    if (firstRoundfirstCard
        && (selectedCards != null
            && (selectedCards[0] == null || !selectedCards[0].isMoveCard()))) {
      fixFirstCardMove(selectedCards);
    }
    return selectedCards != null && selectedCards.length > 0 && selectedCards.length <= 5;
  }

  /**
   * Method that processes the success (boolean) in a ValidateCardSelectionResponseEvent.
   *
   * @param event on the eventBus
   */
  @Subscribe
  public void onValidateCardSelectionResponseEvent(ValidateCardSelectionResponseEvent event) {
    Player playerLocal;
    if (gameDTO.isPresent()
        && !tabClosed
        && event.getResponse().getGameId().equals(gameId)
        && loggedInUser.getUsername().equals(event.getResponse().getUserId())) {
      playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      gameService.retrieveGameDTO(gameId);
      LOG.debug("Received new ValidateCardSelectionResponseEvent. Updating Cards.");
      playerLocal.getRobot().setSelectedCards(event.getResponse().getSelectedCards());
      programmingCardSelectionArray = event.getResponse().getSelectedCards();
      refreshBlockedCards();
      btnOpenCardSelect.setId(BTN_CLOSE_CARD_SELECT_ID);
      btnCloseCardSelect.setId(BTN_CLOSE_CARD_SELECT_ID);
      // Remove new selected cards from depot
      for (int i = 0; i < gridCardDepot.getColumnCount(); i++) {
        ProgrammingCard currentHand = playerLocal.getRobot().getHandCards()[i];
        if (Arrays.stream(playerLocal.getRobot().getSelectedCards())
            .filter(Objects::nonNull)
            .anyMatch(x -> x.equals(currentHand))) {
          ((ImageView) getNodeByRowColumnIndex(0, i, gridCardDepot))
              .setImage(getCard(8, 0)); // Empty
        }
      }
    }
    // show success in UI, not sendet from server yet. need to set message context.
  }

  /**
   * Method that sends the card selection to the server when a ValidateCardsMessage is found on the
   * event bus.
   *
   * @param message the message to validate
   */
  @Subscribe
  public void onValidateCardsMessage(ValidateCardsMessage message) {
    if (!message.getGameId().equals(gameId)) {
      return;
    }
    if (!tabClosed) {
      validateAndSendCardSelection();
    }
  }

  /**
   * Method that processes the countdown of the turnLimit and uses the validateAndSendCardSelection
   * method when it's finished to send the player cards to the server.
   */
  private void countDown() {
    timeInSeconds = getTimelimitInSecondsFromOptions();
    this.countdown.setText(NAME_OF_TIMER_LABEL + timeInSeconds);

    Timeline timeline = new Timeline();
    timeline.setCycleCount(Animation.INDEFINITE);
    KeyFrame keyFrame =
        new KeyFrame(
            Duration.seconds(1),
            event -> {
              updateTime();
              if (isCountdownFinished()) {
                timeline.stop();
                validateAndSendCardSelection();
                LOG.debug("Card selection countdown finished.");
              }
              if (gameDTO.isPresent() && gameDTO.get().isTimerStopped()) {
                timeline.stop();
                validateAndSendCardSelection();
                LOG.debug("Card selection countdown has been stopped.");
              }
            });

    timeline.getKeyFrames().add(keyFrame);
    timeline.playFromStart();
  }

  /**
   * Method that processes the countdown of the unblockCardInteraction When the timer runs out,
   * random selection will be sendet.
   */
  private void unblockTimer() {
    unblockTimeInSeconds = getTimelimitInSecondsFromOptions();

    Timeline timeline = new Timeline();
    timeline.setCycleCount(Animation.INDEFINITE);
    KeyFrame keyFrame =
        new KeyFrame(
            Duration.seconds(1),
            event -> {
              updateTimeUnblock();
              if (!unlockingProcess) {
                timeline.stop();
                LOG.debug("Unblocking made in time, stopping timer.");
              }
              if (unblockTimeInSeconds <= 0) {
                timeline.stop();
                if (unlockingProcess) {
                  timeoutUnblocking();
                  unlockingProcess = false;
                  resetUnblockButton();
                }
                LOG.debug("Card unblock selection timed out.");
              }
            });

    timeline.getKeyFrames().add(keyFrame);
    timeline.playFromStart();
  }

  /** Method to reset the card selection close button. */
  private void resetUnblockButton() {
    btnCloseCardSelect.setDisable(false);
    btnCloseCardSelect.setStyle("-fx-background-color: #FC4241;");
    btnCloseCardSelect.setStyle("-fx-font-size: 20px;");
    btnCloseCardSelect.setText("Schließen");
    btnCloseCardSelect.setOpacity(1.0);
    fadeUnblock.stop();
  }

  /** Method to unblock cards in case of timeout. */
  private void timeoutUnblocking() {
    for (int j = 0; j < blockedCardSlots.length; j++) {
      if (blockedCardSlots[j] && canUnblock > 0) {
        gameService.sendUnblockCardRequest(loggedInUser.getUser().getUsername(), gameId, j);
        canUnblock--;
      }
    }
    canUnblock = 0;
  }

  /** Tiny method that updates the unblock card Timer. */
  private void updateTimeUnblock() {
    if (gameDTO.isPresent() && !gameDTO.get().isTimerStopped()) {
      unblockTimeInSeconds--;
      Platform.runLater(
          () -> {
            if (unlockingProcess) {
              btnCloseCardSelect.setText(
                  "WÄHLE " + canUnblock + " KARTE ZUM ENTSPERREN (" + unblockTimeInSeconds + ")");
            }
          });
    }
  }

  /** Tiny method that updates the timer every second. */
  private void updateTime() {
    if (gameDTO.isPresent() && !gameDTO.get().isTimerStopped()) {
      timeInSeconds--;
      this.countdown.setText(NAME_OF_TIMER_LABEL + timeInSeconds);
    }
  }

  private boolean isCountdownFinished() {
    return timeInSeconds <= 0;
  }

  /** Method which gets the turnLimit from the GameDTO object. */
  private int getTimelimitInSecondsFromOptions() {
    return gameDTO.map(dto -> dto.getLobby().getOptions().getTurnLimit() / 1000).orElse(100);
  }

  /**
   * When a WinMessage is received, this method will create a Text object with the name of the
   * winning player and add it to the chat.
   *
   * @param winMessage the WinMessage object containing information about the winner of the game
   */
  @Subscribe
  public void onWinnerMessage(WinMessage winMessage) {
    if (!winMessage.getGame().getGameId().equals(gameId)) {
      return;
    }
    if (!tabClosed) {
      PlaySound playSound = new PlaySound();
      playSound.playSound("/sounds/winning.wav", false);
      Text text =
          new Text(winMessage.getWinner().getUser().getUsername() + " hat das Spiel gewonnen!");
      Platform.runLater(() -> gameChatMessages.add(text));
      eventBus.post(
          new ShowWinnerTabEvent(winMessage.getWinner().getUser().getUsername(), gameId, gameTab));
    }
  }

  /**
   * Handles the StopTimerMessage found on the Eventbus. It uses the retrieveGameDTO method so that
   * the client refreshes its GameDTO in order to use the current timerStopped boolean set by the
   * server.
   *
   * @param msg StopTimerMessage
   */
  @Subscribe
  public void onStopTimerMessage(StopTimerMessage msg) {
    if (gameDTO.isPresent() && !tabClosed && msg.getGameId().equals(gameId)) {
      gameService.retrieveGameDTO(this.gameId);
      String playerMsg;
      Player playerLocal;
      try {
        playerMsg = msg.getUsername();
        playerLocal = gameDTO.get().getPlayer(loggedInUser.getUser());
      } catch (NoSuchElementException e) {
        LOG.error(e.getMessage());
        return;
      }
      if (playerMsg.equals(playerLocal.getUser().getUsername())) {
        LOG.debug("Received new StopTimerMessage. The timer has been stopped");
      }
    }
  }

  /**
   * Updates the visibility of certain buttons in.
   *
   * @param msg the ChangedSpectatorModeMessage received
   */
  @Subscribe
  public void onChangedSpectatorModeMessage(ChangedSpectatorModeMessage msg) {
    if (!tabClosed
        && msg.getLobbyId().equals(gameId)
        && msg.getUser().getUUID().equals(loggedInUser.getUser().getUUID())) {
      btnOpenCardSelect.setVisible(false);
      btnNextRound.setVisible(false);
    }
  }

  public FloorFieldAnimator getFloorFieldAnimator() {
    return floorFieldAnimator;
  }

  /** makes a line break. */
  @SuppressWarnings("java:S110")
  private static class GameChatCell extends ListCell<Text> {

    @Override
    public void updateItem(Text item, boolean empty) {
      setGraphic(item);
    }
  }
}
