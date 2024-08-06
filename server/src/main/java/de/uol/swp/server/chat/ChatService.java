package de.uol.swp.server.chat;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.chat.ChatCommandResponse;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.chat.request.ChatMessageRequest;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.message.ClientAuthorizedMessage;
import de.uol.swp.server.message.ClientLoggedOutMessage;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This class is responsible for everything that has to do with chats. */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class ChatService extends AbstractService {
  private static final Logger LOG = LogManager.getLogger(ChatService.class);
  private static final String GLOBAL_LOBBY_ID = "GLOBAL";
  private static final String COMMAND_CHAR = "/";
  private static final UserDTO USER_DTO =
      new UserDTO(
          UUID.randomUUID().toString(),
          "System",
          "keinPasswordNötig",
          "keineEmail@nötig.org",
          new UserData(1));
  private static ChatService instance = null;
  private final CommandRegister commandRegister;
  private final LobbyService lobbyService;

  /** Constructor. */
  @Inject
  public ChatService(EventBus eventBus, LobbyService lobbyService) {
    super(eventBus);
    LOG.debug("ChatService created: {}", this);
    this.lobbyService = lobbyService;
    commandRegister = new CommandRegister(eventBus);
  }

  /** Return to the old Instance of the ChatService. */
  public static ChatService getInstance(EventBus bus, LobbyService lobbyService) {
    if (instance == null) {
      instance = new ChatService(bus, lobbyService);
    }
    return instance;
  }

  /**
   * This function receives messages from the clients and forwards them to the other clients.
   *
   * @param chatMessageRequest The message from the client.
   */
  @Subscribe
  public boolean onNewMessageRequest(ChatMessageRequest chatMessageRequest) {
    Optional<MessageContext> optionalMessageContext = chatMessageRequest.getMessageContext();
    if (optionalMessageContext.isEmpty()) {
      LOG.error("Error reading the message context");
      return false;
    }
    if (chatMessageRequest.getMessage().getMessage().isBlank()) {
      LOG.error("Chat message is blank");
      return false;
    }
    if (!(commandProcess(chatMessageRequest.getMessage(), optionalMessageContext.get()))) {
      ServerMessage msg = new ChatMessageMessage(chatMessageRequest.getMessage());
      if (chatMessageRequest.getMessage().getLobbyId().equals(GLOBAL_LOBBY_ID)) {
        post(msg);
      } else {
        // LobbyService is only used here: better communicate by ServerInternalMessage
        lobbyService.sendToAllInLobby(chatMessageRequest.getMessage().getLobbyId(), msg);
      }
      return true;
    }
    return false;
  }

  /**
   * This function displays a message in global chat.
   *
   * @param message The message to display.
   */
  public void globalChatMessage(String message) {
    ServerMessage msg =
        new ChatMessageMessage(new ChatMessageDTO(USER_DTO, message, GLOBAL_LOBBY_ID));
    post(msg);
  }

  /**
   * This method is to test the commands.
   *
   * @param chatMessageDTO a message.
   * @param messageContext The message context.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public boolean commandProcessTest(ChatMessageDTO chatMessageDTO, MessageContext messageContext) {
    return commandProcess(chatMessageDTO, messageContext);
  }

  /**
   * This method checks whether there is a command in the message and processes it.
   *
   * @param chatMessage a message.
   * @param messageContext The message context.
   */
  private boolean commandProcess(ChatMessageDTO chatMessage, MessageContext messageContext) {
    // Check on the ChatMessage is empty.
    if (chatMessage == null) {
      return false;
    }
    // Get the message from the message.
    String message = chatMessage.getMessage();
    // Check if the message is blank.
    if (message.isBlank()) {
      return false;
    }
    // Check whether the command starter is included
    if (!message.contains(COMMAND_CHAR)) {
      return false;
    }
    commandProcessRecursion(message, chatMessage.getLobbyId(), chatMessage, messageContext);
    return true;
  }

  /**
   * This method checks whether there are commands in the message and processes them.
   *
   * @param message the message.
   * @param lobbyId The lobbyId of the lobby where the message is written.
   * @param chatMessageDTO The chat message.
   * @param messageContext The message context of the command.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void commandProcessRecursion(
      String message,
      String lobbyId,
      ChatMessageDTO chatMessageDTO,
      MessageContext messageContext) {
    message = message.replaceAll("\\s\\s+", " ");
    // save where the command is started
    int indexOfCommand = message.indexOf(COMMAND_CHAR);
    // save where the Command ended
    int indexOfEndOfCommand = message.indexOf(" ", indexOfCommand);
    if (indexOfEndOfCommand == -1) {
      indexOfEndOfCommand = message.length();
    }
    // extract the command
    String command = message.substring((indexOfCommand + 1), indexOfEndOfCommand);
    // create new string for next command
    String newCommandString = message.substring(indexOfEndOfCommand);
    String parameterString;
    // generate the parameter string
    if (newCommandString.contains(COMMAND_CHAR)) {
      parameterString = message.substring(indexOfEndOfCommand);
      parameterString = parameterString.substring(newCommandString.indexOf(COMMAND_CHAR));
    } else {
      parameterString = message.substring(indexOfEndOfCommand - 1);
    }
    // get the parameters and their variables
    String[][] parameter = parameterFilter(parameterString);
    // if only one variable has to be passed, you can also pass it without a parameter name
    String variable;
    if (parameterString.length() >= 1 && parameterString.substring(1).contains(" ")) {
      variable = parameterString.substring(1, parameterString.indexOf(" ")).trim();
      if (variable.equals("")) {
        variable = parameterString.substring(1).trim();
      }
      if (variable.contains(" ")) {
        variable = variable.substring(0, variable.indexOf(" "));
      }
    } else {
      variable = parameterString.trim();
    }

    // boolean that stores whether the command was found
    LOG.debug("The Command /{} ist processed", command);
    // here the command is processed
    if (commandRegister.containsKey(command)) {
      commandRegister
          .getCommand(command)
          .execute(chatMessageDTO, command, variable, parameter, this, messageContext);
      LOG.debug("The Command /{} has been executed", command);
    } else {
      sendToClient(messageContext, lobbyId, "Kommando /" + command + " nicht gefunden");
      LOG.debug("The Command /{} was not found", command);
    }
    // query whether there is a new command
    if (newCommandString.contains(COMMAND_CHAR)) {
      // processing of the next command
      commandProcessRecursion(newCommandString, lobbyId, chatMessageDTO, messageContext);
    }
  }

  /**
   * The method filters a string for parameters and their values, which are returned in a 2d array.
   * First the parameter and then the variables of the parameter.
   *
   * @param message The string to be filtered.
   * @return a 2D String Array.
   */
  private String[][] parameterFilter(String message) {
    // create an empty 2D array
    String[][] out;
    // splitting the string at the -
    String[] firstDivider = message.split("-");
    out = new String[firstDivider.length][0];
    for (int i = 0; i < firstDivider.length; i++) {
      String[] secondDivider = firstDivider[i].split(" ");
      out[i] = secondDivider;
    }
    return out;
  }

  /**
   * The method only writes the client back to the chat that also wrote the command.
   *
   * @param messageContext The messageContext of the message where the command is written.
   * @param lobbyId The lobbyId of the lobby where the command is written.
   * @param message The message itself.
   */
  public void sendToClient(MessageContext messageContext, String lobbyId, String message) {
    ChatCommandResponse chatCommandResponse =
        new ChatCommandResponse(new ChatMessageDTO(USER_DTO, message, lobbyId));
    chatCommandResponse.setMessageContext(messageContext);
    post(chatCommandResponse);
  }

  // -------------------------------------------------------------------------------
  // User Management Events (from event bus)
  // -------------------------------------------------------------------------------
  /**
   * Handles ClientAuthorizedMessages found on the EventBus
   *
   * <p>If a ClientAuthorizedMessage is detected on the EventBus, this method is called. By chat it
   * informs all clients that a new user logged in.
   *
   * @param msg The ClientAuthorizedMessage found on the EventBus
   */
  @Subscribe
  private void onClientAuthorizedMessage(ClientAuthorizedMessage msg) {
    globalChatMessage("Der Nutzer " + msg.getUser().getUsername() + " hat sich eingeloggt.");
  }

  /**
   * Handles ClientLoggedOutMessages found on the EventBus.
   *
   * <p>If an ClientLoggedOutMessage is detected on the EventBus, this method is called. By chat it
   * informs all clients that a user logged out.
   *
   * @param msg The ClientLoggedOutMessage found on the EventBus
   */
  @Subscribe
  private void onUserLoggedOutMessage(ClientLoggedOutMessage msg) {
    globalChatMessage("Der Nutzer " + msg.getUser().getUsername() + " hat sich ausgeloggt.");
  }
}
