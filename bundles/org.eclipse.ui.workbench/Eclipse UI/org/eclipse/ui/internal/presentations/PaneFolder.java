/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.layout.SizeCache;
import org.eclipse.ui.internal.presentations.util.ProxyControl;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This class implements the tab folders that contains can contain two toolbars
 * and status text. Wherever possible, the toolbars are aligned with the tabs.
 * If there is not enough room beside the tabs, the toolbars are aligned with
 * the status text. This is the same tab folder that is used to arrange views
 * and editors in Eclipse.
 * <p>
 * This is closely related to DefaultPartPresentation, but they have different
 * responsibilities. This is essentially a CTabFolder that can manage a toolbar.
 * It should not depend on data structures from the workbench, and its public
 * interface should only use SWT objects or listeners. DefaultPartPresentation
 * uses a PaneFolder to arrange views or editors. Knowledge of higher-level data
 * structures should go there.
 * </p>
 * <p>
 * Although it is not actually a control, the public interface is much like an
 * SWT control. Implementation-wise, this is actually a combination of a
 * CTabFolder and a ViewForm. It encapsulates the details of moving the toolbar
 * between the CTabFolder and the ViewForm, and provides a simpler interface to
 * the ViewForm/CTabFolder.
 * </p>
 * To be consistent with SWT composites, this object can deal with its children
 * being disposed without warning. This is treated like a removal.
 * 
 * @since 3.0
 */
public final class PaneFolder {
    // Tab folder and associated proxy controls
    private CTabFolder tabFolder;

    private Control titleAreaProxy;

    // View form and associated proxy controls
    private ViewForm viewForm;

    private ProxyControl contentProxy;

    private ProxyControl viewFormTopLeftProxy;

    private ProxyControl viewFormTopRightProxy;

    private ProxyControl viewFormTopCenterProxy;

    // Cached sizes of the top-right and top-center controls
    private SizeCache topRightCache = new SizeCache();

    private SizeCache topCenterCache = new SizeCache();

    private SizeCache topLeftCache = new SizeCache();

    private boolean putTrimOnTop = true;

    // HACK: Sometimes the topright control isn't resized when
    // CTabFolder.setBounds is called.
    // We use the following data structures to detect if this has happened and
    // force a layout when necessary.
    private boolean topRightResized = false;

    private boolean useTopRightOptimization = false;

    private int lastWidth = 0;

    // END OF HACK

