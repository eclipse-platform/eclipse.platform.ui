package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> 
    - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IViewDescriptor;

public class PartTabFolder extends LayoutPart
	implements ILayoutContainer
{
	private static int tabLocation = -1;	// Initialized in constructor.
	
	private CTabFolder tabFolder;
	private Map mapTabToPart = new HashMap();
	private LayoutPart current;
	private Map mapPartToDragMonitor = new HashMap();
	private boolean assignFocusOnSelection = true;
	
	// inactiveCurrent is only used when restoring the persisted state of
	// perspective on startup.
	private LayoutPart inactiveCurrent;	
	private Composite parent;
	private boolean active = false;

	// listen for mouse down on tab to set focus.
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
			// Else, set focus.
			CTabItem newItem = tabFolder.getItem(new Point(e.x, e.y));
			if (newItem != null) {
				CTabItem oldItem = tabFolder.getSelection();
				if (newItem != oldItem)
					return;
			}
			if (PartTabFolder.this.current != null)
				PartTabFolder.this.current.setFocus();
		}
	};

	private class TabInfo {
		private String tabText;
		private LayoutPart part;
	}
	TabInfo[] invisibleChildren;
/**
 * PartTabFolder constructor comment.
 */
public PartTabFolder() {
	super("PartTabFolder");//$NON-NLS-1$
	setID(this.toString());	// Each folder has a unique ID so relative positioning is unambiguous.

	// Get the location of the tabs from the preferences
	if (tabLocation == -1)
		tabLocation = getPreferenceStore().getInt(
			IPreferenceConstants.VIEW_TAB_POSITION);
		
}
/**
 * Add a part at an index.
 */
public void add(String name, int index, LayoutPart part)
{
	if (active && !(part instanceof PartPlaceholder)) {
		CTabItem tab = createPartTab(part, name, index);
		index = tabFolder.indexOf(tab);
		setSelection(index);
	}
	else {
		TabInfo info = new TabInfo();
		info.tabText = name;
		info.part = part;
		invisibleChildren = arrayAdd(invisibleChildren, info, index);
		if (active)
			part.setContainer(this);
	}
}
/**
 * See IVisualContainer#add
 */
public void add(LayoutPart child) {
	int index = getItemCount();
	String label = "";//$NON-NLS-1$
	if (child instanceof PartPane) {
		WorkbenchPartReference ref = (WorkbenchPartReference)((PartPane)child).getPartReference();
		label = ref.getRegisteredName();
	}
	add(label, index, child);
}
/**
 * See ILayoutContainer::allowBorder
 *
 * There is already a border around the tab
 * folder so no need for one from the parts.
 */
public boolean allowsBorder() {
	return mapTabToPart.size() <= 1;
}
private TabInfo[] arrayAdd(TabInfo[] array, TabInfo item, int index) {

	if (item == null) return array;
	
	TabInfo[] result = null;
	
	if (array == null) {
		result = new TabInfo[1];	
		result[0] = item;
	} else {
		if (index >= array.length) index = array.length;
		result = new TabInfo[array.length + 1];
		System.arraycopy(array, 0, result, 0, index);
		result[index] = item;
		System.arraycopy(array, index, result, index + 1, array.length - index);
	}

	return result;
}
private TabInfo[] arrayRemove(TabInfo[] array, LayoutPart item) {

	if (item == null) return array;
	
	TabInfo[] result = null;
	
	int index = -1;
	for (int i = 0, length = array.length; i < length; i++){
		if (item == array[i].part){
			index = i;
			break;
		}
	}
	if (index == -1) return array;
	
	if (array.length > 1) {
		result = new TabInfo[array.length - 1];
		System.arraycopy(array, 0, result, 0, index);
		System.arraycopy(array, index+1, result, index, result.length - index);
	}
	return result;
}
/**
 * Set the default bounds of a page in a CTabFolder.
 */
