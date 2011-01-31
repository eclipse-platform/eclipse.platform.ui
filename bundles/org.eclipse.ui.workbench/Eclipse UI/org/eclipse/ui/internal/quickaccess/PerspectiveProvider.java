/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 * 
 */
public class PerspectiveProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map idToElement = new HashMap();

	public String getId() {
		return "org.eclipse.ui.perspectives"; //$NON-NLS-1$
	}

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (PerspectiveElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (cachedElements == null) {
			IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench()
					.getPerspectiveRegistry().getPerspectives();
			for (int i = 0; i < perspectives.length; i++) {
				if (!WorkbenchActivityHelper.filterItem(perspectives[i])) {
					PerspectiveElement perspectiveElement = new PerspectiveElement(perspectives[i],
							this);
					idToElement.put(perspectiveElement.getId(), perspectiveElement);
				}
			}
			cachedElements = (QuickAccessElement[]) idToElement.values().toArray(
					new QuickAccessElement[idToElement.size()]);
		}
		return cachedElements;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Perspectives;
	}
}
