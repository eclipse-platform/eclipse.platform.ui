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

import org.eclipse.ui.IViewPart;

public class CloseEditorTest extends ZoomTestCase {
	public CloseEditorTest(String name) {
		super(name);	
	}
	
	// closing the editor activates a regular view
	public void test1() {
		IViewPart view = showRegularView(view1Id);
		page.closeEditor(editor2, false);
		zoom(editor1);
		Assert.assertTrue(isZoomed(editor1));
		page.closeEditor(editor1, false);
		Assert.assertTrue(page.getActivePart() == view);
		Assert.assertTrue(noZoom());
	}
	// closing the editor activates an editor in the same workbook
	public void test2() {
	    // TODO Broken - Bug 54863
//		zoom(editor1);
//		Assert.assertTrue(isZoomed(editor1));
//		page.closeEditor(editor1, false);
//		Assert.assertTrue(page.getActivePart() == editor2);
//		Assert.assertTrue(isZoomed(editor2));
	}
	// closing the editor activates an editor in a different workbook
	public void test3() {
		differentWorkbookSetUp();
		zoom(editor1);
		Assert.assertTrue(isZoomed(editor1));
		page.closeEditor(editor1, false);
		Assert.assertTrue(page.getActivePart() == editor2);
		Assert.assertTrue(noZoom());		
	}
}
