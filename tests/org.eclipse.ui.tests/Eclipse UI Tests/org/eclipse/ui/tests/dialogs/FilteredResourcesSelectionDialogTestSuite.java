/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lucas Bullen (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The suite of tests for the filteredResourcesSelectionDialog.
 *
 * @since 3.14
 */
@RunWith(org.junit.runners.AllTests.class)
public class FilteredResourcesSelectionDialogTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 *
	 * @return A new test suite; never <code>null</code>.;
	 */
	public static Test suite() {
		return new FilteredResourcesSelectionDialogTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public FilteredResourcesSelectionDialogTestSuite() {
		addTestSuite(ResourceItemLabelTest.class);
	}
}
