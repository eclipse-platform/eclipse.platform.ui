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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.ToolBarManager;

import org.eclipse.ui.IViewReference;

import org.eclipse.ui.internal.dnd.AbstractDragSource;
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
	private WorkbenchWindow window;
	private MenuItem restoreItem;
	private IViewReference selection;
	private boolean visible = false;
	private Composite control;
	private GridData toolBarData;
	private static final int HIDDEN_WIDTH = 5;
	private MenuItem showOnLeft;
	private int side = SWT.LEFT;
	private Cursor dragCursor;
	
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
		
		Label dragHandle = new Label(control, SWT.SEPARATOR | SWT.HORIZONTAL);
		
		GridData dragHandleData = new GridData();
		dragHandleData.horizontalAlignment = GridData.CENTER;
		dragHandleData.widthHint = HIDDEN_WIDTH;
		dragHandleData.heightHint = HIDDEN_WIDTH;
		dragHandle.setLayoutData(dragHandleData);
		
		dragCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_SIZEALL);
		dragHandle.setCursor(dragCursor);
		
		DragUtil.addDragSource(dragHandle, new AbstractDragSource() {
			public Object getDraggedItem(Point position) {
				return FastViewBar.this;
			}
			
			public Rectangle getDragRectangle(Object dragged) {
				return DragUtil.getDisplayBounds(control);
			}
		});
		
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MenuDetect) {
					showFastViewBarPopup(new Point(event.x, event.y));
				}
			}
		};

		fastViewBar.createControl(control);
		//dragHandle.addListener(SWT.MenuDetect, listener);
		getToolBar().addListener(SWT.MenuDetect, listener);
		
		IDragSource fastViewDragSource = new AbstractDragSource() {
			
			IViewReference oldFastView = null;
			
			public Object getDraggedItem(Point position) {
				IViewReference ref = getViewAt(position);

				if (ref == null) {
					return null;
				}
				
				ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();
				
				return pane;
			}

			public Rectangle getDragRectangle(Object draggedItem) {
				ViewPane pane = (ViewPane)draggedItem;
				
				Perspective persp = getPerspective(pane);
				return Rectangles.toDisplay(pane.getPage().getClientComposite().getParent(), 
						persp.getFastViewBounds(pane.getViewReference()));
			}

			public void dragFinished(Object draggedItem, boolean success) {
				ViewPane pane = (ViewPane)draggedItem;
				
				Perspective persp = getPerspective(pane);
				
				if (oldFastView != pane.getViewReference() || !success) {
					persp.setActiveFastView(oldFastView);
				}
				
				oldFastView = null;
			}

			public void dragStarted(Object draggedItem) {
				// If a fast view is active, and the dragged element is the active
				// fast view or an icon for a fast view, minimize the active fast
				// view when the drag begins. This is to allow it to be dropped
				// somewhere that the active fast view was covering.
				
				ViewPane pane = (ViewPane)draggedItem;
				
				Perspective persp = getPerspective(pane);
				
				oldFastView = persp.getActiveFastView();
				
				persp.setActiveFastView(null, 0);
			}
		};
		
		IDragOverListener fastViewDragTarget = new IDragOverListener() {

			class ViewDropTarget implements IDropTarget {
				ViewPane pane;
				IViewReference position;
				
				public ViewDropTarget(ViewPane toDrop, IViewReference position) {
					pane = toDrop;
				}
				
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
				 */
				public void drop() {
					pane.getPage().addFastView(pane.getViewReference());
				}
				
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
				 */
				public Cursor getCursor() {
					return DragCursors.getFastViewCursor();
				}
			}
			
			public IDropTarget drag(Control currentControl, Object draggedObject, Point position, Rectangle dragRectangle) {
				if (draggedObject instanceof ViewPane) {
					return new ViewDropTarget((ViewPane)draggedObject, getViewAt(position));
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
	
	/**
	 * Returns the view at the given position, or null if none
	 * 
	 * @param position to test, in display coordinates 
	 * @return the view at the given position or null if none
	 */
	private IViewReference getViewAt(Point position) {
		ToolBar toolbar = getToolBar();
		Point local = toolbar.toControl(position);
		ToolItem item = toolbar.getItem(local);
		
		if (item == null) {
			return null;
		}
		
		return (IViewReference)item.getData(ShowFastViewContribution.FAST_VIEW);
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
		ToolBar toolBar = fastViewBar.getControl();
		ToolItem toolItem = toolBar.getItem(toolBar.toControl(pt));
		if (toolItem == null)
			return;

		// Get the action for the tool item.
		Object data = toolItem.getData();

		// If the tool item is an icon for a fast view
		if (data instanceof ShowFastViewContribution) {
			// The fast view bar menu is created lazily here.
			if (fastViewBarMenu == null) {
				Menu menu = new Menu(toolBar);
				MenuItem closeItem = new MenuItem(menu, SWT.NONE);
				closeItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
				closeItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						ToolItem toolItem = (ToolItem) fastViewBarMenu.getData();
						if (toolItem != null && !toolItem.isDisposed()) {
							IViewReference ref =
								(IViewReference) toolItem.getData(
										ShowFastViewContribution.FAST_VIEW);
							window.getActiveWorkbenchPage().hideView(ref);
						}
					}
				});
				restoreItem = new MenuItem(menu, SWT.CHECK);
				restoreItem.setText(WorkbenchMessages.getString("WorkbenchWindow.restore")); //$NON-NLS-1$
				restoreItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						ToolItem toolItem = (ToolItem) fastViewBarMenu.getData();
						if (toolItem != null && !toolItem.isDisposed()) {
							IViewReference ref =
								(IViewReference) toolItem.getData(
										ShowFastViewContribution.FAST_VIEW);
							window.getActiveWorkbenchPage().removeFastView(ref);
						}
					}
				});
				
				showOnLeft = new MenuItem(menu, SWT.CHECK);
				showOnLeft.setText(WorkbenchMessages.getString("FastViewBar.dock_on_left"));
				showOnLeft.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						window.getActiveWorkbenchPage().getActivePerspective().setActiveFastView(null, 0);
						if (showOnLeft.getSelection()) {
							window.showFastViewBar(SWT.LEFT);
						} else {
							window.showFastViewBar(SWT.RIGHT);
						}
					}
				});
				showOnLeft.setSelection(side == SWT.LEFT);
				
				fastViewBarMenu = menu;
			}
			restoreItem.setSelection(true);
			fastViewBarMenu.setData(toolItem);

			// Show popup menu.
			if (fastViewBarMenu != null) {
				fastViewBarMenu.setLocation(pt.x, pt.y);
				fastViewBarMenu.setVisible(true);
			}
		}
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
	
	/**
	 * Sets the "expanded" state of the fast view bar. This can be called with
	 * "false" when the fast view bar is empty to conserve space. 
	 *  
	 * @param shouldExpand
	 */
	private void expand(boolean shouldExpand) {
 		
		if (shouldExpand != visible) {
			
			getToolBar().setVisible(shouldExpand);
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
		int items = fastViewBar.getControl().getItems().length;
		expand(items > 0);
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
			item.setSelection(viewFor(item) == selected);
		}
		
		selection = selected;
	}

	/**
	 * Returns the view associated with the given toolbar item
	 * 
	 * @param item
	 * @return
	 */
	private IViewReference viewFor(ToolItem item) {
		return (IViewReference)item.getData(ShowFastViewContribution.FAST_VIEW);
	}
	
	/**
	 * Returns the toolbar item associated with the given view
	 * 
	 * @param toFind
	 * @return
	 */
	private ToolItem itemFor(IViewReference toFind) {
		ToolItem[] items = fastViewBar.getControl().getItems();
		for(int i=0; i<items.length; i++) {
			if (items[i].getData(ShowFastViewContribution.FAST_VIEW) == toFind) {
				return items[i];
			}
		}	

		return null;
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
	public void docked(int side) {
		this.side = side;	
		if (showOnLeft != null) {
			showOnLeft.setSelection(window.getFastViewBarSide() == SWT.LEFT);
		}
	}

	public int getSide() {
		return this.side;
	}
	
}
