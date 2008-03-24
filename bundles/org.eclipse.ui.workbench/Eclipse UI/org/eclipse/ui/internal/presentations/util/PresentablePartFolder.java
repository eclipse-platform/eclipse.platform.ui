/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.1
 */
public final class PresentablePartFolder implements IPresentablePartList {    
    private AbstractTabFolder folder;
    private IPresentablePart current;
    //private ProxyControl toolbarProxy;
    private Control contentProxy;
    private static PartInfo tempPartInfo = new PartInfo(); 
    
	/**
	 * Movement listener. Updates the bounds of the target to match the 
	 * bounds of the dummy control.
	 */
	private ControlListener contentListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
			layoutContent();
		}

		public void controlResized(ControlEvent e) {
		}
		
	};
    
    private ShellListener shellListener = new ShellAdapter() {
        public void shellActivated(ShellEvent e) {
            folder.shellActive(true);
        }

        public void shellDeactivated(ShellEvent e) {
            folder.shellActive(false);
        }
    };

    /**
     * Listener attached to all child parts. It responds to changes in part properties
     */
    private IPropertyListener childPropertyChangeListener = new IPropertyListener() {
        public void propertyChanged(Object source, int property) {

            if (source instanceof IPresentablePart) {
                IPresentablePart part = (IPresentablePart) source;

                childPropertyChanged(part, property);
            }
        }
    };

    /**
     * Dispose listener that is attached to the main control. It triggers cleanup of
     * any listeners. This is required to prevent memory leaks.
     */
    private DisposeListener tabDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
            if (e.widget == folder.getControl()) {
                // If we're disposing the main control...
                disposed();
            }
        }
    };
    private List partList = new ArrayList(4);
    
    public PresentablePartFolder(AbstractTabFolder folder) {
        this.folder = folder;
        
        folder.getControl().getShell().addShellListener(shellListener);
        folder.shellActive(folder.getControl().getDisplay()
                .getActiveShell() == folder.getControl().getShell());

        folder.getControl().addDisposeListener(tabDisposeListener);
        
        //toolbarProxy = new ProxyControl(folder.getToolbarParent());
        
        // NOTE: if the shape of contentProxy changes, the fix for bug 85899 in EmptyTabFolder.computeSize may need adjustment.
        contentProxy = new Composite(folder.getContentParent(), SWT.NONE);
        contentProxy.setVisible(false);
        for (Control current = contentProxy; current != folder.getControl().getParent(); current = current.getParent()) {
            current.addControlListener(contentListener);
        }
        folder.setContent(contentProxy);
        
    }
    
    /**
     * 
     * @since 3.1
     */
    private void layoutContent() {
        if (current != null) {
            Rectangle clientArea = DragUtil.getDisplayBounds(contentProxy);
            
            current.setBounds(Geometry.toControl(folder.getControl().getParent(), clientArea));
        }
    }

    /**
     * 
     * @since 3.1
     */
    protected void disposed() {
        folder.getControl().getShell().removeShellListener(shellListener);
        Iterator iter = partList.iterator();
        while(iter.hasNext()) {
            IPresentablePart next = (IPresentablePart)iter.next();
            
            next.removePropertyListener(childPropertyChangeListener);
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.util.IPresentablePartList#getPartList()
	 */
	public IPresentablePart[] getPartList() {
	    AbstractTabItem[] items = folder.getItems();
	    IPresentablePart[] result = new IPresentablePart[items.length];
	    
	    for (int i = 0; i < items.length; i++) {
            result[i] = getPartForTab(items[i]);
            
        }
	    
	    return result;
	}
    
    /**
     * Adds the given presentable part directly into this presentation at the 
     * given index. Does nothing if a tab already exists for the given part.
     * This is intended to be called by TabOrder and its subclasses.
     *
     * @param part part to add
     * @param idx index to insert at
     */
    public void insert(IPresentablePart part, int idx) {
        Assert.isTrue(!folder.getControl().isDisposed());

        if (getTab(part) != null) {
        	if(indexOf(part) != idx)
        		move(part, idx);
            return;
        }

        idx = Math.min(idx, folder.getItemCount());

        AbstractTabItem item;

        int style = SWT.NONE;

        if (part.isCloseable()) {
            style |= SWT.CLOSE;
        }
        
        item = folder.add(idx, style);

        item.setData(part);

        initTab(item, part);

        part.addPropertyListener(childPropertyChangeListener);
        partList.add(part);
    }
    
    public void remove(IPresentablePart toRemove) {
        if (toRemove == current) {
            select(null);
        }
        
        internalRemove(toRemove);
    }
    
    private void internalRemove(IPresentablePart toRemove) {
        AbstractTabItem item = getTab(toRemove);
        if (item != null) {
            item.dispose();
        }
        if (partList.contains(toRemove)) {
            toRemove.removePropertyListener(childPropertyChangeListener);
            partList.remove(toRemove);
        }
    }
    
    /**
     * Moves the given part to the given index. When this method returns,
     * indexOf(part) will return newIndex.
     * 
     * @param part
     * @param newIndex
     */
    public void move(IPresentablePart part, int newIndex) {
        int currentIndex = indexOf(part);

        if (currentIndex == newIndex) {
            return;
        }

        internalRemove(part);
        insert(part, newIndex);
        
        if (current == part) {
            folder.setSelection(getTab(part));
        }
    }

    /**
     * Returns the number of parts in this folder
     */
    public int size() {
        return folder.getItemCount();
    }

    public void setBounds(Rectangle bounds) {
        Point minSize = folder.computeSize(bounds.width, SWT.DEFAULT);
        
        if (folder.getState() == IStackPresentationSite.STATE_MINIMIZED && minSize.y < bounds.height) {
            bounds = Geometry.copy(bounds);
            bounds.height = minSize.y;
        }
        
        // Set the tab folder's bounds
        folder.getControl().setBounds(bounds);

        layout(false);
    }
    
    public void select(IPresentablePart toSelect) {
        
        if (toSelect == current) {
            return;
        }
        
        if (toSelect != null) {
            toSelect.setVisible(true);
        }
        
        if (current != null) {
            current.setVisible(false);
            setToolbar(null);
        }
        
        current = toSelect;
        
        AbstractTabItem selectedItem = getTab(toSelect);

        folder.setSelection(selectedItem);

        if (selectedItem != null) {
            // Determine if we need to un-bold this tab
            selectedItem.setBold(false);
            initTab(selectedItem, toSelect);
        } else {
            setToolbar(null);
        }

        layout(true);
    }
    
    private void setToolbar(Control newToolbar) {
        if (folder.getToolbar() != newToolbar) {
            folder.setToolbar(newToolbar);
        }        
    }
    private boolean isVisible = true;
    
    
    private void childPropertyChanged(IPresentablePart part, int property) {
        AbstractTabItem tab = getTab(part);
        
        switch (property) {
        case IPresentablePart.PROP_HIGHLIGHT_IF_BACK:
            if (tab != null && getCurrent() != part) {//Set bold if it doesn't currently have focus
                tab.setBold(true);
                initTab(tab, part);
            }
            break;
            
        case IPresentablePart.PROP_TOOLBAR:
            if (tab != null && getCurrent() == part) {
                folder.flushToolbarSize();
            }
            /* falls through */
        case IPresentablePart.PROP_CONTENT_DESCRIPTION:
        case IPresentablePart.PROP_PANE_MENU:
        case IPresentablePart.PROP_TITLE:
            if (tab != null) {
                initTab(tab, part);
                if (getCurrent() == part) {
                    layout(true);
                }
            }
            break;
        case IPresentablePart.PROP_PREFERRED_SIZE:
            folder.fireEvent(new TabFolderEvent(TabFolderEvent.EVENT_PREFERRED_SIZE, tab, 0, 0 ));
            break;
        default:
            if (tab != null) initTab(tab, part);
        }
    }
    
    protected void initTab(AbstractTabItem item, IPresentablePart part) {
        tempPartInfo.set(part);
        item.setInfo(tempPartInfo);
        
        item.setBusy(part.isBusy());
        if (part == getCurrent()) {
            folder.setSelectedInfo(tempPartInfo);
            folder.enablePaneMenu(part.getMenu() != null);
            
            setToolbar(part.getToolBar());
        }
    }

    public boolean isDisposed() {
        return SwtUtil.isDisposed(folder.getControl());
    }
    
    public IPresentablePart getPartForTab(AbstractTabItem tab) {
        Assert.isTrue(!isDisposed());

        if (tab == null) {
            return null;
        }
        
        IPresentablePart part = (IPresentablePart) tab.getData();

        return part;
    }

    /**
     * Returns the tab for the given part, or null if there is no such tab
     * 
     * @param part the part being searched for
     * @return the tab for the given part, or null if there is no such tab
     */
    public AbstractTabItem getTab(IPresentablePart part) {
        Assert.isTrue(!isDisposed());
        
        return folder.findItem(part);
    }
    
    

    public int indexOf(IPresentablePart part) {
        AbstractTabItem item = getTab(part);

        if (item == null) {
            return -1;
        }

        return folder.indexOf(item);
    }
    
    public AbstractTabFolder getTabFolder() {
        return folder;
    }
    
    public void setVisible(boolean isVisible) {
        this.isVisible  = isVisible;
        getTabFolder().setVisible(isVisible);
        if (isVisible) {
            layout(true);
        }
    }
    
    public void layout(boolean changed) {
        if (!isVisible) {
            // Don't bother with layout if we're not visible
            return;
        }
        // Lay out the tab folder and compute the client area
        folder.layout(changed);

        //toolbarProxy.layout();
        
        layoutContent();
    }
    
    public IPresentablePart getCurrent() {
        return current;
    }
}
