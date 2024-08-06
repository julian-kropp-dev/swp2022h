package de.uol.swp.client.game;

import de.uol.swp.client.AbstractPresenter;
import de.uol.swp.client.lobby.events.ShowLobbyTabEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;

/**
 * Manages the EndOfGamePresenter. After finishing the game all players get redirected to this
 * screen. It shows the winner of the game. Players also have the possibility to get back to their
 * old lobby or leave the game.
 */
@SuppressWarnings("UnstableApiUsage")
public class EndOfGamePresenter extends AbstractPresenter {

  public static final String FXML = "/fxml/tab/EndOfGameTab.fxml";
  @FXML Tab endOfGameTab;
  @FXML Text txtWinner;

  /** Method is called when the leaveGame Button is pressed. */
  @FXML
  public void onLeave() {
    String name = endOfGameTab.getText().replace("Spiel ", "");
    eventBus.post(new ShowLobbyTabEvent(name, "Lobby " + name, endOfGameTab));
  }
}
