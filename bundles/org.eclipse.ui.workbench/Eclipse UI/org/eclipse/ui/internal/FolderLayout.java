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

import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;

/**
 * This layout is used to define the initial set of views and placeholders
 * in a folder.
 * <p>
 * Views are added to the folder by ID. This id is used to identify 
 * a view descriptor in the view registry, and this descriptor is used to 
 * instantiate the IViewPart.
 * </p>
 */
public class FolderLayout implements IFolderLayout {
	private ViewFactory viewFactory;
	private PartTabFolder folder;
	private PageLayout pageLayout;
	
/**
 * Create an instance of a FolderLayout belonging to an PageLayout.
 */
public FolderLayout(PageLayout pageLayout, PartTabFolder folder, ViewFactory viewFactory) {
	super();
	this.folder = folder;
	this.viewFactory = viewFactory;
	this.pageLayout = pageLayout;
}
/**
 * @see IFolderLayout
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
	
	// Add it to the folder layout.
	String label = desc.getLabel();
	folder.add(label, folder.getItemCount(), newPart);
}
/**
 * @see IFolderLayout
 */
public void addView(String viewId) {
	if (pageLayout.checkPartInLayout(viewId))
		return;

	try {
		// Create the part.
		WorkbenchPartReference ref = (WorkbenchPartReference)viewFactory.createView(viewId);
		ViewPane newPart = (ViewPane)ref.getPane();
		if(newPart == null) {
			newPart = new ViewPane((IViewReference)ref,(WorkbenchPage)ref.getPage());
			ref.setPane(newPart);
		}
		linkPartToPageLayout(viewId, newPart);
		folder.add(newPart);
	} catch (PartInitException e) {
		// cannot safely open the dialog so log the problem
		WorkbenchPlugin.log(e.getMessage()) ;
	}
}
/**
 * Inform the page layout of the new part created
 * and the folder the part belongs to.
 */
private void linkPartToPageLayout(String viewId, LayoutPart newPart) {
	pageLayout.setRefPart(viewId, newPart);
	pageLayout.setFolderPart(viewId, folder);
}
}
