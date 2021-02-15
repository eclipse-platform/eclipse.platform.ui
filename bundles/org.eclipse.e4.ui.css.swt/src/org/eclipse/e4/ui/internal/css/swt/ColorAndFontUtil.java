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
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ColorAndFontUtil {

	static IColorAndFontProvider colorAndFontProvider = null;

	static {
		if (FrameworkUtil.getBundle(ColorAndFontUtil.class).getBundleContext() != null) {
			ServiceTracker<IColorAndFontProvider, IColorAndFontProvider> colorAndFontProviderTracker = new ServiceTracker<>(
					FrameworkUtil.getBundle(ColorAndFontUtil.class).getBundleContext(),
					IColorAndFontProvider.class.getName(), null) {
				@Override
				public IColorAndFontProvider addingService(ServiceReference<IColorAndFontProvider> reference) {
					// this is needed so that the unit test can exchange the color and font provider
					// with a mocked version
					colorAndFontProvider = super.addingService(reference);
					return colorAndFontProvider;
				}
			};
			colorAndFontProviderTracker.open();
		}
	}

	/**
	 * Util method to access the OSGI immediate component IColorAndFontProvider
	 * defined in the same bundle as we do life in the same bundle we do not track
	 * its life-cycle (as it is the same as our life-cycle)
	 */
	public static IColorAndFontProvider getColorAndFontProvider() {
		return colorAndFontProvider;
	}

}
