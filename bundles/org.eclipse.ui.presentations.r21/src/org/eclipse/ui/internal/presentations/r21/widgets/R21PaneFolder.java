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
package org.eclipse.ui.internal.presentations.r21.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.layout.SizeCache;
import org.eclipse.ui.internal.presentations.util.ProxyControl;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This class implements the tab folders that contains can contain two toolbars and
 * status text. Wherever possible, the toolbars are aligned with the tabs. 
 * If there is not enough room beside the tabs, the toolbars are aligned with the status text. This
 * is the same tab folder that is used to arrange views and editors in Eclipse. 
 * <p>
 * This is closely related to DefaultPartPresentation, but they have different responsibilities. This
 * is essentially a CTabFolder that can manage a toolbar. It should not depend on 
 * data structures from the workbench, and its public interface should only use SWT objects or
 * listeners. DefaultPartPresentation uses a PaneFolder to arrange views or editors. Knowledge
 * of higher-level data structures should go there. 
 * </p>
 * <p>
 * Although it is not actually a control, the public interface is much like 
 * an SWT control. Implementation-wise, this is actually a combination of a CTabFolder and 
 * a ViewForm. It encapsulates the details of moving the toolbar between the CTabFolder and
 * the ViewForm, and provides a simpler interface to the ViewForm/CTabFolder. 
 * </p>
 * 
 * @since 3.0
 */
public final class R21PaneFolder {
    // Tab folder and associated proxy controls
    private CTabFolder tabFolder;

    //	private Control titleAreaProxy;	

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

    private int tabPos;

    private boolean putTrimOnTop = false;

    /**
     * List of PaneFolderButtonListener
     */
    private List buttonListeners = new ArrayList(1);

    private int state = IStackPresentationSite.STATE_RESTORED;

    /**
     * State of the folder at the last mousedown event. This is used to prevent
     * a mouseup over the minimize or maximize buttons from undoing a state change 
     * that was caused by the mousedown.
     */
    private int mousedownState = -1;

    //	// CTabFolder listener
    //	private CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
    //		public void minimize(CTabFolderEvent event) {
    //			event.doit = false;
    //			notifyButtonListeners(IStackPresentationSite.STATE_MINIMIZED);
    //		}
    //		
    //		public void restore(CTabFolderEvent event) {
    //			event.doit = false;
    //			notifyButtonListeners(IStackPresentationSite.STATE_RESTORED);
    //		}
    //		
    //		public void maximize(CTabFolderEvent event) {
    //			event.doit = false;
    //			notifyButtonListeners(IStackPresentationSite.STATE_MAXIMIZED);
    //		}
    //		
    //		/* (non-Javadoc)
    //		 * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
    //		 */
    //		public void close(CTabFolderEvent event) {
    //			event.doit = false;
    //			notifyCloseListeners((CTabItem)event.item);
    //		}
    //		
    //		public void showList(CTabFolderEvent event) {
    //			notifyShowListeners(event);
    //		}
    //		
    //	};
    //	
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseDown(MouseEvent e) {
            mousedownState = getState();
        }

