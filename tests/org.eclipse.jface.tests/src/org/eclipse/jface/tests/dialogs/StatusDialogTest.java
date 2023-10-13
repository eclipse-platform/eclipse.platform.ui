/*******************************************************************************
 * Copyright (c) 2012 Rüdiger Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatusDialogTest {

	private static final String PLUGIN_ID = "org.eclipse.ui.tests";

	private Shell shell;

	@Test
	public void testEscapeAmpesandInStatusLabelBug395426() {
		TestableStatusDialog dialog = new TestableStatusDialog(shell);
		dialog.open();
		dialog.updateStatus(new Status(IStatus.ERROR, PLUGIN_ID, "&"));
		CLabel statusLabel = findStatusLabel(dialog.getShell());
		assertEquals("&&", statusLabel.getText());
	}

	@Before
	public void setUp() throws Exception {
		shell = new Shell();
	}

	@After
	public void tearDown() throws Exception {
		shell.dispose();
	}

	private CLabel findStatusLabel(Composite parent) {
		CLabel result = null;
		Control[] children = parent.getChildren();
		for (Control child : children) {
			if (child instanceof CLabel cLabel) {
				result = cLabel;
			}
		}
		if (result == null) {
			for (Control child : children) {
				if (child instanceof Composite composite) {
					result = findStatusLabel(composite);
				}
			}
		}
		return result;
	}

	public class TestableStatusDialog extends StatusDialog {

		public TestableStatusDialog(Shell parent) {
			super(parent);
			setBlockOnOpen(false);
		}

		@Override
		protected void updateStatus(IStatus status) {
			super.updateStatus(status);
		}
	}
}