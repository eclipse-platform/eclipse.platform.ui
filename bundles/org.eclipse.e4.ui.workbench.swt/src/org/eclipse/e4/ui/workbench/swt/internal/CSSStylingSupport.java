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
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 */
public class CSSStylingSupport {

	public static void initializeStyling(Display display, String cssURI,
			String resourceURI, IEclipseContext appContext) {
		Bundle bundle = Activator.getDefault().getBundle();
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(IThemeManager.class
				.getName());
		IThemeManager mgr = (IThemeManager) context.getService(ref);
		final IThemeEngine engine = mgr.getEngineForDisplay(display);

		// Store the app context
		display.setData("org.eclipse.e4.ui.css.context", appContext); //$NON-NLS-1$

		// Create the OSGi resource locator
		if (resourceURI != null) {
			engine.registerResourceLocator(new OSGiResourceLocator(resourceURI
					.toString()));
		}

		if (cssURI != null) {
			// Lookup the style sheet
			ITheme theme = engine.registerTheme(IThemeEngine.DEFAULT_THEME_ID,
					"Default Theme", cssURI);
			engine.setTheme(theme);
		}
		// TODO Should we create an empty default theme?

		appContext.set(IThemeEngine.class.getName(), engine);

		appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
				((Widget) widget).setData(
						"org.eclipse.e4.ui.css.CssClassName", classname); //$NON-NLS-1$
				engine.applyStyles((Widget) widget, true);
			}

			public void setId(Object widget, String id) {
				((Widget) widget).setData("org.eclipse.e4.ui.css.id", id); //$NON-NLS-1$
				engine.applyStyles((Widget) widget, true);
			}

			public void style(Object widget) {
				engine.applyStyles((Widget) widget, true);
			}

		});

	}
}
