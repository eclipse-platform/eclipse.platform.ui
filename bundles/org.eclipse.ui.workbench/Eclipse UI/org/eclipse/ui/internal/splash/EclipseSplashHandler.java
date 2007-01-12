/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.splash;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
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
		Rectangle progressRect = parseRect(progressRectString);
		if (progressRect == null) {
			progressRect = new Rectangle(10, 10, 300, 15);
		}
		setProgressRect(progressRect);

		Rectangle messageRect = parseRect(messageRectString);
		if (messageRect == null) {
			messageRect = new Rectangle(10, 35, 300, 15);
		}
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
			String buildId = System.getProperty("eclipse.buildId"); //$NON-NLS-1$
			if (buildId == null)
				buildId = "Unknown Build"; //$NON-NLS-1$
			// Point versionLocation = new Point(322,200); // hardcoded to be
			// sensible with our current Europa Graphic
			Label label = new Label(getContent(), SWT.NONE);
			label.setForeground(getForeground());
			label.setText(buildId);
			GC gc = new GC(label);
			Point stringExtent = gc.stringExtent(buildId);
			gc.dispose();
			label.setBounds(new Rectangle(322, 190, stringExtent.x,
					stringExtent.y)); // hardcoded to work with the eclipse
										// "Europa" label
			getContent().setBackgroundMode(SWT.INHERIT_NONE); // set the state to something new so that the next call actually does work on the label background
			getContent().setBackgroundMode(SWT.INHERIT_FORCE); // reforce the background for GTK.  
			getContent().setBackgroundImage(getContent().getShell().getBackgroundImage());
			while (label.getDisplay().readAndDispatch())
				; // force painting of the label
		}
	}

	private Rectangle parseRect(String string) {
		int x, y, w, h;
		int lastPos = 0;
		try {
			int i = string.indexOf(',', lastPos);
			x = Integer.parseInt(string.substring(lastPos, i));
			lastPos = i + 1;
			i = string.indexOf(',', lastPos);
			y = Integer.parseInt(string.substring(lastPos, i));
			lastPos = i + 1;
			i = string.indexOf(',', lastPos);
			w = Integer.parseInt(string.substring(lastPos, i));
			lastPos = i + 1;
			h = Integer.parseInt(string.substring(lastPos));
		} catch (RuntimeException e) {
			// sloppy error handling
			return null;
		}
		return new Rectangle(x, y, w, h);
	}
}
