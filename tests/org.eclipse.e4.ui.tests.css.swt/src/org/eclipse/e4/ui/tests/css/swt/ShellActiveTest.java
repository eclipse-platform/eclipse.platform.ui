/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ShellActiveTest extends CSSSWTTestCase {
	
	static final RGB RED = new RGB(255, 0, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	
	protected Shell createShell(String styleSheet) {
		Display display = Display.getDefault();
		CSSEngine engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.NONE);
		
		engine.applyStyles(shell, true);

		shell.pack();
		shell.open();
		return shell;
	}
	
	public void testShellActive() throws Exception {
		Shell shell = createShell("Shell:active {background-color: #FF0000;}\n" +
									"Shell {background-color: #0000FF;}");
		assertEquals(RED, shell.getBackground().getRGB());
		Shell newShell = createShell("Shell { background-color: #0000FF; }");
		assertEquals(BLUE, shell.getBackground().getRGB());
		shell.close();
		newShell.close();
	}
}
