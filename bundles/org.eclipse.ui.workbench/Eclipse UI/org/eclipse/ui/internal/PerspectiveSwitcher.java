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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.layout.CacheWrapper;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * A utility class to manage the perspective switcher.  At some point, it might be nice to
 * move all this into PerspectiveViewBar.
 * 
 * @since 3.0
 */
public class PerspectiveSwitcher {

    private WorkbenchWindow window;
    private CBanner topBar;
    private int style;

	private PerspectiveBarManager perspectiveBar;
	private Menu popupMenu;
	private Menu genericMenu;
	private CoolBar perspectiveCoolBar;
	private CacheWrapper perspectiveCoolBarWrapper;

	private int location = convertLocation(PrefUtil.getAPIPreferenceStore().getString(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR));
	private static final int TOP_RIGHT = 1;
	private static final int TOP_LEFT = 2;
	private static final int LEFT = 3;

	public PerspectiveSwitcher(WorkbenchWindow window, CBanner topBar, int style) {
	    this.window = window;
	    this.topBar = topBar;
	    this.style = style;

		perspectiveBar = new PerspectiveBarManager(style);
		perspectiveBar.add(new PerspectiveBarNewContributionItem(window));	
	}

	private int convertLocation(String preference) {
	    if (IWorkbenchPreferenceConstants.TOP_RIGHT.equals(preference))
	        return TOP_RIGHT;
	    if (IWorkbenchPreferenceConstants.TOP_LEFT.equals(preference))
	        return TOP_LEFT;
	    if (IWorkbenchPreferenceConstants.LEFT.equals(preference))
	        return LEFT;

	    // TODO log the unknown preference
	    return TOP_RIGHT;
	}

	public void createControl(Composite parent) {
	    createPerspectiveBar();
	}

