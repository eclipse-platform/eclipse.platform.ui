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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.PaneFolder;
import org.eclipse.ui.internal.presentations.PaneFolderButtonListener;
import org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder;
import org.eclipse.ui.internal.presentations.newapi.AbstractTabItem;
import org.eclipse.ui.internal.presentations.newapi.EnhancedFillLayout;
import org.eclipse.ui.internal.presentations.newapi.PartInfo;
import org.eclipse.ui.internal.presentations.newapi.TabFolderEvent;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.1
 */
public class DefaultTabFolder extends AbstractTabFolder {

    private PaneFolder paneFolder;
    private Composite viewToolBar;
    private Label titleLabel;
    
    private PaneFolderButtonListener buttonListener = new PaneFolderButtonListener() {
        public void stateButtonPressed(int buttonId) {
            fireEvent(TabFolderEvent.stackStateToEventId(buttonId));
        }

        /**
         * Called when a close button is pressed.
         *   
         * @param item the tab whose close button was pressed
         */
        public void closeButtonPressed(CTabItem item) {
            fireEvent(TabFolderEvent.EVENT_CLOSE, getTab(item));
        }
        /**
         * 
         * @since 3.0
         */
        public void showList(CTabFolderEvent event) {
            event.doit = false;
            fireEvent(TabFolderEvent.EVENT_SHOW_LIST);
        }
    };
    
    private Listener selectionListener = new Listener() {
        public void handleEvent(Event e) {
            AbstractTabItem item = getTab((CTabItem) e.item);

            if (item != null) {
                fireEvent(TabFolderEvent.EVENT_TAB_SELECTED, item);
            }
        }
    };
    
    private static DefaultTabFolderColors defaultColors = new DefaultTabFolderColors();
    
    private DefaultTabFolderColors[] activeShellColors = {defaultColors, defaultColors, defaultColors};
    private DefaultTabFolderColors[] inactiveShellColors = {defaultColors, defaultColors, defaultColors};
    private boolean shellActive = false;
    
    public DefaultTabFolder(Composite parent, int flags, boolean allowMin, boolean allowMax) {
        paneFolder = new PaneFolder(parent, flags | SWT.NO_BACKGROUND);
        paneFolder.addButtonListener(buttonListener);
        paneFolder.setMinimizeVisible(allowMin);
        paneFolder.setMaximizeVisible(allowMax);
        paneFolder.getControl().addListener(SWT.Selection, selectionListener);
        paneFolder.setTopRight(null);
        
        // Initialize view menu dropdown
        {
            viewToolBar = new Composite(paneFolder.getControl(), SWT.NO_BACKGROUND);
            viewToolBar.setLayout(new EnhancedFillLayout());
            viewToolBar.setVisible(false);
            
            ToolBar actualToolBar = new ToolBar(viewToolBar, SWT.FLAT | SWT.NO_BACKGROUND);
            
	        ToolItem pullDownButton = new ToolItem(actualToolBar, SWT.PUSH);
	        Image hoverImage = WorkbenchImages
	                .getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
	        pullDownButton.setDisabledImage(hoverImage);
	        pullDownButton.setImage(hoverImage);
	        pullDownButton.setToolTipText(WorkbenchMessages.getString("Menu")); //$NON-NLS-1$
            actualToolBar.addMouseListener(new MouseAdapter() {
                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
                 */
                public void mouseDown(MouseEvent e) {
                    fireEvent(TabFolderEvent.EVENT_PANE_MENU, getSelection(), getPaneMenuLocation());
                }
            });
        }
        
        // Initialize content description label
        {
	        titleLabel = new Label(paneFolder.getControl(), SWT.NONE);
	        titleLabel.moveAbove(null);
	        titleLabel.setVisible(false);
            attachListeners(titleLabel, false);
        }
        
        attachListeners(paneFolder.getControl(), false);
        attachListeners(paneFolder.getViewForm(), false);
        
        paneFolder.setTabHeight(computeTabHeight());
        
        viewToolBar.moveAbove(null);
    }

