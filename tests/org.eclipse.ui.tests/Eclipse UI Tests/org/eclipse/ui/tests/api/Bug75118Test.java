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
package org.eclipse.ui.tests.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

import junit.framework.TestCase;

/**
 * @since 3.1
 */
public class Bug75118Test extends TestCase {

	/**
	 *
	 */
	public Bug75118Test() {
		super();
	}

	/**
	 * @param name
	 */
	public Bug75118Test(String name) {
		super(name);
	}

	public void testWizards() {
		WizardsRegistryReader reader = new WizardsRegistryReader(
				PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_NEW);
		WorkbenchWizardElement [] primaryWizards = reader.getPrimaryWizards();
		Set<WorkbenchWizardElement> wizardSet = new HashSet<>(Arrays.asList(primaryWizards));

		//any duplicates would have been removed by adding it to the set
		//so if the sizes are different something has been removed - ie:
		//a duplicate
		assertEquals(primaryWizards.length, wizardSet.size());
	}
}
