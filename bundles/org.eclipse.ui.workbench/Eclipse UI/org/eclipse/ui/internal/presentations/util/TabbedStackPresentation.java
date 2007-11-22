/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM - bug 51580
 *     Chris Torrence, ITT Visual Information Solutions - bugs 51580 202208
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.preferences.IDynamicPropertyMap;
import org.eclipse.ui.internal.preferences.PreferenceStoreAdapter;
import org.eclipse.ui.internal.preferences.PreferencesAdapter;
import org.eclipse.ui.internal.preferences.PropertyMapAdapter;
import org.eclipse.ui.internal.preferences.ThemeManagerAdapter;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultPartList;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.0
 */
public final class TabbedStackPresentation extends StackPresentation {

    private PresentablePartFolder folder;
    private ISystemMenu systemMenu;
    private ISystemMenu partList;
    private PreferenceStoreAdapter apiPreferences = new PreferenceStoreAdapter(PrefUtil
            .getAPIPreferenceStore());
    private ThemeManagerAdapter themePreferences = new ThemeManagerAdapter(
            PlatformUI.getWorkbench().getThemeManager());
    
    private TabOrder tabs;

    private TabDragHandler dragBehavior;

    private boolean initializing = true;
    private int ignoreSelectionChanges = 0;
    
    private TabFolderListener tabFolderListener = new TabFolderListener() {
        public void handleEvent(TabFolderEvent e) {
            switch (e.type) {
             	case TabFolderEvent.EVENT_MINIMIZE: {
             	    getSite().setState(IStackPresentationSite.STATE_MINIMIZED);
             		break;
             	}
             	case TabFolderEvent.EVENT_MAXIMIZE: {
             	    getSite().setState(IStackPresentationSite.STATE_MAXIMIZED);
             		break;
             	}
             	case TabFolderEvent.EVENT_RESTORE: {
             	    getSite().setState(IStackPresentationSite.STATE_RESTORED);
             		break;
             	}
             	case TabFolderEvent.EVENT_CLOSE: {
                    IPresentablePart part = folder.getPartForTab(e.tab);

             		if (part != null) {
             		    getSite().close(new IPresentablePart[] { part });
             		}
             		break;
             	}
             	case TabFolderEvent.EVENT_SHOW_LIST: {
             	    showPartList();
             	    break;
             	}
             	case TabFolderEvent.EVENT_GIVE_FOCUS_TO_PART: { 
                    IPresentablePart part = getSite().getSelectedPart();
             		if (part != null) {
             		    part.setFocus();
             		}
             		break;
             	}
             	case TabFolderEvent.EVENT_PANE_MENU: {
                    IPresentablePart part = getSite().getSelectedPart();
             		if (part != null) {
             		    part.setFocus();
             		}
             		TabbedStackPresentation.this.showPaneMenu(folder
             				.getPartForTab(e.tab), new Point(e.x, e.y));
             		break;
             	}
             	case TabFolderEvent.EVENT_DRAG_START: {
             	    AbstractTabItem beingDragged = e.tab;
             	    Point initialLocation = new Point(e.x, e.y);
             	    
                    if (beingDragged == null) {
                        getSite().dragStart(initialLocation, false);
                    } else {
                        IPresentablePart part = folder.getPartForTab(beingDragged);
                        
                        try {
                            dragStart = folder.indexOf(part);
                            getSite().dragStart(part, initialLocation, false);                    
                        } finally {
                            dragStart = -1;
                        }
                    }
                    break;
             	}
             	case TabFolderEvent.EVENT_TAB_SELECTED: {
                    if (ignoreSelectionChanges > 0) {
                        return;
                    }
                    
                    IPresentablePart part = folder.getPartForTab(e.tab);
                    
                    if (part != null) {
                        getSite().selectPart(part);
                    }
             	    break;
             	}
             	case TabFolderEvent.EVENT_SYSTEM_MENU: {
                    IPresentablePart part = folder.getPartForTab(e.tab);
                    
                    if (part == null) {
                        part = getSite().getSelectedPart();
                    }
                    
                    if (part != null) {
                        showSystemMenu(new Point(e.x, e.y), part);
                    }
             	    break;
             	}
             	case TabFolderEvent.EVENT_PREFERRED_SIZE: {
             	    IPresentablePart part = folder.getPartForTab(e.tab);             	       
             	    if (part == null) {
             	        // Standalone views with no title have no tab, so just get the part.
             	        IPresentablePart[] parts = getSite().getPartList();
             	        if (parts.length > 0) part = parts[0];
             	    }
             	    if (part == getSite().getSelectedPart()) {
             	        getSite().flushLayout();
             	    }
             	    break;
             	}
            }
        }
    };

    private int dragStart = -1;
    private Map prefs = new HashMap();
    
    public TabbedStackPresentation(IStackPresentationSite site, AbstractTabFolder widget, ISystemMenu systemMenu) {
        this(site, new PresentablePartFolder(widget), systemMenu);
    }
    
    public TabbedStackPresentation(IStackPresentationSite site, PresentablePartFolder folder, ISystemMenu systemMenu) {
        this(site, folder, new LeftToRightTabOrder(folder), new ReplaceDragHandler(folder.getTabFolder()), systemMenu);
    }
    
