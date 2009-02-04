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

package org.eclipse.e4.workbench.ui.internal;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class WorkbenchStylingSupport {

	static void initializeStyling(Display display, String cssURI) {
		// Instantiate SWT CSS Engine
		try {
			Class engineClass = Class
					.forName("org.eclipse.e4.ui.css.nebula.engine.CSSNebulaEngineImpl"); //$NON-NLS-1$
			Constructor ctor = engineClass.getConstructor(new Class[] { Display.class,
					Boolean.TYPE });
			Object engine = ctor.newInstance(new Object[] { display, Boolean.TRUE });
			Class errorHandlerClass = Class
					.forName("org.eclipse.e4.ui.css.core.engine.CSSErrorHandler"); //$NON-NLS-1$
			Method setErrorHandler = engineClass.getMethod(
					"setErrorHandler", new Class[] { errorHandlerClass }); //$NON-NLS-1$
			Class errorHandlerImplClass = Class
					.forName("org.eclipse.e4.ui.css.core.impl.engine.CSSErrorHandlerImpl"); //$NON-NLS-1$
			setErrorHandler.invoke(engine, new Object[] { errorHandlerImplClass
					.newInstance() });

			URL url = FileLocator.resolve(new URL(cssURI.toString()));
			InputStream stream = url.openStream();

			Method parseStyleSheet = engineClass.getMethod(
					"parseStyleSheet", new Class[] { InputStream.class }); //$NON-NLS-1$
			parseStyleSheet.invoke(engine, new Object[] { stream });
			stream.close();
		} catch (Throwable e) {
			System.err.println("Warning - could not initialize CSS styling (but the applicationCSS property has a value) : " + e.toString()); //$NON-NLS-1$
		}
	}

}
