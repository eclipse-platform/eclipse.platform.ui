/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.internal.util.*;
/**
 * URL that performs live help action.
 */
public class LiveHelpURL extends HelpURL {
	/**
	 * LiveHelpURL constructor.
	 * @param url java.lang.String
	 */
	public LiveHelpURL(String url) {
		super(url, "");
		int index = url.indexOf("?");
		if (index > -1) {
			if (url.length() > index + 1) {
				String query = url.substring(index + 1);
				this.query = new StringBuffer(query);
				parseQuery(query);
			}
			super.url = url.substring(0, index);
		}
	}
	/** Returns the path prefix that identifies the URL. */
	public static String getPrefix() {
		return "livhelp";
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		if (arguments == null)
			return getErrorStream(Resources.getString("E030"));
		String pluginID = (String) arguments.get("pluginID");
		if (pluginID == null)
			return getErrorStream(Resources.getString("E030"));
		String className = (String) arguments.get("class");
		if (className == null)
			return getErrorStream(Resources.getString("E031"));
		String arg = URLCoder.decode((String) arguments.get("arg"));
		Plugin plugin = Platform.getPlugin(pluginID);
		if (plugin == null)
			return getErrorStream(Resources.getString("E032"));
		ClassLoader loader = plugin.getDescriptor().getPluginClassLoader();
		try {
			Class c = loader.loadClass(className);
			Object o = c.newInstance();
			if (o instanceof ILiveHelpAction) {
				ILiveHelpAction helpExt = (ILiveHelpAction) o;
				if (arg != null)
					helpExt.setInitializationString(arg);
				Thread runnableLiveHelp = new Thread(helpExt);
				runnableLiveHelp.setDaemon(true);
				runnableLiveHelp.start();
			}
			return new ByteArrayInputStream("Done it".getBytes());
		} catch (ThreadDeath td) {
			throw td;
		} catch (Exception e) {
			return getErrorStream(e.toString());
		}
	}
	private InputStream getErrorStream(String msg) {
		String message = Resources.getString("E029", msg);
		try {
			return new ByteArrayInputStream(message.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException uee) {
			return new ByteArrayInputStream(message.getBytes());
		}
	}
}