/*******************************************************************************
 * Copyright (c) 2018 Fabian Pfaff and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Pfaff - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.tests.fieldassist.AbstractFieldAssistTestCase;
import org.eclipse.jface.tests.fieldassist.AbstractFieldAssistWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class DirectoryProposalContentAssistTestCase extends AbstractFieldAssistTestCase {

	private DirectoryProposalContentAssistWindow directoryContentAssistWindow;

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		directoryContentAssistWindow = new DirectoryProposalContentAssistWindow();
		return directoryContentAssistWindow;
	}

	public void waitForDirectoryContentAssist() throws InterruptedException, ExecutionException {
		try {
			directoryContentAssistWindow.getContentAssist().wait(10000);
		} catch (TimeoutException e) {
		}
		spinEventLoop();
	}


	public void sendKeyEventToControl(char character) {
		sendKeyDownToControl(character);
		sendKeyUpToControl(character);
	}

	private void sendKeyUpToControl(char character) {
		sendFocusInToControl();
		Event event = new Event();
		event.type = SWT.KeyUp;
		event.character = character;
		assertTrue("unable to post event to display queue for test case",
				getFieldAssistWindow().getDisplay().post(event));
		spinEventLoop();
	}

	public void sendKeyEventToControl(KeyStroke keyStroke) {
		sendKeyDownToControl(keyStroke);
		sendKeyUpToControl(keyStroke);
	}

	private void sendKeyUpToControl(KeyStroke keyStroke) {
		sendFocusInToControl();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keyStroke.getNaturalKey();
		assertTrue("unable to post event to display queue for test case",
				getFieldAssistWindow().getDisplay().post(event));
		spinEventLoop();
	}

	public void assertProposalSize(int size) {
		Shell[] shells = getFieldAssistWindow().getDisplay().getShells();
		Optional<Table> tableOptional = Arrays.stream(shells).map(this::retrieveTable)
				.filter(Objects::nonNull).findFirst();
		assertTrue("Couldn't assert pop-up proposal size - pop-up seems closed.", tableOptional.isPresent());
		TableItem[] proposals = tableOptional.get().getItems();

		assertEquals("Proposal size must be " + size, size, proposals.length);
	}

	private Table retrieveTable(Shell shell) {
		Control[] children = shell.getChildren();
		if (children.length >= 1) {
			Control control = children[0];
			if (control instanceof Composite composite) {
				Control[] children2 = composite.getChildren();
				if (children2.length >= 1) {
					Control control2 = composite.getChildren()[0];
					if (control2 instanceof Table t) {
						return t;
					}
				}
			}
		}
		return null;
	}

}
