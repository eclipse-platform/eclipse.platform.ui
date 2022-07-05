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

import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class HelpSupportTests extends DynamicTestCase {

	public HelpSupportTests() {
		super(HelpSupportTests.class.getSimpleName());
	}

	@Test
	public void testHelpSupport() {
		WorkbenchHelpSystem help = WorkbenchHelpSystem.getInstance();
		help.setDesiredHelpSystemId(getExtensionId());
		assertFalse(help.hasHelpUI());

		getBundle();
		help.dispose();
		assertTrue(help.hasHelpUI());

		removeBundle();
		help.dispose();
		assertFalse(help.hasHelpUI());

		help.setDesiredHelpSystemId(null);
	}

	@Override
	protected String getExtensionId() {
		return "newHelpSupport1.testDynamicHelpSupportAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_HELPSUPPORT;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newHelpSupport1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicHelpSupport";
	}

}
