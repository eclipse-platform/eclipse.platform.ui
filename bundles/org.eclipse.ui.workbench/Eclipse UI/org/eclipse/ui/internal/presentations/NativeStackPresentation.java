/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * A stack presentation using native widgets.
 * <p>
 * EXPERIMENTAL
 * </p> 
 * 
 * @since 3.0
 */
public class NativeStackPresentation extends StackPresentation {

    private TabFolder tabFolder;

    private Listener dragListener;

    private IPresentablePart current;

    private MenuManager systemMenuManager = new MenuManager();

    private static IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
            .getPreferenceStore();

	// don't reset this dynamically, so just keep the information static.
	// see bug:
	//   75422 [Presentations] Switching presentation to R21 switches immediately, but only partially
    private static int tabPos = preferenceStore.getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);
  
    private final static String TAB_DATA = NativeStackPresentation.class
            .getName()
            + ".partId"; //$NON-NLS-1$

    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseDown(MouseEvent e) {
            //			// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
            //			// Else, set focus.
            //			TabItem newItem = tabFolder.getItem(new Point(e.x, e.y));
            //			if (newItem != null) {
            //				TabItem oldItem = tabFolder.getSelection();
            //				if (newItem != oldItem)
            //					return;
            //			}
            if (current != null) {
                current.setFocus();
            }
        }
    };

    private Listener menuListener = new Listener() {
        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event) {
            Point pos = new Point(event.x, event.y);
            //			TabItem item = tabFolder.getItem(pos);
            TabItem item = null;
            IPresentablePart part = null;
            if (item != null) {
                part = getPartForTab(item);
            }
            showPaneMenu(part, pos);
        }
    };

    private Listener selectionListener = new Listener() {
        public void handleEvent(Event e) {
            IPresentablePart item = getPartForTab((TabItem) e.item);
            if (item != null) {
                getSite().selectPart(item);
                //				item.setFocus();
            }
        }
    };

    private Listener resizeListener = new Listener() {
        public void handleEvent(Event e) {
            setControlSize();
        }
    };

    private IPropertyListener childPropertyChangeListener = new IPropertyListener() {
        public void propertyChanged(Object source, int property) {

            if (isDisposed()) {
				return;
			}

            if (source instanceof IPresentablePart) {
                IPresentablePart part = (IPresentablePart) source;
                childPropertyChanged(part, property);
            }
        }
    };

    private DisposeListener tabDisposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
            if (e.widget instanceof TabItem) {
                TabItem item = (TabItem) e.widget;
                IPresentablePart part = getPartForTab(item);
                part.removePropertyListener(childPropertyChangeListener);
            }
        }
    };

    public NativeStackPresentation(Composite parent,
            IStackPresentationSite stackSite) {
        super(stackSite);

        tabFolder = new TabFolder(parent, tabPos);

        // listener to switch between visible tabItems
        tabFolder.addListener(SWT.Selection, selectionListener);

        // listener to resize visible components
        tabFolder.addListener(SWT.Resize, resizeListener);

        // listen for mouse down on tab to set focus.
        tabFolder.addMouseListener(mouseListener);

        tabFolder.addListener(SWT.MenuDetect, menuListener);

        dragListener = new Listener() {
            public void handleEvent(Event event) {
                Point localPos = new Point(event.x, event.y);
                //				TabItem tabUnderPointer = tabFolder.getItem(localPos);
                TabItem tabUnderPointer = null;

                if (tabUnderPointer == null) {
                    return;
                }

                IPresentablePart part = getPartForTab(tabUnderPointer);

                if (getSite().isPartMoveable(part)) {
                    getSite().dragStart(part, tabFolder.toDisplay(localPos),
                            false);
                }
            }
        };

        PresentationUtil.addDragListener(tabFolder, dragListener);

    }

    /**
     * Returns the index of the tab for the given part, or returns tabFolder.getItemCount()
     * if there is no such tab.
     * 
     * @param part part being searched for
     * @return the index of the tab for the given part, or the number of tabs
     * if there is no such tab
     */
    private final int indexOf(IPresentablePart part) {
        if (part == null) {
            return tabFolder.getItemCount();
        }

        TabItem[] items = tabFolder.getItems();

        for (int idx = 0; idx < items.length; idx++) {
            IPresentablePart tabPart = getPartForTab(items[idx]);

            if (part == tabPart) {
                return idx;
            }
        }

        return items.length;
    }

    /**
     * Returns the tab for the given part, or null if there is no such tab
     * 
     * @param part the part being searched for
     * @return the tab for the given part, or null if there is no such tab
     */
    protected final TabItem getTab(IPresentablePart part) {
        TabItem[] items = tabFolder.getItems();

        int idx = indexOf(part);

        if (idx < items.length) {
            return items[idx];
        }

        return null;
    }

    /**
     * @param part
     * @param property
     */
    protected void childPropertyChanged(IPresentablePart part, int property) {
        TabItem tab = getTab(part);
        initTab(tab, part);
    }

    protected final IPresentablePart getPartForTab(TabItem item) {
        IPresentablePart part = (IPresentablePart) item.getData(TAB_DATA);
        return part;
    }

    protected TabFolder getTabFolder() {
        return tabFolder;
    }

    public boolean isDisposed() {
        return tabFolder == null || tabFolder.isDisposed();
    }

    /**
     * Set the size of a page in the folder.
     */
    private void setControlSize() {
        if (current == null || tabFolder == null) {
			return;
		}
        //		Rectangle bounds;
        // @issue as above, the mere presence of a theme should not change the behaviour
        //		if ((mapTabToPart.size() > 1)
        //			|| ((tabThemeDescriptor != null) && (mapTabToPart.size() >= 1)))
        //			bounds = calculatePageBounds(tabFolder);
        //		else
        //			bounds = tabFolder.getBounds();
        current.setBounds(calculatePageBounds(tabFolder));
        //current.moveAbove(tabFolder);
    }

    public static Rectangle calculatePageBounds(TabFolder folder) {
        if (folder == null) {
			return new Rectangle(0, 0, 0, 0);
		}
        Rectangle bounds = folder.getBounds();
        Rectangle offset = folder.getClientArea();
        bounds.x += offset.x;
        bounds.y += offset.y;
        bounds.width = offset.width;
        bounds.height = offset.height;
        return bounds;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#dispose()
     */
    public void dispose() {
        if (isDisposed()) {
            return;
        }
        PresentationUtil.removeDragListener(tabFolder, dragListener);

        //systemMenuManager.dispose();

        tabFolder.dispose();
        tabFolder = null;
    }

    private TabItem createPartTab(IPresentablePart part, int tabIndex) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE, tabIndex);
        tabItem.setData(TAB_DATA, part);
        part.addPropertyListener(childPropertyChangeListener);
        tabItem.addDisposeListener(tabDisposeListener);
        initTab(tabItem, part);
        return tabItem;
    }

    /**
     * Initializes a tab for the given part. Sets the text, icon, tool tip,
     * etc. This will also be called whenever a relevant property changes
     * in the part to reflect those changes in the tab. Subclasses may override
     * to change the appearance of tabs for a particular part.
     * 
     * @param tabItem tab for the part
     * @param part the part being displayed
     */
    protected void initTab(TabItem tabItem, IPresentablePart part) {
        tabItem.setText(part.getName());
        tabItem.setToolTipText(part.getTitleToolTip());

        Image tabImage = part.getTitleImage();
        if (tabImage != tabItem.getImage()) {
            tabItem.setImage(tabImage);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.StackPresentation#addPart(org.eclipse.ui.internal.skins.IPresentablePart, org.eclipse.ui.internal.skins.IPresentablePart)
     */
    public void addPart(IPresentablePart newPart, Object cookie) {
        createPartTab(newPart, tabFolder.getItemCount());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.StackPresentation#removePart(org.eclipse.ui.internal.skins.IPresentablePart)
     */
    public void removePart(IPresentablePart oldPart) {
        TabItem item = getTab(oldPart);
        if (item == null) {
            return;
        }
        oldPart.setVisible(false);

        item.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.StackPresentation#selectPart(org.eclipse.ui.internal.skins.IPresentablePart)
     */
    public void selectPart(IPresentablePart toSelect) {
        if (toSelect == current) {
            return;
        }

        if (current != null) {
            current.setVisible(false);
        }

        current = toSelect;

        if (current != null) {
            tabFolder.setSelection(indexOf(current));
            current.setVisible(true);
            setControlSize();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#setBounds(org.eclipse.swt.graphics.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
        tabFolder.setBounds(bounds);
        setControlSize();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#computeMinimumSize()
     */
    public Point computeMinimumSize() {
        return Geometry.getSize(tabFolder.computeTrim(0, 0, 0, 0));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        if (current != null) {
            current.setVisible(isVisible);
        }
        tabFolder.setVisible(isVisible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
     */
    public void setState(int state) {
        //		tabFolder.setMinimized(state == IPresentationSite.STATE_MINIMIZED);
        //		tabFolder.setMaximized(state == IPresentationSite.STATE_MAXIMIZED);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#getSystemMenuManager()
     */
    public IMenuManager getSystemMenuManager() {
        return systemMenuManager;
    }

    /**
     * @param part
     * @param point
     */
    protected void showPaneMenu(IPresentablePart part, Point point) {
        systemMenuManager.update(false);
        Menu aMenu = systemMenuManager.createContextMenu(tabFolder.getParent());
        aMenu.setLocation(point.x, point.y);
        aMenu.setVisible(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#getControl()
     */
    public Control getControl() {
        return tabFolder;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
     */
    public StackDropResult dragOver(Control currentControl, Point location) {

        // Determine which tab we're currently dragging over
        //		Point localPos = tabFolder.toControl(location);
        //		final TabItem tabUnderPointer = tabFolder.getItem(localPos);
        final TabItem tabUnderPointer = null;

        // This drop target only deals with tabs... if we're not dragging over
        // a tab, exit.
        if (tabUnderPointer == null) {
            return null;
        }

        //		return new StackDropResult(Geometry.toDisplay(tabFolder, tabUnderPointer.getBounds()),
        //			tabFolder.indexOf(tabUnderPointer));
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
     */
    public void showSystemMenu() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#showPaneMenu()
     */
    public void showPaneMenu() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#getTabList(IPresentablePart)
     */
    public Control[] getTabList(IPresentablePart part) {
        ArrayList list = new ArrayList();
        if (getControl() != null) {
			list.add(getControl());
		}
        if (part.getToolBar() != null) {
			list.add(part.getToolBar());
		}
        if (part.getControl() != null) {
			list.add(part.getControl());
		}
        return (Control[]) list.toArray(new Control[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#getCurrentPart()
     */
    public IPresentablePart getCurrentPart() {
        return current;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
     */
    public void setActive(int newState) {

    }

}
