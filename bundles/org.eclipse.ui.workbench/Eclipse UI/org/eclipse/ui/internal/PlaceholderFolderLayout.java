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
package org.eclipse.ui.internal;


import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.internal.registry.*;

/**
 * This layout is used to define the initial set of placeholders
 * in a placeholder.
 * <p>
 * Views are added to the placeholder by ID. This id is used to identify 
 * a view descriptor in the view registry, and this descriptor is used to 
 * instantiate the IViewPart.
 * </p>
 */
public class PlaceholderFolderLayout implements IPlaceholderFolderLayout{
	private PageLayout pageLayout;
	private ContainerPlaceholder placeholder;

	public PlaceholderFolderLayout(PageLayout pageLayout, ContainerPlaceholder folder) {
		super();
		this.placeholder = folder;
		this.pageLayout = pageLayout;
	}
	/**
	 * @see IPlaceholderFolderLayout
	 */
	public void addPlaceholder(String viewId) {
		if (pageLayout.checkPartInLayout(viewId))
			return;

		// Get the view's label.
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(viewId);
		if (desc == null) {
			// cannot safely open the dialog so log the problem
			WorkbenchPlugin.log("Unable to find view label: " + viewId);//$NON-NLS-1$
			return;
		}

		// Create the placeholder.
		LayoutPart newPart = new PartPlaceholder(viewId);
		
		linkPartToPageLayout(viewId, newPart);
	
		// Add it to the placeholder layout.
		placeholder.add(newPart);		
	}
	/**
	 * Inform the page layout of the new part created
	 * and the placeholder the part belongs to.
	 */
	private void linkPartToPageLayout(String viewId, LayoutPart newPart) {
		pageLayout.setRefPart(viewId, newPart);
		pageLayout.setFolderPart(viewId, placeholder);
		newPart.setContainer(placeholder);
	}
}
