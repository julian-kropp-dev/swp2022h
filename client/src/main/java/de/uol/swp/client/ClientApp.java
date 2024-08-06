package de.uol.swp.client;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uol.swp.client.di.ClientModule;
import de.uol.swp.client.textureatlas.TextureAtlas;
import de.uol.swp.client.textureatlas.TextureAtlasFloorPlans;
import de.uol.swp.client.textureatlas.TextureAtlasInstruction;
import de.uol.swp.client.textureatlas.TextureAtlasRobots;
import de.uol.swp.client.user.ClientUserService;
import de.uol.swp.common.lobby.exception.LobbyIsFullExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyLeaveExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyNotWaitingAnymoreExceptionMessage;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.exception.RegistrationExceptionMessage;
import de.uol.swp.common.user.exception.UpdateUserExceptionMessage;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.LogoutSuccessfulResponse;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.LoadException;
import javafx.stage.Stage;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * The application class of the client.
 *
 * <p>This class handles the startup of the application, as well as, incoming login and registration
 * responses and error messages
 *
 * @see de.uol.swp.client.ConnectionListener
 * @see javafx.application.Application
 */
@SuppressWarnings("UnstableApiUsage")
public class ClientApp extends Application implements ConnectionListener {
  private static final String LOG_LEVEL = Configuration.getDebugLevel();
  private static final Boolean LOG_TO_FILE = Configuration.getLogToFile();
  private static final String APPENDER_REF = "logFile";
  private static final Logger LOG = LogManager.getLogger(ClientApp.class);
  private String host;
  private int port;
  private ClientUserService userService;
  private User user;
  private ClientConnection clientConnection;
  private EventBus eventBus;
  private SceneManager sceneManager;

