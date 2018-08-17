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

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.SearchField;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.hamcrest.Matchers;

/**
 * Tests the content of quick access for given requests
 *
 * @since 3.14
 */
public class ContentMatchesTest extends UITestCase {

	private SearchField searchField;

	/**
	 * @param testName
	 */
	public ContentMatchesTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		openTestWindow();
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWorkbench()
				.getActiveWorkbenchWindow();
		MWindow window = workbenchWindow.getModel();
		EModelService modelService = window.getContext().get(
				EModelService.class);
		MToolControl control = (MToolControl) modelService.find(
				"SearchField", window); //$NON-NLS-1$
		searchField = (SearchField) control.getObject();
		assertNotNull("Search Field must exist", searchField);
	}

	@Override
	protected void doTearDown() throws Exception {
		Text text = searchField.getQuickAccessSearchText();
		if (text != null){
			text.setText("");
		}
		Shell shell = searchField.getQuickAccessShell();
		if (shell != null){
			shell.setVisible(false);
		}
	}

	public void testFindPreferenceByKeyword() throws Exception {
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		Text text = searchField.getQuickAccessSearchText();
		text.setText("whitespace");
		final Table table = searchField.getQuickAccessTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertTrue(Matchers.hasItems(Matchers.containsString("Text Editors")).matches(allEntries));
	}

	public void testRequestWithWhitespace() throws Exception {
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		Text text = searchField.getQuickAccessSearchText();
		text.setText("text white");
		final Table table = searchField.getQuickAccessTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertTrue(Matchers.hasItems(Matchers.containsString("Text Editors")).matches(allEntries));
	}

	public void testFindCommandByDescription() throws Exception {
		Shell shell = searchField.getQuickAccessShell();
		assertFalse("Quick access dialog should not be visible yet", shell.isVisible());
		Text text = searchField.getQuickAccessSearchText();
		text.setText("rename ltk");
		final Table table = searchField.getQuickAccessTable();
		processEventsUntil(() -> table.getItemCount() > 1, 200);
		List<String> allEntries = getAllEntries(table);
		assertThat(allEntries, Matchers
				.hasItems(Matchers.containsString("Rename the selected resource and notify LTK participants.")));
	}

	private List<String> getAllEntries(Table table) {
		final int nbColumns = table.getColumnCount();
		return Arrays.stream(table.getItems()).map(item -> {
			String res = "";
			for (int i = 0; i < nbColumns; i++) {
				res += item.getText(i);
				res += " | ";
			}
			return res;
		}).collect(Collectors.toList());
	}

}
