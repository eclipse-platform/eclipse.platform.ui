package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> 
    - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm; 
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;


/**
 * Represents a tab folder of editors. This layout part
 * container only accepts EditorPane parts.
 */
public class EditorWorkbook extends LayoutPart
	implements ILayoutContainer, IPropertyListener
{
	private static final int INACTIVE = 0;
	private static final int ACTIVE_FOCUS = 1;
	private static final int ACTIVE_NOFOCUS = 2;

	private static int tabLocation = -1; // Initialized in constructor.
	
	private int activeState = INACTIVE;
	private boolean assignFocusOnSelection = true;
	private boolean ignoreTabFocusHide = false;

	private boolean isZoomed = false;
	private Composite parent;
	private EditorCoolBar header;
	private ViewForm workbookForm;
	
	private EditorArea editorArea;
	private EditorPane visibleEditor;
	private EditorPartDragDrop editorPartDragDrop;

	private List editors = new ArrayList();
/**
 * EditorWorkbook constructor comment.
 */
public EditorWorkbook(EditorArea editorArea) {
	super("editor workbook");//$NON-NLS-1$
	this.editorArea = editorArea;
	// Each workbook has a unique ID so
	// relative positioning is unambiguous.
	setID(this.toString());

	// Get tab location preference.
	if (tabLocation == -1)
		tabLocation = getPreferenceStore().getInt(
			IPreferenceConstants.EDITOR_TAB_POSITION);
}
/**
 * See ILayoutContainer::add
 *
 * Note: the workbook currently only accepts
 * editor parts.
 */
public void add(LayoutPart part) {
	if (part instanceof EditorPane) {
		EditorPane editorPane = (EditorPane) part;
		editors.add(editorPane);
		editorPane.setWorkbook(this);
		editorPane.setZoomed(isZoomed);
		if (workbookForm != null) {
			createPage(editorPane);
			setVisibleEditor(editorPane);
		}	
	}
}
/**
 * See ILayoutContainer::allowBorder
 *
 * There is already a border around the tab
 * folder so no need for one from the parts.
 */
public boolean allowsBorder() {
	return false;
}
public void becomeActiveWorkbook(boolean hasFocus) {
	EditorArea area = getEditorArea();
	if (area != null)
		area.setActiveWorkbook(this, hasFocus);
}

public void addBookMark(IEditorReference ref) {
	header.updateBookMarks(ref); 
}

public void createControl(Composite parent) {

	if (workbookForm != null)
		return;

	this.parent = parent;
	workbookForm = new ViewForm(parent,SWT.BORDER);
	workbookForm.setContent(new Composite(workbookForm,SWT.NONE));
	workbookForm.setBorderVisible(true);
	
	header = new EditorCoolBar(getEditorArea().getWorkbenchWindow(),this,tabLocation); 
	header.createControl(workbookForm);
	workbookForm.setTopLeft(header.getControl());
	
	// redirect drop request to the workbook
	workbookForm.setData((IPartDropTarget) this);

	// listener to resize visible components
	workbookForm.addListener(SWT.Resize, new Listener() {
		public void handleEvent(Event e) {
			setControlSize(visibleEditor);
		}
	});

	// infw implemented in EditorCoolBar, but it is slow ...
	// listen for mouse down on tab area to set focus.
//	tabFolder.addMouseListener(new MouseAdapter() {
//		public void mouseDoubleClick(MouseEvent event) {
//			doZoom();
//		}
//
//		public void mouseDown(MouseEvent e) {
//			if (visibleEditor != null) {
//				visibleEditor.setFocus();
//				CTabItem item = getTab(visibleEditor);
//				Rectangle bounds = item.getBounds();
//				if(bounds.contains(e.x,e.y)) {
//					if (e.button == 3)
//						visibleEditor.showPaneMenu(tabFolder,new Point(e.x, e.y));
//					else if((e.button == 1) && overImage(item,e.x))
//						visibleEditor.showPaneMenu();
//				}
//			}
//		}
//	});

	// infw - in EditorCoolBar
	// register the interested mouse down listener
//	if (!mouseDownListenerAdded && getEditorArea() != null) {
//		tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
//		mouseDownListenerAdded = true;
//	}

	// Enable drop target data
	enableDrop(this);

	// Create tabs.
	Iterator enum = editors.iterator();
	while (enum.hasNext()) {
		EditorPane pane = (EditorPane) enum.next();
		createPage(pane);
	}

	// Set active tab.
	if (visibleEditor != null)
		setVisibleEditor(visibleEditor);
	else if (getItemCount() > 0)
		setVisibleEditor((EditorPane) editors.get(0));
}

public void openEditorList() {
	if(header != null)
		header.openEditorList();
}
/**
 * Show a title label menu for this pane.
 */
public void showPaneMenu() {
	if (visibleEditor != null) {
		Rectangle bounds = header.getControl().getBounds();
		visibleEditor.showPaneMenu(header.getControl(),new Point(bounds.x,bounds.height));
	}
}
/*
 * Return true if <code>x</code> is over the label image.
 */
protected boolean overImage(EditorPane pane,int x) {
	Rectangle imageBounds = header.getLabelImage().getBounds();
	return x < (pane.getBounds().x + imageBounds.x + imageBounds.width);
	//infw where used, what about over image of favourites?
}		
/**
 * Create a page and tab for an editor.
 */
private void createPage(EditorPane editorPane) {
	editorPane.createControl(parent);
	editorPane.setContainer(this);
	enableDrop(editorPane);
	// Update tab to be in-sync after creation of
	// pane's control since prop listener was not on
	IEditorReference editorRef = editorPane.getEditorReference();
	updateEditorTab(editorRef);
	editorRef.addPropertyListener(this);
}
/**
 * See LayoutPart#dispose
 */
public void dispose() {
	if (workbookForm == null) 
		return;

	for (int i = 0; i < editors.size(); i++)
		removeListeners((EditorPane)editors.get(i));
	editors.clear();
	//Reset the visible editor so that no references are made to it.
	setVisibleEditor(null);

	workbookForm.dispose();
	workbookForm = null;
}
/**
 * Zooms in on the active page in this workbook.
 */
private void doZoom() {
	if (visibleEditor == null)
		return;
	visibleEditor.getPage().toggleZoom(visibleEditor.getPartReference());
}
/**
 * Draws the applicable gradient on the active tab
 */
/* package */ void drawGradient() {
	if (workbookForm == null)
		return;
		
	Color fgColor;
	Color[] bgColors;
	int[] bgPercents;
	
	switch (activeState) {
		case ACTIVE_FOCUS :
			if (getShellActivated()) {
				fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
				bgColors = WorkbenchColors.getActiveEditorGradient();
				bgPercents = WorkbenchColors.getActiveEditorGradientPercents();
			}
			else {
				fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
				bgColors = WorkbenchColors.getDeactivatedEditorGradient();
				bgPercents = WorkbenchColors.getDeactivatedEditorGradientPercents();
			}
			break;
		case ACTIVE_NOFOCUS :
			fgColor = WorkbenchColors.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
			bgColors = WorkbenchColors.getActiveNoFocusEditorGradient();
			bgPercents = WorkbenchColors.getActiveNoFocusEditorGradientPercents();
			break;
		case INACTIVE :
		default :
			fgColor = null;
			bgColors = null;
			bgPercents = null;
			break;
	}
	
	header.getDragControl().setForeground(fgColor);
	header.getDragControl().setBackground(bgColors, bgPercents);
	header.getDragControl().update();
}
/**
 * enableDrop
 */
private void enableDrop(LayoutPart part) {
	Control control = part.getControl();
	if (control != null)
		control.setData((IPartDropTarget)this); // Use workbook as drop target, not part itself.
}
private void updateDrag() {
	if(editors.size() > 1 || editorArea.getEditorWorkbookCount() > 1) {
		if(editorPartDragDrop == null) {
			editorPartDragDrop = new EditorPartDragDrop(getVisibleEditor(),header.getDragControl());
			editorPartDragDrop.addDropListener(getEditorArea().getPartDropListener());
		} else {
			editorPartDragDrop.setSourcePart(getVisibleEditor());
		}
	} else if(editorPartDragDrop != null) {
		editorPartDragDrop.dispose();
	}
}
/**
 * Gets the presentation bounds.
 */
public Rectangle getBounds() {
	if (workbookForm == null)
		return new Rectangle(0, 0, 0, 0);
	return workbookForm.getBounds();
}

// getMinimumHeight() added by cagatayk@acm.org 
/**
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	if (workbookForm != null && !workbookForm.isDisposed())
		/* Subtract 1 for divider line, bottom border is enough
		 * for editor tabs. 
		 */
		return workbookForm.computeTrim(0, 0, 0, 0).height - 1;
 	else
 		return super.getMinimumHeight();
}

