package de.uol.swp.common.game.message;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnblockCardInteractionMessageTest {

  User expectedUser =
      new UserDTO(UUID.randomUUID().toString(), "test", "sfd", "qwe@.de", new UserData(1));
  String gameId = "ssss";
  boolean[] blockedCards = new boolean[] {false, false, true, true, true};
  int amount = 3;
  UnblockCardInteractionMessage message =
      new UnblockCardInteractionMessage(expectedUser, gameId, blockedCards, amount);

  @Test
  void getUser() {
    User actualUser = message.getUser();
    assertEquals(expectedUser, actualUser);
  }

  @Test
  void getGameId() {
    String actualId = message.getGameId();
    assertEquals(gameId, actualId);
  }

  @Test
  void getBlocked() {
    boolean[] actualBlocked = message.getBlocked();
    assertEquals(blockedCards, actualBlocked);
  }

  @Test
  void getAmount() {
    int actualAmount = message.getAmount();
    assertEquals(amount, actualAmount);
  }

  @Test
  void testEquals() {
    // Arrange
    User user1 =
        new UserDTO(UUID.randomUUID().toString(), "test", "sfd", "qwe@.de", new UserData(1));
    User user2 =
        new UserDTO(UUID.randomUUID().toString(), "test2", "sfd", "qwe@.de", new UserData(1));

    String gameId = "12345";
    boolean[] blocked1 = {true, false, true};
    boolean[] blocked2 = {true, false, true};
    int amount = 2;

    UnblockCardInteractionMessage message1 =
        new UnblockCardInteractionMessage(user1, gameId, blocked1, amount);
    UnblockCardInteractionMessage message2 =
        new UnblockCardInteractionMessage(user1, gameId, blocked1, amount);
    UnblockCardInteractionMessage message3 =
        new UnblockCardInteractionMessage(user2, gameId, blocked1, amount);
    UnblockCardInteractionMessage message4 =
        new UnblockCardInteractionMessage(user1, "54321", blocked1, amount);
    UnblockCardInteractionMessage message5 =
        new UnblockCardInteractionMessage(user1, gameId, blocked2, amount);
    UnblockCardInteractionMessage message6 =
        new UnblockCardInteractionMessage(user1, gameId, blocked1, 5);

    // Act & Assert
    assertEquals(message1, message1); // Reflexive property
    assertEquals(message1, message2); // Symmetric property
    assertNotEquals(message1, message3);
    assertNotEquals(message1, message4);
    assertEquals(message1, message5); // Arrays.equals() used for blocked array
    assertNotEquals(message1, message6);
  }

  @Test
  void testHashCode() {
    UnblockCardInteractionMessage message1 = message;
    assertEquals(message1.hashCode(), message.hashCode());
  }
}
