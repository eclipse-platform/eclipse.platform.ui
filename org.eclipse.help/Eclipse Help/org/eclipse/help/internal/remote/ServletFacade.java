package org.eclipse.help.internal.remote;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.*;
import java.util.Properties;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.server.*;
import org.eclipse.help.internal.util.*;
/**
 * Launcher for standalone help system
 */
public class ServletFacade implements IPlatformRunnable {
	/**
	 * StandaloneHelpSystem constructor comment.
	 */
	public ServletFacade() {
		super();
		// Tell the help system this is a remote install
		HelpSystem.setInstall(2);
		// Initialize configuration
		try {
			readINIFile();
			HelpSystem.startup();
		} catch (CoreException e) {
		}
	}
	private URLConnection openConnection(String urlStr) {
		try {
			Logger.logInfo("url =" + urlStr);
			if (urlStr != null && urlStr.length() > 1 && urlStr.charAt(0)=='/') {
				int pathIx = urlStr.indexOf('/', 1);
				if (pathIx>-1) {
					String protocol=urlStr.substring(1,pathIx);
					URL url = new URL(protocol+':' + urlStr.substring(pathIx));
					return url.openConnection();
				}
			}			
		} catch (IOException e) {
			Logger.logError("", e);
		}
		return null;
	}
	/**
	 * Read the help ini file.
	 * Unlike the other plugins, this only cares about the 
	 * log level.
	 * The plugin should call this if needed...
	 */
	public void readINIFile() throws CoreException {
		URL iniPathURL = null;
		IPluginDescriptor descriptor = null;
		try {
			// get the ini file URL   
			descriptor = HelpPlugin.getDefault().getDescriptor();
			URL installURL = descriptor.getInstallURL();
			iniPathURL = new URL(installURL, "remote.ini");
			// now load the config properties
			Properties ini = new Properties();
			InputStream is = iniPathURL.openStream();
			ini.load(is);
			int debug_level = new Integer(ini.getProperty("log_level")).intValue();
			HelpSystem.setDebugLevel(debug_level);
			is.close();
		} catch (Exception e) {
			HelpPlugin.getDefault().getLog().log(
				new Status(
					IStatus.ERROR,
					descriptor.getUniqueIdentifier(),
					0,
					Resources.getString("E004", iniPathURL.toString()),
					e));
		}
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
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		HelpSystem.shutdown();
	}
}