/**
 * See ILayoutContainer::getChildren
 */
public LayoutPart[] getChildren() {
	int nSize = editors.size();
	LayoutPart [] children = new LayoutPart[nSize];
	editors.toArray(children);
	return children;
}
/**
 * Get the part control.  This method may return null.
 */
public Control getControl() {
	return workbookForm;
}
/**
 * Return the editor area to which this editor
 * workbook belongs to.
 */
public EditorArea getEditorArea() {
	return editorArea;
}
/**
 * Answer the number of children.
 */
public int getItemCount() {
	return editors.size();
}
/**
 * Return the composite used to parent all
 * editors within this workbook.
 */
public Composite getParent() {
	return this.parent;
}
/**
 * Returns the tab list to use when this workbook is active.
 * Includes the active editor and its tab, in the appropriate order.
 */
public Control[] getTabList() {
	if (header == null) {
		return new Control[0];
	}
	if (visibleEditor == null) {
		return new Control[] { workbookForm };
	}
	if ((tabLocation & SWT.TOP) != 0) {
		return new Control[] { workbookForm, visibleEditor.getControl() };
	} else {
		return new Control[] { visibleEditor.getControl(), workbookForm };
	}
}
/**
 * Returns the visible child.
 */
public EditorPane getVisibleEditor() {
	return visibleEditor;
}
/**
 * Returns true if this editor workbook is the
 * active one within the editor area.
 */
