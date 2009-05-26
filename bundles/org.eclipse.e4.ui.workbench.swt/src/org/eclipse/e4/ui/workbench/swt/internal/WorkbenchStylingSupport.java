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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class WorkbenchStylingSupport {

	public static void initializeStyling(Display display, String cssURI,
			IEclipseContext appContext) {
		// Instantiate SWT CSS Engine
		try {
			Class engineClass = Class
					.forName("org.eclipse.e4.ui.css.nebula.engine.CSSNebulaEngineImpl"); //$NON-NLS-1$
			Constructor ctor = engineClass.getConstructor(new Class[] {
					Display.class, Boolean.TYPE });
			final Object engine = ctor.newInstance(new Object[] { display,
					Boolean.TRUE });
			display.setData("org.eclipse.e4.ui.css.core.engine", engine); //$NON-NLS-1$

			Class errorHandlerClass = Class
					.forName("org.eclipse.e4.ui.css.core.engine.CSSErrorHandler"); //$NON-NLS-1$
			Method setErrorHandler = engineClass.getMethod(
					"setErrorHandler", new Class[] { errorHandlerClass }); //$NON-NLS-1$
			Class errorHandlerImplClass = Class
					.forName("org.eclipse.e4.ui.css.core.impl.engine.CSSErrorHandlerImpl"); //$NON-NLS-1$
			setErrorHandler.invoke(engine, new Object[] { errorHandlerImplClass
					.newInstance() });

			URL url = FileLocator.resolve(new URL(cssURI.toString()));
			display.setData("org.eclipse.e4.ui.css.core.cssURL", url); //$NON-NLS-1$		

			InputStream stream = url.openStream();
			Method parseStyleSheet = engineClass.getMethod(
					"parseStyleSheet", new Class[] { InputStream.class }); //$NON-NLS-1$
			parseStyleSheet.invoke(engine, new Object[] { stream });
			stream.close();

			final Method applyStyles = engineClass.getMethod(
					"applyStyles", new Class[] { Object.class, Boolean.TYPE }); //$NON-NLS-1$
			appContext.set(IStylingEngine.class.getName(),
					new IStylingEngine() {
						public void setClassname(Object widget, String classname) {
							((Widget) widget)
									.setData(
											"org.eclipse.e4.ui.css.CssClassName", classname); //$NON-NLS-1$
							try {
								applyStyles.invoke(engine, new Object[] {
										widget, Boolean.TRUE });
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						public void setId(Object widget, String id) {
							((Widget) widget).setData(
									"org.eclipse.e4.ui.css.id", id); //$NON-NLS-1$
							try {
								applyStyles.invoke(engine, new Object[] {
										widget, Boolean.TRUE });
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

		} catch (Throwable e) {
			System.err
					.println("Warning - could not initialize CSS styling (but the applicationCSS property has a value) : " + e.toString()); //$NON-NLS-1$
			initializeNullStyling(appContext);
		}
	}

	/**
	 * For use when there is no real styling engine present. Has no behaviour
	 * but conforms to IStylingEngine API.
	 * 
	 * @param appContext
	 */
	public static void initializeNullStyling(IEclipseContext appContext) {
		appContext.set(IStylingEngine.class.getName(), new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
			}

			public void setId(Object widget, String id) {
			}
		});
	}

}
