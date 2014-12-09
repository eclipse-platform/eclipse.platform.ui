/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430848
 *******************************************************************************/
package org.eclipse.ui.internal.splash;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.splash.BasicSplashHandler;

/**
 * Parses the well known product constants and constructs a splash handler
 * accordingly.
 */
public class EclipseSplashHandler extends BasicSplashHandler {

	// CSS id can be used to style the label for the build ID
	private static final String CSS_ID_SPLASH_BUILD_ID = "org-eclipse-ui-buildid-text"; //$NON-NLS-1$

	/**
	 * Initializes the splash screen.
	 * <p>
	 * <strong>WARNING:</strong> Do not change the default values as existing
	 * products might rely on them.
	 * </p>
	 *
	 * @param splash
	 *            the shell that contains the splash screen
	 */
	@Override
	public void init(Shell splash) {
		super.init(splash);
		String progressRectString = null;
		String messageRectString = null;
		String foregroundColorString = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			progressRectString = product
					.getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
			messageRectString = product
					.getProperty(IProductConstants.STARTUP_MESSAGE_RECT);
			foregroundColorString = product
					.getProperty(IProductConstants.STARTUP_FOREGROUND_COLOR);
		}
		Rectangle progressRect = StringConverter.asRectangle(
				progressRectString, new Rectangle(10, 10, 300, 15));
		setProgressRect(progressRect);

		Rectangle messageRect = StringConverter.asRectangle(messageRectString,
				new Rectangle(10, 35, 300, 15));
		setMessageRect(messageRect);

		int foregroundColorInteger;
		try {
			foregroundColorInteger = Integer
					.parseInt(foregroundColorString, 16);
		} catch (Exception ex) {
			foregroundColorInteger = 0xD2D7FF; // off white
		}

		setForeground(new RGB((foregroundColorInteger & 0xFF0000) >> 16,
				(foregroundColorInteger & 0xFF00) >> 8,
				foregroundColorInteger & 0xFF));
		// the following code will be removed for release time
		if (PrefUtil.getInternalPreferenceStore().getBoolean(
				"SHOW_BUILDID_ON_STARTUP")) { //$NON-NLS-1$
			final String buildId = System.getProperty(
					"eclipse.buildId", "Unknown Build"); //$NON-NLS-1$ //$NON-NLS-2$
			// find the specified location.  Not currently API
			// hardcoded to be sensible with our current splash Graphic

			String buildIdLocString = product.getProperty("buildIdLocation"); //$NON-NLS-1$
			String buildIdSize = product.getProperty("buildIdSize"); //$NON-NLS-1$
			if (buildIdLocString != null) {
				if (buildIdSize != null) {
					buildIdLocString += "," + buildIdSize; //$NON-NLS-1$
				} else {
					buildIdLocString += ",100,40"; //$NON-NLS-1$
				}
			}
			Rectangle buildIdRectangle = StringConverter.asRectangle(buildIdLocString,
					new Rectangle(322, 190, 100, 40));

			Label idLabel = new Label(getContent(), SWT.RIGHT);
			idLabel.setForeground(getForeground());
			idLabel.setBounds(buildIdRectangle);
			idLabel.setText(buildId);
			idLabel.setData(CSSSWTConstants.CSS_ID_KEY, CSS_ID_SPLASH_BUILD_ID);
		}
		else {
			getContent(); // ensure creation of the progress
		}
	}
}
