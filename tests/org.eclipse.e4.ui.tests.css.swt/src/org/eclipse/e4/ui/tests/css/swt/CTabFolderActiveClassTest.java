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

import static org.eclipse.e4.ui.tests.css.swt.CssSwtEngine.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Locks in how {@code CTabFolder.active { ... }} rules are matched based on
 * the CSS class set via {@link WidgetElement#setCSSClass(org.eclipse.swt.widgets.Widget, String)}.
 */
public class CTabFolderActiveClassTest {

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	private Shell shell;

	@AfterEach
	public void tearDown() {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}
	}

	private CTabFolder createFolder() {
		Display display = css.getDisplay();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());

		CTabFolder folder = new CTabFolder(shell, SWT.NONE);
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("Item 0");
		folder.setSelection(0);
		return folder;
	}

	@Test
	void testActiveClassAppliesStyle() {
		CTabFolder folder = createFolder();
		WidgetElement.setCSSClass(folder, "active");

		CSSEngine engine = css.createEngine("CTabFolder.active { background-color: #FF0000 }");
		engine.applyStyles(shell, true);

		assertEquals(RED, folder.getBackground().getRGB());
	}

	@Test
	void testWithoutActiveClassRuleDoesNotApply() {
		CTabFolder folder = createFolder();
		// no setCSSClass call

		CSSEngine engine = css.createEngine("CTabFolder.active { background-color: #FF0000 }");
		engine.applyStyles(shell, true);

		assertNotEquals(RED, folder.getBackground().getRGB());
	}

	@Test
	void testClearingActiveClassDoesNotRevertBackground() {
		CTabFolder folder = createFolder();
		WidgetElement.setCSSClass(folder, "active");

		CSSEngine engine = css.createEngine("CTabFolder.active { background-color: #FF0000 }");
		engine.applyStyles(shell, true);
		assertEquals(RED, folder.getBackground().getRGB());

		// Surprise: clearing the CSS class and reapplying does NOT undo the
		// previously painted background. Once CTabFolder#setBackground has
		// been called by the handler, the folder retains that color even
		// though the .active rule no longer matches. Pin this real engine
		// behaviour so a future change that adds proper revert semantics
		// shows up as a test failure.
		WidgetElement.setCSSClass(folder, null);
		engine.applyStyles(shell, true);

		assertEquals(RED, folder.getBackground().getRGB());
	}
}
