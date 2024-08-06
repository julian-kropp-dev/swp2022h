package de.uol.swp.common.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Test Class for the GameDTO
 *
 * @author Marco Grawunder
 * @since 2019-10-08
 */
class GameDTOTest {

  private static final User defaultUser =
      new UserDTO(
          UUID.randomUUID().toString(), "julian", "julian", "julian@swp.de", new UserData(1));
  private static final List<User> users;

  static {
    users = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      users.add(
          new UserDTO(
              UUID.randomUUID().toString(),
              "julian" + i,
              "julian" + i,
              "julian" + i + "@swp.de",
              new UserData(1)));
    }
  }

  // Here I create a lobby first, because gameName and gameCreator should be equivalent to
  // lobbyId and lobbyCreator
  private final Lobby lobby = new LobbyDTO("qm34", defaultUser);
  FloorPlan floorPlan =
      new FloorPlan(
          "096;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000");

  /**
   * This test check whether a game is created correctly
   *
   * <p>If the variables are not set correctly the test fails
   */
  @Test
  void createGame() {
    lobby.setRobotSelected(Robots.BOB, defaultUser);
    Game game = new GameDTO(lobby, floorPlan);

    assertEquals("qm34", game.getGameId());
    assertEquals(1, game.getPlayers().size());
    assertEquals(defaultUser, lobby.getUsers().iterator().next());
  }

  /**
   * This test check whether a user can leave a lobby
   *
   * <p>The test fails if the size of the user list of the lobby does not get smaller or the user
   * who left is still in the list.
   */
  @Test
  void leaveUserGameTest() throws NoSuchElementException {
    users.forEach(lobby::joinUser);
    lobby.setRobotSelected(Robots.GREGOR, defaultUser);
    for (User user : users) {
      lobby.setRobotSelected(Robots.DUSTY, user);
    }
    Game game = new GameDTO(lobby, floorPlan);

    assertEquals(game.getPlayers().size(), users.size() + 1);
    game.leaveUser(users.get(0));

    assertEquals(game.getPlayers().size(), users.size());
    @SuppressWarnings("java:S5778")
    Exception exception = assertThrows(RuntimeException.class, () -> game.getPlayer(users.get(0)));
    assertEquals("Player for User does not exists.", exception.getMessage());
  }

  /** Test the firstRoundFirstCardMoveCardMethod */
  @Test
  void firstRoundFirstCardMoveCardTest() {
    User secondUser = users.get(8);
    lobby.joinUser(secondUser);
    lobby.setRobotSelected(Robots.GANDALF, defaultUser);
    lobby.setRobotSelected(Robots.BOB, secondUser);
    Game game = new GameDTO(lobby, floorPlan);

    // Move Card already in first Slot
    ProgrammingCard[] cardSet1 = new ProgrammingCard[5];
    cardSet1[0] = new ProgrammingCard(420, CardType.MOVE1);
    for (int i = 1; i < cardSet1.length; i++) {
      cardSet1[i] = new ProgrammingCard(690, CardType.TURN_LEFT);
    }
    game.getPlayer(defaultUser).getRobot().setSelectedCards(cardSet1);

    // Move Card not in first Slot
    ProgrammingCard[] cardSet2 = new ProgrammingCard[5];
    for (int i = 0; i < cardSet2.length; i++) {
      cardSet2[i] = new ProgrammingCard(420, CardType.U_TURN);
    }
    cardSet2[4] = new ProgrammingCard(680, CardType.MOVE1);
    game.getPlayer(users.get(8)).getRobot().setSelectedCards(cardSet2);

    game.firstCardFirstRoundMoveCard();

    assertTrue(game.getPlayer(defaultUser).getRobot().getSelectedCards()[0].isMoveCard());
    assertTrue(game.getPlayer(secondUser).getRobot().getSelectedCards()[0].isMoveCard());

    game.getPlayer(defaultUser).getRobot().setSelectedCards(cardSet2);
    game.firstCardFirstRoundMoveCard();
    assertFalse(game.getPlayer(defaultUser).getRobot().getSelectedCards()[0].isMoveCard());
  }
}
