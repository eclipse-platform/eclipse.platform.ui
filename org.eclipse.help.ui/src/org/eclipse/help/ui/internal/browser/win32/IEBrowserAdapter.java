/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import java.io.*;
import java.net.*;
import java.util.*;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.util.StreamConsumer;
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
	String[] cmd;
	/**
	 * Adapter constructor.
	 */
	public IEBrowserAdapter() {
		try {
			// Create command string for launching the process
			String executable = "java";
			if (BootLoader.OS_WIN32.equals(BootLoader.getOS()))
				executable += "w.exe";
			cmd =
				new String[] {
					System.getProperty("java.home")
						+ File.separator
						+ "bin"
						+ File.separator
						+ executable,
					"-D"
						+ IEHost.SYS_PROPERTY_INSTALLURL
						+ "="
						+ Platform.resolve(
							WorkbenchHelpPlugin.getDefault().getDescriptor().getInstallURL()),
					"-D"
						+ IEHost.SYS_PROPERTY_STATELOCATION
						+ "="
						+ WorkbenchHelpPlugin.getDefault().getStateLocation().toString(),
					"-Djava.library.path="
						+ getPath(PLUGIN_ID_SWT)
						+ System.getProperty("java.library.path"),
					"-cp",
					getClassPath(PLUGIN_ID_HELPUI),
					IE_CLASS };
		} catch (IOException ioe) {
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
			pr = Runtime.getRuntime().exec(cmd);
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			commandWriter = new PrintWriter(pr.getOutputStream(), true);
		} catch (IOException e) {
			pr = null;
		}
		launched = true;
		if (pr == null)
			return;
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
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
		if (cmd == null) // did not initialize propertly
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
		} catch (Exception e) {
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
		URL installURL = Platform.getPlugin(pluginID).getDescriptor().getInstallURL();
		try {
			installURL = Platform.resolve(installURL);
		} catch (IOException ioe) {
			return "";
		}
		File installFile = new File(installURL.getFile());
		String[] variants = buildLibraryVariants();
		String path = "";
		for (int v = 0; v < variants.length; v++) {
			path += new File(installFile, variants[v]).getAbsolutePath()
				+ File.pathSeparator;
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
}