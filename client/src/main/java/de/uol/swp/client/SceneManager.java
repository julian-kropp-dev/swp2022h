package de.uol.swp.client;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import de.uol.swp.client.auth.LoginPresenter;
import de.uol.swp.client.auth.events.ShowLoginViewEvent;
import de.uol.swp.client.game.EndOfGamePresenter;
import de.uol.swp.client.game.GamePresenter;
import de.uol.swp.client.game.GameService;
import de.uol.swp.client.game.events.ShowGameTabEvent;
import de.uol.swp.client.game.events.ShowWinnerTabEvent;
import de.uol.swp.client.instruction.InstructionPresenter;
import de.uol.swp.client.instruction.event.ShowInstructionTabEvent;
import de.uol.swp.client.lobby.LobbyCreationPresenter;
import de.uol.swp.client.lobby.LobbyListPresenter;
import de.uol.swp.client.lobby.LobbyPresenter;
import de.uol.swp.client.lobby.LobbyService;
import de.uol.swp.client.lobby.events.LobbyErrorEvent;
import de.uol.swp.client.lobby.events.LobbyInfoEvent;
import de.uol.swp.client.lobby.events.ShowLobbyCreationTabEvent;
import de.uol.swp.client.lobby.events.ShowLobbyListTabEvent;
import de.uol.swp.client.lobby.events.ShowLobbyTabEvent;
import de.uol.swp.client.main.MainMenuPresenter;
import de.uol.swp.client.main.events.ShowMainMenuTabEvent;
import de.uol.swp.client.register.RegistrationPresenter;
import de.uol.swp.client.register.event.RegistrationErrorEvent;
import de.uol.swp.client.register.event.ShowRegistrationViewEvent;
import de.uol.swp.client.userprofile.UserProfilePresenter;
import de.uol.swp.client.userprofile.event.ChangeUserDataErrorEvent;
import de.uol.swp.client.userprofile.event.ShowUserProfileTabEvent;
import de.uol.swp.common.user.response.LogoutSuccessfulResponse;
import java.awt.Taskbar;
import java.awt.Taskbar.Feature;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class that manages which window/scene is currently shown. */
@SuppressWarnings("UnstableApiUsage")
public class SceneManager {

  static final Logger LOG = LogManager.getLogger(SceneManager.class);
  static final String STYLE_SHEET = "css/swp.css";
  static final Image icon = new Image("file:client/src/main/resources/appicon/appicon.png");
  private static final String FAILED_FXML = "Failed to load FXML file {}";
  private final Stage primaryStage;
  private final Injector injector;
  private String lastTitle;
  private Scene registrationScene;
  private Scene mainScene;
  private Scene lastScene = null;
  private Scene currentScene = null;
  private Scene loginScene;
  private TabPane tabPane;
  @Inject private LobbyService lobbyService;
  @Inject private GameService gameService;
  private Tab mainTab;
  private Tab lobbyListTab;
  private Tab lobbyCreationTab;
  private Tab userProfileTab;
  private Tab instructionTab;
  private Tab endOfGameTab;

  /**
   * SceneManager.
   *
   * @param eventBus Eventbus
   * @param injected Injector
   * @param primaryStage Stage
   * @throws IOException Exception
   */
  @Inject
  public SceneManager(EventBus eventBus, Injector injected, @Assisted Stage primaryStage)
      throws IOException {
    Font.loadFont(getClass().getResourceAsStream("/css/font/VoiceActivatedBB_reg.otf"), 24);
    Font.loadFont(getClass().getResourceAsStream("/css/font/ARIAL.fft"), 24);
    eventBus.register(this);
    this.primaryStage = primaryStage;
    primaryStage.getIcons().add(icon);
    // Set icon on apple taskbar
    if (Taskbar.isTaskbarSupported()) {
      Taskbar taskbar = Taskbar.getTaskbar();
      if (taskbar.isSupported(Feature.ICON_IMAGE)) {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        java.awt.Image dockIcon =
            defaultToolkit.getImage("client/src/main/resources/appicon/appicon.png");
        taskbar.setIconImage(dockIcon);
      }
    }
    this.injector = injected;
    initViews();
  }

