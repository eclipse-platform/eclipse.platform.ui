/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
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

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.QuickAccessContents;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.hamcrest.Matchers;

/**
 * Tests the content of quick access for given requests
 *
 * @since 3.14
 */
public class ContentMatchesTest extends UITestCase {

	private QuickAccessDialog dialog;
	private QuickAccessContents quickAccessContents;

	/**
	 * @param testName
	 */
	public ContentMatchesTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkbenchWindow window = openTestWindow();
		dialog = new QuickAccessDialog(window, null);
		quickAccessContents = dialog.getQuickAccessContents();
		dialog.open();
	}

	@Override
	protected void doTearDown() throws Exception {
		dialog.close();
	}

	public void testFindPreferenceByKeyword() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("whitespace");
		final Table table = quickAccessContents.getTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertTrue(Matchers.hasItems(Matchers.containsString("Text Editors")).matches(allEntries));
	}

	public void testRequestWithWhitespace() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("text white");
		final Table table = quickAccessContents.getTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertTrue(Matchers.hasItems(Matchers.containsString("Text Editors")).matches(allEntries));
	}

	public void testFindCommandByDescription() throws Exception {
		Text text = quickAccessContents.getFilterText();
		text.setText("rename ltk");
		final Table table = quickAccessContents.getTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertThat(allEntries, Matchers
				.hasItems(Matchers.containsString("Rename the selected resource and notify LTK participants.")));
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
		}).collect(Collectors.toList());
	}

}