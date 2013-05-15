/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

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
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class Bug264804Test extends UITestCase {

	public Bug264804Test(String testName) {
		super(testName);
	}

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

	/**
	 * @param id
	 * @param items
	 */
	private void find(String id, IContributionItem[] items) throws Exception {
		for (int i = 0; i < items.length; i++) {
			if (id.equals(items[i].getId())) {
				assertTrue("Should be visible", items[i].isVisible());
				return;
			}
		}
		fail("Could not find " + id);
	}

}