    /**
     * Changes the minimum number of characters to display in the pane folder
     * tab. This control how much information will be displayed to the user.
     * 
     * @param count
     *            The number of characters to display in the tab folder; this
     *            value should be a positive integer.
     * @see org.eclipse.swt.custom.CTabFolder#setMinimumCharacters(int)
     * @since 3.1
     */
    public void setMinimumCharacters(int count) {
        paneFolder.setMinimumCharacters(count);
    }
    
    public void setSimpleTabs(boolean simple) {
        paneFolder.setSimpleTab(simple);
    }
    
    /**
     * @param item
     * @return
     * @since 3.1
     */
    protected DefaultTabItem getTab(CTabItem item) {
        return (DefaultTabItem)item.getData();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#computeSize(int, int)
     */
    public Point computeSize(int widthHint, int heightHint) {
        return paneFolder.computeMinimumSize();
    }

    /* package */ PaneFolder getFolder() {
        return paneFolder;
    }
    
    public AbstractTabItem getSelection() {
        return getTab(paneFolder.getSelection());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#add(int)
     */
    public AbstractTabItem add(int index, int flags) {
        DefaultTabItem result = new DefaultTabItem(this, index, flags);
        
        result.getWidget().setData(result);
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getContentParent()
     */
    public Composite getContentParent() {
        return paneFolder.getContentParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setContent(org.eclipse.swt.widgets.Control)
     */
    public void setContent(Control newContent) {
        paneFolder.setContent(newContent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getItems()
     */
    public AbstractTabItem[] getItems() {
        CTabItem[] items = paneFolder.getItems();
        
        AbstractTabItem[] result = new AbstractTabItem[items.length];
        
        for (int i = 0; i < result.length; i++) {
            result[i] = getTab(items[i]);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getItemCount()
     */
    public int getItemCount() {
        // Override retrieving all the items when we just want the count.
        return paneFolder.getItemCount();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setSelection(org.eclipse.ui.internal.presentations.newapi.AbstractTabItem)
     */
    public void setSelection(AbstractTabItem toSelect) {
        paneFolder.setSelection(indexOf(toSelect));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getToolbarParent()
     */
    public Composite getToolbarParent() {
        return paneFolder.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getControl()
     */
    public Control getControl() {
        return paneFolder.getControl();
    }
    
    public void setUnselectedCloseVisible(boolean visible) {
        paneFolder.setUnselectedCloseVisible(visible);
    }

    public void setUnselectedImageVisible(boolean visible) {
        paneFolder.setUnselectedImageVisible(visible);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getTabArea()
     */
    public Rectangle getTabArea() {
        return paneFolder.getTitleArea();
    }

    /**
     * @param enabled
     * @since 3.1
     */
    public void enablePaneMenu(boolean enabled) {
        if (enabled) {
            paneFolder.setTopRight(viewToolBar);
            viewToolBar.setVisible(true);
        } else {
            paneFolder.setTopRight(null);
            viewToolBar.setVisible(false);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setSelectedInfo(org.eclipse.ui.internal.presentations.newapi.PartInfo)
     */
    public void setSelectedInfo(PartInfo info) {
        if (!info.contentDescription.equals(Util.ZERO_LENGTH_STRING)) {
            paneFolder.setTopLeft(titleLabel);
            titleLabel.setVisible(true);
        } else {
            paneFolder.setTopLeft(null);
            titleLabel.setVisible(false);
        }
        
        if (!Util.equals(titleLabel.getText(), info.contentDescription)) {
            titleLabel.setText(info.contentDescription);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getPaneMenuLocation()
     */
    public Point getPaneMenuLocation() {
        Point toolbarSize = viewToolBar.getSize();
        
        return viewToolBar.toDisplay(0,toolbarSize.y);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getPartListLocation()
     */
    public Point getPartListLocation() {
        return paneFolder.getControl().toDisplay(paneFolder.getChevronLocation());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getSystemMenuLocation()
     */
    public Point getSystemMenuLocation() {
        Rectangle bounds = DragUtil.getDisplayBounds(paneFolder.getControl());
		
		int idx = paneFolder.getSelectionIndex();
		if (idx > -1) {
		    CTabItem item = paneFolder.getItem(idx);
		    Rectangle itemBounds = item.getBounds();
		
		    bounds.x += itemBounds.x;
		    bounds.y += itemBounds.y;
		}
		
		Point location = new Point(bounds.x, bounds.y
		        + paneFolder.getTabHeight());
		
		return location;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#isOnBorder(org.eclipse.swt.graphics.Point)
     */
    public boolean isOnBorder(Point toTest) {
        Control content = paneFolder.getContent();
        if (content != null) {
            Rectangle displayBounds = DragUtil.getDisplayBounds(content);
            
            if (paneFolder.getTabPosition() == SWT.TOP) {
                return toTest.y >= displayBounds.y;
            }
            
            if (toTest.y >= displayBounds.y && toTest.y < displayBounds.y + displayBounds.height) {
                return true;
            }
        }
        
        return super.isOnBorder(toTest);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#layout(boolean)
     */
    public void layout(boolean flushCache) {
        paneFolder.layout(flushCache);
        super.layout(flushCache);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setState(int)
     */
    public void setState(int state) {
        paneFolder.setState(state);
        super.setState(state);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setActive(int)
     */
    public void setActive(int activeState) {
        super.setActive(activeState);
        updateColors();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setTabPosition(int)
     */
    public void setTabPosition(int tabPosition) {
        paneFolder.setTabPosition(tabPosition);
        super.setTabPosition(tabPosition);
        layout(true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setToolbar(org.eclipse.swt.widgets.Control)
     */
    public void setToolbar(Control toolbarControl) {
        paneFolder.setTopCenter(toolbarControl);
        super.setToolbar(toolbarControl);
    }
    
    public void setColors(DefaultTabFolderColors colors, int activationState, boolean shellActivationState) {
        Assert.isTrue(activationState < activeShellColors.length);
                
        if (shellActivationState) {
            activeShellColors[activationState] = colors;
        } else {
            inactiveShellColors[activationState] = colors;
        }
        
        if (activationState == getActive() && shellActive == shellActivationState) {
            updateColors();
        }
    }
    
    /**
     * 
     * @since 3.1
     */
    private void updateColors() {
        DefaultTabFolderColors currentColors = shellActive ? 
                activeShellColors[getActive()] 
                : inactiveShellColors[getActive()];
                
        paneFolder.setSelectionForeground(currentColors.foreground);
        paneFolder.setSelectionBackground(currentColors.background, currentColors.percentages, currentColors.vertical);
    }

    public void setColors(DefaultTabFolderColors colors, int activationState) {
        setColors(colors, activationState, true);
        setColors(colors, activationState, false);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#shellActive(boolean)
     */
    public void shellActive(boolean isActive) {
        this.shellActive = isActive;
        super.shellActive(isActive);
        
        updateColors();
    }

    /**
     * @param font
     * @since 3.1
     */
    public void setFont(Font font) {
        if (font != paneFolder.getControl().getFont()) {
            paneFolder.getControl().setFont(font);
            layout(true);
            paneFolder.setTabHeight(computeTabHeight());
        }
    }
    
    /**
     * @return the required tab height for this folder.
     */
    protected int computeTabHeight() {
        GC gc = new GC(getControl());

        // Compute the tab height
        int tabHeight = Math.max(viewToolBar.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).y, gc.getFontMetrics().getHeight());

        gc.dispose();

        return tabHeight;
    }

    /**
     * @param b
     * @since 3.1
     */
    public void setSingleTab(boolean b) {
        paneFolder.setSingleTab(b);
        AbstractTabItem[] items = getItems();
        
        for (int i = 0; i < items.length; i++) {
            DefaultTabItem item = (DefaultTabItem)items[i];
        
            item.updateTabText();
        }
        
        layout(true);
    }
}