  /**
   * Subroutine to initialize all views.
   *
   * <p>This is a subroutine of the constructor to initialize all views
   */
  private void initViews() throws IOException {
    initLoginView();
    initRegistrationView();
    initMainView();
    initMainMenuTab();
  }

  /**
   * Subroutine creating parent panes from FXML files.
   *
   * <p>This Method tries to create a parent pane from the FXML file specified by the URL String
   * given to it. If the LOG-Level is set to Debug or higher loading is written to the LOG. If it
   * fails to load the view a RuntimeException is thrown.
   *
   * @param fxmlFile FXML file to load the view from
   * @return view loaded from FXML or null
   */
  private EventTarget initPresenter(String fxmlFile) throws IOException {
    EventTarget rootPane;
    FXMLLoader loader = injector.getInstance(FXMLLoader.class);
    try {
      URL url = getClass().getResource(fxmlFile);
      LOG.debug("Loading {}", url);
      loader.setLocation(url);
      rootPane = loader.load();
    } catch (IOException e) {
      showError("Das Spiel kann die nächste\nOberfläche nicht laden.");
      LOG.fatal(e);
      throw new IOException(String.format("Could not load View! %s", e.getMessage()));
    }
    return rootPane;
  }

  /**
   * Initializes the main menu view.
   *
   * <p>If the mainScene is null it gets set to a new scene containing the pane showing the main
   * view
   */
  private void initMainView() {
    if (mainScene == null) {
      tabPane = new TabPane();
      tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
      tabPane.setLayoutY(1);

      final AnchorPane anchorPane = new AnchorPane(tabPane);
      AnchorPane.setBottomAnchor(tabPane, 0.0);
      AnchorPane.setTopAnchor(tabPane, 0.0);
      AnchorPane.setLeftAnchor(tabPane, 0.0);
      AnchorPane.setRightAnchor(tabPane, 0.0);

      mainScene = new Scene(anchorPane, 1536, 864);
      mainScene.getStylesheets().add(STYLE_SHEET);
    }
  }

  /**
   * Initializes the login view.
   *
   * <p>If the loginScene is null it gets set to a new scene containing the pane showing the login
   * view as specified by the LoginView FXML file.
   *
   * @see de.uol.swp.client.auth.LoginPresenter
   */
  private void initLoginView() throws IOException {
    if (loginScene == null) {
      Parent rootPane = (Parent) initPresenter(LoginPresenter.FXML);
      loginScene = new Scene(rootPane, 800, 500);
      loginScene.getStylesheets().add(STYLE_SHEET);
    }
  }

  /**
   * Initializes the registration view.
   *
   * <p>If the registrationScene is null, it gets set to a new scene containing the pane showing the
   * registration view as specified by the RegistrationView FXML file.
   *
   * @see de.uol.swp.client.register.RegistrationPresenter
   */
  private void initRegistrationView() throws IOException {
    if (registrationScene == null) {
      Parent rootPane = (Parent) initPresenter(RegistrationPresenter.FXML);
      registrationScene = new Scene(rootPane, 800, 500);
      registrationScene.getStylesheets().add(STYLE_SHEET);
    }
  }

  /**
   * Initializes the main menu tab.
   *
   * <p>If the mainTab is null it gets set to a new scene containing a pane showing the main menu
   * tab as specified by the MainMenuTab FXML file.
   *
   * @see de.uol.swp.client.main.MainMenuPresenter
   */
  public void initMainMenuTab() {
    Platform.runLater(
        () -> {
          if (mainTab == null) {
            try {
              mainTab = (Tab) initPresenter(MainMenuPresenter.FXML);
            } catch (IOException e) {
              LOG.error(FAILED_FXML, e.getMessage());
            }
            tabPane.getTabs().add(mainTab);
          }
          tabPane.getSelectionModel().select(mainTab);
        });
  }

