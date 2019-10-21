/*******************************************************************************
 * Copyright (c) 2017-2019 Red Hat Inc. and others.
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
 *     Emmanuel Chebbi - test dialog's initial selection - Bug 214491
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The suite of tests for the FilteredResourcesSelectionDialog.
 *
 * @since 3.14
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ResourceItemLabelTest.class,
	ResourceInitialSelectionTest.class,
	ResourceSelectionFilteringDialogTest.class,
})
public class FilteredResourcesSelectionDialogTestSuite {
}
