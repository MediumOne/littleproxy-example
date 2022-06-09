package m1.learning.littleproxy.example;

import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class SimpleProxy {

    private static final int PORT = 8100;

    public static void main(String[] args) {

        DefaultHttpProxyServer.bootstrap()
                .withPort(PORT)
                .start();
    }
}
