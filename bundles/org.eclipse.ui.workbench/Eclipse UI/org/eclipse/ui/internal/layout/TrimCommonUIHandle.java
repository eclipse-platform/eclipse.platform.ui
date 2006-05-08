/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.layout;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.IChangeListener;
import org.eclipse.ui.internal.IntModel;
import org.eclipse.ui.internal.RadioMenu;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.presentations.PresentationUtil;

/**
 * This control provides common UI functionality for trim elements. Its
 * lifecycle is managed by the <code>TrimLayout</code> which automatically
 * adds a UI handle to all added trim elements. It uses an instance of a
 * CoolBar to provide the platform-specific drag affordance.
 * <p>
 * It provides the following features:
 * <p>
 * Drag affordance and handling:
 * <ol>
 * <li>Drag affordance is provided in the <code>paintControl</code> method</li>
 * <li>Drag handling is provided to allow rearrangement within a trim side or
 * to other sides, depending on the values returned by <code>IWindowTrim.getValidSides</code></li>
 * </ol>
 * </p>
 * <p>
 * Context Menu:
 * <ol>
 * <li>A "Dock on" menu item is provided to allow changing the side, depending on the values returned by 
 * <code>IWindowTrim.getValidSides</code></li>
 * <li>A "Close" menu item is provided to allow the User to close (hide) the trim element,
 * based on the value returned by <code>IWindowTrim.isCloseable</code>
 * </ol>
 * </p>
 * <p>
 * @since 3.2
 * </p>
 */
public class TrimCommonUIHandle extends Composite {
	/*
	 * Fields
	 */
	private TrimLayout  layout;
    private IWindowTrim trim;
	private Control     toDrag;
	private int orientation;

	// CoolBar handling
	private CoolBar cb = null;
	private CoolItem ci = null;
	private static int horizontalHandleSize = -1;
	private static int verticalHandleSize = -1;
	
    /*
     * Context Menu
     */
	private MenuManager dockMenuManager;
	private ContributionItem dockContributionItem = null;
    private Menu sidesMenu;
	private MenuItem dockCascade;
    private RadioMenu radioButtons;
    private IntModel radioVal = new IntModel(0);
//	private Menu showMenu;
//	private MenuItem showCascade;
	
	/*
	 * Listeners...
	 */
    
    /**
     * This listener starts a drag operation when
     * the Drag and Drop manager tells it to
     */
    private Listener dragListener = new Listener() {
        public void handleEvent(Event event) {
        	// Only allow 'left mouse' drags...
        	if (event.button != 3) {
	            Point position = DragUtil.getEventLoc(event);
	            startDraggingTrim(position);
        	}
        }
    };

