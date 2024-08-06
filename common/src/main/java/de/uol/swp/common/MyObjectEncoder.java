package de.uol.swp.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to encode Objects into ByteBuffers to send.
 *
 * <p>An Object of this class is used in the start method of de.uol.swp.server.communication.Server
 */
@SuppressWarnings({"java:S1874", "deprecation"})
public class MyObjectEncoder extends ObjectEncoder {

  private static final Logger LOG = LogManager.getLogger(MyObjectEncoder.class);

  @Override
  protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Trying to encode {}", msg);
    }
    try {
      super.encode(ctx, msg, out);
    } catch (Exception e) {
      LOG.error(e);
      throw e;
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("{} to {}", msg, out);
    }
  }
}
