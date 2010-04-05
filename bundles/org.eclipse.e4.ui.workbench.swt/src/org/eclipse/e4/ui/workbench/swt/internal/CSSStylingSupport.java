/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.swt.internal;

//import java.io.InputStream;
import org.eclipse.e4.core.contexts.IEclipseContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.nebula.engine.CSSNebulaEngineImpl;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class CSSStylingSupport {

	public static void initializeStyling(Display display, String cssURI,
			String resourceURI, IEclipseContext appContext) {

		// Instantiate SWT CSS Engine
		final CSSEngine engine = new CSSNebulaEngineImpl(display, true);
		engine.setErrorHandler(new CSSErrorHandler() {
			public void error(Exception e) {
				e.printStackTrace();
			}
		});

		display.setData("org.eclipse.e4.ui.css.core.engine", engine);

		// Create the OSGi resource locator
		if (resourceURI != null) {
			engine.getResourcesLocatorManager().registerResourceLocator(
					new OSGiResourceLocator(resourceURI.toString()));
		}

		// Lookup the style sheet

		try {
			URL url = FileLocator.resolve(new URL(cssURI.toString()));
			display.setData("org.eclipse.e4.ui.css.core.cssURL", url); //$NON-NLS-1$		

			display.setData("org.eclipse.e4.ui.css.context", appContext); //$NON-NLS-1$

			InputStream stream = url.openStream();
			engine.parseStyleSheet(stream);
			stream.close();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
				((Widget) widget).setData(
						"org.eclipse.e4.ui.css.CssClassName", classname); //$NON-NLS-1$
				engine.applyStyles(widget, true);
			}

			public void setId(Object widget, String id) {
				((Widget) widget).setData("org.eclipse.e4.ui.css.id", id); //$NON-NLS-1$
				engine.applyStyles(widget, true);
			}

			public void style(Object widget) {
				engine.applyStyles(widget, true);
			}

		});

	}

}
