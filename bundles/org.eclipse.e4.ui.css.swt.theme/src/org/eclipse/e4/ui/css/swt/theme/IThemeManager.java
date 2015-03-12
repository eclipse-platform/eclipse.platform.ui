/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.theme;

import org.eclipse.swt.widgets.Display;

/**
 * Manages the theme engines for displays. This service is available through the
 * OSGi-Service registry
 */
public interface IThemeManager {
	/**
	 * Get the engine for a given display
	 *
	 * @param display
	 *            the display the engine is for
	 * @return a new theme engine when none already created
	 */
	public IThemeEngine getEngineForDisplay(Display display);
}
