/*******************************************************************************
 * Copyright (c) 2010, 2015 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