public boolean isActiveWorkbook() {
	return getEditorArea().isActiveWorkbook(this);
}
/**
 * See LayoutPart
 */
public boolean isDragAllowed(Point p) {
	if (isZoomed) {
		return false;
	} else if (getEditorArea().getEditorWorkbookCount() == 1) {
		return false;
	} else if (visibleEditor != null) {
		if(!overImage(visibleEditor, p.x))
			return true;
	}
	return false;
}
/**
 * Open the tracker to allow the user to move
 * the specified part using keyboard.
 */
public void openTracker(LayoutPart part) {
	editorPartDragDrop.setSourcePart(part);
	editorPartDragDrop.openTracker();
	updateDrag();
}

/**
 * Returns true if this part is visible.  A part is visible if it has a control.
 */
public boolean isVisible() {
	return (workbookForm != null);
}
/**
 * Listen for notifications from the editor part
 * that its title has change or it's dirty, and
 * update the corresponding tab
 *
 * @see IPropertyListener
 */
public void propertyChanged(Object source, int property) {
	if (property == IEditorPart.PROP_DIRTY || property == IWorkbenchPart.PROP_TITLE) {
		if (source instanceof IEditorPart) {
			updateEditorTab((IEditorPart)source);
		}
	}
}
/**
 * See ILayoutContainer::remove
 *
 * Note: workbook only handles editor parts.
 */
public void remove(LayoutPart child) {
	// Get editor position.
	int tabIndex = editors.indexOf(child);
	if (tabIndex < 0)
		return;

	// Dereference the old editor.  
	// This must be done before "show" to get accurate decorations.
	editors.remove(child);
	removeListeners((EditorPane)child);
	
	// Show new editor
	if (visibleEditor == child) {
		EditorPane nextEditor = null;
		int maxIndex = editors.size() - 1;
		if (maxIndex >= 0) {
			tabIndex = Math.min(tabIndex, maxIndex);
			nextEditor = (EditorPane)editors.get(tabIndex);
		}
		if (workbookForm != null) {
			// Dispose old editor.
			child.setContainer(null);
		}
		setVisibleEditor(nextEditor);
	} else if (workbookForm != null) {
		// Dispose old editor.
		child.setContainer(null);
	}
}
/**
 * See IVisualContainer#remove
 */
