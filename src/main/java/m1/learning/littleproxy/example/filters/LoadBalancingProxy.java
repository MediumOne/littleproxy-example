package m1.learning.littleproxy.example.filters;

import java.net.InetSocketAddress;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import m1.learning.littleproxy.example.utils.TestUtils;

/**
 * Proxy that chooses the webserver based on parameters in the URL or
 *              in the headers.
 * 
 * Test URLs - (1) http://<anydomainname>/node1 - selects node 1
 *             (2) http://<anydomainname>/node2 - selects node 
 *             
 *             OR
 *             
 * Requests containing 
 *             (1) the header "X-Node: node1" will cause the proxy
 * to select node 1.
 *             (2) the header "X-Node: node2" will cause the proxy
 * to select node 2.
 */
public class LoadBalancingProxy {

    private static final int PORT = 8100;
    
    private static final int WEB_SERVER_PORT_1 = 8201;
    
    private static final int WEB_SERVER_PORT_2 = 8202;
    
    private static final String HEADER_NODE = "X-Node";

    public static void main(String[] args) {
        
        //Web server 1
        setUpWebServer(WEB_SERVER_PORT_1);
        
        //Web server 2
        setUpWebServer(WEB_SERVER_PORT_2);

        HttpFiltersSource filtersSource = getFiltersSource();

        DefaultHttpProxyServer.bootstrap()
        .withPort(PORT)
        .withAllowLocalOnly(false)
        .withFiltersSource(filtersSource)
        .withName("LoadBalancingProxy")
        .start();
    }

    private static void setUpWebServer(int webServerPort) {
        TestUtils.startWebServer(webServerPort);
    }

    private static HttpFiltersSource getFiltersSource() {
        return new HttpFiltersSourceAdapter(){

            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {

                return new HttpFiltersAdapter(originalRequest){
                    
                    @Override
                    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
                        
                        System.out.println("Filter: p2SResolutionStarted for: " + resolvingServerHostAndPort);
                        System.out.println("Filter: originalRequest: " + originalRequest.getUri());
                                                
                        //Node selection based on URL
                        if(originalRequest.getUri().contains("node1")){
                            System.out.println("Routing to server 1");
                            return new InetSocketAddress("localhost", WEB_SERVER_PORT_1);
                        } else if (originalRequest.getUri().contains("node2")){
                            System.out.println("Routing to server 2");
                            return new InetSocketAddress("localhost", WEB_SERVER_PORT_2);
                        }
                        
                        //Node selection based on header
                        if(originalRequest.headers().contains(HEADER_NODE)){

                            String node = originalRequest.headers().get(HEADER_NODE).trim();
                            
                            if(node.equals("node1")){
                                System.out.println("Routing to server 1");
                                return new InetSocketAddress("localhost", WEB_SERVER_PORT_1);
                            } else if (node.equals("node2")){
                                System.out.println("Routing to server 2");
                                return new InetSocketAddress("localhost", WEB_SERVER_PORT_2);
                            }
                        }                        
                        
                        return null;
                    }

                    @Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {

                        if(httpObject instanceof HttpRequest){
                            HttpRequest request = (HttpRequest) httpObject;

                            System.out.println("Method URI : " + request.getMethod() + " " + request.getUri());
                        }

                        return null;
                    }                   			
                };
            }			
        };
    }
}
