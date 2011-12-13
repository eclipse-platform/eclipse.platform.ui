/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.MissingContentManager;
import org.eclipse.help.internal.base.remote.RemoteHelpInputStream;
import org.eclipse.help.internal.base.remote.RemoteStatusData;
import org.eclipse.help.internal.protocols.HelpURLConnection;
import org.eclipse.help.internal.protocols.HelpURLStreamHandler;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.internal.webapp.data.ServletResources;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.webapp.IFilter;

/**
 * Performs transfer of data from eclipse to a jsp/servlet
 */
public class EclipseConnector {
	public interface INotFoundCallout {
		public void notFound(String url);
	}
	private static final String errorPageBegin = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" //$NON-NLS-1$
			+ "<html><head>\n" //$NON-NLS-1$
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" //$NON-NLS-1$
			+ "</head>\n" //$NON-NLS-1$
			+ "<body><p>\n"; //$NON-NLS-1$
	private static final String errorPageEnd = "</p></body></html>"; //$NON-NLS-1$
	private static final IFilter allFilters[] = new IFilter[] {
			new HighlightFilter(), new FramesetFilter(), new InjectionFilter(false),
			new DynamicXHTMLFilter(), new BreadcrumbsFilter(), new PluginsRootFilter(),
			new ShowInTocFilter(), new ExtraFilters() };

	private static final IFilter errorPageFilters[] = new IFilter[] {
			new FramesetFilter(), new InjectionFilter(false),
			new DynamicXHTMLFilter() };

	private ServletContext context;
	private static INotFoundCallout notFoundCallout = null; // For JUnit Testing
	 
 	public EclipseConnector(ServletContext context) {
		this.context= context;
 	}


	public void transfer(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		// URL
		String pathInfo = req.getPathInfo();
		if (pathInfo == null)
			return;
		if (pathInfo.startsWith("/")) //$NON-NLS-1$
			pathInfo = pathInfo.substring(1);
		String query = req.getQueryString();
		String url = query == null ? pathInfo : (pathInfo + "?" + query); //$NON-NLS-1$

		try {
	        
		    //System.out.println("Transfer " + url); //$NON-NLS-1$
			// Redirect if the request includes PLUGINS_ROOT and is not a content request
			int index = url.lastIndexOf(HelpURLConnection.PLUGINS_ROOT);
			if (index!= -1 && url.indexOf("content/" + HelpURLConnection.PLUGINS_ROOT) == -1) {  //$NON-NLS-1$
				StringBuffer redirectURL = new StringBuffer();
				
				redirectURL.append(req.getContextPath());
				redirectURL.append(req.getServletPath());
				redirectURL.append("/"); //$NON-NLS-1$
				redirectURL.append(url.substring(index+HelpURLConnection.PLUGINS_ROOT.length()));
				
				resp.sendRedirect(redirectURL.toString());
				return;
			}
			String lowerCaseuRL = url.toLowerCase(Locale.ENGLISH);
			if (lowerCaseuRL.startsWith("jar:") //$NON-NLS-1$
					|| lowerCaseuRL.startsWith("platform:") //$NON-NLS-1$
					|| (lowerCaseuRL.startsWith("file:") && UrlUtil.wasOpenedFromHelpDisplay(url))) { //$NON-NLS-1$
				url = pathInfo; // without query
				
				// ensure the file is only accessed from a local installation
				if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER
						|| !UrlUtil.isLocalRequest(req)) {
					return;
				}
			} else {
				// enable activities matching url
				// HelpBasePlugin.getActivitySupport().enableActivities(url);

				url = URIUtil.fromString(url).toString();
				url = "help:" + url; //$NON-NLS-1$
			}

			URLConnection con = createConnection(req, resp, url);

			InputStream is;
			boolean pageNotFound = false;
			try {
				is = con.getInputStream();
			} catch (IOException ioe) {
			    pageNotFound = true;
			    if (notFoundCallout != null) {
			    	notFoundCallout.notFound(url);
			    }				
			    
			    boolean isRTopicPath = isRTopicPath(req.getServletPath());
			    
			    if (requiresErrorPage(lowerCaseuRL) && !isRTopicPath) { 
					
			    	String errorPage = null;
			    	if (RemoteStatusData.isAnyRemoteHelpUnavailable()) {
			            errorPage = '/'+HelpWebappPlugin.PLUGIN_ID+'/'+ MissingContentManager.MISSING_TOPIC_HREF;
			    	} else {
				        errorPage = MissingContentManager.getInstance().getPageNotFoundPage(url, false);
			    	}
			        if (errorPage != null && errorPage.length() > 0) {				
						con = createConnection(req, resp, "help:" + errorPage); //$NON-NLS-1$
						resp.setContentType("text/html"); //$NON-NLS-1$
						try {
						    is = con.getInputStream();
						} catch (IOException ioe2) {
							// Cannot open error page
						    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
							return;							
						}
					} else {
						// Error page not defined
					    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
				} else {
					// Non HTML file
				    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}
			catch (Exception e) {
				// if it's a wrapped exception, unwrap it
				Throwable t = e;
				if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
					t = t.getCause();
				}

				StringBuffer message = new StringBuffer();
				message.append(errorPageBegin);
				message.append("<p>"); //$NON-NLS-1$
				message.append(ServletResources.getString(
						"contentProducerException", //$NON-NLS-1$
						req));
				message.append("</p>"); //$NON-NLS-1$
				message.append("<pre>"); //$NON-NLS-1$
				Writer writer = new StringWriter();
				t.printStackTrace(new PrintWriter(writer));
				message.append(writer.toString());
				message.append("</pre>"); //$NON-NLS-1$
				message.append(errorPageEnd);
				
				is = new ByteArrayInputStream(message.toString().getBytes("UTF8")); //$NON-NLS-1$
			}

			OutputStream out = resp.getOutputStream();
			IFilter filters[] = pageNotFound ? errorPageFilters : allFilters;
			if (isProcessingRequired(resp.getContentType())) {
				for (int i = 0; i < filters.length; i++) {
					// condition for enabling remote css 
					if((filters[i] instanceof InjectionFilter) && is instanceof RemoteHelpInputStream){
						InjectionFilter ifilter = new InjectionFilter(true);
						out=ifilter.filter(req, out);
					}
					else{
						out = filters[i].filter(req, out);
					}
				}
			}

			transferContent(is, out);
			try {
			    out.close();
			} catch (IOException ioe) {
				//  Bug 314324 - do not report an error
			}
			is.close();

		} catch (Exception e) {
			String msg = "Error processing help request " + url; //$NON-NLS-1$
			HelpWebappPlugin.logError(msg, e);
		}
	}


