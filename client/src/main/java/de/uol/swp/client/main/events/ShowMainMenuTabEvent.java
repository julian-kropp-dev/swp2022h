package de.uol.swp.client.main.events;

import javafx.scene.control.Tab;

/**
 * Event to show the MainMenu.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class ShowMainMenuTabEvent {

  private final Tab tabToClose;

  /**
   * Constructor to set the tabToClose.
   *
   * @param tabToClose tab that should be closed
   */
  public ShowMainMenuTabEvent(Tab tabToClose) {
    this.tabToClose = tabToClose;
  }

  /**
   * Getter for tabToClose.
   *
   * @return the tab to close
   */
  public Tab getTabToClose() {
    return tabToClose;
  }
}
