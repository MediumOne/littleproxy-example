package m1.learning.littleproxy.example.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.littleshoot.proxy.extras.SelfSignedSslEngineSource;

public class TestUtils {

    private TestUtils() {
    }

    /**
     * Creates and starts an embedded web server on a JVM-assigned HTTP ports.
     * Each response has a body that indicates how many bytes were received with
     * a message like "Received x bytes\n".
     * 
     * @return Instance of Server
     */
    public static Server startWebServer(int port) {
        return startWebServer(false, port);
    }

    /**
     * Creates and starts an embedded web server on a JVM-assigned HTTP ports.
     * Creates and starts embedded web server that is running on given port.
     * Each response has a body that contains the specified contents.
     *
     * @return Instance of Server
     */
    public static Server startWebServerWithResponse(byte[] content) {
        return startWebServerWithResponse(false, content);
    }

    /**
     * Creates and starts an embedded web server on JVM-assigned HTTP and HTTPS ports.
     * Each response has a body that indicates how many bytes were received with a message like
     * "Received x bytes\n".
     *
     * @param enableHttps if true, an HTTPS connector will be added to the web server
     * @return Instance of Server
     */
    public static Server startWebServer(boolean enableHttps, final int port) {
        final Server httpServer = new Server(port);

        httpServer.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                if (request.getRequestURI().contains("hang")) {
                    System.out.println("Hanging as requested");
                    try {
                        Thread.sleep(90000);
                    } catch (InterruptedException ie) {
                        System.out.println("Stopped hanging due to interruption");
                    }
                }
                
                long numberOfBytesRead = 0;
                InputStream in = new BufferedInputStream(request
                        .getInputStream());
                while (in.read() != -1) {
                    numberOfBytesRead += 1;
                }
                System.out.println("Received on port: " + port + " request: " + request.getRequestURI());
                System.out.println("Done reading # of bytes: "
                        + numberOfBytesRead);
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                byte[] content = ("Received on port: " + port + " request: " + request.getRequestURI()
                        + "\nReceived " + numberOfBytesRead + " bytes\n").getBytes();
                response.addHeader("Content-Length", Integer.toString(content.length));
                response.getOutputStream().write(content);
            }
        });
        if (enableHttps) {
            // Add SSL connector
            org.eclipse.jetty.util.ssl.SslContextFactory sslContextFactory = new org.eclipse.jetty.util.ssl.SslContextFactory();

            SelfSignedSslEngineSource contextSource = new SelfSignedSslEngineSource();
            SSLContext sslContext = contextSource.getSslContext();

            sslContextFactory.setSslContext(sslContext);
            SslSocketConnector connector = new SslSocketConnector(
                    sslContextFactory);
            connector.setPort(0);
            /*
             * <p>Ox: For some reason, on OS X, a non-zero timeout can causes
             * sporadic issues. <a href="http://stackoverflow.com/questions
             * /16191236/tomcat-startup-fails
             * -due-to-java-net-socketexception-invalid-argument-on-mac-o">This
             * StackOverflow thread</a> has some insights into it, but I don't
             * quite get it.</p>
             * 
             * <p>This can cause problems with Jetty's SSL handshaking, so I
             * have to set the handshake timeout and the maxIdleTime to 0 so
             * that the SSLSocket has an infinite timeout.</p>
             */
            connector.setHandshakeTimeout(0);
            connector.setMaxIdleTime(0);
            httpServer.addConnector(connector);
        }

        try {
            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Jetty web server", e);
        }

        return httpServer;
    }

    /**
     * Creates and starts an embedded web server on JVM-assigned HTTP and HTTPS ports.
     * Each response has a body that contains the specified contents.
     *
     * @param enableHttps if true, an HTTPS connector will be added to the web server
     * @param content The response the server will return
     * @return Instance of Server
     */
    public static Server startWebServerWithResponse(boolean enableHttps, final byte[] content) {
        final Server httpServer = new Server(0);
        httpServer.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                if (request.getRequestURI().contains("hang")) {
                    System.out.println("Hanging as requested");
                    try {
                        Thread.sleep(90000);
                    } catch (InterruptedException ie) {
                        System.out.println("Stopped hanging due to interruption");
                    }
                }

                long numberOfBytesRead = 0;
                InputStream in = new BufferedInputStream(request
                        .getInputStream());
                while (in.read() != -1) {
                    numberOfBytesRead += 1;
                }
                System.out.println("Done reading # of bytes: "
                        + numberOfBytesRead);
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                response.addHeader("Content-Length", Integer.toString(content.length));
                response.getOutputStream().write(content);
            }
        });
        if (enableHttps) {
            // Add SSL connector
            org.eclipse.jetty.util.ssl.SslContextFactory sslContextFactory = new org.eclipse.jetty.util.ssl.SslContextFactory();

            SelfSignedSslEngineSource contextSource = new SelfSignedSslEngineSource();
            SSLContext sslContext = contextSource.getSslContext();

            sslContextFactory.setSslContext(sslContext);
            SslSocketConnector connector = new SslSocketConnector(
                    sslContextFactory);
            connector.setPort(0);
            /*
             * <p>Ox: For some reason, on OS X, a non-zero timeout can causes
             * sporadic issues. <a href="http://stackoverflow.com/questions
             * /16191236/tomcat-startup-fails
             * -due-to-java-net-socketexception-invalid-argument-on-mac-o">This
             * StackOverflow thread</a> has some insights into it, but I don't
             * quite get it.</p>
             *
             * <p>This can cause problems with Jetty's SSL handshaking, so I
             * have to set the handshake timeout and the maxIdleTime to 0 so
             * that the SSLSocket has an infinite timeout.</p>
             */
            connector.setHandshakeTimeout(0);
            connector.setMaxIdleTime(0);
            httpServer.addConnector(connector);
        }

        try {
            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Jetty web server", e);
        }

        return httpServer;
    }

    /**
     * Finds the port the specified server is listening for HTTP connections on.
     *
     * @param webServer started web server
     * @return HTTP port, or -1 if no HTTP port was found
     */
    public static int findLocalHttpPort(Server webServer) {
        for (Connector connector : webServer.getConnectors()) {
            if (!(connector instanceof SslSocketConnector)) {
                return connector.getLocalPort();
            }
        }

        return -1;
    }

    /**
     * Finds the port the specified server is listening for HTTPS connections on.
     *
     * @param webServer started web server
     * @return HTTP port, or -1 if no HTTPS port was found
     */
    public static int findLocalHttpsPort(Server webServer) {
        for (Connector connector : webServer.getConnectors()) {
            if (connector instanceof SslSocketConnector) {
                return connector.getLocalPort();
            }
        }

        return -1;
    }
    

    public static int randomPort() {
        final SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 20; i++) {
            // The +1 on the random int is because
            // Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE -- caught
            // by FindBugs.
            final int randomPort = 1024 + (Math.abs(secureRandom.nextInt() + 1) % 60000);
            ServerSocket sock = null;
            try {
                sock = new ServerSocket();
                sock.bind(new InetSocketAddress("127.0.0.1", randomPort));
                return sock.getLocalPort();
            } catch (final IOException e) {
            } finally {
                if (sock != null) {
                    try {
                        sock.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // If we can't grab one of our securely chosen random ports, use
        // whatever port the OS assigns.
        ServerSocket sock = null;
        try {
            sock = new ServerSocket();
            sock.bind(null);
            return sock.getLocalPort();
        } catch (final IOException e) {
            return 1024 + (Math.abs(secureRandom.nextInt() + 1) % 60000);
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
