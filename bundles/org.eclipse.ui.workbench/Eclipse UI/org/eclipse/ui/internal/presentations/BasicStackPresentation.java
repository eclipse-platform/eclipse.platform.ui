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
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
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
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
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
	
	private CTabFolder tabFolder;
	private IPresentablePart current;
	private boolean activeState = false;
	private int tabPosition;
	private MenuManager systemMenuManager = new MenuManager();
	private TabFolderLayout layout;
	private Label titleLabel;
	private Listener dragListener;
	
	// Current tab colours
	private Color backgroundGradientStart;
	private Color backgroundGradientEnd;
	private Color background;
	
	/**
	 * State of the presentation at the last mousedown event. This is used to prevent
	 * a mouseup over the minimize or maximize buttons from undoing a state change 
	 * that was caused by the mousedown.
	 */
	private int mousedownState = -1;
	
	private final static String TAB_DATA = BasicStackPresentation.class.getName() + ".partId"; //$NON-NLS-1$
	
	private CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
		public void minimize(CTabFolderEvent event) {
			event.doit = false;
			if (mousedownState == getSite().getState()) {
				getSite().setState(IStackPresentationSite.STATE_MINIMIZED);
			}
		}
		
		public void restore(CTabFolderEvent event) {
			event.doit = false;
			getSite().setState(IStackPresentationSite.STATE_RESTORED);
		}
		
		public void maximize(CTabFolderEvent event) {
			event.doit = false;
			getSite().setState(IStackPresentationSite.STATE_MAXIMIZED);
		}
	};
	
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Control) {
				Control ctrl = (Control)e.widget;
				
				Point globalPos = ctrl.toDisplay(new Point(e.x, e.y));
				
				mousedownState = getSite().getState();
				
				// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
				// Else, set focus.
				CTabItem newItem = tabFolder.getItem(tabFolder.toControl(globalPos));
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
			CTabItem item = tabFolder.getItem(pos);
			IPresentablePart part = null;
			if (item != null) {
				part = getPartForTab(item);
			}
			showSystemMenu(part, pos);
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
	
	public int getTopTrimStart() {
		return layout.getTrimStart();
	}
	
	private Listener resizeListener = new Listener() {
		public void handleEvent(Event e) {
			setControlSize();
		}
	};
	
	private CTabFolder2Adapter closeListener = new CTabFolder2Adapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
		 */
		public void close(CTabFolderEvent event) {
			event.doit = false;
			IPresentablePart part = getPartForTab((CTabItem)event.item);
			
			getSite().close(part);
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

	public BasicStackPresentation(CTabFolder control, IStackPresentationSite stackSite) {
	    super(stackSite);
		tabFolder = control;
		
		layout = new TabFolderLayout(tabFolder);

		titleLabel = new Label(control.getParent(), SWT.WRAP);
		titleLabel.setVisible(false);
		
		viewToolBar = new ToolBar(control.getParent(), SWT.HORIZONTAL 
				| SWT.FLAT | SWT.WRAP);
		
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
		tabFolder.addListener(SWT.Selection, selectionListener);

		// listener to resize visible components
		tabFolder.addListener(SWT.Resize, resizeListener);

		// listen for mouse down on tab to set focus.
		tabFolder.addMouseListener(mouseListener);
		
		tabFolder.addListener(SWT.MenuDetect, menuListener);

		tabFolder.addCTabFolder2Listener(closeListener);
		
		tabFolder.addCTabFolder2Listener(expandListener);
		
		dragListener = new Listener() {
			public void handleEvent(Event event) {
				
				Point localPos = new Point(event.x, event.y);
				// Ignore drags unless they are on the title area
				if ((tabFolder.getStyle() & SWT.TOP) != 0) {
					if (localPos.y > tabFolder.getTabHeight()) {
						return;
					}
				} else {
					if (localPos.y < tabFolder.getBounds().height - tabFolder.getTabHeight()) {
						return;
					}
				}
				
				CTabItem tabUnderPointer = tabFolder.getItem(localPos);
		
				if (tabUnderPointer == null) {
					getSite().dragStart(tabFolder.toDisplay(localPos), false);
					return;
				}

				IPresentablePart part = getPartForTab(tabUnderPointer); 
				
				if (getSite().isMoveable(part)) {
					getSite().dragStart(part, 
						tabFolder.toDisplay(localPos), false);
				}
			}
		};
		
		PresentationUtil.addDragListener(tabFolder, dragListener);
		
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
		int toolbarHeight = viewToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int tabHeight = toolbarHeight + layout.differenceBetweenTabHeightAndTrimRegion();
		// Enforce a minimum tab height
		if (tabHeight < 20) {
			tabHeight = 20;
		}
		tabFolder.setTabHeight(tabHeight);
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
			FontRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
			if(part.isBusy())
				tab.setFont(registry.getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT));
			else{
				if(getCurrent().equals(part))//Set bold if it does not already have focus
					tab.setFont(registry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
				else
					tab.setFont(registry.getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT));
			}			
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
	protected CTabFolder getTabFolder() {
		return tabFolder;
	}
	
	public void setTabPosition(int position) {
		tabPosition = position;
		getTabFolder().setTabPosition(tabPosition);
	}
	
	public int getTabPosition() {
		return tabPosition;
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
		
		String result = current.getTitle();
		if (result.equals(current.getName())) {
			return "";
		}
		
		return result + " ";
	}
	
	/**
	 * Set the size of a page in the folder.
	 */
	protected void setControlSize() {
		// Set up the top-right controls
		List topRight = new ArrayList(3);
		
		Control currentTop = tabFolder;
		
		String currentTitle = getCurrentTitle();
		
		if (!currentTitle.equals("")) {
			titleLabel.moveAbove(currentTop);
			currentTop = titleLabel;
			topRight.add(titleLabel);
			layout.setNumLeftAligned(1);
			titleLabel.setText(currentTitle);
			titleLabel.setVisible(true);
			titleLabel.setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
					.getFontRegistry().get(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT));
		} else {
			titleLabel.setVisible(false);
			layout.setNumLeftAligned(0);
		}
		
		Control toolbar = getCurrentToolbar();
		
		if (toolbar != null) {
			topRight.add(toolbar);
		}
		
		IPartMenu partMenu = getPartMenu();
		
		if (partMenu != null) {
			viewToolBar.moveAbove(currentTop);
			currentTop = viewToolBar;
			topRight.add(viewToolBar);
			viewToolBar.setVisible(true);
		} else {
			viewToolBar.setVisible(false);
		}
		
		Control[] controls = (Control[])topRight.toArray(new Control[topRight.size()]);
		
		layout.setTopRight(controls);
		
		if (current == null || tabFolder == null)
			return;

		layout.layout();
		
		current.setBounds(layout.getClientBounds());
		
		updateTrimColours();
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
	
	/**
	 * Update the colours of the trim widgets based on whether they are
	 * currently in the title bar or below it.
	 */
	protected void updateTrimColours() {
		Color background = layout.isTrimOnTop() ? backgroundGradientEnd : this.background ;
		
		Control[] trim = layout.getTopRight();
		
		for (int idx = 0; idx < trim.length; idx++) {
			Control next = trim[idx];
			
			Color nextCol = next.getBackground();
			
			if (nextCol != background) {
				next.setBackground(background);
			}
		}
		
		updateBackgroundColors();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#dispose()
	 */
	public void dispose() {
		if (isDisposed()) {
			return;
		}
		PresentationUtil.removeDragListener(tabFolder, dragListener);
		
		systemMenuManager.dispose();
		tabFolder.dispose();
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
		
		tabItem = new CTabItem(tabFolder, style, tabIndex);
				
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#addPart(org.eclipse.ui.internal.skins.IPresentablePart, org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void addPart(IPresentablePart newPart, IPresentablePart position) {
		int idx = indexOf(position);
		
		createPartTab(newPart, idx);		
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
		tabFolder.setBounds(bounds);
		setControlSize();
		viewToolBar.moveAbove(getControl());
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
		tabFolder.setVisible(isVisible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
	 */
	public void setState(int state) {
		tabFolder.setMinimized(state == IStackPresentationSite.STATE_MINIMIZED);
		tabFolder.setMaximized(state == IStackPresentationSite.STATE_MAXIMIZED);
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
	protected void showSystemMenu(IPresentablePart part, Point point) {
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
		Point localPos = tabFolder.toControl(location);
		final CTabItem tabUnderPointer = tabFolder.getItem(localPos);
		
		// This drop target only deals with tabs... if we're not dragging over
		// a tab, exit.
		if (tabUnderPointer == null) {
			return null;
		}
		
		return new StackDropResult(Geometry.toDisplay(tabFolder, tabUnderPointer.getBounds()),
			tabFolder.indexOf(tabUnderPointer));
	}
	
	/**
	 * Returns the current percentage for the background gradient (this is the percentage
	 * of the way along the tab folder where the toolbar begins) 
	 * 
	 * @return a percentage (0 - 100)
	 */
	public int getGradientPercentage() {
		Rectangle clientBounds = getTabFolder().getBounds();
		
		int topTrimStart = getTopTrimStart(); 
		
		int percentage = clientBounds.width == 0 ? 100 : Math.min(100, 
				100 * topTrimStart / clientBounds.width);
		
		if (percentage < 0) {
			percentage = 0;
		}
		
		return percentage;	    
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
		backgroundGradientStart = gradientStart;
		backgroundGradientEnd = gradientEnd;
		this.background = background;
		updateBackgroundColors();
		
		updateTrimColours();
	}
	
	/**
	 * Causes the current background colours to be reapplied to the underlying CTabFolder. This
	 * should be invoked whenever the colours change or when the toolbars may have been resized
	 * or repositioned, since the gradient percentage is computed as a function of the toolbar
	 * position.
	 */
	protected void updateBackgroundColors() {
		Color [] c = new Color[3];
		c[0] = backgroundGradientStart;
		c[1] = backgroundGradientEnd;
		c[2] = c[1];
	
		int[] percents = new int[] {getGradientPercentage(), 100};				
	   
		getTabFolder().setBackground(c, percents, false);
		getTabFolder().setBackground(background);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
		IPresentablePart part = getCurrentPart();
		if (part != null) {
			Rectangle bounds = DragUtil.getDisplayBounds(tabFolder);
			
			int idx = tabFolder.getSelectionIndex();
			if (idx > -1) {
				CTabItem item = tabFolder.getItem(idx);
				Rectangle itemBounds = item.getBounds();
				
				bounds.x += itemBounds.x;
				bounds.y += itemBounds.y;
			}
			
			Point location = new Point(bounds.x, bounds.y + tabFolder.getTabHeight());
			showSystemMenu(part, location);
		}
	}
}
