/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;

/**
 * @since 3.1
 */
public class DecoratorTests extends DynamicTestCase {

	public static final String FULL1 = "fullDecorator1";
	public static final String LIGHT1 = "lightDecorator1";
	public static final String LIGHT2 = "lightDecorator2";

	@Test
	public void testDecorators() {
		assertFalse(hasDecorator(FULL1));
		assertFalse(hasDecorator(LIGHT1));
		assertFalse(hasDecorator(LIGHT2));
		getBundle();
		assertTrue(hasDecorator(FULL1));
		assertTrue(hasDecorator(LIGHT1));
		assertTrue(hasDecorator(LIGHT2));
		removeBundle();
		assertFalse(hasDecorator(FULL1));
		assertFalse(hasDecorator(LIGHT1));
		assertFalse(hasDecorator(LIGHT2));
	}

	public boolean hasDecorator(String id) {
		DecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
		DecoratorDefinition [] definitions = manager.getAllDecoratorDefinitions();
		for (DecoratorDefinition definition : definitions) {
			if (definition.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getExtensionId() {
		return "newDecorator1.testDynamicDecoratorAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_DECORATORS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newDecorator1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicLabelDecorator";
	}
}