  /**
   * Initializes the user profile tab.
   *
   * <p>If the userProfileTab is not in the TabPane a new Tab is created. Otherwise, the tab
   * switches to the userProfileTab
   *
   * @see de.uol.swp.client.userprofile.UserProfilePresenter
   */
  private void initUserProfileTab() throws IOException {
    if (!tabPane.getTabs().contains(userProfileTab)) {
      userProfileTab = (Tab) initPresenter(UserProfilePresenter.FXML);
      tabPane.getTabs().add(1, userProfileTab);
    }
    tabPane.getSelectionModel().select(userProfileTab);
  }

  /**
   * Initializes the user Instruction tab.
   *
   * <p>If the instructionTab is not in the TabPane a new Tab is created. Otherwise, the tab
   * switches to the instructionTab.
   */
  private void initInstructionTab() throws IOException {
    if (!tabPane.getTabs().contains(instructionTab)) {
      instructionTab = (Tab) initPresenter(InstructionPresenter.FXML);
      tabPane.getTabs().add(1, instructionTab);
    }
    tabPane.getSelectionModel().select(instructionTab);
  }

  /**
   * Initializes the LobbyCreationView.
   *
   * @throws IOException thrown by initPresenter
   * @see LobbyCreationPresenter
   */
  private void initLobbyCreationTab() throws IOException {
    if (!tabPane.getTabs().contains(lobbyCreationTab)) {
      lobbyCreationTab = (Tab) initPresenter(LobbyCreationPresenter.FXML);
      tabPane.getTabs().add(lobbyCreationTab);
    }
    tabPane.getSelectionModel().select(lobbyCreationTab);
  }

  /**
   * Initializes the LobbyTab.
   *
   * @see LobbyPresenter
   */
  private void initLobbyTab(String name, Tab lobbyCreationTab) {
    int index =
        List.copyOf(tabPane.getTabs()).stream()
            .map(Tab::getText)
            .collect(Collectors.toList())
            .indexOf("Lobby " + name);
    if (index >= 0) {
      tabPane.getSelectionModel().select(index);
      return;
    }
    Platform.runLater(
        () -> {
          Tab lobbyTab;
          try {
            lobbyTab = (Tab) initPresenter(LobbyPresenter.FXML);
          } catch (IOException e) {
            LOG.fatal(FAILED_FXML, e.getMessage());
            return;
          }
          tabPane.getTabs().remove(lobbyCreationTab);
          lobbyTab.setText("Lobby " + name);
          tabPane.getTabs().add(lobbyTab);
          tabPane.getSelectionModel().select(lobbyTab);
          lobbyService.retrieveLobbyOptions(name);
        });
  }

  /**
   * Initializes the LobbyListTab.
   *
   * @throws IOException thrown by initPresenter
   * @see LobbyPresenter
   */
  private void initLobbyListTab() throws IOException {
    if (!tabPane.getTabs().contains(lobbyListTab)) {
      lobbyListTab = (Tab) initPresenter(LobbyListPresenter.FXML);
      tabPane.getTabs().add(1, lobbyListTab);
    }
    tabPane.getSelectionModel().select(lobbyListTab);
  }

  @SuppressWarnings("java:S1192")
  private void initGameTab(String name) {
    int index =
        List.copyOf(tabPane.getTabs()).stream()
            .map(Tab::getText)
            .collect(Collectors.toList())
            .indexOf("Spiel " + name);
    if (index >= 0) {
      tabPane.getSelectionModel().select(index);
      return;
    }
    Platform.runLater(
        () -> {
          Tab gameTab;
          try {
            gameTab = (Tab) initPresenter(GamePresenter.FXML);
          } catch (IOException e) {
            LOG.error(FAILED_FXML, e.getMessage());
            return;
          }
          gameTab.setText("Spiel " + name);
          tabPane.getTabs().add(gameTab);
          tabPane.getSelectionModel().select(gameTab);
          gameService.retrieveGameDTO(name);
        });
  }

