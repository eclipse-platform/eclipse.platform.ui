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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.PresentationUtil;
import org.osgi.framework.Bundle;

/**
 * Represents the fast view bar.
 * 
 * <p>The set of fastviews are obtained from the WorkbenchWindow that 
 * is passed into the constructor. The set of fastviews may be refreshed to 
 * match the state of the perspective by calling the update(...) method.</p>
 * 
 * @see org.eclipse.ui.internal.FastViewPane
 */
public class FastViewBar implements IWindowTrim {
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
	private Cursor moveCursor;
	private MenuItem closeItem;
	private MenuItem orientationItem;
	private IntModel side = new IntModel(getInitialSide());
	private IntModel currentOrientation = new IntModel(SWT.VERTICAL);
	private RadioMenu radioButtons;
	private IViewReference selectedView;
	private int lastSide;
	private Label fastViewLabel;
	private Label fastViewLabel2;
	private int oldLength = 0;
	private IChangeListener orientationChangeListener = new IChangeListener() {
		public void update(boolean changed) {
			if (changed && selectedView != null) {
				setOrientation(selectedView, currentOrientation.get());
			}
		}
	};
	
	// Map of string view IDs onto Booleans (true iff horizontally aligned)
	private Map viewOrientation = new HashMap();
	private Listener menuListener;
	
