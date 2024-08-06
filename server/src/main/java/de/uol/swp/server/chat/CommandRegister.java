package de.uol.swp.server.chat;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.server.ServerConfig;
import de.uol.swp.server.chat.commands.DamageRobotCommand;
import de.uol.swp.server.chat.commands.GetCardsCommand;
import de.uol.swp.server.chat.commands.MoveRobotCommand;
import de.uol.swp.server.chat.commands.RefreshGuiCommand;
import de.uol.swp.server.chat.commands.SetCommand;
import de.uol.swp.server.chat.commands.StartGameCommand;
import de.uol.swp.server.chat.commands.StartLogicCommand;
import de.uol.swp.server.chat.commands.StopTimerCommand;
import de.uol.swp.server.chat.commands.TestCommand;
import de.uol.swp.server.chat.commands.TestCommandParameter;
import de.uol.swp.server.chat.commands.TestCommandVariable;
import de.uol.swp.server.chat.commands.ValidateCardsCommand;
import de.uol.swp.server.chat.commands.WinGameCommand;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class for CommandRegister. */
@SuppressWarnings("UnstableApiUsage")
public class CommandRegister {
  private static final Logger LOG = LogManager.getLogger(CommandRegister.class);
  private static CommandRegister instance;
  private final Map<String, Command> commandMap = new TreeMap<>();
  private final ServerConfig serverConfig = ServerConfig.getInstance();
  private final EventBus eventBus;

  /** The constructor of the class that also loads all commands. */
  @SuppressWarnings("java:S3010")
  @Inject
  public CommandRegister(EventBus eventBus) {
    this.eventBus = eventBus;
    instance = this;
  }

  public static CommandRegister getInstance() {
    return instance;
  }

  /**
   * With this method you get the command object that you can execute.
   *
   * @param command the name of the command.
   * @return The command object.
   */
  public Command getCommand(String command) {
    return commandMap.get(command);
  }

  /**
   * This method checks whether the command exists.
   *
   * @param command the name of the Command.
   * @return true when the command exists.
   */
  public boolean containsKey(String command) {
    return commandMap.containsKey(command);
  }

  /**
   * Loads the commands into the command map. Place normal commands before the mockup commands. In
   * mockup mode, additional mockup commands are added to the command map.
   */
  public void loadCommands() {
    // _________________________________________________
    // place normal commands here
    // _________________________________________________
    commandMap.put("test", new TestCommand());
    commandMap.put("variableTest", new TestCommandVariable());
    commandMap.put("parameterTest", new TestCommandParameter());
    commandMap.put("gui", new RefreshGuiCommand(eventBus));
    // _________________________________________________
    // place mockup commands here
    // _________________________________________________
    if (serverConfig.isMockupMode()) {
      LOG.info("MockUp-Mode enabled.");
      commandMap.put("startGame", new StartGameCommand(eventBus));
      commandMap.put("sendCards", new GetCardsCommand(eventBus));
      commandMap.put("move", new MoveRobotCommand(eventBus));
      commandMap.put("validateCards", new ValidateCardsCommand(eventBus));
      commandMap.put("logic", new StartLogicCommand(eventBus));
      commandMap.put("stopTimer", new StopTimerCommand(eventBus));
      commandMap.put("set", new SetCommand(eventBus));
      commandMap.put("win", new WinGameCommand(eventBus));
      commandMap.put("damage", new DamageRobotCommand());
    }

    LOG.info("All commands loaded.");
  }
}
