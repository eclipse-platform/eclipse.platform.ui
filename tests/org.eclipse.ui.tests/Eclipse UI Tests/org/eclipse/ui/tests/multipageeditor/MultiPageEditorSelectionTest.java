/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.properties.PropertySheet;

public class MultiPageEditorSelectionTest extends UITestCase {

	private static final String MTEST01_FILE = "mtest01.multivar";

	private static final String PROJECT_NAME = "MultiPageEditorSelction";

	public MultiPageEditorSelectionTest(String testName) {
		super(testName);
	}

	@Override
	protected void doTearDown() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(PROJECT_NAME);
		if (testProject.exists()) {
			testProject.delete(true, true, null);
		}
		super.doTearDown();
	}

	public void testPostSelection() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openEditor(window, MTEST01_FILE);
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		assertTrue(provider instanceof IPostSelectionProvider);

		final boolean[] called = { false };
		IPostSelectionProvider postSelectionProvider = (IPostSelectionProvider) provider;
		postSelectionProvider
				.addPostSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						called[0] = true;
					}
				});

		((MultiPageResourceEditor) part).updateSelection();
		assertTrue(called[0]);
	}

	public void testPropertiesView() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openEditor(window, MTEST01_FILE);

		PropertySheet propertiewView = (PropertySheet) window.getActivePage()
				.showView(IPageLayout.ID_PROP_SHEET);

		window.getActivePage().activate(part);

		Tree tree = (Tree) propertiewView.getCurrentPage().getControl();
		assertEquals(0, tree.getItemCount());

		MultiPageResourceEditor editor = (MultiPageResourceEditor) part;
		editor.updateSelection();
		assertFalse(tree.getItemCount() == 0);
	}

	public void testPropertiesView2() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openEditor(window, MTEST01_FILE);

		window.getActivePage().activate(part);
		MultiPageResourceEditor editor = (MultiPageResourceEditor) part;
		editor.updateSelection();

		PropertySheet propertiewView = (PropertySheet) window.getActivePage().showView(IPageLayout.ID_PROP_SHEET);
		processUiEvents();

		Tree tree = (Tree) propertiewView.getCurrentPage().getControl();

		assertFalse(tree.getItemCount() == 0);
	}

	private IEditorPart openEditor(IWorkbenchWindow window, String filename)
			throws CoreException, PartInitException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject testProject = workspace.getRoot().getProject(PROJECT_NAME);
		if (!testProject.exists()) {
			testProject.create(null);
		}
		testProject.open(null);

		IFile multiFile = testProject.getFile(filename);
		if (!multiFile.exists()) {
			multiFile.create(new ByteArrayInputStream("".getBytes()), true,
					null);
		}

		IWorkbenchPage page = window.getActivePage();
		IEditorPart part = IDE.openEditor(page, multiFile,
				MultiPageResourceEditor.EDITOR_ID);
		assertTrue(part instanceof MultiPageResourceEditor);
		return part;
	}

	private void processUiEvents() {
		while (fWorkbench.getDisplay().readAndDispatch()) {
			;
		}
	}
}
