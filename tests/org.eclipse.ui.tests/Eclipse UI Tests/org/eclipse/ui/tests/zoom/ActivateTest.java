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

public class ActivateTest extends ZoomTestCase {
    public ActivateTest(String name) {
        super(name);
    }

    // activate a regular view when a view is zoomed
    public void test1() {
        IViewPart view1 = showRegularView(view1Id);
        IViewPart view2 = showRegularView(view2Id);
        zoom(view1);
        Assert.assertTrue(isZoomed(view1));
        page.activate(view2);
        Assert.assertTrue(noZoom());
    }

    // activate a fast view when a view is zoomed
    public void test2() {
        IViewPart view1 = showRegularView(view1Id);
        IViewPart view2 = showFastView(view2Id);
        zoom(view1);
        Assert.assertTrue(isZoomed(view1));
        page.activate(view2);
        Assert.assertTrue(isZoomed(view1));
    }

    // activate an editor when a view is zoomed
    public void test3() {
        IViewPart view = showRegularView(view1Id);
        page.activate(view);
        zoom(view);
        Assert.assertTrue(isZoomed(view));
        page.activate(editor1);
        Assert.assertTrue(noZoom());
    }

    //activate a regular view when an editor is zoomed
    public void test4() {
        IViewPart view = showRegularView(view1Id);
        zoom(editor1);
        Assert.assertTrue(isZoomed(editor1));
        page.activate(view);
        Assert.assertTrue(noZoom());
    }

    // activate a fast view when an editor is zoomed
    public void test5() {
        IViewPart view = showFastView(view1Id);
        zoom(editor1);
        Assert.assertTrue(isZoomed(editor1));
        page.activate(view);
        Assert.assertTrue(isZoomed(editor1));
    }

    // activate an editor in the same workbench as the zoomed editor
    public void test6() {
        // TODO Broken - Bug 54863
        //		zoom(editor1);
        //		Assert.assertTrue(isZoomed(editor1));
        //		page.activate(editor2);
        //		Assert.assertTrue(isZoomed(editor2));		
    }

    // activate an editor in a different workbench than the zoomed editor
    public void test7() {
        differentWorkbookSetUp();
        zoom(editor1);
        Assert.assertTrue(isZoomed(editor1));
        page.activate(editor2);
        Assert.assertTrue(noZoom());
    }
}