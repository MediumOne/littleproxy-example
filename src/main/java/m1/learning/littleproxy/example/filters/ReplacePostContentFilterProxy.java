package m1.learning.littleproxy.example.filters;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

/**
 * Modifies content of POST requests to http://httpbin.org/post.
 * 
 * Test URL : http://httpbin.org/post
 */
public class ReplacePostContentFilterProxy {

    private static final int PORT = 8100;

    public static void main(String[] args) {

        HttpFiltersSource filtersSource = getFiltersSource();

        DefaultHttpProxyServer.bootstrap()
        .withPort(PORT)
        .withAllowLocalOnly(false)
        .withFiltersSource(filtersSource)
        .withName("ReplacePostContentFilterProxy")
        .start();
    }

    private static HttpFiltersSource getFiltersSource() {
        return new HttpFiltersSourceAdapter(){
            
            @Override
            public int getMaximumRequestBufferSizeInBytes() {
                return 512 * 1024;
            }

            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest) {

                return new HttpFiltersAdapter(originalRequest){

                    @Override
                    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                        
                        if(httpObject instanceof FullHttpRequest){
                            FullHttpRequest request = (FullHttpRequest) httpObject;
                            
                            if(request.getMethod() == HttpMethod.POST
                                    && request.getUri().contains("/post")){
                                
                                CompositeByteBuf contentBuf = (CompositeByteBuf) request.content();           
                                
                                String contentStr = contentBuf.toString(CharsetUtil.UTF_8);
                                
                                System.out.println("Post content for " + request.getUri() + " : " + contentStr);
                                
                                String newBody = contentStr.replace("e", "ei");
                                
                                ByteBuf bodyContent = Unpooled.copiedBuffer(newBody, CharsetUtil.UTF_8);
                               
                                contentBuf.clear().writeBytes(bodyContent);
                                HttpHeaders.setContentLength(request, newBody.length());
                            }
                        }

                        return null;
                    }
                };
            }			
        };
    }
}
