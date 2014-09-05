/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;

/**
 * Tests whether the "org.eclipse.ui.acceleratorConfigurations" extension point
 * can be added and removed dynamically.
 * 
 * @since 3.1.1
 */
public final class AcceleratorConfigurationsExtensionDynamicTest extends
		DynamicTestCase {

	/**
	 * Constructs a new instance of
	 * <code>AcceleratorConfigurationsExtensionDynamicTest</code>.
	 * 
	 * @param testName
	 *            The name of the test; may be <code>null</code>.
	 */
	public AcceleratorConfigurationsExtensionDynamicTest(final String testName) {
		super(testName);
	}

	/**
	 * Returns the full-qualified identifier of the extension to be tested.
	 * 
	 * @return The extension identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionId() {
		return "acceleratorConfigurationsExtensionDynamicTest.testDynamicAcceleratorConfigurationAddition";
	}

	/**
	 * Returns the unqualified identifier of the extension point to be tested.
	 * 
	 * @return The extension point identifier; never <code>null</code>.
	 */
	@Override
	protected final String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_ACCELERATOR_CONFIGURATIONS;
	}

	/**
	 * Returns the relative location of the folder on disk containing the
	 * plugin.xml file.
	 * 
	 * @return The relative install location; never <code>null</code>.
	 */
	@Override
	protected final String getInstallLocation() {
		return "data/org.eclipse.acceleratorConfigurationsExtensionDynamicTest";
	}

	/**
	 * Tests whether the items defined in the extension point can be added and
	 * removed dynamically. It tests that the data doesn't exist, and then loads
	 * the extension. It tests that the data then exists, and unloads the
	 * extension. It tests that the data then doesn't exist.
	 */
	public final void testAcceleratorConfigurations() {
		final IBindingService service = (IBindingService) getWorkbench()
				.getAdapter(IBindingService.class);
		NamedHandleObject namedHandleObject;

		namedHandleObject = service.getScheme("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}

		getBundle();

		namedHandleObject = service.getScheme("monkey");
		try {
			assertTrue("Monkey".equals(namedHandleObject.getName()));
		} catch (final NotDefinedException e) {
			fail();
		}

		removeBundle();

		namedHandleObject = service.getScheme("monkey");
		try {
			namedHandleObject.getName();
			fail();
		} catch (final NotDefinedException e) {
			assertTrue(true);
		}
	}
}
