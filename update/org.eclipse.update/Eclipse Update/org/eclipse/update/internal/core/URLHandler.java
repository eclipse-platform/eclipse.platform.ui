package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.boot.update.*;
import org.eclipse.webdav.http.client.*;
import org.eclipse.webdav.client.WebDAVFactory;
import org.eclipse.webdav.Context;
import java.io.*;
import java.net.*;

public class URLHandler extends BaseURLHandler {

	private static HttpClient http = null;
	private static WebDAVFactory factory = null;
	
	private static final String HTTP = "http";
	
	static class Response extends BaseURLHandler.Response {
		private org.eclipse.webdav.http.client.Response r = null;
		private Response(org.eclipse.webdav.http.client.Response r) {
			this.r = r;
		}
		public InputStream getInputStream() throws IOException {
			return r.getInputStream();
		}
		public long getContentLength() {
			return r.getContentLength();
		}
		public int getResponseCode() {
			return r.getStatusCode();
		}
		public String getResponseMessage() {
			return r.getStatusMessage();
		}
	}

private URLHandler() {}
public static BaseURLHandler.Response open(URL url) throws IOException {

	if (url==null) return null;

	if (!url.getProtocol().equals(HTTP)) {
		URLConnection c = url.openConnection();
		Context ctx = factory.newContext();
		int i = 1;
		String name = null;
		while ((name = c.getHeaderFieldKey(i)) != null) {
			ctx.put(name.toLowerCase(), c.getHeaderField(i));
			i++;
		}	
		InputStream is = c.getInputStream();	
		return new Response(new org.eclipse.webdav.http.client.Response(UNKNOWN_STATUS,UNKNOWN_MSG,ctx,is));
	}
	else {
		if (http==null) throw new IllegalStateException(UpdateManagerStrings.getString("S_HTTP_client_not_set"));	
		Request request = new Request("GET", url, null);
		return new Response(http.invoke(request));
	}	
}
protected BaseURLHandler.Response openURL(URL url) throws IOException {
	return URLHandler.open(url);
}
public static void setHttpClient(HttpClient c) {

	if (http==null) {
		http = c;
		factory = new WebDAVFactory();

		// setup this handler as the handler for boot portion of update manager
		BaseURLHandler.setURLHandler(new URLHandler());
	}
}
}