protected static Rectangle calculatePageBounds(CTabFolder folder) {
	if (folder == null) 
		return new Rectangle(0,0,0,0);
	Rectangle bounds = folder.getBounds();
	Rectangle offset = folder.getClientArea();
	bounds.x += offset.x;
	bounds.y += offset.y;
	bounds.width = offset.width;
	bounds.height = offset.height;
	return bounds;
}
public void createControl(Composite parent) {

	if (tabFolder != null) return;

	// Create control.	
	this.parent = parent;
	tabFolder = new CTabFolder(parent, tabLocation | SWT.BORDER);

	// listener to switch between visible tabItems
	tabFolder.addListener(SWT.Selection, new Listener(){
		public void handleEvent(Event e){
			LayoutPart item = (LayoutPart)mapTabToPart.get(e.item);
			// Item can be null when tab is just created but not map yet.
			if (item != null) {
				setSelection(item);
				if (assignFocusOnSelection)
					item.setFocus();
			}
		}
	});

	// listener to resize visible components
	tabFolder.addListener(SWT.Resize, new Listener(){
		public void handleEvent(Event e){
			setControlSize();
		}
	});

	// listen for mouse down on tab to set focus.
	tabFolder.addMouseListener(this.mouseListener);

	// enable for drag & drop target
	tabFolder.setData((IPartDropTarget)this);
	
	// Create pages.
	if (invisibleChildren != null) {
		TabInfo[] stillInactive = new TabInfo[0];
		int tabCount = 0;
		for (int i = 0, length = invisibleChildren.length; i < length; i++){
			if (invisibleChildren[i].part instanceof PartPlaceholder) {
				invisibleChildren[i].part.setContainer(this);
				TabInfo[] newStillInactive = new TabInfo[stillInactive.length + 1];
				System.arraycopy(stillInactive, 0, newStillInactive, 0, stillInactive.length);
				newStillInactive[stillInactive.length] = invisibleChildren[i];
				stillInactive = newStillInactive;
			}
			else {
				createPartTab(invisibleChildren[i].part, invisibleChildren[i].tabText, tabCount);
				++ tabCount;
			}
		}
		invisibleChildren = stillInactive;
	}
	
	active = true;

	// Set current page.
	if (getItemCount() > 0) {
		int newPage = 0;
		if (current != null)
			newPage = indexOf(current);
		setSelection(newPage);
	}
}
private CTabItem createPartTab(LayoutPart part, String tabName, int tabIndex) {
	CTabItem tabItem;

	if (tabIndex < 0)
		tabItem = new CTabItem(this.tabFolder, SWT.NONE);
	else
		tabItem = new CTabItem(this.tabFolder, SWT.NONE, tabIndex);
	tabItem.setText(tabName);

	mapTabToPart.put(tabItem, part);

	part.createControl(this.parent);
	part.setContainer(this);

	// Because the container's allow border api
	// is dependent on the number of tabs it has,
	// reset the container so the parts can update.
	if (mapTabToPart.size() == 2) {
		Iterator parts = mapTabToPart.values().iterator();
		((LayoutPart)parts.next()).setContainer(this);
		((LayoutPart)parts.next()).setContainer(this);
	}
	
	return tabItem;
}
/**
 * Remove the ability to d&d using the tab
 *
 * See PerspectivePresentation
 */
public void disableDrag(LayoutPart part) {
	PartDragDrop partDragDrop = (PartDragDrop)mapPartToDragMonitor.get(part);
	if (partDragDrop != null) {
		partDragDrop.dispose();
		mapPartToDragMonitor.remove(part);
	}

	// remove d&d on folder when no tabs left
	if (mapPartToDragMonitor.size() == 1) {
		partDragDrop = (PartDragDrop)mapPartToDragMonitor.get(this);
		if (partDragDrop != null) {
			partDragDrop.dispose();
			mapPartToDragMonitor.remove(this);
		}
	}
}
/**
 * See LayoutPart#dispose
 */
