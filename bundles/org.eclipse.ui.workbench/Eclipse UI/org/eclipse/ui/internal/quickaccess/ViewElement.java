/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.3
 * 
 */
public class ViewElement extends QuickAccessElement {

	private final IViewDescriptor viewDescriptor;

	private String category;

	/* package */ViewElement(IViewDescriptor viewDescriptor, ViewProvider viewProvider) {
		super(viewProvider);
		this.viewDescriptor = viewDescriptor;

		IViewCategory[] categories = PlatformUI.getWorkbench().getViewRegistry().getCategories();
		for (int i = 0; i < categories.length; i++) {
			IViewDescriptor[] views = categories[i].getViews();
			for (int j = 0; j < views.length; j++) {
				if (views[j] == viewDescriptor) {
					category = categories[i].getLabel();
					return;
				}
			}
		}
	}

	public void execute() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			try {
				activePage.showView(viewDescriptor.getId());
			} catch (PartInitException e) {
			}
		}
	}

	public String getId() {
		return viewDescriptor.getId();
	}

	public ImageDescriptor getImageDescriptor() {
		return viewDescriptor.getImageDescriptor();
	}

	public String getLabel() {
		if (category == null) {
			return viewDescriptor.getLabel();
		}
		return viewDescriptor.getLabel() + separator + category;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((viewDescriptor == null) ? 0 : viewDescriptor.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ViewElement other = (ViewElement) obj;
		if (viewDescriptor == null) {
			if (other.viewDescriptor != null)
				return false;
		} else if (!viewDescriptor.equals(other.viewDescriptor))
			return false;
		return true;
	}
}
