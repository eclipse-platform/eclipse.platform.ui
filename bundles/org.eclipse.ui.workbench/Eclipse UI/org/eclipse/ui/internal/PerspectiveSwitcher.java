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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.layout.CacheWrapper;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.Row;
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

    private Composite parent;
    private Composite trimControl;
    private Label trimSeparator;
    private GridData trimLayoutData;
    private boolean trimVisible = false;
	private int trimOldLength = 0;

	private PerspectiveBarManager perspectiveBar;
	private CoolBar perspectiveCoolBar;
	private CacheWrapper perspectiveCoolBarWrapper;

	// The menus are cached, so the radio buttons should not be disposed until
	// the switcher is disposed.
	private Menu popupMenu;
	private Menu genericMenu;
	private List radioButtons = new ArrayList(4);

	private IntModel location;
	private int oldLocation = INITIAL;
	private static final int INITIAL = 0;
	private static final int TOP_RIGHT = 1;
	private static final int TOP_LEFT = 2;
	private static final int LEFT = 3;
	private static final int SEPARATOR_LENGTH = 20;
	
	private Listener popupListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.MenuDetect) {
				showPerspectiveBarPopup(new Point(event.x, event.y));
			}
		}
	};

	public PerspectiveSwitcher(WorkbenchWindow window, CBanner topBar, int style) {
	    this.window = window;
	    this.topBar = topBar;
	    this.style = style;

	    location = new IntModel(convertLocation(PrefUtil
                .getAPIPreferenceStore().getString(
                        IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR)));
	}

	private static int convertLocation(String preference) {
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
	    Assert.isTrue(this.parent == null);
	    this.parent = parent;

	    location.addChangeListener(new IChangeListener() {
            public void update(boolean changed) {
                createControlForLocation();
                oldLocation = location.get();
                showPerspectiveBar();
            }
        });
	}

	public void addPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage workbenchPage) {
	    if (perspectiveBar == null)
	        return;

		perspectiveBar.add(new PerspectiveBarContributionItem(perspective, workbenchPage));
		perspectiveBar.update(false);
		if (perspectiveBar.getControl() != null)
		    LayoutUtil.resize(perspectiveBar.getControl());
	}

	public IContributionItem findPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page) {
	    if (perspectiveBar == null)
	        return null;

		IContributionItem[] items = perspectiveBar.getItems();
		int length = items.length;
		for (int i = 0; i < length; i++) {
            IContributionItem item = items[i];
            if (item instanceof PerspectiveBarContributionItem
                    && ((PerspectiveBarContributionItem) item).handles(perspective, page))
                return item;
        }
		return null;
	}

	public void removePerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page) {
	    if (perspectiveBar == null)
	        return;

	    IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null) {
			perspectiveBar.remove(item);
			perspectiveBar.update(false);

			if (location.get() == TOP_RIGHT || location.get() == TOP_LEFT)
			    topBar.layout(true);
		}
	}

	public void setPerspectiveBarLocation(String preference) {
	    int newLocation = convertLocation(preference);
	    if (newLocation == oldLocation)
	        return;

	    oldLocation = location.get();
	    location.set(newLocation);
	}

	/**
	 * Make the perspective bar visible in its current location.  This method should not
	 * be used unless the control has been successfully created. 
	 */
 	private void showPerspectiveBar() {
 	    switch(location.get())
 	    {
 	    case TOP_LEFT:
 			topBar.setRight(null);
			topBar.setBottom(perspectiveCoolBarWrapper.getControl());
			break;
	    case TOP_RIGHT:
			topBar.setBottom(null);
			topBar.setRight(perspectiveCoolBarWrapper.getControl());
 			topBar.setRightWidth(150);
 			break;
 	    case LEFT:
			topBar.setBottom(null);
			topBar.setRight(null);
 	        LayoutUtil.resize(topBar);
 	        window.addPerspectiveBarToTrim(trimControl, SWT.LEFT);
 	        break;
 	    default:
 	        // TODO log?
 	        return;
 		}

		LayoutUtil.resize(perspectiveBar.getControl());
 	}

 	public void update(boolean force) {
 	    if (perspectiveBar == null)
 	        return;

	    perspectiveBar.update(force);

		if (location.get() == LEFT) {
			ToolItem[] items = perspectiveBar.getControl().getItems();
			boolean shouldExpand = items.length > 0;
			if (shouldExpand != trimVisible) {
			    perspectiveBar.getControl().setVisible(true);
				trimVisible = shouldExpand;
			}

			if (items.length != trimOldLength) {
				LayoutUtil.resize(trimControl);
				trimOldLength = items.length;
			}
		}
 	}

	public void selectPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page, boolean selected) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null && (item instanceof PerspectiveBarContributionItem))
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

	public void dispose() {
	    disposeChildControls();

	    Iterator i = radioButtons.iterator();
	    while (i.hasNext())
	        ((RadioMenu)i.next()).dispose();
	}

	private void disposeChildControls() {
	    if (perspectiveBar != null) {
	        perspectiveBar.dispose();
	        perspectiveBar = null;
	    }

	    if (trimControl != null) {
	        trimControl.dispose();
	        trimControl = null;
	    }

	    if (trimSeparator != null) {
	        trimSeparator.dispose();
	        trimSeparator = null;
	    }

	    if (perspectiveCoolBar != null) {
	        perspectiveCoolBar.dispose();
	        perspectiveCoolBar = null;
	    }

		perspectiveCoolBarWrapper = null;
	}

	/**
	 * Ensures the control has been set for the argument location.  If the control
	 * already exists and can be used the argument location, nothing happens.  Updates
	 * the location attribute.
	 * @param newLocation
	 */
	private void createControlForLocation() {
	    // if there is a control, then perhaps it can be reused
		if (perspectiveBar != null && perspectiveBar.getControl() != null
                && !perspectiveBar.getControl().isDisposed()) {
            if (oldLocation == LEFT && location.get() == LEFT)
                return;
            if ((oldLocation == TOP_LEFT || oldLocation == TOP_RIGHT)
                    && (location.get() == TOP_LEFT || location.get() == TOP_RIGHT))
                return;
        }

		// otherwise dispose the current controls and make new ones
		disposeChildControls();
	    if (location.get() == LEFT)
		    createControlForLeft();
		else
	        createControlForTop();
	}

	private void createControlForLeft() {
	    trimControl = new Composite(parent, SWT.NONE);

	    trimControl.setLayout(new CellLayout(1)
			.setMargins(0,0)
			.setSpacing(3, 3)
			.setDefaultRow(Row.fixed())
			.setDefaultColumn(Row.growing()));

	    perspectiveBar = createBarManager(SWT.VERTICAL);

		perspectiveBar.createControl(trimControl);
		perspectiveBar.getControl().addListener(SWT.MenuDetect, popupListener);

		trimSeparator = new Label(trimControl, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sepData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_CENTER);
		sepData.widthHint = SEPARATOR_LENGTH;
		trimSeparator.setLayoutData(sepData);

		trimLayoutData = new GridData(GridData.FILL_BOTH);
		trimVisible = false;
		perspectiveBar.getControl().setLayoutData(trimLayoutData);
	}

 	private void createControlForTop() {
 	    perspectiveBar = createBarManager(SWT.HORIZONTAL);

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
				    if (perspectiveBar != null)
				        perspectiveBar.handleChevron(e);
				}
			}
		});
		coolItem.setMinimumSize(0, 0);

		perspectiveBar.getControl().addListener(SWT.MenuDetect, popupListener);
 	}

	private void showPerspectiveBarPopup(Point pt) {
	    if (perspectiveBar == null)
	        return;

		// Get the tool item under the mouse.
		ToolBar toolBar = perspectiveBar.getControl();
		ToolItem toolItem = toolBar.getItem(toolBar.toControl(pt));

		// Get the action for the tool item.
		Object data = null;
		if (toolItem != null)
		    data = toolItem.getData();

		if (toolItem == null || !(data instanceof PerspectiveBarContributionItem)) {
			if (genericMenu == null) {
				Menu menu = new Menu(toolBar);
				addDockOnSubMenu(menu);
				addShowTextItem(menu);
				genericMenu = menu;
			}

			// set the state of the menu items to match the preferences
			genericMenu.getItem(1).setSelection(PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));

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

			new MenuItem(menu, SWT.SEPARATOR);
			addDockOnSubMenu(menu);
			addShowTextItem(menu);
			popupMenu = menu;
		}
		popupMenu.setData(toolItem);

		popupMenu.getItem(4).setSelection(PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		
		// Show popup menu.
		if (popupMenu != null) {
			popupMenu.setLocation(pt.x, pt.y);
			popupMenu.setVisible(true);
		}
	}

	/**
	 * @param direction one of <code>SWT.HORIZONTAL</code> or <code>SWT.VERTICAL</code>
	 */
	private PerspectiveBarManager createBarManager(int direction) {
	    PerspectiveBarManager barManager = new PerspectiveBarManager(style | direction);
	    barManager.add(new PerspectiveBarNewContributionItem(window));

		// add an item for all open perspectives
		WorkbenchPage page = (WorkbenchPage)window.getActivePage();
		if (page != null) {
		    // these are returned with the most recently opened one first
			IPerspectiveDescriptor[] perspectives = page.getOpenedPerspectives();
			for (int i = 0; i < perspectives.length; ++i)
			    barManager.add(new PerspectiveBarContributionItem(perspectives[i], page));
		}

	    return barManager;
	}

	private void addDockOnSubMenu(Menu menu) {
	    MenuItem item = new MenuItem(menu, SWT.CASCADE);
	    item.setText(WorkbenchMessages.getString("PerspectiveSwitcher.dockOn")); //$NON-NLS-1$

	    Menu subMenu = new Menu(item);

	    RadioMenu radio = new RadioMenu(subMenu, location);
		radio.addMenuItem(WorkbenchMessages.getString("PerspectiveSwitcher.topRight"), new Integer(TOP_RIGHT)); //$NON-NLS-1$
		radio.addMenuItem(WorkbenchMessages.getString("PerspectiveSwitcher.topLeft"), new Integer(TOP_LEFT)); //$NON-NLS-1$
		radio.addMenuItem(WorkbenchMessages.getString("PerspectiveSwitcher.left"), new Integer(LEFT)); //$NON-NLS-1$
		radioButtons.add(radio);

		item.setMenu(subMenu);
	}

	private void addShowTextItem(Menu menu) {
		final MenuItem showtextMenuItem = new MenuItem(menu, SWT.CHECK);
		showtextMenuItem.setText(WorkbenchMessages.getString("PerspectiveBar.showText")); //$NON-NLS-1$
		showtextMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    if (perspectiveBar == null)
			        return;

				boolean preference = showtextMenuItem.getSelection();
                PrefUtil.getAPIPreferenceStore()
                        .setValue(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, preference);
                LayoutUtil.resize(perspectiveBar.getControl());	
			}
		});
	}
}
