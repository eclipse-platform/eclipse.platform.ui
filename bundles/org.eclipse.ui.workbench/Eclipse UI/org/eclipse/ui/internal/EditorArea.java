package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Represents the area set aside for editor workbooks.
 * This container only accepts EditorWorkbook and PartSash
 * as layout parts.
 *
 * Note no views are allowed within this container.
 */
public class EditorArea extends PartSashContainer {
	
	private static final String DEFAULT_WORKBOOK_ID = "DefaultEditorWorkbook";//$NON-NLS-1$
	private IPartDropListener partDropListener;
	private ArrayList editorWorkbooks = new ArrayList(3);
	private EditorWorkbook activeEditorWorkbook;
	private DropTarget dropTarget;
	private DropTargetAdapter dropTargetAdapter;
	
public EditorArea(String editorId, IPartDropListener listener, WorkbenchPage page) {
	super(editorId,page);

	this.partDropListener = listener;
	this.dropTargetAdapter = new EditorAreaDropAdapter(page);
	createDefaultWorkbook();
}
/**
 * Add an editor to the active workbook.
 */
public void addEditor(EditorPane pane) {
	EditorWorkbook workbook = getActiveWorkbook();
	workbook.add(pane);
}
/**
 * Notification that a child layout part has been
 * added to the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected void childAdded(LayoutPart child) {
	if (child instanceof EditorWorkbook)
		editorWorkbooks.add(child);
}
/**
 * Notification that a child layout part has been
 * removed from the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected void childRemoved(LayoutPart child) {
	if (child instanceof EditorWorkbook) {
		editorWorkbooks.remove(child);
		if (activeEditorWorkbook == child)
			setActiveWorkbook(null, false);
	}
}
protected EditorWorkbook createDefaultWorkbook() {
	EditorWorkbook newWorkbook = new EditorWorkbook(this);
	newWorkbook.setID(DEFAULT_WORKBOOK_ID);
	add(newWorkbook);
	return newWorkbook;
}
/**
 * Subclasses override this method to specify
 * the composite to use to parent all children
 * layout parts it contains.
 */
protected Composite createParent(Composite parentWidget) {
	return new Composite(parentWidget, SWT.NONE);
}
/**
 * Dispose of the editor area.
 */
public void dispose() {
	// Free editor workbooks.
	Iterator iter = editorWorkbooks.iterator();
	while (iter.hasNext()) {
		EditorWorkbook wb = (EditorWorkbook)iter.next();
		wb.dispose();
	}
	editorWorkbooks.clear();

	// Free rest.
	super.dispose();
}
/**
 * Subclasses override this method to dispose
 * of any swt resources created during createParent.
 */
protected void disposeParent() {
	this.parent.dispose();
}
/**
 * Return the editor workbook which is active.
 */
public EditorWorkbook getActiveWorkbook() {
	if (activeEditorWorkbook == null) {
		if (editorWorkbooks.size() < 1)
			setActiveWorkbook(createDefaultWorkbook(), false);
		else 
			setActiveWorkbook((EditorWorkbook)editorWorkbooks.get(0), false);
	}

	return activeEditorWorkbook;
}
/**
 * Return the editor workbook id which is active.
 */
public String getActiveWorkbookID() {
	return getActiveWorkbook().getID();
}
/**
 * Return the all the editor workbooks.
 */
public ArrayList getEditorWorkbooks() {
	return (ArrayList)editorWorkbooks.clone();
}
/**
 * Return the all the editor workbooks.
 */
public int getEditorWorkbookCount() {
	return editorWorkbooks.size();
}
/**
 * Return the interested listener of d&d events.
 */
public IPartDropListener getPartDropListener() {
	return partDropListener;
}
/**
 * Return true is the workbook specified
 * is the active one.
 */
protected boolean isActiveWorkbook(EditorWorkbook workbook) {
	return activeEditorWorkbook == workbook;
}
/**
 * Find the sashs around the specified part.
 */
public void findSashes(LayoutPart pane,PartPane.Sashes sashes) {
	//Find the sashes around the current editor and
	//then the sashes around the editor area.
	super.findSashes(pane,sashes);
	getRootContainer().findSashes(this,sashes);
}
/**
 * Remove all the editors
 */
public void removeAllEditors() {
	EditorWorkbook currentWorkbook = getActiveWorkbook();

	// Iterate over a copy so the original can be modified.	
	Iterator workbooks = ((ArrayList)editorWorkbooks.clone()).iterator();
	while (workbooks.hasNext()) {
		EditorWorkbook workbook = (EditorWorkbook)workbooks.next();
		workbook.removeAll();
		if (workbook != currentWorkbook) {
			remove(workbook);
			workbook.dispose();
		}
	}
}
/**
 * Remove an editor from its' workbook.
 */
public void removeEditor(EditorPane pane) {
	EditorWorkbook workbook = pane.getWorkbook();
	if (workbook == null)
		return;
	workbook.remove(pane);

	// remove the editor workbook if empty and
	// there are other workbooks
	if (workbook.getItemCount() < 1 && editorWorkbooks.size() > 1) {
		remove(workbook);
		workbook.dispose();
	}
}
/**
 * @see IPersistablePart
 */
public IStatus restoreState(IMemento memento) {
	// Remove the default editor workbook that is
	// initialy created with the editor area.
	if (children != null) {
		EditorWorkbook defaultWorkbook = null;
		for (int i = 0; i < children.size(); i++) {
			LayoutPart child = (LayoutPart)children.get(i);
			if (child.getID() == DEFAULT_WORKBOOK_ID) {
				defaultWorkbook = (EditorWorkbook)child;
				if (defaultWorkbook.getItemCount() > 0)
					defaultWorkbook = null;
			}
		}
		if (defaultWorkbook != null)
			remove(defaultWorkbook);
	}

	// Restore the relationship/layout
	IMemento [] infos = memento.getChildren(IWorkbenchConstants.TAG_INFO);
	Map mapIDtoPart = new HashMap(infos.length);

	for (int i = 0; i < infos.length; i ++) {
		// Get the info details.
		IMemento childMem = infos[i];
		String partID = childMem.getString(IWorkbenchConstants.TAG_PART);
		String relativeID = childMem.getString(IWorkbenchConstants.TAG_RELATIVE);
		int relationship = 0;
		float ratio = 0.0f;
		if (relativeID != null) {
			relationship = childMem.getInteger(IWorkbenchConstants.TAG_RELATIONSHIP).intValue();
			ratio = childMem.getFloat(IWorkbenchConstants.TAG_RATIO).floatValue();
		}

		// Create the part.
		EditorWorkbook workbook = new EditorWorkbook(this);
		workbook.setID(partID);
		// 1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
		workbook.setContainer(this);
		
		// Add the part to the layout
		if (relativeID == null) {
			add(workbook);
		} else {
			LayoutPart refPart = (LayoutPart)mapIDtoPart.get(relativeID);
			if (refPart != null) {
				add(workbook, relationship, ratio, refPart);	
			} else {
				WorkbenchPlugin.log("Unable to find part for ID: " + relativeID);//$NON-NLS-1$
			}
		}
		mapIDtoPart.put(partID, workbook);
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}
/**
 * @see IPersistablePart
 */
public IStatus saveState(IMemento memento) {
	RelationshipInfo[] relationships = computeRelation();
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
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}
/**
 * Set the editor workbook which is active.
 */
public void setActiveWorkbook(EditorWorkbook newWorkbook, boolean hasFocus) {
	EditorWorkbook oldWorkbook = activeEditorWorkbook;
	activeEditorWorkbook = newWorkbook;
	
	if (oldWorkbook != null && oldWorkbook != newWorkbook)
		oldWorkbook.tabFocusHide();

	if (newWorkbook != null)
		newWorkbook.tabFocusShow(hasFocus);
		
	updateTabList();
}
/**
 * Set the editor workbook which is active.
 */
public void setActiveWorkbookFromID(String id) {
	for (int i = 0; i < editorWorkbooks.size(); i++) {
		EditorWorkbook workbook = (EditorWorkbook) editorWorkbooks.get(i);
		if (workbook.getID().equals(id))
			setActiveWorkbook(workbook, false);
	}
}

/**
 * Updates the editor area's tab list to include the active
 * editor and its tab.
 */
public void updateTabList() {
	Composite parent = getParent();
	if (parent != null) {  // parent may be null on startup
		EditorWorkbook wb = getActiveWorkbook();
		if (wb == null) {
			parent.setTabList(new Control[0]);
		}
		else {
			parent.setTabList(wb.getTabList());
		}
	}
}


	/**
	 * @see org.eclipse.ui.internal.LayoutPart#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//let the user drop files/editor input on the editor area
		addDropSupport();		
	}
	private void addDropSupport() {
		if (dropTarget == null) {
			Transfer[] types = new Transfer[] {
					EditorInputTransfer.getInstance(), 
					ResourceTransfer.getInstance(),
					MarkerTransfer.getInstance() };
		
			dropTarget = new DropTarget(getControl(), DND.DROP_DEFAULT);
			dropTarget.setTransfer(types);
			dropTarget.addDropListener(dropTargetAdapter);
		}
	}

}
