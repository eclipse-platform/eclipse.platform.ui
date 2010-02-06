/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class HttpsUtility {

	private static final String PATH_TOC = "/toc"; //$NON-NLS-1$
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$

	public static InputStream getHttpsStream(URL httpsURL)
	{
		InputStream in =null; 
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
	           in = httpsURL.openStream();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return in;
	}
	
	public static InputStream getHttpsInputStream(String thisProtocol,String thisHost, String thisPort, String thisPath, String locale)
	{
		URL url; 
		InputStream in = null;
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
	            url = new URL(thisProtocol, thisHost, new Integer(thisPort) .intValue(), 
						thisPath + PATH_TOC + '?' + PARAM_LANG + '=' + locale);
				
				in = url.openStream();
		}
		catch(Exception e)
		{
			
		}
		return in;
	}
	
	public static URL getHttpsURL(String thisProtocol,String thisHost, int thisPort, String thisPath)
	{
		URL url=null; 
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
	            url = new URL(thisProtocol, thisHost, thisPort, thisPath);
		}
		catch(Exception e)
		{
			
		}
		return url;
	}
	
	public static URL getHttpsURL(String urlPath)
	{
		URL url=null; 
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
	            url = new URL(urlPath);
		}
		catch(Exception e)
		{
			
		}
		return url;
	}
	
	public static URL getHttpsURL(String thisProtocol,String thisHost, String thisPort, String thisPath)
	{
		URL url=null; 
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
	            url = new URL(thisProtocol, thisHost, Integer.parseInt(thisPort), thisPath);
		}
		catch(Exception e)
		{
			
		}
		return url;
	}
	
	public static boolean canConnectToHttpsURL(String urlConnection)
	{
		boolean validConnection=true;
		try
		{
			System.setProperty( "java.protocol.handler.pkgs" , "javax.net.ssl" ); //$NON-NLS-1$ //$NON-NLS-2$
	           // java.security.Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                        new X509TrustManager(){
	                              public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                                    return null;
	                              }
	                        public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                              public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) { }
	                        }
	                  };

	            SSLContext sc = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$
	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            
			HttpsURLConnection testConnection = (HttpsURLConnection)new URL(urlConnection).openConnection();
			// testConnection.setReadTimeout(5000); // TODO recode to compile with Java 1.4
			testConnection.connect();
			testConnection.getContent();
		}
		catch(Exception e)
		{
			validConnection=false;
		}
		
		return validConnection;
	}
}
