
/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *  IBM Corporation - initial API and implementation 
 * 	Cagatay Kavukcuoglu <cagatayk@acm.org> - Fix for bug 10025 - Resizing views 
 *    should not use height ratios		
 ************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.ui.internal.dnd.DragUtil;

/**
 * Represents a tab folder of editors. This layout part
 * container only accepts EditorPane parts.
 */
public abstract class EditorWorkbook
	extends LayoutPart
	implements ILayoutContainer, IPropertyListener, IWorkbenchDragSource {
	private static final int INACTIVE = 0;
	private static final int ACTIVE_FOCUS = 1;
	private static final int ACTIVE_NOFOCUS = 2;

	private EditorArea editorArea;
	private List editors = new ArrayList();
	private EditorPane visibleEditor;
	protected Composite parent;
	private int activeState = INACTIVE;
	private boolean isZoomed = false;
	private Map mapPartToDragMonitor = new HashMap();

	/**
	 * Factory method for editor workbooks.
	 */
	public static EditorWorkbook newEditorWorkbook(EditorArea editorArea) {
		return new TabbedEditorWorkbook(editorArea);
	}

	/**
	 * Constructs a new EditorWorkbook.
	 */
	protected EditorWorkbook(EditorArea editorArea) {
		super("editor workbook"); //$NON-NLS-1$
		this.editorArea = editorArea;
		// Each workbook has a unique ID so
		// relative positioning is unambiguous.
		setID(this.toString());

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
			if (getControl() != null) {
				createItem(editorPane);
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

	public void createControl(Composite parent) {

		if (getControl() != null)
			return;

		this.parent = parent;

		createPresentation(parent);

		// Enable drop target data
		enableDrop(this);

		// Create items.
		Iterator enum = editors.iterator();
		while (enum.hasNext()) {
			EditorPane pane = (EditorPane) enum.next();
			createItem(pane);
			createPage(pane);
		}

		// Set active tab.
		if (visibleEditor != null)
			setVisibleEditor(visibleEditor);
		else if (getItemCount() > 0)
			setVisibleEditor((EditorPane) editors.get(0));
	}

	protected abstract void createPresentation(Composite parent);

	/**
	 * Show a title label menu for this pane.
	 */
	public abstract void showPaneMenu();

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
	 * Creates the presentation item for the given editor.
	 * 
	 * @return the item representing the editor
	 */
	protected abstract Object createItem(EditorPane editorPane);

	/**
	 * See LayoutPart#dispose
	 */
	public void dispose() {
		if (getControl() == null)
			return;

		for (int i = 0; i < editors.size(); i++) {
			removeListeners((EditorPane) editors.get(i));
		}
		editors.clear();

		// Reset the visible editor so that no references are made to it.
		setVisibleEditor(null);

		disposePresentation();
	}

	/**
	 * Disposes the presentation of the editors.
	 */
	protected abstract void disposePresentation();

	/**
	 * Zooms in on the active page in this workbook.
	 */
	protected void doZoom() {
		if (visibleEditor == null)
			return;
		visibleEditor.getPage().toggleZoom(visibleEditor.getPartReference());
	}

	/**
	 * Draws the applicable gradient on the active item.
	 */
	public void drawGradient() {
		Color fgColor;
		Color[] bgColors = new Color[1];
		int[] bgPercents = null;

		switch (activeState) {
			case ACTIVE_FOCUS :
				fgColor = WorkbenchColors.getActiveEditorForeground();
				bgColors = WorkbenchColors.getActiveEditorGradient();
				bgPercents = WorkbenchColors.getActiveEditorGradientPercents();
				break;
			case ACTIVE_NOFOCUS :
				fgColor = WorkbenchColors.getActiveEditorForeground();
				bgColors = WorkbenchColors.getActiveNoFocusEditorGradient();
				bgPercents = WorkbenchColors.getActiveNoFocusEditorGradientPercents();
				break;
			case INACTIVE :
				fgColor = JFaceColors.getTabFolderSelectionForeground(getControl().getDisplay());
				bgColors[0] = JFaceColors.getTabFolderSelectionBackground(getControl().getDisplay());
				bgPercents = null;
				break;
			default :
				fgColor = null;
				bgColors[0] = null;
				bgPercents = null;
				break;
		}
		drawGradient(fgColor, bgColors, bgPercents,activeState == ACTIVE_FOCUS);
	}

	protected abstract void drawGradient(Color fgColor, Color[] bgColors, int[] bgPercents, boolean activeState);

	/**
	 * enableDrop
	 */
	private void enableDrop(LayoutPart part) {
		Control control = part.getControl();
		if (control != null)
			control.setData(this); // Use workbook as drop target, not part itself.
	}
	/**
	 * Gets the presentation bounds.
	 */
	public Rectangle getBounds() {
		if (getControl() == null)
			return new Rectangle(0, 0, 0, 0);
		return getControl().getBounds();
	}

	/**
	 * See ILayoutContainer::getChildren
	 */
	public LayoutPart[] getChildren() {
		int nSize = editors.size();
		LayoutPart[] children = new LayoutPart[nSize];
		editors.toArray(children);
		return children;
	}
	/**
	 * Get the part control.  This method may return null.
	 */
	public abstract Control getControl();

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
	public abstract Control[] getTabList();

	/**
	 * Makes sure the visible editor's item is visible.
	 */
	public abstract void showVisibleEditor();

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
			if (!isDragAllowed(visibleEditor, p))
				return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if, as far as the workbook is concerned,
	 * a drag is allowed when the user clicks down at the given point.
	 */
	public abstract boolean isDragAllowed(EditorPane pane, Point p);

	/**
	 * Open the tracker to allow the user to move
	 * the specified part using keyboard.
	 */
	public void openTracker(LayoutPart part) {
		DragUtil.performDrag(part, DragUtil.getDisplayBounds(part.getControl()));
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
				updateEditorTab((IEditorPart) source);
			}
		}
	}
	/**
	 * See ILayoutContainer::remove
	 *
	 * Note: workbook only handles editor parts.
	 */
	public void remove(LayoutPart child) {
		if (!(child instanceof EditorPane)) {
			return;
		}
		EditorPane editorPane = (EditorPane) child;

		int index = editors.indexOf(editorPane);
		if (index == -1) {
			return;
		}

		// Dereference the old editor.  
		// This must be done before "show" to get accurate decorations.
		editors.remove(editorPane);
		removeListeners(editorPane);

		// Show new editor
		if (visibleEditor == editorPane) {
			EditorPane nextEditor = null;
			int maxIndex = editors.size() - 1;
			if (maxIndex >= 0) {
				index = Math.min(index, maxIndex);
				nextEditor = (EditorPane) editors.get(index);
			}
			if (getControl() != null) {
				disposeItem(editorPane);
				editorPane.setContainer(null);
			}
			setVisibleEditor(nextEditor);
		} else if (getControl() != null) {
			disposeItem(editorPane);
			editorPane.setContainer(null);
		}
	}

	/**
	 * Removes the presentation item for the given editor. 
	 */
	protected abstract void disposeItem(EditorPane editorPane);

	/**
	 * Removes all editors from the workbook, disposing any presentation items,
	 * and unhooking all listeners.  The editors themselves are not disposed.
	 */
	public void removeAll() {
		// Show empty space.
		setVisibleEditor(null);

		for (Iterator i = editors.iterator(); i.hasNext();) {
			EditorPane child = (EditorPane) i.next();
			removeListeners(child);
		}
		if (getControl() != null) {
			disposeAllItems();
		}
		for (Iterator i = editors.iterator(); i.hasNext();) {
			EditorPane child = (EditorPane) i.next();
			child.setContainer(null);
		}
		editors.clear();
	}

	protected abstract void disposeAllItems();

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
	public abstract void reorderTab(EditorPane pane, int x, int y);

	/**
	 * Move the specified editor to the a new position. 
	 * Move to the end if <code>newIndex</code> is less then
	 * zero.
	 */
	public abstract void reorderTab(EditorPane pane, int newIndex);

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
		if (getControl() != null) {
			getControl().setBounds(r);
			setControlSize();
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
	protected abstract void setControlSize();

	public void setVisibleEditor(EditorPane comp) {

		if (getControl() == null) {
			visibleEditor = comp;
			return;
		}

		if (comp != null) {
			//Make sure the EditorPart is created.
			Object part = comp.getPartReference().getPart(true);
			if (part == null)
				comp = null;
		}

		// Hide old part. Be sure that it is not in the middle of closing
		if (visibleEditor != null && visibleEditor != comp) {
			visibleEditor.setVisible(false);
		}

		// Show new part.
		visibleEditor = comp;
		if (visibleEditor != null) {
			setVisibleItem(visibleEditor);
			setControlSize();
			if (visibleEditor != null) {
				visibleEditor.setVisible(true);
			}
			becomeActiveWorkbook(activeState == ACTIVE_FOCUS);
		}
	}

	protected abstract void setVisibleItem(EditorPane editorPane);

	public void tabFocusHide() {
		if (getControl() == null)
			return;

		if (isActiveWorkbook())
			setActiveState(ACTIVE_NOFOCUS);
		else
			setActiveState(INACTIVE);
	}

	public void tabFocusShow(boolean hasFocus) {
		if (getControl() == null)
			return;

		if (hasFocus)
			setActiveState(ACTIVE_FOCUS);
		else
			setActiveState(ACTIVE_NOFOCUS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#targetPartFor(org.eclipse.ui.internal.IWorkbenchDragSource)
	 */
	public LayoutPart targetPartFor(IWorkbenchDragSource dragSource) {

		if (dragSource.getType() == EDITOR)
			return this;
		else
			return getEditorArea();
		
	}

	// TODO: can one of the updateEditorTab methods be removed?

	/**
	 * Update the tab for an editor.  This is typically called
	 * by a site when the tab title changes.
	 */
	public void updateEditorTab(IEditorPart part) {
		EditorPane pane = (EditorPane) ((EditorSite) part.getSite()).getPane();
		updateItem(pane);
	}

	/**
	 * Update the tab for an editor.  This is typically called
	 * by a site when the tab title changes.
	 */
	public void updateEditorTab(IEditorReference ref) {
		EditorPane pane = (EditorPane) ((WorkbenchPartReference) ref).getPane();
		updateItem(pane);
	}

	protected abstract void updateItem(EditorPane editorPane);

	/**
	 * Zoom in on the active part.
	 */
	public void zoomIn() {
		if (isZoomed)
			return;
		isZoomed = true;

		// Mark its editors as zoomed in
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

		// Mark its editors as zoomed out
		Iterator iterator = editors.iterator();
		while (iterator.hasNext())
			 ((EditorPane) iterator.next()).setZoomed(false);
	}
	/**
	 * Returns the collection of editors.
	 */
	public EditorPane[] getEditors() {
		EditorPane[] children = new EditorPane[editors.size()];
		editors.toArray(children);
		return children;
	}

	/**
	 * Returns the actual list of editors.
	 * Subclasses occasionally need to adjust the order of items in this list,
	 * but normally should not add or remove items from the list.
	 */
	protected List getEditorList() {
		return editors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#getType()
	 */
	public int getType() {
		return EDITOR | VIEW;
	}
	
	/**
	 * Retained for compatibility with ide.
	 * @deprecated Do not call this.
	 * @return
	 */
	public static boolean usingNewDropDown(){
		return false;
	}
}
