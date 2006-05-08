/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class DecoratorTests extends DynamicTestCase {
	
	public static final String FULL1 = "fullDecorator1";
	public static final String LIGHT1 = "lightDecorator1";
	public static final String LIGHT2 = "lightDecorator2";

	/**
	 * @param testName
	 */
	public DecoratorTests(String testName) {
		super(testName);
	}
	
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
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].getId().equals(id))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newDecorator1.testDynamicDecoratorAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_DECORATORS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newDecorator1";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicLabelDecorator";
	}
}