        public void mouseDoubleClick(MouseEvent e) {
        }
    };

    /**
     * Creates a pane folder. This will create exactly one child control in the
     * given parent.
     * 
     * @param parent
     * @param flags
     */
    public R21PaneFolder(Composite parent, int flags) {
        // Initialize tab folder
        {
            tabFolder = new CTabFolder(parent, flags);

            //			// Create a proxy control to measure the title area of the tab folder
            //			titleAreaProxy = new Composite(tabFolder, SWT.NONE);
            //			titleAreaProxy.setVisible(false);
            //			tabFolder.setTopRight(titleAreaProxy, SWT.FILL);

            //			tabFolder.addCTabFolder2Listener(expandListener);
            //			
            tabFolder.addMouseListener(mouseListener);
        }

        // Initialize view form
        {
            viewForm = new ViewForm(tabFolder, SWT.NONE);

            // Only attach these to the viewForm when there's actuall a control to display
            viewFormTopLeftProxy = new ProxyControl(viewForm);
            viewFormTopCenterProxy = new ProxyControl(viewForm);
            viewFormTopRightProxy = new ProxyControl(viewForm);

            contentProxy = new ProxyControl(viewForm);
            viewForm.setContent(contentProxy.getControl());
        }
    }

    /**
     * Return the main control for this pane folder
     * 
     * @return Composite the control
     */
    public Composite getControl() {
        return tabFolder;
    }

    /**
     * Sets the top-center control (usually a toolbar), or null if none.
     * Note that the control can have any parent.
     * 
     * @param topCenter the top-center control or null if none
     */
    public void setTopCenter(Control topCenter) {
        topCenterCache.setControl(topCenter);
        if (topCenter != null) {
            if (!putTrimOnTop) {
                viewFormTopCenterProxy.setTarget(topCenterCache);
                viewForm.setTopCenter(viewFormTopCenterProxy.getControl());
            }
        } else {
            if (!putTrimOnTop) {
                viewForm.setTopCenter(null);
            }
        }
    }

    /**
     * Sets the top-right control (usually a dropdown), or null if none
     * 
     * @param topRight
     */
    public void setTopRight(Control topRight) {
        topRightCache.setControl(topRight);
        if (topRight != null) {
            if (!putTrimOnTop) {
                viewFormTopRightProxy.setTarget(topRightCache);
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
        if (topLeftCache.getControl() != topLeft) {
            topLeftCache.setControl(topLeft);
            // The top-left control always goes directly in the ViewForm
            if (topLeft != null) {
                viewFormTopLeftProxy.setTarget(topLeftCache);
                viewForm.setTopLeft(viewFormTopLeftProxy.getControl());
            } else {
                viewFormTopLeftProxy.setTargetControl(null);
                viewForm.setTopLeft(null);
            }
        }
    }

    /**
     * Flush all of this folder's size caches to ensure they will be re-computed
     * on the next layout.
     */
    public void flush() {
        topLeftCache.flush();
        topRightCache.flush();
        topCenterCache.flush();
    }

    /**
     * Layout the receiver, flusing the cache if needed.
     * 
     * @param flushCache
     */
    public void layout(boolean flushCache) {
        // Flush the cached sizes if necessary
        if (flushCache) {
			flush();
		}

        Rectangle tabFolderClientArea = tabFolder.getClientArea();

        // Hide tabs if there is only one
        if (tabFolder.getItemCount() < 2) {
            //Rectangle tabFolderBounds = tabFolder.getBounds();

            int delta = getTabHeight() + 1;
            tabFolderClientArea.height += delta;

            if (getTabPosition() == SWT.TOP) {
                tabFolderClientArea.y -= delta;
            }
        }

        viewForm.setBounds(tabFolderClientArea);
        viewFormTopRightProxy.layout();
        viewFormTopLeftProxy.layout();
        viewFormTopCenterProxy.layout();
    }

    /**
     * Returns the client area for this PaneFolder, relative to the pane folder's control.
     * 
     * @return Rectangle the client area 
     */
    public Rectangle getClientArea() {
        Rectangle bounds = contentProxy.getControl().getBounds();

        Rectangle formArea = viewForm.getBounds();

        bounds.x += formArea.x;
        bounds.y += formArea.y;

        return bounds;
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
     * @param buttonId one of the IStackPresentationSite.STATE_* constants
     */
    protected void notifyButtonListeners(int buttonId) {
        if (mousedownState == getState()) {
            Iterator iter = buttonListeners.iterator();

            while (iter.hasNext()) {
                R21PaneFolderButtonListener listener = (R21PaneFolderButtonListener) iter
                        .next();

                listener.stateButtonPressed(buttonId);
            }
        }
    }

    /**
     * Notifies all listeners that the user clicked on the chevron
     * 
     * @param event
     */
    protected void notifyShowListeners(CTabFolderEvent event) {
        Iterator iter = buttonListeners.iterator();

        while (iter.hasNext()) {
            R21PaneFolderButtonListener listener = (R21PaneFolderButtonListener) iter
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
            R21PaneFolderButtonListener listener = (R21PaneFolderButtonListener) iter
                    .next();

            listener.closeButtonPressed(tabItem);
        }
    }

    /**
     * @param listener
     */
    public void addButtonListener(R21PaneFolderButtonListener listener) {
        buttonListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeButtonListener(R21PaneFolderButtonListener listener) {
        buttonListeners.remove(listener);
    }

    /**
     * @param newTabPosition
     */
    public void setTabPosition(int newTabPosition) {
        tabPos = newTabPosition;
        tabFolder.setTabPosition(tabPos);
    }

    /**
     * @return int the postion of the tab
     */
    public int getTabPosition() {
        return tabPos;
    }

    /**
     * @return boolean <code>true</code> if the receiver has been disposed
     */
    public boolean isDisposed() {
        return tabFolder == null || tabFolder.isDisposed();
    }

    /**
     * @param style
     * @param index
     * @return CTabItem the created item
     */
    public CTabItem createItem(int style, int index) {
        return new CTabItem(tabFolder, style, index);
    }

    // The remainder of the methods in this class redirect directly to CTabFolder methods

    /**
     * @param selection
     */
    public void setSelection(int selection) {
        tabFolder.setSelection(selection);
    }

    /**
     * @param i
     * @param j
     * @param k
     * @param l
     * @return Rectangle the trim rectangle
     */
    public Rectangle computeTrim(int i, int j, int k, int l) {
        return tabFolder.computeTrim(i, j, k, l);
    }

    /**
     * @param fgColor
     */
    public void setSelectionForeground(Color fgColor) {
        tabFolder.setSelectionForeground(fgColor);
    }

    /**
     * @param idx
     * @return CTabItem the indexed item
     */
    public CTabItem getItem(int idx) {
        return tabFolder.getItem(idx);
    }

    /**
     * @return int the selected items index
     */
    public int getSelectionIndex() {
        return tabFolder.getSelectionIndex();
    }

    /**
     * @return int the height of the tabs
     */
    public int getTabHeight() {
        return tabFolder.getTabHeight();
    }

    /**
     * @param toFind
     * @return int the index of the item to find
     */
    public int indexOf(CTabItem toFind) {
        return tabFolder.indexOf(toFind);
    }

    /**
     * @param height
     */
    public void setTabHeight(int height) {
        tabFolder.setTabHeight(height);
    }

    /**
     * @return int the item count
     */
    public int getItemCount() {
        return tabFolder.getItemCount();
    }

    /**
     * @return CTabItem the items
     */
    public CTabItem[] getItems() {
        return tabFolder.getItems();
    }

    /**
     * @param toGet
     * @return CTabItem the indexed item
     */
    public CTabItem getItem(Point toGet) {
        return tabFolder.getItem(toGet);
    }

    /**
     * @return CTabItem the selected item
     */
    public CTabItem getSelection() {
        return tabFolder.getSelection();
    }
}
