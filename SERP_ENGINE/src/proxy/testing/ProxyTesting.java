package proxy.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ProxyTesting {

	// iface IP to be found	
	//	String[] ips = {
	//			"5.39.42.23",
	//			"5.39.42.20",
	//			"5.39.42.21",
	//			"5.39.42.22"				
	//	};

	public static void main(String[] args) throws IOException{


		// getting all my ips
		tellAllMyIPs();


		tellAllMyProxies();


		String url = "http://www.google.com/";
		String proxy = "proxy.mydomain.com";
		String port = "8080";
		URL server = new URL(url);
		Properties systemProperties = System.getProperties();
		systemProperties.setProperty("http.proxyHost",proxy);
		systemProperties.setProperty("http.proxyPort",port);
		//systemProperties.setProperty("net.useSystemProxies",true);

		HttpURLConnection connection = (HttpURLConnection)server.openConnection();
		connection.connect();
		InputStream in = connection.getInputStream();
		//readResponse(in);

		// bypassing the proxy
		URL urlone = new URL("http://www.yahoo.com");
		URLConnection conn = urlone.openConnection(Proxy.NO_PROXY);


//		//Proxy instance, proxy ip = 123.0.0.1 with port 8080
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("5.39.42.20", 8080));
//		URL url = new URL("http://www.yahoo.com");
//		HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
//		uc.connect();
//		String line;
//		StringBuilder page = new StringBuilder();
//		StringBuffer tmp = new StringBuffer();
//		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//		while ((line = in.readLine()) != null){
//			page.append(line + "\n");
//		}
//		System.out.println(page);
	}

	public static void tellAllMyProxies(){
		System.setProperty("java.net.useSystemProxies", "true");
		List l = null;
		try {
			l = ProxySelector.getDefault().select(new URI("http://www.yahoo.com"));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (l != null) {
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				java.net.Proxy proxy = (java.net.Proxy) iter.next();
				System.out.println("proxy hostname : " + proxy.type());
				InetSocketAddress addr = (InetSocketAddress) proxy.address();
				if (addr == null) {
					System.out.println("No Proxy");
				} 
				else {
					System.out.println("proxy hostname : " + addr.getHostName());
					System.out.println("proxy port : " + addr.getPort());
				}
			}
		}
	}


	public static void tellAllMyIPs()
	{
		NetworkInterface iface = null;
		String ethr;
		String myip = "";
		String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		try
		{
			for(Enumeration ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();)
			{
				iface = (NetworkInterface)ifaces.nextElement();
				ethr = iface.getDisplayName();
				//
				//				if (Pattern.matches("eth[0-9]", ethr))
				//				{
				System.out.println("Interface:" + ethr);
				InetAddress ia = null;
				for(Enumeration ips = iface.getInetAddresses();ips.hasMoreElements();)
				{
					ia = (InetAddress)ips.nextElement();
					System.out.println( ia.getCanonicalHostName());
					//						if (Pattern.matches(regex, ia.getCanonicalHostName()))
					//						{
					//							myip = ia.getCanonicalHostName();
					//							return myip;
					//						}
				}
				//}
			}
		}
		catch (SocketException e){}
		//return myip;
	}
}
