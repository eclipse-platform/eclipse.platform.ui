/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.internal.layout.SizeCache;
import org.eclipse.ui.internal.presentations.util.AbstractTabFolder;
import org.eclipse.ui.internal.presentations.util.AbstractTabItem;
import org.eclipse.ui.internal.presentations.util.PartInfo;
import org.eclipse.ui.internal.presentations.util.ProxyControl;
import org.eclipse.ui.internal.presentations.util.TabFolderEvent;
import org.eclipse.ui.internal.util.Util;

/**
 * 
 * 
 * 
 * @since 3.1
 */
public class DetachedViewTabFolder extends AbstractTabFolder {

    private Composite control;
    private CTabFolder folder;
    private Control viewToolBar;
    private ViewForm viewForm;
    private ProxyControl toolbarProxy;
    private Composite topLeftControl;
    private SizeCache toolbarCache;
    private Cursor dragCursor;
    
    // CTabFolder listener
    private CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
         */
        public void close(CTabFolderEvent event) {
            event.doit = false;
            fireEvent(TabFolderEvent.EVENT_CLOSE, getTab(event.item));
        }

        public void showList(CTabFolderEvent event) {
            event.doit = false;
            fireEvent(TabFolderEvent.EVENT_SHOW_LIST);
        }

    };
    
    
    private Listener selectionListener = new Listener() {
        public void handleEvent(Event e) {
            fireEvent(TabFolderEvent.EVENT_TAB_SELECTED, getTab(e.item));
        }
    };

    private DisposeListener tabDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
            if (e.widget == control) {
                disposed();
            }
        }  
    };
    
    public DetachedViewTabFolder(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        control.addDisposeListener(tabDisposeListener);
        CellLayout layout = new CellLayout(1)
            .setDefaultRow(Row.fixed())
            .setDefaultColumn(Row.growing())
            .setRow(0, Row.growing())
            .setSpacing(0, 0)
            .setMargins(0, 0);
        control.setLayout(layout);
        dragCursor = parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL);
        
        viewForm = new ViewForm(control, SWT.FLAT);
        attachListeners(viewForm, false);
        
        topLeftControl = new Composite(viewForm, SWT.NONE);
        topLeftControl.setLayout(new CellLayout(0).setDefaultColumn(Row.fixed()).setSpacing(0,0).setMargins(0,0));
        topLeftControl.setCursor(dragCursor);
        attachListeners(topLeftControl, false);
        toolbarProxy = new ProxyControl(topLeftControl);
        viewForm.setTopLeft(topLeftControl);
       
        toolbarCache = new SizeCache();
        
        folder = new CTabFolder(control, SWT.BOTTOM);
        folder.setMinimizeVisible(false);
        folder.setMaximizeVisible(false);
        folder.setUnselectedCloseVisible(true);
        folder.setUnselectedImageVisible(true);
        folder.addListener(SWT.Selection, selectionListener);
        folder.addCTabFolder2Listener(expandListener);
        attachListeners(folder, false);
        
        // Initialize view menu dropdown
        {            
            ToolBar actualToolBar = new ToolBar(viewForm, SWT.FLAT | SWT.NO_BACKGROUND);
            viewToolBar = actualToolBar;
            
            ToolItem pullDownButton = new ToolItem(actualToolBar, SWT.PUSH);
            Image hoverImage = WorkbenchImages
                    .getImage(IWorkbenchGraphicConstants.IMG_LCL_RENDERED_VIEW_MENU);
            pullDownButton.setDisabledImage(hoverImage);
            pullDownButton.setImage(hoverImage);
            pullDownButton.setToolTipText(WorkbenchMessages.Menu); 
            actualToolBar.addMouseListener(new MouseAdapter() {
                public void mouseDown(MouseEvent e) {
                    fireEvent(TabFolderEvent.EVENT_PANE_MENU, getSelection(), getPaneMenuLocation());
                }
            });
        }
        
        viewForm.setTopRight(viewToolBar);
    }

    protected void disposed() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#computeSize(int, int)
     */
    public Point computeSize(int widthHint, int heightHint) {
        return new Point(50, 50);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#add(int)
     */
    public AbstractTabItem add(int index, int flags) {
        DetachedViewTabItem item = new DetachedViewTabItem(this, index, flags);
        item.getWidget().setData(item);
        item.getWidget().addDisposeListener(tabDisposeListener);
        
        if (folder.getItemCount() == 2) {
            layout(true);
        }
        
        return item;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#layout(boolean)
     */
    public void layout(boolean flushCache) {
        
        Rectangle oldBounds = viewForm.getBounds();
        
        CellLayout layout = (CellLayout)control.getLayout();
        if (folder.getItemCount() < 2) {
            layout.setRow(1, Row.fixed(0));
        } else {
            layout.setRow(1, Row.fixed(folder.getTabHeight() + 2));
        }
        
        super.layout(flushCache);
        
        control.layout(flushCache);
        
        Rectangle newBounds = viewForm.getBounds();
        
        if (Util.equals(oldBounds, newBounds)) {
            viewForm.layout(flushCache);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getClientArea()
     */
    public Rectangle getClientArea() {
        Control content = viewForm.getContent();
        
        if (content == null) {
            return new Rectangle(0,0,0,0);
        }
        
        return Geometry.toControl(control, DragUtil.getDisplayBounds(content));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getItems()
     */
    public AbstractTabItem[] getItems() {
        CTabItem[] items = folder.getItems();
        
        AbstractTabItem[] result = new AbstractTabItem[items.length];
        
        for (int i = 0; i < result.length; i++) {
            result[i] = getTab(items[i]);
        }
        
        return result;
    }
    
    /**
     * @param item
     * @return
     * @since 3.1
     */
    private AbstractTabItem getTab(Widget item) {
        return (AbstractTabItem)item.getData();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelection(org.eclipse.ui.internal.presentations.util.Widget)
     */
    public void setSelection(AbstractTabItem toSelect) {
        if (toSelect == null) {
            return;
        }
        
        DetachedViewTabItem tab = (DetachedViewTabItem) toSelect;
        folder.setSelection((CTabItem)tab.getWidget());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelectedInfo(org.eclipse.ui.internal.presentations.util.PartInfo)
     */
    public void setSelectedInfo(PartInfo info) {
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getToolbarParent()
     */
    public Composite getToolbarParent() {
        return viewForm;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getTabArea()
     */
    public Rectangle getTabArea() {
        return Geometry.toDisplay(folder.getParent(), folder.getBounds());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setToolbar(org.eclipse.swt.widgets.Control)
     */
    public void setToolbar(Control toolbarControl) {
        
        if (toolbarControl == getToolbar()) {
            return;
        }
        
        if (toolbarControl != null) {
            toolbarCache.setControl(toolbarControl);
            toolbarProxy.setTarget(toolbarCache);
            toolbarProxy.getControl().getParent().changed(new Control[]{toolbarProxy.getControl()});
            LayoutUtil.resize(toolbarProxy.getControl());

            toolbarProxy.layout();

        } else {
            toolbarCache.setControl(null);
            toolbarProxy.setTarget(null);
        }
        
        super.setToolbar(toolbarControl);
    }
    
    public Control getControl() {
        return control;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#isOnBorder(org.eclipse.swt.graphics.Point)
     */
    public boolean isOnBorder(Point globalPos) {
        Point localPos = getControl().toControl(globalPos);
        
        Rectangle clientArea = getClientArea();
        return localPos.y > clientArea.y && localPos.y < clientArea.y + clientArea.height; 
    }
    
    public AbstractTabItem getSelection() {
        CTabItem sel = folder.getSelection();
        
        if (sel == null) {
            return null;
        }
        
        return getTab(sel);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getContentParent()
     */
    public Composite getContentParent() {
        return viewForm;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setContent(org.eclipse.swt.widgets.Control)
     */
    public void setContent(Control newContent) {
        viewForm.setContent(newContent);
    }
    
    /**
     * @return
     * @since 3.1
     */
    public CTabFolder getTabFolder() {
        return folder;
    }
    
    protected void handleDragStarted(Point displayPos, Event e) {
        if (isOnBorder(displayPos)) {
            return;
        }
        
        AbstractTabItem tab = null;
        
        if (DragUtil.getDisplayBounds(viewForm).contains(displayPos)) {
            tab = getSelection();
        } else {
            tab = getItem(displayPos); 
        }
        
        fireEvent(TabFolderEvent.EVENT_DRAG_START, tab, displayPos);
    }

    public Point getPartListLocation() {

        // get the last visible item
        int numItems = folder.getItemCount();
        CTabItem item = null, tempItem = null;
        for (int i = 0; i < numItems; i++) {
            tempItem = folder.getItem(i);
            if (tempItem.isShowing())
                item = tempItem;
        }

        // if we have no visible tabs, abort.
        if (item == null)
            return new Point(0, 0);

        Rectangle itemBounds = item.getBounds();
        int x = itemBounds.x + itemBounds.width;
        int y = itemBounds.y + itemBounds.height;
        return folder.toDisplay(new Point(x, y));
    }
    
    public Point getPaneMenuLocation() {
        Point toolbarSize = viewToolBar.getSize();
        
        return viewToolBar.toDisplay(0,toolbarSize.y);
    }
    
    public void enablePaneMenu(boolean enabled) {
        if (enabled) {
            viewToolBar.setVisible(true);
        } else {
            viewToolBar.setVisible(false);
        }
    }

    public void itemRemoved() {
        if (folder.getItemCount() == 1 && !control.isDisposed() && !viewForm.isDisposed()) {
            layout(true);
        }
    }

}
