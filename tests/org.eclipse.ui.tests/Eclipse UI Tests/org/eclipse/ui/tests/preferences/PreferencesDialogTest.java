/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.ui.tests.preferences;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.tests.preferences.SamplePreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Added as a result of the bug 226547:
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=226547
 *
 * @since 3.5
 */
public class PreferencesDialogTest {

	static ImageDescriptor descriptor = ResourceLocator
			.imageDescriptorFromBundle("org.eclipse.ui.tests", "icons/anything.gif").orElse(null);

	public Shell shell;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();
	}

	@After
	public void tearDown() throws Exception {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
	}

	/**
	 * Test preference dialog with a custom manager, custom nodes.
	 */
	@Test
	public void testCustomManager() {
		PreferenceManager manager = new PreferenceManager();

		IPreferencePage page1 = new SamplePreferencePage("Top", "First Sample");
		IPreferenceNode node1 = new PreferenceNode("Top", page1);
		manager.addToRoot(node1);

		IPreferencePage page2 = new SamplePreferencePage("Sub", "Second Sample");
		IPreferenceNode node2 = new PreferenceNode("Sub", page2);
		node1.add(node2);

		PreferenceDialog dialog = null;
		try {
			dialog = new PreferenceDialog(shell, manager);
			dialog.setBlockOnOpen(false);

			// Check that we can create a dialog with custom preference manager.
			// Should be no exceptions.
			dialog.open();
		} finally {
			if (dialog != null) {
				dialog.close();
			}
		}
	}

	/**
	 * Test preference dialog with a Workbench manager adding custom nodes to it.
	 */
	@Test
	public void testMixedNodes() {
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();

		IPreferencePage page1 = new SamplePreferencePage("Top", "First Sample");
		IPreferenceNode node1 = new PreferenceNode("Top", page1);
		manager.addToRoot(node1);

		IPreferencePage page2 = new SamplePreferencePage("Sub", "Second Sample");
		IPreferenceNode node2 = new PreferenceNode("Sub", page2);
		manager.addToRoot(node2);

		PreferenceDialog dialog = null;
		try {
			dialog = new PreferenceDialog(shell, manager);
			dialog.setBlockOnOpen(false);

			// Check that we can create a dialog with custom preference manager.
			// Should be no exceptions.
			dialog.open();
		} finally {
			if (dialog != null) {
				dialog.close();
			}
			manager.remove(node2);
			manager.remove(node1);
		}
	}

	/**
	 * Test preference dialog with a custom manager, custom nodes, this time
	 * using an icon.
	 */
	@Test
	public void testWithIcons() {
		PreferenceManager manager = new PreferenceManager();

		IPreferencePage page1 = new SamplePreferencePage("Zzz", "First Sample");
		PreferenceNode node1 = new PreferenceNode("one", "Zzz", descriptor,
				SamplePreferencePage.class.getName());
		node1.setPage(page1);
		manager.addToRoot(node1);

		IPreferencePage page2 = new SamplePreferencePage("Aaa", "Second Sample");
		PreferenceNode node2 = new PreferenceNode("two", "Aaa", descriptor,
				SamplePreferencePage.class.getName());
		node2.setPage(page2);
		manager.addToRoot(node2);

		PreferenceDialog dialog = null;
		try {
			dialog = new PreferenceDialog(shell, manager);
			dialog.setBlockOnOpen(false);

			// check that we can create a dialog with custom preference manager
			// with
			// pages with icons
			dialog.open();
		} finally {
			if (dialog != null) {
				dialog.close();
			}
		}
	}

	/**
	 * To test sorting elements we need to access internal class
	 * FilteredPreferenceDialog. It is available  from the IWorkbench#getPreferenceManager(),
	 * but using it would bring all workbench pages into this test which could
	 * impact sorting.
	 */
	@Test
	public void testWithSorting() {
		PreferenceManager manager = new PreferenceManager();

		IPreferencePage page1 = new SamplePreferencePage("Zzz", "First Sample");
		IPreferenceNode node1 = new PreferenceNode("abc", page1);
		manager.addToRoot(node1);
		IPreferencePage page2 = new SamplePreferencePage("Aaa", "Second Sample");
		IPreferenceNode node2 = new PreferenceNode("www", page2);
		manager.addToRoot(node2);

		FilteredPreferenceDialog dialog = null;
		try {
			dialog = new FilteredPreferenceDialog(shell, manager) {};
			dialog.setBlockOnOpen(false);

			// check that we can create a dialog with custom preference manager
			dialog.open();

			// The page with title "Aaa" should be the first one despite being
			// added second
			assertEquals(page2, dialog.getCurrentPage());

			// Also, "Aaa" should be the first tree item
			TreeItem item = dialog.getTreeViewer().getTree().getItem(0);
			assertEquals("Aaa", item.getText());
		} finally {
			if (dialog != null) {
				dialog.close();
			}
		}
	}
}
