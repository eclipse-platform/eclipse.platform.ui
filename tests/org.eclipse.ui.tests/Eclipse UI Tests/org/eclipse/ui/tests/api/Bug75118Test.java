/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

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
		NewWizardsRegistryReader reader = new NewWizardsRegistryReader();
		WorkbenchWizardElement [] primaryWizards = reader.getPrimaryWizards();
		Set wizardSet = new HashSet(Arrays.asList(primaryWizards));
		
		//any duplicates would have been removed by adding it to the set
		//so if the sizes are different something has been removed - ie:
		//a duplicate
		assertEquals(primaryWizards.length, wizardSet.size());
	}
}
