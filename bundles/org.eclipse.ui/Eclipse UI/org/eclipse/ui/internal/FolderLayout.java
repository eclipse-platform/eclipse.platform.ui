package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

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
		IViewReference ref = viewFactory.createView(viewId);
		IViewPart view = (IViewPart)ref.getPart(true);
		ViewSite site = (ViewSite)view.getSite();
		LayoutPart newPart = site.getPane();		
		linkPartToPageLayout(viewId, newPart);

		// Add it to the folder.
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
