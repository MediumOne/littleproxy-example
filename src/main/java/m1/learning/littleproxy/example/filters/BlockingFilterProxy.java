package m1.learning.littleproxy.example.filters;

import java.nio.charset.Charset;
import java.util.Date;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Blocks access to URLs ending in "png" or "jpeg" and returns a 502 response. 
 * 
 * Test URLs - HTTP  : http://httpbin.org/image/png
 *             HTTPS : https://httpbin.org/image/png
 */
public class BlockingFilterProxy {
	
	private static final int PORT = 8100;

	public static void main(String[] args) {
		
		HttpFiltersSource filtersSource = getFiltersSource();
		
		DefaultHttpProxyServer.bootstrap()
		.withPort(PORT)
		.withFiltersSource(filtersSource)
		.withName("BlockingFilterProxy")
		.start();
	}

	private static HttpFiltersSource getFiltersSource() {
		return new HttpFiltersSourceAdapter(){
			
			@Override
			public HttpFilters filterRequest(HttpRequest originalRequest) {
				
				return new HttpFiltersAdapter(originalRequest){
					
					@Override
					public HttpResponse clientToProxyRequest(HttpObject httpObject) {
						
						if(httpObject instanceof HttpRequest){
							HttpRequest request = (HttpRequest) httpObject;
							
							System.out.println("Method URI : " + request.getMethod() + " " + request.getUri());
							
							if(request.getUri().endsWith("png") || request.getUri().endsWith("jpeg")){
								//For URLs ending in 'png' and 'jpeg', return a 502 response.
								return getBadGatewayResponse();
							}
						}
						
						return null;
					}

					private HttpResponse getBadGatewayResponse() {
				        String body = "<!DOCTYPE HTML \"-//IETF//DTD HTML 2.0//EN\">\n"
				                + "<html><head>\n"
				                + "<title>"+"Bad Gateway"+"</title>\n"
				                + "</head><body>\n"
				                + "An error occurred"
				                + "</body></html>\n";
				        byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
				        ByteBuf content = Unpooled.copiedBuffer(bytes);
				        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, content);
				        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
				        response.headers().set("Content-Type", "text/html; charset=UTF-8");
				        response.headers().set("Date", ProxyUtils.formatDate(new Date()));
				        response.headers().set(HttpHeaders.Names.CONNECTION, "close");
				        return response;
					}					
				};
			}			
		};
	}
}
