package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

import javax.servlet.*;

/**
 * Eclipse launcher
 */
public class Eclipse {
	private static final String HELP_APPLICATION = "org.eclipse.help.helpApplication";
	private static final String PI_BOOT = "org.eclipse.core.boot";
	private static final String BOOTJAR = "boot.jar";
	private static final String BOOTLOADER = "org.eclipse.core.boot.BootLoader";
	private Class bootLoader;
	private Object platformRunnable;
	private Method runMethod;
	private ServletContext context;

	/**
	 * Constructor
	 */
	public Eclipse(ServletContext context) throws ServletException {
		this.context = context;

		init();
	}

	/**
	 * Servlet destroy method shuts down Eclipse.
	 */
	public void shutdown() {
		try {
			Class bootLoader = getBootLoader();
			bootLoader.getMethod("shutdown", new Class[] {
			}).invoke(bootLoader, new Object[] {
			});
		} catch (Exception e) {
			context.log("problem shutting down");
		}

	}

	/**
	 * Gets content from the named url (this could be and eclipse defined url)
	 */
	public URLConnection openConnection(String url) throws Exception {

		//System.out.println("opening connection");
		Object[] params = new Object[1];
		params[0] = new Object[] { "openConnection", url };
		Object retObj = runMethod.invoke(platformRunnable, params);
		if (retObj != null && retObj instanceof URLConnection)
			return (URLConnection) retObj;
		else
			return null;

	}

	/**
	 * returns a platform <code>BootLoader</code> which can be used to start
	 * up and run the platform.
	 */
	private Class getBootLoader() throws Exception {
		if (bootLoader == null) {
			String installDirName =
				context.getInitParameter("ECLIPSE_HOME");

			File pluginsDir;
			if (installDirName == null || "".equals(installDirName)) {
				URL baseURL =
					new URL(
						"file",
						null,
						context.getRealPath("/").replace('\\', '/'));
				pluginsDir = new File(baseURL.getFile()).getParentFile();
			} else {
				URL baseURL =
					new URL("file", null, installDirName.replace('\\', '/'));
				pluginsDir = new File(baseURL.getFile(), "plugins");
			}
			String path = searchForBoot(pluginsDir);
			URL bootUrl = new URL("file", null, path);
			//System.out.println("URL for bootloader:" + bootUrl);

			bootLoader =
				new URLClassLoader(new URL[] { bootUrl }, null).loadClass(
					BOOTLOADER);
		}
		return bootLoader;
	}

	private synchronized void init() throws ServletException {

		try {
			// need to handle conflicts on setting url stream handlers
			initializeHandlers();

			if (context.getAttribute("platformRunnable") == null) {
				//System.out.println("getting boot loader");
				bootLoader = getBootLoader();
				//System.out.println("got bootloader: " + bootLoader);
				Method mStartup =
					bootLoader.getMethod(
						"startup",
						new Class[] {
							URL.class,
							String.class,
							String[].class });

				String work =
					((File) context
						.getAttribute("javax.servlet.context.tempdir"))
						.getAbsolutePath();

				//System.out.println("starting eclipse");
				mStartup
					.invoke(
						bootLoader,
						new Object[] { null, work, new String[] { "-noupdate" }
				});

				Method mGetRunnable =
					bootLoader.getMethod(
						"getRunnable",
						new Class[] { String.class });

				//System.out.println("get platform runnable");
				platformRunnable =
					mGetRunnable.invoke(
						bootLoader,
						new Object[] { HELP_APPLICATION });

				runMethod =
					platformRunnable.getClass().getMethod(
						"run",
						new Class[] { Object.class });
			}

		} catch (Throwable e) {
			context.log("Problem occured initializing Eclipse", e);
			//System.out.println(((InvocationTargetException)e).getCause());
			throw new ServletException(e);
		}
	}

