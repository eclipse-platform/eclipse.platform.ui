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
package org.eclipse.ui.examples.presentation.wrappedtabs;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.presentation.PresentationImages;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.0
 */
public class WrappedTabsPartPresentation extends StackPresentation {

	private static class DropLocation {
		int insertionPosition = 0;
		IPresentablePart part;
		boolean before;
	}
	
	private static final String PART_DATA = "part";
	
	private boolean activeFocus = false;
	
	/**
	 * Main widget for the presentation
	 */
	private Composite presentationControl;
	
	/**
	 * Currently selected part
	 */
	private IPresentablePart current;
	
	/**
	 * ToolBar that will be used to select the active presentable part 
	 */
	private ToolBar toolBar;
	
	/**
	 * ToolBar that will contain close, minimize, etc.
	 */
	private ToolBar upperRight;
	
	/**
	 * close button
	 */
	private ToolItem close;
	
	/**
	 * View menu button
	 */
	private ToolItem viewMenu;
	
	/**
	 * Minimize button
	 */
	private ToolItem minView;

	/**
	 * Show/hide toolbar button
	 */
	private ToolItem showToolbar;
	
	private ToolBar titleIconToolbar;
	
	/**
	 * Title icon
	 */
	private ToolItem titleIcon;
	
	private MenuManager systemMenuManager = new MenuManager();
	private ViewForm contentArea;
	
	private Label contentDescription;
	private Composite contentDescriptionWrapper;
	
	private Composite clientArea;
	
	private ProxyControl toolbarProxy;
	
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
	 * Drag listener for regions outside the toolbar
	 */
	Listener dragListener = new Listener() {
		public void handleEvent(Event event) {
			Point loc = new Point(event.x, event.y);
			Control ctrl = (Control)event.widget;
			
			getSite().dragStart(ctrl.toDisplay(loc), false);
		}
	};
	