    private DisposeListener tabFolderDisposeListener = new DisposeListener() {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e) {
            PaneFolder.this.widgetDisposed();
        }
    };

    /**
     * Listens for its children being disposed, and removes them if this happens
     * (although this may indicate a programming error, this behavior is
     * consistent with SWT composites).
     */
    private DisposeListener prematureDisposeListener = new DisposeListener() {

        public void widgetDisposed(DisposeEvent e) {
            Control disposedControl = (Control) e.widget;

            if (isDisposed()) {
                return;
            }

            // Probably unnecessary, but it can't hurt garbage collection
            disposedControl.removeDisposeListener(this);

            if (disposedControl == topLeftCache.getControl()) {
                setTopLeft(null);
            }

            if (disposedControl == topRightCache.getControl()) {
                setTopRight(null);
            }

            if (disposedControl == topCenterCache.getControl()) {
                setTopCenter(null);
            }
        }

    };

    /**
     * List of PaneFolderButtonListener
     */
    private List buttonListeners = new ArrayList(1);

    private int state = IStackPresentationSite.STATE_RESTORED;

    /**
     * State of the folder at the last mousedown event. This is used to prevent
     * a mouseup over the minimize or maximize buttons from undoing a state
     * change that was caused by the mousedown.
     */
    private int mousedownState = -1;
    
    /**
     * Location of the last mousedown event. This is used to determine if the
     * mouseup event occured on the same item.
     */
    private Point mousedownPoint = new Point(-1, -1);
    
    /**
     * Time of the last mousedown event. This is used to determine if the
     * mouseup event occurs within a reasonable time.
     */
    private long mousedownTime = 0L;
    
    /**
	 * Timeout value for when a pair of mousedown, mouseup events should not be
	 * interpreted as a click.
	 */
    private static long CLICK_TIME = 1000;

    // CTabFolder listener
    private CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
        public void minimize(CTabFolderEvent event) {
            event.doit = false;
            notifyButtonListeners(IStackPresentationSite.STATE_MINIMIZED);
        }

        public void restore(CTabFolderEvent event) {
            event.doit = false;
            notifyButtonListeners(IStackPresentationSite.STATE_RESTORED);
        }

        public void maximize(CTabFolderEvent event) {
            event.doit = false;
            notifyButtonListeners(IStackPresentationSite.STATE_MAXIMIZED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
         */
        public void close(CTabFolderEvent event) {
            event.doit = false;
            notifyCloseListeners((CTabItem) event.item);
        }

        public void showList(CTabFolderEvent event) {
            notifyShowListeners(event);
        }

    };

    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseDown(MouseEvent e) {
            mousedownState = getState();
            mousedownPoint = new Point(e.x, e.y);
            mousedownTime = e.time & 0x0FFFFFFFFL;
        }

        public void mouseUp(MouseEvent e) {
			if (e.button == 2 && e.count == 1) {
				Point mouseupPoint = new Point(e.x, e.y);
				CTabItem item = tabFolder.getItem(mouseupPoint);
				long mouseupTime = e.time & 0x0FFFFFFFFL;
				if (item != null && (mouseupTime - mousedownTime <= CLICK_TIME)) {
					CTabItem mousedownItem = tabFolder.getItem(mousedownPoint);
					if (mousedownItem == item) {
						notifyCloseListeners(item);
					}
				}
			}
        }
        
        public void mouseDoubleClick(MouseEvent e) {
        }
    };

    private boolean showButtons = true;
    private boolean minimizeVisible = false;
    private boolean maximizeVisible = false;

    /**
     * Make sure we don't recursively enter the layout() code.
     */
	private boolean inLayout = false;

	private int tabPosition;

    /**
     * Creates a pane folder. This will create exactly one child control in the
     * given parent.
     * 
     * @param parent
     * @param flags
     */
    public PaneFolder(Composite parent, int flags) {
        // Initialize tab folder
        {
            tabFolder = new CTabFolder(parent, flags);
            
			tabFolder.setMRUVisible(((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).enableMRUTabVisibility());

            // Create a proxy control to measure the title area of the tab folder
            titleAreaProxy = new Composite(tabFolder, SWT.NO_BACKGROUND);
            titleAreaProxy.setVisible(false);
            titleAreaProxy.addControlListener(new ControlListener() {
                public void controlMoved(ControlEvent e) {
                    topRightResized = true;
                }

                public void controlResized(ControlEvent e) {
                    topRightResized = true;

					// bug 101683 - we need to do a layout of the PaneFolder
					// when the title area proxy is resized.
					if (!inLayout && !PaneFolder.this.isDisposed()
							&& viewForm!=null && contentProxy!=null) {
						PaneFolder.this.aboutToResize();
						PaneFolder.this.layout(false);
					}
                }
            });
            tabFolder.setTopRight(titleAreaProxy, SWT.FILL);

            tabFolder.addCTabFolder2Listener(expandListener);

            tabFolder.addMouseListener(mouseListener);

            tabFolder.addDisposeListener(tabFolderDisposeListener);
        }

        // Initialize view form
        {
            viewForm = new ViewForm(tabFolder, SWT.NO_BACKGROUND);

            // Only attach these to the viewForm when there's actually a control
            // to display
            viewFormTopLeftProxy = new ProxyControl(viewForm);
            viewFormTopCenterProxy = new ProxyControl(viewForm);
            viewFormTopRightProxy = new ProxyControl(viewForm);

            contentProxy = new ProxyControl(viewForm);
            viewForm.setContent(contentProxy.getControl());
        }
    }

    /**
     * Returns the title area (the empty region to the right of the tabs), in
     * the tab folder's coordinate system.
     * 
     * @return the title area (the empty region to the right of the tabs)
     */
    public Rectangle getTitleArea() {
        return titleAreaProxy.getBounds();
    }

    /**
     * Return the main control for this pane folder
     * 
     * @return
     */
    public Composite getControl() {
        return tabFolder;
    }

    public void flushTopCenterSize() {
        topCenterCache.flush();
        viewForm.changed(new Control[] {viewFormTopCenterProxy.getControl()});
    }
    
    /**
     * Sets the top-center control (usually a toolbar), or null if none. Note
     * that the control can have any parent.
     * 
     * @param topCenter
     *            the top-center control or null if none
     */
    public void setTopCenter(Control topCenter) {
        if (topCenter == topCenterCache.getControl()) {
            return;
        }

        removeDisposeListener(topCenterCache.getControl());

        topCenterCache.setControl(topCenter);
        
        if (putTrimOnTop) {
            viewFormTopCenterProxy.setTarget(null);
        } else {
            viewFormTopCenterProxy.setTarget(topCenterCache);
        }
        
        viewForm.changed(new Control[] {viewFormTopCenterProxy.getControl()});
        
        if (topCenter != null) {
            topCenter.addDisposeListener(prematureDisposeListener);
        
            if (!putTrimOnTop) {
                if (!viewForm.isDisposed()) {
                    viewForm.setTopCenter(viewFormTopCenterProxy.getControl());
                }
            }
        } else {
            if (!putTrimOnTop) {
                if (!viewForm.isDisposed()) {
                    viewForm.setTopCenter(null);
                }
            }
        }
    }

    /**
     * Sets the top-right control (usually a dropdown), or null if none
     * 
     * @param topRight
     */
    public void setTopRight(Control topRight) {
        if (topRightCache.getControl() == topRight) {
            return;
        }

        removeDisposeListener(topRightCache.getControl());

        topRightCache.setControl(topRight);
        
        if (putTrimOnTop) {
            viewFormTopRightProxy.setTarget(null);
        } else {
            viewFormTopRightProxy.setTarget(topRightCache);
        }
        
        if (topRight != null) {
            topRight.addDisposeListener(prematureDisposeListener);
            if (!putTrimOnTop) {
                
                viewForm.setTopRight(viewFormTopRightProxy.getControl());
            }
        } else {
            if (!putTrimOnTop) {
                viewForm.setTopRight(null);
            }
        }
    }

    /**
     * Sets the top-left control (usually a title label), or null if none
     * 
     * @param topLeft
     */
    public void setTopLeft(Control topLeft) {
        if (topLeftCache.getControl() == topLeft) {
            return;
        }

        removeDisposeListener(topLeftCache.getControl());

        topLeftCache.setControl(topLeft);
        // The top-left control always goes directly in the ViewForm
        if (topLeft != null) {
            topLeft.addDisposeListener(prematureDisposeListener);
            viewFormTopLeftProxy.setTarget(topLeftCache);
            viewForm.setTopLeft(viewFormTopLeftProxy.getControl());
        } else {
            viewFormTopLeftProxy.setTarget(null);
            viewForm.setTopLeft(null);
        }
    }

    /**
     * Optimization: calling this method immediately before setting the
     * control's bounds will allow for improved caching.
     */
    public void aboutToResize() {
        useTopRightOptimization = true;
        topRightResized = false;
        lastWidth = getControl().getBounds().width;
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
    	showButtons = show;
    	setMaximizeVisible(show);
    	setMinimizeVisible(show);
    	layout(true);    	
    }
    
    public void layout(boolean flushCache) {
    	if (inLayout) {
    		return;
    	}
    	
        inLayout = true;
		try {
	        
        	viewForm.setLayoutDeferred(true);
	
	        tabFolder.setMinimizeVisible(showButtons && minimizeVisible);
	        tabFolder.setMaximizeVisible(showButtons && maximizeVisible);
	
	        // Flush the cached sizes if necessary
	        if (flushCache) {
	            topLeftCache.flush();
	            topRightCache.flush();
	            topCenterCache.flush();
	        }
	
	        // HACK: Force the tab folder to do a layout, since it doesn't always
	        // resize its title area each time setBounds is called.
	        if (!(useTopRightOptimization && (topRightResized || lastWidth == getControl()
	                .getBounds().width))) {
	            // If we can't use the optimization, then we need to force a layout
	            // of the tab folder
	            tabFolder.setTopRight(titleAreaProxy, SWT.FILL);
	        }
	        useTopRightOptimization = false;
	        // END OF HACK
	
	        Rectangle titleArea = DragUtil.getDisplayBounds(titleAreaProxy);
	
	        Point topRightSize = topRightCache
	                .computeSize(SWT.DEFAULT, SWT.DEFAULT);
	        Point topCenterSize = topCenterCache.computeSize(SWT.DEFAULT,
	                SWT.DEFAULT);
	
	        // Determine if there is enough room for the trim in the title area
	        int requiredWidth = topRightSize.x + topCenterSize.x;
	        int requiredHeight = Math.max(topRightSize.y, topCenterSize.y);
	
	        boolean lastTrimOnTop = putTrimOnTop;
	        putTrimOnTop = (titleArea.width >= requiredWidth && titleArea.height >= requiredHeight);
	
	        Control topRight = topRightCache.getControl();
	        Control topCenter = topCenterCache.getControl();
	
	        if (putTrimOnTop) {
	            // Try to avoid calling setTop* whenever possible, since this will
	            // trigger a layout
	            // of the viewForm.
	            if (!lastTrimOnTop) {
	                //	Arrange controls in the title bar
	                viewFormTopCenterProxy.setTarget(null);
	                viewFormTopRightProxy.setTarget(null);
	                viewForm.setTopCenter(null);
	                viewForm.setTopRight(null);
	            }
	
	            Rectangle topRightArea = new Rectangle(titleArea.x
	                    + titleArea.width - topRightSize.x, titleArea.y
	                    + (titleArea.height - topRightSize.y) / 2, topRightSize.x,
	                    topRightSize.y);
	
	            if (topRight != null) {
	                topRight.setBounds(Geometry.toControl(topRight.getParent(),
	                        topRightArea));
	            }
	
	            if (topCenter != null) {
	                Rectangle topCenterArea = new Rectangle(topRightArea.x
	                        - topCenterSize.x, titleArea.y
	                        + (titleArea.height - topCenterSize.y) / 2,
	                        topCenterSize.x, topCenterSize.y);
	
	                Rectangle localCoords = Geometry.toControl(topCenter
	                        .getParent(), topCenterArea);
	                
	                topCenter.setBounds(localCoords);
	            }
	        } else {
                if (topCenter != null) {
                    viewFormTopCenterProxy.setTarget(topCenterCache);
                    viewForm.setTopCenter(viewFormTopCenterProxy.getControl());
                }

                if (topRight != null) {
                    viewFormTopRightProxy.setTarget(topRightCache);
                    viewForm.setTopRight(viewFormTopRightProxy.getControl());
                }
	        }
	
	        Rectangle newBounds = tabFolder.getClientArea();
	        viewForm.setBounds(newBounds);
        } finally {
        	viewForm.setLayoutDeferred(false);
        	inLayout = false;
        }

        viewFormTopRightProxy.layout();
        viewFormTopLeftProxy.layout();
        viewFormTopCenterProxy.layout();
    }

    public Composite getContentParent() {
        return viewForm;
    }

    public void setContent(Control newContent) {
        viewForm.setContent(newContent);
    }

    /**
     * Returns the current state of the folder (as shown on the button icons)
     * 
     * @return one of the IStackPresentationSite.STATE_* constants
     */
    public int getState() {
        return state;
    }

    /**
     * @param buttonId
     *            one of the IStackPresentationSite.STATE_* constants
     */
    protected void notifyButtonListeners(int buttonId) {
        if (mousedownState == getState()) {
            Iterator iter = buttonListeners.iterator();

            while (iter.hasNext()) {
                PaneFolderButtonListener listener = (PaneFolderButtonListener) iter
                        .next();

                listener.stateButtonPressed(buttonId);
            }
        }
    }

    public Control getContent() {
        return viewForm.getContent();
    }

    /**
     * Notifies all listeners that the user clicked on the chevron
     * 
     * @param tabItem
     */
    protected void notifyShowListeners(CTabFolderEvent event) {
        Iterator iter = buttonListeners.iterator();

        while (iter.hasNext()) {
            PaneFolderButtonListener listener = (PaneFolderButtonListener) iter
                    .next();

            listener.showList(event);
        }
    }

    /**
     * Notifies all listeners that the close button was pressed
     * 
     * @param tabItem
     */
    protected void notifyCloseListeners(CTabItem tabItem) {
        Iterator iter = buttonListeners.iterator();

        while (iter.hasNext()) {
            PaneFolderButtonListener listener = (PaneFolderButtonListener) iter
                    .next();

            listener.closeButtonPressed(tabItem);
        }
    }

    /**
     * Sets the state that will be shown on the CTabFolder's buttons
     * 
     * @param state
     *            one of the IStackPresentationSite.STATE_* constants
     */
    public void setState(int state) {
        this.state = state;

        tabFolder.setMinimized(state == IStackPresentationSite.STATE_MINIMIZED);
        tabFolder.setMaximized(state == IStackPresentationSite.STATE_MAXIMIZED);
    }

    public void addButtonListener(PaneFolderButtonListener listener) {
        buttonListeners.add(listener);
    }

    public void removeButtonListener(PaneFolderButtonListener listener) {
        buttonListeners.remove(listener);
    }

    public void setTabPosition(int newTabPosition) {
        tabPosition = newTabPosition;
        tabFolder.setTabPosition(tabPosition);
    }

    public int getTabPosition() {
        return tabPosition;
    }

    public boolean isDisposed() {
        return tabFolder == null || tabFolder.isDisposed();
    }

    public CTabItem createItem(int style, int index) {
        return new CTabItem(tabFolder, style, index);
    }

    public Point computeMinimumSize() {
        Point result = Geometry.getSize(tabFolder.computeTrim(0, 0, 0, 0));

        // Add some space for the minimize and maximize buttons plus a tab.
        // Right now this isn't exposed from SWT as API, so we just add 50
        // pixels.
        result.x += 100;
        return result;
    }

    /**
     * Removes the dispose listener from the given control, unless the given
     * control is null or disposed.
     * 
     * @param oldControl
     *            control to detach the dispose listener from
     */
    private void removeDisposeListener(Control oldControl) {
        if (!SwtUtil.isDisposed(oldControl)) {
            oldControl.removeDisposeListener(prematureDisposeListener);
        }
    }

    private void widgetDisposed() {
        removeDisposeListener(topCenterCache.getControl());
        topCenterCache.setControl(null);
        removeDisposeListener(topRightCache.getControl());
        topRightCache.setControl(null);
        removeDisposeListener(topLeftCache.getControl());
        topLeftCache.setControl(null);
    }

    public Point getChevronLocation() {
        // get the last visible item
        int numItems = tabFolder.getItemCount();
        CTabItem item = null, tempItem = null;
        for (int i = 0; i < numItems; i++) {
            tempItem = tabFolder.getItem(i);
            if (tempItem.isShowing()) {
				item = tempItem;
			}
        }

        // if we have no visible tabs, abort.
        if (item == null) {
			return new Point(0, 0);
		}

        Rectangle itemBounds = item.getBounds();
        int x = itemBounds.x + itemBounds.width;
        int y = itemBounds.y + itemBounds.height;
        return new Point(x, y);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // The remainder of the methods in this class redirect directly to
    // CTabFolder methods

    public void setSelection(int selection) {
        tabFolder.setSelection(selection);
    }

    /**
     * @param i
     * @param j
     * @param k
     * @param l
     * @return
     */
    public Rectangle computeTrim(int i, int j, int k, int l) {
        return tabFolder.computeTrim(i, j, k, l);
    }

    /**
     * @param b
     */
    public void setUnselectedCloseVisible(boolean b) {
        tabFolder.setUnselectedCloseVisible(b);
    }

    /**
     * @param fgColor
     */
    public void setSelectionForeground(Color fgColor) {
        tabFolder.setSelectionForeground(fgColor);
    }

    /**
	 * Set the selection gradient with the activation state.
     * @param bgColors
     * @param percentages
     * @param vertical
     */
    public void setSelectionBackground(Color[] bgColors, int[] percentages,
            boolean vertical) {
        tabFolder.setSelectionBackground(bgColors, percentages, vertical);
    }

    public CTabItem getItem(int idx) {
        return tabFolder.getItem(idx);
    }

    public int getSelectionIndex() {
        return tabFolder.getSelectionIndex();
    }

    public int getTabHeight() {
        return tabFolder.getTabHeight();
    }

    public int indexOf(CTabItem toFind) {
        return tabFolder.indexOf(toFind);
    }

    public void setTabHeight(int height) {
        tabFolder.setTabHeight(height);
    }

    /**
     * @return
     */
    public int getItemCount() {
        return tabFolder.getItemCount();
    }

    /**
     * @return
     */
    public CTabItem[] getItems() {
        return tabFolder.getItems();
    }

    public CTabItem getItem(Point toGet) {
        return tabFolder.getItem(toGet);
    }

    public CTabItem getSelection() {
        return tabFolder.getSelection();
    }

    /**
     * @param isVisible
     */
    public void setMinimizeVisible(boolean isVisible) {
        tabFolder.setMinimizeVisible(isVisible);
        minimizeVisible = isVisible;
    }

    /**
     * Changes the minimum number of characters to display in a pane folder tab.
     * This control how much information will be displayed to the user.
     * 
     * @param count
     *            The number of characters to display in the tab folder; this
     *            value should be a positive integer.
     * @see org.eclipse.swt.custom.CTabFolder#setMinimumCharacters(int)
     * @since 3.1
     */
    public void setMinimumCharacters(int count) {
        tabFolder.setMinimumCharacters(count);
    }

    /**
     * @param isVisible
     */
    public void setMaximizeVisible(boolean isVisible) {
        tabFolder.setMaximizeVisible(isVisible);
        maximizeVisible = isVisible;
    }

    /**
     * @param traditionalTab
     */
    public void setSimpleTab(boolean traditionalTab) {
        tabFolder.setSimple(traditionalTab);
    }

    /**
     * @param b
     */
    public void setUnselectedImageVisible(boolean b) {
        tabFolder.setUnselectedImageVisible(b);
    }

    /**
     * @param b
     */
    public void setSingleTab(boolean b) {
        tabFolder.setSingle(b);
    }

    public void hideTitle() {
        tabFolder.setTabHeight(0);
    }
    
    public ViewForm getViewForm() {
        return viewForm;
    }

    /**
	 * Propogate the visibility change requests to the proxy controls. When
	 * their target is null, they no longer get visibility updates. Currently
	 * this only propagates the changes to the ProxyControls held by this
	 * folder.
	 * 
	 * @param visible
	 *            <code>true</code> - it's visible.
	 * @since 3.2
	 */
    public void setVisible(boolean visible) {
		contentProxy.setVisible(visible);
		viewFormTopCenterProxy.setVisible(visible);
		viewFormTopLeftProxy.setVisible(visible);
		viewFormTopRightProxy.setVisible(visible);
	}
}
