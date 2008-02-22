/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.TestCase;

public class ResourceTest extends TestCase {

	public void testHelpUIResources() throws IllegalArgumentException, IllegalAccessException {
		checkFields(org.eclipse.help.ui.internal.Messages.class);
	}

	public void testHelpBaseResources() throws IllegalArgumentException, IllegalAccessException {
		checkFields(org.eclipse.help.internal.base.HelpBaseResources.class);
	}

	public void testCheatsheetResources() throws IllegalArgumentException, IllegalAccessException {
		checkFields(org.eclipse.ui.internal.cheatsheets.Messages.class);
	}

	public void testIntroResources() throws IllegalArgumentException, IllegalAccessException {
		checkFields(org.eclipse.ui.internal.intro.impl.Messages.class);
	}
	
	public void testUniversalIntroResources() throws IllegalArgumentException, IllegalAccessException {
		checkFields(org.eclipse.ui.internal.intro.universal.Messages.class);
	}

	private void checkFields(Class messages) throws IllegalAccessException {
		Field[] fields = messages.getFields();
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
				Object value = fields[i].get(null); 
				if (value instanceof String) {
					String stringValue = (String)value;
					if (stringValue.startsWith("NLS missing message")) {
						fail("Missing resource for " + fields[i].getName());
					}
				}             
			}
		}
	}

}
