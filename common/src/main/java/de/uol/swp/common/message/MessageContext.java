package de.uol.swp.common.message;

import java.io.Serializable;

/**
 * Interface to encapsulate different Types of MessageContexts.
 *
 * <p>In the base project the only implementation of this interface is the NettyMessageContext
 * within the communication package of the server
 */
public interface MessageContext extends Serializable {

  /**
   * Send a ResponseMessage.
   *
   * @param message The message that should be sent
   */
  void writeAndFlush(ResponseMessage message);

  /**
   * Send a ServerMessage.
   *
   * @param message The server message that should be sent
   */
  void writeAndFlush(ServerMessage message);
}
