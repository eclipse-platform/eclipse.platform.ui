/*******************************************************************************
 * Copyright (c) 2017, 2022 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.QuickAccessContents;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the content of quick access for given requests
 *
 * @since 3.14
 */
public class ContentMatchesTest {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static final int TIMEOUT = 3000;
	private QuickAccessDialog dialog;
	private QuickAccessContents quickAccessContents;

	@Before
	public void doSetUp() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		dialog = new QuickAccessDialog(window, null);
		quickAccessContents = dialog.getQuickAccessContents();
		dialog.open();
	}

	@After
	public void doTearDown() throws Exception {
		dialog.close();
	}

	@Test
	public void testFindPreferenceByKeyword() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("whitespace");
		final Table table = quickAccessContents.getTable();
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> getAllEntries(table).stream()
				.filter(x -> x.contains("Text Editors")).toList().isEmpty()));
	}

	@Test
	public void testRequestWithWhitespace() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("text white");
		final Table table = quickAccessContents.getTable();
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT, () -> getAllEntries(table).stream()
				.filter(x -> x.contains("Text Editors")).toList().isEmpty()));
	}

	@Test
	public void testFindCommandByDescription() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("rename ltk");
		final Table table = quickAccessContents.getTable();
		assertTrue(DisplayHelper.waitForCondition(table.getDisplay(), TIMEOUT,
				() -> getAllEntries(table).stream()
						.filter(x -> x.contains("Rename the selected resource and notify LTK participants."))
						.toList().isEmpty()));
	}

	static List<String> getAllEntries(Table table) {
		final int nbColumns = table.getColumnCount();
		return Arrays.stream(table.getItems()).map(item -> {
			StringBuilder res = new StringBuilder("");
			for (int i = 0; i < nbColumns; i++) {
				res.append(item.getText(i));
				res.append(" | ");
			}
			return res.toString();
		}).toList();
	}

}
