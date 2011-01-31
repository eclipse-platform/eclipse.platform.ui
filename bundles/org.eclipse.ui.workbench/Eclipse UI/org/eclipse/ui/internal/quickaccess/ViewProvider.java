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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.3
 * 
 */
public class ViewProvider extends QuickAccessProvider {

	private QuickAccessElement[] cachedElements;
	private Map idToElement = new HashMap();
	private Collection multiInstanceViewIds = new HashSet(0);

	public String getId() {
		return "org.eclipse.ui.views"; //$NON-NLS-1$
	}

	public QuickAccessElement getElementForId(String id) {
		getElements();
		return (ViewElement) idToElement.get(id);
	}

	public QuickAccessElement[] getElements() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage() == null) {
			cachedElements = null;
			return new QuickAccessElement[0];
		}
		if (cachedElements == null) {
			IViewDescriptor[] views = PlatformUI.getWorkbench()
					.getViewRegistry().getViews();
			Collection elements = new HashSet(views.length);
			for (int i = 0; i < views.length; i++) {
				if (!WorkbenchActivityHelper.filterItem(views[i])) {
					addElement(views[i], elements, null, null);
				}
			}

			addOpenViews(elements);

			markMultiInstance(elements);


			cachedElements = (QuickAccessElement[]) elements
					.toArray(new QuickAccessElement[elements.size()]);

		}
		return cachedElements;
	}

	private void addElement(IViewDescriptor viewDesc, Collection elements, String secondaryId,
			String desc) {
		ViewElement viewElement = new ViewElement(viewDesc, this);
		viewElement.setSecondaryId(secondaryId);
		viewElement.setContentDescription(desc);
		boolean added = elements.add(viewElement);
		if (added) {
			idToElement.put(viewElement.getId(), viewElement);
		} else {
			// *could* be multinstance
			multiInstanceViewIds.add(viewDesc.getId());
		}
	}

	public void addOpenViews(Collection elements) {

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewRegistry viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
		IViewReference[] refs = page.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IViewDescriptor viewDescriptor = viewRegistry.find(refs[i].getId());
			addElement(viewDescriptor, elements, refs[i].getSecondaryId(),
					refs[i].getContentDescription());
		}
	}

	/**
	 * @param elements
	 */
	protected void markMultiInstance(Collection elements) {
		for (Iterator i = multiInstanceViewIds.iterator(); i.hasNext();) {
			String viewId = (String) i.next();
			ViewElement firstInstance = null;
			for (Iterator j = elements.iterator(); j.hasNext();) {
				ViewElement viewElement = (ViewElement) j.next();
				if (viewElement.getPrimaryId().equals(viewId)) {
					if (firstInstance == null)
						firstInstance = viewElement;
					else {
						firstInstance.setMultiInstance(true);
						viewElement.setMultiInstance(true);
					}
				}
			}
		}
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC);
	}

	public String getName() {
		return QuickAccessMessages.QuickAccess_Views;
	}
}
