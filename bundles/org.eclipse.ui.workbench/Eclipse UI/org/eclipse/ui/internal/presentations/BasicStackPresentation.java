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
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Base class for StackPresentations that display IPresentableParts in a CTabFolder. 
 * 
 * @since 3.0
 */
public class BasicStackPresentation extends StackPresentation {
	
	private PaneFolder tabFolder;
	private IPresentablePart current;
	private boolean activeState = false;
	private MenuManager systemMenuManager = new MenuManager();
	private Label titleLabel;
	private Listener dragListener;
	
	/**
	 * While we are dragging a tab from this folder, this holdes index of the tab
	 * being dragged. Set to -1 if we are not currently dragging a tab from this folder.
	 */
	private int dragStart = -1;
		
	private final static String TAB_DATA = BasicStackPresentation.class.getName() + ".partId"; //$NON-NLS-1$
	
	private PaneFolderButtonListener buttonListener = new PaneFolderButtonListener() {
		public void stateButtonPressed(int buttonId) {
			getSite().setState(buttonId);
		}

		public void closeButtonPressed(CTabItem item) {
			IPresentablePart part = getPartForTab(item);
			
			getSite().close(part);		
		}
	};
	
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Control) {
				Control ctrl = (Control)e.widget;
				
				Point globalPos = ctrl.toDisplay(new Point(e.x, e.y));
							
				// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
				// Else, set focus.
				CTabItem newItem = tabFolder.getItem(tabFolder.getControl().toControl(globalPos));
				if (newItem != null) {
					CTabItem oldItem = tabFolder.getSelection();
					if (newItem != oldItem)
						return;
				}
				if (current != null) {
					current.setFocus();
				}
			}
		}
		
		public void mouseDoubleClick(MouseEvent e) {
			if (getSite().getState() == IStackPresentationSite.STATE_MAXIMIZED) {
				getSite().setState(IStackPresentationSite.STATE_RESTORED);
			} else {
				getSite().setState(IStackPresentationSite.STATE_MAXIMIZED);
			}
		}
	};
	
	private Listener menuListener = new Listener() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			Point pos = new Point(event.x, event.y);

			showSystemMenu(pos);
		}
	};
	
	private Listener selectionListener = new Listener() {
		public void handleEvent(Event e) {
			IPresentablePart item = getPartForTab((CTabItem)e.item);
			
			if (item != null) {
				getSite().selectPart(item);
			}
		}
	};
	
	private Listener resizeListener = new Listener() {
		public void handleEvent(Event e) {
			//setControlSize();
		}
	};
	
	private IPropertyListener childPropertyChangeListener = new IPropertyListener() {
		public void propertyChanged(Object source, int property) {
			if (source instanceof IPresentablePart) {
				IPresentablePart part = (IPresentablePart) source;
				childPropertyChanged(part, property);
			}
		}	
	};
	
	private DisposeListener tabDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (e.widget instanceof CTabItem) {
				CTabItem item = (CTabItem)e.widget;
				
				IPresentablePart part = getPartForTab(item);
				
				part.removePropertyListener(childPropertyChangeListener);
			}
		}
	};
	private ToolBar viewToolBar;

	public BasicStackPresentation(PaneFolder control, IStackPresentationSite stackSite) {
	    super(stackSite);
		tabFolder = control;
		
		tabFolder.setMinimizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MINIMIZED));
		tabFolder.setMaximizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
				
		titleLabel = new Label(tabFolder.getControl(), SWT.WRAP);
		titleLabel.setVisible(false);
		titleLabel.moveAbove(null);
		
		ColorSchemeService.setViewTitleFont(this, titleLabel);
		
		viewToolBar = new ToolBar(control.getControl(), SWT.HORIZONTAL 
				| SWT.FLAT | SWT.WRAP);
		viewToolBar.moveAbove(null);
		
		ToolItem pullDownButton = new ToolItem(viewToolBar, SWT.PUSH);
		//				Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		Image hoverImage =
			WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
		pullDownButton.setDisabledImage(hoverImage); // TODO: comment this out?
		// PR#1GE56QT - Avoid creation of unnecessary image.
		pullDownButton.setImage(hoverImage);
		pullDownButton.setToolTipText(WorkbenchMessages.getString("Menu")); //$NON-NLS-1$
		pullDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showPaneMenu();
			}
		});
		
		// listener to switch between visible tabItems
		tabFolder.getControl().addListener(SWT.Selection, selectionListener);

		// listener to resize visible components
		tabFolder.getControl().addListener(SWT.Resize, resizeListener);

		// listen for mouse down on tab to set focus.
		tabFolder.getControl().addMouseListener(mouseListener);
		
		tabFolder.getControl().addListener(SWT.MenuDetect, menuListener);
		
		tabFolder.addButtonListener(buttonListener);
		
		dragListener = new Listener() {
			public void handleEvent(Event event) {
				
				Point localPos = new Point(event.x, event.y);
				// Ignore drags unless they are on the title area
				if ((tabFolder.getControl().getStyle() & SWT.TOP) != 0) {
					if (localPos.y > tabFolder.getTabHeight()) {
						return;
					}
				} else {
					if (localPos.y < tabFolder.getControl().getBounds().height - tabFolder.getTabHeight()) {
						return;
					}
				}
				
				CTabItem tabUnderPointer = tabFolder.getItem(localPos);
		
				if (tabUnderPointer == null) {
					getSite().dragStart(tabFolder.getControl().toDisplay(localPos), false);
					return;
				}

				IPresentablePart part = getPartForTab(tabUnderPointer); 
				
				if (getSite().isMoveable(part)) {
					dragStart = tabFolder.indexOf(tabUnderPointer);
					getSite().dragStart(part, 
						tabFolder.getControl().toDisplay(localPos), false);
					dragStart = -1;
				}
			}
		};
		
		PresentationUtil.addDragListener(tabFolder.getControl(), dragListener);
		
		// Uncomment to allow dragging from the title label
