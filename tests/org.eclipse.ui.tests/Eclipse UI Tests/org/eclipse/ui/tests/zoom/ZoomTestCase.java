/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.zoom;
import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

public class ZoomTestCase extends UITestCase {
	protected static final String view1Id = IPageLayout.ID_RES_NAV;
	protected static final String view2Id = IPageLayout.ID_OUTLINE;
	
	protected WorkbenchWindow window;
	protected WorkbenchPage page;
	protected IProject project;
	protected IFile file1, file2;
	protected IEditorPart editor1, editor2;

	public ZoomTestCase(String name) {
		super(name);
	}
	
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = (WorkbenchWindow)openTestWindow();
		page = (WorkbenchPage)window.getActivePage();
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setDefault(IPreferenceConstants.OPEN_VIEW_MODE, IPreferenceConstants.OVM_FAST);
		try {
			project = FileUtil.createProject("IEditorPartTest"); //$NON-NLS-1$
			file1 = FileUtil.createFile("Test1.txt", project); //$NON-NLS-1$
			file2 = FileUtil.createFile("Test2.txt", project); //$NON-NLS-1$
			editor1 = page.openEditor(new FileEditorInput(file1), MockEditorPart.ID1);
			editor2 = page.openEditor(new FileEditorInput(file2), MockEditorPart.ID2);
		} catch(PartInitException e) {
		} catch(CoreException e) {
		}
	}
	
	// opens editor2 in a seperate workbook
	protected void differentWorkbookSetUp() {
		EditorPane pane = (EditorPane)((PartSite)editor1.getSite()).getPane();
		EditorSashContainer area = pane.getWorkbook().getEditorArea();
		EditorStack workbook = EditorStack.newEditorWorkbook(area, page);

		area.add(workbook, IPageLayout.BOTTOM, (float) 0.5, pane.getWorkbook());
		workbook.add(pane);
		
		EditorPane pane1 = (EditorPane)((EditorSite)editor1.getSite()).getPane();
		EditorPane pane2 = (EditorPane)((EditorSite)editor2.getSite()).getPane();
		Assert.assertTrue(!pane1.getWorkbook().equals(pane2.getWorkbook()));
	}
	
	// zooms the given part
	protected void zoom(IWorkbenchPart part) {
		if (part == null)
			throw new NullPointerException();
		page.toggleZoom(((PartSite) part.getSite()).getPane().getPartReference());
		Assert.assertTrue(page.isZoomed());		
	}
	// open the given file in an editor
	protected void openEditor(IFile file) {
		try {
			if(file == file1) 
				editor1 = IDE.openEditor(page, file, true);
			if(file == file2)
				editor2 = IDE.openEditor(page, file, true);
		} catch(PartInitException e) {
		}			
	}
	// show the given view as a regular view
	protected IViewPart showRegularView(String id) {
		try {
			IViewPart view = page.showView(id);
			IViewReference ref = (IViewReference)page.getReference(view);
			if(page.isFastView(ref))
				page.removeFastView(ref);
			return view;
		} catch(PartInitException e) {
		}
		return null;
	}
	// show the given view
	protected IViewPart showFastView(String id) {
		try {
			IViewPart view = page.showView(id);
			IViewReference ref = (IViewReference)page.getReference(view);
			Assert.assertTrue(page.isFastView(ref));
			return view;
		} catch(PartInitException e) {
		}
		return null;
	}
	// returns whether this part is zoomed
	protected boolean isZoomed(IWorkbenchPart part) {
		PartSite site = (PartSite)part.getSite();
		PartPane pane = site.getPane();
		return pane.isZoomed();
	}
	// returns true if the page is not zoomed, false otherwise
	protected boolean noZoom() {
		if (page.isZoomed())
			return false;
		return true;	
	}
}
