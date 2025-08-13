/*******************************************************************************
 * Copyright (c) 2009, 2015 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - Initial implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.SWTEventHelper;
import org.eclipse.ui.tests.navigator.extension.TestDragAssistant;
import org.junit.Test;

public class DnDTest extends NavigatorTestBase {

	public DnDTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	@Test
	public void testBasicDragDrop() {
		_viewer.expandToLevel(_p1, 3);

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(_p1.getFolder("f1")
				.getFile("file1.txt")));

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
		items = _viewer.getTree().getItems();

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
	@Test
	public void testResourceDrag() throws PartInitException {
		_viewer.expandToLevel(_p1, 3);

		IFile file = _p1.getFolder("f1").getFile("file1.txt");

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(file));

		// Want to drag this item to an editor so that the ResourceTransferType is
		// used
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		TextEditor editorPart = (TextEditor) IDE.openEditor(activePage, file);

		Control end = editorPart.getAdapter(Control.class);

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

	// bug 264323 [CommonNavigator] CommonDragAdapterAssistant should be allowed to opt out of a drag
	@Test
	public void testDragOptOut() throws PartInitException {
		_viewer.expandToLevel(_p1, 3);

		IFile file = _p1.getFolder("f1").getFile("file1.txt");

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(file));

		// Want to drag this item to an editor so that the ResourceTransferType is
		// used
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		TextEditor editorPart = (TextEditor) IDE.openEditor(activePage, file);

		Control end = editorPart.getAdapter(Control.class);

		TreeItem[] items = _viewer.getTree().getItems();

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(0).getItem(0);

		TestDragAssistant._doit = false;

		if (!SWTEventHelper.performDnD(start, end)) {
			System.out.println("Drag and drop failed - test invalid");
			return;
		}

		assertFalse(TestDragAssistant._dragSetDataCalled);
	}

	// Bug 261060 Add capability of setting drag operation
	// Bug 242265 Allow event to be available for validateDrop
	@Test
	public void testSetDragOperation() {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_DROP_COPY },
				false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_DROP_COPY }, false);

		_viewer.expandToLevel(_p1, 3);

		// Need to set the selection because the Dnd stuff is not doing it
		_viewer.setSelection(new StructuredSelection(_p1.getFolder("f1")
				.getFile("file1.txt")));

		DisplayHelper.sleep(100);

		TreeItem[] items = _viewer.getTree().getItems();

		int firstFolder = 0;

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(firstFolder).getItem(0);

		// p1/f2
		TreeItem end = items[_p1Ind].getItem(firstFolder + 1);
		if (!SWTEventHelper.performDnD(start, end)) {
			System.out.println("Drag and drop failed - test invalid");
			return;
		}

		// Trying to make this test deterministic
		refreshViewer();
		DisplayHelper.sleep(100);
		_viewer.expandToLevel(_p1, 3);
		items = _viewer.getTree().getItems();

		// This is copied not moved
		assertEquals(_p1.getFolder("f1").getFile("file1.txt"), items[_p1Ind]
				.getItem(firstFolder).getItem(0).getData());
		assertEquals(_p1.getFolder("f1").getFile("file2.txt"), items[_p1Ind]
				.getItem(firstFolder).getItem(1).getData());

		// This line fails to see the firstFolder+1 unless all of that
		// refreshing crap above is in
		assertEquals(_p1.getFolder("f2").getFile("file1.txt"), items[_p1Ind]
				.getItem(firstFolder + 1).getItem(0).getData());

		assertTrue(_p1.getFolder("f1").getFile("file1.txt").exists());
		assertTrue(_p1.getFolder("f2").getFile("file1.txt").exists());
	}

}
