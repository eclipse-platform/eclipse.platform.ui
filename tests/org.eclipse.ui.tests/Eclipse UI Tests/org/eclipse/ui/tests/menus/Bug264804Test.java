/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.tests.api.ListElement;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
public class Bug264804Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Test
	public void testPopup() throws Exception {
		IWorkbenchWindow window = openTestWindow();

		ListView part = (ListView) window.getActivePage().showView(
				"org.eclipse.ui.tests.api.IActionFilterTest1");
		ListElement red = new ListElement("red");
		ListElement blue = new ListElement("blue");
		ListElement redTrue = new ListElement("red", true);
		part.addElement(red);
		part.addElement(blue);
		part.addElement(redTrue);

		assertNotNull(part);
		MenuManager manager = new MenuManager();

		final ISelectionProvider prov = part.getSelectionProvider();
		prov.setSelection(new StructuredSelection(blue));
		PopupMenuExtender popupMenuExtender = null;
		try {
			popupMenuExtender = new PopupMenuExtender(
					"org.eclipse.ui.tests.Bug264804", manager, prov, part,
					((PartSite)part.getSite()).getContext(), false);

			Menu contextMenu = manager.createContextMenu(window.getShell());
			// contextMenu.setVisible(true);
			Event e = new Event();
			e.widget = contextMenu;
			processEvents();
			contextMenu.notifyListeners(SWT.Show, e);

			find("org.eclipse.ui.file.close", manager.getItems());

			// This is our error case, we process the Hide event and then
			// process the Show event, and then the async execs are allowed
			// to run
			contextMenu.notifyListeners(SWT.Hide, e);
			contextMenu.notifyListeners(SWT.Show, e);
			processEvents();

			find("org.eclipse.ui.file.close", manager.getItems());
		} finally {
			popupMenuExtender.dispose();
		}
	}

	private void find(String id, IContributionItem[] items) throws Exception {
		for (IContributionItem item : items) {
			if (id.equals(item.getId())) {
				assertTrue("Should be visible", item.isVisible());
				return;
			}
		}
		fail("Could not find " + id);
	}

}
