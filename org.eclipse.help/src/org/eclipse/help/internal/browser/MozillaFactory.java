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
package org.eclipse.help.internal.browser;
import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
public class MozillaFactory implements IBrowserFactory, IExecutableExtension {
	private String executable;
	private String executableName;
	private String os;
	private MozillaBrowserAdapter browserInstance = null;
	/**
	 * Constructor.
	 */
	public MozillaFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		if (!System
			.getProperty("os.name")
			.toLowerCase()
			.startsWith(os.toLowerCase())) {
			return false;
		}
		try {
			Process pr = Runtime.getRuntime().exec("which " + executable);
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			pr.waitFor();
			int ret = pr.exitValue();
			if (ret == 0) {
				return true;
			}
		} catch (InterruptedException e) {
		} catch (IOException e) {
			// launching which failed, assume executable is present
			return true;
		}
		return false;

	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		// Create single browser for all clients
		if (browserInstance == null) {
			browserInstance =
				new MozillaBrowserAdapter(executable, executableName);
		}
		return browserInstance;
	}
	/**
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(
		IConfigurationElement config,
		String propertyName,
		Object data)
		throws CoreException {
		try {
			Hashtable params = (Hashtable) data;
			executable = (String) params.get("executable");
			executableName = (String) params.get("executableName");
			os = (String) params.get("os");
		} catch (Exception e) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					HelpPlugin.PLUGIN_ID,
					IStatus.OK,
					Resources.getString("MozillaFactory.dataMissing"),
					e));
		}
	}
}