public void removeAll() {
	// Show empty space.
	setVisibleEditor(null);

	// Dispose of all tabs.
	if (workbookForm != null) {
		Iterator tabs = editors.iterator();
		while (tabs.hasNext()) {
			EditorPane child = (EditorPane)tabs.next();
			removeListeners(child);
			child.setContainer(null);
		}
	}
	editors.clear();
}

private void removeListeners(EditorPane editor) {
	if (editor == null)
		return;
	editor.getPartReference().removePropertyListener(this);
}
/**
 * Reorder the tab representing the specified pane.
 * If a tab exists under the specified x,y location,
 * then move the tab before it, otherwise place it
 * as the last tab.
 */
public void reorderTab(EditorPane pane, int x, int y) {
//	CTabItem sourceTab = getTab(pane);
//	if (sourceTab == null)
//		return;
//
//	// adjust the y coordinate to fall within the tab area
//	Point location = new Point(1, 1);
//	if ((tabFolder.getStyle() & SWT.BOTTOM) != 0)
//		location.y = tabFolder.getSize().y - 4; // account for 3 pixel border
//
//	// adjust the x coordinate to be within the tab area
//	if (x > location.x)
//		location.x = x;
//		
//	// find the tab under the adjusted location.
//	CTabItem targetTab = tabFolder.getItem(location);
//
//	// no tab under location so move editor's tab to end
//	if (targetTab == null) {
//		// do nothing if already at the end
//		if (tabFolder.indexOf(sourceTab) != tabFolder.getItemCount() - 1)
//			reorderTab(pane, sourceTab, -1);
//		
//		return;
//	}
//
//	// do nothing if over editor's own tab
//	if (targetTab == sourceTab)
//		return;
//
//	// do nothing if already before target tab
//	int sourceIndex = tabFolder.indexOf(sourceTab);
//	int targetIndex = tabFolder.indexOf(targetTab);
//	
//	if (sourceIndex == targetIndex - 1)
//		return;
//		
//	//Source is going to be dispose so the target index will change.
//	if (sourceIndex < targetIndex)
//		targetIndex--;
//		
//	reorderTab(pane, sourceTab, targetIndex);
}
/**
 * Move the specified editor to the a new position. 
 * Move to the end if <code>newIndex</code> is less then
 * zero.
 */
public void reorderTab(EditorPane pane,int newIndex) {
//	reorderTab(pane,getTab(pane),newIndex);
}

/**
 * Reorder the tab representing the specified pane.
 */
//private void reorderTab(EditorPane pane, CTabItem sourceTab, int newIndex) {
//	// remember if the source tab was the visible one
//	boolean wasVisible = (tabFolder.getSelection() == sourceTab);
//
//	// Remove old tab.
//	disableTabDrag(pane);
//	removeTab(sourceTab);
//
//	// Create the new tab at the specified index
//	CTabItem newTab;
//	if (newIndex < 0)
//		newTab = createTab(pane);
//	else
//		newTab = createTab(pane, newIndex);
//	CTabPartDragDrop partDragDrop = (CTabPartDragDrop)mapPartToDragMonitor.get(pane);
//	partDragDrop.setTab(newTab);
//
//	// update order of editors.
//	editors.remove(pane);
//	if (newIndex < 0)
//		editors.add(pane);
//	else
//		editors.add(newIndex, pane);
//
//	// update the new tab's visibility but do
//	// not set focus...caller's responsibility.
//	// Note, if the pane already had focus, it
//	// will still have it after the tab order change.
//	if (wasVisible) {
//		tabFolder.setSelection(newTab);
//		setVisibleEditor(pane);
//	}
//}
/**
 * See ILayoutContainer::replace
 *
 * Note: this is not currently supported
 */
public void replace(LayoutPart oldPart, LayoutPart newPart) {
}
/**
 * Sets the gradient state of the active tab
 */
private void setActiveState(int state) {
	if (activeState != state) {
		activeState = state;
		drawGradient();
	}
}
/**
 * Sets the presentation bounds.
 */
public void setBounds(Rectangle r) {
	if (workbookForm != null) {
		workbookForm.setBounds(r);
//		header.getControl().setBounds(workbookForm.getTopLeft().getBounds());
//		setControlSize(visibleEditor);
	}
}
/**
 * Sets the parent for this part.
 */
public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
}
/**
 * Set the size of a page in the folder.
 */