	public void addPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage workbenchPage) {
	    if (perspectiveBar != null) {
			perspectiveBar.add(new PerspectiveBarContributionItem(perspective, workbenchPage));
			perspectiveBar.update(false);
			if (perspectiveBar.getControl() != null) {
			    LayoutUtil.resize(perspectiveBar.getControl());
			}
	    }
	}

	public IContributionItem findPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page) {
	    if (perspectiveBar == null)
	        return null;

		IContributionItem[] array = perspectiveBar.getItems();
		int length = array.length;
		for (int i = 0; i < length; i++) {
			IContributionItem item = array[i];
						
			if (item instanceof PerspectiveBarContributionItem)
				if (((PerspectiveBarContributionItem) item).handles(perspective, page))
					return item;
		}
		return null;
	}

	public void removePerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null) {
			perspectiveBar.remove(item);
			perspectiveBar.update(false);
			topBar.layout(true);
		}
	}

	public void setPerspectiveBarLocation(String preference) {
	    int newLocation = convertLocation(preference);
	    if (newLocation == location)
	        return;

	    location = newLocation;
	    createPerspectiveBar();
	}

 	private void createPerspectiveBar() {
		if (perspectiveBar.getControl() == null)
		    createControl();

 		if (location == TOP_LEFT) {
 			topBar.setRight(null);
			topBar.setBottom(perspectiveCoolBarWrapper.getControl());
 		} else {
			topBar.setBottom(null);
			topBar.setRight(perspectiveCoolBarWrapper.getControl());
 			topBar.setRightWidth(150);
 		}

 		LayoutUtil.resize(perspectiveBar.getControl());		
 	}

 	public void update(boolean force) {
		if (perspectiveBar != null)
		    perspectiveBar.update(force);
 	}

	public void selectPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page, boolean selected) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null)
			((PerspectiveBarContributionItem) item).setSelection(selected);
	}

	public void updatePerspectiveShortcut(IPerspectiveDescriptor oldDesc, IPerspectiveDescriptor newDesc, WorkbenchPage page) {
		IContributionItem item = findPerspectiveShortcut(oldDesc, page);
		if (item != null && (item instanceof PerspectiveBarContributionItem))
			((PerspectiveBarContributionItem)item).update(newDesc);
	}

	public PerspectiveBarManager getPerspectiveBar() {
		return perspectiveBar;
	}

 	private void createControl() {
		perspectiveCoolBarWrapper = new CacheWrapper(topBar);
		perspectiveCoolBar = new CoolBar(perspectiveCoolBarWrapper.getControl(), SWT.FLAT);
		final CoolItem coolItem = new CoolItem(perspectiveCoolBar, SWT.DROP_DOWN);
		final CacheWrapper toolbarWrapper = new CacheWrapper(perspectiveCoolBar); 
		perspectiveBar.createControl(toolbarWrapper.getControl());
		coolItem.setControl(toolbarWrapper.getControl());
		perspectiveCoolBar.setLocked(true);
		perspectiveBar.setParent(perspectiveCoolBar);
		perspectiveBar.update(true);

		// adjust the toolbar size to display as many items as possible
		perspectiveCoolBar.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				
				// Would it be possible to fit the toolbar in this space if we wrapped it? 
				Rectangle area = perspectiveCoolBar.getClientArea();

				// Determine the difference in size betwen the coolitem's client area and the coolbar's bounds
				Point offset = coolItem.computeSize(0,0);
				
				Point wrappedSize = toolbarWrapper.getControl().computeSize(area.width - offset.x, SWT.DEFAULT);

				// If everything will fit, set it to the wrapped size
				if (wrappedSize.y <= area.height - offset.y) { 
					coolItem.setSize(wrappedSize.x + offset.x, wrappedSize.y + offset.y);
					return;
				}
				
				// Set the cool item to be 1 pixel larger than the coolbar, in order to force a chevron
				// to appear
				coolItem.setSize(wrappedSize.x + offset.x + 1, area.height + offset.y);
				}

		});
		
		coolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.ARROW) {
					perspectiveBar.handleChevron(e);
				}
			}
		});
		coolItem.setMinimumSize(0, 0);
		
	    // TODO: encapsulate the listener and showPerspectiveBarPopup in PerspectiveBar
	    Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MenuDetect) {
					showPerspectiveBarPopup(new Point(event.x, event.y));
				}
			}
		};
		perspectiveBar.getControl().addListener(SWT.MenuDetect, listener);
 	}

	private void showPerspectiveBarPopup(Point pt) {
		// Get the tool item under the mouse.
		ToolBar toolBar = perspectiveBar.getControl();
		ToolItem toolItem = toolBar.getItem(toolBar.toControl(pt));

		// Get the action for the tool item.
		Object data = null;
		if (toolItem != null)
		    data = toolItem.getData();

		if (toolItem == null || !(data instanceof PerspectiveBarContributionItem)) {
            if (location != TOP_RIGHT && location != TOP_LEFT)
                return;

			if (genericMenu == null) {
				Menu menu = new Menu(toolBar);
				final MenuItem dockMenuItem = new MenuItem(menu, SWT.CHECK);
				dockMenuItem.setText(WorkbenchMessages.getString("PerspectiveBar.dockLeft")); //$NON-NLS-1$

				dockMenuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String preference = dockMenuItem.getSelection() ? IWorkbenchPreferenceConstants.TOP_LEFT
                                : IWorkbenchPreferenceConstants.TOP_RIGHT;
                        PrefUtil.getAPIPreferenceStore()
                                .setValue(
                                        IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
                                        preference);
					}
				});
				genericMenu = menu;
			}

			// get the dock menu item and update the state to ensure it matches the preference
			genericMenu.getItem(0).setSelection(location == TOP_LEFT);

			// Show popup menu.
			if (genericMenu != null) {
			    genericMenu.setLocation(pt.x, pt.y);
			    genericMenu.setVisible(true);
			}
			return;
		}

		if (data == null || !(data instanceof PerspectiveBarContributionItem))
			return;

		// The perspective bar menu is created lazily here.
		// Its data is set (each time) to the tool item, which refers to the SetPagePerspectiveAction
		// which in turn refers to the page and perspective.
		// It is important not to refer to the action, the page or the perspective directly
		// since otherwise the menu hangs on to them after they are closed.
		// By hanging onto the tool item instead, these references are cleared when the
		// corresponding page or perspective is closed.
		// See bug 11282 for more details on why it is done this way.
		if (popupMenu == null) {
			Menu menu = new Menu(toolBar);
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ToolItem perspectiveToolItem = (ToolItem) popupMenu.getData();
					if (perspectiveToolItem != null && !perspectiveToolItem.isDisposed()) {
						PerspectiveBarContributionItem item =
							(PerspectiveBarContributionItem) perspectiveToolItem.getData();
						item.getPage().closePerspective(item.getPerspective(), true);
					}
				}
			});
			menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.closeAll")); //$NON-NLS-1$
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ToolItem perspectiveToolItem = (ToolItem) popupMenu.getData();
					if (perspectiveToolItem != null && !perspectiveToolItem.isDisposed()) {
						PerspectiveBarContributionItem item =
							(PerspectiveBarContributionItem) perspectiveToolItem.getData();
						item.getPage().closeAllPerspectives();
					}
				}
			});
			popupMenu = menu;
		}
		popupMenu.setData(toolItem);

		// Show popup menu.
		if (popupMenu != null) {
			popupMenu.setLocation(pt.x, pt.y);
			popupMenu.setVisible(true);
		}
	}
}
