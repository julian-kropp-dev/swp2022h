package de.uol.swp.server.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.ChatCommandResponse;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.chat.request.ChatMessageRequest;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.lobby.LobbyService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("UnstableApiUsage")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  final UserDTO defaultUser =
      new UserDTO(UUID.randomUUID().toString(), "Marco", "test", "marco@test.de", new UserData(1));
  EventBus bus = new EventBus();
  Object event;
  @Mock LobbyService lobbyService = Mockito.mock(LobbyService.class);
  @Mock ChatMessageMessage chatMessageMessage = Mockito.mock(ChatMessageMessage.class);
  private ChatService chatService;

  @Subscribe
  void onDeadEvent(DeadEvent e) {
    this.event = e.getEvent();
    System.out.print(e.getEvent());
  }

  private MessageContext creatMessageContext() {
    return new MessageContext() {
      @Override
      public void writeAndFlush(ResponseMessage message) {}

      @Override
      public void writeAndFlush(ServerMessage message) {}
    };
  }

  /**
   * Helper method run before each test case This method resets the variable event to null and
   * registers the object of this class to the EventBus.
   */
  @BeforeEach
  void registerBus() {
    bus.register(this);
    chatService = new ChatService(bus, lobbyService);
    this.event = null;
    CommandRegister.getInstance().loadCommands();
  }

  /**
   * Helper method run after each test case This method only unregisters the object of this class
   * from the EventBus.
   *
   * @since 2019-10-10
   */
  @AfterEach
  void deregisterBus() {
    bus.unregister(this);
  }

  @Test
  void globalChatMessage() {
    chatService.globalChatMessage("testMessage");
    Assertions.assertTrue(event instanceof ChatMessageMessage);
  }

  @Test
  void commandTest() {
    chatService.commandProcessTest(
        new ChatMessageDTO(defaultUser, "/test", "testLobby"), creatMessageContext());
    Assertions.assertTrue(event instanceof ChatCommandResponse);
  }

  @Test
  @SuppressWarnings({"java:S3415", "java:S5863"})
  void invalidCommand() {
    String command = "invalidCommand";
    chatService.commandProcessTest(
        new ChatMessageDTO(defaultUser, "/" + command, "testLobby"), creatMessageContext());

    assertTrue(event instanceof ChatCommandResponse);
    ChatCommandResponse receivedMsg = (ChatCommandResponse) event;
    ChatMessageDTO chatMessageDTO = receivedMsg.getChatMassage();
    assertEquals("Kommando /" + command + " nicht gefunden", chatMessageDTO.getMessage());
    assertEquals(event, event);
    assertNotEquals(event, null);
    assertNotEquals(event, 10);
    ChatCommandResponse equalMsg = new ChatCommandResponse(chatMessageDTO);
    assertNotEquals(equalMsg, event);
    equalMsg.initWithMessage(receivedMsg);
    assertEquals(equalMsg, event);
    assertEquals(equalMsg.hashCode(), receivedMsg.hashCode());
  }

  @Test
  void commandWithVariable() {
    String command = "/variableTest ";
    String variable = "test";
    chatService.commandProcessTest(
        new ChatMessageDTO(defaultUser, command + variable, "testLobbyname"),
        creatMessageContext());
    assertTrue(event instanceof ChatCommandResponse);
    assertEquals(variable, (((ChatCommandResponse) event).getChatMassage().getMessage()));
  }

  @Test
  void commandWithParameter() {
    String command = "/parameterTest ";
    String variable = "-text ";
    String text = "textString";
    chatService.commandProcessTest(
        new ChatMessageDTO(defaultUser, command + variable + text, "testLobbyname"),
        creatMessageContext());
    assertTrue(event instanceof ChatCommandResponse);
    assertEquals((((ChatCommandResponse) event).getChatMassage().getMessage()), text);
  }

  @Test
  void newMessagesTestMessagesContextEmpty() {
    bus.post(new ChatMessageRequest(new ChatMessageDTO(defaultUser, "TestMessage", "Test")));
    assertNull(event);
  }

  @Test
  void newMessageTestMessagesEmpty() {
    ChatMessageRequest msg = new ChatMessageRequest(new ChatMessageDTO(defaultUser, "", "TEst"));
    msg.setMessageContext(creatMessageContext());
    bus.post(msg);
    assertNull(event);
    assertFalse(chatService.onNewMessageRequest(msg));
  }

  @Test
  void newMessageTestGLOBAL() {
    ChatMessageRequest msg =
        new ChatMessageRequest(new ChatMessageDTO(defaultUser, "Test", "GLOBAL"));
    msg.setMessageContext(creatMessageContext());
    bus.post(msg);
    assertTrue(event instanceof ChatMessageMessage);
    assertEquals("GLOBAL", ((ChatMessageMessage) event).getChatMessage().getLobbyId());
    assertTrue(chatService.onNewMessageRequest(msg));
  }

  @Test
  void newMessageTestLobby() {
    doAnswer(T -> event = chatMessageMessage)
        .when(lobbyService)
        .sendToAllInLobby(anyString(), any(ChatMessageMessage.class));
    ChatMessageRequest msg =
        new ChatMessageRequest(new ChatMessageDTO(defaultUser, "Test", "ABCD"));
    msg.setMessageContext(creatMessageContext());
    bus.post(msg);
    assertTrue(event instanceof ChatMessageMessage);
    assertTrue(chatService.onNewMessageRequest(msg));
  }

  @Test
  void commandProcessChatMessageNULL() {
    assertFalse(chatService.commandProcessTest(null, creatMessageContext()));
  }

  @Test
  void commandProcessMessageNULL() {
    assertFalse(
        chatService.commandProcessTest(
            new ChatMessageDTO(defaultUser, "", ""), creatMessageContext()));
  }
}
