/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Shawn Minto, patch for Bug 247731
 *******************************************************************************/
package org.eclipse.ui.internal.browser.browsers;

import org.eclipse.ui.browser.BrowserFactory;
import org.eclipse.ui.browser.IWebBrowser;

public class MozillaFactory extends BrowserFactory {
	private MozillaBrowser browserInstance = null;

	/**
	 * Constructor.
	 */
	public MozillaFactory() {
		super();
	}

	/*
	 * @see BrowserFactory#isAvailable()
	 */
	/*public boolean isAvailable() {
		try {
			Process pr = Runtime.getRuntime().exec("which " + executable); //$NON-NLS-1$
			StreamConsumer outputs = new StreamConsumer(pr.getInputStream());
			(outputs).start();
			StreamConsumer errors = new StreamConsumer(pr.getErrorStream());
			(errors).start();
			pr.waitFor();

			int ret = pr.exitValue();
			if (ret == 0)
				return !errorsInOutput(outputs, errors);
			return false;
		} catch (InterruptedException e) {
			return false;
		} catch (IOException e) {
			// launching which failed, assume browser executable is present
			return true;
		}
	}*/

	/*
	 * On some OSes 0 is always returned by "which" command it is necessary to
	 * examine ouput to find out failure.
	 *
	 * @return @throws
	 *         InterruptedException
	 */
	/*private boolean errorsInOutput(StreamConsumer outputs, StreamConsumer errors) {
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
	}*/

	@Override
	public IWebBrowser createBrowser(String id, String location, String parameters) {
		// Create single browser for all clients
		if (browserInstance == null
				|| !browserInstance.getExecutable().equals(location)
				|| !browserInstance.getParameters().equals(parameters)) {
			browserInstance = new MozillaBrowser(id, location, parameters);
		}
		return browserInstance;
	}
}