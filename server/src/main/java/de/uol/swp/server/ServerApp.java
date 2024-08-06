package de.uol.swp.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uol.swp.common.user.Hashing;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.CommandRegister;
import de.uol.swp.server.communication.ServerHandler;
import de.uol.swp.server.communication.netty.NettyServerHandler;
import de.uol.swp.server.communication.netty.Server;
import de.uol.swp.server.di.ServerModule;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.UserService;
import io.netty.channel.ChannelHandler;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * This class handles the startup of the server, as well as, the creation of default users while the
 * MainMemoryBasedUserStore is still in use.
 *
 * @see de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore
 */
class ServerApp {

  private static final Logger LOG = LogManager.getLogger(ServerApp.class);
  private static final ServerConfig serverConfig = ServerConfig.getInstance();
  private static final String APPENDER_REF = "logFile";

  /**
   * Main Method.
   *
   * <p>This method handles the creation of the server components and the start of the server
   *
   * @param args Any arguments given when starting the application e.g. a port number
   */
  public static void main(String[] args) throws Exception {
    // logging
    setupLogging();

    int port = -1;
    if (args.length == 1) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (Exception e) {
        // Ignore and use default value
      }
    }

    // create components
    Injector injector = Guice.createInjector(new ServerModule());
    createServices(injector);
    if (port < 0) {
      port = serverConfig.getPort();
    }
    LOG.info("Starting Server on port {}", port);
    ServerHandler serverHandler = injector.getInstance(ServerHandler.class);
    ChannelHandler channelHandler = new NettyServerHandler(serverHandler);
    Server server = new Server(channelHandler);
    try {
      server.start(port);
    } catch (InterruptedException e) {
      LOG.info("Stopping Process");
      Thread.currentThread().interrupt();
      System.exit(1);
    }
  }

  /**
   * Helper method to create the services the server uses and for the time being the test users.
   *
   * @param injector the Google guice injector used for dependency injection
   */
  protected static void createServices(Injector injector) {
    UserManagement userManagement = injector.getInstance(UserManagement.class);

    if (serverConfig.isMockupMode() && !serverConfig.getDatabaseOn()) {
      for (int i = 0; i < 5; i++) {
        try {
          userManagement.createUser(
              new UserDTO(
                  UUID.randomUUID().toString(),
                  "test" + i,
                  Hashing.hashing("test" + i),
                  "test" + i + "@test.de",
                  new UserData(1)));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // Remark: As these services are not referenced by any other class
    // we will need to create instances here (and inject dependencies)
    injector.getInstance(UserService.class); // <- UserManagement
    injector.getInstance(AuthenticationService.class); // <- UserManagement
    injector.getInstance(LobbyService.class); // <- AuthenticationService, LobbyManagement
    injector.getInstance(ChatService.class); // <- LobbyService
    injector.getInstance(GameService.class); // <- GameManagement, LobbyManagement, LobbyService

    // Loading the commands into CommandRegister
    loadCommands();
  }

  private static void loadCommands() {
    try {
      CommandRegister.getInstance().loadCommands();
    } catch (Exception e) {
      LOG.error("Error loading commands");
    }
  }

  protected static void setupLogging(String value) {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    LoggerConfig rootLoggerConfig = config.getLoggers().get("");
    rootLoggerConfig.removeAppender(APPENDER_REF);

    switch (value) {
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

    if (serverConfig.logToFile()) {
      LOG.info("Logging to File");
      rootLoggerConfig.addAppender(
          config.getAppender(APPENDER_REF), LogManager.getRootLogger().getLevel(), null);
      ctx.updateLoggers();
    } else {
      LOG.info("Not Logging to File");
      rootLoggerConfig.addAppender(config.getAppender(APPENDER_REF), Level.OFF, null);
      ctx.updateLoggers();
    }
    LOG.info("Loglevel set to {}", LogManager.getRootLogger().getLevel());
  }

  protected static void setupLogging() {
    setupLogging(serverConfig.getLogLevel().toLowerCase());
  }
}
