package de.uol.swp.server.communication.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.uol.swp.common.game.message.GameStartMessage;
import de.uol.swp.common.message.ExceptionMessage;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NettyMessageContextTest {

  private ChannelHandlerContext mockedCtx;
  private NettyMessageContext messageContext;

  @BeforeEach
  void setUp() {
    mockedCtx = mock(ChannelHandlerContext.class);
    messageContext = new NettyMessageContext(mockedCtx);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void getCtx_returnsCorrectValue() {
    ChannelHandlerContext expectedCtx = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext = new NettyMessageContext(expectedCtx);

    ChannelHandlerContext actualCtx = messageContext.getCtx();

    assertEquals(expectedCtx, actualCtx);
  }

  @Test
  void testWriteAndFlush_responseMessage() {
    ResponseMessage responseMessage = new ExceptionMessage("testCase");

    messageContext.writeAndFlush(responseMessage);

    verify(mockedCtx, times(1)).writeAndFlush(responseMessage);
  }

  @Test
  void testWriteAndFlush_serverMessage() {
    ServerMessage responseMessage = new GameStartMessage("TEST");

    messageContext.writeAndFlush(responseMessage);

    verify(mockedCtx, times(1)).writeAndFlush(responseMessage);
  }

  @Test
  void equals_withSameObject_returnsTrue() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext = new NettyMessageContext(ctx);

    assertEquals(messageContext, messageContext);
  }

  @Test
  void equals_withNullObject_returnsFalse() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext = new NettyMessageContext(ctx);

    assertNotEquals(null, messageContext);
  }

  @Test
  void equals_withDifferentClass_returnsFalse() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext = new NettyMessageContext(ctx);

    assertNotEquals("Not a NettyMessageContext", messageContext);
  }

  @Test
  void equals_withSameContext_returnsTrue() {
    ChannelHandlerContext ctx1 = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext1 = new NettyMessageContext(ctx1);
    NettyMessageContext messageContext2 = new NettyMessageContext(ctx1);

    boolean result = messageContext1.equals(messageContext2);

    assertTrue(result);
  }

  @Test
  void equals_withDifferentContext_returnsFalse() {
    ChannelHandlerContext ctx1 = mock(ChannelHandlerContext.class);
    ChannelHandlerContext ctx2 = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext1 = new NettyMessageContext(ctx1);
    NettyMessageContext messageContext2 = new NettyMessageContext(ctx2);

    boolean result = messageContext1.equals(messageContext2);

    assertFalse(result);
  }

  @Test
  void testHashCode() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    NettyMessageContext messageContext1 = new NettyMessageContext(ctx);
    NettyMessageContext messageContext2 = new NettyMessageContext(ctx);

    int hashCode1 = messageContext1.hashCode();
    int hashCode2 = messageContext2.hashCode();

    assertEquals(hashCode1, hashCode2);
  }
}
