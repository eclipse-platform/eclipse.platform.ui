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

import org.eclipse.jface.dialogs.InputDialog;
import org.junit.After;
import org.junit.Test;

public class InputDialogTest {

	private InputDialog dialog;

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
	}

	@Test
	public void testSetErrorMessageEarly() {
		dialog = new InputDialog(null, "TEST", "value", "test", null);
		dialog.setBlockOnOpen(false);
		dialog.setErrorMessage("error");
		dialog.open();
	}
}
