package org.eclipse.help.internal.remote;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.*;
import org.eclipse.core.boot.IPlatformRunnable;
/**
 * Launcher for standalone help system
 */
public class ServletFacade implements IPlatformRunnable {
	private URLConnection openConnection(String urlStr) {
		try {
			if (urlStr != null && urlStr.length() > 1 && urlStr.charAt(0) == '/') {
				int pathIx = urlStr.indexOf('/', 1);
				if (pathIx > -1) {
					String protocol = urlStr.substring(1, pathIx);
					URL url = new URL(protocol + ':' + urlStr.substring(pathIx));
					return url.openConnection();
				}
			}
		} catch (IOException e) {
		}
		return null;
	}
	/**
	 * @param args array of objects
	 *  first is String command
	 *  rest are command parameters
	 */
	public Object run(Object args) {
		if (args == null || !(args instanceof Object[]))
			return null;
		Object[] argsArray = (Object[]) args;
		if (argsArray.length < 1
			|| !(argsArray[0] instanceof String)
			|| argsArray[0] == null)
			return null;
		String command = (String) argsArray[0];
		if (command == "openConnection") { //(String url)
			if (argsArray.length == 2)
				if (argsArray[1] instanceof String)
					return openConnection((String) argsArray[1]);
			return null;
		}
		return null;
	}
}