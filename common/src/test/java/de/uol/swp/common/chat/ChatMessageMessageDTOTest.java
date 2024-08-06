package de.uol.swp.common.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test Class for the ChatMassage
 *
 * @see ChatMessageDTO
 */
class ChatMessageMessageDTOTest {
  private final String testUuid = UUID.randomUUID().toString();
  private final String message = "Test message";
  private final String testUser = "testUser";
  private final String testPassword = "testPassword";
  private final String testEmail = "testEmail";
  private final UserDTO user =
      new UserDTO(testUuid, testUser, testPassword, testEmail, new UserData(1));
  private final String lobbyId = "TESTLOBBYNAME";
  private final ChatMessageDTO chatMessageDTO = new ChatMessageDTO(user, message, lobbyId);

  @Test
  void getMassage() {
    assertEquals(chatMessageDTO.getMessage(), message);
  }

  @Test
  void getUserDTO() {
    assertEquals(chatMessageDTO.getUserDTO(), user);
  }

  @Test
  void creatNewUser() {
    ChatMessageDTO test = new ChatMessageDTO(user, message, lobbyId);
    assertEquals(test.getMessage(), chatMessageDTO.getMessage());
    assertEquals(test.getUserDTO(), chatMessageDTO.getUserDTO());
    assertEquals(test, chatMessageDTO);
  }

  @Test
  void messageToString() {
    Assertions.assertEquals(chatMessageDTO.toString(), (testUser + ": " + message));
  }

  @Test
  void creatNewMessage() {
    ChatMessageDTO test = new ChatMessageDTO(user, message, lobbyId);
    assertEquals(test, chatMessageDTO);
  }
}
