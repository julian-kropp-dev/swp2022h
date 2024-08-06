package de.uol.swp.server.chat;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.eventbus.EventBus;
import de.uol.swp.server.ServerConfig;
import de.uol.swp.server.chat.commands.GetCardsCommand;
import de.uol.swp.server.chat.commands.StartGameCommand;
import java.lang.reflect.Field;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Yes this class uses java reflections but that's ok please don't touch it only if you know what
 * you are doing.
 */
@SuppressWarnings("UnstableApiUsage")
class CommandRegisterTest {
  private final ServerConfig serverConfig = ServerConfig.getInstance();
  EventBus bus = new EventBus();
  private CommandRegister commandRegister;
  private Boolean oldValue;

  @BeforeEach
  void setUp() {
    bus.register(this);
    oldValue = serverConfig.isMockupMode();
    Field field;
    try {
      field = serverConfig.getClass().getDeclaredField("properties");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    field.setAccessible(true);
    try {
      Properties prop = new Properties();
      prop.setProperty("server.MockupMode", "true");
      field.set(serverConfig, prop);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    commandRegister = new CommandRegister(bus);
    commandRegister.loadCommands();
  }

  @AfterEach
  void tearDown() {
    bus.unregister(this);
    Field field;
    try {
      field = serverConfig.getClass().getDeclaredField("properties");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    field.setAccessible(true);
    try {
      Properties prop = new Properties();
      prop.setProperty("server.MockupMode", String.valueOf(oldValue));
      field.set(serverConfig, prop);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void commandMockupTest() {
    assertTrue(commandRegister.getCommand("startGame") instanceof StartGameCommand);
    assertTrue(commandRegister.getCommand("sendCards") instanceof GetCardsCommand);
  }

  @Test
  void commandNotMockupTest() {
    Field field;
    try {
      field = serverConfig.getClass().getDeclaredField("properties");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    field.setAccessible(true);
    try {
      Properties prop = new Properties();
      prop.setProperty("server.MockupMode", "false");
      field.set(serverConfig, prop);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    commandRegister = new CommandRegister(bus);

    assertFalse(commandRegister.containsKey("startGame"));
  }
}
