/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser.internal;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
/**
 * Utility class for the Web browser tooling.
 */
public class WebBrowserUtil {
	private static final String BROWSER_PACKAGE_NAME = "org.eclipse.swt.browser.Browser";
	public static Boolean isInternalBrowserOperational;
	
	private static DefaultBrowser[] defaultBrowsers;

	static class DefaultBrowser {
		String name;
		String params;
		String executable;
		String os;
		String[] locations;
		
		public DefaultBrowser(String name, String os, String executable, String params, String[] locations) {
			if (name == null)
				name = "<unknown>";
			else if (name.startsWith("%"))
				name = WebBrowserUIPlugin.getResource(name);

			this.name = name;
			this.os = os;
			this.executable = executable;
			this.params = params;
			this.locations = locations;
		}
		
		public String toString() {
			String s = "ExternalBrowserInstance: " + name + ", " + os + ", " + executable + ", " + params + ", ";
			if (locations != null) {
				int size = locations.length;
				for (int i = 0; i < size; i++) {
					s += locations[i] + ";";
				}
			}
			return s;
		}
	}

	/**
	 * WebBrowserUtil constructor comment.
	 */
	public WebBrowserUtil() {
		super();
	}

	/**
	 * Returns true if we're running on Windows.
	 *
	 * @return boolean
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("win") >= 0)
			return true;
		return false;
	}

	/**
	 * Returns true if we're running on linux.
	 *
	 * @return boolean
	 */
	public static boolean isLinux() {
		String os = System.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("lin") >= 0)
			return true;
		return false;
	}

	/**
	 * Open a dialog window.
	 *
	 * @param message java.lang.String
	 */
	public static void openError(String message) {
		Display d = Display.getCurrent();
		if (d == null)
			d = Display.getDefault();
	
		Shell shell = d.getActiveShell();
		MessageDialog.openError(shell, WebBrowserUIPlugin.getResource("%errorDialogTitle"), message);
	}
	
	/**
	 * Open a dialog window.
	 *
	 * @param message java.lang.String
	 */
	public static void openMessage(String message) {
		Display d = Display.getCurrent();
		if (d == null)
			d = Display.getDefault();
	
		Shell shell = d.getActiveShell();
		MessageDialog.openInformation(shell, WebBrowserUIPlugin.getResource("%searchingTaskName"), message);
	}

	/**
	 * Returns whether it should be possible to use the internal browser or not, based on whether or not
	 * the org.eclipse.swt.Browser class can be found/loaded. If it can it means is is supported on the platform in which
	 * this plugin is running. If not, disable the ability to use the internal browser.
	 * 
	 * This method checks to see if it can new up a new ExternalBrowserInstance. If the SWT widget can not be bound
	 * to the particular operating system it throws an SWTException. We catch that and set a boolean
	 * flag which represents whether or not we were successfully able to create a ExternalBrowserInstance instance or not.
	 * If not, don't bother adding the Internal Web ExternalBrowserInstance that uses this widget. Designed to be attemped
	 * only once and the flag set used throughout.
	 * 
	 * @return boolean
	 */
	public static boolean canUseInternalWebBrowser() {
		// if we have already figured this out, don't do it again.
		if (isInternalBrowserOperational != null)
			return isInternalBrowserOperational.booleanValue();
		
		// check for the class
		try {
			Class.forName(BROWSER_PACKAGE_NAME);
		} catch (ClassNotFoundException e) {
			isInternalBrowserOperational = new Boolean(false);
			return false;
		}
		
		// try loading it
		try {
			new Browser(new Shell(Display.getCurrent()), SWT.NONE);
			isInternalBrowserOperational = new Boolean(true);
			return true;
		} catch (Throwable t) {
			WebBrowserUIPlugin.getInstance().getLog().log(new Status(IStatus.WARNING,
				WebBrowserUIPlugin.PLUGIN_ID, 0, "Internal browser is not operational", t));
			isInternalBrowserOperational = new Boolean(false);
			return false;
		}
	}
	
	public static List getExternalBrowserPaths() {
		List paths = new ArrayList();
		Iterator iterator = BrowserManager.getInstance().getWebBrowsers().iterator();
		while (iterator.hasNext()) {
			IBrowserDescriptor wb = (IBrowserDescriptor) iterator.next();
			paths.add(wb.getLocation().toLowerCase());
		}
		return paths;
	}

	/**
	 * Add any supported EXTERNAL web browsers found after an arbitrary check in specific paths
	 */
	public static void addFoundBrowsers(List list) {
		List paths = getExternalBrowserPaths();

		String os = Platform.getOS();
		File[] roots = File.listRoots();
		int rootSize = Math.min(roots.length, 2); // just check the first two drives
		
		DefaultBrowser[] browsers = getDefaultBrowsers();
		int size = browsers.length;
		for (int i = 0; i < size; i++) {
			if (browsers[i].locations != null && browsers[i].os.indexOf(os) >= 0) {
				for (int k = 0; k < rootSize; k++) {
					int size2 = browsers[i].locations.length;
					for (int j = 0; j < size2; j++) {
						String location = browsers[i].locations[j];
						try {
							File f = new File(roots[k], location);
							if (!paths.contains(f.getAbsolutePath().toLowerCase())) {
								if (f.exists()) {
									BrowserDescriptor browser = new BrowserDescriptor();
									browser.name = browsers[i].name;
									browser.location = f.getAbsolutePath();
									browser.parameters = browsers[i].params;
									list.add(browser);
									j += size2;
								}
							}
						} catch (Exception e) {
							// ignore
						}
					}
				}
			}
		}
	}

	/**
	 * Create an external Web browser if the file matches the default (known) browsers.
	 * 
	 * @param file
	 * @return an external browser working copy
	 */
	public static IBrowserDescriptorWorkingCopy createExternalBrowser(File file) {
		if (file == null || !file.isFile())
			return null;
		
		String executable = file.getName();
		DefaultBrowser[] browsers = getDefaultBrowsers();
		int size = browsers.length;
		for (int i = 0; i < size; i++) {
			if (executable.equals(browsers[i].executable)) {
				IBrowserDescriptorWorkingCopy browser = BrowserManager.getInstance().createExternalWebBrowser();
				browser.setName(browsers[i].name);
				browser.setLocation(file.getAbsolutePath());
				browser.setParameters(browsers[i].params);
				return browser;
			}
		}
		
		return null;
	}

	protected static DefaultBrowser[] getDefaultBrowsers() {
		if (defaultBrowsers != null)
			return defaultBrowsers;
		
		Reader reader = null;
		List list = new ArrayList();
		try {
			URL url = WebBrowserUIPlugin.getInstance().getBundle().getEntry("defaultBrowsers.xml");
			URL url2 = Platform.resolve(url);
			reader = new InputStreamReader(url2.openStream());
			IMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] children = memento.getChildren("browser");
			if (children != null) {
				int size = children.length;
				for (int i = 0; i < size; i++) {
					IMemento child = children[i];
					String name = child.getString("name");
					String os = child.getString("os");
					String executable = child.getString("executable");
					String params = child.getString("params");
					List locations = new ArrayList();
					
					IMemento[] locat = child.getChildren("location");
					if (locat != null) {
						int size2 = locat.length;
						for (int j = 0; j < size2; j++)
							locations.add(locat[j].getTextData());
					}
					
					String[] loc = new String[locations.size()];
					locations.toArray(loc);
					DefaultBrowser db = new DefaultBrowser(name, os, executable, params, loc);
					Trace.trace(Trace.CONFIG, "Default " + db);
					list.add(db);
				}
			}
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error loading default browsers", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// ignore
			}
		}
		
		defaultBrowsers = new DefaultBrowser[list.size()];
		list.toArray(defaultBrowsers);
		return defaultBrowsers;
	}
}