	/**
	 * Listener attached to all tool items. It removes listeners from the associated
	 * part when the tool item is destroyed. This is required to prevent memory leaks.
	 */
	private DisposeListener tabDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (e.widget instanceof ToolItem) {
				ToolItem item = (ToolItem)e.widget;
				
				IPresentablePart part = getPartForTab(item);
				
				part.removePropertyListener(childPropertyChangeListener);
			}
		}
	};
	
	/**
	 * This listener responds to selection events in all tool items.
	 */
	SelectionAdapter tabItemSelectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			ToolItem toolItem = (ToolItem) e.widget;
			IPresentablePart item = getPartForTab(toolItem);
			if (item != null) {
				// Clicking on the active tab should give focus to the current part
				if (item == current) {
					item.setFocus();
				}
				getSite().selectPart(item);
			}
			toolItem.setSelection(true);
		}
	};
	
	/**
	 * Listener to changes made to the current theme. The presentation will
	 * redraw when the theme changes.
	 */
	private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {	
		public void propertyChange(PropertyChangeEvent event) {
			if(! presentationControl.isDisposed()) {
				toolBar.setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(WrappedTabsThemeConstants.TAB_FONT));
				layout();
				presentationControl.redraw();
			}
		}
	};
	
	private Listener menuListener = new Listener() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			Point globalPos = new Point(event.x, event.y);
			
			if (event.widget == toolBar) {
				Point localPos = toolBar.toControl(globalPos);
				ToolItem item = toolBar.getItem(localPos);
					
				if (item != null) {
					IPresentablePart part = getPartForTab(item);
					getSite().selectPart(part);
					showSystemMenu(globalPos);
					return;
				}
			}
            
            IPresentablePart part = getCurrent();
            if (part != null) {
                showSystemMenu(globalPos);
            }
		}
	};
	
	private MouseListener mouseListener = new MouseAdapter() {
		
		// If we single-click on an empty space on the toolbar, move focus to the
		// active control
		public void mouseDown(MouseEvent e) {
			Point p = new Point(e.x, e.y);
			
			// Ignore double-clicks if we're currently over a toolbar item
			if (isOverToolItem(e)) {
				return;
			}
			
			if (current != null) {
				current.setFocus();
			}
		}
		
		public boolean isOverToolItem(MouseEvent e) {
			Point p = new Point(e.x, e.y);
			Control control = (Control)e.widget;
			
			if (control instanceof ToolBar) {
				ToolItem item = ((ToolBar)control).getItem(p);
				
				if (item != null) {
					return true;
				}
			} 
			
			return false;
			
		}
		
		// If we double-click on the toolbar, maximize the presentation
		public void mouseDoubleClick(MouseEvent e) {
			
			// Ignore double-clicks if we're currently over a toolbar item
			if (isOverToolItem(e) && e.widget == upperRight) {
				return;
			}
			
			if (getSite().getState() == IStackPresentationSite.STATE_MAXIMIZED) {
				getSite().setState(IStackPresentationSite.STATE_RESTORED);
			} else {
				getSite().setState(IStackPresentationSite.STATE_MAXIMIZED);
			}
		}
	};
	
	private boolean showIconOnTabs;
	private static final int SPACING_WIDTH = 2;

	private static final String SHOWING_TOOLBAR = "showing_toolbar";

	private static final String TAG_PART = "part";
	private static final String TAG_ID = "id";
	private static final String TAG_TOOLBAR = "showing_toolbar";
	
	/**
	 * Creates a new bare-bones part presentation, given the parent composite and 
	 * an IStackPresentationSite interface that will be used to communicate with 
	 * the workbench.
	 * 
	 * @param stackSite interface to the workbench
	 */
	public WrappedTabsPartPresentation(Composite parent, 
			IStackPresentationSite stackSite, boolean showIconOnTabs) {
		super(stackSite);		
		
		this.showIconOnTabs = showIconOnTabs;
		
		// Create a top-level control for the presentation.
		presentationControl = new Composite(parent, SWT.NONE);
		
		// Add a dispose listener. This will call the presentationDisposed()
		// method when the widget is destroyed.
		presentationControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				presentationDisposed();
			}
		});
		
		upperRight = new ToolBar(presentationControl, SWT.RIGHT | SWT.FLAT);
		//initPresentationWidget(upperRight);
		
		titleIconToolbar = new ToolBar(presentationControl, SWT.RIGHT | SWT.FLAT);
		titleIcon = new ToolItem(titleIconToolbar, SWT.PUSH);
        
        titleIconToolbar.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                showPaneMenu();
            }
        });
		titleIconToolbar.addListener(SWT.MenuDetect, menuListener);
		
		titleIconToolbar.setVisible(!showIconOnTabs);
		
		toolBar = new ToolBar(presentationControl, SWT.WRAP | SWT.RIGHT | SWT.FLAT);
		toolBar.addListener(SWT.MenuDetect, menuListener);
		toolBar.addMouseListener(mouseListener);
		toolBar.setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(WrappedTabsThemeConstants.TAB_FONT));
		
		// Add drag listener to the toolbar
		PresentationUtil.addDragListener(toolBar, new Listener() {
			public void handleEvent(Event event) {
				Point loc = new Point(event.x, event.y);
				ToolItem item = toolBar.getItem(loc);
				
				if (item != null) {
					// Move the current part
					IPresentablePart draggedItem = getPartForTab(item);
					draggedItem.setFocus();
					getSite().dragStart(draggedItem, toolBar.toDisplay(loc), false);
				} else {
					// Move the stack
					getSite().dragStart(toolBar.toDisplay(loc), false);
				}
			}
			
		});
		
		presentationControl.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = presentationControl.getClientArea();			
				e.gc.setLineWidth(getBorderWidth());
				
				e.gc.setForeground(getBorderColor());
				e.gc.drawRectangle(clientArea.x, clientArea.y, clientArea.width-1, clientArea.height-1);
				Rectangle contentAreaBounds = contentArea.getBounds();
				int ypos = contentAreaBounds.y - 1;
				e.gc.drawLine(clientArea.x, ypos, clientArea.x + clientArea.width - 1, ypos);
			}
			
		});
		initPresentationWidget(presentationControl);
		
		contentArea = new ViewForm(presentationControl, SWT.FLAT);
		initPresentationWidget(contentArea);
		contentDescriptionWrapper = new Composite(contentArea, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 4;
		layout.marginHeight = 2;
		contentDescriptionWrapper.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
		
		contentDescription = new Label(contentDescriptionWrapper, SWT.NONE);
		initPresentationWidget(contentDescription);
		contentDescription.setLayoutData(data);

		clientArea = new Composite(contentArea, SWT.NONE);
		clientArea.setVisible(false);
		
		contentArea.setContent(clientArea);
		toolbarProxy = new ProxyControl(contentArea);
		
		createButtonBar();
		
		createSystemMenu();
		
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeChangeListener);
	}
	
	private void initPresentationWidget(Control toInitialize) {
		PresentationUtil.addDragListener(toInitialize, dragListener);
		toInitialize.addListener(SWT.MenuDetect, menuListener);
		toInitialize.addMouseListener(mouseListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#restoreState(org.eclipse.ui.presentations.IPresentationSerializer, org.eclipse.ui.IMemento)
	 */
	public void restoreState(IPresentationSerializer serializer, IMemento savedState) {
		IMemento[] parts = savedState.getChildren(TAG_PART);
		
		for (int idx = 0; idx < parts.length; idx++) {
			String id = parts[idx].getString(TAG_ID);
			
			if (id != null) {
				IPresentablePart part = serializer.getPart(id);
				
				if (part != null) {
					addPart(part, null);
					
					Integer hasToolbar = parts[idx].getInteger(TAG_TOOLBAR);
					showToolbar(part, hasToolbar != null && hasToolbar.intValue() != 0);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#saveState(org.eclipse.ui.presentations.IPresentationSerializer, org.eclipse.ui.IMemento)
	 */
	public void saveState(IPresentationSerializer context, IMemento memento) {
		super.saveState(context, memento);
		
		IPresentablePart[] parts = getParts();
		
		for (int i = 0; i < parts.length; i++) {
			IPresentablePart part = parts[i];

			IMemento childMem = memento.createChild(TAG_PART);
			childMem.putString(TAG_ID, context.getId(part));
			childMem.putInteger(TAG_TOOLBAR, isShowingToolbar(part) ? 1 : 0);
		}
	}
	
	public IPresentablePart[] getParts() {
		ToolItem[] items = toolBar.getItems();
		IPresentablePart[] result = new IPresentablePart[items.length];
		
		for (int idx = 0; idx < items.length; idx++) {
			ToolItem item = items[idx];
			
			IPresentablePart next = getPartForTab(item);
			
			result[idx] = next;
		}
		
		return result;
	}
	
	private final void createSystemMenu() {
		getSite().addSystemActions(systemMenuManager);
		systemMenuManager.add(new ChangeStackStateContributionItem(getSite()));
		systemMenuManager.add(new ShowToolbarContributionItem(this));
		// This example presentation includes the part list at the end of the system menu
		systemMenuManager.add(new Separator());
		systemMenuManager.add(new CloseContributionItem(this));
		systemMenuManager.add(new CloseOthersContributionItem(this));
		systemMenuManager.add(new CloseAllContributionItem(this));
		systemMenuManager.add(new PartListContributionItem(this, getSite()));
	}
	
	private void createButtonBar() {
		viewMenu = new ToolItem(upperRight, SWT.PUSH);
        upperRight.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                Point p = new Point(event.x, event.y);
                Rectangle r = viewMenu.getBounds();
                
                if (r.contains(p)) {
                    showSystemMenu();
                }
            }
        });
		viewMenu.setImage(PresentationImages.getImage(PresentationImages.VIEW_MENU));
	
		showToolbar = new ToolItem(upperRight, SWT.PUSH);        
        showToolbar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showToolbar(!isShowingToolbar());
            }
        });
        	
		if (getSite().supportsState(IStackPresentationSite.STATE_MINIMIZED)) {
			minView = new ToolItem(upperRight, SWT.PUSH);
			minView.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (current != null) {
						if (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
							getSite().setState(IStackPresentationSite.STATE_RESTORED);
						} else {
							getSite().setState(IStackPresentationSite.STATE_MINIMIZED);
						}	
					}
				}				
			});
		}
		
		close = new ToolItem(upperRight, SWT.PUSH);
		close.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (current != null) {
					getSite().close(new IPresentablePart[] {current});
				}
			}
			
		});
		close.setImage(PresentationImages.getImage(PresentationImages.CLOSE_VIEW));
		
		updateToolbarImages();
	}
	
	private void updateToolbarImages() {
		if (isShowingToolbar()) {
			showToolbar.setImage(PresentationImages.getImage(PresentationImages.HIDE_TOOLBAR));
		} else {
			showToolbar.setImage(PresentationImages.getImage(PresentationImages.SHOW_TOOLBAR));
		}

		if (minView != null) {
			String minImage = (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) ?
					PresentationImages.RESTORE_VIEW : PresentationImages.MIN_VIEW;
			minView.setImage(PresentationImages.getImage(minImage));
		}
		
		upperRight.pack(true);
		upperRight.redraw();
	}
	
	public void refreshButtonBarEnablement() {
		close.setEnabled(current != null && getSite().isCloseable(current));
		titleIcon.setEnabled(current != null && current.getMenu() != null);
		showToolbar.setEnabled(current != null && current.getToolBar() != null);
	}

	public void dispose() {
		// Dispose the main presentation widget. This will cause 
		// presentationDisposed to be called, which will do the real cleanup.
		presentationControl.dispose();
	}
	
	/**
	 * Perform any cleanup. This method should remove any listeners that were
	 * attached to other objects. This gets called when the presentation
	 * widget is disposed. This is safer than cleaning up in the dispose() 
	 * method, since this code will run even if some unusual circumstance 
	 * destroys the Shell without first calling dispose().
	 */
	protected void presentationDisposed() {
	    // Remove any listeners that were attached to any
		// global Eclipse resources. This is necessary in order to prevent
		// memory leaks.
		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeChangeListener);
	}

	public void setBounds(Rectangle bounds) {
		Rectangle newBounds = Geometry.copy(bounds);
		
		if (newBounds.width == 0) {
			// Workaround a bug in the Eclipse 3.0 release: minimized presentations will be
			// given a width of 0.
			newBounds.width = presentationControl.getBounds().width;
		}
		
		if (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
			newBounds.height = computeMinimumSize().y;
		}
		
		// Set the bounds of the presentation widge
		presentationControl.setBounds(newBounds);
		
		// Update the bounds of the currently visible part
		layout();
	}
	
	/**
	 * Lay out the presentation's widgets
	 */
	private void layout() {
		
		// Determine the inner bounds of the presentation
		Rectangle presentationClientArea = presentationControl.getClientArea();
		presentationClientArea.x += getBorderWidth();
		presentationClientArea.width -= getBorderWidth() * 2;
		presentationClientArea.y += getBorderWidth();
		presentationClientArea.height -= getBorderWidth() * 2;
		
		// Position the upper-right toolbar
		Point upperRightSize = upperRight.getSize();
		int upperRightStartX = presentationClientArea.x + presentationClientArea.width - upperRightSize.x - SPACING_WIDTH;

		Rectangle upperRightBounds = new Rectangle(upperRightStartX,
				presentationClientArea.y + SPACING_WIDTH, upperRightSize.x, upperRightSize.y);
		
		upperRight.setBounds(upperRightBounds);
		
		int tabStart = presentationClientArea.x + SPACING_WIDTH + 1;
		int verticalSpaceRequired = 0;
		if (!showIconOnTabs) {
			Point upperLeftSize;

			upperLeftSize = titleIconToolbar.getSize();
			Rectangle upperLeftBounds = new Rectangle(presentationClientArea.x + SPACING_WIDTH, 
					presentationClientArea.y + SPACING_WIDTH,
					upperLeftSize.x, upperLeftSize.y);
			
			titleIconToolbar.setBounds(upperLeftBounds);

			tabStart = upperLeftBounds.x + upperLeftBounds.width + SPACING_WIDTH;
			verticalSpaceRequired = upperLeftSize.y;
		} 
		
		int availableTabWidth = upperRightStartX - tabStart - SPACING_WIDTH;
		
		Point toolbarSize = toolBar.computeSize(availableTabWidth, SWT.DEFAULT);
		int minToolbarWidth = WrappedTabsUtil.getMaximumItemWidth(toolBar);
				
		toolBar.setBounds(tabStart,
				presentationClientArea.y + SPACING_WIDTH, 
				availableTabWidth, toolbarSize.y);
		
		verticalSpaceRequired = Math.max(verticalSpaceRequired, upperRightSize.y);
		verticalSpaceRequired = Math.max(verticalSpaceRequired, toolbarSize.y);
		
		int verticalOffset = presentationClientArea.y + verticalSpaceRequired + getBorderWidth() + 2 * SPACING_WIDTH;
		
		contentArea.setBounds(presentationClientArea.x, verticalOffset, 
				presentationClientArea.width, presentationClientArea.height - verticalOffset);
		
		if (isShowingToolbar()) {
			contentArea.setTopLeft(contentDescriptionWrapper);
			contentArea.setTopCenter(toolbarProxy.getControl());
		} else {
			contentArea.setTopLeft(null);
			contentArea.setTopCenter(null);
		}
		
		contentArea.layout();
		
		// Position the view's widgets
		if (current != null) {
			Rectangle clientRectangle = clientArea.getBounds();
			Point clientAreaStart = presentationControl.getParent().toControl(
					contentArea.toDisplay(clientRectangle.x, clientRectangle.y));
			
			current.setBounds(new Rectangle(clientAreaStart.x, 
					clientAreaStart.y,
					clientRectangle.width, 
					clientRectangle.height));
		}
		
	}

	private int getBorderWidth() {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getInt(WrappedTabsThemeConstants.BORDER_SIZE);
	}

	public Point computeMinimumSize() {
		Point minSize = new Point(100, 16);
		Point upperLeftSize = titleIconToolbar.getSize();
		Point toolBarSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		
		int BORDER_WIDTH = getBorderWidth();
		Point result = new Point(minSize.x + upperLeftSize.x + 3 * SPACING_WIDTH + 2 * BORDER_WIDTH, 
				Math.max(Math.max(upperLeftSize.y, minSize.y), toolBarSize.y) 
				+ 2 * SPACING_WIDTH + 2 * BORDER_WIDTH);
		
		return result;
	}

	public void setVisible(boolean isVisible) {
		
		// Make the presentation widget visible
		presentationControl.setVisible(isVisible);
		
		// Make the currently visible part visible
		if (current != null) {
			current.setVisible(isVisible);
			if (current.getToolBar() != null) {
				current.getToolBar().setVisible(isVisible && isShowingToolbar());
			}
		}

		if (isVisible) {
			// Restore the bounds of the currently visible part. 
			// IPartPresentations can be used by multiple StackPresentations,
			// although only one such presentation is ever visible at a time.
			// It is possible that some other presentation has changed the
			// bounds of the part since it was last visible, so we need to
			// update the part's bounds when the presentation becomes visible.
			layout();
		}
	}

	private void clearSelection() {
		// If there was an existing part selected, make it invisible
		if (current != null) {
			current.setVisible(false);
		}

		current = null;
	}
	
	public void currentPartChanged() {
		boolean layoutNeeded = false;
		
		if (titleIcon.getImage() != current.getTitleImage()) {
			titleIcon.setImage(current.getTitleImage());
			titleIcon.setDisabledImage(current.getTitleImage());
			titleIconToolbar.pack(true);
			titleIconToolbar.redraw();
			
			layoutNeeded = true;
		}
		
		if (!contentDescription.getText().equals(current.getTitleStatus())) {
			contentDescription.setText(current.getTitleStatus());
			
			layoutNeeded = true;
		}
			
		if (current.getToolBar() != null) {
			if (isShowingToolbar()) {
				current.getToolBar().setVisible(true);
				layoutNeeded = true;
			} else {
				current.getToolBar().setVisible(false);
			}
			
			toolbarProxy.setTargetControl(current.getToolBar());
		}
		
		if (layoutNeeded) {
			layout();
		}
	}
	
	public void selectPart(IPresentablePart toSelect) {
		// Ignore redundant selections
		if (toSelect == current) {
			return;
		}
		
		clearSelection();
		
		// Select the new part
		current = toSelect;
		
		// Ordering is important here. We need to make the part
		// visible before updating its bounds, or the call to setBounds
		// may be ignored.
		
		if (current != null) {
			// Make the newly selected part visible
			current.setVisible(true);
			
			ToolItem[] items = toolBar.getItems();
			
			for (int idx = 0; idx < items.length; idx++) {
				ToolItem item = items[idx];
				
				item.setSelection(getPartForTab(item) == current);
			}
			
			currentPartChanged();
		}
		
		refreshButtonBarEnablement();
		updateToolbarImages();
		
		// Update the bounds of the newly selected part
		layout();
	}

	public Control[] getTabList(IPresentablePart part) {
		return new Control[] {part.getControl()};
	}

	public Control getControl() {
		return presentationControl;
	}

	private int indexOf(IPresentablePart part) {
		ToolItem item = getTab(part);
		
		if (item == null) {
			return -1;
		}
		
		return toolBar.indexOf(item); 
	}
	
	public void addPart(IPresentablePart newPart, Object cookie) {
		if (getTab(newPart) != null) {
			return;
		}
		
		int position = toolBar.getItemCount();
		
		// If this part is being added due to a drag/drop operation,
		// determine the correct insertion position
		if (cookie instanceof DropLocation) {
			DropLocation location = (DropLocation)cookie;
			
			position = indexOf(location.part);
			
			// If we can't find the tab, then fall back to the
			// insertionPosition field
			if (position == -1) {
				position = location.insertionPosition;
			} else {
				if (!location.before) {
					position++;
				}
			}
		}
		
		// Ignore the cookie for now, since we don't support drag-and-drop yet.
		ToolItem toolItem = new ToolItem(toolBar, SWT.RADIO, position);
		
		// Attach the newPart pointer to the ToolItem. This is used for getPartForTab
		// to determine which part is associated with the tool item
		toolItem.setData(PART_DATA, newPart);
		
		// Attach a property change listener to the part. This will update the ToolItem
		// to reflect changes in the part.
		newPart.addPropertyListener(childPropertyChangeListener);
		
		// Attach a dispose listener to the item. This removes the above property
		// change listener from the part when the item is destroyed. This prevents
		// memory leaks.
		toolItem.addDisposeListener(tabDisposeListener);
		
		// Listen to selection events in the new tool item
		toolItem.addSelectionListener(tabItemSelectionAdapter);
		
		// Initialize the tab for this part
		initTab(toolItem, newPart);
		
		layout();
		
		toolBar.layout(true);
		presentationControl.redraw();
	}

	protected void initTab(ToolItem item, IPresentablePart part) {
		String tabName = getTabText(part);
		
		if (!tabName.equals(item.getText())) {
			item.setText(tabName);
		}
		
		if (!(part.getTitleToolTip().equals(item.getToolTipText()))) {
			item.setToolTipText(part.getTitleToolTip());
		}

		if (showIconOnTabs && part.getTitleImage() != item.getImage()) {
			item.setImage(part.getTitleImage());
		}
		
	}
	
	/**
	 * Returns the decorated tab text for the given part. By default, we attach 
	 * a star to indicate dirty tabs.
	 * 
	 * @param part part whose text is being computed
	 * @return the decorated tab text for the given part
	 */
	protected String getTabText(IPresentablePart part) {
		String result = part.getName();
		
		if (part.isDirty()) {
			result = "*" + result;
		}
		
		return result;
	}
	
	/**
	 * Removes the given part from the stack.
	 * 
	 * @param oldPart the part to remove (not null)
	 */
	public void removePart(IPresentablePart oldPart) {
		// If we're removing the currently selected part, clear the selection
		if (oldPart == current) {
			clearSelection();
			refreshButtonBarEnablement();
		}
		
		ToolItem item = getTab(oldPart);
		
		// Don't need to do anything if the part has already been removed
		if (item == null) {
			return;
		}
		
		// Dispose the tab. The dispose listener on the item
		// will handle all the cleanup.
		item.dispose();
		
		layout();
		
		presentationControl.redraw();
	}

	/**
	 * Called whenever a property changes for one of the parts in this
	 * presentation. 
	 * 
	 * @param part
	 * @param property
	 */
	protected void childPropertyChanged(IPresentablePart part, int property) {
		ToolItem toolItem = getTab(part);
		
		// If there is no tab for this part, just ignore the property change
		if (toolItem == null) {
			return;
		}
		
		initTab(toolItem, part);
		
		if (part == current) {
			currentPartChanged();
		}
	}

	/**
	 * Returns the tab associated with the given part or null if none 
	 * 
	 * @param part the part to check for
	 * @return the tab associated with the given part or null if none
	 */
	protected final ToolItem getTab(IPresentablePart part) {
		ToolItem[] items = toolBar.getItems();
		
		for (int idx = 0; idx < items.length; idx++) {
			ToolItem item = items[idx];
			
			if (getPartForTab(item) == part) {
				return item;
			}
		}
			
		return null;
	}
	
	/**
	 * Returns the part associated with the given tab, or null if none.
	 * 
	 * @param item the tab to query
	 * @return the part associated with the given tab or null if none
	 */
	protected final IPresentablePart getPartForTab(ToolItem item) {
		return (IPresentablePart)item.getData(PART_DATA);
	}
	
	public StackDropResult dragOver(Control currentControl, Point location) {		
		Point localCoordinates = toolBar.toControl(location);
		
		// Ignore drag operations that aren't on top of the toolbar we're using
		// for tabs.
		if (toolBar.getClientArea().contains(localCoordinates)) {
			DropLocation dropLocation = new DropLocation();
			
			ToolItem item = toolBar.getItem(localCoordinates);
			
			if (item == null) {
				item = toolBar.getItem(toolBar.getItemCount() - 1);
			}
			
			Rectangle itemBounds = item.getBounds();
			dropLocation.before = (localCoordinates.x - itemBounds.x < itemBounds.width / 2);
			dropLocation.part = getPartForTab(item);
			// Also store the current index of the part we're dragging over. We will use
			// the index if the part no longer exists at the time the drop occurs (ie:
			// if we're dragging an item over itself)
			dropLocation.insertionPosition = toolBar.indexOf(item);
			
			Point displayCoordinates = toolBar.toDisplay(itemBounds.x, itemBounds.y);
			
			Rectangle bounds = new Rectangle(displayCoordinates.x, displayCoordinates.y, 
					4, itemBounds.height);
			if (!dropLocation.before) {
				bounds.x += itemBounds.width;
			}
			
			return new StackDropResult(bounds, dropLocation);
		}
		
		return null;
	}
	
	public void setActive(int newState) {
		activeFocus = (newState == AS_ACTIVE_FOCUS);
		presentationControl.redraw();
	}
	
	public void setState(int state) {
		updateToolbarImages();
	}
	
	public void showPaneMenu(Point location) {
		if (current == null) {
			return;
		}
		
		IPartMenu menu = current.getMenu();
		
		if (menu == null) {
			return;
		}

		menu.showMenu(location);
	}
	
	public void showPaneMenu() {	
		Rectangle bounds = titleIconToolbar.getBounds();
		Point location = titleIconToolbar.getParent().toDisplay(bounds.x, bounds.y + bounds.height);
		
		showPaneMenu(location);
	}
	
	public void showSystemMenu() {
		Rectangle bounds = viewMenu.getBounds();
		Point displayPos = viewMenu.getParent().toDisplay(bounds.x, bounds.y + bounds.height);
    	
		showSystemMenu(displayPos);
	}
	
	public void showSystemMenu(Point displayPos) {
		Menu aMenu = systemMenuManager.createContextMenu(presentationControl);
		systemMenuManager.update(true);
		aMenu.setLocation(displayPos.x, displayPos.y);
		aMenu.setVisible(true);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showPartList()
	 */
	public void showPartList() {
		
		// The part list opens when the user presses ctrl-e to open the list
		// of editors. In this presentation, the part list is part of the system menu,
		// so opening the part list is equivalent to opening the system menu.
		showSystemMenu();
	}

	protected void showToolbar(IPresentablePart part, boolean shouldShow) {
		
		if (shouldShow != isShowingToolbar(part)) {
			ToolItem tab = getTab(part);
			tab.setData(SHOWING_TOOLBAR, shouldShow ? SHOWING_TOOLBAR : null);
			
			if (part == current) {
				Control toolbar = part.getToolBar();
				if (toolbar != null) {
					toolbar.setVisible(shouldShow);
				}
				
				layout();
		
				updateToolbarImages();
				
				if (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
					getSite().setState(IStackPresentationSite.STATE_RESTORED);
				}
			}
		}
	}
	
	/**
	 * @param selection
	 */
	public void showToolbar(boolean selection) {
		if (current != null) {
			showToolbar(current, selection);
		}
	}
	
	public IPresentablePart getCurrent() {
		return current;
	}
	
	private boolean isShowingToolbar(IPresentablePart part) {
		ToolItem tab = getTab(part);
		return tab.getData(SHOWING_TOOLBAR) != null;
	}
	
	public boolean isShowingToolbar() {
		if (current == null) {
			return false;
		}
		
		return isShowingToolbar(current);		
	}

	/**
	 * @param parts
	 */
	public void close(IPresentablePart[] parts) {
		getSite().close(parts);
	}
	
	private Color getBorderColor() {
		ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		if(activeFocus) {
			return current.getColorRegistry().get(WrappedTabsThemeConstants.BORDER_COLOR_FOCUS);
		} else {
			return current.getColorRegistry().get(WrappedTabsThemeConstants.BORDER_COLOR_NOFOCUS);
		}
	}
}