  @SuppressWarnings("java:S112")
  private void initEndOfGameTab(String gameId, String gewinner) {
    Platform.runLater(
        () -> {
          if (!tabPane.getTabs().contains(endOfGameTab)) {
            try {
              endOfGameTab = (Tab) initPresenter(EndOfGamePresenter.FXML);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
            tabPane.getSelectionModel().select(lobbyListTab);
            ((Text)
                    ((AnchorPane)
                            ((HBox)
                                    ((VBox)
                                            ((AnchorPane) endOfGameTab.getContent())
                                                .getChildren()
                                                .get(0))
                                        .getChildren()
                                        .get(0))
                                .getChildren()
                                .get(0))
                        .getChildren()
                        .get(1))
                .setText(gewinner);
            endOfGameTab.setText("Spiel " + gameId);
            tabPane.getTabs().add(endOfGameTab);
          }
          tabPane.getSelectionModel().select(endOfGameTab);
        });
  }

  /**
   * Handles a ShowMainMenuEvent detected on the EventBus and shows the MainScreen.
   *
   * @param event The detected ShowMainMenuEvent
   * @see ShowMainMenuTabEvent
   */
  @Subscribe
  public void onShowMainMenuTabEvent(ShowMainMenuTabEvent event) {
    if (event.getTabToClose() != null) {
      Platform.runLater(() -> tabPane.getTabs().remove(event.getTabToClose()));
    }
    showMainMenuScreen();
  }

  /**
   * Handles a ShowLobbyCreationViewEvent detected on the EventBus and shows the LobbyCreationView.
   *
   * @param event The detected ShowLobbyCreationViewEvent
   * @see ShowLobbyCreationTabEvent
   */
  @Subscribe
  public void onShowLobbyCreationTabEvent(ShowLobbyCreationTabEvent event) throws IOException {
    showLobbyCreationScreen();
  }

  /**
   * Handles a ShowLobbyViewEvent detected on the EventBus and shows the Lobby.
   *
   * @param event The detected ShowLobbyTabEvent
   * @see ShowLobbyTabEvent
   */
  @Subscribe
  public void onShowLobbyTabEvent(ShowLobbyTabEvent event) {
    showLobbyScreen(event.getLobbyId(), event.getTab());
  }

  /**
   * Handles a ShowLobbyListViewEvent detected on the EventBus and shows the LobbyListView.
   *
   * @param event The detected ShowLobbyViewEvent
   * @see ShowLobbyTabEvent
   */
  @Subscribe
  public void onShowLobbyListViewEvent(ShowLobbyListTabEvent event) throws IOException {
    showLobbyListScreen();
  }

  /**
   * Handles ShowRegistrationViewEvent detected on the EventBus.
   *
   * <p>If a ShowRegistrationViewEvent is detected on the EventBus, this method gets called. It
   * calls a method to switch the current screen to the registration screen.
   *
   * @param event The ShowRegistrationViewEvent detected on the EventBus
   * @see de.uol.swp.client.register.event.ShowRegistrationViewEvent
   * @see de.uol.swp.client.register.event.ShowRegistrationViewEvent
   */
  @Subscribe
  public void onShowRegistrationViewEvent(ShowRegistrationViewEvent event) {
    showRegistrationScreen();
  }

  /**
   * Handles ShowLoginViewEvent detected on the EventBus.
   *
   * <p>If a ShowLoginViewEvent is detected on the EventBus, this method gets called. It calls a
   * method to switch the current screen to the login screen.
   *
   * @param event The ShowLoginViewEvent detected on the EventBus
   * @see de.uol.swp.client.auth.events.ShowLoginViewEvent
   */
  @Subscribe
  public void onShowLoginViewEvent(ShowLoginViewEvent event) {
    showLoginScreen();
  }

  /**
   * Handles ShowUserProfileTabEvent detected on the EventBus.
   *
   * <p>If a ShowUserProfileTabEvent is detected on the EventBus, this method gets called. It calls
   * a method to switch the current screen to the Profile screen.
   *
   * @param event ShowUserProfileTabEvent
   */
  @Subscribe
  public void onShowUserProfileTabEvent(ShowUserProfileTabEvent event) throws IOException {
    showUserProfileScreen();
  }

  /**
   * Handels ShowInstructionTabEvent found on the Eventbus.
   *
   * @param event ShowInstructionTabEvent
   * @throws IOException Exception
   */
  @Subscribe
  public void onShowInstructionTabEvent(ShowInstructionTabEvent event) throws IOException {
    showInstructionScreen();
  }

  /**
   * Subscribes to the ShowWinnerTabEvent and performs the necessary actions to show the winner tab.
   *
   * @param event the ShowWinnerTabEvent to handle
   */
  @Subscribe
  public void onShowWinnerTabEvent(ShowWinnerTabEvent event) {
    if (event.getTabToClose() != null) {
      Platform.runLater(() -> tabPane.getTabs().remove(event.getTabToClose()));
    }
    showWinnerTab(event.getLobbyId(), event.getUsername());
  }

  /**
   * Handles the ShowGameTabEvent found on the Eventbus.
   *
   * @param event ShowGameTabEvent
   */
  @Subscribe
  public void onShowGameTabEvent(ShowGameTabEvent event) {
    if (event.getTabToClose() != null) {
      Platform.runLater(() -> tabPane.getTabs().remove(event.getTabToClose()));
    }
    showGameScreen(event.getLobbyId());
  }

  /**
   * Handles CancelEvent detected on the EventBus.
   *
   * <p>If a CancelEvent is detected on the EventBus, this method gets called. It calls a method to
   * show the screen shown before.
   *
   * @param event The CancelEvent detected on the EventBus
   * @see de.uol.swp.client.CancelEvent
   */
  @Subscribe
  public void onCancelEvent(CancelEvent event) {
    showScene(lastScene, lastTitle);
  }

  /**
   * Handles RegistrationErrorEvent detected on the EventBus.
   *
   * <p>If a RegistrationErrorEvent is detected on the EventBus, this method gets called. It shows
   * the error message of the event in an error alert.
   *
   * @param event The RegistrationErrorEvent detected on the EventBus
   * @see de.uol.swp.client.register.event.RegistrationErrorEvent
   */
  @Subscribe
  public void onRegistrationErrorEvent(RegistrationErrorEvent event) {
    showError(event.getMessage());
  }

  /**
   * Handles LobbyErrorEvent detected on the EventBus.
   *
   * <p>If a LobbyErrorEvent is detected on the EventBus, this method gets called. It shows the
   * error message of the event in an error alert.
   *
   * @param event The LobbyErrorEvent detected on the EventBus
   * @see de.uol.swp.client.lobby.events.LobbyErrorEvent
   */
  @Subscribe
  public void onLobbyErrorEvent(LobbyErrorEvent event) {
    showError(event.getMessage());
  }

  /**
   * Handels the ChangeUserDataErrorEvent found on the Eventbus. The User will be informed of this
   * error by an Error-Popup.
   *
   * @param event ChangeUserDataErrorEvent
   */
  @Subscribe
  public void onChangeUserDataErrorEvent(ChangeUserDataErrorEvent event) {
    showError(event.getMessage());
  }

  /**
   * Shows an information message inside a warning alert.
   *
   * @param message The type of information to be shown.
   * @param e The information message.
   */
  public void showInfo(String message, String e) {
    Platform.runLater(
        () -> {
          Alert a = new Alert(AlertType.WARNING);
          a.setTitle(message);
          a.setHeaderText("Wichtige Info!");
          a.setContentText(e);
          DialogPane pane = a.getDialogPane();
          pane.getStylesheets().add("css/warningBox.css");
          a.showAndWait();
        });
  }

  /**
   * Shows an information message inside an information alert.
   *
   * @param e The information message.
   */
  public void showUserInfo(String e) {
    showInfo("Information:\n", e);
  }

  /**
   * Shows an error message inside an error alert.
   *
   * @param message The type of error to be shown
   * @param e The error message
   */
  public void showError(String message, String e) {
    Platform.runLater(
        () -> {
          Alert a = new Alert(Alert.AlertType.ERROR);
          a.setTitle(message);
          a.setHeaderText("EIN FEHLER IST AUFGETRETEN!");
          a.setContentText(e);
          DialogPane pane = a.getDialogPane();
          pane.getStylesheets().add("css/alertBox.css");
          a.showAndWait();
        });
  }

  /**
   * Shows an error message inside an error alert.
   *
   * @param e The error message
   */
  public void showError(String e) {
    showError("System Meldung:\n", e);
  }

  /**
   * Shows a server error message inside an error alert.
   *
   * @param e The error message
   */
  public void showServerError(String e) {
    showError("Der Server hat folgendes gemeldet:\n", e);
  }

  /**
   * Switches the current scene and title to the given ones.
   *
   * <p>The current scene and title are saved in the lastScene and lastTitle variables, before the
   * new scene and title are set and shown.
   *
   * @param scene New scene to show
   * @param title New window title
   */
  private void showScene(final Scene scene, final String title) {
    this.lastScene = currentScene;
    this.lastTitle = primaryStage.getTitle();
    this.currentScene = scene;
    Platform.runLater(
        () -> {
          primaryStage.setTitle(title);
          primaryStage.setScene(scene);
          primaryStage.show();
        });
  }

  /**
   * Shows the main menu.
   *
   * <p>Switches the current Scene to the mainScene and sets the title of the window to "Welcome "
   * and the username of the current user
   *
   * @throws IOException thrown by initMainMenuTab
   */
  public void showMainScreen() throws IOException {
    showScene(mainScene, "RoboRally");
    Platform.runLater(
        () -> {
          primaryStage.setResizable(true);
          primaryStage.setMaximized(true);
          primaryStage.addEventHandler(
              KeyEvent.KEY_PRESSED,
              event -> {
                if (KeyCode.F11.equals(event.getCode())) {
                  primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
              });
        });
    initMainMenuTab();
  }

  /**
   * Shows the login screen.
   *
   * <p>Switches the current Scene to the loginScene and sets the title of the window to "Login"
   */
  public void showLoginScreen() {
    showScene(loginScene, "Zugang zur Roboterwelt");
    Platform.runLater(() -> primaryStage.setResizable(false));
    initMainMenuTab();
  }

  /**
   * Shows the registration screen.
   *
   * <p>Switches the current Scene to the registrationScene and sets the title of the window to
   * "Registration"
   */
  public void showRegistrationScreen() {
    showScene(registrationScene, "Erstellen der Zugangsdaten für den Roboter");
    Platform.runLater(() -> primaryStage.setResizable(false));
  }

  public void showMainMenuScreen() {
    initMainMenuTab();
  }

  /** Shows the lobbyCreation screen. */
  public void showLobbyCreationScreen() throws IOException {
    initLobbyCreationTab();
  }

  /**
   * Shows the screen of a specific lobby.
   *
   * @param name the name of the lobby
   */
  public void showLobbyScreen(String name, Tab lobbyCreationTab) {
    initLobbyTab(name, lobbyCreationTab);
  }

  /** Shows the lobbyList screen. */
  public void showLobbyListScreen() throws IOException {
    initLobbyListTab();
  }

  /**
   * Shows the user profile screen.
   *
   * <p>Switches the current Scene to the user profile screen and sets the title of the window to
   * "User profile"
   */
  public void showUserProfileScreen() throws IOException {
    initUserProfileTab();
  }

  /**
   * Shows the user instruction screen.
   *
   * <p>Switches the current Scene to the instruction screen and sets the title of the window to
   * "Spielanleitung"
   */
  public void showInstructionScreen() throws IOException {
    initInstructionTab();
  }

  public void showGameScreen(String name) {
    initGameTab(name);
  }

  /**
   * Shows the winner tab for the specified game.
   *
   * @param gameId the name of the game
   * @param winner the username of the winner
   */
  public void showWinnerTab(String gameId, String winner) {
    initEndOfGameTab(gameId, winner);
  }

  /**
   * Handels the LogoutSuccessfulResponse found on the Eventbus.
   *
   * @param response LogoutSuccessfulResponse
   */
  @Subscribe
  public void onLogoutSuccessfulResponse(LogoutSuccessfulResponse response) {
    Platform.runLater(
        () -> {
          mainTab = null;
          userProfileTab = null;
          instructionTab = null;
          lobbyListTab = null;
          tabPane.getTabs().clear();
          showLoginScreen();
        });
  }

  /**
   * Shows an information whenever an information/ warning should be told to the player.
   *
   * @param event the information event.
   */
  @Subscribe
  public void onLobbyInfoEvent(LobbyInfoEvent event) {
    showUserInfo(event.getMessage());
  }
}
