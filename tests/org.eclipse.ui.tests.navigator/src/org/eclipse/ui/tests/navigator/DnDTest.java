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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.SWTEventHelper;

public class DnDTest extends NavigatorTestBase {

	public DnDTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
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

		TreeItem[] items = _viewer.getTree().getItems();

		// p1/f1/file1.txt
		TreeItem start = items[_p1Ind].getItem(0).getItem(0);
		// p1/f2
		TreeItem end = items[_p1Ind].getItem(1);
		SWTEventHelper.performTreeDnD(start, end);

		_viewer.expandToLevel(_p1, 3);

		assertEquals(_p1.getFolder("f1").getFile("file2.txt"), items[_p1Ind]
				.getItem(0).getItem(0).getData());
		assertEquals(_p1.getFolder("f2").getFile("file1.txt"), items[_p1Ind]
				.getItem(1).getItem(0).getData());

		assertFalse(_p1.getFolder("f1").getFile("file1.txt").exists());
		assertTrue(_p1.getFolder("f2").getFile("file1.txt").exists());
	}


}
