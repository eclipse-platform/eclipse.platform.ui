/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A snippet to demonstrate the progress indicator on Vista showing paused and
 * errors
 *
 */
public class Snippet059VistaProgressIndicator {

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		shell.setSize(300, 100);

		ProgressIndicator indicator = new ProgressIndicator(shell,
				SWT.HORIZONTAL);

		Color backgroundColor = shell.getDisplay().getSystemColor(
				SWT.COLOR_CYAN);

		indicator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		indicator.setBackground(backgroundColor);
		shell.open();

		performProgressOn(display, indicator);

		indicator.done();
		display.dispose();

	}

	private static void performProgressOn(Display display,
			ProgressIndicator indicator) {

		indicator.beginTask(200);
		indicator.showNormal();
		indicator.worked(50);
		spin(display);

		indicator.showPaused();
		indicator.worked(50);
		spin(display);

		indicator.showError();
		indicator.worked(50);
		spin(display);
	}

	private static void spin(Display display) {
		long endTime = System.currentTimeMillis() + 1000;
		while (System.currentTimeMillis() < endTime)
			display.readAndDispatch();

	}
}
