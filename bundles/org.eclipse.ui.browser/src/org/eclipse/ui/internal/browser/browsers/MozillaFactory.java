/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser.browsers;
import java.io.*;

import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.internal.browser.provisional.IBrowserFactory;

public class MozillaFactory implements IBrowserFactory {
	private String executable;
	private String executableName;
	private String osList;
	private MozillaBrowser browserInstance = null;
	
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
		try {
			Process pr = Runtime.getRuntime().exec("which " + executable); //$NON-NLS-1$
			StreamConsumer outputs = new StreamConsumer(pr.getInputStream());
			(outputs).start();
			StreamConsumer errors = new StreamConsumer(pr.getErrorStream());
			(errors).start();
			pr.waitFor();
			int ret = pr.exitValue();
			if (ret == 0) {
				return !errorsInOutput(outputs, errors);
			} else {
				return false;
			}
		} catch (InterruptedException e) {
			return false;
		} catch (IOException e) {
			// launching which failed, assume browser executable is present
			return true;
		}
	}
	
	/**
	 * On some OSes 0 is always returned by "which" command it is necessary to
	 * examine ouput to find out failure.
	 * 
	 * @param outputs
	 * @param errors
	 * @return @throws
	 *         InterruptedException
	 */
	private boolean errorsInOutput(StreamConsumer outputs, StreamConsumer errors) {
		try {
			outputs.join(1000);
			if (outputs.getLastLine() != null
				&& outputs.getLastLine()
					.indexOf("no " + executable + " in") //$NON-NLS-1$ //$NON-NLS-2$
					>= 0) {
				return true;
			}
			errors.join(1000);
			if (errors.getLastLine() != null
					&& errors.getLastLine().indexOf("no " + executable + " in") //$NON-NLS-1$ //$NON-NLS-2$
					>= 0) {
				return true;
			}
		} catch (InterruptedException ie) {
			// ignore
		}
		return false;
	}
	
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IWebBrowser createBrowser(String id, String location, String parameters) {
		// Create single browser for all clients
		if (browserInstance == null) {
			browserInstance = new MozillaBrowser(id, executable, executableName);
		}
		return browserInstance;
	}
}