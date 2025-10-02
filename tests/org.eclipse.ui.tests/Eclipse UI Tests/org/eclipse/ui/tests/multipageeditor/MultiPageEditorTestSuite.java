/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.multipageeditor;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The suite of tests for multi-page editors.
 *
 * @since 3.0
 */
@Suite
@SelectClasses({
	MultiEditorInputTest.class,
	MultiPageEditorPartTest.class,
	MultiVariablePageTest.class,
	MultiPageKeyBindingTest.class,
})
public class MultiPageEditorTestSuite {
}
