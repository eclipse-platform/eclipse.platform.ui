package org.eclipse.help.internal.remote;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.help.internal.server.*;
import java.io.*;

import org.eclipse.help.internal.util.*;

import java.util.*;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

/**
 * Launcher for standalone help system
 */
public class ServletFacade implements IPlatformRunnable {

	final static String errorString =
		"<html><body>The topic that you have requested is not available. The link may be wrong, or you may not have the corresponding product feature installed. This online help only includes documentation for features that are installed.</body></html>";
	/**
	 * StandaloneHelpSystem constructor comment.
	 */
	public ServletFacade() {
		super();

		HelpSystem.setPlugin(HelpPlugin.getDefault());

		// Tell the help system this is a remote install
		HelpSystem.setInstall(2);

		// Initialize configuration
		try {
			readINIFile();
			HelpSystem.startup();
		} catch (CoreException e) {
		}
	}
	public void fillContent(String url, OutputStream out) {
		try {
			Logger.logInfo("url =" + url);
			HelpURL helpURL = HelpURLFactory.createHelpURL(url);
			Logger.logInfo("helpURL: " + helpURL.getClass());
			InputStream stream = helpURL.openStream();
			if (stream != null)
				HelpContentManager.fillInResponse(helpURL, stream, out);
			else
				if (!helpURL.toString().endsWith(".class")) {
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
			Logger.logError(e.getMessage(), e);
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

			int debug_level = Integer.getInteger(ini.getProperty("log_level")).intValue();
			HelpSystem.setDebugLevel(debug_level);

			is.close();
		} catch (Exception e) {
			// pass the error to System.err AND to the internal platform log.
			System.err.println(Resources.getString("E004", iniPathURL.toString()));

			HelpPlugin.getDefault().getLog().log(
				new Status(
					IStatus.ERROR,
					descriptor.getUniqueIdentifier(),
					0,
					Resources.getString("E004", iniPathURL.toString()),
					e));
		}

	}
	public Object run(Object args) {
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
