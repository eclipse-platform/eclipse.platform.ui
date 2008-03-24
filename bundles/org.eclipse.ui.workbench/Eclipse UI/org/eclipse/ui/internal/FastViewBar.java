/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kiryl Kazakevich, Intel - bug 88359
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
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.CellData;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.IWindowTrim;
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
	public static String FASTVIEWBAR_ID = "org.eclise.ui.internal.FastViewBar"; //$NON-NLS-1$
	
    private ToolBarManager fastViewBar;
    private MenuManager fastViewBarMenuManager;
    private MenuManager showViewMenuMgr;
    private FastViewBarContextMenuContribution contextContributionItem;

    private WorkbenchWindow window;
    private IViewReference selection;
    
    // "New Fast View" 'Button' fields
    private MenuManager newFastViewMenuMgr;
    private Composite fvbComposite;
    private ToolBar menuTB;
    private ToolItem menuItem;
    private CellData toolBarData;

    private static final int HIDDEN_WIDTH = 5;


    private int oldLength = 0;
    
    private ViewDropTarget dropTarget;

    private Listener dragListener = new Listener() {
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

    // Map of string view IDs onto Booleans (true iff horizontally aligned)
    private Map viewOrientation = new HashMap();

    private Listener addMenuListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showAddFastViewPopup(loc);
            }
        }
    };

    private Listener menuListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showFastViewBarPopup(loc);
            }
        }
    };
	private int fCurrentSide = SWT.DEFAULT;

	private boolean hasNewFastViewDisabled = false;

    class ViewDropTarget extends AbstractDropTarget {
        List panes;

        ToolItem position;

        /**
         * @param panesToDrop the list of ViewPanes to drop at the given position
         */
        public ViewDropTarget(List panesToDrop, ToolItem position) {
            setTarget(panesToDrop, position);
        }
        
        public void setTarget(List panesToDrop, ToolItem position) {
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
                ViewPane pane = (ViewPane) iter.next();
                IViewReference ref = pane.getViewReference();
                getPerspective().getFastViewManager().addViewReference(FASTVIEWBAR_ID, getIndex(view), ref, true);
//                getPage().addFastView(pane.getViewReference());
//                getPage().getActivePerspective().moveFastView(
//                        pane.getViewReference(), view);
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
			}

			return Geometry.toDisplay(getToolBar(), position.getBounds());
        }
    }
    
    /**
     * Constructs a new fast view bar for the given workbench window.
     * 
     * @param theWindow
     */
    public FastViewBar(WorkbenchWindow theWindow) {
        window = theWindow;

        hasNewFastViewDisabled = PrefUtil.getAPIPreferenceStore().getBoolean(
        		IWorkbenchPreferenceConstants.DISABLE_NEW_FAST_VIEW);

        // Construct the context menu for the fast view bar area
        fastViewBarMenuManager = new MenuManager();
        contextContributionItem = new FastViewBarContextMenuContribution(this);
        fastViewBarMenuManager.add(contextContributionItem);

        if (!hasNewFastViewDisabled) {
        	// Add "New Fast View" submenu
        	showViewMenuMgr = new MenuManager(WorkbenchMessages.FastViewBar_show_view, "showView"); //$NON-NLS-1$
        	IContributionItem showViewMenu = new ShowViewMenu(window, ShowViewMenu.class.getName(), true);
        	showViewMenuMgr.add(showViewMenu);
        	fastViewBarMenuManager.add(showViewMenuMgr);

        	// Construct the context menu for the "New Fast View" 'button'
        	newFastViewMenuMgr = new MenuManager(WorkbenchMessages.FastViewBar_show_view, "showView"); //$NON-NLS-1$
        	showViewMenu = new ShowViewMenu(window, ShowViewMenu.class.getName(), true);
        	newFastViewMenuMgr.add(showViewMenu);
        }
    }

    /**
     * Returns the platform's idea of where the fast view bar should be docked in a fresh
     * workspace.  This value is meaningless after a workspace has been setup, since the
     * fast view bar state is then persisted in the workbench.  This preference is just
     * used for applications that want the initial docking location to be somewhere other
     * than bottom. 
     * @return the initial side to dock on
     */
    public static int getInitialSide() {
        String loc = PrefUtil.getAPIPreferenceStore().getString(
                IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION);

        if (IWorkbenchPreferenceConstants.BOTTOM.equals(loc)) {
			return SWT.BOTTOM;
		}
        if (IWorkbenchPreferenceConstants.LEFT.equals(loc)) {
			return SWT.LEFT;
		}
        if (IWorkbenchPreferenceConstants.RIGHT.equals(loc)) {
			return SWT.RIGHT;
		}

        Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
        if (bundle != null) {
            IStatus status = new Status(
                    IStatus.WARNING,
                    PlatformUI.PLUGIN_ID,
                    IStatus.WARNING,
                    "Invalid value for " //$NON-NLS-1$
                            + PlatformUI.PLUGIN_ID
                            + "/" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.INITIAL_FAST_VIEW_BAR_LOCATION
                            + " preference.  Value \"" + loc //$NON-NLS-1$
                            + "\" should be one of \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.LEFT + "\", \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.BOTTOM
                            + "\", or \"" //$NON-NLS-1$
                            + IWorkbenchPreferenceConstants.RIGHT + "\".", null); //$NON-NLS-1$
            Platform.getLog(bundle).log(status);
        }

        // use bottom as the default-default
        return SWT.BOTTOM;
    }

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
     * Creates the underlying SWT fvbComposite for the fast view bar. Will add exactly
     * one new fvbComposite to the given composite. Makes no assumptions about the layout
     * being used in the parent composite.
     * 
     * @param parent enclosing SWT composite
     */
    public void createControl(Composite parent) {
        fvbComposite = new Composite(parent, SWT.NONE) {
        	public Point computeSize(int wHint, int hHint, boolean changed) {
				Point size = super.computeSize(wHint, hHint, changed);
				if (Geometry.isHorizontal(getSide())) {
					if (size.y < TrimUtil.TRIM_DEFAULT_HEIGHT) {
						size.y = TrimUtil.TRIM_DEFAULT_HEIGHT;
					}
				} else {
					if (size.x < TrimUtil.TRIM_DEFAULT_HEIGHT) {
						size.x = TrimUtil.TRIM_DEFAULT_HEIGHT;
					}
				}
				return size;
			}};
        String tip = WorkbenchMessages.FastViewBar_0; 
        fvbComposite.setToolTipText(tip);

        fvbComposite.addListener(SWT.MenuDetect, menuListener);
        PresentationUtil.addDragListener(fvbComposite, dragListener);

        createChildControls();
    }

    /**
     * Create the contents of the fast view bar. The top-level fvbComposite (created by createControl) is a 
     * composite that is created once over the lifetime of the fast view bar. This method creates the 
     * rest of the widgetry inside that composite. The controls created by this method will be 
     * destroyed and recreated if the fast view bar is docked to a different side of the window.
     */
    protected void createChildControls() {
        int newSide = getSide();
        int orientation = Geometry.isHorizontal(newSide) ? SWT.HORIZONTAL
                : SWT.VERTICAL;
        
        // Create a ControlLayout apropriate for the new orientation
        CellLayout controlLayout;        
        if (Geometry.isHorizontal(newSide)) {
        	controlLayout = new CellLayout(0)
        		.setMargins(0, 0)
        		.setDefaultRow(Row.growing())
        		.setDefaultColumn(Row.fixed())
        		.setColumn(1, Row.growing());
        } else {
        	controlLayout = new CellLayout(1)
        		.setMargins(0, 3)
        		.setDefaultColumn(Row.growing())
        		.setDefaultRow(Row.fixed())
        		.setRow(1, Row.growing());
        }
        
        // Set up the composite for the new orientation
        fvbComposite.setLayout(controlLayout);

        if (!hasNewFastViewDisabled) {
        	// Create a toolbar to show an 'Add FastView' menu 'button'
        	menuTB = new ToolBar(fvbComposite, SWT.FLAT | orientation);

        	// Construct an item to act as a 'menu button' (a la the PerspectiveSwitcher)
        	menuItem = new  ToolItem(menuTB, SWT.PUSH, 0);

        	Image tbImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_FASTVIEW);
        	menuItem.setImage(tbImage);

        	final String menuTip = WorkbenchMessages.FastViewBar_0;
        	menuItem.setToolTipText(menuTip);
        	//new ToolItem(menuTB, SWT.SEPARATOR, 1);
        	
        	menuTB.getAccessible().addAccessibleListener(new AccessibleAdapter() {
            	public void getName(AccessibleEvent e) {
            		e.result = menuTip;
            	}
            });

        	// Now that the ToolBar is populated calculate its size...
        	Point size = menuTB.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        	menuTB.setBounds(0, 0, size.x, size.y);

        	// Bring up the 'Add Fast View' menu on a left -or- right button click
        	// Right click (context menu)
        	menuItem.addListener(SWT.MenuDetect, addMenuListener);        
        	menuTB.addListener(SWT.MenuDetect, addMenuListener);

        	// Left Click...
        	menuItem.addSelectionListener(new SelectionListener() {
        		public void widgetSelected(SelectionEvent e) {
        			Rectangle bb = DragUtil.getDisplayBounds(menuTB);
        			showAddFastViewPopup(new Point(bb.x,bb.y+bb.height));
        		}

        		public void widgetDefaultSelected(SelectionEvent e) {
        		}

        	});

        	// try to get the layout correct...
        	toolBarData = new CellData();
        	toolBarData.align(SWT.FILL, SWT.FILL);
        	menuTB.setLayoutData(toolBarData);
        }

        // Construct the ToolBar containing the 'Fast' views
        fastViewBar = new ToolBarManager(SWT.FLAT | SWT.WRAP | orientation);
        fastViewBar.add(new ShowFastViewContribution(window));

        fastViewBar.createControl(fvbComposite);

        getToolBar().addListener(SWT.MenuDetect, menuListener);

        IDragOverListener fastViewDragTarget = new IDragOverListener() {

            public IDropTarget drag(Control currentControl,
                    Object draggedObject, Point position,
                    Rectangle dragRectangle) {
                ToolItem targetItem = getToolItem(position);
                if (draggedObject instanceof ViewPane) {
                    ViewPane pane = (ViewPane) draggedObject;

                    // Can't drag views between windows
                    if (pane.getWorkbenchWindow() != window) {
                        return null;
                    }

                    List newList = new ArrayList(1);
                    newList.add(draggedObject);

                    return createDropTarget(newList, targetItem);
                }
                if (draggedObject instanceof ViewStack) {
                    ViewStack folder = (ViewStack) draggedObject;

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

                    return createDropTarget(viewList, targetItem);
                }

                return null;
            }

        };

        toolBarData = new CellData();
        toolBarData.align(SWT.FILL, SWT.FILL);

        getToolBar().setLayoutData(toolBarData);
        PresentationUtil.addDragListener(getToolBar(), dragListener);
        DragUtil.addDragTarget(getControl(), fastViewDragTarget);

        update(true);
    }

    /**
     * Creates and returns a drop target with the given properties. To save object allocation,
     * the same instance is saved and reused wherever possible.
     * 
     * @param targetItem
     * @param viewList
     * @since 3.1
     */
    private IDropTarget createDropTarget(List viewList, ToolItem targetItem) {
        if (dropTarget == null) {
            dropTarget = new ViewDropTarget(viewList, targetItem);
        } else {
            dropTarget.setTarget(viewList, targetItem);
        }
        return dropTarget;
    }
    
    /**
     * Begins dragging a particular fast view
     * 
     * @param ref
     * @param position
     */
    protected void startDraggingFastView(IViewReference ref, Point position,
            boolean usingKeyboard) {
        ViewPane pane = (ViewPane) ((WorkbenchPartReference) ref).getPane();

        ToolItem item = itemFor(pane.getViewReference());

        Rectangle dragRect = Geometry.toDisplay(getToolBar(), item.getBounds());

        startDrag(((WorkbenchPartReference) ref).getPane(), dragRect, position,
				usingKeyboard);
    }

    private void startDrag(Object toDrag, Rectangle dragRect, Point position,
            boolean usingKeyboard) {

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

        boolean success = DragUtil.performDrag(toDrag, dragRect, position,
                !usingKeyboard);

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
    protected void startDraggingFastViewBar(Point position,
            boolean usingKeyboard) {
        Rectangle dragRect = DragUtil.getDisplayBounds(fvbComposite);

        startDrag(this, dragRect, position, usingKeyboard);
    }

    /**
     * Returns the toolbar for the fastview bar.
     */
    private ToolBar getToolBar() {
        return fastViewBar.getControl();
    }

    private IViewReference getViewFor(ToolItem item) {
        if (item == null) {
            return null;
        }

        return (IViewReference) item
                .getData(ShowFastViewContribution.FAST_VIEW);
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

        Menu menu = fastViewBarMenuManager.createContextMenu(toolBar);

        IViewReference selectedView = getViewAt(pt);
        contextContributionItem.setTarget(selectedView);

        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }

    /**
     * Shows the popup menu for an item in the fast view bar.
     */
    private void showAddFastViewPopup(Point pt) {
        Menu menu = newFastViewMenuMgr.createContextMenu(menuTB);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }

    public int getOrientation(IViewReference ref) {
        return isHorizontal(ref) ? SWT.HORIZONTAL : SWT.VERTICAL;
    }

    /**
     * Returns the underlying SWT fvbComposite for the fast view bar, or null if
     * createControl has not yet been invoked. The caller must not make any
     * assumptions about the type of Control that is returned.
     * 
     * @return the underlying SWT fvbComposite for the fast view bar
     */
    public Control getControl() {
        return fvbComposite;
    }

    public void dispose() {
        fastViewBarMenuManager.dispose();

        disposeChildControls();
    }

    protected void disposeChildControls() {
        fastViewBar.dispose();
        fastViewBar = null;

        if (menuItem != null) {
        	menuItem.dispose();
        	menuTB.dispose();
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

        updateLayoutData();

        for (int idx = 0; idx < items.length; idx++) {
            IViewReference view = getViewFor(items[idx]);

            viewOrientation.put(view.getId(), new Integer(
                    isHorizontal(view) ? SWT.HORIZONTAL : SWT.VERTICAL));
        }
        
        // Resize the FVB to its new size
        fvbComposite.setSize(fvbComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
    
	private void updateLayoutData() {
		ToolItem[] items = fastViewBar.getControl().getItems();
		boolean isHorizontal = Geometry.isHorizontal(getSide());
		boolean shouldExpand = items.length > 0;

        Point hint = new Point(32, shouldExpand ? SWT.DEFAULT : HIDDEN_WIDTH);
        
        if (!isHorizontal) {
            Geometry.flipXY(hint);
        }
        
        if (shouldExpand) {
            toolBarData.setHint(CellData.MINIMUM, hint);
        } else {
            toolBarData.setHint(CellData.OVERRIDE, hint);
        }
   
        if (items.length != oldLength) {
            LayoutUtil.resize(fvbComposite);
            oldLength = items.length;
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
        for (int i = 0; i < items.length; i++) {
            ToolItem item = items[i];
            item.setSelection(getView(item) == selected);
        }

        selection = selected;
    }

    /**
     * Returns the view associated with the given toolbar item
     * 
     * @param item
     */
    private IViewReference getView(ToolItem item) {
        return (IViewReference) item
                .getData(ShowFastViewContribution.FAST_VIEW);
    }

    private int getIndex(IViewReference toFind) {
        ToolItem[] items = fastViewBar.getControl().getItems();
        for (int i = 0; i < items.length; i++) {
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
     */
    private ToolItem itemFor(IViewReference toFind) {
        return getItem(getIndex(toFind));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWindowTrim#getValidSides()
     */
    public int getValidSides() {
        return SWT.TOP | SWT.LEFT | SWT.RIGHT | SWT.BOTTOM;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWindowTrim#docked(int)
     */
    public void dock(int side) {
    	fCurrentSide = side;
		disposeChildControls();
		createChildControls();
	}

    /**
     * Get the current side.
     * @return SWT.BOTTOM or SWT.RIGHT or SWT.LEFT
     */
    public int getSide() {
    	if (fCurrentSide==SWT.DEFAULT) {
    		fCurrentSide = getInitialSide();
    	}
        return fCurrentSide;
    }


    private boolean isHorizontal(IViewReference ref) {
        Integer orientation = (Integer) viewOrientation.get(ref.getId());
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
     */
    public int getViewSide(IViewReference ref) {
        boolean horizontal = isHorizontal(ref);

        if (horizontal) {
            return (getSide() == SWT.BOTTOM) ? SWT.BOTTOM : SWT.TOP;
        }
        
        return (getSide() == SWT.RIGHT) ? SWT.RIGHT : SWT.LEFT;
    }

    public void saveState(IMemento memento) {
        memento.putInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE, getSide());

        Iterator iter = viewOrientation.keySet().iterator();
        while (iter.hasNext()) {
            String next = (String) iter.next();
            IMemento orientation = memento
                    .createChild(IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION);

            orientation.putString(IWorkbenchConstants.TAG_VIEW, next);
            orientation.putInteger(IWorkbenchConstants.TAG_POSITION,
                    ((Integer) viewOrientation.get(next)).intValue());
        }

    }

    /**
     * Returns the approximate location where the next fastview icon
     * will be drawn (display coordinates)
     */
    public Rectangle getLocationOfNextIcon() {
        ToolBar control = getToolBar();

        Rectangle result = control.getBounds();
        Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
        result.height = size.y;
        result.width = size.x;
        
        boolean horizontal = Geometry.isHorizontal(getSide());
        if (control.getItemCount() == 0) {
        	Geometry.setDimension(result, horizontal, 0);
        }
        
        int hoverSide = horizontal ? SWT.RIGHT : SWT.BOTTOM;

        result = Geometry.getExtrudedEdge(result, -Geometry.getDimension(
                result, !horizontal), hoverSide);

        return Geometry.toDisplay(control.getParent(), result);
    }

    public void restoreState(IMemento memento) {
        Integer bigInt;
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE);
        if (bigInt != null) {
            dock(bigInt.intValue());
        }

        IMemento[] orientations = memento
                .getChildren(IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION);
        for (int i = 0; i < orientations.length; i++) {
            IMemento next = orientations[i];

            viewOrientation.put(next.getString(IWorkbenchConstants.TAG_VIEW),
                    next.getInteger(IWorkbenchConstants.TAG_POSITION));
        }
    }
    
    public WorkbenchWindow getWindow() {
        return window;
    }
    
    public void restoreView(IViewReference selectedView) {
        if (selectedView != null) {
            WorkbenchPage page = window.getActiveWorkbenchPage();
            if (page != null) {
                int idx = getIndex(selectedView);
                ToolItem item = getItem(idx);
                Rectangle bounds = item.getBounds();
                Rectangle startBounds = Geometry.toDisplay(item
                        .getParent(), bounds);

                Perspective persp = getPerspective();
                if (persp != null) {
                	persp.getFastViewManager().removeViewReference(selectedView, true, true);
                }

                IWorkbenchPart toActivate = selectedView
                        .getPart(true);
                if (toActivate != null) {
                    page.activate(toActivate);
                }

                ViewPane pane = (ViewPane) ((WorkbenchPartReference) selectedView)
                        .getPane();

                RectangleAnimation animation = new RectangleAnimation(
                        window.getShell(), startBounds, pane
                                .getParentBounds());

                animation.schedule();
            }
        }
    }

	/**
	 * @return The list of all view references in the stack
	 */
	public List getViewRefs() {
		List refs = new ArrayList(fastViewBar.getControl().getItemCount());
		ToolItem[] items = fastViewBar.getControl().getItems();
		for (int i = 0; i < items.length; i++) {
			Object data = items[i].getData(ShowFastViewContribution.FAST_VIEW);
			if (data != null) 
				refs.add(data);
		}
		
		return refs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#isCloseable()
	 */
	public boolean isCloseable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#handleClose()
	 */
	public void handleClose() {
		// nothing to do...
	}
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getId()
	 */
	public String getId() {
		return "org.eclise.ui.internal.FastViewBar"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		return WorkbenchMessages.TrimCommon_FastView_TrimName;
	}

	/**
     * Returns the context menu contribution item.  This is for
     * internal UI testing only.
     * 
     * @return the context menu contribution item
     * @since 3.1.1
     */
    public FastViewBarContextMenuContribution testContextMenu() {
    	return contextContributionItem;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#getWidthHint()
	 */
	public int getWidthHint() {
		return SWT.DEFAULT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#getHeightHint()
	 */
	public int getHeightHint() {
		return SWT.DEFAULT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowTrim#isResizeable()
	 */
	public boolean isResizeable() {
		return false;
	}
}
