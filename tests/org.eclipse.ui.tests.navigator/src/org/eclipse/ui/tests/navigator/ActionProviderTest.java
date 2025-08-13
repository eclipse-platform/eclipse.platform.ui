/*******************************************************************************
 * Copyright (c) 2009, 2023 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestExtensionTreeData;
import org.junit.Test;

public class ActionProviderTest extends NavigatorTestBase {

	private static final boolean SLEEP_LONG = false;


	public ActionProviderTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	@Test
	public void testBasicModel() throws Exception {
		waitForModelObjects();

		IStructuredSelection sel;
		TestExtensionTreeData data = TestContentProvider._modelRoot;

		sel = new StructuredSelection(data);
		_viewer.setSelection(sel);
		verifyMenu(sel, ACTION_NESTED);

		data = data.getChildren()[0];
		sel = new StructuredSelection(data);
		_viewer.setSelection(sel);
		verifyMenu(sel, ACTION_NESTED);

		data = data.getChildren()[0];
		sel = new StructuredSelection(data);
		_viewer.setSelection(sel);
		verifyMenu(sel, ACTION_NESTED);

	}

	@Test
	public void testOverride() throws CoreException {
		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_ACTION_PROVIDER }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_ACTION_PROVIDER }, false);

		IMenuManager mm;

		refreshViewer();

		IStructuredSelection sel = new StructuredSelection(((IContainer) _p2.members()[1]).members()[0]);
		_viewer.setSelection(sel);

		if (SLEEP_LONG)
			DisplayHelper.sleep(10000000);

		// Overridden
		assertNull(verifyMenu(sel, "Rena&me"));
		// Overrides
		mm = (IMenuManager) verifyMenu(sel, "CN Test Menu");
		assertNotNull(mm);
		// Should have the two dependent items
		assertEquals(4, mm.getItems().length);

		_contentService.getActivationService().deactivateExtensions(
				new String[] { TEST_CONTENT_ACTION_PROVIDER }, false);

		_viewer.setSelection(sel);

		// Overridden
		assertNotNull(verifyMenu(sel, "Rena&me"));
		// Overrides
		mm = (IMenuManager) verifyMenu(sel, "CN Test Menu");
		assertNull(mm);

	}

	@Test
	public void testAppearsBefore() throws CoreException {

		IStructuredSelection sel = new StructuredSelection(((IContainer) _p2.members()[1]).members()[0]);
		_viewer.setSelection(sel);

		MenuManager mm = new MenuManager();
		_actionService.setContext(new ActionContext(sel));
		_actionService.fillContextMenu(mm);

		List<String> priorityItems = new ArrayList<>();

		IContributionItem[] items = mm.getItems();
		for (IContributionItem item : items) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem aci = (ActionContributionItem) item;
				if (DEBUG) {
					System.out.println("action text: " + aci.getAction().getText());
				}
				if (aci.getAction().getText().startsWith(TEST_ACTION_PROVIDER_PRIORITY))
					priorityItems.add(aci.getAction().getText());
			}
		}

		if (SLEEP_LONG)
			DisplayHelper.sleep(10000000);

		assertEquals(4, priorityItems.size());
		assertEquals(TEST_ACTION_PROVIDER_PRIORITY + "2", priorityItems.get(0));
		assertEquals(TEST_ACTION_PROVIDER_PRIORITY + "4", priorityItems.get(1));
		assertEquals(TEST_ACTION_PROVIDER_PRIORITY + "1", priorityItems.get(2));
		assertEquals(TEST_ACTION_PROVIDER_PRIORITY + "3", priorityItems.get(3));
	}

}
