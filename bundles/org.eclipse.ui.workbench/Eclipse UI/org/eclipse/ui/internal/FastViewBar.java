/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;

import org.eclipse.ui.IViewReference;

import org.eclipse.ui.internal.dnd.AbstractDragSource;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDragSource;
import org.eclipse.ui.internal.dnd.IDropTarget;

/**
 * Represents the fast view bar.
 * 
 * <p>The set of fastviews are obtained from the WorkbenchWindow that 
 * is passed into the constructor. The set of fastviews may be refreshed to 
 * match the state of the perspective by calling the update(...) method.</p>
 * 
 * @see org.eclipse.ui.internal.FastViewPane
 */
class FastViewBar implements IWindowTrim {
	private ToolBarManager fastViewBar;
	private Menu fastViewBarMenu;
	private Menu sidesMenu; 
	private WorkbenchWindow window;
	private MenuItem restoreItem;
	private IViewReference selection;
	private boolean visible = false;
	private Composite control;
	private GridData toolBarData;
	private static final int HIDDEN_WIDTH = 5;
	private MenuItem showOn;
	private MenuItem closeItem;
	private IntModel side = new IntModel(SWT.LEFT);
	private RadioMenu radioButtons;
	private IViewReference selectedView;
	
	/**
	 * Constructs a new fast view bar for the given workbench window.
	 * 
	 * @param theWindow
	 */
	public FastViewBar(WorkbenchWindow theWindow) {
		window = theWindow;
		
		fastViewBar = new ToolBarManager(SWT.FLAT | SWT.WRAP | SWT.VERTICAL);
		fastViewBar.add(new ShowFastViewContribution(window));
	}
	
