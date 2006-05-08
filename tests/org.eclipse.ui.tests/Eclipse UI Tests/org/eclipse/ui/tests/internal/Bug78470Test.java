/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.internal;

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
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class Bug78470Test extends UITestCase {

	public static class MyPerspective implements IPerspectiveFactory {
		public static String ID = "org.eclipse.ui.tests.internal.Bug78470Test.MyPerspective";

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

		public void createPartControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(getSite().getId());
		}

		public void setFocus() {
			// nothing to do
		}
	}

	public Bug78470Test(String testName) {
		super(testName);
	}

	boolean partVisibleExecuted = false;

	public void test78470() throws Exception {
		IWorkbench workbench = getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		activeWorkbenchWindow.getPartService().addPartListener(
				new IPartListener2() {
					public void partActivated(IWorkbenchPartReference partRef) {
					}
					public void partBroughtToTop(IWorkbenchPartReference partRef) {
					}
					public void partClosed(IWorkbenchPartReference partRef) {
					}
					public void partDeactivated(IWorkbenchPartReference partRef) {
					}
					public void partOpened(IWorkbenchPartReference partRef) {
					}
					public void partHidden(IWorkbenchPartReference partRef) {
					}
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
