/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IStickyViewDescriptor;

/**
 * Utility class that will test various view properties.
 *
 * @since 3.0
 */
public final class ViewUtils {

	public static boolean findInStack(IViewPart[] stack, IViewPart target) {
		for (IViewPart element : stack) {
			if (element == target) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param part the view part
	 */
	public static boolean isCloseable(IViewPart part) {
//        IWorkbenchPartSite viewSite = part.getSite();
//        IViewReference ref = (IViewReference) viewSite.getPage().getReference(part);
		// FIXME: Facade claimed closeable perspectives where not supported
		return false;
	}

	/**
	 * @param part the view part
	 */
	public static boolean isMoveable(IViewPart part) {
//    	IWorkbenchPartSite viewSite = part.getSite();
//        IViewReference ref = (IViewReference) viewSite.getPage().getReference(part);
		// FIXME: Facade claimed moveable perspectives where not supported
		return false;
	}

	public static boolean isSticky(IViewPart part) {
		String id = part.getSite().getId();
		IStickyViewDescriptor[] descs = PlatformUI.getWorkbench()
				.getViewRegistry().getStickyViews();
		for (IStickyViewDescriptor desc : descs) {
			if (desc.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	protected ViewUtils() {
		//no-op
	}
}
