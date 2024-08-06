package de.uol.swp.client;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.client.user.ClientUserService;

/**
 * This class is the base for creating a new Presenter.
 *
 * <p>This class prepares the child classes to have the UserService and EventBus set in order to
 * reduce unnecessary code repetition.
 */
@SuppressWarnings("UnstableApiUsage")
public class AbstractPresenter {

  @Inject protected ClientUserService userService;
  protected EventBus eventBus;

  /**
   * Sets the field eventBus.
   *
   * <p>This method sets the field eventBus to the EventBus given via parameter. Afterwards it
   * registers this class to the new EventBus.
   *
   * @implNote This method does not unregister this class from any EventBus it may already be
   *     registered to.
   * @param eventBus The EventBus this class should use.
   */
  @Inject
  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(this);
  }
}
