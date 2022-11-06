/*******************************************************************************
 * Copyright (c) 2016 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - initial contribution
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.junit.jupiter.api.Test;

public class Bug459961Test extends CSSSWTTestCase {

	@Test
	void testRegularColorConstantReference() {
		String cssString = "Label { background-color: COLOR-GREEN; }";

		Label label = createTestLabel(cssString);

		RGBA expected = Display.getDefault().getSystemColor(SWT.COLOR_GREEN).getRGBA();
		RGBA actual = label.getBackground().getRGBA();
		assertEquals(expected, actual);
	}

	@Test
	void testTransparentColorConstantReference() {
		String cssString = "Label { background-color: COLOR-TRANSPARENT; }";

		Label label = createTestLabel(cssString);

		RGBA expected = Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT).getRGBA();
		RGBA actual = label.getBackground().getRGBA();
		assertEquals(expected, actual);
	}

}
