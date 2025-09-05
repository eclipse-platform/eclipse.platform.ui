/*******************************************************************************
 * Copyright (c) 2025 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.tests.window;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class WindowTest {

	// See https://github.com/eclipse-platform/eclipse.platform.ui/issues/3242
	@Test
	public void testCloseDialogWhileOpening() throws Exception {
		Window window = new Window((Shell) null) {
		};
		window.setBlockOnOpen(true);

		Listener closeWindowListener = event -> window.close();
		Display.getDefault().addFilter(SWT.Show, closeWindowListener);
		try {
			window.open();
		} finally {
			Display.getDefault().removeFilter(SWT.Show, closeWindowListener);
			window.close();
		}
	}

}
