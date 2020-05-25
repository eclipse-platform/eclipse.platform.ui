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
import org.osgi.util.tracker.ServiceTracker;

public class CSSActivator implements BundleActivator {

	private static CSSActivator activator;

	private BundleContext context;
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
		if (colorAndFontProviderTracker != null) {
			colorAndFontProviderTracker.close();
			colorAndFontProviderTracker = null;
		}
		this.context = null;
	}

	public IColorAndFontProvider getColorAndFontProvider() {
		if (colorAndFontProviderTracker == null) {
			if (context == null) {
				return null;
			}
			colorAndFontProviderTracker = new ServiceTracker<>(
					context,
					IColorAndFontProvider.class.getName(), null);
			colorAndFontProviderTracker.open();
		}
		return colorAndFontProviderTracker.getService();
	}

}
