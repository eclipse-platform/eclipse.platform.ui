package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import java.util.*;

/**
 * Represents the top level container.
 */
public class RootLayoutContainer extends PartSashContainer {
public RootLayoutContainer(WorkbenchPage page) {
	super("root layout container",page);//$NON-NLS-1$
}
/**
 * Notification that a child layout part has been
 * added to the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected void childAdded(LayoutPart child) {
	// do nothing
}
/**
 * Gets root container for this part.
 */
public RootLayoutContainer getRootContainer() {
	return this;
}
/**
 * Notification that a child layout part has been
 * removed from the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected void childRemoved(LayoutPart child) {
	// do nothing
}
/**
 * Subclasses override this method to specify
 * the composite to use to parent all children
 * layout parts it contains.
 */
protected Composite createParent(Composite parentWidget) {
	return parentWidget;
}
/**
 * Subclasses override this method to dispose
 * of any swt resources created during createParent.
 */
protected void disposeParent() {
	// do nothing
}
/**
 * Get the part control.  This method may return null.
 */
public Control getControl() {
	return this.parent;
}
/**
 * @see IPersistablePart
 */
public IStatus restoreState(IMemento memento) 
{
	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("RootLayoutContainer.problemsRestoringPerspective"),null);
	
	// Read the info elements.
	IMemento [] children = memento.getChildren(IWorkbenchConstants.TAG_INFO);

	// Create a part ID to part hashtable.
	Map mapIDtoPart = new HashMap(children.length);

	// Loop through the info elements.
	for (int i = 0; i < children.length; i ++) 
	{
		// Get the info details.
		IMemento childMem = children[i];
		String partID = childMem.getString(IWorkbenchConstants.TAG_PART);
		String relativeID = childMem.getString(IWorkbenchConstants.TAG_RELATIVE);
		int relationship = 0;
		float ratio = 0.0f;
		if (relativeID != null) {
			relationship = childMem.getInteger(IWorkbenchConstants.TAG_RELATIONSHIP).intValue();
			ratio = childMem.getFloat(IWorkbenchConstants.TAG_RATIO).floatValue();
		}
		String strFolder = childMem.getString(IWorkbenchConstants.TAG_FOLDER);

		// Create the part.
		LayoutPart part = null;
		if (strFolder == null)
			part = new PartPlaceholder(partID);
		else {
			PartTabFolder folder = new PartTabFolder();
			folder.setID(partID);
			result.add(folder.restoreState(childMem.getChild(IWorkbenchConstants.TAG_FOLDER)));
			ContainerPlaceholder placeholder = new ContainerPlaceholder(partID);
			placeholder.setRealContainer(folder);
			part = placeholder;
		}
		// 1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
		part.setContainer(this);
		
		// Add the part to the layout
		if (relativeID == null) {
			add(part);
		} else {
			LayoutPart refPart = (LayoutPart)mapIDtoPart.get(relativeID);
			if (refPart != null) {
				add(part, relationship, ratio, refPart);	
			} else {
				WorkbenchPlugin.log("Unable to find part for ID: " + relativeID);//$NON-NLS-1$
			}
		}
		mapIDtoPart.put(partID, part);
	}
	return result;
}
/**
 * @see IPersistablePart
 */
public IStatus saveState(IMemento memento) {
	RelationshipInfo[] relationships = computeRelation();

	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("RootLayoutContainer.problemsSavingPerspective"),null);
	
	// Loop through the relationship array.
	for (int i = 0; i < relationships.length; i ++) {
		// Save the relationship info ..
		//		private LayoutPart part;
		// 		private int relationship;
		// 		private float ratio;
		// 		private LayoutPart relative;
		RelationshipInfo info = relationships[i];
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_INFO);
		childMem.putString(IWorkbenchConstants.TAG_PART, info.part.getID());
		if (info.relative != null) {
			childMem.putString(IWorkbenchConstants.TAG_RELATIVE, info.relative.getID());
			childMem.putInteger(IWorkbenchConstants.TAG_RELATIONSHIP, info.relationship);
			childMem.putFloat(IWorkbenchConstants.TAG_RATIO, info.ratio);
		}

		// Is this part a folder or a placeholder for one?
		PartTabFolder folder = null;
		if (info.part instanceof PartTabFolder) {
			folder = (PartTabFolder)info.part;
		} else if (info.part instanceof ContainerPlaceholder) {
			LayoutPart part = ((ContainerPlaceholder)info.part).getRealContainer();
			if (part instanceof PartTabFolder)
				folder = (PartTabFolder)part;
		}

		// If this is a folder save the contents.
		if (folder != null) {
			childMem.putString(IWorkbenchConstants.TAG_FOLDER, "true");//$NON-NLS-1$
			IMemento folderMem = childMem.createChild(IWorkbenchConstants.TAG_FOLDER);
			result.add(folder.saveState(folderMem));
		}
	}
	return result;
}
}
