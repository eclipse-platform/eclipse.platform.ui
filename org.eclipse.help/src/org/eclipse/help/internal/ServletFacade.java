package org.eclipse.help.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.*;
import org.eclipse.core.boot.IPlatformRunnable;
/**
 * Facade for Eclipse servlet
 */
public class ServletFacade implements IPlatformRunnable {
	private URLConnection openConnection(String urlStr) {
		try {
			if (urlStr != null && urlStr.length()>1){
				URL url = new URL(urlStr);
				return url.openConnection();
			}
		} catch (IOException e) {
			// for debugging purposes
			e.printStackTrace();
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
			if ((argsArray.length == 2) &&(argsArray[1] instanceof String))
				return openConnection((String) argsArray[1]);
		}
		return null;
	}
}