    public TabbedStackPresentation(IStackPresentationSite site,
            PresentablePartFolder newFolder, TabOrder tabs, TabDragHandler dragBehavior, ISystemMenu systemMenu) {
        super(site);
        this.systemMenu = systemMenu;
        
        this.folder = newFolder;
        this.tabs = tabs;
        this.dragBehavior = dragBehavior;

        // Add a dispose listener. This will call the presentationDisposed()
        // method when the widget is destroyed.
        folder.getTabFolder().getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                presentationDisposed();
            }
        });

        folder.getTabFolder().addListener(tabFolderListener);
        
        this.partList = new DefaultPartList(site, newFolder);
    }
    
    /**
     * Restores a presentation from a previously stored state
     * 
     * @param serializer (not null)
     * @param savedState (not null)
     */
    public void restoreState(IPresentationSerializer serializer,
            IMemento savedState) {
        tabs.restoreState(serializer, savedState);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#saveState(org.eclipse.ui.presentations.IPresentationSerializer, org.eclipse.ui.IMemento)
     */
    public void saveState(IPresentationSerializer context, IMemento memento) {
        super.saveState(context, memento);

        tabs.saveState(context, memento);
    }
    
    /**
     * Returns true iff the presentation has been disposed
     * 
     * @return true iff the presentation has been disposed
     */
    private boolean isDisposed() {
        return folder == null || folder.isDisposed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setBounds(org.eclipse.swt.graphics.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
        folder.setBounds(bounds);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#computeMinimumSize()
     */
    public Point computeMinimumSize() {
        return folder.getTabFolder().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    /**
     * Returns the minimum size for this stack, taking into account
     * the available perpendicular space.
     * @param width indicates whether a width (=true) or a height (=false) is being computed
     * @param availablePerpendicular available space perpendicular to the direction being measured
     * or INFINITE if unbounded (pixels).
     * @return returns the preferred minimum size (pixels).
     * This is a width if width == true or a height if width == false.  
     */
    private int computePreferredMinimumSize(boolean width, int availablePerpendicular) {
        int minSize;
        int hint = availablePerpendicular == INFINITE ? SWT.DEFAULT : availablePerpendicular;
        if (width) {
            minSize = folder.getTabFolder().computeSize(SWT.DEFAULT, hint).x;
        } else {
            minSize = folder.getTabFolder().computeSize(hint, SWT.DEFAULT).y;
        }
        return minSize;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)
     */
    public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredResult) {

        // If there is exactly one part in the stack, this just returns the
        // preferred size of the part as the preferred size of the stack.
        IPresentablePart[] parts = getSite().getPartList();
        if (parts.length == 1 && parts[0] != null 
        		&& !(getSite().getState() == IStackPresentationSite.STATE_MINIMIZED)) {
            int partSize = parts[0].computePreferredSize(width,
                    availableParallel, availablePerpendicular, preferredResult);

            if (partSize == INFINITE)
            	return partSize;
            
            // Adjust preferred size to take into account tab and border trim.
            int minSize = computePreferredMinimumSize(width, availablePerpendicular);
            if (width) {
                // PaneFolder adds some bogus tab spacing, so just find the maximum width.
                partSize = Math.max(minSize, partSize);
            } else {
            	// Add them (but only if there's enough room)
            	if (INFINITE-minSize > partSize)
            		partSize += minSize;
            }

            return partSize;
        }    

        if (preferredResult != INFINITE || getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
            int minSize = computePreferredMinimumSize(width, availablePerpendicular);
	        
	        if (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
	            return minSize;
	        }
	        
	        return Math.max(minSize, preferredResult);
        }
        
        return INFINITE;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#getSizeFlags(boolean)
     */
    public int getSizeFlags(boolean width) {
        int flags = 0;
        // If there is exactly one part in the stack,
        // then take into account the size flags of the part.
        IPresentablePart[] parts = getSite().getPartList();
        if (parts.length == 1 && parts[0] != null) {
            flags |= parts[0].getSizeFlags(width);
        }

        return flags | super.getSizeFlags(width);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#showPartList()
     */
    public void showPartList() {
        if (partList != null) {
            final int numberOfParts = folder.getTabFolder().getItemCount();
            if (numberOfParts > 0) {
                partList.show(getControl(), folder.getTabFolder()
                        .getPartListLocation(), getSite().getSelectedPart());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#dispose()
     */
    public void dispose() {
        // Dispose the tab folder's widgetry
        folder.getTabFolder().getControl().dispose();
    }

    /**
     * Called when the tab folder is disposed.
     */
    private void presentationDisposed() {
        apiPreferences.dispose();
        themePreferences.dispose();
        
        Iterator iter = prefs.values().iterator();
        while(iter.hasNext()) {
            PropertyMapAdapter next = (PropertyMapAdapter)iter.next();
            next.dispose();
        }

        if (systemMenu != null) {
            systemMenu.dispose();
        }

        if (partList != null) {
            partList.dispose();
        }

        systemMenu = null;
        partList = null;
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
     */
    public void setActive(int newState) {
        folder.getTabFolder().setActive(newState);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        IPresentablePart current = getSite().getSelectedPart();
        if (current != null) {
            current.setVisible(isVisible);
        }

        folder.setVisible(isVisible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setState(int)
     */
    public void setState(int state) {
        folder.getTabFolder().setState(state);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#getControl()
     */
    public Control getControl() {
        return folder.getTabFolder().getControl();
    }

    /**
     * @return AbstractTabFolder the presentation's tab folder
     */
    public AbstractTabFolder getTabFolder() {
        return folder.getTabFolder();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#addPart(org.eclipse.ui.presentations.IPresentablePart, java.lang.Object)
     */
    public void addPart(IPresentablePart newPart, Object cookie) {
        ignoreSelectionChanges++;
        try {
	        if (initializing) {
	            tabs.addInitial(newPart);
	        } else {
	            if (cookie == null) {
	                tabs.add(newPart);
	            } else {
	                int insertionPoint = dragBehavior
	                        .getInsertionPosition(cookie);
	
	                tabs.insert(newPart, insertionPoint);
	            }
	        }
        } finally {
            ignoreSelectionChanges--;
        }

        if (tabs.getPartList().length == 1) {
            if (newPart.getSizeFlags(true) != 0 || newPart.getSizeFlags(false) != 0) {
                getSite().flushLayout();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#movePart(org.eclipse.ui.presentations.IPresentablePart, java.lang.Object)
     */
    public void movePart(IPresentablePart toMove, Object cookie) {
        ignoreSelectionChanges++;
        try {
	        int insertionPoint = dragBehavior.getInsertionPosition(cookie);
	        
	        if (insertionPoint == folder.indexOf(toMove)) {
	            return;
	        }
	        
	        tabs.move(toMove, insertionPoint);
        } finally {
            ignoreSelectionChanges--;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#removePart(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void removePart(IPresentablePart oldPart) {
        ignoreSelectionChanges++;
        try {
            tabs.remove(oldPart);
        } finally {
            ignoreSelectionChanges--;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#selectPart(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void selectPart(IPresentablePart toSelect) {
        initializing = false;

        tabs.select(toSelect);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
     */
    public StackDropResult dragOver(Control currentControl, Point location) {
        return dragBehavior.dragOver(currentControl, location, dragStart);
    }

    public void showSystemMenu() {
        showSystemMenu(folder.getTabFolder().getSystemMenuLocation(), getSite().getSelectedPart());
    }
    
    public void showSystemMenu(Point displayCoordinates, IPresentablePart context) {
        if (context != getSite().getSelectedPart()) {
            getSite().selectPart(context);
        }
        systemMenu.show(getControl(), displayCoordinates, context);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#showPaneMenu()
     */
    public void showPaneMenu() {
        IPresentablePart part = getSite().getSelectedPart();
        
        if (part != null) {
            showPaneMenu(part, folder.getTabFolder().getPaneMenuLocation());
        }
    }
        
    public void showPaneMenu(IPresentablePart part, Point location) {
        Assert.isTrue(!isDisposed());
        
        IPartMenu menu = part.getMenu();

        if (menu != null) {
            menu.showMenu(location);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#getTabList(org.eclipse.ui.presentations.IPresentablePart)
     */
    public Control[] getTabList(IPresentablePart part) {
        ArrayList list = new ArrayList();
        if (folder.getTabFolder().getTabPosition() == SWT.BOTTOM) {
            if (part.getControl() != null) {
				list.add(part.getControl());
			}
        }

        list.add(folder.getTabFolder().getControl());
        
        if (part.getToolBar() != null) {
            list.add(part.getToolBar());
        }
        
        if (folder.getTabFolder().getTabPosition() == SWT.TOP) {
            if (part.getControl() != null) {
				list.add(part.getControl());
			}
        }

        return (Control[]) list.toArray(new Control[list.size()]);
    }

    public void setPartList(ISystemMenu menu) {
        this.partList = menu;
    }
    
    public IDynamicPropertyMap getTheme() {
        return themePreferences;
    }
    
    public IDynamicPropertyMap getApiPreferences() {
        return apiPreferences;
    }
    
    public IDynamicPropertyMap getPluginPreferences(Plugin toQuery) {
        String id = toQuery.getBundle().getSymbolicName();
        IDynamicPropertyMap result = (IDynamicPropertyMap)prefs.get(id);
        
        if (result != null) {
            return result;
        }
        
        result = new PreferencesAdapter(toQuery.getPluginPreferences());
        prefs.put(id, result);
        return result;
    }
    
    /**
     * Move the tabs around.  This is for testing <b>ONLY</b>.
     * @param part the part to move
     * @param index the new index
     * @since 3.2
     */
    public void moveTab(IPresentablePart part, int index) {
    	tabs.move(part, index);
    	folder.layout(true);
    }
    
    /**
     * Get the tab list. This is for testing <b>ONLY</b>.
     * @return the presentable parts in order.
     * @since 3.2
     */
    public IPresentablePart[] getPartList() {
    	return tabs.getPartList();
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
        folder.getTabFolder().showMinMax(show);
	}
}
