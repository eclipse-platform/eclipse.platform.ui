package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
import java.util.*;

/**
 * This factory is used to define the initial layout of a part sash container.
 * By definition, one part may exist in several containers, so views are added
 * to the container by id rather than handle.  This id is used to identify 
 * a view descriptor in the view registry, and this descriptor is used to 
 * instantiate the IViewPart.
 */
public class FolderLayout implements IFolderLayout {
	private ViewFactory viewFactory;
	private PartTabFolder folder;
/**
 * LayoutFactory constructor comment.
 */
public FolderLayout(PartTabFolder folder, ViewFactory viewFactory) {
	super();
	this.folder = folder;
	this.viewFactory = viewFactory;
}
/**
 * @see ILayoutFactory
 */
public void addPlaceholder(String newID) 
{
	// Get the view label.
	IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
	IViewDescriptor desc = reg.find(newID);
	if (desc == null) {
		// cannot safely open the dialog so log the problem
		WorkbenchPlugin.log("Unable to find view label: " + newID);
		return;
	}

	// Create the placeholder.
	LayoutPart newPart = new PartPlaceholder(newID);
	
	// Add it to the layout.
	String label = desc.getLabel();
	folder.add(label, folder.getItemCount(), newPart);
}
/**
 * Adds a view with the given id to this folder.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 */
public void addView(String newID) {
	try {
		// Create the part.
		LayoutPart newPart = viewFactory.createView(newID);

		// Add it to the folder.
		folder.add(newPart);
	} catch (PartInitException e) {
		// cannot safely open the dialog so log the problem
		WorkbenchPlugin.log(e.getMessage()) ;
	}
}
}
