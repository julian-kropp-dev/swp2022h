package de.uol.swp.server;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.ServerMessage;
import java.util.Collections;

/**
 * This class is the base for creating a new Service.
 *
 * <p>This class prepares the child classes to have the EventBus set and methods post and sendToAll
 * implemented in order to reduce unnecessary code repetition.
 */
@SuppressWarnings("UnstableApiUsage")
public class AbstractService {

  private final EventBus bus;

  /**
   * Constructor.
   *
   * @param bus the EvenBus used throughout the server
   */
  public AbstractService(EventBus bus) {
    this.bus = bus;
    bus.register(this);
  }

  /**
   * Posts a message on the EventBus.
   *
   * @param message the message to post
   * @see de.uol.swp.common.message.Message
   */
  protected void post(Message message) {
    bus.post(message);
  }

  /**
   * Prepares a ServerMessage to be sent to all connected users and posts it to the EventBus.
   *
   * @param message the message to be sent to every user
   * @see de.uol.swp.common.message.ServerMessage
   */
  public void sendToAll(ServerMessage message) {
    message.setReceiver(Collections.emptyList());
    post(message);
  }
}
