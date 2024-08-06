package de.uol.swp.server.communication.netty;

import de.uol.swp.common.MyObjectDecoder;
import de.uol.swp.common.MyObjectEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This class handles opening a port clients can connect to. */
@SuppressWarnings("deprecation")
public class Server {

  private static final Logger LOG = LogManager.getLogger(Server.class);
  private final ChannelHandler serverHandler;

  /**
   * Constructor.
   *
   * <p>Creates a new Server Object
   *
   * @see io.netty.channel.ChannelHandler
   * @see de.uol.swp.server.communication.ServerHandler
   */
  public Server(ChannelHandler serverHandler) {
    this.serverHandler = serverHandler;
  }

  /**
   * Start a new server on given port.
   *
   * @param port port number the server shall be reachable on
   * @throws InterruptedException server failed to start e.g. because the port is already in use
   * @see InetSocketAddress
   */
  @SuppressWarnings({"java:S1141", "java:S1874"})
  public void start(int port) throws InterruptedException, CertificateException, SSLException {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      SslContext context =
          SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
              .protocols("TLSv1.3")
              .build();
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(port))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) {
                  SslHandler sslHandler = context.newHandler(ch.alloc());
                  // Encoder and decoder are both needed! Send and
                  // receive serializable objects
                  ch.pipeline().addLast(sslHandler);
                  ch.pipeline().addLast(new MyObjectEncoder());
                  ch.pipeline().addLast(new MyObjectDecoder(ClassResolvers.cacheDisabled(null)));
                  // must be last in the pipeline else they will not
                  // get encoded/decoded objects but ByteBuf
                  ch.pipeline().addLast(serverHandler);
                }
              });
      // Just wait for server shutdown
      try {
        ChannelFuture f = b.bind().sync();
        LOG.info("Server started!");
        f.channel().closeFuture().sync();
      } catch (Exception e) {
        LOG.fatal(
            "Connection failed. Error type: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        throw new InterruptedException(
            "Der Server ist offline oder reagiert nicht.\nDas Programm wird beendet.");
      }

    } catch (CertificateException e) {
      LOG.warn("Error when exchanging certificates for SSL.");
      throw new CertificateException(e);
    } catch (SSLException e) {
      LOG.warn("Error creating SSL certificate.");
      throw new SSLException(e);
    } finally {
      bossGroup.shutdownGracefully().sync();
      workerGroup.shutdownGracefully().sync();
    }
  }
}