  private static void setupLogging() {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
    LoggerConfig rootLoggerConfig = config.getLoggers().get("");
    rootLoggerConfig.removeAppender(APPENDER_REF);

    switch (LOG_LEVEL.toLowerCase()) {
      case "off":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
        break;
      case "all":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
        break;
      case "trace":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.TRACE);
        break;
      case "debug":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
        break;
      case "warn":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.WARN);
        break;
      case "error":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ERROR);
        break;
      case "fatal":
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.FATAL);
        break;
      case "info":
      default:
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        break;
    }

    if (Boolean.TRUE.equals(LOG_TO_FILE)) {
      LOG.info("Logging to File");
      rootLoggerConfig.addAppender(
          config.getAppender(APPENDER_REF), LogManager.getRootLogger().getLevel(), null);
      ctx.updateLoggers();
    } else {
      LOG.info("Not Logging to File");
      rootLoggerConfig.addAppender(config.getAppender(APPENDER_REF), Level.OFF, null);
      ctx.updateLoggers();
    }
  }

  /**
   * Default startup method for javafx applications.
   *
   * @param args Any arguments given when starting the application
   */
  public static void main(String[] args) {
    setupLogging();
    launch(args);
  }

  @Override
  public void init() {
    Parameters p = getParameters();
    List<String> args = p.getRaw();

    if (args.size() != 2) {
      host = Configuration.getHostName();
      port = Configuration.getDefaultPort();
      LOG.info("Using connection class: {}", ClientConnection.class.getSimpleName());
      LOG.info("Using Address: {}:{}", host, port);
    } else {
      host = args.get(0);
      port = Integer.parseInt(args.get(1));
    }

    // do not establish connection here
    // if connection is established in this stage, no GUI is shown and
    // exceptions are only visible in console!
  }

  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  @Override
  public void start(Stage primaryStage) {

    LOG.info("Starting Primary Stage");

    // Client app is created by java, so injection must
    // be handled here manually
    Injector injector = Guice.createInjector(new ClientModule());

    // get user service from guice, is needed for logout
    this.userService = injector.getInstance(ClientUserService.class);

    // get event bus from guice
    eventBus = injector.getInstance(EventBus.class);
    // Register this class for de.uol.swp.client.events (e.g. for exceptions)
    eventBus.register(this);
    // Client app is created by java, so injection must
    // be handled here manually
    SceneManagerFactory sceneManagerFactory = injector.getInstance(SceneManagerFactory.class);
    this.sceneManager = sceneManagerFactory.create(primaryStage);

    ClientConnectionFactory connectionFactory = injector.getInstance(ClientConnectionFactory.class);
    clientConnection = connectionFactory.create(host, port);
    clientConnection.addConnectionListener(this);
    // JavaFX Thread should not be blocked to long!
    Thread connection =
        new Thread(
            () -> {
              try {
                clientConnection.start();
              } catch (InterruptedException e) {
                LOG.info("Showing error: connection not established");
                exceptionOccurred(e.getMessage());
                Thread.currentThread().interrupt();
              } catch (SSLException e) {
                LOG.fatal("Fatal error when exchanging certificates for SSL.");
                Thread.currentThread().interrupt();
                exceptionOccurred(
                    "Die Verbindung zum Server ist fehlgeschlagen.\n "
                        + "Ein SSL-Fehler ist aufgetreten.");
              }
            });
    // This thread loads the instrcutions
    Thread atlasInstruction =
        new Thread(
            () -> {
              try {
                TextureAtlasInstruction.loadImages();
              } catch (LoadException e) {
                exceptionOccurred(e.getMessage());
                Thread.currentThread().interrupt();
              }
            });
    // This thread loads the texture atlas for the floorplan textures
    Thread atlasFloorPlan =
        new Thread(
            () -> {
              try {
                TextureAtlasFloorPlans.loadImages();
              } catch (LoadException e) {
                exceptionOccurred(e.getMessage());
                Thread.currentThread().interrupt();
              }
            });
    // This thread loads the atlas for the roboter pictures
    Thread atlasRobots =
        new Thread(
            () -> {
              try {
                TextureAtlasRobots.loadImages();
              } catch (LoadException e) {
                exceptionOccurred(e.getMessage());
                Thread.currentThread().interrupt();
              }
            });
    Thread atlasAssets =
        new Thread(
            () -> {
              try {
                TextureAtlas.loadImages();
              } catch (LoadException e) {
                exceptionOccurred(e.getMessage());
                Thread.currentThread().interrupt();
              }
            });
    atlasInstruction.setDaemon(true);
    atlasInstruction.start();
    atlasFloorPlan.setDaemon(true);
    atlasFloorPlan.start();
    atlasRobots.setDaemon(true);
    atlasRobots.start();
    atlasAssets.setDaemon(true);
    atlasAssets.start();
    connection.setDaemon(true);
    connection.start();
  }

  @Override
  public void connectionEstablished(Channel ch) {
    sceneManager.showLoginScreen();
  }

  @Override
  public void stop() throws InterruptedException {
    if (userService != null && user != null) {
      userService.logout(user);
      user = null;
    }
    eventBus.unregister(this);
    // Important: Close connection so connection thread can terminate
    // else client application will not stop
    LOG.trace("Trying to shut down client ...");
    if (clientConnection != null) {
      clientConnection.close();
    }
    LOG.info("ClientConnection shutdown");
  }

  /**
   * Handles successful login.
   *
   * <p>If an LoginSuccessfulResponse object is detected on the EventBus this method is called. It
   * tells the SceneManager to show the main menu and sets this clients user to the user found in
   * the object. If the loglevel is set to DEBUG or higher "user logged in successfully " and the
   * username of the logged-in user are written to the log.
   *
   * @param message The LoginSuccessfulResponse object detected on the EventBus
   * @see de.uol.swp.client.SceneManager
   */
  @Subscribe
  public void onLoginSuccessfulResponse(LoginSuccessfulResponse message) {
    LOG.debug("User logged in successfully {}", message.getUser().getUsername());
    this.user = message.getUser();
    try {
      sceneManager.showMainScreen();
    } catch (IOException e) {
      LOG.error("Failed to load FXML file {}", e.getMessage());
    }
  }

  /**
   * Handles successful logout.
   *
   * <p>If an LogoutSuccessfulResponse object is detected on the EventBus this method is called. It
   * tells the SceneManager to show the login screen and resets this clients user to null. If the
   * loglevel is set to DEBUG or higher "user logged out successfully " and the username of the
   * logged-out user are written to the log.
   *
   * @param message The LogoutSuccessfulResponse object detected on the EventBus
   * @see de.uol.swp.client.SceneManager
   */
  @Subscribe
  public void onLogoutSuccessfulResponse(LogoutSuccessfulResponse message) {
    LOG.debug("User logged out successfully {}", message.getUser().getUsername());
    this.user = null;
  }

  /**
   * Handles unsuccessful updates of lobby options.
   *
   * @param msg UpdateLobbyOptionsExceptionMessage on the EventBus
   * @see SceneManager
   */
  @Subscribe
  public void onUpdateLobbyOptionsExceptionMessage(UpdateLobbyOptionsExceptionMessage msg) {
    sceneManager.showServerError(String.valueOf(msg));
    LOG.error("{}", msg);
  }

  @Subscribe
  public void onLobbyNotWaitingAnymoreExceptionMessage(LobbyNotWaitingAnymoreExceptionMessage msg) {
    sceneManager.showServerError(msg.getMessage());
    LOG.error("LobbyNotWaitingAnymoreExceptionMessage: {}", msg.getMessage());
  }

  /**
   * Handles unsuccessful lobby join because of a full lobby.
   *
   * @param msg LobbyIsFullExceptionMessage on the EventBus
   */
  @Subscribe
  public void onLobbyIsFullExceptionMessage(LobbyIsFullExceptionMessage msg) {
    sceneManager.showServerError(String.valueOf(msg));
    LOG.error("{}", msg);
  }

  /**
   * Handels unsuccessful lobby leave.
   *
   * @param msg LobbyLeaveExceptionMessage on the EventBus
   * @see SceneManager
   */
  @Subscribe
  public void onLobbyLeaveExceptionMessage(LobbyLeaveExceptionMessage msg) {
    sceneManager.showServerError("Lobby konnte nicht verlassen werden!");
    LOG.error("Leave Lobby error {}", msg);
  }

  /**
   * Handles unsuccessful registrations.
   *
   * <p>If an RegistrationExceptionMessage object is detected on the EventBus this method is called.
   * It tells the SceneManager to show the sever error alert. If the loglevel is set to Error or
   * higher "Registration error " and the error message are written to the log.
   *
   * @param message The RegistrationExceptionMessage object detected on the EventBus
   * @see de.uol.swp.client.SceneManager
   */
  @Subscribe
  public void onRegistrationExceptionMessage(RegistrationExceptionMessage message) {
    sceneManager.showServerError("Fehler beim Registrieren!");
    LOG.error("Registration error {}", message);
  }

  /**
   * Handles successful registrations.
   *
   * <p>If an RegistrationSuccessfulResponse object is detected on the EventBus this method is
   * called. It tells the SceneManager to show the login window. If the loglevel is set to INFO or
   * higher "Registration Successful." is written to the log.
   *
   * @param message The RegistrationSuccessfulResponse object detected on the EventBus
   * @see de.uol.swp.client.SceneManager
   */
  @Subscribe
  public void onRegistrationSuccessfulMessage(RegistrationSuccessfulResponse message) {
    LOG.info("Registration successful.");
    sceneManager.showLoginScreen();
  }

  /**
   * If an error occurs when changing a user, this is intercepted here.
   *
   * @param message The UpdateUserExceptionMessage object detected on the EventBus
   */
  @Subscribe
  public void onUpdateUserExceptionMessage(UpdateUserExceptionMessage message) {
    LOG.error("UpdateUserException {}", message);
    sceneManager.showServerError(message.getMessage());
  }

  /**
   * Suppress DeadEvent errors when lobby list tab is closed.
   *
   * @param message the UpdateLobbyListMessage
   */
  @Subscribe
  private void onUpdateLobbyListMessage(UpdateLobbyListMessage message) {
    // pass
  }

  /**
   * Handles errors produced by the EventBus.
   *
   * <p>If an DeadEvent object is detected on the EventBus, this method is called. It writes
   * "DeadEvent detected " and the error message of the detected DeadEvent object to the log, if the
   * loglevel is set to ERROR or higher.
   *
   * @param deadEvent The DeadEvent object found on the EventBus
   */
  @Subscribe
  private void onDeadEvent(DeadEvent deadEvent) {
    LOG.error("DeadEvent detected {}", deadEvent);
  }

  // -----------------------------------------------------
  // JavaFX Help method
  // -----------------------------------------------------

  @Override
  public void exceptionOccurred(String e) {
    sceneManager.showServerError(e);
  }
}