	/**
	 * Creates the underlying SWT control for the fast view bar. Will add exactly
	 * one new control to the given composite. Makes no assumptions about the layout
	 * being used in the parent composite.
	 * 
	 * @param parent enclosing SWT composite
	 */
	public void createControl(Composite parent) {
		control = new Composite(parent, SWT.NONE);
					
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MenuDetect) {
					showFastViewBarPopup(new Point(event.x, event.y));
				}
			}
		};

		fastViewBar.createControl(control);
		getToolBar().addListener(SWT.MenuDetect, listener);
		
		IDragSource fastViewDragSource = new AbstractDragSource() {
			
			IViewReference oldFastView = null;
			
			public Object getDraggedItem(Point position) {
				IViewReference ref = getViewAt(position);

				// If no fastview icons here, drag the fastview bar itself
				if (ref == null) {
					return FastViewBar.this;
				}
				
				ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();
				
				return pane;
			}

			public Rectangle getDragRectangle(Object draggedItem) {
				// If we're dragging the fastview bar itself..
				if (draggedItem instanceof FastViewBar) {
					return DragUtil.getDisplayBounds(control);
				}
				
				ViewPane pane = (ViewPane)draggedItem;
				
				ToolItem item = itemFor(pane.getViewReference());
				
				return Geometry.toDisplay(getToolBar(), item.getBounds()); 
			}

			public void dragFinished(Object draggedItem, boolean success) {

				if (oldFastView != null) {
					Perspective persp = window.getActiveWorkbenchPage().getActivePerspective();
					
					if (persp.isFastView(oldFastView)) {
						persp.setActiveFastView(oldFastView);
					}
				}
				
				oldFastView = null;
			}

			public void dragStarted(Object draggedItem) {				
				Perspective persp = window.getActiveWorkbenchPage().getActivePerspective();
				
				oldFastView = persp.getActiveFastView();
				
				persp.setActiveFastView(null, 0);
			}
		};
		
		IDragOverListener fastViewDragTarget = new IDragOverListener() {

			class ViewDropTarget extends AbstractDropTarget {
				ViewPane pane;
				ToolItem position;
				
				public ViewDropTarget(ViewPane toDrop, ToolItem position) {
					pane = toDrop;
					this.position = position;
				}
				
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
				 */
				public void drop() {
					IViewReference view = getViewFor(position);
					
					pane.getPage().addFastView(pane.getViewReference());
					pane.getPage().getActivePerspective().moveFastView(pane.getViewReference(), view);
					update(true);
				}
				
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
				 */
				public Cursor getCursor() {
					return DragCursors.getCursor(DragCursors.FASTVIEW);
				}
				
				public Rectangle getSnapRectangle() {
					if (position == null) {
						return DragUtil.getDisplayBounds(getControl());
					} else {
						return Geometry.toDisplay(getToolBar(), position.getBounds());
					}
				}
			}
			
			public IDropTarget drag(Control currentControl, Object draggedObject, Point position, Rectangle dragRectangle) {
				if (draggedObject instanceof ViewPane) {
					ViewPane pane = (ViewPane) draggedObject;
					ToolItem targetItem = getToolItem(position);
					ToolItem sourceItem = itemFor(pane.getViewReference());
					
					if (sourceItem != null && (sourceItem == targetItem || targetItem == null)) {
						return null;
					}
					
					return new ViewDropTarget((ViewPane)draggedObject, targetItem);
				}
				
				return null;
			}
			
		};
		
		GridLayout controlLayout = new GridLayout();
		controlLayout.marginHeight = 0;
		controlLayout.marginWidth = 0;
		control.setLayout(controlLayout);
		
		toolBarData = new GridData(GridData.FILL_BOTH);
		toolBarData.widthHint = HIDDEN_WIDTH;
		
		getToolBar().setLayoutData(toolBarData);
		
		DragUtil.addDragSource(getToolBar(), fastViewDragSource);
		DragUtil.addDragTarget(getControl(), fastViewDragTarget);
		
		update(true);
	}

	/**
	 * Returns the toolbar for the fastview bar.
	 * 
	 * @return
	 */
	private ToolBar getToolBar() {
		return fastViewBar.getControl();
	}
	
	private IViewReference getViewFor(ToolItem item) {
		if (item == null) {
			return null;
		}
		
		return (IViewReference)item.getData(ShowFastViewContribution.FAST_VIEW);		
	}
	
	/**
	 * Returns the view at the given position, or null if none
	 * 
	 * @param position to test, in display coordinates 
	 * @return the view at the given position or null if none
	 */
	private IViewReference getViewAt(Point position) {	
		return getViewFor(getToolItem(position));
	}
	
	/**
	 * Returns the toolbar item at the given position, in display coordinates
	 * @param position
	 * @return
	 */
	private ToolItem getToolItem(Point position) {
		ToolBar toolbar = getToolBar();
		Point local = toolbar.toControl(position);
		return toolbar.getItem(local);		
	}

	/**
	 * 
	 * @param pane
	 * @return
	 */
	private Perspective getPerspective(ViewPane pane) {
		return pane.getPage().getActivePerspective();
	}
	
	/**
	 * Shows the popup menu for an item in the fast view bar.
	 */
	private void showFastViewBarPopup(Point pt) {
		// Get the tool item under the mouse.
		
		ToolBar toolBar = getToolBar();

		// The fast view bar menu is created lazily here.
		if (fastViewBarMenu == null) {
			Menu menu = new Menu(toolBar);
			closeItem = new MenuItem(menu, SWT.NONE);
			closeItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
			closeItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (selectedView != null) {
						window.getActiveWorkbenchPage().hideView(selectedView);						
					}
				}
			});
			restoreItem = new MenuItem(menu, SWT.NONE);
			restoreItem.setText(WorkbenchMessages.getString("WorkbenchWindow.restore")); //$NON-NLS-1$
			restoreItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (selectedView != null) {
						window.getActiveWorkbenchPage().removeFastView(selectedView);
					}
				}
			});
			
			new MenuItem(menu, SWT.SEPARATOR);
			
			showOn = new MenuItem(menu, SWT.CASCADE);
			{
				showOn.setText(WorkbenchMessages.getString("FastViewBar.dock_on")); //$NON-NLS-1$
				
				sidesMenu = new Menu(showOn);
				radioButtons = new RadioMenu(sidesMenu, side);
				
				radioButtons.addMenuItem(WorkbenchMessages.getString("FastViewBar.Left"), new Integer(SWT.LEFT)); //$NON-NLS-1$
				radioButtons.addMenuItem(WorkbenchMessages.getString("FastViewBar.Right"), new Integer(SWT.RIGHT)); //$NON-NLS-1$
				
				showOn.setMenu(sidesMenu);
			}
			fastViewBarMenu = menu;
		}

		selectedView = getViewAt(pt);
		boolean selectingView = (selectedView != null);
		restoreItem.setEnabled(selectingView);
		closeItem.setEnabled(selectingView);

		fastViewBarMenu.setLocation(pt.x, pt.y);
		fastViewBarMenu.setVisible(true);		
	}
		
	/**
	 * Returns the underlying SWT control for the fast view bar, or null if
	 * createControl has not yet been invoked. The caller must not make any
	 * assumptions about the type of Control that is returned.
	 * 
	 * @return the underlying SWT control for the fast view bar
	 */
	public Control getControl() {
		return control;
	}
	
	public void dispose() {
		if (radioButtons != null) {
			radioButtons.dispose();
		}
	}
	
	/**
	 * Sets the "expanded" state of the fast view bar. This can be called with
	 * "false" when the fast view bar is empty to conserve space. 
	 *  
	 * @param shouldExpand
	 */
	private void expand(boolean shouldExpand) {	
		if (shouldExpand != visible) {
			
			getToolBar().setVisible(true);
			if (!shouldExpand) {
				toolBarData.widthHint = HIDDEN_WIDTH;
			} else {
				toolBarData.widthHint = SWT.DEFAULT;
			}
			
			visible = shouldExpand;
			control.getParent().layout();
		}
	}
	
	/**
	 * Refreshes the contents to match the fast views in the window's
	 * current perspective. 
	 * 
	 * @param force
	 */
	public void update(boolean force) {
		fastViewBar.update(force);
		ToolItem[] items = fastViewBar.getControl().getItems();
		expand(items.length > 0);
	}

	/**
	 * Returns the currently selected fastview
	 * 
	 * @return the currently selected fastview or null if none
	 */
	public IViewReference getSelection() {
		return selection;
	}
	
	/**
	 * Sets the currently selected fastview.
	 * 
	 * @param selected the currently selected fastview, or null if none
	 */
	public void setSelection(IViewReference selected) {
		ToolItem[] items = fastViewBar.getControl().getItems();
		for(int i=0; i<items.length; i++) {
			ToolItem item = items[i];
			item.setSelection(getView(item) == selected);
		}
		
		selection = selected;
	}

	/**
	 * Returns the view associated with the given toolbar item
	 * 
	 * @param item
	 * @return
	 */
	private IViewReference getView(ToolItem item) {
		return (IViewReference)item.getData(ShowFastViewContribution.FAST_VIEW);
	}
	
	private int getIndex(IViewReference toFind) {
		ToolItem[] items = fastViewBar.getControl().getItems();
		for(int i=0; i<items.length; i++) {
			if (items[i].getData(ShowFastViewContribution.FAST_VIEW) == toFind) {
				return i;
			}
		}	

		return items.length;
	}

	private ToolItem getItem(int idx) {
		ToolItem[] items = fastViewBar.getControl().getItems();
		if (idx >= items.length) {
			return null;
		}
		
		return items[idx];		
	}
	
	/**
	 * Returns the toolbar item associated with the given view
	 * 
	 * @param toFind
	 * @return
	 */
	private ToolItem itemFor(IViewReference toFind) {
		return getItem(getIndex(toFind));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getValidSides()
	 */
	public int getValidSides() {
		return SWT.LEFT | SWT.RIGHT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#docked(int)
	 */
	public void dock(int side) {
		this.side.set(side);
	}

	public int getSide() {
		return this.side.get();
	}
	
	/**
	 * Adds a listener that will be notified whenever
	 * 
	 * @param listener
	 */
	public void addDockingListener(IChangeListener listener) {
		this.side.addChangeListener(listener);
	}
	
}
