package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.message.WinMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.message.UpdateUserStatsMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is for the win command /win.
 *
 * <p>Is handle the reaction from the ChatService to let the player win, who wrote the command
 */
@SuppressWarnings("UnstableApiUsage")
public class WinGameCommand implements Command {
  private final EventBus eventBus;
  private static final Logger LOG = LogManager.getLogger(WinGameCommand.class);
  private Player winner;

  public WinGameCommand(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * This method is used when executing the command, everything we put in it is executed.
   *
   * @param chatMessageDTO The message of the command.
   * @param command The Command as a String.
   * @param variable The first variable that comes after the command, without having to specify the
   *     name of the variable.
   * @param parameter The parameters that are passed with the command.
   * @param chatService The chat service is used to send messages to the client.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Override
  public void execute(
      ChatMessageDTO chatMessageDTO,
      String command,
      String variable,
      String[][] parameter,
      ChatService chatService,
      MessageContext messageContext) {
    if (chatMessageDTO.getLobbyId().equals("GLOBAL")) {
      chatService.sendToClient(
          messageContext,
          chatMessageDTO.getLobbyId(),
          "Kann nur im Chat in einem Spiel ausgeführt werden.");
      return;
    }
    GameService gameService = GameService.getInstance();
    Optional<Game> gameOptional = gameService.getGame(chatMessageDTO.getLobbyId());
    int placement = 1;
    try {
      if (gameOptional.isPresent()
          && (variable != null
              && !variable.equals("")
              && (Integer.parseInt(variable) > 0 && Integer.parseInt(variable) < 9))) {
        placement = Integer.parseInt(variable);
      }
    } catch (NumberFormatException e) {
      LOG.info("No correct Input for WinCommand");
    } finally {
      if (gameOptional.isPresent()) {
        GameDTO game = (GameDTO) gameOptional.get();
        Optional<Game> winnerGameOptional = gameService.getGame(game.getGameId());
        winnerGameOptional.ifPresent(
            value -> this.winner = value.getPlayer(chatMessageDTO.getUserDTO()));
        gameService.sendToAllInGame(game.getGameId(), new WinMessage(game, winner));
        String id = game.getGameId();
        game.getLobby().setLobbyStatus(LobbyStatus.WAITING);
        game.getLobby().clearRobotSelection();
        game.getPlayers().forEach(p -> game.getLobby().setReady(p.getUser()));
        gameService.getGameManagement().dropGame(id);
        gameService.getGameTimerService().dropAllTimer(id);
        gameService.getGameLogicService().stopLogic(id);
        eventBus.post(
            new UpdateUserStatsMessage(
                winner.getUser(), new UserStatistic(placement, LocalDateTime.now())));
        chatService.sendToClient(
            messageContext, chatMessageDTO.getLobbyId(), "Konnte nicht ausgeführt werden.");
      }
    }
  }
}
