package org.eclipse.help.internal.remote;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
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
	private void fillContent(String url, OutputStream out) {
		try {
			Logger.logInfo("url =" + url);
			// Is it search url?
			if (url != null && url.length() != 0) {
				if (url.startsWith("/search")) {
					if (url.length() > "/search".length() + 1) {
						URL searchURL = new URL(url.substring("/search".length() + 1));
						InputStream stream = searchURL.openStream();
						byte[] buf = new byte[8192];
						int n = stream.read(buf);
						while (n > -1) {
							if (n > 0)
								out.write(buf, 0, n);
							n = stream.read(buf);
						}
					}
					return;
				}
			}
			HelpURL helpURL = HelpURLFactory.createHelpURL(url);
			Logger.logInfo("helpURL: " + helpURL.getClass());
			InputStream stream = helpURL.openStream();
			if (stream != null)
				HelpContentManager.fillInResponse(helpURL, stream, out);
			else if (!helpURL.toString().endsWith(".class")) {
				// this is needed when the class loader
				// wants to load a property file and queries
				// for doc_en_us.class
				HelpURL errorURL =
					HelpURLFactory.createHelpURL(
						"/org.eclipse.help/" + Resources.getString("notopic.html"));
				stream = errorURL.openStream();
				HelpContentManager.fillInResponse(errorURL, stream, out);
			}
		} catch (IOException e) {
			Logger.logError("", e);
		}
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
		if (command == "fillContent") { //(URL, OutpuString)
			if (argsArray.length == 3)
				if (argsArray[1] instanceof String && argsArray[2] instanceof OutputStream)
					fillContent((String) argsArray[1], (OutputStream) argsArray[2]);
		}
		return this;
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