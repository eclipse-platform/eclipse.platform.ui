/*******************************************************************************
 * Copyright (c) 2012 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class StatusDialogTest extends TestCase {

	private static final String PLUGIN_ID = "org.eclipse.ui.tests";

	private Shell shell;

	public void testEscapeAmpesandInStatusLabelBug395426() {
		TestableStatusDialog dialog = new TestableStatusDialog(shell);
		dialog.open();
		dialog.updateStatus(new Status(IStatus.ERROR, PLUGIN_ID, "&"));
		CLabel statusLabel = findStatusLabel(dialog.getShell());
		assertEquals( "&&", statusLabel.getText());
	}

	@Override
	protected void setUp() throws Exception {
		shell = new Shell();
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	private CLabel findStatusLabel(Composite parent) {
		CLabel result = null;
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof CLabel) {
				result = (CLabel) children[i];
			}
		}
		if (result == null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof Composite) {
					result = findStatusLabel((Composite) children[i]);
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
