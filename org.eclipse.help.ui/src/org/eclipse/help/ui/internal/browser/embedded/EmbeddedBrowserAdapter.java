/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser.embedded;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.ui.internal.*;

/**
 * Web browser.  Win32 implmentation of IBrowser interface.
 * Launches Browser as an external process.
 */
public class EmbeddedBrowserAdapter implements IBrowser, Runnable {
	private static final String PLUGIN_ID_SWT = "org.eclipse.swt";
	private static final String IE_CLASS = EmbeddedBrowserHost.class.getName();
	PrintWriter commandWriter;
	boolean launched = false;
	private static String[] cmdarray;
	String cmd;
	String installURL = "";
	EmbeddedBrowserHost ieHost;
	/**
	 * Adapter constructor.
	 */
	public EmbeddedBrowserAdapter() {
		if (cmdarray != null) {
			// already constructed process launching command
			// for this platform installation
			return;
		}
		// Create command string for launching the process
		String vm = System.getProperty("java.vm.name");
		String executable = "J9".equals(vm) ? "j9" : "java";
		if (BootLoader.OS_WIN32.equals(BootLoader.getOS()))
			executable += "w.exe";

		String javaInstallDir =
			System.getProperty("java.home") + File.separator + "bin";
		String program = javaInstallDir + File.separator + executable;

		String locale = BootLoader.getNL();
		if (locale == null)
			locale = Locale.getDefault().toString();

		try {
			URL instURL =
				Platform.resolve(
					HelpUIPlugin
						.getDefault()
						.getDescriptor()
						.getInstallURL());
			installURL = instURL.toExternalForm();
		} catch (IOException ioe) {
			HelpUIPlugin.logError(
				HelpUIResources.getString("WE022"),
				ioe);
			return;
		}
		String libraryPath =
			javaInstallDir
				+ ";"
				+ getPath(PLUGIN_ID_SWT)
				+ System.getProperty("java.library.path");
		libraryPath = TString.change(libraryPath, "\"", "");
		if (libraryPath.charAt(libraryPath.length() - 1) != ';') {
			libraryPath += ";";
		}
		String classPath = getClassPath(HelpUIPlugin.PLUGIN_ID);
		String stateLocation =
			HelpUIPlugin.getDefault().getStateLocation().toString();
		String imageURL = getProductImageURL();
		if (imageURL == null)
			imageURL = "";
		if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER) {
			cmdarray =
				new String[] {
					program,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_LOCALE + "=" + locale,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_INSTALLURL + "=" + installURL,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_PRODUCTIMAGEURL + "=" + imageURL,
					"-D"
						+ EmbeddedBrowserHost.SYS_PROPERTY_PRODUCTNAME
						+ "="
						+ getWindowTitle(),
					"-D"
						+ EmbeddedBrowserHost.SYS_PROPERTY_STATELOCATION
						+ "="
						+ stateLocation,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_DEBUG + "=true",
					"-Djava.library.path=" + libraryPath,
					"-cp",
					classPath,
					IE_CLASS };
		} else {
			cmdarray =
				new String[] {
					program,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_LOCALE + "=" + locale,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_INSTALLURL + "=" + installURL,
					"-D" + EmbeddedBrowserHost.SYS_PROPERTY_PRODUCTIMAGEURL + "=" + imageURL,
					"-D"
						+ EmbeddedBrowserHost.SYS_PROPERTY_PRODUCTNAME
						+ "="
						+ getWindowTitle(),
					"-D"
						+ EmbeddedBrowserHost.SYS_PROPERTY_STATELOCATION
						+ "="
						+ stateLocation,
					"-Djava.library.path=" + libraryPath,
					"-cp",
					classPath,
					IE_CLASS };
		}
		cmd = "\"" + cmdarray[0] + "\"";
		for (int i = 1; i < cmdarray.length; i++)
			cmd += " \"" + cmdarray[i] + "\"";
		if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER) {
			System.out.println("IEBrowserAdapter launch command is: " + cmd);
		}
	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
		sendCommand(EmbeddedBrowserHost.CMD_CLOSE);
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		sendCommand(EmbeddedBrowserHost.CMD_DISPLAY_URL + " " + url);
	}
	/*
	 * @see IBrowser#isCloseSupported()
	 */
	public boolean isCloseSupported() {
		return true;
	}
	/*
	 * @see IBrowser#isSetLocationSupported()
	 */
	public boolean isSetLocationSupported() {
		return true;
	}
	/*
	 * @see IBrowser#isSetSizeSupported()
	 */
	public boolean isSetSizeSupported() {
		return true;
	}
	/*
	 * @see IBrowser#setLocation(int, int)
	 */
	public void setLocation(int x, int y) {
		sendCommand(EmbeddedBrowserHost.CMD_SET_LOCATION + " " + x + " " + y);
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		sendCommand(EmbeddedBrowserHost.CMD_SET_SIZE + " " + width + " " + height);
	}
	/**
	 * Launches IE as external process.
	 * initializes commandWriter to this process standard input.
	 */
	public void run() {
		Process pr;
		try {
			pr = Runtime.getRuntime().exec(cmdarray);
		} catch (IOException e) {
			HelpUIPlugin.logError(
				HelpUIResources.getString("WE024", cmd),
				e);
			pr = null;
			launched = true;
			return;
		}
		Thread ieOutConsumer = new StreamConsumer(pr.getInputStream());
		ieOutConsumer.setName("Internet Explorer adapter output reader");
		ieOutConsumer.start();
		Thread ieErrConsumer = new StreamConsumer(pr.getErrorStream());
		ieErrConsumer.setName("Internet Explorer adapter error reader");
		ieErrConsumer.start();
		commandWriter = new PrintWriter(pr.getOutputStream(), true);
		launched = true;
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			HelpUIPlugin.logError(
				HelpUIResources.getString("WE023"),
				e);
		}
		if (commandWriter != null) {
			PrintWriter w = commandWriter;
			commandWriter = null;
			w.close();
		}
	}
	/**
	 * Writes specified command to IE standard input
	 */
	private void sendCommand(String command) {
		if (cmdarray == null) // did not initialize propertly
			return;
		if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
			if (ieHost == null || ieHost.isDisposed()) {
				ieHost = new EmbeddedBrowserHost(installURL, getProductImageURL());
			}
			if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER) {
				System.out.println(
					"Sending the following command to the IE browser (in process): "
						+ command);
			}
			ieHost.executeCommand(command);
			return;

		}
		if (commandWriter == null) {
			if (EmbeddedBrowserHost.CMD_CLOSE.equals(command))
				// do not start browser just to close it again
				return;
			new Thread(this).start();
			while (!launched) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					launched = false;
					return;
				}
			}
			launched = false;
		}
		try {
			commandWriter.println(command);
			if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER) {
				System.out.println(
					"Sending the following command to the IE browser process: "
						+ command);
			}
		} catch (Exception e) {
			HelpUIPlugin.logWarning(
				HelpUIResources.getString("WW003"));
		}
	}
	/** 
	 * Calculates classpath for a specified plugin
	 */
	private String getClassPath(String pluginID) {
		Collection pluginIDs = new HashSet();
		addRequiredPluginIDs(pluginID, pluginIDs);
		pluginIDs.add(pluginID);
		Collection cpaths = new HashSet();
		for (Iterator it = pluginIDs.iterator(); it.hasNext();) {
			URLClassLoader cl =
				(URLClassLoader) Platform
					.getPlugin((String) it.next())
					.getDescriptor()
					.getPluginClassLoader();
			URL urls[] = cl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				cpaths.add(urls[i].getFile());
			}
		}
		StringBuffer classPath = new StringBuffer(4096);
		// class path ends up around 3k long
		for (Iterator i = cpaths.iterator(); i.hasNext();) {
			String cp = (String) i.next();
			if (cp.length() > 0) {
				classPath.append(cp);
				classPath.append(File.pathSeparator);
			}
		}
		return classPath.toString();
	}
	/**
	 * Obtains IDs of plugins required by given plugin
	 */
	private void addRequiredPluginIDs(String pluginID, Collection IDs) {
		IPluginPrerequisite[] preqs =
			Platform
				.getPlugin(pluginID)
				.getDescriptor()
				.getPluginPrerequisites();
		for (int i = 0; i < preqs.length; i++) {
			IDs.add(preqs[i].getUniqueIdentifier());
			addRequiredPluginIDs(preqs[i].getUniqueIdentifier(), IDs);
		}
	}
	/** 
	 * Calculates library path for a specified plugin
	 */
	private String getPath(String pluginID) {
		String path = "";
		Collection installURLs = new ArrayList();
		// get Install URL of plugin
		IPluginDescriptor pluginDescriptor =
			Platform.getPlugin(pluginID).getDescriptor();
		URL pluginInstallURL = pluginDescriptor.getInstallURL();
		installURLs.add(pluginInstallURL);
		// get Install URL of fragments
		if (pluginDescriptor instanceof PluginDescriptorModel) {
			PluginFragmentModel[] fragmentModels =
				((PluginDescriptorModel) pluginDescriptor).getFragments();
			if (fragmentModels != null) {
				for (int f = 0; f < fragmentModels.length; f++) {
					String location = fragmentModels[f].getLocation();
					try {
						URL fragInstallURL = new URL(location);
						installURLs.add(fragInstallURL);
					} catch (MalformedURLException mue) {
					}
				}
			}
		}
		for (Iterator it = installURLs.iterator(); it.hasNext();) {
			URL installURL = (URL) it.next();
			try {
				installURL = Platform.resolve(installURL);
			} catch (IOException ioe) {
				continue;
			}
			File installFile = new File(installURL.getFile());
			String[] variants = buildLibraryVariants();
			for (int v = 0; v < variants.length; v++) {
				path += new File(installFile, variants[v]).getAbsolutePath()
					+ File.pathSeparator;
			}
		}
		return path;
	}
	/**
	 * Copied and modified from DelegatingURLClassLoader
	 */
	private static String[] buildLibraryVariants() {
		ArrayList result = new ArrayList();
		result.add("ws/" + BootLoader.getWS() + "/");
		result.add(
			"os/"
				+ BootLoader.getOS()
				+ "/"
				+ BootLoader.getOSArch()
				+ "/");
		result.add("os/" + BootLoader.getOS() + "/");
		String nl = BootLoader.getNL();
		nl = nl.replace('_', '/');
		while (nl.length() > 0) {
			result.add("nl/" + nl + "/");
			int i = nl.lastIndexOf('/');
			nl = (i < 0) ? "" : nl.substring(0, i);
		}
		result.add("");
		return (String[]) result.toArray(new String[result.size()]);
	}
	/**
	 * Obtains URL to product image
	 * @return URL as String or null
	 */
	private String getProductImageURL() {
		IPlatformConfiguration c = BootLoader.getCurrentPlatformConfiguration();
		String primaryFeatureId = c.getPrimaryFeatureIdentifier();
		if (primaryFeatureId == null)
			return null; // no primary feature installed
		IPluginDescriptor pfd =
			Platform.getPluginRegistry().getPluginDescriptor(primaryFeatureId);
		if (pfd == null)
			return null; // no primary feature installed

		URL aboutURL = pfd.find(new Path("about.ini"));
		if (aboutURL == null)
			return null;
		try {
			aboutURL = Platform.resolve(aboutURL);
			Properties aboutProps = new Properties();
			aboutProps.load(aboutURL.openStream());
			String windowIconPathStr = (String) aboutProps.get("windowImage");
			if (windowIconPathStr == null)
				return null;
			IPath windowIconPath = new Path(windowIconPathStr);
			URL windowIconURL;
			// find icon under pluginID/nl/ directory
			Map override = new HashMap(1);
			override.put("$nl$", BootLoader.getNL());
			windowIconURL = pfd.find(windowIconPath, override);
			if (windowIconURL == null)
				return null;
			windowIconURL = Platform.resolve(windowIconURL);
			return windowIconURL.toString();
		} catch (IOException ioe) {
			HelpUIPlugin.logError(
				HelpUIResources.getString("WE029"),
				ioe);
		}
		return null;
	}

	public String getWindowTitle() {
		if ("true"
			.equalsIgnoreCase(
				HelpBasePlugin.getDefault().getPluginPreferences().getString(
					"windowTitlePrefix"))) {
			return HelpUIResources.getString(
				"browserTitle",
				BaseHelpSystem.getProductName());
		} else {
			return BaseHelpSystem.getProductName();
		}
	}
}
