package de.uol.swp.server.communication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.communication.netty.NettyMessageContext;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
class ServerHandlerTest {

  private EventBus eventBus;
  private ServerHandler serverHandler;
  @Mock private ChannelHandlerContext channelHandlerContext;
  private NettyMessageContext nettyMessageContext;

  @BeforeEach
  public void setUp() {
    eventBus = mock(EventBus.class);
    serverHandler = new ServerHandler(eventBus);
    MockitoAnnotations.initMocks(this);
    nettyMessageContext = new NettyMessageContext(channelHandlerContext);
  }

  @Test
  void testProcessWithNoMessageContext() {
    RequestMessage msg = mock(RequestMessage.class);
    when(msg.getMessageContext()).thenReturn(Optional.empty());

    serverHandler.process(msg);

    verify(eventBus, times(0)).post(any());
  }

  @Test
  void testProcessWithMessageContextAndNoAuthorizationNeeded() {
    RequestMessage msg = mock(RequestMessage.class);
    MessageContext ctx = mock(MessageContext.class);

    when(msg.getMessageContext()).thenReturn(Optional.of(ctx));
    when(msg.authorizationNeeded()).thenReturn(false);

    serverHandler.process(msg);

    verify(eventBus, times(1)).post(msg);
  }

  @Test
  void testNewClientConnected() throws NoSuchFieldException, IllegalAccessException {
    MessageContext ctx = Mockito.mock(MessageContext.class);

    serverHandler.newClientConnected(ctx);

    Field field = ServerHandler.class.getDeclaredField("connectedClients");
    field.setAccessible(true);
    List<MessageContext> connectedClients = (List<MessageContext>) field.get(serverHandler);

    assertTrue(connectedClients.contains(ctx));
  }

  @Test
  void process_MessageNeedsAuthorization_ClientNotLoggedIn() {
    RequestMessage msg = mock(RequestMessage.class);
    when(msg.getMessageContext()).thenReturn(Optional.of(nettyMessageContext));
    when(msg.authorizationNeeded()).thenReturn(true);

    serverHandler.process(msg);

    verify(eventBus, never()).post(msg);
  }

  @Test
  void process_MessageDoesNotNeedAuthorization_ClientNotLoggedIn() {
    RequestMessage msg = mock(RequestMessage.class);
    when(msg.getMessageContext()).thenReturn(Optional.of(nettyMessageContext));
    when(msg.authorizationNeeded()).thenReturn(false);

    serverHandler.process(msg);

    verify(eventBus).post(msg);
  }

  @Test
  void process_MessageNeedsAuthorization_ClientLoggedIn() {
    serverHandler.newClientConnected(nettyMessageContext);
    Session session = new UUIDSession(new UserDTO("w", "w", "w", "s@.d", new UserData(2)));
    serverHandler.activeSessions.put(nettyMessageContext, session);

    RequestMessage msg = mock(RequestMessage.class);
    when(msg.getMessageContext()).thenReturn(Optional.of(nettyMessageContext));
    when(msg.authorizationNeeded()).thenReturn(true);

    serverHandler.process(msg);

    verify(eventBus).post(msg);
  }
}
