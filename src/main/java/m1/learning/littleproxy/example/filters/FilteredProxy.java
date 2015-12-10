package m1.learning.littleproxy.example.filters;

import java.net.InetSocketAddress;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class FilteredProxy {

    private static final int PORT = 8100;

    public static void main(String[] args) {

        HttpFiltersSource filtersSource = getFiltersSource();

        DefaultHttpProxyServer.bootstrap()
        .withPort(PORT)
        .withFiltersSource(filtersSource)
        .withAllowLocalOnly(false)
        .withName("FilteringProxy")
        .start();
    }

    private static HttpFiltersSource getFiltersSource() {
        return new HttpFiltersSource() {

            public int getMaximumResponseBufferSizeInBytes() {
                return 0; 
            }

            public int getMaximumRequestBufferSizeInBytes() {
                return 0;
            }

            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFilters() {

                    public void serverToProxyResponseTimedOut() {
                        System.out.println("serverToProxyResponseTimedOut");						
                    }

                    public void serverToProxyResponseReceiving() {
                        System.out.println("serverToProxyResponseReceiving");

                    }

                    public void serverToProxyResponseReceived() {
                        System.out.println("serverToProxyResponseReceived");
                    }

                    public HttpObject serverToProxyResponse(HttpObject httpObject) {
                        System.out.println("serverToProxyResponse");
                        return httpObject;
                    }

                    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
                        System.out.println("proxyToServerResolutionSucceeded");
                    }

                    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
                        System.out.println("proxyToServerResolutionStarted");
                        return null;
                    }

                    public void proxyToServerResolutionFailed(String hostAndPort) {
                        System.out.println("proxyToServerResolutionFailed");
                    }

                    public void proxyToServerRequestSent() {
                        System.out.println("proxyToServerRequestSent");
                    }

                    public void proxyToServerRequestSending() {
                        System.out.println("proxyToServerRequestSending");
                    }

                    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                        System.out.println("proxyToServerRequest");
                        return null;
                    }

                    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
                        System.out.println("proxyToServerConnectionSucceeded");
                    }

                    public void proxyToServerConnectionStarted() {
                        System.out.println("proxyToServerConnectionStarted");
                    }

                    public void proxyToServerConnectionSSLHandshakeStarted() {
                        System.out.println("proxyToServerConnectionSSLHandshakeStarted");
                    }

                    public void proxyToServerConnectionQueued() {
                        System.out.println("proxyToServerConnectionQueued");
                    }

                    public void proxyToServerConnectionFailed() {
                        System.out.println("proxyToServerConnectionFailed");
                    }

                    public HttpObject proxyToClientResponse(HttpObject httpObject) {
                        System.out.println("proxyToClientResponse");
                        return httpObject;
                    }

                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                        System.out.println("clientToProxyRequest");
                        return null;
                    }
                };
            }
        };
    }
}
