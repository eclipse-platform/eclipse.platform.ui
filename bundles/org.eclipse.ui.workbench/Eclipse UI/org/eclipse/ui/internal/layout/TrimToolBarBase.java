/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.IChangeListener;
import org.eclipse.ui.internal.IntModel;
import org.eclipse.ui.internal.RadioMenu;
import org.eclipse.ui.internal.TrimFrame;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.presentations.PresentationUtil;

/**
 * This control provides common UI functionality for trim elements
 * that wish to use a ToolBarManager-based implementation.
 * <p>
 * It provides the following features:
 * <p>
 * Drag affordance and handling:
 * <ol>
 * <li>The ToolBar is contained within a CoolBar/Item to provide the same
 * drag handle affordance as the rest of the trim
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
 * @since 3.3
 * </p>
 */
public abstract class TrimToolBarBase implements IWindowTrim {
	// Fields
	protected String id;
	protected int orientation;
	protected WorkbenchWindow wbw;
	protected TrimLayout layout;
	protected ToolBarManager tbMgr = null;
	protected ToolItem contextToolItem = null;

	// CoolBar handling
	private TrimFrame frame = null;
	private CoolBar cb = null;
	private CoolItem ci = null;
    
	// Context Menu
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

    private Listener tbListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showToolBarPopup(loc);
            }
        }
    };

    /**
     * This listener brings up the context menu
     */
    private Listener cbListener = new Listener() {
        public void handleEvent(Event event) {
            Point loc = new Point(event.x, event.y);
            if (event.type == SWT.MenuDetect) {
                showDockTrimPopup(loc);
            }
        }
    };
    
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
     * Create a new trim UI handle for a particular IWindowTrim item
     * 
     * @param layout the TrimLayout we're being used in
     * @param trim the IWindowTrim we're acting on behalf of
     * @param curSide  the SWT side that the trim is currently on
     */
    protected TrimToolBarBase(String id, int curSide, WorkbenchWindow wbw) {
    	this.id = id;
    	this.wbw = wbw;
    	this.layout = (TrimLayout) wbw.getTrimManager();
    }

    /**
	 * @param loc
	 */
	private void showToolBarPopup(Point loc) {
		Point tbLoc = tbMgr.getControl().toControl(loc);
		contextToolItem = tbMgr.getControl().getItem(tbLoc);
		MenuManager mm = tbMgr.getContextMenuManager();
		if (mm != null) {
			Menu menu = mm.createContextMenu(wbw.getShell());
	        menu.setLocation(loc.x, loc.y);
	        menu.setVisible(true);
		}
	}

	/**
     * Initialize the ToolBarManger for this instance. We create a
     * new ToolBarManager whenever we need to and this gives the
     * derived class a chance to define the ICI's and context
     * menu...
     * 
     * @param mgr The manager to initialize
     */
    public abstract void initToolBarManager(ToolBarManager mgr);
    
    /**
     * Hook any necessary listeners to the new ToolBar instance.
     * <p>
     * NOTE: Clients should add a dispose listener if necessary to
     * unhook listeners added through this call.
     * </p>
     * @param mgr The ToolBarManager whose control is to be hooked
     */
    public abstract void hookControl(ToolBarManager mgr);
    
	/**
	 * Set up the trim with its cursor, drag listener, context menu and menu listener.
	 * This method can also be used to 'recycle' a trim handle as long as the new handle
	 * is for trim under the same parent as it was originally used for.
	 */
	private void createControl(int curSide) {
		// out with the old
		dispose();
		
    	this.radioVal.set(curSide);
    	
    	// remember the orientation to use
    	orientation = (curSide == SWT.LEFT || curSide == SWT.RIGHT) ? SWT.VERTICAL  : SWT.HORIZONTAL;

    	frame = new TrimFrame(wbw.getShell());
		
		// Create the necessary parts...
		cb = new CoolBar(frame.getComposite(), orientation | SWT.FLAT);
		ci = new CoolItem(cb, SWT.FLAT);

		// Create (and 'fill') the toolbar
		tbMgr = new ToolBarManager(orientation | SWT.FLAT);
		
		// Have the subclass define any manager content
		initToolBarManager(tbMgr);
		
		// Create the new ToolBar
		ToolBar tb = tbMgr.createControl(cb);
		ci.setControl(tb);
		
		// Have the subclass hook any listeners
		hookControl(tbMgr);
		
		// set up the frame's layout
		update(true);
		
    	// Set the cursor affordance
    	Cursor dragCursor = getControl().getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL);
    	cb.setCursor(dragCursor);
    	
    	// Now, we have to explicity set the arrow for the TB
    	Cursor tbCursor = getControl().getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
    	tb.setCursor(tbCursor);
    	
    	//cb.setBackground(cb.getDisplay().getSystemColor(SWT.COLOR_RED));
    	
        // Set up the dragging behaviour
        PresentationUtil.addDragListener(cb, dragListener);
    	
    	// Create the docking context menu
    	dockMenuManager = new MenuManager();
    	dockContributionItem = getDockingContribution();
        dockMenuManager.add(dockContributionItem);

        tb.addListener(SWT.MenuDetect, tbListener);
        cb.addListener(SWT.MenuDetect, cbListener);
        
        //tbMgr.getControl().setBackground(cb.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        //tbMgr.getControl().pack(true);
        cb.pack(true);
        cb.setVisible(true);
        
        tbMgr.getControl().setVisible(true);
        cb.setVisible(true);
        frame.getComposite().setVisible(true);
    }

    /**
     * Handle the event generated when a User selects a new side to
     * dock this trim on using the context menu
     */
    private void handleShowOnChange() {
    	if (getControl() == null)
    		return;
    	
    	layout.removeTrim(this);
    	dock(radioVal.get());
    	layout.addTrim(radioVal.get(), this, null);
    	
    	// perform an optimized layout to show the trim in its new location
    	LayoutUtil.resize(getControl());
	}

	/**
	 * Force the toobar to re-synch to the model
	 * @param changed true if changes have occurred in the structure
	 */
	public void update(boolean changed) {
		tbMgr.update(changed);
		
		// Force a resize
		tbMgr.getControl().pack();
	    Point size = tbMgr.getControl().getSize();
	    //tbMgr.getControl().setBounds(0, 0, size.x, size.y);
	    Point ps = ci.computeSize (size.x, size.y);
		ci.setPreferredSize (ps);
		ci.setSize(ps);
		cb.pack();
		cb.update();
		LayoutUtil.resize(getControl());
	}
	
	/**
	 * Construct (if necessary) a context menu contribution item and return it. This
	 * is explicitly <code>public</code> so that trim elements can retrieve the item
	 * and add it into their own context menus if desired.
	 * 
	 * @return The contribution item for the handle's context menu. 
	 */
	private ContributionItem getDockingContribution() {
    	if (dockContributionItem == null) {
    		dockContributionItem = new ContributionItem() {
    			public void fill(Menu menu, int index) {
    				// populate from superclass
    				super.fill(menu, index);
    				
    				// Add a 'Close' menu entry if the trim supports the operation
    				if (isCloseable()) {
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
	 * @return The side the trm is currently on
	 */
	public int getCurrentSide() {
		return radioVal.get();
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
		handleClose();
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
    	if (getControl() == null || getControl().isDisposed())
    		return;
    	
        if (radioButtons != null) {
            radioButtons.dispose();
        }

        // tidy up...
        getControl().removeListener(SWT.MenuDetect, cbListener);
        
        tbMgr.dispose();
        tbMgr = null;
        
        getControl().dispose();
        frame = null;
    }

    /**
     * Begins dragging the trim
     * 
     * @param position initial mouse position
     */
    private void startDraggingTrim(Point position) {
    	Rectangle fakeBounds = new Rectangle(100000, 0,0,0);
        DragUtil.performDrag(this, fakeBounds, position, true);
    }

    /**
     * Shows the popup menu for an item in the fast view bar.
     */
    private void showDockTrimPopup(Point pt) {
        Menu menu = dockMenuManager.createContextMenu(this.getControl());
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#dock(int)
	 */
	public void dock(int dropSide) {
		createControl(dropSide);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getControl()
	 */
	public Control getControl() {
		if (frame == null)
			return null;
		
		return frame.getComposite();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getHeightHint()
	 */
	public int getHeightHint() {
		return getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getValidSides()
	 */
	public int getValidSides() {
		return SWT.BOTTOM | SWT.TOP | SWT.LEFT | SWT.RIGHT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#getWidthHint()
	 */
	public int getWidthHint() {
		return getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#handleClose()
	 */
	public void handleClose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#isCloseable()
	 */
	public boolean isCloseable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.layout.IWindowTrim#isResizeable()
	 */
	public boolean isResizeable() {
		return false;
	}	    
}