    /**
     * This listener brings up the context menu
     */
    private Listener menuListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showDockTrimPopup(loc);
            }
        }
    };

    /**
     * Listen to size changes in the control so we can adjust the
     * Coolbar and CoolItem to match.
     */
    private ControlListener controlListener = new ControlListener() {
		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			if (e.widget instanceof TrimCommonUIHandle) {
				TrimCommonUIHandle ctrl = (TrimCommonUIHandle) e.widget;
		        Point size = ctrl.getSize();

		        // Set the CoolBar and item to match the handle's size
		        cb.setSize(size);
		        ci.setSize(size);
		        ci.setPreferredSize(size);
		        cb.layout(true);
			}
		}
    };

    /**
     * Create a new trim UI handle for a particular IWindowTrim item
     * 
     * @param layout the TrimLayout we're being used in
     * @param trim the IWindowTrim we're acting on behalf of
     * @param curSide  the SWT side that the trim is currently on
     */
    public TrimCommonUIHandle(TrimLayout layout, IWindowTrim trim, int curSide) {
    	super(trim.getControl().getParent(), SWT.NONE);
    	
    	// Set the control up with all its various hooks, cursor...
    	setup(layout, trim, curSide);
		
        // Listen to size changes to keep the CoolBar synched
        addControlListener(controlListener);
    }

	/**
	 * Set up the trim with its cursor, drag listener, context menu and menu listener.
	 * This method can also be used to 'recycle' a trim handle as long as the new handle
	 * is for trim under the same parent as it was originally used for.
	 */
	public void setup(TrimLayout layout, IWindowTrim trim, int curSide) {    	
    	this.layout = layout;
    	this.trim = trim;
    	this.toDrag = trim.getControl();
    	this.radioVal.set(curSide);
    	
    	// remember the orientation to use
    	orientation = (curSide == SWT.LEFT || curSide == SWT.RIGHT) ? SWT.VERTICAL  : SWT.HORIZONTAL;
    	
        // Insert a CoolBar and extras in order to provide the drag affordance
        insertCoolBar(orientation);
        
		// Create a window trim proxy for the handle
		createWindowTrimProxy();
       	
    	// Set the cursor affordance
    	setDragCursor();
    	
        // Set up the dragging behaviour
        PresentationUtil.addDragListener(cb, dragListener);
    	
    	// Create the docking context menu
    	dockMenuManager = new MenuManager();
    	dockContributionItem = getDockingContribution();
        dockMenuManager.add(dockContributionItem);

        cb.addListener(SWT.MenuDetect, menuListener);
        
        setVisible(true);
    }

    /**
     * Handle the event generated when a User selects a new side to
     * dock this trim on using the context menu
     */
    private void handleShowOnChange() {
    	layout.removeTrim(trim);
    	trim.dock(radioVal.get());
    	layout.addTrim(radioVal.get(), trim, null);
    	
    	// perform an optimized layout to show the trim in its new location
    	LayoutUtil.resize(trim.getControl());
	}

	/**
	 * Create and format the IWindowTrim for the handle, ensuring that the
	 * handle will be 'wide' enough to display the drag affordance.
	 */
	private void createWindowTrimProxy() {
		// Create a window trim proxy for the handle
		WindowTrimProxy proxy = new WindowTrimProxy(this, "NONE", "NONE", //$NON-NLS-1$ //$NON-NLS-2$
				SWT.TOP | SWT.BOTTOM | SWT.LEFT | SWT.RIGHT, false);

		// Set up the handle's hints based on the computed size that
		// the handle has to be (i.e. if it's HORIZONTAL then the
		// 'width' is determined by the space required to show the
		// CB's drag affordance).
		if (orientation == SWT.HORIZONTAL) {
			proxy.setWidthHint(getHandleSize());
			proxy.setHeightHint(0);
		}
		else {
			proxy.setWidthHint(0);
			proxy.setHeightHint(getHandleSize());
		}
		
		setLayoutData(proxy);
	}
	
	/**
	 * Calculate a size for the handle that will be large enough to show
	 * the CoolBar's drag affordance.
	 * 
	 * @return The size that the handle has to be, based on the orientation
	 */
	private int getHandleSize() {
		// Do we already have a 'cached' value?
		if (orientation == SWT.HORIZONTAL && horizontalHandleSize != -1) {
			return horizontalHandleSize;
		}
				
		if (orientation == SWT.VERTICAL && verticalHandleSize != -1) {
			return verticalHandleSize;
		}
				
		// Must be the first time, calculate the value
		CoolBar bar = new CoolBar (trim.getControl().getParent(), orientation);
		
		CoolItem item = new CoolItem (bar, SWT.NONE);
		
		Label ctrl = new Label (bar, SWT.PUSH);
		ctrl.setText ("Button 1"); //$NON-NLS-1$
	    Point size = ctrl.computeSize (SWT.DEFAULT, SWT.DEFAULT);
		
	    Point ps = item.computeSize (size.x, size.y);
		item.setPreferredSize (ps);
		item.setControl (ctrl);

		bar.pack ();

		// OK, now the difference between the location of the CB and the
		// location of the 
		Point bl = ctrl.getLocation();
		Point cl = bar.getLocation();

		// Toss them now...
		ctrl.dispose();
		item.dispose();
		bar.dispose();
	
		// The 'size' is the difference between the start of teh CoolBar and
		// start of its first control
		int length;
		if (orientation == SWT.HORIZONTAL) {
			length = bl.x - cl.x;
			horizontalHandleSize = length;
		}
		else {
			length = bl.y - cl.y;
			verticalHandleSize = length;
		}
		
		return length;
	}
	
	/**
	 * Place a CoolBar / CoolItem / Control inside the current
	 * UI handle. These elements will maintain thier size based on
	 * the size of their 'parent' (this).
	 * 
	 * @param parent 
	 * @param orientation
	 */
	public void insertCoolBar(int orientation) {
		// Clean up the previous info in case we've changed orientation
		if (cb != null) {
			ci.dispose();
	        PresentationUtil.removeDragListener(cb, dragListener);
			cb.dispose();
		}
		
		// Create the necessary parts...
		cb = new CoolBar(this, orientation | SWT.FLAT);
		cb.setLocation(0,0);
		ci = new CoolItem(cb, SWT.FLAT);
		
		// Create a composite in order to get the handles to appear
		Composite comp = new Composite(cb, SWT.NONE);
		ci.setControl(comp);
	}
	
	/**
	 * Set the cursor to the four-way arrow to indicate that the
	 * trim can be dragged
	 */
	private void setDragCursor() {
    	Cursor dragCursor = toDrag.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL);
    	setCursor(dragCursor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point ctrlPrefSize = trim.getControl().computeSize(wHint, hHint);
		if (orientation == SWT.HORIZONTAL) {
			return new Point(getHandleSize(), ctrlPrefSize.y);
		}
		
		// Must be vertical....
		return new Point(ctrlPrefSize.x, getHandleSize());
	}
	
	/**
	 * Construct (if necessary) a context menu contribution item and return it. This
	 * is explicitly <code>public</code> so that trim elements can retrieve the item
	 * and add it into their own context menus if desired.
	 * 
	 * @return The contribution item for the handle's context menu. 
	 */
	public ContributionItem getDockingContribution() {
    	if (dockContributionItem == null) {
    		dockContributionItem = new ContributionItem() {
    			public void fill(Menu menu, int index) {
    				// populate from superclass
    				super.fill(menu, index);
    				
    				// Add a 'Close' menu entry if the trim supports the operation
    				if (trim.isCloseable()) {
	    				MenuItem closeItem = new MenuItem(menu, SWT.PUSH, index++);
	    				closeItem.setText(WorkbenchMessages.TrimCommon_Close);
	    				
	    				closeItem.addSelectionListener(new SelectionListener() {
							public void widgetSelected(SelectionEvent e) {
								handleCloseTrim();
							}

							public void widgetDefaultSelected(SelectionEvent e) {
							}
	    				});

	    				new MenuItem(menu, SWT.SEPARATOR, index++);
    				}
    				
    				// Test Hook: add a menu entry that brings up a dialog to allow
    				// testing with various GUI prefs.
//    				MenuItem closeItem = new MenuItem(menu, SWT.PUSH, index++);
//    				closeItem.setText("Change Preferences"); //$NON-NLS-1$
//    				
//    				closeItem.addSelectionListener(new SelectionListener() {
//						public void widgetSelected(SelectionEvent e) {
//							handleChangePreferences();
//						}
//
//						public void widgetDefaultSelected(SelectionEvent e) {
//						}
//    				});
//
//    				new MenuItem(menu, SWT.SEPARATOR, index++);
    				
    				// Create a cascading menu to allow the user to dock the trim
    				dockCascade = new MenuItem(menu, SWT.CASCADE, index++);
    				{
    					dockCascade.setText(WorkbenchMessages.TrimCommon_DockOn); 
    					
    					sidesMenu = new Menu(dockCascade);
    					radioButtons = new RadioMenu(sidesMenu, radioVal);
    					
						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Top, new Integer(SWT.TOP));
						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Bottom, new Integer(SWT.BOTTOM));
						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Left, new Integer(SWT.LEFT));
						radioButtons.addMenuItem(WorkbenchMessages.TrimCommon_Right, new Integer(SWT.RIGHT));
    					
    					dockCascade.setMenu(sidesMenu);
    				}

    		    	// if the radioVal changes it means that the User wants to change the docking location
    		    	radioVal.addChangeListener(new IChangeListener() {
    					public void update(boolean changed) {
    						if (changed) {
								handleShowOnChange();
							}
    					}
    		    	});
    				
    				// Provide Show / Hide trim capabilities
//    				showCascade = new MenuItem(menu, SWT.CASCADE, index++);
//    				{
//    					showCascade.setText(WorkbenchMessages.TrimCommon_ShowTrim); 
//    					
//    					showMenu = new Menu(dockCascade);
//    					
//    					// Construct a 'hide/show' cascade from -all- the existing trim...
//    					List trimItems = layout.getAllTrim();
//    					Iterator d = trimItems.iterator();
//    					while (d.hasNext()) {
//    						IWindowTrim trimItem = (IWindowTrim) d.next();
//							MenuItem item = new MenuItem(showMenu, SWT.CHECK);
//							item.setText(trimItem.getDisplayName());
//							item.setSelection(trimItem.getControl().getVisible());
//							item.setData(trimItem);
//							
//							// TODO: Make this work...wire it off for now
//							item.setEnabled(false);
//							
//							item.addSelectionListener(new SelectionListener() {
//
//								public void widgetSelected(SelectionEvent e) {
//									IWindowTrim trim = (IWindowTrim) e.widget.getData();
//									layout.setTrimVisible(trim, !trim.getControl().getVisible());
//								}
//
//								public void widgetDefaultSelected(SelectionEvent e) {
//								}
//								
//							});
//						}
//    					
//    					showCascade.setMenu(showMenu);
//    				}
    			}
    		};
    	}
    	return dockContributionItem;
    }

	/**
	 * Test Hook: Bring up a dialog that allows the user to
	 * modify the trimdragging GUI preferences.
	 */
//	private void handleChangePreferences() {
//		TrimDragPreferenceDialog dlg = new TrimDragPreferenceDialog(getShell());
//		dlg.open();
//	}
   
	/**
	 * Handle the event generated when the "Close" item is
	 * selected on the context menu. This removes the associated
	 * trim and calls back to the IWidnowTrim to inform it that
	 * the User has closed the trim.
	 */
	private void handleCloseTrim() {
		layout.removeTrim(trim);
		trim.handleClose();
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (radioButtons != null) {
            radioButtons.dispose();
        }

        // tidy up...
        removeControlListener(controlListener);
        removeListener(SWT.MenuDetect, menuListener);
        
        super.dispose();
    }

    /**
     * Begins dragging the trim
     * 
     * @param position initial mouse position
     */
    protected void startDraggingTrim(Point position) {
    	Rectangle fakeBounds = new Rectangle(100000, 0,0,0);
        DragUtil.performDrag(trim, fakeBounds, position, true);
    }

    /**
     * Shows the popup menu for an item in the fast view bar.
     */
    private void showDockTrimPopup(Point pt) {
        Menu menu = dockMenuManager.createContextMenu(toDrag);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }	    
}
