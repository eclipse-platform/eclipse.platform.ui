/*******************************************************************************
 *  Copyright (c) 2010, 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class CSSActivator implements BundleActivator {

	private static CSSActivator activator;

	private BundleContext context;
	private ServiceTracker<LogService, LogService> logTracker;
	private ServiceTracker<IColorAndFontProvider, IColorAndFontProvider> colorAndFontProviderTracker;

	public static CSSActivator getDefault() {
		return activator;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		activator = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		if (colorAndFontProviderTracker != null) {
			colorAndFontProviderTracker.close();
			colorAndFontProviderTracker = null;
		}
		this.context = null;
	}

	private LogService getLogger() {
		if (logTracker == null) {
			if (context == null) {
				return null;
			}
			logTracker = new ServiceTracker<LogService, LogService>(context,
					LogService.class.getName(), null);
			logTracker.open();
		}
		return logTracker.getService();
	}

	public void log(int logError, String message) {
		LogService logger = getLogger();
		if (logger != null) {
			logger.log(logError, message);
		}
	}

	public void log(int logError, String message, Throwable e) {
		LogService logger = getLogger();
		if (logger != null) {
			logger.log(logError, message, e);
		}
	}

	public IColorAndFontProvider getColorAndFontProvider() {
		if (colorAndFontProviderTracker == null) {
			if (context == null) {
				return null;
			}
			colorAndFontProviderTracker = new ServiceTracker<IColorAndFontProvider, IColorAndFontProvider>(
					context,
					IColorAndFontProvider.class.getName(), null);
			colorAndFontProviderTracker.open();
		}
		return colorAndFontProviderTracker.getService();
	}

}
