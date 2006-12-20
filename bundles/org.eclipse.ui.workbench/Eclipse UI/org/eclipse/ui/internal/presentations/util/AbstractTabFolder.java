/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;

/**
 * @since 3.1
 */
public abstract class AbstractTabFolder {
    
    private List listeners = new ArrayList(1);

    private Control toolbar;
    private int state;

    public abstract Point computeSize(int widthHint, int heightHint);

    public abstract AbstractTabItem add(int index, int flags);

    public abstract Composite getContentParent();
    public abstract void setContent(Control newContent);
    
    public abstract AbstractTabItem[] getItems();

    public abstract AbstractTabItem getSelection();
    public abstract void setSelection(AbstractTabItem toSelect);
    public abstract void setSelectedInfo(PartInfo info);
    public abstract void enablePaneMenu(boolean enabled);
    private int activeState = IStackPresentationSite.STATE_RESTORED;
    
	private Listener menuListener = new Listener() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			Point globalPos = new Point(event.x, event.y);
		    handleContextMenu(globalPos, event);			
		}
	};

    private Listener dragListener = new Listener() {
        public void handleEvent(Event e) {
            Point globalPos = ((Control)e.widget).toDisplay(e.x, e.y);
            handleDragStarted(globalPos, e);
        }
    };
	
	private MouseListener mouseListener = new MouseAdapter() {
		
		// If we single-click on an empty space on the toolbar, move focus to the
		// active control
		public void mouseDown(MouseEvent e) {
			Point p = ((Control)e.widget).toDisplay(e.x, e.y);

			handleMouseDown(p, e);
		}
		
		
		// If we double-click on the toolbar, maximize the presentation
		public void mouseDoubleClick(MouseEvent e) {
			Point p = ((Control)e.widget).toDisplay(e.x, e.y);

			handleDoubleClick(p, e);						
		}
	};
    
    public void setActive(int activeState) {
        this.activeState = activeState;
    }
    
    public int getActive() {
        return activeState;
    }

    /**
     * Returns the location where the pane menu should be opened when activated
     * by a keyboard shortcut (display coordinates)
     * 
     * @return the location for the pane menu (display coordinates)
     * @since 3.1
     */
    public Point getPaneMenuLocation() {
        return getControl().toDisplay(new Point(0,0));
    }

    /**
     * Returns the location where the part list should be opened when activated
     * by a keyboard shortcut (display coordinates)
     * 
     * @return the location for the part list (display coordinates)
     * @since 3.1
     */
    public Point getPartListLocation() {
        return getSystemMenuLocation();
    }

    /**
     * Returns the location where the pane menu should be opened when activated
     * by a keyboard shortcut (display coordinates)
     * 
     * @return the location for the pane menu (display coordinates)
     * @since 3.1
     */
    public Point getSystemMenuLocation() {
        return getControl().toDisplay(new Point(0,0));
    }
    
    /**
     * Returns the parent composite that should be used for creating the toolbar.
     * Any control passed into setToolbar must have this composite as its parent.
     * 
     * @return the parent composite that should be used for creating the toolbar
     * 
     * @since 3.1
     */
    public abstract Composite getToolbarParent();

    /**
     * Returns the main control for this folder.
     * 
     * @return the main control for the folder
     * @since 3.1
     */
    public abstract Control getControl();
    
    public AbstractTabItem getItem(int idx) {
        return getItems()[idx];
    }

    public AbstractTabItem getItem(Point toFind) {
        AbstractTabItem[] items = getItems();
        
        for (int i = 0; i < items.length; i++) {
            AbstractTabItem item = items[i];
            
            if (item.getBounds().contains(toFind)) {
                return item;
            }
        }
        
        return null;
    }
    
    public AbstractTabItem findItem(Object dataToFind) {
        AbstractTabItem[] items = getItems();
        
        for (int i = 0; i < items.length; i++) {
            AbstractTabItem item = items[i];
            
            if (item.getData() == dataToFind) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the index of the given item, or -1 if the given item is
     * not found in this tab folder. Subclasses should override this if
     * the underlying SWT widget has an equivalent method
     * 
     * @param item item to find
     * @return the index of the given item or -1
     */
    public int indexOf(AbstractTabItem item) {
        AbstractTabItem[] items = getItems();

        for (int idx = 0; idx < items.length; idx++) {
            AbstractTabItem next = items[idx];

            if (next == item) {
                return idx;
            }
        }

        return -1;
    }

    public int getItemCount() {
        return getItems().length;
    }

    public void setToolbar(Control toolbarControl) {
        this.toolbar = toolbarControl;
    }

    public final Control getToolbar() {
        return toolbar;
    }

    /**
     * Sets the current state for the folder
     * 
     * @param state one of the IStackPresentationSite.STATE_* constants
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Returns the title area for this control (in the control's coordinate system)
     * 
     * @return
     */
    public abstract Rectangle getTabArea();

    /**
     * Called when the tab folder's shell becomes active or inactive. Subclasses
     * can override this to change the appearance of the tabs based on activation.
     * 
     * @param isActive
     */
    public void shellActive(boolean isActive) {
    }

    /**
     * Adds the given listener to this AbstractTabFolder
     * 
     * @param newListener the listener to add
     */
    public final void addListener(TabFolderListener newListener) {
        listeners.add(newListener);
    }

    /**
     * Removes the given listener from this AbstractTabFolder
     * 
     * @param toRemove the listener to remove
     */
    public final void removeListener(TabFolderListener toRemove) {
        listeners.remove(toRemove);
    }

    public void flushToolbarSize() {
        
    }
    
    protected final void fireEvent(TabFolderEvent e) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            TabFolderListener next = (TabFolderListener)iter.next();

            next.handleEvent(e);
        }        
    }
    
    protected final void fireEvent(int id) {
        fireEvent(new TabFolderEvent(id));
    }
    
    protected final void fireEvent(int id, AbstractTabItem w) {
        fireEvent(new TabFolderEvent(id, w, 0, 0));
    }
    
    protected final void fireEvent(int id, AbstractTabItem w, Point pos) {
        fireEvent(new TabFolderEvent(id, w, pos));
    }
    
    public void layout(boolean flushCache) {
    }
    
    public void setTabPosition(int tabPosition) {
    }
    
    public int getTabPosition() {
        return SWT.TOP;
    }
    
    public int getState() {
        return state;
    }
    
    protected void attachListeners(Control theControl, boolean recursive) {
        theControl.addListener(SWT.MenuDetect, menuListener);
        theControl.addMouseListener(mouseListener);
        PresentationUtil.addDragListener(theControl, dragListener);
        
        if (recursive && theControl instanceof Composite) {
            Composite composite = (Composite) theControl;
            Control[] children = composite.getChildren();
            
            for (int i = 0; i < children.length; i++) {
                Control control = children[i];
                
                attachListeners(control, recursive);
            }
        }
    }
    
    protected void detachListeners(Control theControl, boolean recursive) {
        theControl.removeListener(SWT.MenuDetect, menuListener);
        theControl.removeMouseListener(mouseListener);
        PresentationUtil.removeDragListener(theControl, dragListener);
        
        if (recursive && theControl instanceof Composite) {
            Composite composite = (Composite) theControl;
            Control[] children = composite.getChildren();
            
            for (int i = 0; i < children.length; i++) {
                Control control = children[i];
                
                detachListeners(control, recursive);
            }
        }
    }
    
    protected void handleContextMenu(Point displayPos, Event e) {
        if (isOnBorder(displayPos)) {
            return;
        }

        AbstractTabItem tab = getItem(displayPos); 
        
        fireEvent(TabFolderEvent.EVENT_SYSTEM_MENU, tab, displayPos);
    }
    
    protected void handleMouseDown(Point displayPos, MouseEvent e) {
        fireEvent(TabFolderEvent.EVENT_GIVE_FOCUS_TO_PART);
    }

    protected void handleDoubleClick(Point displayPos, MouseEvent e) {
        if (isOnBorder(displayPos)) {
            return;
        }
        
		if (getState() == IStackPresentationSite.STATE_MAXIMIZED) {
			fireEvent(TabFolderEvent.EVENT_RESTORE);
		} else {
		    fireEvent(TabFolderEvent.EVENT_MAXIMIZE);
		}
    }
    
    protected void handleDragStarted(Point displayPos, Event e) {

        if (isOnBorder(displayPos)) {
            return;
        }
        
        AbstractTabItem tab = getItem(displayPos);
        fireEvent(TabFolderEvent.EVENT_DRAG_START, tab, displayPos);
    }

    /**
     * Returns true iff the given point is on the border of the folder.
     * By default, double-clicking, context menus, and drag/drop are disabled
     * on the folder's border.
     *  
     * @param toTest a point (display coordinates)
     * @return true iff the point is on the presentation border
     * @since 3.1
     */
    public boolean isOnBorder(Point toTest) {
        return false;
    }
    
    /**
	 * Set the folder to visible. This can be extended to propogate the
	 * visibility request to other components in the subclass.
	 * 
	 * @param visible
	 *            <code>true</code> - the folder is visible.
	 * @since 3.2
	 */
    public void setVisible(boolean visible) {
		getControl().setVisible(visible);
	}

	/**
	 * Cause the folder to hide or show its
	 * Minimize and Maximize affordances.
	 * 
	 * @param show
	 *            <code>true</code> - the min/max buttons are visible.
	 * @since 3.3
	 */
	public void showMinMax(boolean show) {
	}
}
