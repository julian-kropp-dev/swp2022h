package de.uol.swp.client;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uol.swp.common.MyObjectDecoder;
import de.uol.swp.common.MyObjectEncoder;
import de.uol.swp.common.message.ExceptionMessage;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.RequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The ClientConnection Connection class.
 *
 * <p>This Class manages connecting to a server, disconnecting from the server and handling of
 * incoming and outgoing messages.
 */
@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class ClientConnection {

  private static final Logger LOG = LogManager.getLogger(ClientConnection.class);
  private final String host;
  private final int port;
  private final List<ConnectionListener> connectionListener = new CopyOnWriteArrayList<>();
  private EventLoopGroup group;
  private EventBus eventBus;
  private Channel channel;

  /**
   * Creates a new connection to a specific port on the given host.
   *
   * @param host The server name or IP to connect to
   * @param port The server port to connect to
   */
  @Inject
  public ClientConnection(@Assisted String host, @Assisted int port, EventBus eventBus) {
    this.host = host;
    this.port = port;
    setEventBus(eventBus);
  }

  /**
   * Sets the EventBus for the object
   *
   * <p>Sets the EventBus for the object and registers the object to it.
   *
   * @implNote If the object already has an EventBus it is replaced but not unregistered
   * @param eventBus The new EventBus to set
   */
  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(this);
  }

  /**
   * The netty init method
   *
   * <p>The example method on how to initialize a connection to a server via netty. Inside the
   * ChannelInitializer multiple settings are made with the {@code pipeline.addLast()} method.
   * Things usually added are encoders, decoders and the ChannelHandler.
   *
   * @implNote If no ChannelHandler is added, communication will not be possible
   * @throws InterruptedException Connection failed
   */
  @SuppressWarnings("java:S1874")
  public void start() throws InterruptedException, SSLException {
    LOG.info("Trying to connect to server");
    group = new NioEventLoopGroup();
    try {
      SslContext context =
          SslContextBuilder.forClient()
              .protocols("TLSv1.3")
              .trustManager(InsecureTrustManagerFactory.INSTANCE)
              .build();
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(
              new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) {
                  SslHandler sslHandler = context.newHandler(ch.alloc());
                  // Add both Encoder and Decoder to send and receive serializable objects
                  ch.pipeline().addLast(sslHandler);
                  ch.pipeline().addLast(new MyObjectEncoder());
                  ch.pipeline().addLast(new MyObjectDecoder(ClassResolvers.cacheDisabled(null)));
                  // Add a client handler
                  ch.pipeline().addLast(new ClientHandler(ClientConnection.this));
                }
              });
      try {
        ChannelFuture channelFuture = b.connect().sync();
        channelFuture.channel().closeFuture().sync();
      } catch (Exception e) {
        LOG.info("Server is offline or unresponsive");
        throw new InterruptedException(
            "Der Server ist offline oder reagiert nicht.\nDas Programm wird beendet.");
      }
    } finally {
      group.shutdownGracefully().sync();
    }
  }

  /**
   * Disconnects the client from the server.
   *
   * <p>Disconnects the client from the server and prints the stack trace if an InterruptedException
   * is thrown.
   */
  public void close() throws InterruptedException {
    group.shutdownGracefully().sync();
  }

  /**
   * Calls the ConnectionEstablished method of every ConnectionListener added to this.
   *
   * @param channel The netty channel the new Connection is established on
   * @see de.uol.swp.client.ConnectionListener
   */
  void fireConnectionEstablished(Channel channel) {
    for (ConnectionListener listener : connectionListener) {
      listener.connectionEstablished(channel);
    }
    this.channel = channel;
  }

  /**
   * Add a new ConnectionListener to the ConnectionListener Array of this Object.
   *
   * @param listener The ConnectionListener to add to the Array
   * @see de.uol.swp.client.ConnectionListener
   */
  public void addConnectionListener(ConnectionListener listener) {
    this.connectionListener.add(listener);
  }

  /**
   * Processes the incoming messages
   *
   * <p>This method posts the message it gets on the EventBus
   *
   * <p>Post on event bus " and the Message to the LOG if the LOG-Level is set to DEBUG or higher.
   * If it is a different kind of Message, it gets discarded and with LOG-Level set to WARN or
   * higher "Can only process ServerMessage and ResponseMessage. Received " and the message are
   * written to the LOG.
   *
   * @param in The incoming messages read by the ClientHandler
   * @see de.uol.swp.client.ClientHandler
   */
  public void receivedMessage(Message in) {
    LOG.debug("Received message. Post on event bus {}", in);
    eventBus.post(in);
  }

  /**
   * Handles RequestMessages detected on the EventBus
   *
   * <p>If the client is connected to the server and the channel of this object is set, the
   * RequestMessage given to this method is sent to the server. Otherwise, "Some tries to send a
   * message, but server is not connected" is written to the LOG if the LOG-Level is set to WARN or
   * higher.
   *
   * @param message The RequestMessage object to send to the server
   */
  @Subscribe
  public void onRequestMessage(RequestMessage message) {
    if (channel != null) {
      channel.writeAndFlush(message);
    } else {
      throw new NotYetConnectedException();
    }
  }

  /**
   * Handles ExceptionMessages found on the EventBus
   *
   * <p>If an ExceptionMessage object is detected on the EventBus, this method is called. It calls
   * the exceptionOccurred method of every ConnectionListener in the ConnectionListener array.
   *
   * @param message The ExceptionMessage object found on the EventBus
   */
  @Subscribe
  public void onExceptionMessage(ExceptionMessage message) {
    for (ConnectionListener l : connectionListener) {
      l.exceptionOccurred(message.getException());
    }
  }

  /**
   * Handles errors produced by the EventBus
   *
   * <p>If an DeadEvent object is detected on the EventBus, this method is called. It writes
   * "DeadEvent detected " and the error message of the detected DeadEvent object to the log, if the
   * loglevel is set to WARN or higher.
   *
   * @param deadEvent The DeadEvent object found on the EventBus
   */
  @Subscribe
  public void onDeadEvent(DeadEvent deadEvent) {
    LOG.warn("DeadEvent detected {}", deadEvent);
  }

  /**
   * Handles the distribution of throwable messages
   *
   * <p>This method distributes throwable messages to the ConnectionListeners. It calls the
   * exceptionOccurred method of every ConnectionListener in the ConnectionListener array passing
   * them the message.
   *
   * @param message The ExceptionMessage object found on the EventBus
   * @see de.uol.swp.client.ClientHandler
   */
  public void process(Throwable message) {
    for (ConnectionListener l : connectionListener) {
      l.exceptionOccurred(message.getMessage());
    }
  }
}