private void setControlSize(LayoutPart part) {
	if (part == null || workbookForm == null || part.getControl() == null) 
		return;
	
	Control formControl = workbookForm.getContent();
	Control partControl = part.getControl();

	Rectangle contentBounds = formControl.getBounds();
	Point position = new Point(contentBounds.x,contentBounds.y);
	position = partControl.getParent().toControl(formControl.getParent().toDisplay(position));
	contentBounds.x = position.x;
	contentBounds.y = position.y;
	part.setBounds(contentBounds);
	part.getControl().moveAbove(workbookForm);
}
public void setVisibleEditor(EditorPane comp) {

	if (workbookForm == null) {
		visibleEditor = comp;
		return;
	}
	
	if(comp != null) {
		//Make sure the EditorPart is created.
		Object part = comp.getPartReference().getPart(true);
		if(part == null)
			comp = null;
	}

	// Hide old part. Be sure that it is not in the middle of closing
	if (visibleEditor != null && visibleEditor != comp && visibleEditor.getControl()!= null){
		visibleEditor.getControl().setVisible(false);
	}

	// Show new part.
	visibleEditor = comp;
	if (visibleEditor != null) {
		setControlSize(visibleEditor);
		if(visibleEditor.getControl() != null) {
			visibleEditor.getControl().setVisible(true);
		}
		becomeActiveWorkbook(activeState == ACTIVE_FOCUS);
		updateEditorTab(((IEditorPart)comp.getPartReference().getPart(true)));
	} else {
		header.updateEmptyEditorLabel();
	}
	updateDrag();
	
}
public void tabFocusHide() {
	if (workbookForm == null || ignoreTabFocusHide) 
		return;

	if (isActiveWorkbook())
		setActiveState(ACTIVE_NOFOCUS);
	else
		setActiveState(INACTIVE);
}
public void tabFocusShow(boolean hasFocus) {
	if (workbookForm == null) 
		return;

	if (hasFocus)
		setActiveState(ACTIVE_FOCUS);
	else
		setActiveState(ACTIVE_NOFOCUS);
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	if (dragSource instanceof EditorPane || dragSource instanceof EditorWorkbook)
		return this;
	else
		return getEditorArea();
}
/**
 * Update the tab for an editor.  This is typically called
 * by a site when the tab title changes.
 */
public void updateEditorTab(IEditorPart part) {
	PartPane pane = ((EditorSite)part.getSite()).getPane();
	String title = part.getTitle();
	boolean isDirty = part.isDirty();
	Image image = part.getTitleImage();
	String toolTip = part.getTitleToolTip();
	updateEditorTab(pane,title,isDirty,image,toolTip);
}
/**
 * Update the tab for an editor.  This is typically called
 * by a site when the tab title changes.
 */
public void updateEditorTab(IEditorReference ref) {
	PartPane pane = ((WorkbenchPartReference)ref).getPane();
	String title = ref.getTitle();
	boolean isDirty = ref.isDirty();
	Image image = ref.getTitleImage();
	String toolTip = ref.getTitleToolTip();
	updateEditorTab(pane,title,isDirty,image,toolTip);
}
/**
 * Update the tab for an editor.  This is typically called
 * by a site when the tab title changes.
 */
public void updateEditorTab(PartPane pane,String title,boolean isDirty,Image image,String toolTip) {
	header.updateEditorLabel(title, isDirty, image, toolTip);
}
/**
 * Zoom in on the active part.
 */
public void zoomIn() {
	if (isZoomed)
		return;
	isZoomed = true;

	// Mark it's editors as zoom in
	Iterator iterator = editors.iterator();
	while (iterator.hasNext())
		((EditorPane) iterator.next()).setZoomed(true);
}
/**
 * Zoom out and show all editors.
 */
public void zoomOut() {
	if (!isZoomed)
		return;
	isZoomed = false;
	
	// Mark it's editors as zoom out
	Iterator iterator = editors.iterator();
	while (iterator.hasNext())
		((EditorPane) iterator.next()).setZoomed(false);
}
/**
 * Method getEditors.
 * @return EditorPane
 */
public EditorPane [] getEditors() {
	int nSize = editors.size();
	EditorPane [] children = new EditorPane[nSize];
	editors.toArray(children);
	return children;
}
}
