package de.uol.swp.server;

import static de.uol.swp.server.ServerApp.setupLogging;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.CommandRegister;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.UserService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("UnstableApiUsage")
class ServerAppTest {
  @Mock ServerConfig serverConfig;

  @Mock UserManagement userManagement;

  @Mock Injector injector;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    serverConfig = mock(ServerConfig.class);
  }

  @Test
  void testCreateServices() {
    when(injector.getInstance(UserManagement.class)).thenReturn(userManagement);
    when(serverConfig.isMockupMode()).thenReturn(true);
    when(serverConfig.getDatabaseOn()).thenReturn(false);
    CommandRegister commandRegister = new CommandRegister(new EventBus());

    ServerApp.createServices(injector);

    // Verify service creation
    verify(injector, times(1)).getInstance(UserService.class);
    verify(injector, times(1)).getInstance(AuthenticationService.class);
    verify(injector, times(1)).getInstance(LobbyService.class);
    verify(injector, times(1)).getInstance(ChatService.class);
    verify(injector, times(1)).getInstance(GameService.class);
  }

  @BeforeEach
  public void setup() {
    serverConfig = mock(ServerConfig.class);
  }

  @Test
  void setupLogging_WithLogLevelOff() {
    setupLogging("off");
    verifyLog4jRootLoggerLevel(Level.OFF);
  }

  @Test
  void setupLogging_WithLogLevelAll() {
    setupLogging("all");
    verifyLog4jRootLoggerLevel(Level.ALL);
  }

  @Test
  void setupLogging_WithLogLevelTrace() {
    setupLogging("trace");
    verifyLog4jRootLoggerLevel(Level.TRACE);
  }

  @Test
  void setupLogging_WithLogLevelDebug() {
    setupLogging("debug");
    verifyLog4jRootLoggerLevel(Level.DEBUG);
  }

  @Test
  void setupLogging_WithLogLevelWarn() {
    setupLogging("warn");
    verifyLog4jRootLoggerLevel(Level.WARN);
  }

  @Test
  void setupLogging_WithLogLevelError() {
    setupLogging("error");
    verifyLog4jRootLoggerLevel(Level.ERROR);
  }

  @Test
  void setupLogging_WithLogLevelFatal() {
    setupLogging("fatal");
    verifyLog4jRootLoggerLevel(Level.FATAL);
  }

  @Test
  void setupLogging_WithLogLevelInfo() {
    setupLogging("info");
    verifyLog4jRootLoggerLevel(Level.INFO);
  }

  @Test
  void setupLogging_WithLogLevelDefault() {
    setupLogging("NADA");
    verifyLog4jRootLoggerLevel(Level.INFO);
  }

  private void verifyLog4jRootLoggerLevel(Level expectedLevel) {
    LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    Configuration configuration = loggerContext.getConfiguration();
    LoggerConfig rootLoggerConfig = configuration.getLoggers().get("");
    Level actualLevel = rootLoggerConfig.getLevel();
    assertEquals(expectedLevel, actualLevel);
  }
}
