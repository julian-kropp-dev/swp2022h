package de.uol.swp.server.communication.netty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.server.communication.ServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NettyServerHandlerTest {

  private ServerHandler mockDelegate;
  private NettyServerHandler serverHandler;
  private ChannelHandlerContext mockCtx;
  private RequestMessage mockRequestMessage;

  @BeforeEach
  void setUp() {
    mockDelegate = mock(ServerHandler.class);
    serverHandler = new NettyServerHandler(mockDelegate);
    mockCtx = mock(ChannelHandlerContext.class);
    Channel mockChannel = mock(Channel.class);
    mockRequestMessage = mock(RequestMessage.class);

    when(mockCtx.channel()).thenReturn(mockChannel);
  }

  @Test
  void channelActive_callsNewClientConnectedOnDelegate() {
    serverHandler.channelActive(mockCtx);

    verify(mockDelegate, times(1)).newClientConnected(any(NettyMessageContext.class));
  }

  @Test
  void channelRead0_setsMessageContextAndCallsProcessOnDelegate() {
    NettyMessageContext expectedMessageContext = new NettyMessageContext(mockCtx);
    when(mockRequestMessage.getMessageContext()).thenReturn(null);

    serverHandler.channelRead0(mockCtx, mockRequestMessage);

    verify(mockRequestMessage, times(1)).setMessageContext(expectedMessageContext);
    verify(mockDelegate, times(1)).process(mockRequestMessage);
  }

  @Test
  void exceptionCaught_withActiveChannel_logsError() {
    Throwable mockThrowable = mock(Throwable.class);
    when(mockCtx.channel().isActive()).thenReturn(true);

    serverHandler.exceptionCaught(mockCtx, mockThrowable);

    verify(mockDelegate, never()).clientDisconnected(any(NettyMessageContext.class));
  }

  @Test
  void exceptionCaught_withInactiveChannel_delegatesToClientDisconnected() {
    Throwable mockThrowable = mock(Throwable.class);
    when(mockCtx.channel().isActive()).thenReturn(false);

    serverHandler.exceptionCaught(mockCtx, mockThrowable);

    verify(mockDelegate, times(1)).clientDisconnected(any(NettyMessageContext.class));
  }
}
