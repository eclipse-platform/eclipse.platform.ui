/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension for all JFace action tests.
 *
 * @since 3.1
 */
public class JFaceActionExtension implements BeforeEachCallback, AfterEachCallback {

	private Display display;
	private Shell shell;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());
		shell.open();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
	}

	public Shell getShell() {
		return shell;
	}

}
