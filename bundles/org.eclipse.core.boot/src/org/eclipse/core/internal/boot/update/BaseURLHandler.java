package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 
import java.io.*;
import java.net.*;

/*
 * BaseURLHandler. Uses base java.net.* support until the full
 * update manager plugin is initialized. After that this class simply
 * delegates to the full URLHandler.
 *
 * This class is abstract to ensure we never end up with instances.
 * Subclasses must implement the openURL() method.
 */

public abstract class BaseURLHandler {
	private static BaseURLHandler handler = null;
	protected static final int UNKNOWN_STATUS = 200; // http OK
	protected static final String UNKNOWN_MSG = "";

	public static class Response {
		private URLConnection c = null;
		
		protected Response() {}
		private Response(URLConnection c) {
			this.c = c;
		}
		public InputStream getInputStream() throws IOException {
			return c.getInputStream();
		}
		public long getContentLength() {
			return c.getContentLength();
		}
		public int getResponseCode() throws IOException {
			if (c instanceof HttpURLConnection)
				return ((HttpURLConnection)c).getResponseCode();
			else
				return UNKNOWN_STATUS; // no applicable response code
		}
		public String getResponseMessage() throws IOException {
			if (c instanceof HttpURLConnection)
				return ((HttpURLConnection)c).getResponseMessage();
			else
				return UNKNOWN_MSG; // no applicable response message
		}
	}

public static BaseURLHandler.Response open(URL url) throws IOException {

	// full handler is configured
	if (handler!=null)
		return handler.openURL(url);

	// don't have full handler ... use base Java support
	return new BaseURLHandler.Response(url.openConnection());
}
protected abstract BaseURLHandler.Response openURL(URL url) throws IOException;
public static void setURLHandler(BaseURLHandler h) {
	if (h==null)
		return;
	if (handler == null)
		handler = h;
}
}
