/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Pins {@code CTabItem:selected} pseudo behaviour and the
 * {@code CTabFolderElement} selection listener path.
 */
public class CTabItemSelectionTest extends CSSSWTTestCase {

	private Shell shell;

	@Override
	@AfterEach
	public void tearDown() {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}
		super.tearDown();
	}

	private void spinEventLoop() {
		// Drain queued SWT events. Same pattern as CTabItemTest.
		for (int i = 0; i < 3; i++) {
			while (display.readAndDispatch()) {
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	private CTabFolder createFolderWithTwoItems(String styleSheet) {
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());

		CTabFolder folder = new CTabFolder(shell, SWT.NONE);
		CTabItem item0 = new CTabItem(folder, SWT.NONE);
		item0.setText("Item 0");
		CTabItem item1 = new CTabItem(folder, SWT.NONE);
		item1.setText("Item 1");
		folder.setSelection(0);

		engine = createEngine(styleSheet, display);
		engine.applyStyles(shell, true);
		shell.open();
		return folder;
	}

	@Test
	void testSelectedTabReceivesStyledSelectionForeground() {
		// :selected for CTabItem maps to CTabFolder#getSelectionForeground(),
		// not the per-item CTabItem#getForeground(). The matched declaration
		// is intentionally observable on the parent folder.
		CTabFolder folder = createFolderWithTwoItems("CTabItem:selected { color: #FF0000 }");
		spinEventLoop();

		assertEquals(RED, folder.getSelectionForeground().getRGB());
	}

	@Test
	void testSelectionListenerReappliesStylesOnSelectionEvent() {
		CTabFolder folder = createFolderWithTwoItems(
				"CTabItem { color: #0000FF }\n" + "CTabItem:selected { color: #FF0000 }");
		spinEventLoop();
		assertEquals(RED, folder.getSelectionForeground().getRGB());

		// Switch the selection. CTabFolder#setSelection on its own does not
		// fire the SWT selection listener, so we explicitly dispatch the
		// SWT.Selection event the way an end-user click would. This
		// exercises CTabFolderElement#selectionListener which invokes
		// applyStyles(folder, true) for us.
		CTabItem item1 = folder.getItem(1);
		folder.setSelection(item1);
		Event event = new Event();
		event.widget = folder;
		event.item = item1;
		folder.notifyListeners(SWT.Selection, event);
		spinEventLoop();

		// The folder's selection-foreground stays red because the rule still
		// matches whichever item is currently selected.
		assertEquals(RED, folder.getSelectionForeground().getRGB());
		// Non-selected items inherit the plain CTabItem rule.
		assertEquals(new RGB(0, 0, 255), folder.getForeground().getRGB());
	}

	@Test
	void testNonSelectedRuleColorIsNotRed() {
		CTabFolder folder = createFolderWithTwoItems(
				"CTabItem { color: #0000FF }\n" + "CTabItem:selected { color: #FF0000 }");
		spinEventLoop();

		// The non-selected foreground (plain CTabItem rule) is blue, not red.
		assertNotEquals(RED, folder.getForeground().getRGB());
		assertEquals(new RGB(0, 0, 255), folder.getForeground().getRGB());
	}
}
