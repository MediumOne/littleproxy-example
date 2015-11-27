package m1.learning.littleproxy.example;

import java.net.InetSocketAddress;
import java.util.Queue;

import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.HostNameMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;

import io.netty.handler.codec.http.HttpRequest;

public class CertSniffingMitmWithChainedProxy {
	
	private static final String UPSTREAM_PROXY_HOST = "127.0.0.1";
	private static final int UPSTREAM_PROXY_PORT = 8101;
	
	private static final int PORT = 8100;

	public static void main(String[] args) throws RootCertificateException {
		
		setupUpstreamProxy();
		
		ChainedProxyManager cpm = getChainedProxyManager();
		
		DefaultHttpProxyServer.bootstrap()
		.withPort(PORT)
		.withManInTheMiddle(new CertificateSniffingMitmManager())
		.withChainProxyManager(cpm)
		.withListenOnAllAddresses(true)
		.withName("Mitm")
		.start();
	}

	private static ChainedProxyManager getChainedProxyManager() {
		return new ChainedProxyManager() {
			
			public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
				
				ChainedProxyAdapter chainedProxy = new ChainedProxyAdapter(){
					
					@Override
					public InetSocketAddress getChainedProxyAddress() {
						return new InetSocketAddress(UPSTREAM_PROXY_HOST, UPSTREAM_PROXY_PORT);
					}					
				};		
				
				chainedProxies.add(chainedProxy);				
			}
		};
	}

	private static void setupUpstreamProxy() {
		DefaultHttpProxyServer.bootstrap()
		.withAddress(new InetSocketAddress(UPSTREAM_PROXY_HOST, UPSTREAM_PROXY_PORT))
		.withName("Upstream")
		.start();		
	}
}
