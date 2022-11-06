/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("see bug #273582")
public class ShellActiveTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected Shell createShell(String styleSheet) {
		createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.NONE);

		engine.applyStyles(shell, true);

		shell.pack();
		shell.open();
		return shell;
	}

	@Test
	void testShellActive() throws Exception {
		Shell shell = createShell("Shell:active {background-color: #FF0000;}\n" +
				"Shell {background-color: #0000FF;}");
		assertEquals(RED, shell.getBackground().getRGB());
		Shell newShell = createShell("Shell { background-color: #0000FF; }");
		assertEquals(BLUE, shell.getBackground().getRGB());
		shell.close();
		newShell.close();
	}
}
