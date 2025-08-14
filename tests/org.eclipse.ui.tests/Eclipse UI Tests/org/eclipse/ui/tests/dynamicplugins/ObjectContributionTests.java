/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.util.HashSet;
import java.util.Random;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ObjectActionContributorManager;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class ObjectContributionTests extends DynamicTestCase {

	private static final String GROUP_ID = "#OC";
	private static final String OBJECT_ACTION_ID = "org.eclipse.newOC1";
	private static final String VIEWER_ACTION_ID = "org.eclipse.newOC2";

	public ObjectContributionTests() {
		super(ObjectContributionTests.class.getSimpleName());
	}

	@Override
	protected String getExtensionId() {
		return "newOC1.testDynamicOCAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_POPUP_MENU;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newOC1";
	}

	@Test
	public void testViewerContributions() {
		IWorkbenchWindow window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		IWorkbenchPart part = window.getActivePage().getActivePart();
		MenuManager menu = new MenuManager();
		resetViewerMenu(menu);
		ISelectionProvider provider = new ISelectionProvider() {

			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {

			}

			@Override
			public ISelection getSelection() {
				return new StructuredSelection(new Random());
			}

			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			@Override
			public void setSelection(ISelection selection) {
			}

		};

		PopupMenuExtender extender = new PopupMenuExtender(GROUP_ID, menu, provider, part, ((PartSite)part.getSite()).getContext());
		extender.menuAboutToShow(menu);

		assertNull(menu.find(VIEWER_ACTION_ID));
		resetViewerMenu(menu);
		getBundle();

		extender.menuAboutToShow(menu);
		assertNotNull(menu.find(VIEWER_ACTION_ID));
		resetViewerMenu(menu);
		removeBundle();

		extender.menuAboutToShow(menu);
		assertNull(menu.find(VIEWER_ACTION_ID));

		extender.dispose();
	}

	private void resetViewerMenu(MenuManager menu) {
		menu.removeAll();
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(GROUP_ID));
	}

	@Test
	public void testObjectContribtions() {
		IWorkbenchWindow window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		IWorkbenchPart part = window.getActivePage().getActivePart();
		ObjectActionContributorManager manager = ObjectActionContributorManager.getManager();
		IMenuManager menu = new MenuManager();
		ISelectionProvider provider = new ISelectionProvider() {

			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {

			}

			@Override
			public ISelection getSelection() {
				return new StructuredSelection(new Random());
			}

			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			@Override
			public void setSelection(ISelection selection) {
			}

		};

		manager.contributeObjectActions(part, menu, provider, new HashSet<>());
		assertNull(menu.find(OBJECT_ACTION_ID));
		menu.removeAll();
		getBundle();

		manager.contributeObjectActions(part, menu, provider, new HashSet<>());
		assertNotNull(menu.find(OBJECT_ACTION_ID));
		menu.removeAll();
		removeBundle();

		manager.contributeObjectActions(part, menu, provider, new HashSet<>());
		assertNull(menu.find(OBJECT_ACTION_ID));
		menu.removeAll();
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.MockObjectActionDelegate";
	}
}