	/**
	 * Constructs a new fast view bar for the given workbench window.
	 * 
	 * @param theWindow
	 */
	public FastViewBar(WorkbenchWindow theWindow) {
		window = theWindow;
		window.addPerspectiveListener(new IPerspectiveListener() {
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				update(true);
			}
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
				update(true);				
			}
		});
	}

	/**
	 * Returns the platform's idea of where the fast view bar should be docked in a fresh
	 * workspace.  This value is meaningless after a workspace has been setup, since the
	 * fast view bar state is then persisted in the workbench.  This preference is just
	 * used for applications that want the initial docking location to be somewhere other
	 * than bottom. 
	 */
	private static int getInitialSide() {
	    String loc = PrefUtil.getAPIPreferenceStore().getString(
                IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION);

        if (IWorkbenchPreferenceConstants.BOTTOM.equals(loc))
                return SWT.BOTTOM;
        if (IWorkbenchPreferenceConstants.LEFT.equals(loc))
                return SWT.LEFT;
        if (IWorkbenchPreferenceConstants.RIGHT.equals(loc))
                return SWT.RIGHT;

        Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
        if (bundle != null) {
            IStatus status = new Status(
                    IStatus.WARNING,
                    PlatformUI.PLUGIN_ID,
                    IStatus.WARNING,
                    "Invalid value for " //$NON-NLS-1$
                            + PlatformUI.PLUGIN_ID + "/" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION
                            + " preference.  Value \"" + loc //$NON-NLS-1$
                            + "\" should be one of \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.LEFT + "\", \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.BOTTOM + "\", or \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.RIGHT + "\".", null); //$NON-NLS-1$
            Platform.getLog(bundle).log(status);
        }

	    // use bottom as the default-default
        return SWT.BOTTOM;
	}

	/**
	 * @param selectedView2
	 * @param object
	 */
	public void setOrientation(IViewReference refToSet, int newState) {
		if (newState == getOrientation(refToSet)) {
			return;
		}
		
		viewOrientation.put(refToSet.getId(), new Integer(newState));
		Perspective persp = getPerspective();
		
		if (persp != null) {
			IViewReference ref = persp.getActiveFastView();
			if (ref != null) {
				persp.setActiveFastView(null);
			}
			persp.setActiveFastView(refToSet);
		}
	}
	
	/**
	 * Returns the active workbench page or null if none
	 */
	private WorkbenchPage getPage() {
		if (window == null) {
			return null;
		}
		
		return window.getActiveWorkbenchPage();
	}

	/**
	 * Returns the current perspective or null if none
	 */
	private Perspective getPerspective() {

		WorkbenchPage page = getPage();
		
		if (page == null) {
			return null;
		}
		
		return page.getActivePerspective();
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
		
		side.addChangeListener(new IChangeListener() {
			public void update(boolean changed) {
				if (changed && (Geometry.isHorizontal(getSide()) != Geometry.isHorizontal(lastSide))) {
					disposeChildControls();
					createChildControls();
				}
				lastSide = getSide();
			}
		});
		
		currentOrientation.addChangeListener(orientationChangeListener);
		
		createChildControls();
	}
	
	/**
	 * Create the contents of the fast view bar. The top-level control (created by createControl) is a 
	 * composite that is created once over the lifetime of the fast view bar. This method creates the 
	 * rest of the widgetry inside that composite. The controls created by this method will be 
	 * destroyed and recreated if the fast view bar is docked to a different side of the window.
	 */
	protected void createChildControls() {
		int newSide = getSide();
		int flags = Geometry.isHorizontal(newSide) ? SWT.HORIZONTAL : SWT.VERTICAL;
		
		fastViewBar = new ToolBarManager(SWT.FLAT | SWT.WRAP | flags);
		fastViewBar.add(new ShowFastViewContribution(window));
		
		menuListener = new Listener() {
			public void handleEvent(Event event) {
				Point loc = new Point(event.x, event.y);
				if (event.type == SWT.MenuDetect) {
					showFastViewBarPopup(loc);
				} 
			}
		};
		CellLayout controlLayout = new CellLayout(0)
			.setMargins(0, newSide == SWT.BOTTOM ? 0 : 3)
			.setDefaultRow(Row.growing())
			.setDefaultColumn(Row.fixed())
			.setColumn(newSide == SWT.BOTTOM ? 1 : 0, Row.growing());
		control.setLayout(controlLayout);
		
		// When we're on the bottom, add a drag handle. Otherwise, it's impossible to drag the fast view
		// bar if there's nothing in it.
		if (newSide == SWT.BOTTOM) {
						
			fastViewLabel = createFastViewSeparator(control);
		}
		fastViewBar.createControl(control);
		if (newSide == SWT.BOTTOM) {
						
			fastViewLabel2 = createFastViewSeparator(control);
		}		

		getToolBar().addListener(SWT.MenuDetect, menuListener);
		
		Listener dragListener = new Listener() {
			public void handleEvent(Event event) {
				Point position = DragUtil.getEventLoc(event);
				
				IViewReference ref = getViewAt(position);
				
				if (ref == null) {
					startDraggingFastViewBar(position, false);
				} else {
					startDraggingFastView(ref, position, false);
				}
			}
		};
				
		IDragOverListener fastViewDragTarget = new IDragOverListener() {

			class ViewDropTarget extends AbstractDropTarget {
				List panes;
				ToolItem position;
				
				/**
				 * @param panesToDrop the list of ViewPanes to drop at the given position
				 */
				public ViewDropTarget(List panesToDrop, ToolItem position) {
					panes = panesToDrop;
					this.position = position;
				}
				
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
				 */
				public void drop() {
					IViewReference view = getViewFor(position);
					
					Iterator iter = panes.iterator();
					while (iter.hasNext()) {
						ViewPane pane = (ViewPane)iter.next();
						getPage().addFastView(pane.getViewReference());
						getPage().getActivePerspective().moveFastView(pane.getViewReference(), view);
					}
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
						// As long as the toolbar is not empty, highlight the place
						// where this view will appear (we
						// may have compressed it to save space when empty, so the actual
						// icon location may not be over the toolbar when it is empty)
						if (getToolBar().getItemCount() > 0) {
							return getLocationOfNextIcon();
						}
						// If the toolbar is empty, highlight the entire toolbar 
						return DragUtil.getDisplayBounds(getControl());
					} else {
						return Geometry.toDisplay(getToolBar(), position.getBounds());
					}
				}
			}
			
			public IDropTarget drag(Control currentControl, Object draggedObject, Point position, Rectangle dragRectangle) {
				ToolItem targetItem = getToolItem(position);
				if (draggedObject instanceof ViewPane) {
					ViewPane pane = (ViewPane) draggedObject;
					
					// Can't drag views between windows
					if (pane.getWorkbenchWindow() != window) {
						return null;
					}
					
					List newList = new ArrayList(1);
					newList.add(draggedObject);
					
					return new ViewDropTarget(newList, targetItem);
				}
				if (draggedObject instanceof ViewStack) {
					ViewStack folder = (ViewStack)draggedObject;
					
					if (folder.getWorkbenchWindow() != window) {
						return null;
					}
					
					List viewList = new ArrayList(folder.getItemCount());
					LayoutPart[] children = folder.getChildren();
					
					for (int idx = 0; idx < children.length; idx++) {
						if (!(children[idx] instanceof PartPlaceholder)) {
							viewList.add(children[idx]);
						}
					}
					
					return new ViewDropTarget(viewList, targetItem);
				}
				
				return null;
			}
			
		};
		
		toolBarData = new GridData(GridData.FILL_BOTH);
		toolBarData.widthHint = HIDDEN_WIDTH;
		visible = false;
		
		getToolBar().setLayoutData(toolBarData);
		PresentationUtil.addDragListener(getToolBar(), dragListener);
		DragUtil.addDragTarget(getControl(), fastViewDragTarget);
		if (fastViewLabel != null) {
			PresentationUtil.addDragListener(fastViewLabel, dragListener);
		}
		if (fastViewLabel2 != null) {
			PresentationUtil.addDragListener(fastViewLabel2, dragListener);
		}
		
		update(true);
	}

	/**
	 * Begins dragging a particular fast view
	 * 
	 * @param ref
	 * @param position
	 * @param b
	 */
	protected void startDraggingFastView(IViewReference ref, Point position, boolean usingKeyboard) {
		ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();
		
		ToolItem item = itemFor(pane.getViewReference());
		
		Rectangle dragRect = Geometry.toDisplay(getToolBar(), item.getBounds()); 

		Perspective persp = getPerspective();
		
		WorkbenchPage page = getPage();
		
		startDrag((ViewPane)((WorkbenchPartReference)ref).getPane(), dragRect, position, usingKeyboard);
	}

	private void startDrag(Object toDrag, Rectangle dragRect, Point position, boolean usingKeyboard) {
		
		Perspective persp = getPerspective();
		
		WorkbenchPage page = getPage();
		
		IViewReference oldFastView = null;
		if (persp != null) {
			oldFastView = persp.getActiveFastView();
			
			if (page != null) {
				page.hideFastView();
			}
		}
		
		if (page.isZoomed()) {
            page.zoomOut();
        }
		
		boolean success = DragUtil.performDrag(toDrag, dragRect, position, !usingKeyboard);
		
		// If the drag was cancelled, reopen the old fast view
		if (!success && oldFastView != null && page != null) {
			page.toggleFastView(oldFastView);
		}
	}
	
	/**
	 * Begins dragging the fast view bar
	 * 
	 * @param position initial mouse position
	 * @param usingKeyboard true iff the bar is being dragged using the keyboard
	 */
	protected void startDraggingFastViewBar(Point position, boolean usingKeyboard) {
		Rectangle dragRect = DragUtil.getDisplayBounds(control);
		
		startDrag(this, dragRect, position, usingKeyboard);		
	}

	/**
	 * @param control2
	 * @return
	 */
	private Label createFastViewSeparator(Composite control2) {
		Label result = new Label(control2, SWT.SEPARATOR | SWT.VERTICAL);
		//fastViewLabel.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU));
		result.addListener(SWT.MenuDetect, menuListener);
		if (moveCursor == null) {
			moveCursor = new Cursor(control.getDisplay(), SWT.CURSOR_SIZEALL);
		}
		result.setCursor(moveCursor);
		GridData data = new GridData(GridData.FILL_VERTICAL);
		data.heightHint = 10;
		data.widthHint = 10;
		data.verticalAlignment = GridData.CENTER;
		data.horizontalAlignment = GridData.CENTER;
		result.setLayoutData(data);

		return result;
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
	 * Shows the popup menu for an item in the fast view bar.
	 */
	private void showFastViewBarPopup(Point pt) {
		// Get the tool item under the mouse.
		
		ToolBar toolBar = getToolBar();

		// The fast view bar menu is created lazily here.
		if (fastViewBarMenu == null) {
			Menu menu = new Menu(toolBar);
			
			orientationItem = new MenuItem(menu, SWT.CASCADE);
			{
				orientationItem.setText(WorkbenchMessages.getString("FastViewBar.view_orientation")); //$NON-NLS-1$
				
				Menu orientationSwtMenu = new Menu(orientationItem);
				RadioMenu orientationMenu = new RadioMenu(orientationSwtMenu, currentOrientation);
				orientationMenu.addMenuItem(WorkbenchMessages.getString("FastViewBar.horizontal"), new Integer(SWT.HORIZONTAL)); //$NON-NLS-1$
				orientationMenu.addMenuItem(WorkbenchMessages.getString("FastViewBar.vertical"), new Integer(SWT.VERTICAL)); //$NON-NLS-1$
				
				orientationItem.setMenu(orientationSwtMenu);
			}
			
			restoreItem = new MenuItem(menu, SWT.CHECK);
			restoreItem.setSelection(true);
			restoreItem.setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
			restoreItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (selectedView != null) {
						WorkbenchPage page = window.getActiveWorkbenchPage();
						if (page != null) {
							int idx = getIndex(selectedView);
							ToolItem item = getItem(idx);
							Rectangle bounds = item.getBounds();
							Rectangle startBounds = Geometry.toDisplay(item.getParent(), bounds);
							
							page.removeFastView(selectedView);
							
							LayoutPart pane = ((WorkbenchPartReference)selectedView).getPane();
							
							RectangleAnimation animation = new RectangleAnimation(window.getShell(),
									startBounds,
									DragUtil.getDisplayBounds(pane.getControl()));
			
							animation.schedule();
						}
					}
				}
			});
			
			closeItem = new MenuItem(menu, SWT.NONE);
			closeItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
			closeItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (selectedView != null) {
						WorkbenchPage page = window.getActiveWorkbenchPage();
						if (page != null) {
							page.hideView(selectedView);
						}
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
				radioButtons.addMenuItem(WorkbenchMessages.getString("FastViewBar.Bottom"), new Integer(SWT.BOTTOM)); //$NON-NLS-1$
				
				showOn.setMenu(sidesMenu);
			}
			fastViewBarMenu = menu;
		}

		selectedView = getViewAt(pt);
		boolean selectingView = (selectedView != null);
		restoreItem.setEnabled(selectingView);
		restoreItem.setSelection(true);
		closeItem.setEnabled(selectingView);
		orientationItem.setEnabled(selectingView);
		if (selectingView) {
			// Set the new orientation, but avoid re-sending the event to our own
			// listener
			currentOrientation.set(getOrientation(selectedView),
					orientationChangeListener);
		}
		
		fastViewBarMenu.setLocation(pt.x, pt.y);
		fastViewBarMenu.setVisible(true);		
	}
		
	public int getOrientation(IViewReference ref) {
		return isHorizontal(ref) ? SWT.HORIZONTAL : SWT.VERTICAL;
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
		
		disposeChildControls();
	}
	
	protected void disposeChildControls() {
		fastViewBar.dispose();
		fastViewBar = null;
		
		if (fastViewLabel != null) {
			fastViewLabel.dispose();
			fastViewLabel = null;
		}

		if (fastViewLabel2 != null) {
			fastViewLabel2.dispose();
			fastViewLabel2 = null;
		}
		
		if (moveCursor != null) {
			moveCursor.dispose();
			moveCursor = null;
		}
		
		oldLength = 0;
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
		
		boolean shouldExpand = items.length > 0;
		if (shouldExpand != visible) {
			
			getToolBar().setVisible(true);
			if (!shouldExpand) {
				toolBarData.widthHint = HIDDEN_WIDTH;
			} else {
				toolBarData.widthHint = SWT.DEFAULT;
			}
			
			visible = shouldExpand;
		}
		
		if (items.length != oldLength) {
			LayoutUtil.resize(control);
			oldLength = items.length;
		}
		
		for (int idx = 0; idx < items.length; idx++) {
			 IViewReference view = getViewFor(items[idx]);
			 
			 viewOrientation.put(view.getId(), new Integer(isHorizontal(view) ? SWT.HORIZONTAL : SWT.VERTICAL));
		}		
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
		return SWT.LEFT | SWT.RIGHT | SWT.BOTTOM;
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

	private boolean isHorizontal(IViewReference ref) {
		Integer orientation = (Integer)viewOrientation.get(ref.getId());
		boolean horizontalBar = Geometry.isHorizontal(getSide()); 
		boolean horizontal = horizontalBar;
		if (orientation != null) {
			horizontal = orientation.intValue() == SWT.HORIZONTAL;
		} else {
			horizontal = false;
		}
		
		return horizontal;
	}
	
	/**
	 * @param ref
	 * @return
	 */
	public int getViewSide(IViewReference ref) {
		boolean horizontal = isHorizontal(ref);
		
		if (horizontal) {
			return (getSide() == SWT.BOTTOM) ? SWT.BOTTOM : SWT.TOP;
		} else {
			return (getSide() == SWT.RIGHT) ? SWT.RIGHT : SWT.LEFT;
		}
	}

	public void saveState(IMemento memento) {
		memento.putInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE, getSide());
		
		Iterator iter = viewOrientation.keySet().iterator();
		while (iter.hasNext()) {
			String next = (String)iter.next();
			IMemento orientation = memento.createChild(IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION);
		
			orientation.putString(IWorkbenchConstants.TAG_VIEW, next);
			orientation.putInteger(IWorkbenchConstants.TAG_POSITION, ((Integer)viewOrientation.get(next)).intValue());
		}
		
	}
	
	/**
	 * Returns the approximate location where the next fastview icon
	 * will be drawn (display coordinates)
	 * 
	 * @param fastViewMem
	 */
	public Rectangle getLocationOfNextIcon() {
		ToolBar control = getToolBar();
		
		Rectangle result = control.getBounds();
		Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		result.height = size.y;
		result.width = size.x;
		
		boolean horizontal = Geometry.isHorizontal(getSide()); 
		int hoverSide = horizontal ? SWT.RIGHT : SWT.BOTTOM;
		
		result = Geometry.getExtrudedEdge(result, 
				-Geometry.getDimension(result, !horizontal),
				hoverSide);
		
		return Geometry.toDisplay(control.getParent(), result);
	}
	
	/**
	 * @param fastViewMem
	 */
	public void restoreState(IMemento memento) {
		Integer bigInt;
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE);
		if (bigInt != null) {
			dock(bigInt.intValue()); 
		}
		
		IMemento[] orientations = memento.getChildren(IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION);
		for (int i = 0; i < orientations.length; i++) {
			IMemento next = orientations[i];
			
			viewOrientation.put(next.getString(IWorkbenchConstants.TAG_VIEW), next.getInteger(IWorkbenchConstants.TAG_POSITION));		
		}
	}

}
