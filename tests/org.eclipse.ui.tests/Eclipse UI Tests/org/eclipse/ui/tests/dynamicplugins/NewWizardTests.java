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

import static org.junit.Assert.assertThrows;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class NewWizardTests extends DynamicTestCase {

	private static final String WIZARD_ID = "org.eclipse.newNewWizard1.newNewWizard1";

	public NewWizardTests() {
		super(NewWizardTests.class.getSimpleName());
	}

	@Test
	public void testNewWizardProperties() {
		IWizardRegistry registry = WorkbenchPlugin.getDefault().getNewWizardRegistry();
		assertNull(registry.findWizard(WIZARD_ID));
		getBundle();
		IWizardDescriptor wizard = registry.findWizard(WIZARD_ID);
		assertNotNull(wizard);
		testNewWizardProperties(wizard);
		removeBundle();
		assertNull(registry.findWizard(WIZARD_ID));
		assertThrows(RuntimeException.class, () -> testNewWizardProperties(wizard));
	}

	private void testNewWizardProperties(IWizardDescriptor wizard) {
		assertNotNull(wizard.getId());
		assertNotNull(wizard.getDescription());
		assertNotNull(wizard.getHelpHref());
		assertNotNull(wizard.getDescriptionImage());
		assertNotNull(wizard.getImageDescriptor());
	}

	@Override
	protected String getExtensionId() {
		return "newNewWizard1.testDynamicNewWizardAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_NEW;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newNewWizard1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicWizard";
	}
}
