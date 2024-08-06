package de.uol.swp.client;

import de.uol.swp.common.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Netty handler for incoming connections.
 *
 * @see io.netty.channel.ChannelInboundHandlerAdapter
 */
class ClientHandler extends SimpleChannelInboundHandler<Message> {

  private static final Logger LOG = LogManager.getLogger(ClientHandler.class);

  private final ClientConnection clientConnection;

  ClientHandler(ClientConnection clientConnection) {
    this.clientConnection = clientConnection;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOG.info("Connected to server {}", ctx);
    clientConnection.fireConnectionEstablished(ctx.channel());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {
    clientConnection.receivedMessage(message);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOG.error(cause);
    clientConnection.process(cause);
    ctx.close();
  }
}
