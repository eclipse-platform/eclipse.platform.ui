/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.widgets.Display;

public class IEclipsePreferencesTest extends CSSSWTTestCase {
	private Display display;

	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}

	public void testIEclipsePreferences() throws Exception {
		// given
		IEclipsePreferences preferences = new EclipsePreferences(null, "org.eclipse.jdt.ui") {};

		CSSEngine engine = createEngine(
				"IEclipsePreferences#org-eclipse-jdt-ui{preferences:"
						+ "'semanticHighlighting.abstractClass.color=128,255,0',"
						+ "'java_bracket=0,255,255',"
						+ "'java_bracket_italic=true',"
						+ "'java_bracket_underline='" + "}", display);
		// when
		engine.applyStyles(preferences, false);

		// then
		assertEquals("128,255,0", preferences.get(
				"semanticHighlighting.abstractClass.color", null));
		assertEquals("0,255,255", preferences.get("java_bracket", null));
		assertEquals("true", preferences.get("java_bracket_italic", null));
		assertEquals("", preferences.get("java_bracket_underline", null));
	}
}
