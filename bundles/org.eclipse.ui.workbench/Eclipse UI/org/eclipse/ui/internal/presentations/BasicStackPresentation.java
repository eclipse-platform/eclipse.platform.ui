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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
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
			mousedownState = getSite().getState();
			
			// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
			// Else, set focus.
			CTabItem newItem = tabFolder.getItem(new Point(e.x, e.y));
			if (newItem != null) {
				CTabItem oldItem = tabFolder.getSelection();
				if (newItem != oldItem)
					return;
			}
			if (current != null) {
				current.setFocus();
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
			showPaneMenu(part, pos);
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

	public BasicStackPresentation(CTabFolder control, IStackPresentationSite stackSite) {
	    super(stackSite);
		tabFolder = control;
		
		// listener to switch between visible tabItems
		tabFolder.addListener(SWT.Selection, selectionListener);

		// listener to resize visible components
		tabFolder.addListener(SWT.Resize, resizeListener);

		// listen for mouse down on tab to set focus.
		tabFolder.addMouseListener(mouseListener);
		
		tabFolder.addListener(SWT.MenuDetect, menuListener);

		tabFolder.addCTabFolder2Listener(closeListener);
		
		tabFolder.addCTabFolder2Listener(expandListener);
		
		PresentationUtil.addDragListener(tabFolder, new Listener() {
			public void handleEvent(Event event) {
				Point localPos = new Point(event.x, event.y);
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
		});
						
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
		if(property == IPresentablePart.PROP_BUSY){
			FontRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
			if(part.isBusy())
				tab.setFont(registry.getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT));
			else{
				if(getCurrent().equals(part))//Set bold if it does not already have focus
					tab.setFont(registry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
				else
					tab.setFont(registry.getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT));
			}
				
		}
	}

	protected final IPresentablePart getPartForTab(CTabItem item) {
		IPresentablePart part = (IPresentablePart)item.getData(TAB_DATA);
		
		return part;
	}
	
	public CTabFolder getTabFolder() {
		return tabFolder;
	}
	
	public void setTabPosition(int position) {
		tabPosition = position;
		getTabFolder().setTabPosition(tabPosition);
	}
	
	public int getTabPosition() {
		return tabPosition;
	}
	
	public boolean isDisposed() {
		return tabFolder == null || tabFolder.isDisposed();
	}
	
	public void drawGradient(Color fgColor, Color [] bgColors, int [] percentages, boolean vertical) {
		tabFolder.setSelectionForeground(fgColor);
		tabFolder.setSelectionBackground(bgColors, percentages, vertical);			    
	}
	
	public boolean isActive() {
		return activeState;
	}
	
	/**
	 * Set the size of a page in the folder.
	 */
	private void setControlSize() {
		if (current == null || tabFolder == null)
			return;
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
	
	public static Rectangle calculatePageBounds(CTabFolder folder) {
		if (folder == null)
			return new Rectangle(0, 0, 0, 0);
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
		
		systemMenuManager.dispose();
		
		tabFolder.dispose();
		tabFolder = null;
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
}