	private void initializeHandlers() {
		// register proxy handlers
		Properties props = System.getProperties();
		String propName = "java.protocol.handler.pkgs";
		String pkgs = System.getProperty(propName);
		String proxyPkgs = "org.eclipse.help.internal.proxy.protocol";
		if (pkgs != null)
			pkgs = pkgs + "|" + proxyPkgs;
		else
			pkgs = proxyPkgs;
		props.put(propName, pkgs);
		System.setProperties(props);
	}

	/**
	* Searches for a boot directory starting in the "plugins" subdirectory
	* of the given location.  If one is found then this location is returned; 
	* otherwise an exception is thrown.
	* 
	* @return the location where boot directory was found
	* @param start the location to begin searching at
	*/
	protected String searchForBoot(File start) {
		//System.out.println("search boot in " + start);
		FileFilter filter = new FileFilter() {
			public boolean accept(File candidate) {
				//System.out.println("candidate: " + candidate);
				return candidate.isDirectory() && (candidate.getName().equals(PI_BOOT) || candidate.getName().startsWith(PI_BOOT + "_")); //$NON-NLS-1$
			}
		};
		File[] boots = start.listFiles(filter); //$NON-NLS-1$
		if (boots == null)
			throw new RuntimeException("Could not find bootstrap code. Check location of boot plug-in or specify -boot."); //$NON-NLS-1$
		String result = null;
		Object maxVersion = null;
		for (int i = 0; i < boots.length; i++) {
			String name = boots[i].getName();
			//System.out.println("try " + name);
			int index = name.indexOf('_');
			String version;
			Object currentVersion;
			if (index == -1)
				version = ""; //$NON-NLS-1$ // Note: directory with version suffix is always > than directory without version suffix
			else
				version = name.substring(index + 1);
			currentVersion = getVersionElements(version);
			if (maxVersion == null) {
				result = boots[i].getAbsolutePath();
				maxVersion = currentVersion;
			} else {
				if (compareVersion((Object[]) maxVersion,
					(Object[]) currentVersion)
					< 0) {
					result = boots[i].getAbsolutePath();
					maxVersion = currentVersion;
				}
			}
		}
		if (result == null)
			throw new RuntimeException("Could not find bootstrap code. Check location of boot plug-in or specify -boot."); //$NON-NLS-1$
		return result.replace(File.separatorChar, '/') + "/" + BOOTJAR; //$NON-NLS-1$
	}

	/**
	 * Compares version strings. 
	 * @return result of comparison, as integer;
	 * <code><0</code> if left < right;
	 * <code>0</code> if left == right;
	 * <code>>0</code> if left > right;
	 */
	private int compareVersion(Object[] left, Object[] right) {

		int result = ((Integer) left[0]).compareTo((Integer) right[0]);
		// compare major
		if (result != 0)
			return result;

		result = ((Integer) left[1]).compareTo((Integer) right[1]);
		// compare minor
		if (result != 0)
			return result;

		result = ((Integer) left[2]).compareTo((Integer) right[2]);
		// compare service
		if (result != 0)
			return result;

		return ((String) left[3]).compareTo((String) right[3]);
		// compare qualifier
	}

	/**
	 * Do a quick parse of version identifier so its elements can be correctly compared.
	 * If we are unable to parse the full version, remaining elements are initialized
	 * with suitable defaults.
	 * @return an array of size 4; first three elements are of type Integer (representing
	 * major, minor and service) and the fourth element is of type String (representing
	 * qualifier). Note, that returning anything else will cause exceptions in the caller.
	 */
	private Object[] getVersionElements(String version) {
		Object[] result = { new Integer(0), new Integer(0), new Integer(0), "" }; //$NON-NLS-1$
		StringTokenizer t = new StringTokenizer(version, "."); //$NON-NLS-1$
		String token;
		int i = 0;
		while (t.hasMoreTokens() && i < 4) {
			token = t.nextToken();
			if (i < 3) {
				// major, minor or service ... numeric values
				try {
					result[i++] = new Integer(token);
				} catch (Exception e) {
					// invalid number format - use default numbers (0) for the rest
					break;
				}
			} else {
				// qualifier ... string value
				result[i++] = token;
			}
		}
		return result;
	}

}