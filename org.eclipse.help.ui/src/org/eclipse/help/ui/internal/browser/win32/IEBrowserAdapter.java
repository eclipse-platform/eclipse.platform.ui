/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.help.ui.browser.IBrowser;
/**
 * Web browser.  Win32 implmentation of IBrowser interface.
 * Launches Browser as an external process.
 */
public class IEBrowserAdapter implements IBrowser, Runnable {
	private static final String PLUGIN_ID_HELPUI = "org.eclipse.help.ui";
	private static final String PLUGIN_ID_SWT = "org.eclipse.swt";
	private static final String IE_CLASS =
		"org.eclipse.help.ui.internal.browser.win32.IEHost";
	PrintWriter commandWriter;
	boolean launched = false;
	String[] cmdarray;
	String cmd;
	/**
	 * Adapter constructor.
	 */
	public IEBrowserAdapter() {
		// Create command string for launching the process
		String executable = "java";
		if (BootLoader.OS_WIN32.equals(BootLoader.getOS()))
			executable += "w.exe";
		String program =
			System.getProperty("java.home")
				+ File.separator
				+ "bin"
				+ File.separator
				+ executable;
		String installURL = "";
		try {
			URL instURL =
				Platform.resolve(
					WorkbenchHelpPlugin.getDefault().getDescriptor().getInstallURL());
			installURL = instURL.toExternalForm();
		} catch (IOException ioe) {
			Logger.logError(WorkbenchResources.getString("WE022"), ioe);
			return;
		}
		String libraryPath =
			getPath(PLUGIN_ID_SWT) + System.getProperty("java.library.path");
		libraryPath = TString.change(libraryPath, "\"", "");
		if (libraryPath.charAt(libraryPath.length() - 1) != ';') {
			libraryPath += ";";
		}
		String classPath = getClassPath(PLUGIN_ID_HELPUI);
		String stateLocation =
			WorkbenchHelpPlugin.getDefault().getStateLocation().toString();
		String imageURL = getProductImageURL();
		if (imageURL == null)
			imageURL = "";
		cmdarray =
			new String[] {
				program,
				"-D" + IEHost.SYS_PROPERTY_INSTALLURL + "=" + installURL,
				"-D" + IEHost.SYS_PROPERTY_PRODUCTIMAGEURL + "=" + imageURL,
				"-D" + IEHost.SYS_PROPERTY_PRODUCTNAME + "=" + getProductName(),
				"-D" + IEHost.SYS_PROPERTY_STATELOCATION + "=" + stateLocation,
				"-Djava.library.path=" + libraryPath,
				"-cp",
				classPath,
				IE_CLASS };
		cmd = cmdarray[0];
		for (int i = 1; i < cmdarray.length; i++)
			cmd += " " + cmdarray[i];
		if (Logger.LOG_DEBUG == Logger.getDebugLevel()) {
			Logger.logInfo("IEBrowserAdapter launch command is: " + cmd);
		}

	}
	/*
	 * @see IBrowser#close()
	 */
	public void close() {
		sendCommand(IEHost.CMD_CLOSE);
	}
	/*
	 * @see IBrowser#displayURL(String)
	 */
	public void displayURL(String url) {
		sendCommand(IEHost.CMD_DISPLAY_URL + " " + url);
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
		sendCommand(IEHost.CMD_SET_LOCATION + " " + x + " " + y);
	}
	/*
	 * @see IBrowser#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		sendCommand(IEHost.CMD_SET_SIZE + " " + width + " " + height);
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
			Logger.logError(WorkbenchResources.getString("WE024", cmd), e);
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
			Logger.logError(WorkbenchResources.getString("WE023"), e);
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
		if (commandWriter == null) {
			if (IEHost.CMD_CLOSE.equals(command))
				// do not start browser just to close it again
				return;
			new Thread(this).start();
			while (!launched) {
				try {
					Thread.currentThread().sleep(50);
				} catch (InterruptedException ie) {
					launched = false;
					return;
				}
			}
			launched = false;
		}
		try {
			commandWriter.println(command);
			Logger.logInfo("Sending the following command to the IE browser: " + command);
		} catch (Exception e) {
			Logger.logWarning(WorkbenchResources.getString("WW003"));
		}
	}
	/** 
	 * Calculates classpath for a specified plugin
	 */
	private String getClassPath(String pluginID) {
		Collection pluginIDs = getRequiredPluginIDs(pluginID);
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
				cpaths.add(new File(urls[i].getFile()).toString());
			}
		}
		String classPath = "";
		for (Iterator i = cpaths.iterator(); i.hasNext();) {
			String cp = (String) i.next();
			if (cp.length() > 0)
				classPath += cp + File.pathSeparator;
		}
		return classPath;
	}
	/**
	 * Obtains IDs of plugins required by given plugin
	 */
	private Collection getRequiredPluginIDs(String pluginID) {
		Collection IDs = new ArrayList();
		IPluginPrerequisite[] preqs =
			Platform.getPlugin(pluginID).getDescriptor().getPluginPrerequisites();
		for (int i = 0; i < preqs.length; i++) {
			IDs.add(preqs[i].getUniqueIdentifier());
			IDs.addAll(getRequiredPluginIDs(preqs[i].getUniqueIdentifier()));
		}
		return IDs;
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
			"os/" + BootLoader.getOS() + "/" + System.getProperty("os.arch") + "/");
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
		IPluginDescriptor pfd =
			Platform.getPluginRegistry().getPluginDescriptor(primaryFeatureId);
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
			override.put("$nl$", Locale.getDefault().toString());
			windowIconURL = pfd.find(windowIconPath, override);
			if (windowIconURL == null)
				return null;
			windowIconURL = Platform.resolve(windowIconURL);
			return windowIconURL.toString();
		} catch (IOException ioe) {
			Logger.logError(WorkbenchResources.getString("WE029"), ioe);
		}
		return null;
	}
	/**
	 * Obtains Name of the product
	 * @return String
	 */
	private String getProductName() {
		IPlatformConfiguration c = BootLoader.getCurrentPlatformConfiguration();
		String primaryFeatureId = c.getPrimaryFeatureIdentifier();
		IPluginDescriptor pfd =
			Platform.getPluginRegistry().getPluginDescriptor(primaryFeatureId);
		return pfd.getLabel();
	}
}