public void dispose() {

	if (!active) return;

	// combine active and inactive entries into one
	TabInfo[] newInvisibleChildren = new TabInfo[mapTabToPart.size()];
		
	if (invisibleChildren != null){
		// tack the inactive ones on at the end
		newInvisibleChildren = new TabInfo[newInvisibleChildren.length + invisibleChildren.length];
		System.arraycopy(invisibleChildren, 0, newInvisibleChildren, mapTabToPart.size(), invisibleChildren.length);
	}
			
	Iterator keys = mapTabToPart.keySet().iterator();
	while(keys.hasNext()) {
		CTabItem item = (CTabItem)keys.next();
		LayoutPart part = (LayoutPart)mapTabToPart.get(item);
		TabInfo info = new TabInfo();
		info.tabText = item.getText();
		info.part = part;
		newInvisibleChildren[tabFolder.indexOf(item)] = info;
		disableDrag(part);
	}

	invisibleChildren = newInvisibleChildren;

	if (invisibleChildren != null) {
		for (int i = 0, length = invisibleChildren.length; i < length; i++){
			invisibleChildren[i].part.setContainer(null);
		}
	}
		
	mapTabToPart.clear();

	if (tabFolder != null)
		tabFolder.dispose();
	tabFolder = null;
	
	active = false;
}
/**
 * Enable the view pane to be d&d via its tab
 *
 * See PerspectivePresentation::enableDrag
 */
public void enableDrag(ViewPane pane, IPartDropListener listener) {
	// make sure its not already registered
	if (mapPartToDragMonitor.containsKey(pane))
		return;

	CTabItem tab = getTab(pane);
	if (tab == null)
		return;

	CTabPartDragDrop dragSource = new CTabPartDragDrop(pane, this.tabFolder, tab);
	mapPartToDragMonitor.put(pane, dragSource);
	dragSource.addDropListener(listener);

	// register d&d on empty tab area the first time thru
	if (mapPartToDragMonitor.size() == 1) {
		dragSource = new CTabPartDragDrop(this, this.tabFolder, null);
		mapPartToDragMonitor.put(this, dragSource);
		dragSource.addDropListener(listener);
	}
}
/**
 * Open the tracker to allow the user to move
 * the specified part using keyboard.
 */
public void openTracker(LayoutPart part) {
	CTabPartDragDrop dnd = (CTabPartDragDrop)mapPartToDragMonitor.get(part);
	dnd.openTracker();
}
/**
 * Gets the presentation bounds.
 */
public Rectangle getBounds() {
	return tabFolder.getBounds();
}

// getMinimumHeight() added by cagatayk@acm.org 
/**
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	if (current == null || tabFolder == null || tabFolder.isDisposed())
		return super.getMinimumHeight();
	
	if (getItemCount() > 1) {
		Rectangle trim = tabFolder.computeTrim(0, 0, 0, current.getMinimumHeight());
		return trim.height;
	}
	else
		return current.getMinimumHeight();
}

/**
 * See IVisualContainer#getChildren
 */
public LayoutPart[] getChildren() {
	LayoutPart [] children = new LayoutPart[0];
	
	if (invisibleChildren != null) {
		children = new LayoutPart[invisibleChildren.length];
		for (int i = 0, length = invisibleChildren.length; i < length; i++){
			children[i] = invisibleChildren[i].part;
		}
	}

	int count = mapTabToPart.size();
	if (count > 0) {
		int index = children.length;
		LayoutPart [] newChildren = new LayoutPart[children.length + count];
		System.arraycopy(children, 0, newChildren, 0, children.length);
		children = newChildren;
		for (int nX = 0; nX < count; nX ++) {
			CTabItem tabItem = tabFolder.getItem(nX);
			children[index] = (LayoutPart)mapTabToPart.get(tabItem);
			index++;
		}
	}
	
	return children;
}
public Control getControl() {
	return tabFolder;
}
/**
 * Answer the number of children.
 */
public int getItemCount() {
	if (active)
		return tabFolder.getItemCount();
	else if (invisibleChildren != null)
		return invisibleChildren.length;
	else
		return 0;
	
}
/**
 * Get the parent control.
 */
public Composite getParent() {
	return tabFolder.getParent();
}
public int getSelection() {
	if (!active) return 0;
	return tabFolder.getSelectionIndex();
}
/**
 * Returns the tab for a part.
 */
private CTabItem getTab(LayoutPart child) {
	Iterator tabs = mapTabToPart.keySet().iterator();
	while (tabs.hasNext()) {
		CTabItem tab = (CTabItem) tabs.next();
		if (mapTabToPart.get(tab) == child)
			return tab;
	}
	
	return null;
}
/**
 * Returns the visible child.
 */
