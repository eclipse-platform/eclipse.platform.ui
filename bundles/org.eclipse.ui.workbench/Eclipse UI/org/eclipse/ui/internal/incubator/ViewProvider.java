/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.3
 * 
 */
public class ViewProvider extends AbstractProvider {

	private AbstractElement[] cachedElements;
	private Map idToElement = new HashMap();

	public String getId() {
		return "org.eclipse.ui.views"; //$NON-NLS-1$
	}

	public AbstractElement getElementForId(String id) {
		getElements();
		return (ViewElement) idToElement.get(id);
	}

	public AbstractElement[] getElements() {
		if (cachedElements == null) {
			IViewDescriptor[] views = PlatformUI.getWorkbench()
					.getViewRegistry().getViews();
			cachedElements = new AbstractElement[views.length];
			for (int i = 0; i < views.length; i++) {
				ViewElement viewElement = new ViewElement(views[i], this);
				cachedElements[i] = viewElement;
				idToElement.put(viewElement.getId(), viewElement);
			}
		}
		return cachedElements;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC);
	}

	public String getName() {
		return IncubatorMessages.CtrlEAction_Views;
	}
}
