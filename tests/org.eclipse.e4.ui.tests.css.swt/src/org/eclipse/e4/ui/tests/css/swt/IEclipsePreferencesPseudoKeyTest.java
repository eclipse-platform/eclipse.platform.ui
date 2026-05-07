/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
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
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.jupiter.api.Test;

/**
 * Locks in the {@code IEclipsePreferences#node:pseudo} selector form used by
 * shipped themes. See Bug 466075.
 */
public class IEclipsePreferencesPseudoKeyTest extends CSSSWTTestCase {

	@Test
	void testPseudoSelectorMatchesAndWritesPreferenceValue() {
		IEclipsePreferences preferences = new EclipsePreferences(null, "org.eclipse.jdt.ui") {};

		engine = createEngine(
				"""
					IEclipsePreferences#org-eclipse-jdt-ui:org-eclipse-ui-themes {\
					preferences: 'semanticHighlighting.abstractClass.color=128,255,0'\
					}""",
				display);
		engine.applyStyles(preferences, false);

		// Bug 466075
		assertEquals("128,255,0", preferences.get("semanticHighlighting.abstractClass.color", null));
	}

	@Test
	void testDifferentPseudosOnSameNodeAllContribute() {
		// Mirrors how org.eclipse.ui.editors and org.eclipse.ui.themes both
		// contribute to the same preference node via different pseudo tags.
		IEclipsePreferences preferences = new EclipsePreferences(null, "org.eclipse.ui.workbench") {};

		engine = createEngine(
				"""
					IEclipsePreferences#org-eclipse-ui-workbench:org-eclipse-ui-editors {\
					preferences: 'org.eclipse.ui.editors.inlineAnnotationColor=155,155,155'\
					}\
					IEclipsePreferences#org-eclipse-ui-workbench:org-eclipse-ui-themes {\
					preferences: 'ERROR_COLOR=247,68,117'\
					}""",
				display);
		engine.applyStyles(preferences, false);

		assertEquals("155,155,155", preferences.get("org.eclipse.ui.editors.inlineAnnotationColor", null));
		assertEquals("247,68,117", preferences.get("ERROR_COLOR", null));
	}

	@Test
	void testCascadeOverrideForSameKeyAndSamePseudoLastWins() {
		// Two equal-specificity rules under the same pseudo write the same
		// preference key with different values. The CSS cascade in viewCSS
		// resolves the duplicate "preferences" property to the second rule's
		// value before EclipsePreferencesHandler ever sees it, so the LATER
		// rule wins. This matches standard CSS source-order tiebreak.
		IEclipsePreferences preferences = new EclipsePreferences(null, "org.eclipse.jdt.ui") {};

		engine = createEngine(
				"""
					IEclipsePreferences#org-eclipse-jdt-ui:org-eclipse-ui-themes {\
					preferences: 'semanticHighlighting.abstractClass.color=128,255,0'\
					}\
					IEclipsePreferences#org-eclipse-jdt-ui:org-eclipse-ui-themes {\
					preferences: 'semanticHighlighting.abstractClass.color=255,0,0'\
					}""",
				display);
		engine.applyStyles(preferences, false);

		assertEquals("255,0,0", preferences.get("semanticHighlighting.abstractClass.color", null));
	}
}