public LayoutPart getVisiblePart() {
	if(current == null)
		return inactiveCurrent;
	return current;
}
public int indexOf (LayoutPart item) {

	Iterator keys = mapTabToPart.keySet().iterator();
	while (keys.hasNext()) {
		CTabItem tab = (CTabItem)keys.next();
		LayoutPart part = (LayoutPart)mapTabToPart.get(tab);
		if (part.equals(item))
			return tabFolder.indexOf(tab);
	}
	
	return 0;
}
/**
 * See IVisualContainer#remove
 */
public void remove(LayoutPart child) {
	
	if (active && !(child instanceof PartPlaceholder)) {
		
		Iterator keys = mapTabToPart.keySet().iterator();
		while (keys.hasNext()) {
			CTabItem key = (CTabItem) keys.next();
			if (mapTabToPart.get(key).equals(child)) {
				removeTab(key);
				break;
			}
		}
	} else	if (invisibleChildren != null) {
		invisibleChildren = arrayRemove(invisibleChildren, child);
	}

	if (active) {
		child.setVisible(false);
		child.setContainer(null);
	}
}
private void removeTab(CTabItem tab) {
	// disable any d&d based on this tab
	LayoutPart part = (LayoutPart)mapTabToPart.get(tab);
	if (part != null)
		disableDrag(part);

	// remove the tab now
	// Note, that disposing of the tab causes the
	// tab folder to select the next tab and fires
	// a selection event. In this situation, do
	// not assign focus.
	assignFocusOnSelection = false;
	mapTabToPart.remove(tab);
	tab.dispose();
	assignFocusOnSelection = true;
	
	// Because the container's allow border api
	// is dependent on the number of tabs it has,
	// reset the container so the parts can update.
	if (mapTabToPart.size() == 1) {
		Iterator parts = mapTabToPart.values().iterator();
		((LayoutPart)parts.next()).setContainer(this);
	}
}
/**
 * Reorder the tab representing the specified pane.
 * If a tab exists under the specified x,y location,
 * then move the tab before it, otherwise place it
 * as the last tab.
 */
public void reorderTab(ViewPane pane, int x, int y) {
	CTabItem sourceTab = getTab(pane);
	if (sourceTab == null)
		return;

	// adjust the y coordinate to fall within the tab area
	Point location = new Point(1, 1);
	if ((tabFolder.getStyle() & SWT.BOTTOM) != 0)
		location.y = tabFolder.getSize().y - 4; // account for 3 pixel border

	// adjust the x coordinate to be within the tab area
	if (x > location.x)
		location.x = x;
		
	// find the tab under the adjusted location.
	CTabItem targetTab = tabFolder.getItem(location);

	// no tab under location so move view's tab to end
	if (targetTab == null) {
		// do nothing if already at the end
		if (tabFolder.indexOf(sourceTab) != tabFolder.getItemCount() - 1)
			reorderTab(pane, sourceTab, -1);
		
		return;
	}

	// do nothing if over view's own tab
	if (targetTab == sourceTab)
		return;

	// do nothing if already before target tab
	int sourceIndex = tabFolder.indexOf(sourceTab);
	int targetIndex = tabFolder.indexOf(targetTab);
	if (sourceIndex == targetIndex - 1)
		return;

	reorderTab(pane, sourceTab, targetIndex);
}
/**
 * Reorder the tab representing the specified pane.
 */
private void reorderTab(ViewPane pane, CTabItem sourceTab, int newIndex) {
	// remember if the source tab was the visible one
	boolean wasVisible = (tabFolder.getSelection() == sourceTab);

	// create the new tab at the specified index
	CTabItem newTab;
	if (newIndex < 0)
		newTab = new CTabItem(tabFolder, SWT.NONE);
	else
		newTab = new CTabItem(tabFolder, SWT.NONE, newIndex);

	// map it now before events start coming in...	
	mapTabToPart.put(newTab, pane);

	// update the drag & drop
	CTabPartDragDrop partDragDrop = (CTabPartDragDrop)mapPartToDragMonitor.get(pane);
	partDragDrop.setTab(newTab);

	// dispose of the old tab and remove it
	String sourceLabel = sourceTab.getText();
	mapTabToPart.remove(sourceTab);
	assignFocusOnSelection = false;
	sourceTab.dispose();
	assignFocusOnSelection = true;

	// update the new tab's title and visibility
	newTab.setText(sourceLabel);
	if (wasVisible) {
		tabFolder.setSelection(newTab);
		setSelection(pane);
		pane.setFocus();
	}
}
/**
 * Reparent a part. Also reparent visible children...
 */