//		PresentationUtil.addDragListener(titleLabel, new Listener() {
//			public void handleEvent(Event event) {
//				if (layout.isTrimOnTop()) {
//					Point localPos = new Point(event.x, event.y);
//					getSite().dragStart(titleLabel.toDisplay(localPos), false);
//				}
//			}
//		});

		titleLabel.addMouseListener(mouseListener);
		
		// Compute the tab height
		int tabHeight = viewToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		// Enforce a minimum tab height
		if (tabHeight < 20) {
			tabHeight = 20;
		}
		tabFolder.setTabHeight(tabHeight);
		
		populateSystemMenu(systemMenuManager);		
	}

	/**
	 * @param systemMenuManager2
	 */
	private void populateSystemMenu(IMenuManager systemMenuManager) {

		systemMenuManager.add(new GroupMarker("misc"));
		systemMenuManager.add(new GroupMarker("restore"));
		systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuRestore(getSite())));
		

		systemMenuManager.add(new SystemMenuMove(getSite(), getPaneName()));
		systemMenuManager.add(new GroupMarker("size"));
		systemMenuManager.add(new GroupMarker("state"));
		systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuMinimize(getSite())));
		
		systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuMaximize(getSite())));
		systemMenuManager.add(new Separator("close"));
		systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuClose(getSite())));
		
		getSite().addSystemActions(systemMenuManager);
	}
	
	protected String getPaneName() {
		return "&Pane";
	}

	/**
	 * Displays the view menu as a popup
	 */
	public void showPaneMenu() {
		IPartMenu menu = getPartMenu();
		
		if (menu != null) {
			Rectangle bounds = DragUtil.getDisplayBounds(viewToolBar);
			menu.showMenu(new Point(bounds.x, bounds.y + bounds.height));
		}
	}

	/**
	 * Returns the currently selected part, or <code>null</code>.
	 * 
	 * @return the currently selected part, or <code>null</code>
	 */
	protected IPresentablePart getCurrent() {
	    return current;
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
	
		CTabItem[] items = tabFolder.getItems();
		
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
	protected final CTabItem getTab(IPresentablePart part) {
		CTabItem[] items = tabFolder.getItems();
		
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
		
		CTabItem tab = getTab(part);
		initTab(tab, part);
		
		switch (property) {
		 case IPresentablePart.PROP_BUSY:
			break;
	     case IPresentablePart.PROP_HIGHLIGHT_IF_BACK:
	     	FontRegistry registry = 
			    PlatformUI.getWorkbench().
			    	getThemeManager().getCurrentTheme().
			    		getFontRegistry();
	     	
	       	if(!getCurrent().equals(part))//Set bold if it does currently have focus
				tab.setFont(registry.getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT));
	        break;
		 case IPresentablePart.PROP_TOOLBAR:
		 case IPresentablePart.PROP_PANE_MENU:
		 case IPresentablePart.PROP_TITLE:
		 	setControlSize();
		 	break;
		}
	}

	protected final IPresentablePart getPartForTab(CTabItem item) {
		IPresentablePart part = (IPresentablePart)item.getData(TAB_DATA);
		
		return part;
	}
	
	/**
	 * Returns the underlying tab folder for this presentation.
	 * 
	 * @return
	 */
	protected PaneFolder getTabFolder() {
		return tabFolder;
	}
	
	/**
	 * Returns true iff the underlying tab folder has been disposed.
	 * 
	 * @return
	 */
	public boolean isDisposed() {
		return tabFolder == null || tabFolder.isDisposed();
	}
	
	/**
	 * Sets the gradient for the selected tab 
	 * 
	 * @param fgColor
	 * @param bgColors
	 * @param percentages
	 * @param vertical
	 */
	public void drawGradient(Color fgColor, Color [] bgColors, int [] percentages, boolean vertical) {
		tabFolder.setSelectionForeground(fgColor);
		tabFolder.setSelectionBackground(bgColors, percentages, vertical);	
	}
	
	public boolean isActive() {
		return activeState;
	}
	
	protected String getCurrentTitle() {
		if (current == null) {
			return "";
		} 
		
		String result = current.getTitleStatus();
		
		return result;
	}
	
	/**
	 * Set the size of a page in the folder.
	 * 
	 * TODO: Kim here...I had to make this public so that the when the font
	 * was updated via the color scheme service it could relayout the 
	 * presentation... calling control.getLayout() doesn't do the trick.
	 */
	public void setControlSize() {
		// Set up the top-right controls
		//List topRight = new ArrayList(3);
		
		String currentTitle = getCurrentTitle();
		
		if (!currentTitle.equals(Util.ZERO_LENGTH_STRING)) {
			tabFolder.setTopLeft(titleLabel);
			titleLabel.setText(currentTitle);
			titleLabel.setVisible(true);
		} else {
			tabFolder.setTopLeft(null);
			titleLabel.setVisible(false);
		}
		
		Control currentToolbar = getCurrentToolbar(); 
		tabFolder.setTopCenter(currentToolbar);
			
		IPartMenu partMenu = getPartMenu();
		
		if (partMenu != null) {
			tabFolder.setTopRight(viewToolBar);
			viewToolBar.setVisible(true);
		} else {
			tabFolder.setTopRight(null);
			viewToolBar.setVisible(false);
		}

		tabFolder.layout(true);

		if (current != null) {
			Rectangle clientArea = tabFolder.getClientArea();
			Rectangle bounds = tabFolder.getControl().getBounds();
			clientArea.x += bounds.x;
			clientArea.y += bounds.y;
			
			current.setBounds(clientArea);
		}

	}
	
	/**
	 * Returns the IPartMenu for the currently selected part, or null if the current
	 * part does not have a menu.
	 * 
	 * @return the IPartMenu for the currently selected part or null if none
	 */
	protected IPartMenu getPartMenu() {
		IPresentablePart part = getCurrentPart();		
		if (part == null) {
			return null;
		}

		return part.getMenu();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#dispose()
	 */
	public void dispose() {
		if (isDisposed()) {
			return;
		}
		PresentationUtil.removeDragListener(tabFolder.getControl(), dragListener);
		
		systemMenuManager.dispose();
		systemMenuManager.removeAll();
		tabFolder.getControl().dispose();
		tabFolder = null;
		
		titleLabel.dispose();
		titleLabel = null;
		
		viewToolBar.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
	 */
	public void setActive(boolean isActive) {
		activeState = isActive;
	}
	
	private CTabItem createPartTab(IPresentablePart part, int tabIndex) {
		CTabItem tabItem;

		int style = SWT.NONE;
		
		if (getSite().isCloseable(part)) {
			style |= SWT.CLOSE;
		}
		
		tabItem = tabFolder.createItem(style, tabIndex);
				
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
	protected void initTab(CTabItem tabItem, IPresentablePart part) {
		tabItem.setText(part.getName());
		
		tabItem.setImage(part.getTitleImage());
		
        String toolTipText = part.getTitleToolTip();
        if (!toolTipText.equals(Util.ZERO_LENGTH_STRING)) {
        	tabItem.setToolTipText(toolTipText);
        }
		
		FontRegistry registry = 
		    PlatformUI.getWorkbench().
		    	getThemeManager().getCurrentTheme().
		    		getFontRegistry();
		
		if(part.isBusy())
			tabItem.setFont(registry.getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT));
		else{
			tabItem.setFont(registry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
		}			

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#addPart(org.eclipse.ui.internal.skins.IPresentablePart, org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void addPart(IPresentablePart newPart, IPresentablePart position) {
		int idx = indexOf(position);
		
		createPartTab(newPart, idx);
		
		setControlSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#removePart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
	    if (current == oldPart)
	        current = null;
	    
		CTabItem item = getTab(oldPart);
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
			setControlSize();
			current.setVisible(true);			
		}
	}
	
	public IPresentablePart getCurrentPart() {
		return current;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		tabFolder.getControl().setBounds(bounds);
		setControlSize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		return Geometry.getSize(tabFolder.computeTrim(0,0,0,0));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
		if (current != null) {
			current.setVisible(isVisible);
		}
		tabFolder.getControl().setVisible(isVisible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
	 */
	public void setState(int state) {
		tabFolder.setState(state);
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
	protected void showSystemMenu(Point point) {
		Menu aMenu = systemMenuManager.createContextMenu(tabFolder.getControl().getParent());
		systemMenuManager.update(true);
		aMenu.setLocation(point.x, point.y);
		aMenu.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#getControl()
	 */
	public Control getControl() {
		return tabFolder.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
	 */
	public StackDropResult dragOver(Control currentControl, Point location) {
		// Determine which tab we're currently dragging over
		Point localPos = tabFolder.getControl().toControl(location);
		final CTabItem tabUnderPointer = tabFolder.getItem(localPos);
		
		// This drop target only deals with tabs... if we're not dragging over
		// a tab, exit.
		if (tabUnderPointer == null) {
			return null;
		}
		
		int dragOverIndex = tabFolder.indexOf(tabUnderPointer); 
		
		IPresentablePart position = null;
		boolean dropNext = dragStart >= 0 && dragStart < dragOverIndex;
		if (dropNext) {
			int idx = dragOverIndex + 1;
			if (idx < tabFolder.getItemCount()) {
				position = getPartForTab(tabFolder.getItem(idx));
			}
		} else {
			position = getPartForTab(tabUnderPointer);
		}
		
		return new StackDropResult(Geometry.toDisplay(tabFolder.getControl(), tabUnderPointer.getBounds()),
			position);
	}
		
	/**
	 * Returns the toolbar control for the currently selected part, or null if none (not 
	 * all parts have a toolbar).
	 * 
	 * @return the current toolbar or null if none
	 */
	protected Control getCurrentToolbar() {
		IPresentablePart part = getCurrentPart();		
		if (part == null) {
			return null;
		}

		return part.getToolBar();
	}
	
	/**
	 * Use this method instead of setting the background colours directly on the CTabFolder.
	 * This will cause the correct colours to be applied to the tab folder 
	 * 
	 * @param gradientStart
	 * @param gradientEnd
	 * @param background
	 */
	public void setBackgroundColors(Color gradientStart, Color gradientEnd, Color background) {
		tabFolder.setBackgroundColors(gradientStart, gradientEnd, background);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
		IPresentablePart part = getCurrentPart();
		if (part != null) {
			Rectangle bounds = DragUtil.getDisplayBounds(tabFolder.getControl());
			
			int idx = tabFolder.getSelectionIndex();
			if (idx > -1) {
				CTabItem item = tabFolder.getItem(idx);
				Rectangle itemBounds = item.getBounds();
				
				bounds.x += itemBounds.x;
				bounds.y += itemBounds.y;
			}
			
			Point location = new Point(bounds.x, bounds.y + tabFolder.getTabHeight());
			showSystemMenu(location);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#getTabList(IPresentablePart)
	 */
	public Control[] getTabList(IPresentablePart part) {
		ArrayList list = new ArrayList();
		if (tabFolder.getTabPosition() == SWT.BOTTOM) {
			if (part.getToolBar() != null) list.add(part.getToolBar());
			if (part.getControl() != null) list.add(part.getControl());
			if (getTabFolder() != null) list.add(getTabFolder().getControl());
		}
		else {
			if (getTabFolder() != null) list.add(getTabFolder().getControl());
			if (part.getToolBar() != null) list.add(part.getToolBar());
			if (part.getControl() != null) list.add(part.getControl());
		}
		return (Control[]) list.toArray(new Control[list.size()]);
	}

}
