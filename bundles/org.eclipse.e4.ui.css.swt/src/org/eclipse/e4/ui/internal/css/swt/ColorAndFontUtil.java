/*******************************************************************************
 *  Copyright (c) 2010, 2026 IBM Corporation and others.
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class ColorAndFontUtil {

	private static final IColorAndFontProvider DEFAULT_PROVIDER = new JFaceColorAndFontProvider();

	private static final ServiceTracker<IColorAndFontProvider, IColorAndFontProvider> TRACKER = openTracker();

	private static ServiceTracker<IColorAndFontProvider, IColorAndFontProvider> openTracker() {
		BundleContext context = FrameworkUtil.getBundle(ColorAndFontUtil.class).getBundleContext();
		if (context == null) {
			return null;
		}
		ServiceTracker<IColorAndFontProvider, IColorAndFontProvider> tracker = new ServiceTracker<>(context,
				IColorAndFontProvider.class, null);
		tracker.open();
		return tracker;
	}

	/**
	 * Returns the registered {@link IColorAndFontProvider} service, or a provider
	 * backed by the JFace registries if none is registered.
	 */
	public static IColorAndFontProvider getColorAndFontProvider() {
		IColorAndFontProvider provider = TRACKER != null ? TRACKER.getService() : null;
		return provider != null ? provider : DEFAULT_PROVIDER;
	}

}
