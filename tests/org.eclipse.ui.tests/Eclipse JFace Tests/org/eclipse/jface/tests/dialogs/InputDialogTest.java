/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.jface.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.InputDialog;

public class InputDialogTest extends TestCase {

	private InputDialog dialog;

	@Override
	protected void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
		super.tearDown();
	}

	public void testSetErrorMessageEarly() {
		dialog = new InputDialog(null, "TEST", "value", "test", null);
		dialog.setBlockOnOpen(false);
		dialog.setErrorMessage("error");
		dialog.open();
	}
}