public void reparent(Composite newParent) {
	if (!newParent.isReparentable())
		return;
		
	Control control = getControl();
	if ((control == null) || (control.getParent() == newParent))
		return;
		
	super.reparent(newParent);

	// reparent also the visible children.
	Iterator enum = mapTabToPart.values().iterator();
	while (enum.hasNext())
		((LayoutPart)enum.next()).reparent(newParent);
}
/**
 * See IVisualContainer#replace
 */
public void replace(LayoutPart oldChild, LayoutPart newChild) {
	
	if ((oldChild instanceof PartPlaceholder) && !(newChild instanceof PartPlaceholder)){
		replaceChild((PartPlaceholder)oldChild, newChild);
		return;
	}
		
	if (!(oldChild instanceof PartPlaceholder) && (newChild instanceof PartPlaceholder)){
		replaceChild(oldChild, (PartPlaceholder)newChild);
		return;
	}

}
private void replaceChild(LayoutPart oldChild, PartPlaceholder newChild) {
	
	// remove old child from display
	if (active) {
		Iterator keys = mapTabToPart.keySet().iterator();
		while (keys.hasNext()) {
			CTabItem key = (CTabItem)keys.next();
			LayoutPart part = (LayoutPart)mapTabToPart.get(key);
			if (part == oldChild) {
				boolean partIsActive = (current == oldChild);
				TabInfo info = new TabInfo();
				info.part = newChild;
				info.tabText = key.getText();
				removeTab(key);
				int index = 0;
				if (invisibleChildren != null)
					index = invisibleChildren.length;
				invisibleChildren = arrayAdd(invisibleChildren, info, index);
				oldChild.setVisible(false);
				oldChild.setContainer(null);
				newChild.setContainer(this);
				if (tabFolder.getItemCount() > 0 && !partIsActive) {
					setControlSize();
				}
				break;
			}
		}
	} else if (invisibleChildren != null) {
		for (int i = 0, length = invisibleChildren.length; i < length; i++){
			if (invisibleChildren[i].part == oldChild) {
				invisibleChildren[i].part = newChild;
			}
		}
	}

}
private void replaceChild(PartPlaceholder oldChild, LayoutPart newChild) {
	if (invisibleChildren == null) return;
	
	for (int i = 0, length = invisibleChildren.length; i < length; i++) {
		if (invisibleChildren[i].part == oldChild) {
			if (active) {
				TabInfo info = invisibleChildren[i];
				invisibleChildren = arrayRemove(invisibleChildren, oldChild);
				oldChild.setContainer(null);
				
				if (newChild instanceof PartPane) {
					WorkbenchPartReference ref = (WorkbenchPartReference)((PartPane)newChild).getPartReference();
					info.tabText = ref.getRegisteredName();
				}
				CTabItem item = createPartTab(newChild, info.tabText, -1);
				int index = tabFolder.indexOf(item);
				setSelection(index);
			} else {
				invisibleChildren[i].part = newChild;
				// On restore, all views are initially represented by placeholders and then 
				// they are replaced with the real views.  The following code is used to preserve the active 
				// tab when a prespective is restored from its persisted state.
				if (inactiveCurrent != null && inactiveCurrent == oldChild){
					current = newChild;
					inactiveCurrent = null;
				}
			}
			break;
		}
	}
}
/**
 * @see IPersistable
 */
