package org.eclipse.ui.tests.zoom;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
**********************************************************************/
import junit.framework.Assert;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

public class ZoomTestCase extends UITestCase {
	private static final String projectName = "Test";
	private static final String file1Name = "TestFile1";
	private static final String file2Name = "TestFile2";
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
	
	public void setUp() {
		window = (WorkbenchWindow)openTestWindow();
		page = (WorkbenchPage)window.getActivePage();
		try {
			project = FileUtil.createProject("IEditorPartTest");
			file1 = FileUtil.createFile("Test1.java", project);
			file2 = FileUtil.createFile("Test2.java", project);
			editor1 = page.openEditor(file1, MockEditorPart.ID1);
			editor2 = page.openEditor(file2, MockEditorPart.ID2);
		} catch(PartInitException e) {
		} catch(CoreException e) {
		}
	}
	
	// opens editor2 in a seperate workbook
	protected void differentWorkbookSetUp() {
		EditorPane pane = (EditorPane)((PartSite)editor1.getSite()).getPane();
		EditorArea area = pane.getWorkbook().getEditorArea();
		EditorWorkbook workbook = new EditorWorkbook(area);

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
		page.toggleZoom(part);
		Assert.assertTrue(page.isZoomed());		
	}
	// open the given file in an editor
	protected void openEditor(IFile file) {
		try {
			if(file == file1) 
				editor1 = page.openEditor(file);
			if(file == file2)
				editor2 = page.openEditor(file);
		} catch(PartInitException e) {
		}			
	}
	// show the given view as a regular view
	protected IViewPart showRegularView(String id) {
		try {
			IViewPart view = page.showView(id);
			if(page.isFastView(view))
				page.removeFastView(view);
			return view;
		} catch(PartInitException e) {
		}	;	
		return null;
	}
	// show the given view
	protected IViewPart showFastView(String id) {
		try {
			IViewPart view = page.showView(id);
			Assert.assertTrue(page.isFastView(view));
			return view;
		} catch(PartInitException e) {
		}	;	
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
