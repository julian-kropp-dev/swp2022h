package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.card.CardDealer;
import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.card.ProgrammingCardManager;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.request.ValidateCardSelectionRequest;
import de.uol.swp.common.game.response.ValidateCardSelectionResponse;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * The GameCardValidationTest class represents a unit test for the GameManagement class, which is
 * responsible for managing games on the server.
 */
@SuppressWarnings("UnstableApiUsage")
class GameCardValidationTest {

  private static final FloorPlanSetting[] floorPlansWithoutDirection = {
    new FloorPlanSetting(FloorPlans.CROSS), new FloorPlanSetting(FloorPlans.EMPTY),
    new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
  };
  EventBus eventBus;
  AuthenticationService authenticationService =
      new AuthenticationService(new EventBus(), new UserManagement(new MainMemoryBasedUserStore()));
  User user1;
  User user2;
  Player player1;
  Player player2;
  String lobbyId = "testValidationLobby";
  Lobby lobby;
  LobbyDTO lobbyDTO;
  LobbyManagement lobbyManagement;
  LobbyService lobbyService;
  GameManagement gameManagement;
  GameService gameServiceServer;
  ValidateCardSelectionRequest request;
  ValidateCardSelectionResponse response;

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  Optional<Game> gameOptional;

  CardDealer cardDealer;
  ProgrammingCard[][] dealtCards;

  @BeforeEach
  void setup() throws NoSuchElementException {
    eventBus = Mockito.mock(EventBus.class);
    lobbyManagement = LobbyManagement.getInstance();
    gameManagement = new GameManagement();
    lobbyService = new LobbyService(lobbyManagement, authenticationService, eventBus);
    gameServiceServer = new GameService(eventBus, gameManagement, lobbyManagement, lobbyService);
    cardDealer =
        new CardDealer(
            ProgrammingCardManager.getProgramingCardSetMove(),
            ProgrammingCardManager.getProgramingCardSetTurn());
    user1 =
        new UserDTO(
            UUID.randomUUID().toString(),
            "testUser01",
            "testPw01",
            "testMail01@test.de",
            new UserData(1));
    user2 =
        new UserDTO(
            UUID.randomUUID().toString(),
            "testUser02",
            "testPw02",
            "testMail02@test.de",
            new UserData(1));
    lobbyManagement.createLobby(lobbyId, user1, new LobbyOptions());
    lobbyDTO = lobbyManagement.getLobbyDTO(lobbyId);
    lobby = lobbyManagement.getLobby(lobbyId).get();
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.ROCKY, user1);
    lobby.setRobotSelected(Robots.BOB, user2);
    lobby.getOptions().setFloorPlanSettings(floorPlansWithoutDirection);
    gameManagement.createGame(lobbyManagement.getLobby(lobbyId));
    gameOptional = gameManagement.getGame(lobbyId);
    player1 = gameOptional.get().getPlayer(user1);
    player2 = gameOptional.get().getPlayer(user2);
    dealtCards = cardDealer.dealCards(new int[] {0, 0}, player1.getRobot().getBlockedCards());
  }

  @AfterEach
  void cleanUp() throws NoSuchElementException {
    lobbyManagement.dropLobby(lobbyId);
  }

  // -------------------------------------------------------------------------------

  @Test
  void onValidateCardSelectionRequest_Timeout() {
    GameService fastGameService =
        new GameService(eventBus, gameManagement, lobbyManagement, lobbyService, -1);
    ProgrammingCard[] selectedCards = {
      new ProgrammingCard(1, CardType.MOVE1),
    };
    ValidateCardSelectionRequest vcsRequest;
    //
    // Good lobby with game, but too late
    vcsRequest = new ValidateCardSelectionRequest(user1.getUsername(), lobbyId, selectedCards);
    fastGameService.onValidateCardSelectionRequest(vcsRequest); // OTHER GameService!
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onValidateCardSelectionRequest_NoResponse() {
    ProgrammingCard[] selectedCards = {
      new ProgrammingCard(1, CardType.MOVE1),
    };
    //
    // Bad Lobby
    gameServiceServer.onValidateCardSelectionRequest(
        new ValidateCardSelectionRequest(user1.getUsername(), "LOBY", selectedCards));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Good lobby, bad user name
    gameServiceServer.onValidateCardSelectionRequest(
        new ValidateCardSelectionRequest("player1", lobbyId, selectedCards));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Good lobby without game
    Optional<Game> gameOptional = gameServiceServer.getGame(lobbyId);
    assertTrue(gameOptional.isPresent());
    gameManagement.dropGame(lobbyId);
    gameServiceServer.onValidateCardSelectionRequest(
        new ValidateCardSelectionRequest(user1.getUsername(), lobbyId, selectedCards));
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void shouldAcceptCardSelection() {
    ProgrammingCard[] player1HardCodedHand = {
      new ProgrammingCard(1, CardType.MOVE1),
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(3, CardType.MOVE3),
      new ProgrammingCard(4, CardType.TURN_LEFT),
      new ProgrammingCard(5, CardType.TURN_RIGHT),
      new ProgrammingCard(6, CardType.U_TURN),
    };
    player1.getRobot().setHandCards(player1HardCodedHand);
    ProgrammingCard[] selectedCardsValid = {
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(3, CardType.MOVE3),
      new ProgrammingCard(1, CardType.MOVE1),
    };
    request = new ValidateCardSelectionRequest(player1.getName(), lobbyId, selectedCardsValid);

    gameServiceServer.onValidateCardSelectionRequest(request);

    ArgumentCaptor<ValidateCardSelectionResponse> responseCaptor =
        ArgumentCaptor.forClass(ValidateCardSelectionResponse.class);
    Mockito.verify(eventBus).post(responseCaptor.capture());
    response = responseCaptor.getValue();

    assertEquals(request.getRequestId(), response.getRequestId());
    assertEquals(request.getGameId(), response.getGameId());
    assertEquals(request.getUsername(), response.getUserId());
    assertTrue(response.isSuccess());
    //
    //	Detailed tests of the validation logic are in RobotTest
    //
  }

  @Test
  void shouldRejectCardSelection() {
    ProgrammingCard[] player1HardCodedHand = {
      new ProgrammingCard(1, CardType.MOVE1),
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(3, CardType.MOVE3),
      new ProgrammingCard(4, CardType.TURN_LEFT),
      new ProgrammingCard(5, CardType.TURN_RIGHT),
      new ProgrammingCard(6, CardType.U_TURN),
    };
    player1.getRobot().setHandCards(player1HardCodedHand);
    ProgrammingCard[] selectedCardsInvalid = {
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(1, CardType.MOVE1),
      new ProgrammingCard(9, CardType.TURN_LEFT),
      new ProgrammingCard(3, CardType.MOVE3),
    };
    request = new ValidateCardSelectionRequest(player1.getName(), lobbyId, selectedCardsInvalid);

    gameServiceServer.onValidateCardSelectionRequest(request);

    ArgumentCaptor<ValidateCardSelectionResponse> responseCaptor =
        ArgumentCaptor.forClass(ValidateCardSelectionResponse.class);
    Mockito.verify(eventBus).post(responseCaptor.capture());
    response = responseCaptor.getValue();

    assertEquals(request.getRequestId(), response.getRequestId());
    assertEquals(request.getGameId(), response.getGameId());
    assertEquals(request.getUsername(), response.getUserId());
    assertFalse(response.isSuccess());
    //
    //	Detailed tests of the validation logic are in RobotTest
    //
  }
}
