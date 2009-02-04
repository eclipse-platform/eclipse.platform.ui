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
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.nebula.engine.CSSNebulaEngineImpl;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class WorkbenchStylingSupport {

	static CSSEngine initializeStyling(Display display, String cssURI) {
		// Instantiate SWT CSS Engine
		CSSEngine engine = new CSSNebulaEngineImpl(display, true);
		engine.setErrorHandler(new CSSErrorHandler() {
			public void error(Exception e) {
				e.printStackTrace();
			}
		});
		
		try {
			URL url = FileLocator.resolve(new URL(cssURI.toString()));
			InputStream stream = url.openStream();
			engine.parseStyleSheet(stream);	
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return engine;
	}

}
