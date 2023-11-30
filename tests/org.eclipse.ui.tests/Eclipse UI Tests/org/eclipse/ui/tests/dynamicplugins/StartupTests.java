/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class StartupTests extends DynamicTestCase {

	public StartupTests() {
		super(StartupTests.class.getSimpleName());
	}

	/**
	 * Tests to ensure that the IStartup implementation in the bundle is run
	 * when the bundle is loaded.
	 */
	@Test
	public void testStartupRun() throws ClassNotFoundException,
			SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Bundle bundle = getBundle();
		Class<?> clazz = bundle.loadClass(getMarkerClass());
		assertNotNull(clazz);
		Field field = clazz.getDeclaredField("history");
		assertNotNull(field);
		assertTrue((field.getModifiers() & Modifier.STATIC) != 0);
		// if the startup code has run then this will not be null - the early
		// startup method sets this
		assertNotNull(field.get(null));
	}

	@Override
	protected String getExtensionId() {
		return "newStartup1.testDynamicStartupAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_STARTUP;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newStartup1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicStartup";
	}
}
