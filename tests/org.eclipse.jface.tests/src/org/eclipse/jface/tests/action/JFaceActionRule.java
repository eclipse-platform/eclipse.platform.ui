/*******************************************************************************
 * Copyright (c) 2023 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.action;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Junit4 rule for all JFace action tests.
 *
 * @since 3.1
 */
public class JFaceActionRule implements TestRule {

	private Display display;
	private Shell shell;

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				display = Display.getCurrent();
				if (display == null) {
					display = new Display();
				}
				shell = new Shell(display);
				shell.setSize(500, 500);
				shell.setLayout(new FillLayout());
				shell.open();

				try {
					base.evaluate(); // This will run the test.
				} finally {
					shell.dispose();
				}
			}
		};
	}

	protected Shell getShell() {
		return shell;
	}

}
