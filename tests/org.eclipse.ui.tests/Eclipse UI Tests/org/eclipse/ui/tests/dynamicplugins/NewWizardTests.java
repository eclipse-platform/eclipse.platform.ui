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
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @since 3.1
 */
public class NewWizardTests extends DynamicTestCase {

	private static final String WIZARD_ID = "org.eclipse.newNewWizard1.newNewWizard1";
	
	/**
	 * 
	 */
	public NewWizardTests(String testName) {
		super(testName);
	}
	
	public void testNewWizardProperties() {
		IWizardRegistry registry = WorkbenchPlugin.getDefault().getNewWizardRegistry();
		assertNull(registry.findWizard(WIZARD_ID));
		getBundle();
		IWizardDescriptor wizard = registry.findWizard(WIZARD_ID);
		assertNotNull(wizard);
		testNewWizardProperties(wizard);
		removeBundle();
		assertNull(registry.findWizard(WIZARD_ID));
		try {
			testNewWizardProperties(wizard);
			fail();
		}
		catch (RuntimeException e) {
			//no-op
		}
	}
	
	/**
	 * @param wizard
	 */
	private void testNewWizardProperties(IWizardDescriptor wizard) {
		assertNotNull(wizard.getId());
		assertNotNull(wizard.getDescription());
		assertNotNull(wizard.getHelpHref());
		assertNotNull(wizard.getDescriptionImage());
		assertNotNull(wizard.getImageDescriptor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {		
		return "newNewWizard1.testDynamicNewWizardAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_NEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newNewWizard1";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicWizard";
	}
}
