/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IStickyViewDescriptor;

/**
 * Utility class that will test various view properties.
 * 
 * @since 3.0
 */
public final class ViewUtils {

    public static boolean findInStack(IViewPart[] stack, IViewPart target) {
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == target)
                return true;
        }
        return false;
    }

    public static boolean isCloseable(IViewPart part) {
        ViewSite viewSite = (ViewSite) part.getSite();
        String id = viewSite.getId();
        IViewReference ref = viewSite.getPage().findViewReference(id);
        return ((WorkbenchPage) viewSite.getPage()).getActivePerspective()
                .isCloseable(ref);
    }

    public static boolean isMoveable(IViewPart part) {
        ViewSite viewSite = (ViewSite) part.getSite();
        String id = viewSite.getId();
        IViewReference ref = viewSite.getPage().findViewReference(id);
        return ((WorkbenchPage) viewSite.getPage()).getActivePerspective()
                .isMoveable(ref);
    }

    public static boolean isSticky(IViewPart part) {
        String id = part.getSite().getId();
        IStickyViewDescriptor[] descs = WorkbenchPlugin.getDefault()
                .getViewRegistry().getStickyViews();
        for (int i = 0; i < descs.length; i++) {
            if (descs[i].getId().equals(id))
                return true;
        }
        return false;
    }

    /**
     * 
     */
    protected ViewUtils() {
        //no-op
    }
}
