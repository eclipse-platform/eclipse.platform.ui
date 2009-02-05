/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - Initial implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.SWTEventHelper;
import org.eclipse.ui.tests.navigator.extension.TestDragAssistant;

public class DnDTest extends NavigatorTestBase {

	public DnDTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBasicDragDrop() throws Exception {
		_viewer.expandToLevel(_p1, 3);

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(_p1.getFolder("f1")
				.getFile("file1.txt")));

		// DisplayHelper.sleep(Display.getCurrent(), 100);

		TreeItem[] items = _viewer.getTree().getItems();

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(0).getItem(0);
		// p1/f2
		TreeItem end = items[_p1Ind].getItem(1);
		if (!SWTEventHelper.performDnD(start, end)) {
			System.out.println("Drag and drop failed - test invalid");
			return;
		}

		_viewer.expandToLevel(_p1, 3);

		assertEquals(_p1.getFolder("f1").getFile("file2.txt"), items[_p1Ind]
				.getItem(0).getItem(0).getData());
		assertEquals(_p1.getFolder("f2").getFile("file1.txt"), items[_p1Ind]
				.getItem(1).getItem(0).getData());

		assertFalse(_p1.getFolder("f1").getFile("file1.txt").exists());
		assertTrue(_p1.getFolder("f2").getFile("file1.txt").exists());

	}

	// bug 185569 CommonDragAdapter should provide ways for
	// CommonDragAdapterAssistant
	// to perform clean up after drag has finished
	public void testResourceDrag() throws Exception {
		_viewer.expandToLevel(_p1, 3);

		IFile file = _p1.getFolder("f1").getFile("file1.txt");

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(file));

		// Want to drag this item to an editor so that the ResourceTransferType is
		// used
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		TextEditor editorPart = (TextEditor) IDE.openEditor(activePage, file);

		Control end = (Control) editorPart.getAdapter(Control.class);
		
		TreeItem[] items = _viewer.getTree().getItems();

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(0).getItem(0);

		if (!SWTEventHelper.performDnD(start, end)) {
			System.out.println("Drag and drop failed - test invalid");
			return;
		}

		assertNotNull(TestDragAssistant._finishedEvent);
		assertNotNull(TestDragAssistant._finishedSelection);
	}

	// Bug 261060 Add capability of setting drag operation
	// Bug 242265 Allow event to be available for validateDrop
	public void testSetDragOperation() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_DROP_COPY_CONTENT },
				false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_DROP_COPY_CONTENT }, false);

		_viewer.expandToLevel(_p1, 3);

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(_p1.getFolder("f1")
				.getFile("file1.txt")));

		DisplayHelper.sleep(Display.getCurrent(), 100);

		TreeItem[] items = _viewer.getTree().getItems();

		// .project is at index 0
		int firstFolder = 1;

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(firstFolder).getItem(0);

		// p1/f2
		TreeItem end = items[_p1Ind].getItem(firstFolder + 1);
		if (!SWTEventHelper.performDnD(start, end)) {
			System.out.println("Drag and drop failed - test invalid");
			return;
		}

		_viewer.expandToLevel(_p1, 3);

		// This is copied not moved
		assertEquals(_p1.getFolder("f1").getFile("file1.txt"), items[_p1Ind]
				.getItem(firstFolder).getItem(0).getData());
		assertEquals(_p1.getFolder("f1").getFile("file2.txt"), items[_p1Ind]
				.getItem(firstFolder).getItem(1).getData());
		assertEquals(_p1.getFolder("f2").getFile("file1.txt"), items[_p1Ind]
				.getItem(firstFolder + 1).getItem(0).getData());

		assertTrue(_p1.getFolder("f1").getFile("file1.txt").exists());
		assertTrue(_p1.getFolder("f2").getFile("file1.txt").exists());
	}

}
