/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.image.SVGFileFormat;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * Handler for {@code swt-svg-current-color}, the color SVG images use for
 * {@code currentColor}. The color is global and applied only once per session,
 * so a theme change takes effect after a restart. An explicit
 * {@code swt.svg.currentColor} system property takes precedence.
 */
public class CSSPropertySVGCurrentColorHandler extends AbstractCSSPropertySWTHandler {

	private static volatile boolean frozen;

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (frozen || System.getProperty("swt.svg.currentColor") != null) { //$NON-NLS-1$
			return;
		}
		// a definition registered later must not lock in the black fallback
		if (CSSSWTColorHelper.isUnresolvedColorDefinition(value)) {
			return;
		}
		Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
		if (color != null) {
			synchronized (CSSPropertySVGCurrentColorHandler.class) {
				if (!frozen) {
					frozen = true;
					SVGFileFormat.setCurrentColor(color.getRGB());
				}
			}
		}
	}

	/**
	 * Keeps the current color for the rest of the session, also when the starting
	 * theme did not declare one.
	 */
	public static synchronized void freeze() {
		frozen = true;
	}

	/**
	 * Allows the color to be applied again, for tests.
	 */
	public static synchronized void reset() {
		frozen = false;
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}
}
