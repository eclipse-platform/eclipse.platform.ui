/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests how changes in the underlying preference store may affect the path
 * variable manager. 
 */

public class Bug_027271 extends ResourceTest {

	static final String VARIABLE_PREFIX = "pathvariable."; //$NON-NLS-1$

	private Preferences preferences;

	public Bug_027271(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		clearPathVariablesProperties();
	}

	protected void tearDown() throws Exception {
		clearPathVariablesProperties();
		super.tearDown();
	}

	private void clearPathVariablesProperties() {
		// ensure we have no preferences related to path variables
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++)
			if (propertyNames[i].startsWith(VARIABLE_PREFIX))
				preferences.setToDefault(propertyNames[i]);
	}

	public static Test suite() {
		return new TestSuite(Bug_027271.class);
	}

	public void testBug() {
		//this bug is only relevant on Windows
		if (!isWindows())
			return;
		IPathVariableManager pvm = getWorkspace().getPathVariableManager();
		Preferences prefs = ResourcesPlugin.getPlugin().getPluginPreferences();

		assertEquals("1.0", 0, pvm.getPathVariableNames().length);
		prefs.setValue(VARIABLE_PREFIX + "VALID_VAR", new Path("c:/temp").toPortableString());
		assertEquals("1.1", 1, pvm.getPathVariableNames().length);
		assertEquals("1.2", "VALID_VAR", pvm.getPathVariableNames()[0]);

		//sets invalid value (relative path)
		IPath relativePath = new Path("temp");
		prefs.setValue(VARIABLE_PREFIX + "INVALID_VAR", relativePath.toPortableString());
		assertEquals("2.0", 1, pvm.getPathVariableNames().length);
		assertEquals("2.1", "VALID_VAR", pvm.getPathVariableNames()[0]);

		//sets invalid value (invalid path)
		IPath invalidPath = new Path("c:\\a\\:\\b");
		prefs.setValue(VARIABLE_PREFIX + "ANOTHER_INVALID_VAR", invalidPath.toPortableString());
		assertTrue("3.0", !Path.EMPTY.isValidPath(invalidPath.toPortableString()));
		assertEquals("3.1", 1, pvm.getPathVariableNames().length);
		assertEquals("3.2", "VALID_VAR", pvm.getPathVariableNames()[0]);
	}
}
