package de.uol.swp.common.game.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidateCardsMessageTest {

  String gameId = "wwww";
  ValidateCardsMessage message = new ValidateCardsMessage(gameId);

  @Test
  void getGameId() {
    assertEquals(gameId, message.getGameId());
  }

  @Test
  void testEquals() {
    ValidateCardsMessage message1 = new ValidateCardsMessage("game1");
    ValidateCardsMessage message2 = new ValidateCardsMessage("game1");
    ValidateCardsMessage message3 = new ValidateCardsMessage("game2");

    assertEquals(message1, message1);

    assertEquals(message1, message2);
    assertEquals(message2, message1);

    assertEquals(message1, message2);
    assertNotEquals(message2, message3);
    assertNotEquals(message1, message3);

    assertNotEquals(null, message1);

    assertNotEquals("game1", message1);

    assertNotEquals(message1, message3);
  }

  @Test
  void testHashCode() {
    ValidateCardsMessage message1 = new ValidateCardsMessage("game1");
    ValidateCardsMessage message2 = new ValidateCardsMessage("game1");
    ValidateCardsMessage message3 = new ValidateCardsMessage("game2");

    assertEquals(message1.hashCode(), message2.hashCode());
    assertEquals(message1.hashCode(), message2.hashCode());

    assertEquals(message1.hashCode(), message2.hashCode());

    assertNotEquals(message1.hashCode(), message3.hashCode());
  }
}