	private boolean requiresErrorPage(String lowerCaseuRL) {
		return lowerCaseuRL.endsWith("htm") //$NON-NLS-1$
		|| lowerCaseuRL.endsWith("pdf")  //$NON-NLS-1$
		|| lowerCaseuRL.endsWith("xhtml")  //$NON-NLS-1$
		|| lowerCaseuRL.endsWith("shtml")  //$NON-NLS-1$
			|| lowerCaseuRL.endsWith("html"); //$NON-NLS-1$
	}

	private boolean isProcessingRequired(String contentType) {
		if (contentType.equals("application/xhtml+xml")) { //$NON-NLS-1$
			return true;
		}
		if (!contentType.startsWith("text")) {  //$NON-NLS-1$
				return false;
	    }
		if (contentType.equals("text/css")) { //$NON-NLS-1$
			return false;
		}
		if (contentType.equals("text/javascript")) { //$NON-NLS-1$
			return false;
		}
		return true;
	}


	private URLConnection createConnection(HttpServletRequest req,
			HttpServletResponse resp, String url) throws Exception {
		URLConnection con;
		con = openConnection(url, req, resp);
		String contentType;
		// use the context to get the mime type where possible
		String pathInfo = req.getPathInfo();
		String mimeType = context.getMimeType(pathInfo);
		if (useMimeType(req, mimeType)) {
			contentType = mimeType;
		} else {
			contentType = con.getContentType();
		}

		resp.setContentType(contentType);

		long maxAge = 0;
		try {
			// getExpiration() throws NullPointerException when URL is
			// jar:file:...
			long expiration = con.getExpiration();
			maxAge = (expiration - System.currentTimeMillis()) / 1000;
			if (maxAge < 0)
				maxAge = 0;
		} catch (Exception e) {
		}
		resp.setHeader("Cache-Control", "max-age=" + maxAge); //$NON-NLS-1$ //$NON-NLS-2$
		return con;
	}


	private boolean useMimeType(HttpServletRequest req, String mimeType) {
		if  ( mimeType == null ) {
	        return false;
        }
        if (mimeType.equals("application/xhtml+xml") && !UrlUtil.isMozilla(req)) { //$NON-NLS-1$
        	return false;
        }
        return true;
	}

	/**
	 * Write the body to the response
	 */
	private void transferContent(InputStream inputStream, OutputStream out)
			throws IOException {
		// Prepare the input stream for reading
		BufferedInputStream dataStream = new BufferedInputStream(
				inputStream);
		try {

			// Create a fixed sized buffer for reading.
			// We could create one with the size of availabe data...
			byte[] buffer = new byte[4096];
			int len = 0;
			while (true) {
				len = dataStream.read(buffer); // Read file into the byte array
				if (len == -1)
					break;
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
		}
		
		try{
			dataStream.close();
		}catch(Exception e){}
	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	private URLConnection openConnection(String url,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		URLConnection con = null;
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			// it is an infocentre, add client locale to url
			String locale = UrlUtil.getLocale(request, response);
			if (url.indexOf('?') >= 0) {
				url = url + "&lang=" + locale; //$NON-NLS-1$
			} else {
				url = url + "?lang=" + locale; //$NON-NLS-1$
			}
		}
		URL helpURL;
		if (url.startsWith("help:")) { //$NON-NLS-1$
			helpURL = new URL("help", //$NON-NLS-1$
					null, -1, url.substring("help:".length()), //$NON-NLS-1$
					HelpURLStreamHandler.getDefault());
		} else {
			if (url.startsWith("jar:")) { //$NON-NLS-1$
				// fix for bug 83929
				int excl = url.indexOf("!/"); //$NON-NLS-1$
				String jar = url.substring(0, excl);
				String path = url.length() > excl + 2 ? url.substring(excl + 2)
						: ""; //$NON-NLS-1$
				url = jar.replaceAll("!", "%21") + "!/" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ path.replaceAll("!", "%21"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			helpURL = new URL(url);
		}
		String protocol = helpURL.getProtocol();
		if (!("help".equals(protocol) //$NON-NLS-1$
				|| "file".equals(protocol) //$NON-NLS-1$
				|| "platform".equals(protocol) //$NON-NLS-1$
				|| "jar".equals(protocol))) { //$NON-NLS-1$
			throw new IOException();
		}

		con = helpURL.openConnection();

		con.setAllowUserInteraction(false);
		con.setDoInput(true);
		con.connect();
		return con;
	}

	public static void setNotFoundCallout(INotFoundCallout callout) {
		notFoundCallout = callout;
	}
	
	public static boolean isRTopicPath(String servletPath)
	{
		boolean isRTopicPath=false;
		
		if(servletPath.equals("/rtopic")) //$NON-NLS-1$
			isRTopicPath = true;
		
		return isRTopicPath;
	}
}