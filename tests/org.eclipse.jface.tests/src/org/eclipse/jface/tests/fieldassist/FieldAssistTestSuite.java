/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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

package org.eclipse.jface.tests.fieldassist;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for the platform operations support.
 */
@Suite
@SelectClasses({ // disabled, see bug 275393...
		// TextFieldAssistTests.class, ComboFieldAssistTests.class,
		ControlDecorationTests.class, FieldAssistAPITests.class })
public class FieldAssistTestSuite {
}
