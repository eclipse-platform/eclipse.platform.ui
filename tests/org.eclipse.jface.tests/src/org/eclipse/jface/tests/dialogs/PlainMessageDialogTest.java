/*******************************************************************************
 * Copyright (c) 2021 SAP SE and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial api
 *******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.PlainMessageDialog;
import org.eclipse.jface.dialogs.PlainMessageDialog.Builder;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlainMessageDialogTest {

	private Builder builder;
	private PlainMessageDialog dialog;

	@BeforeEach
	public void setup() {
		Shell shell = new Shell(Display.getDefault());
		builder = PlainMessageDialog.getBuilder(shell, "My Dialog");
	}

	@AfterEach
	public void tearDown() {
		if (dialog != null) {
			dialog.close();
		}
	}

	@Test
	public void createsDialogInShellWithTitle() {
		createAndOpenDialog();

		assertEquals("My Dialog", dialog.getShell().getText());
	}

	@Test
	public void createsDialogWithMessage() {
		builder.message("Hello World!");
		createAndOpenDialog();

		Label label = (Label) dialog.getShell().getChildren()[0];

		assertEquals("Hello World!", label.getText());
	}

	@Test
	public void createsDialogWithButtons() {
		builder.buttonLabels(Arrays.asList("Yes", "No", "Cancel"));
		createAndOpenDialog();

		Composite buttonComposite = (Composite) dialog.getShell().getChildren()[1];
		Button left = (Button) buttonComposite.getChildren()[0];
		Button middle = (Button) buttonComposite.getChildren()[1];
		Button right = (Button) buttonComposite.getChildren()[2];

		// default button on windows is as declared
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			assertEquals("Yes", left.getText());
			assertEquals("No", middle.getText());
			assertEquals("Cancel", right.getText());
		} else {
			// on Linux / Mac default button is moved to the right
			assertEquals("No", left.getText());
			assertEquals("Cancel", middle.getText());
			assertEquals("Yes", right.getText());
		}

	}

	@Test
	public void createsDialogWithDefaultButton() {
		builder.buttonLabels(Arrays.asList("Yes", "No", "Cancel")).defaultButtonIndex(1);
		createAndOpenDialog();

		Composite buttonComposite = (Composite) dialog.getShell().getChildren()[1];
		Button left = (Button) buttonComposite.getChildren()[0];
		Button middle = (Button) buttonComposite.getChildren()[1];
		Button right = (Button) buttonComposite.getChildren()[2];

		assertEquals("Yes", left.getText());

		// default button on windows is as declared
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			assertEquals("No", middle.getText());
			assertEquals("Cancel", right.getText());
		} else {
			// on Linux / Mac default button is moved to the right
			assertEquals("Cancel", middle.getText());
			assertEquals("No", right.getText());
		}
	}

	private void createAndOpenDialog() {
		dialog = builder.build();
		dialog.create();
		dialog.setBlockOnOpen(false);
		dialog.open();
	}

}
