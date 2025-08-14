/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.junit.Test;

public class Bug78470Test {

	public static class MyPerspective implements IPerspectiveFactory {
		public static String ID = "org.eclipse.ui.tests.internal.Bug78470Test.MyPerspective";

		@Override
		public void createInitialLayout(IPageLayout layout) {
			String editorArea = layout.getEditorArea();
			IFolderLayout folder1 = layout.createFolder("folder1",
					IPageLayout.LEFT, .75f, editorArea);
			// the order is important
			folder1.addPlaceholder(MyViewPart.ID2);
			folder1.addView(MyViewPart.ID);
			folder1.addPlaceholder(MyViewPart.ID3);
		}
	}

	public static class MyViewPart extends ViewPart {
		public static String ID = "org.eclipse.ui.tests.internal.Bug78470Test.MyViewPart";

		public static String ID2 = ID + "2";
		public static String ID3 = ID + "3";

		@Override
		public void createPartControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(getSite().getId());
		}

		@Override
		public void setFocus() {
			// nothing to do
		}
	}

	boolean partVisibleExecuted = false;

	@Test
	public void test78470() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		activeWorkbenchWindow.getPartService().addPartListener(
				new IPartListener2() {
					@Override
					public void partActivated(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partBroughtToTop(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partClosed(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partDeactivated(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partOpened(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partHidden(IWorkbenchPartReference partRef) {
					}
					@Override
					public void partVisible(IWorkbenchPartReference partRef) {
						if (partRef.getId().equals(MyViewPart.ID)) {
							partVisibleExecuted = true;
							try {
								activePage.showView(MyViewPart.ID2);
								activePage.showView(MyViewPart.ID3);
							} catch(Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					@Override
					public void partInputChanged(IWorkbenchPartReference partRef) {
					}
				});
		workbench.showPerspective(MyPerspective.ID, activeWorkbenchWindow);
		processEvents();
		Thread.sleep(2000);
		assertTrue("view was not made visible", partVisibleExecuted);
		assertNotNull(activePage.findView(MyViewPart.ID2));
		assertNotNull(activePage.findView(MyViewPart.ID3));
	}

}