public IStatus restoreState(IMemento memento) 
{
	// Read the active tab.
	String activeTabID = memento.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
	
	// Read the page elements.
	IMemento [] children = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
	if(children != null) {
		// Loop through the page elements.
		for (int i = 0; i < children.length; i ++) {
			// Get the info details.
			IMemento childMem = children[i];
			String partID = childMem.getString(IWorkbenchConstants.TAG_CONTENT);
			String tabText = childMem.getString(IWorkbenchConstants.TAG_LABEL);

			IViewDescriptor descriptor = (IViewDescriptor)WorkbenchPlugin.getDefault().
				getViewRegistry().find(partID);
			if(descriptor != null)
				tabText = descriptor.getLabel();

			// Create the part.
			LayoutPart part = new PartPlaceholder(partID);
			add(tabText, i, part);
			//1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
			part.setContainer(this);
			if (partID.equals(activeTabID)) {
				// Mark this as the active part.
				inactiveCurrent = part;
			}
		}
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}
/**
 * @see IPersistable
 */
public IStatus saveState(IMemento memento) 
{
	
	// Save the active tab.
	if (current != null)
		memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID, current.getID());

	if(mapTabToPart.size() == 0) {
		// Loop through the invisible children.
		if(invisibleChildren != null) {
			for (int i = 0; i < invisibleChildren.length; i ++) {
				// Save the info.
				// Fields in TabInfo ..
				//		private String tabText;
				//		private LayoutPart part;
				TabInfo info = invisibleChildren[i];
				IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
				childMem.putString(IWorkbenchConstants.TAG_LABEL, info.tabText);
				childMem.putString(IWorkbenchConstants.TAG_CONTENT, info.part.getID());
			}
		}
	} else {
		LayoutPart [] children = getChildren();
		CTabItem keys[] = new CTabItem[mapTabToPart.size()];
		mapTabToPart.keySet().toArray(keys);
		if(children != null) {
			for (int i = 0; i < children.length; i ++){
				IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
				childMem.putString(IWorkbenchConstants.TAG_CONTENT,children[i].getID());
				boolean found = false;
				for (int j = 0; j < keys.length; j++){
					if(mapTabToPart.get(keys[j]) == children[i]) {
						childMem.putString(IWorkbenchConstants.TAG_LABEL, keys[j].getText());
						found = true;
						break;
					}
				}
				if(!found) {
					for (int j = 0; j < invisibleChildren.length; j++){
						if(invisibleChildren[j].part == children[i]) {
							childMem.putString(IWorkbenchConstants.TAG_LABEL,invisibleChildren[j].tabText);
							found = true;
							break;
						}
					}
				}
				if(!found) {
					childMem.putString(IWorkbenchConstants.TAG_LABEL,"LabelNotFound");//$NON-NLS-1$
				}
			}
		}
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}
/**
 * Sets the presentation bounds.
 */
public void setBounds(Rectangle r) {
	if (tabFolder != null)
		tabFolder.setBounds(r);
	setControlSize();
}
/**
 * Set the size of a page in the folder.
 */
private void setControlSize() {
	if (current == null || tabFolder == null) 
		return;
	Rectangle bounds;
	if (mapTabToPart.size() > 1)
		bounds = calculatePageBounds(tabFolder);
	else
		bounds = tabFolder.getBounds();
	current.setBounds(bounds);
	current.moveAbove(tabFolder);
}

public void setSelection(int index) {
	if (!active) return;

	if (mapTabToPart.size() == 0) {
		setSelection(null);
		return;
	}

	// make sure the index is in the right range
	if (index < 0) index = 0;
	if (index > mapTabToPart.size() - 1) index = mapTabToPart.size() - 1;
	tabFolder.setSelection(index);

	CTabItem item = tabFolder.getItem(index);
	LayoutPart part = (LayoutPart)mapTabToPart.get(item);
	setSelection(part);
}
private void setSelection(LayoutPart part) {

	if (!active) return;
	if (part instanceof PartPlaceholder) return;

	// Deactivate old / Activate new.
	if (current != null && current != part){
		current.setVisible(false);
	}
	current = part;
	if (current != null) {
		setControlSize();
		current.setVisible(true);
	}

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // set the title of the detached window to reflact the active tab
	 * Window window = getWindow();
	 * if (window instanceof DetachedWindow) {
	 * 	if (current == null || !(current instanceof PartPane))
	 * 		window.getShell().setText("");//$NON-NLS-1$
	 * 	else
	 * 		window.getShell().setText(((PartPane)current).getPart().getTitle());
	 * }
	 */
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	return this;
}
}
