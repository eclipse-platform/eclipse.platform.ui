package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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

	public static class Response {
		private URLConnection c = null;
		protected Response() {}
		private Response(URLConnection c) {
			this.c = c;
		}
		public InputStream getInputStream() throws IOException {
			return c.getInputStream();
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
