/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;

public class HideViewTest extends ZoomTestCase {
    public HideViewTest(String name) {
        super(name);
    }

    // hiding a fast view causes a zoomed regular view to be activated
    public void test1() {
        IViewPart view1 = showFastView(view1Id);
        zoom(editor1);
        Assert.assertTrue(isZoomed(editor1));
        page.hideView(view1);
        Assert.assertTrue(page.getActivePart() == editor1);
        Assert.assertTrue(isZoomed(editor1));
    }

    // hiding a fast view causes a zoomed regular view to be activated
    public void test2() {
        IViewPart view1 = showRegularView(view1Id);
        IViewPart view2 = showFastView(view2Id);
        zoom(view1);
        Assert.assertTrue(isZoomed(view1));
        page.hideView(view2);
        Assert.assertTrue(page.getActivePart() == view1);
        Assert.assertTrue(isZoomed(view1));
    }

    // hiding a regular view causes another regular view to be activated
    public void test3() {
        IViewPart view1 = showRegularView(view1Id);
        IViewPart view2 = showRegularView(view2Id);
        zoom(view1);
        Assert.assertTrue(isZoomed(view1));
        page.hideView(view1);
        Assert.assertTrue(page.getActivePart() == view2);
        Assert.assertTrue(noZoom());
    }

    // hiding view causes an editor to be activated
    public void test4() {
        IViewPart view1 = showRegularView(view1Id);
        zoom(view1);
        Assert.assertTrue(isZoomed(view1));
        page.hideView(view1);
        Assert.assertTrue(page.getActivePart() instanceof IEditorPart);
        Assert.assertTrue(noZoom());
    }
}