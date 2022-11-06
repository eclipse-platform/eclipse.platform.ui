/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemesExtension;
import org.junit.jupiter.api.Test;

public class ThemesExtensionTest extends CSSSWTTestCase {

	@Test
	void testThemesExtension() {
		//given
		engine = createEngine(
				"ThemesExtension {font-definition: '#org-eclipse-ui-workbench-FONT_DEF_1',"
						+
						"'#org-eclipse-ui-workbench-FONT_DEF_2'; color-definition: '#org-eclipse-ui-workbench-COLOR_DEF_1';}", display);
		ThemesExtension themesExtention = new ThemesExtension();

		//when
		engine.applyStyles(themesExtention, true);

		//then
		assertEquals(3, themesExtention.getDefinitions().size());

		assertTrue(themesExtention.getDefinitions().get(0) instanceof FontDefinition);
		FontDefinition fontDefinition1 = (FontDefinition) themesExtention.getDefinitions().get(0);
		assertTrue(fontDefinition1.isAddedByCss());
		assertFalse(fontDefinition1.isOverridden());
		assertEquals("org.eclipse.ui.workbench.FONT_DEF_1",fontDefinition1.getId());
		assertEquals(ThemesExtension.DEFAULT_CATEGORY_ID,fontDefinition1.getCategoryId());
		assertTrue(fontDefinition1.getName().startsWith(FontDefinition.class.getSimpleName()));
		assertTrue(fontDefinition1.getName().endsWith(fontDefinition1.getId()));
		assertNotNull(fontDefinition1.getDescription());

		assertTrue(themesExtention.getDefinitions().get(1) instanceof FontDefinition);
		FontDefinition fontDefinition2 = (FontDefinition) themesExtention.getDefinitions().get(1);
		assertTrue(fontDefinition2.isAddedByCss());
		assertFalse(fontDefinition2.isOverridden());
		assertEquals("org.eclipse.ui.workbench.FONT_DEF_2",fontDefinition2.getId());
		assertEquals(ThemesExtension.DEFAULT_CATEGORY_ID,fontDefinition1.getCategoryId());
		assertTrue(fontDefinition2.getName().startsWith(FontDefinition.class.getSimpleName()));
		assertTrue(fontDefinition2.getName().endsWith(fontDefinition2.getId()));
		assertNotNull(fontDefinition2.getDescription());

		assertTrue(themesExtention.getDefinitions().get(2) instanceof ColorDefinition);
		ColorDefinition colorDefinition1 = (ColorDefinition) themesExtention.getDefinitions().get(2);
		assertTrue(colorDefinition1.isAddedByCss());
		assertFalse(colorDefinition1.isOverridden());
		assertEquals("org.eclipse.ui.workbench.COLOR_DEF_1",colorDefinition1.getId());
		assertNotNull(colorDefinition1.getDescription());
		assertEquals(ThemesExtension.DEFAULT_CATEGORY_ID,colorDefinition1.getCategoryId());
		assertTrue(colorDefinition1.getName().startsWith(ColorDefinition.class.getSimpleName()));
		assertTrue(colorDefinition1.getName().endsWith(colorDefinition1.getId()));
		assertNotNull(colorDefinition1.getDescription());
	}
}
