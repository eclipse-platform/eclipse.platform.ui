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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.layout.CacheWrapper;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.PresentationUtil;

/**
 * A utility class to manage the perspective switcher.  At some point, it might be nice to
 * move all this into PerspectiveViewBar.
 * 
 * @since 3.0
 */
public class PerspectiveSwitcher implements IWindowTrim {

    private IWorkbenchWindow window;

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

    private CoolItem coolItem;

    private CacheWrapper toolbarWrapper;

    // The menus are cached, so the radio buttons should not be disposed until
    // the switcher is disposed.
    private Menu popupMenu;

    private Menu genericMenu;

    private static final int INITIAL = -1;

    private static final int TOP_RIGHT = 1;

    private static final int TOP_LEFT = 2;

    private static final int LEFT = 3;

    private static final int DEFAULT_RIGHT_X = 160;

    private int currentLocation = INITIAL;

    private IPreferenceStore apiPreferenceStore = PrefUtil
            .getAPIPreferenceStore();

    private IPropertyChangeListener propertyChangeListener;

    private Listener popupListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.type == SWT.MenuDetect) {
                showPerspectiveBarPopup(new Point(event.x, event.y));
            }
        }
    };

    private PerspectiveAdapter perspectiveAdapter = new PerspectiveAdapter() {
        public void perspectiveOpened(IWorkbenchPage page,
                IPerspectiveDescriptor perspective) {
            addPerspectiveShortcut(perspective, page);
        }
        public void perspectiveClosed(IWorkbenchPage page,
                IPerspectiveDescriptor perspective) {
            removePerspectiveShortcut(perspective, page);
        }
        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            selectPerspectiveShortcut(perspective, page, true);
        }
        public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            selectPerspectiveShortcut(perspective, page, false);
        }
        public void perspectiveSavedAs(IWorkbenchPage page,
                IPerspectiveDescriptor oldPerspective,
                IPerspectiveDescriptor newPerspective) {
            updatePerspectiveShortcut(oldPerspective, newPerspective, page);
        }
    };
    
	private Listener dragListener;

	private IDragOverListener dragTarget;

	private IDragOverListener externalDragTarget;

	private DisposeListener toolBarListener;

	private IReorderListener reorderListener;

	/**
     * Creates an instance of the perspective switcher.
	 * @param window  it's window
	 * @param topBar  the CBanner to place this widget in
	 * @param style   the widget style to use
	 */
	public PerspectiveSwitcher(IWorkbenchWindow window, CBanner topBar, int style) {
		this.window = window;
		this.topBar = topBar;
		this.style = style;
		setPropertyChangeListener();
		// this listener will only be run when the Shell is being disposed
		// and each WorkbenchWindow has its own PerspectiveSwitcher
		toolBarListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
        window.addPerspectiveListener(perspectiveAdapter);
	}

	private static int convertLocation(String preference) {
		if (IWorkbenchPreferenceConstants.TOP_RIGHT.equals(preference))
			return TOP_RIGHT;
		if (IWorkbenchPreferenceConstants.TOP_LEFT.equals(preference))
			return TOP_LEFT;
		if (IWorkbenchPreferenceConstants.LEFT.equals(preference))
			return LEFT;

		return TOP_RIGHT;
	}

	/**
     * Create the contents of the receiver
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Assert.isTrue(this.parent == null);
		this.parent = parent;
		// set the initial location read from the preference
		setPerspectiveBarLocation(PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR));
	}

	private void addPerspectiveShortcut(IPerspectiveDescriptor perspective,
			IWorkbenchPage workbenchPage) {
		if (perspectiveBar == null)
			return;

		PerspectiveBarContributionItem item = new PerspectiveBarContributionItem(
				perspective, workbenchPage);
		perspectiveBar.addItem(item);
		setCoolItemSize(coolItem);
		// This is need to update the vertical size of the tool bar on GTK+ when
		// using large fonts.
		if (perspectiveBar != null)
			perspectiveBar.update(true);
	}

	/**
     * Find a contribution item that matches the perspective provided.
     * 
	 * @param perspective
	 * @param page
	 * @return the <code>IContributionItem</code> or null if no matches were found
	 */
	public IContributionItem findPerspectiveShortcut(
			IPerspectiveDescriptor perspective, IWorkbenchPage page) {
		if (perspectiveBar == null)
			return null;

		IContributionItem[] items = perspectiveBar.getItems();
		int length = items.length;
		for (int i = 0; i < length; i++) {
			IContributionItem item = items[i];
			if (item instanceof PerspectiveBarContributionItem
					&& ((PerspectiveBarContributionItem) item).handles(
							perspective, page))
				return item;
		}
		return null;
	}

	private void removePerspectiveShortcut(IPerspectiveDescriptor perspective,
			IWorkbenchPage page) {
		if (perspectiveBar == null)
			return;

		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null) {
			if (item instanceof PerspectiveBarContributionItem)
				perspectiveBar
						.removeItem((PerspectiveBarContributionItem) item);
			item.dispose();
			perspectiveBar.update(false);
			setCoolItemSize(coolItem);
		}
	}

	/**
     * Locate the perspective bar according to the provided location
	 * @param preference the location to put the perspective bar at
	 */
	public void setPerspectiveBarLocation(String preference) {
		// return if the control has not been created. createControl(...) will
		// handle updating the state in that case
		if (parent == null)
			return;
		int newLocation = convertLocation(preference);
		if (newLocation == currentLocation)
			return;
		createControlForLocation(newLocation);
		currentLocation = newLocation;
		showPerspectiveBar();
		if (newLocation == TOP_LEFT || newLocation == TOP_RIGHT) {
			updatePerspectiveBar();
			updateBarParent();
		}
	}

	/**
	 * Make the perspective bar visible in its current location. This method
	 * should not be used unless the control has been successfully created.
	 */
	private void showPerspectiveBar() {
		switch (currentLocation) {
		case TOP_LEFT:
			topBar.setRight(null);
			topBar.setBottom(perspectiveCoolBarWrapper.getControl());
			break;
		case TOP_RIGHT:
			topBar.setBottom(null);
			topBar.setRight(perspectiveCoolBarWrapper.getControl());
			topBar.setRightWidth(DEFAULT_RIGHT_X);
			break;
		case LEFT:
			topBar.setBottom(null);
			topBar.setRight(null);
			LayoutUtil.resize(topBar);
			((WorkbenchWindow) window).addToTrim(this, SWT.LEFT);
			break;
		default:
			return;
		}

		LayoutUtil.resize(perspectiveBar.getControl());
	}

	/**
     * Update the receiver
	 * @param force
	 */
	public void update(boolean force) {
		if (perspectiveBar == null)
			return;

		perspectiveBar.update(force);

		if (currentLocation == LEFT) {
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

	private void selectPerspectiveShortcut(IPerspectiveDescriptor perspective,
			IWorkbenchPage page, boolean selected) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null && (item instanceof PerspectiveBarContributionItem)) {
			if (selected) {
				// check if not visible and ensure visible
				PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem) item;
				perspectiveBar.select(contribItem);
			}
			// select or de-select
			((PerspectiveBarContributionItem) item).setSelection(selected);
		}
	}

	private void updatePerspectiveShortcut(IPerspectiveDescriptor oldDesc,
			IPerspectiveDescriptor newDesc, IWorkbenchPage page) {
		IContributionItem item = findPerspectiveShortcut(oldDesc, page);
		if (item != null && (item instanceof PerspectiveBarContributionItem))
			((PerspectiveBarContributionItem) item).update(newDesc);
	}

	/**
     * Answer the perspective bar manager
	 * @return the manager
	 */
	public PerspectiveBarManager getPerspectiveBar() {
		return perspectiveBar;
	}

	/**
	 * Dispose resources being held by the receiver
	 */
	public void dispose() {
        window.removePerspectiveListener(perspectiveAdapter);
		if (propertyChangeListener != null) {
			apiPreferenceStore
					.removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}
		unhookDragSupport();
		toolBarListener = null;
	}

	private void disposeChildControls() {

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

		if (toolbarWrapper != null) {
			toolbarWrapper.dispose();
			toolbarWrapper = null;
		}

		if (perspectiveBar != null) {
			perspectiveBar.dispose();
			perspectiveBar = null;
		}

		perspectiveCoolBarWrapper = null;
	}

	/**
	 * Ensures the control has been set for the argument location. If the
	 * control already exists and can be used the argument location, nothing
	 * happens. Updates the location attribute.
	 * 
	 * @param newLocation
	 */
	private void createControlForLocation(int newLocation) {
		// if there is a control, then perhaps it can be reused
		if (perspectiveBar != null && perspectiveBar.getControl() != null
				&& !perspectiveBar.getControl().isDisposed()) {
			if (newLocation == LEFT && currentLocation == LEFT)
				return;
			if ((newLocation == TOP_LEFT || newLocation == TOP_RIGHT)
					&& (currentLocation == TOP_LEFT || currentLocation == TOP_RIGHT))
				return;
		}

		if (perspectiveBar != null) {
			perspectiveBar.getControl().removeDisposeListener(toolBarListener);
			unhookDragSupport();
		}
		// otherwise dispose the current controls and make new ones
		disposeChildControls();
		if (newLocation == LEFT)
			createControlForLeft();
		else
			createControlForTop();
		hookDragSupport();

		perspectiveBar.getControl().addDisposeListener(toolBarListener);
	}

	/**
	 * Remove any drag and drop support and associated listeners hooked for the
	 * perspective switcher.
	 */
	private void unhookDragSupport() {
		ToolBar bar = perspectiveBar.getControl();

		if (bar == null || bar.isDisposed() || dragListener == null)
			return;
		PresentationUtil.removeDragListener(bar, dragListener);
		DragUtil.removeDragTarget(perspectiveBar.getControl(), dragTarget);
		DragUtil.removeDragTarget(null, externalDragTarget);
		dragListener = null;
		dragTarget = null;
		externalDragTarget = null;
	}

    /**
	 * Attach drag and drop support and associated listeners hooked for 
	 * the perspective switcher.
	 */
	 private void hookDragSupport() {
        dragListener = new Listener() {
        	/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				ToolBar toolbar = perspectiveBar.getControl();
                ToolItem item = toolbar.getItem(new Point(event.x, event.y));
                
                if (item != null) {
                	//ignore the first item, which remains in position Zero
                    if (item.getData() instanceof PerspectiveBarNewContributionItem)
                    	return;
                    
                	Rectangle bounds = item.getBounds();
                	Rectangle parentBounds = toolbar.getBounds();
                	bounds.x += parentBounds.x;
                	bounds.y += parentBounds.y;
                	startDragging(item.getData(), toolbar.getDisplay().map(toolbar, null, bounds));
                } else { 
                    //startDragging(toolbar, toolbar.getDisplay().map(toolbar, null, toolbar.getBounds()));
                }
            }
			
			private void startDragging(Object widget, Rectangle bounds) {
				if(!DragUtil.performDrag(widget, bounds, new Point(bounds.x, bounds.y), true)) {
				       //currently do nothing on a failed drag 
                }
		    }
        };

		dragTarget = new IDragOverListener() {
			protected PerspectiveDropTarget perspectiveDropTarget;

			class PerspectiveDropTarget extends AbstractDropTarget {

				private PerspectiveBarContributionItem perspective;

				private Point location;

                /**
				 * @param location
				 * @param draggedObject
				 */
				public PerspectiveDropTarget(Object draggedObject,
						Point location) {
					update(draggedObject, location);
				}

				/**
				 * 
				 * @param draggedObject
				 * @param location
				 */
				private void update(Object draggedObject, Point location) {
					this.location = location;
					this.perspective = (PerspectiveBarContributionItem) draggedObject;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
				 */
				public void drop() {
					ToolBar toolBar = perspectiveBar.getControl();
					ToolItem item = toolBar.getItem(toolBar.getDisplay().map(
							null, toolBar, location));
					if (toolBar.getItem(0) == item)
						return;
					ToolItem[] items = toolBar.getItems();
					ToolItem droppedItem = null;
					int dropIndex = -1;
					for (int i = 0; i < items.length; i++) {
						if (item == items[i])
							dropIndex = i;
						if (items[i].getData() == perspective)
							droppedItem = items[i];
					}
					if (dropIndex != -1 && droppedItem != null && (droppedItem != item)) {
						PerspectiveBarContributionItem barItem = (PerspectiveBarContributionItem) droppedItem.getData();
						// policy is to insert at the beginning so mirror the value when indicating a 
						// new position for the perspective
						if (reorderListener != null)
							reorderListener.reorder(barItem.getPerspective(), Math.abs(dropIndex - (items.length - 1)));

						perspectiveBar.relocate(barItem, dropIndex);
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
				 */
				public Cursor getCursor() {
					return DragCursors.getCursor(DragCursors.CENTER);
				}

				boolean sameShell() {
					return perspective.getToolItem().getParent().getShell().equals(perspectiveBar.getControl().getShell());
				}
				
				public Rectangle getSnapRectangle() {
					ToolBar toolBar = perspectiveBar.getControl();
					ToolItem item = toolBar.getItem(toolBar.getDisplay().map(
							null, toolBar, location));
					Rectangle bounds;
					if (item != null && item != toolBar.getItem(0)) {
						bounds = item.getBounds();
					} else {
						// it should not be possible to start a drag with item 0
						return null;
					}
					return toolBar.getDisplay().map(toolBar, null, bounds);
				}
			}

			public IDropTarget drag(Control currentControl,
					Object draggedObject, Point position,
					Rectangle dragRectangle) {
				if (draggedObject instanceof PerspectiveBarContributionItem) {
					if (perspectiveDropTarget == null) {
						perspectiveDropTarget = new PerspectiveDropTarget(
								draggedObject, position);
					} else {
						perspectiveDropTarget.update(draggedObject, position);
					}
					// do not support drag to perspective bars between shells.
					if (!perspectiveDropTarget.sameShell())
							return null;
					
					return perspectiveDropTarget;
				}// else if (draggedObject instanceof IPerspectiveBar) {
				//	return new PerspectiveBarDropTarget();
				//}

				return null;
			}

		};

		externalDragTarget = new IDragOverListener() {
			protected ExternalPerspectiveDropTarget externalPerspectiveDropTarget;

			class ExternalPerspectiveDropTarget extends AbstractDropTarget {

				private PerspectiveBarContributionItem perspective;
				private Rectangle rect; 
				private Point location;

				/**
				 * @param location
				 * @param draggedObject
				 */
				public ExternalPerspectiveDropTarget(Object draggedObject,
						Point location) {
					update(draggedObject, location);
				}

				/**
				 * 
				 * @param draggedObject
				 * @param location
				 */
				private void update(Object draggedObject, Point location) {
					this.location = location;
					this.perspective = (PerspectiveBarContributionItem) draggedObject;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#drop()
				 */
				public void drop() {
					IWorkbenchWindow workbenchWindow = Workbench.getInstance()
							.getActiveWorkbenchWindow();
					IWorkbenchPage page = workbenchWindow.getActivePage();
					
			        // Open the page.
			        try {
			        	workbenchWindow.getWorkbench().openWorkbenchWindow(perspective.getId(),
								page.getInput());
			        } catch (WorkbenchException e) {
			            MessageDialog.openError(workbenchWindow.getShell(), WorkbenchMessages.OpenNewWindowMenu_dialogTitle, 
			                    e.getMessage());
			            return;
			        }
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ui.internal.dnd.IDropTarget#getCursor()
				 */
				public Cursor getCursor() {
					return DragCursors.getCursor(DragCursors.OFFSCREEN);
				}

				public Rectangle getSnapRectangle() {
					/*
					 * ToolBar toolBar = perspectiveBar.getControl(); ToolItem
					 * item = toolBar.getItem(toolBar.getDisplay().map(null,
					 * toolBar, location)); Rectangle bounds; if (item != null &&
					 * item != toolBar.getItem(0)) { bounds = item.getBounds(); }
					 * else { // it should not be possible to start a drag with
					 * item 0 return null; }
					 */
					if (rect == null) {
						IWorkbenchWindow workbenchWindow = Workbench.getInstance()
						.getActiveWorkbenchWindow();
						rect = workbenchWindow.getShell().getBounds();
					}
					rect.x = location.x;
					rect.y = location.y;					
					return rect;
				}
			}

			public IDropTarget drag(Control currentControl,
					Object draggedObject, Point position,
					Rectangle dragRectangle) {
				if (draggedObject instanceof PerspectiveBarContributionItem) {
					if (externalPerspectiveDropTarget == null) {
						externalPerspectiveDropTarget = new ExternalPerspectiveDropTarget(
								draggedObject, position);
					} else {
						externalPerspectiveDropTarget.update(draggedObject, position);
					}
					return externalPerspectiveDropTarget;
				}// else if (draggedObject instanceof IPerspectiveBar) {
				//	return new PerspectiveBarDropTarget();
				//}

				return null;
			}

		};

		PresentationUtil.addDragListener(perspectiveBar.getControl(),
				dragListener);
		DragUtil.addDragTarget(perspectiveBar.getControl(), dragTarget);
		DragUtil.addDragTarget(null, externalDragTarget);
	}

	private void setPropertyChangeListener() {
        propertyChangeListener = new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR
                        .equals(propertyChangeEvent.getProperty())) {
                    if (perspectiveBar == null)
                        return;
                    updatePerspectiveBar();
                    updateBarParent();
                }
            }
        };
        apiPreferenceStore.addPropertyChangeListener(propertyChangeListener);
    }

    private void createControlForLeft() {
        trimControl = new Composite(parent, SWT.NONE);

        trimControl.setLayout(new CellLayout(1).setMargins(0, 0).setSpacing(3,
                3).setDefaultRow(Row.fixed()).setDefaultColumn(Row.growing()));

        perspectiveBar = createBarManager(SWT.VERTICAL);

        perspectiveBar.createControl(trimControl);
        perspectiveBar.getControl().addListener(SWT.MenuDetect, popupListener);

//        trimSeparator = new Label(trimControl, SWT.SEPARATOR | SWT.HORIZONTAL);
//        GridData sepData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
//                | GridData.HORIZONTAL_ALIGN_CENTER);
//        sepData.widthHint = SEPARATOR_LENGTH;
//        trimSeparator.setLayoutData(sepData);
//        trimSeparator.setVisible(false);

        trimLayoutData = new GridData(GridData.FILL_BOTH);
        trimVisible = false;
        perspectiveBar.getControl().setLayoutData(trimLayoutData);
    }

    private void createControlForTop() {
        perspectiveBar = createBarManager(SWT.HORIZONTAL);

        perspectiveCoolBarWrapper = new CacheWrapper(topBar);
        perspectiveCoolBar = new CoolBar(
                perspectiveCoolBarWrapper.getControl(), SWT.FLAT);
        coolItem = new CoolItem(perspectiveCoolBar, SWT.DROP_DOWN);
        toolbarWrapper = new CacheWrapper(perspectiveCoolBar);
        perspectiveBar.createControl(toolbarWrapper.getControl());
        coolItem.setControl(toolbarWrapper.getControl());
        perspectiveCoolBar.setLocked(true);
        perspectiveBar.setParent(perspectiveCoolBar);
        perspectiveBar.update(true);

        // adjust the toolbar size to display as many items as possible
        perspectiveCoolBar.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                setCoolItemSize(coolItem);
            }
        });

        coolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.ARROW) {
                    if (perspectiveBar != null) {
                        perspectiveBar.handleChevron(e);
                    }
                }
            }
        });
        coolItem.setMinimumSize(0, 0);
        perspectiveBar.getControl().addListener(SWT.MenuDetect, popupListener);
    }

    /**
     * @param coolItem
     */
    private void setCoolItemSize(final CoolItem coolItem) {
        // there is no coolItem when the bar is on the left
        if (currentLocation == LEFT)
            return;

        ToolBar toolbar = perspectiveBar.getControl();
        if (toolbar == null)
            return;

        int rowHeight = 0;
        ToolItem[] toolItems = toolbar.getItems();
        for (int i = 0; i < toolItems.length; i++) {
            rowHeight = Math.max(rowHeight, toolItems[i].getBounds().height);
        }

        Rectangle area = perspectiveCoolBar.getClientArea();
        int rows = rowHeight <= 0 ? 1 : (int) Math.max(1, Math
                .floor(area.height / rowHeight));
        if (rows == 1 || (toolbar.getStyle() & SWT.WRAP) == 0
                || currentLocation == TOP_LEFT) {
            Point p = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            coolItem.setSize(coolItem.computeSize(p.x, p.y));
            return;
        }
        Point offset = coolItem.computeSize(0, 0);
        Point wrappedSize = toolbar.computeSize(area.width - offset.x,
                SWT.DEFAULT);
        int h = rows * rowHeight;
        int w = wrappedSize.y <= h ? wrappedSize.x : wrappedSize.x + 1;
        coolItem.setSize(coolItem.computeSize(w, h));
    }

    private void showPerspectiveBarPopup(Point pt) {
        if (perspectiveBar == null)
            return;

        // Get the tool item under the mouse.
        ToolBar toolBar = perspectiveBar.getControl();
        ToolItem toolItem = toolBar.getItem(toolBar.toControl(pt));
        
        // Get the action for the tool item.
        Object data = null;
        if (toolItem != null){
            data = toolItem.getData();
        }
        if (toolItem == null
                || !(data instanceof PerspectiveBarContributionItem)) {
            if (genericMenu == null) {
                Menu menu = new Menu(toolBar);
                addDockOnSubMenu(menu);
                addShowTextItem(menu);
                genericMenu = menu;
            }

            // set the state of the menu items to match the preferences
            genericMenu
                    .getItem(1)
                    .setSelection(
                            PrefUtil
                                    .getAPIPreferenceStore()
                                    .getBoolean(
                                            IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
            updateLocationItems(genericMenu.getItem(0).getMenu(),
                    currentLocation);

            // Show popup menu.
            genericMenu.setLocation(pt.x, pt.y);
            genericMenu.setVisible(true);
            return;
        }

        if (data == null || !(data instanceof PerspectiveBarContributionItem))
            return;

        boolean selectedIsActive = selectedPerspectiveIsActive(data); 

        // The perspective bar menu is created lazily here.
        // Its data is set (each time) to the tool item, which refers to the SetPagePerspectiveAction
        // which in turn refers to the page and perspective.
        // It is important not to refer to the action, the page or the perspective directly
        // since otherwise the menu hangs on to them after they are closed.
        // By hanging onto the tool item instead, these references are cleared when the
        // corresponding page or perspective is closed.
        // See bug 11282 for more details on why it is done this way.
        if (popupMenu == null) {
        	popupMenu = createPopup(toolBar, selectedIsActive);
        }
        
    	if (!selectedIsActive && popupMenu.getItemCount() > 5) {
    		popupMenu.getItem(0).dispose();
        	popupMenu.getItem(0).dispose(); 
        	popupMenu.getItem(0).dispose(); 
        } else if(selectedIsActive && popupMenu.getItemCount() <= 5){
    		popupMenu.dispose();
    		popupMenu = createPopup(toolBar, selectedIsActive);        		
    	}
 
        popupMenu.setData(toolItem);

        // set the state of the menu items to match the preferences
        if(selectedIsActive){
        popupMenu
                .getItem(7)
                .setSelection(
                        PrefUtil
                                .getAPIPreferenceStore()
                                .getBoolean(
                                        IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
        updateLocationItems(popupMenu.getItem(6).getMenu(), currentLocation);
        }
        else{
        	popupMenu
            .getItem(4)
            .setSelection(
                    PrefUtil
                            .getAPIPreferenceStore()
                            .getBoolean(
                                    IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
    updateLocationItems(popupMenu.getItem(3).getMenu(), currentLocation);
        }
        
        // Show popup menu.
        popupMenu.setLocation(pt.x, pt.y);
        popupMenu.setVisible(true);
    }

    /**
     * @param data
     * @return <code>true</code> if the button whose menu has to be created represents the active perspective 
     */
    private boolean selectedPerspectiveIsActive(Object data) {
        if (data == null || data instanceof PerspectiveBarNewContributionItem)
            return false;
        IPerspectiveDescriptor perspectiveInFocus=((PerspectiveBarContributionItem)data).getPerspective();
        return perspectiveInFocus.equals(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getPerspective());
    }

	/**
	 * @param toolBar
	 */
	private Menu createPopup(ToolBar toolBar, boolean isActive){
		Menu menu = new Menu(toolBar);
		if(isActive){
			addCustomizeItem(menu);
			addSaveAsItem(menu);
			addResetItem(menu);
		} 
		addCloseItems(menu);
		new MenuItem(menu, SWT.SEPARATOR);
		addDockOnSubMenu(menu);
		addShowTextItem(menu);
		return menu;
	}

    /**
     * @param menu
     */
    private void addCloseItems(Menu menu) {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.setText(WorkbenchMessages.WorkbenchWindow_close); 
        window.getWorkbench().getHelpSystem().setHelp(menuItem,
        		IWorkbenchHelpContextIds.CLOSE_PAGE_ACTION);
        menuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ToolItem perspectiveToolItem = (ToolItem) popupMenu
                        .getData();
                if (perspectiveToolItem != null
                        && !perspectiveToolItem.isDisposed()) {
                    PerspectiveBarContributionItem item = (PerspectiveBarContributionItem) perspectiveToolItem
                            .getData();
                    item.getPage().closePerspective(item.getPerspective(),
                            true, true);
                }
            }
        });
        menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.setText(WorkbenchMessages.WorkbenchWindow_closeAll); 
        window.getWorkbench().getHelpSystem().setHelp(menuItem,
        		IWorkbenchHelpContextIds.CLOSE_ALL_PAGES_ACTION);
        menuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ToolItem perspectiveToolItem = (ToolItem) popupMenu
                        .getData();
                if (perspectiveToolItem != null
                        && !perspectiveToolItem.isDisposed()) {
                    PerspectiveBarContributionItem item = (PerspectiveBarContributionItem) perspectiveToolItem
                            .getData();
                    item.getPage().closeAllPerspectives(true, true);
                }
            }
        });
    }

    /**
     * @param direction one of <code>SWT.HORIZONTAL</code> or <code>SWT.VERTICAL</code>
     */
    private PerspectiveBarManager createBarManager(int direction) {
        PerspectiveBarManager barManager = new PerspectiveBarManager(style
                | direction);
        barManager.add(new PerspectiveBarNewContributionItem(window));

        // add an item for all open perspectives
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            // these are returned with the most recently opened one first
            IPerspectiveDescriptor[] perspectives = page
                    .getOpenPerspectives();
            for (int i = 0; i < perspectives.length; i++)
                barManager.insert(1, new PerspectiveBarContributionItem(
                        perspectives[i], page));
        }

        return barManager;
    }

    private void updateLocationItems(Menu parent, int newLocation) {
        MenuItem left;
        MenuItem topLeft;
        MenuItem topRight;

        topRight = parent.getItem(0);
        topLeft = parent.getItem(1);
        left = parent.getItem(2);

        if (newLocation == LEFT) {
            left.setSelection(true);
            topRight.setSelection(false);
            topLeft.setSelection(false);
        } else if (newLocation == TOP_LEFT) {
            topLeft.setSelection(true);
            left.setSelection(false);
            topRight.setSelection(false);
        } else {
            topRight.setSelection(true);
            left.setSelection(false);
            topLeft.setSelection(false);
        }
    }

    private void addDockOnSubMenu(Menu menu) {
        MenuItem item = new MenuItem(menu, SWT.CASCADE);
        item.setText(WorkbenchMessages.PerspectiveSwitcher_dockOn);

        final Menu subMenu = new Menu(item);

        final MenuItem menuItemTopRight = new MenuItem(subMenu, SWT.RADIO);
        menuItemTopRight.setText(WorkbenchMessages.PerspectiveSwitcher_topRight); 
        
        window.getWorkbench().getHelpSystem().setHelp(menuItemTopRight,
        		IWorkbenchHelpContextIds.DOCK_ON_PERSPECTIVE_ACTION);

        final MenuItem menuItemTopLeft = new MenuItem(subMenu, SWT.RADIO);
        menuItemTopLeft.setText(WorkbenchMessages.PerspectiveSwitcher_topLeft); 
        
        window.getWorkbench().getHelpSystem().setHelp(menuItemTopLeft,
        		IWorkbenchHelpContextIds.DOCK_ON_PERSPECTIVE_ACTION);

        final MenuItem menuItemLeft = new MenuItem(subMenu, SWT.RADIO);
        menuItemLeft.setText(WorkbenchMessages.PerspectiveSwitcher_left); 
        
        window.getWorkbench().getHelpSystem().setHelp(menuItemLeft,
        		IWorkbenchHelpContextIds.DOCK_ON_PERSPECTIVE_ACTION);

        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                MenuItem item = (MenuItem) e.widget;
                String pref = null;
                if (item.equals(menuItemLeft)) {
                    updateLocationItems(subMenu, LEFT);
                    pref = IWorkbenchPreferenceConstants.LEFT;
                } else if (item.equals(menuItemTopLeft)) {
                    updateLocationItems(subMenu, TOP_LEFT);
                    pref = IWorkbenchPreferenceConstants.TOP_LEFT;
                } else {
                    updateLocationItems(subMenu, TOP_RIGHT);
                    pref = IWorkbenchPreferenceConstants.TOP_RIGHT;
                }
                IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
                apiStore.setValue(
                        IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
                        pref);
            }
        };

        menuItemTopRight.addSelectionListener(listener);
        menuItemTopLeft.addSelectionListener(listener);
        menuItemLeft.addSelectionListener(listener);
        item.setMenu(subMenu);
    }

    private void addShowTextItem(Menu menu) {
        final MenuItem showtextMenuItem = new MenuItem(menu, SWT.CHECK);
        showtextMenuItem.setText(WorkbenchMessages.PerspectiveBar_showText);
        window.getWorkbench().getHelpSystem().setHelp(showtextMenuItem,
        		IWorkbenchHelpContextIds.SHOW_TEXT_PERSPECTIVE_ACTION);

        showtextMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (perspectiveBar == null)
                    return;

                boolean preference = showtextMenuItem.getSelection();
                PrefUtil
                        .getAPIPreferenceStore()
                        .setValue(
                                IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
                                preference);
            }
        });
    }

    private void addCustomizeItem(Menu menu) {
        final MenuItem customizeMenuItem = new MenuItem(menu, SWT.Activate);
        customizeMenuItem.setText(WorkbenchMessages.PerspectiveBar_customize);
        window.getWorkbench().getHelpSystem().setHelp(customizeMenuItem,
        		IWorkbenchHelpContextIds.EDIT_ACTION_SETS_ACTION);
        customizeMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (perspectiveBar == null)
                    return;
                EditActionSetsAction editAction=new EditActionSetsAction(window);
                editAction.setEnabled(true);
                editAction.run();
            }
        });
    }
    
    private void addSaveAsItem(Menu menu) {
        final MenuItem saveasMenuItem = new MenuItem(menu, SWT.Activate);
        saveasMenuItem.setText(WorkbenchMessages.PerspectiveBar_saveAs); 
        window.getWorkbench().getHelpSystem().setHelp(saveasMenuItem,
        		IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
        saveasMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (perspectiveBar == null)
                    return;
                SavePerspectiveAction saveAction=new SavePerspectiveAction(window);
                saveAction.setEnabled(true);
                saveAction.run();
            }
        });
    }
    
    private void addResetItem(Menu menu) {
        final MenuItem resetMenuItem = new MenuItem(menu, SWT.Activate);
        resetMenuItem.setText(WorkbenchMessages.PerspectiveBar_reset);
        window.getWorkbench().getHelpSystem().setHelp(resetMenuItem,
        		IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
        resetMenuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (perspectiveBar == null)
                    return;
                ResetPerspectiveAction resetAction=new ResetPerspectiveAction(window);
                resetAction.setEnabled(true);
                resetAction.run(); 
             }
        });
    }
    
    /**
     * Method to save the width of the perspective bar in the 
     * @param persBarMem 
     */
    public void saveState(IMemento persBarMem) {
        // save the width of the perspective bar
        IMemento childMem = persBarMem
                .createChild(IWorkbenchConstants.TAG_ITEM_SIZE);

        int x;
        if (currentLocation == TOP_RIGHT && topBar != null)
            x = topBar.getRightWidth();
        else
            x = DEFAULT_RIGHT_X;

        childMem.putString(IWorkbenchConstants.TAG_X, Integer.toString(x));
    }

    /**
     * Method to restore the width of the perspective bar
     * @param memento 
     */
    public void restoreState(IMemento memento) {
        if (memento == null)
            return;
        // restore the width of the perspective bar
        IMemento attributes = memento
                .getChild(IWorkbenchConstants.TAG_PERSPECTIVE_BAR);
        IMemento size = null;
        if (attributes != null)
            size = attributes.getChild(IWorkbenchConstants.TAG_ITEM_SIZE);
        if (size != null && currentLocation == TOP_RIGHT && topBar != null) {
            Integer x = size.getInteger(IWorkbenchConstants.TAG_X);
            if (x != null)
                topBar.setRightWidth(x.intValue());
            else
                topBar.setRightWidth(DEFAULT_RIGHT_X);
        }
    }

    /**
     * Method to rebuild and update the toolbar when necessary
     */
    void updatePerspectiveBar() {
        // Update each item as the text may have to be shortened.
        IContributionItem[] items = perspectiveBar.getItems();
        for (int i = 0; i < items.length; i++)
            items[i].update();
        // make sure the selected item is visible
        perspectiveBar.arrangeToolbar();
        setCoolItemSize(coolItem);
        perspectiveBar.getControl().redraw();
    }

    /**
     * Updates the height of the CBanner if the perspective bar
     * is docked on the top right
     */
    public void updateBarParent() {
        if (perspectiveBar == null || perspectiveBar.getControl() == null)
            return;

        // TOP_LEFT and LEFT need only relayout in this case, however TOP_RIGHT
        // will need to set the minimum height of the CBanner as it might have changed.
        if (currentLocation == TOP_RIGHT && topBar != null) {
            // This gets the height of the tallest tool item.
            int maxRowHeight = 0;
            ToolItem[] toolItems = perspectiveBar.getControl().getItems();
            for (int i = 0; i < toolItems.length; i++) {
                maxRowHeight = Math.max(maxRowHeight,
                        toolItems[i].getBounds().height);
            }
            // This sets the CBanner's minimum height to support large fonts
            topBar.setRightMinimumSize(new Point(SWT.DEFAULT, maxRowHeight));
        }

        LayoutUtil.resize(perspectiveBar.getControl());
    }

	/**
	 * Add a listener for reordering of perspectives (usually done through drag
	 * and drop).
	 * 
	 * @param listener
	 */
	public void addReorderListener(IReorderListener listener) {
		reorderListener = listener;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#dock(int)
	 */
	public void dock(int dropSide) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getControl()
	 */
	public Control getControl() {
		return trimControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getId()
	 */
	public String getId() {
		return "org.eclipse.ui.internal.PerspectiveSwitcher"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		return WorkbenchMessages.TrimCommon_PerspectiveSwitcher_TrimName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getValidSides()
	 */
	public int getValidSides() {
		return SWT.TOP|SWT.LEFT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#isCloseable()
	 */
	public boolean isCloseable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#handleClose()
	 */
	public void handleClose() {
		// nothing to do...		
	}
}
