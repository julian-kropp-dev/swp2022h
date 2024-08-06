package de.uol.swp.common.message;

import de.uol.swp.common.user.Session;
import java.util.List;

/**
 * A message from server to a number of clients that is not necessary a response to a request (aka
 * server push).
 *
 * @see de.uol.swp.common.message.Message
 */
public interface ServerMessage extends Message {

  /**
   * Getter for the receiver list.
   *
   * @return List of Session objects defining the receivers
   */
  List<Session> getReceiver();

  /**
   * Sets the receivers of this message.
   *
   * @param receiver List of Session objects defining the receivers
   */
  void setReceiver(List<Session> receiver);
}
