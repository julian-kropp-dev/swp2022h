package de.uol.swp.server.communication;

import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.RequestMessage;

/**
 * An interface used to decouple the concrete network application from the handling of connections
 * and messages.
 *
 * @see de.uol.swp.server.communication.netty.NettyServerHandler
 */
interface ServerHandlerDelegate {

  /**
   * Is called when a new client connects.
   *
   * @param ctx The MessageContext for this client
   */
  void newClientConnected(MessageContext ctx);

  /**
   * Is called when a client disconnects.
   *
   * @param ctx The MessageContext for this client
   */
  void clientDisconnected(MessageContext ctx);

  /**
   * A message from a client connected via the ChannelHandlerContext ctx is received and can be
   * processed.
   *
   * @param msg The message send from the client
   */
  void process(RequestMessage msg);
}
