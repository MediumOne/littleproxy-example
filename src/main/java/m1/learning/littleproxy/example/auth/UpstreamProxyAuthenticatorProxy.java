package m1.learning.littleproxy.example.auth;

import java.net.InetSocketAddress;
import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

/**
 * A proxy that authenticates to another upstream proxy.
 */
public class UpstreamProxyAuthenticatorProxy {

    private static final String UPSTREAM_PROXY_HOST = "127.0.0.1";
    private static final int UPSTREAM_PROXY_PORT = 8101;

    private static final String USERNAME = "johndoe";
    private static final String PASSWORD = "abcd1234";

    private static final int PORT = 8100;

    public static void main(String[] args) {

        setupUpstreamProxy();

        ChainedProxyManager cpm = getChainedProxyManager();

        DefaultHttpProxyServer.bootstrap()
        .withPort(PORT)
        .withListenOnAllAddresses(true)
        .withName("AuthenticatorProxy")
        .withChainProxyManager(cpm)
        .start();
    }

    private static void setupUpstreamProxy() {
        DefaultHttpProxyServer.bootstrap()
        .withAddress(new InetSocketAddress(UPSTREAM_PROXY_HOST, UPSTREAM_PROXY_PORT))
        .withName("AuthenticatedUpstreamProxy")
        .withProxyAuthenticator(getProxyAuthenticator())
        .start();		
    }

    private static ProxyAuthenticator getProxyAuthenticator() {
        return new ProxyAuthenticator() {

            public boolean authenticate(String userName, String password) {

                if(userName.equals(USERNAME) && password.equals(PASSWORD)){
                    return true;
                }				

                return false;
            }
        };
    }

    private static ChainedProxyManager getChainedProxyManager() {

        return new ChainedProxyManager() {

            public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                chainedProxies.add(getChainedProxy());				
            }

            private ChainedProxy getChainedProxy() {

                return new ChainedProxyAdapter() {

                    public InetSocketAddress getChainedProxyAddress() {
                        return new InetSocketAddress(UPSTREAM_PROXY_HOST, UPSTREAM_PROXY_PORT);
                    }

                    public void filterRequest(HttpObject httpObject) {
                        if(httpObject instanceof HttpRequest){
                            HttpRequest request = (HttpRequest) httpObject;

                            String creds = USERNAME + ":" + PASSWORD;
                            String authValue = org.apache.commons.codec.binary.Base64.encodeBase64String(creds.getBytes());
                            String finalValue = "Basic " + authValue;
                            request.headers().add("Proxy-Authorization", finalValue);
                        }						
                    }
                };
            }
        };
    }
}
