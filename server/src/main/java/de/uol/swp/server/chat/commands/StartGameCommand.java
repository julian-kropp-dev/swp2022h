package de.uol.swp.server.chat.commands;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.request.StartGameRequest;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.user.User;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.chat.Command;
import de.uol.swp.server.lobby.LobbyManagement;
import java.awt.Point;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

/**
 * This Class is for the GameStart command /gameStart.
 *
 * <p>Is handle the reaction from the ChatService to create a new Lobby
 */
@SuppressWarnings("UnstableApiUsage")
public class StartGameCommand implements Command {

  private final LobbyManagement lobbyManagement = LobbyManagement.getInstance();
  private final EventBus eventBus;

  public StartGameCommand(EventBus eventBus) {
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
    LobbyDTO lobbyDTO = lobbyManagement.getLobbyDTO(chatMessageDTO.getLobbyId());
    if (chatMessageDTO.getLobbyId().equals("GLOBAL")) {
      chatService.sendToClient(
          messageContext,
          chatMessageDTO.getLobbyId(),
          "Kann nur im Chat einer Lobby ausgeführt werden.");
      return;
    }
    chatService.sendToClient(messageContext, chatMessageDTO.getLobbyId(), "Starte das Spiel");
    int robotStyleInt = 0;
    for (User user : lobbyDTO.getUsers()) {
      if (!lobbyDTO.getReady(user)) {
        lobbyDTO.setReady(user);
      }
      if (!lobbyDTO.isRobotSelected(user)) {
        while (lobbyDTO.getRobotSelected(Robots.values()[robotStyleInt])) {
          robotStyleInt++;
        }
        lobbyDTO.setRobotSelected(Robots.values()[robotStyleInt], user);
        robotStyleInt++;
      }
    }

    FloorPlanSetting[] floorPlanSetting = lobbyDTO.getOptions().getFloorPlanSettings();
    if (floorPlanSetting[0].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSetting[1].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSetting[2].getFloorPlansEnum() == FloorPlans.EMPTY
        && floorPlanSetting[3].getFloorPlansEnum() == FloorPlans.EMPTY
        && (Arrays.stream(lobbyDTO.getOptions().getFloorPlanSettings())
            .anyMatch(floorPlans -> floorPlans.getFloorPlansEnum().equals(FloorPlans.EMPTY)))) {
      HashMap<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition = new HashMap<>();
      EnumMap<FloorPlans, Point> newCheckpoint = new EnumMap<>(FloorPlans.class);
      newCheckpoint.put(FloorPlans.CROSS, new Point(4, 0));
      EnumMap<FloorPlans, Point> newCheckpoint2 = new EnumMap<>(FloorPlans.class);
      newCheckpoint2.put(FloorPlans.CROSS, new Point(4, 1));
      checkpointsPosition.put(1, newCheckpoint);
      checkpointsPosition.put(2, newCheckpoint2);
      lobbyDTO
          .getOptions()
          .setFloorPlanSettings(
              new FloorPlanSetting[] {
                new FloorPlanSetting(FloorPlans.CROSS), new FloorPlanSetting(FloorPlans.EMPTY),
                new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
              });
      lobbyDTO.getOptions().setCheckpointsPosition(checkpointsPosition);
    }
    eventBus.post(
        new StartGameRequest(chatMessageDTO.getLobbyId(), chatMessageDTO.getUserDTO(), true));
  }
}
