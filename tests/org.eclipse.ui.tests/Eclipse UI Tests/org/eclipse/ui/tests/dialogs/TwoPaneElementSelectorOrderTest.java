/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the order of the entries of {@link TwoPaneElementSelector}. The sorting
 * of the lists must be stable, so elements that are folded into one entry of
 * the upper list keep their order when the comparator of the lower list treats
 * all of them as equal. Otherwise, both lists are sorted ignoring the case.
 */
public class TwoPaneElementSelectorOrderTest {

	/** An element with the label of the upper list and the one of the lower list. */
	private record Entry(String label, String qualifier) {
	}

	private Shell parent;

	private TwoPaneElementSelector dialog;

	@AfterEach
	public void doTearDown() {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
		if (parent != null) {
			parent.dispose();
			parent = null;
		}
	}

	/**
	 * Three elements with the same label are folded into one entry of the upper
	 * list. The lower list must show them in the order in which they were passed to
	 * the dialog, whatever the position of that group among the other elements is.
	 */
	@Test
	public void testLowerListKeepsOrderOfFoldedElements() {
		List<Entry> others = List.of(new Entry("component.xml", "component.xml"), new Entry("plugin.xml", "plugin.xml"),
				new Entry("settings.ini", "settings.ini"), new Entry("settings.xml", "settings.xml"));
		List<Entry> folded = List.of(new Entry("KeyUser.java (3 matches)", "Line 12"),
				new Entry("KeyUser.java (3 matches)", "Line 28"), new Entry("KeyUser.java (3 matches)", "Line 29"));

		for (int position = 0; position <= others.size(); position++) {
			List<Entry> entries = new ArrayList<>(others);
			entries.addAll(position, folded);

			// The same as the Ctrl+Click dialog of the properties file editor: keep the order
			openDialog(entries, (_, _) -> 0, folded.get(0));
			List<String> lowerList = readTable(findTable(dialog.getShell(), false), folded.size());

			assertEquals(Arrays.asList("Line 12", "Line 28", "Line 29"), lowerList,
					"Wrong order of the lower list when the group is at index " + position);
			doTearDown();
		}
	}

	/**
	 * Without a comparator for the lower list, both lists are sorted ignoring the
	 * case, and elements with the same label are folded into one entry.
	 */
	@Test
	public void testListsAreSortedIgnoringCase() {
		List<Entry> others = List.of(new Entry("beta", "b"), new Entry("Alpha", "a"), new Entry("gamma", "g"),
				new Entry("alpha2", "a2"));
		List<Entry> folded = List.of(new Entry("delta", "b"), new Entry("delta", "C"), new Entry("delta", "a"));

		for (int position = 0; position <= others.size(); position++) {
			List<Entry> entries = new ArrayList<>(others);
			entries.addAll(position, folded);

			openDialog(entries, null, folded.get(0));
			List<String> lowerList = readTable(findTable(dialog.getShell(), false), folded.size());
			List<String> upperList = readTable(findTable(dialog.getShell(), true), 5);

			assertEquals(Arrays.asList("Alpha", "alpha2", "beta", "delta", "gamma"), upperList,
					"Wrong order of the upper list when the group is at index " + position);
			assertEquals(Arrays.asList("a", "b", "C"), lowerList,
					"Wrong order of the lower list when the group is at index " + position);
			doTearDown();
		}
	}

	/**
	 * Opens the dialog with the given elements.
	 *
	 * @param lowerComparator comparator of the lower list, or <code>null</code> for
	 * the default
	 * @param selected the element to select initially
	 */
	private void openDialog(List<Entry> entries, Comparator<Object> lowerComparator, Entry selected) {
		parent = new Shell(Display.getDefault());
		dialog = new TwoPaneElementSelector(parent, new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry) element).label();
			}
		}, new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry) element).qualifier();
			}
		});
		if (lowerComparator != null) {
			dialog.setLowerListComparator(lowerComparator);
		}
		dialog.setElements(entries.toArray());
		// Without an initial selection, the first row of the upper list is selected, and
		// this is not the folded group. Select the group, as the user does.
		dialog.setInitialSelections(selected);
		dialog.setBlockOnOpen(false);
		dialog.open();
	}

	/** Waits until the table has the expected number of rows and returns their texts. */
	private static List<String> readTable(Table table, int expectedCount) {
		assertNotNull(table, "The list was not found");
		// The lists are filled by a UI job, so wait until the group is selected
		DisplayHelper.waitAndAssertCondition(Display.getDefault(),
				() -> assertEquals(expectedCount, table.getItemCount(), "The list was not filled"));

		List<String> result = new ArrayList<>();
		for (TableItem item : table.getItems()) {
			result.add(item.getText());
		}
		return result;
	}

	/**
	 * Returns the table of the upper list, which is part of the {@link FilteredList},
	 * or the table of the lower list, which is the only other table.
	 */
	private static Table findTable(Control control, boolean upper) {
		if (control instanceof Table table && (table.getParent() instanceof FilteredList) == upper) {
			return table;
		}
		if (control instanceof Composite composite) {
			for (Control child : composite.getChildren()) {
				Table found = findTable(child, upper);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
