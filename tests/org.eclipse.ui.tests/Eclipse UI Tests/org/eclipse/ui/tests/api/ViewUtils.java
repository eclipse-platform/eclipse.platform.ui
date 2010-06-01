/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.tests.helpers.TestFacade;
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
        IWorkbenchPartSite viewSite = part.getSite();
        IViewReference ref = (IViewReference) viewSite.getPage().getReference(part);
        return ((TestFacade)Tweaklets.get(TestFacade.KEY)).isClosableInPerspective(ref);
    }

    public static boolean isMoveable(IViewPart part) {
    	IWorkbenchPartSite viewSite = part.getSite();
        IViewReference ref = (IViewReference) viewSite.getPage().getReference(part);
        return ((TestFacade)Tweaklets.get(TestFacade.KEY)).isMoveableInPerspective(ref);
    }

    public static boolean isSticky(IViewPart part) {
        String id = part.getSite().getId();
        IStickyViewDescriptor[] descs = PlatformUI.getWorkbench()
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
