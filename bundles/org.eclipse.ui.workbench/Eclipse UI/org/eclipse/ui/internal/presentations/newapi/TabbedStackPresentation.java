package org.eclipse.ui.internal.presentations.newapi;

import java.util.ArrayList;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.0
 */
public final class TabbedStackPresentation extends StackPresentation {

	private final static String TAB_DATA = "part"; //$NON-NLS-1$
	private final static String BOLD_DATA = "isBold"; //$NON-NLS-1$
	
	private AbstractTabFolder tabFolder;
	private TabOrder tabs;
	private TabDragHandler dragBehavior;
	private int ignoreSelectionChanges = 0;
	private boolean initializing = true;
	private int dragStart = -1;
	
	private IPresentablePartList tabOrderListener = new IPresentablePartList() {
		public void insert(IPresentablePart part, int idx) {
			doInsert(part, idx);
		}

		public void remove(IPresentablePart part) {
			doRemove(part);
		}

		public void move(IPresentablePart part, int newIndex) {
			doMove(part, newIndex);
		}

		public int size() {
			return tabFolder.getItemCount();
		}
		
		public void select(IPresentablePart toSelect) {
			doSelect(toSelect);
		}
	};
	
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
	 * Listener attached to all tool items. It removes listeners from the associated
	 * part when the tool item is destroyed. This is required to prevent memory leaks.
	 */
	private DisposeListener tabDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {				
			IPresentablePart part = getPartForTab(e.widget);
			
			part.removePropertyListener(childPropertyChangeListener);
		}
	};
	
	private DisposeListener toolbarDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			setToolbar(null);
		}		
	};
	
	private AbstractTabFolderListener tabFolderListener = new AbstractTabFolderListener() {
		public void stateButtonPressed(int buttonId) {
			getSite().setState(buttonId);
		}
		
		public void closeButtonPressed(AbstractTabItem item) {
			IPresentablePart part = getPartForTab(item.getControl());
			
			getSite().close(new IPresentablePart[]{part});	
		}
		
		public void showList() {
            showPartList();
		}
		
		/**
		 * Called to show the pane menu at the given location (display coordinates)
		 */
		public void showPaneMenu(Point location) {
			TabbedStackPresentation.this.showPaneMenu(location);
		}
		
		public void dragStart(AbstractTabItem beingDragged, Point initialLocation) {
			if (beingDragged == null) {
				getSite().dragStart(initialLocation, false);
			} else {
				dragStart = tabFolder.indexOf(beingDragged);
				try {
					IPresentablePart part = getPartForTab(beingDragged.getControl());
					getSite().dragStart(part, initialLocation, false);
				} finally {
					dragStart = -1;
				}
			}
		}
		
	};
	
	private ShellListener shellListener = new ShellAdapter() {
        public void shellActivated(ShellEvent e) {
        	tabFolder.shellActive(true);
        }
        
        public void shellDeactivated(ShellEvent e) {
        	tabFolder.shellActive(false);
        }
	};
	
	public TabbedStackPresentation(IStackPresentationSite site, AbstractTabFolder folder, 
			TabOrder tabs, TabDragHandler dragBehavior) {
		super(site);
		
		tabFolder = folder;
		this.tabs = tabs;
		this.dragBehavior = dragBehavior;
		
		// Add a dispose listener. This will call the presentationDisposed()
		// method when the widget is destroyed.
		folder.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				presentationDisposed();
			}
		});
		
		folder.allowMaximizeButton(getSite().supportsState(IStackPresentationSite.STATE_MAXIMIZED));
		folder.allowMinimizeButton(getSite().supportsState(IStackPresentationSite.STATE_MINIMIZED));
		folder.addListener(tabFolderListener);
		
		tabFolder.getControl().getShell().addShellListener(shellListener);
		tabFolder.shellActive(tabFolder.getControl().getDisplay().getActiveShell() 
				== tabFolder.getControl().getShell());
		
	}

	private void childPropertyChanged(IPresentablePart part, int property) {
		AbstractTabItem tab = getTab(part);
		// If we're in the process of removing this part, it's possible that we might still receive
		// some events for it. If everything is working perfectly, this should never happen... however,
		// we check for this case just to be safe.
		if (tab == null) {
			return;
		}
		
		switch (property) {
		 case IPresentablePart.PROP_HIGHLIGHT_IF_BACK: 	
		   	if(getCurrent() != part) {//Set bold if it does currently have focus
		   		tab.getControl().setData(BOLD_DATA, BOLD_DATA);
		   		initTab(tab, part);
		   	}
		    break;
		 case IPresentablePart.PROP_CONTENT_DESCRIPTION:
		 case IPresentablePart.PROP_TOOLBAR:
		 case IPresentablePart.PROP_PANE_MENU:
		 case IPresentablePart.PROP_TITLE:
		 	if (getCurrent() == part) {
		 		initTab(tab, part);
		 		layout(true);
		 	}
		 	break;
		 default:
			initTab(tab, part);
		}
	}
	
	protected void initTab(AbstractTabItem item, IPresentablePart part) {
		if (!Util.equals(part.getName(), item.getPartName())) {
			item.setPartName(part.getName());
		}
		
    	if (item.getImage() != part.getTitleImage()) {
    		item.setImage(part.getTitleImage());
    	}
		
		if (!(Util.equals(part.getTitleToolTip(), item.getTitleToolTip()))) {
			item.setTitleToolTip(part.getTitleToolTip());
		}
		
		boolean isBold = item.getControl().getData(BOLD_DATA) != null;
		item.setBusyState(part.isBusy(), isBold);
	}
	
	/**
	 * Returns the currently selected part, or <code>null</code>.
	 * 
	 * @return the currently selected part, or <code>null</code>
	 */
	private IPresentablePart getCurrent() {
    	Assert.isTrue(!isDisposed());
    	
	    return getSite().getSelectedPart();
	}
	
	private IPresentablePart getPartForTab(Widget tab) {
    	Assert.isTrue(!isDisposed());
    	
		IPresentablePart part = (IPresentablePart)tab.getData(TAB_DATA);
		
		return part;
	}
	
	/**
	 * Returns the tab for the given part, or null if there is no such tab
	 * 
	 * @param part the part being searched for
	 * @return the tab for the given part, or null if there is no such tab
	 */
	protected final AbstractTabItem getTab(IPresentablePart part) {
    	Assert.isTrue(!isDisposed());
		AbstractTabItem[] items = tabFolder.getItems();
		
		for (int idx = 0; idx < items.length; idx++) {
			AbstractTabItem item = items[idx];
			
			if (getPartForTab(item.getControl()) == part) {
				return item;
			}
		}
		
		return null;
	}
	
	private int indexOf(IPresentablePart part) {
		AbstractTabItem item = getTab(part);
		
		if (item == null) {
			return -1;
		}
		
		return tabFolder.indexOf(item); 
	}
	
	/**
	 * Returns true iff the presentation has been disposed
	 * 
	 * @return true iff the presentation has been disposed
	 */
	private boolean isDisposed() {
		return tabFolder != null && !SwtUtil.isDisposed(tabFolder.getControl());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		// Set the tab folder's bounds
		tabFolder.getControl().setBounds(bounds);
		
		layout(false);		
	}

	public void layout(boolean changed) {
		// Lay out the tab folder and compute the client area
		tabFolder.layout(changed);
		
		// Lay out the current part if necessary
		IPresentablePart current = getSite().getSelectedPart();
		
		if (current != null) {
			// Compute the client area (in the tabFolder's local coordinate system)
			Rectangle clientArea = tabFolder.getClientArea();
			
			// Convert into the same coordinate system as the presentation
			Point clientAreaStart = tabFolder.getControl().getParent().toControl(
					tabFolder.getControl().toDisplay(clientArea.x, clientArea.y));
			clientArea.x = clientAreaStart.x;
			clientArea.y = clientAreaStart.y;
			
			// Set the bounds of the current part
			current.setBounds(clientArea);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		return tabFolder.computeMinimumSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#dispose()
	 */
	public void dispose() {
		// Dispose the tab folder's widgetry
		tabFolder.getControl().dispose();
	}

	/**
	 * Called when the tab folder is disposed.
	 */
	private void presentationDisposed() {
		setToolbar(null);
		// Notify the tab folder that it has been disposed
		tabFolder.disposed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
	 */
	public void setActive(int newState) {
		tabFolder.setActive(newState);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
		tabFolder.getControl().setVisible(isVisible);
		
		IPresentablePart current = getSite().getSelectedPart();
		if (current != null) {
			current.setVisible(true);
		}
		
		if (isVisible) {
			layout(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setState(int)
	 */
	public void setState(int state) {
		tabFolder.setState(state);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#getControl()
	 */
	public Control getControl() {
		return tabFolder.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#addPart(org.eclipse.ui.presentations.IPresentablePart, java.lang.Object)
	 */
	public void addPart(IPresentablePart newPart, Object cookie) {
		ignoreSelectionChanges++;
		try {
			if (initializing) {
				tabs.addInitial(newPart, tabOrderListener);
			} else {
				if (cookie == null) {
					tabs.add(newPart, tabOrderListener);	
				} else {
					int insertionPoint = dragBehavior.getInsertionPosition(cookie);
					
					tabs.insert(newPart, insertionPoint, tabOrderListener);
				}
			}
		} finally {
			ignoreSelectionChanges--;
		}
	}

	
	/**
	 * Adds the given presentable part directly into this presentation at the 
	 * given index. Does nothing if a tab already exists for the given part.
	 * This is intended to be called by TabOrder and its subclasses.
	 *
	 * @param newPart
	 * @param index
	 */
	private void doInsert(IPresentablePart part, int insertionIndex) {
		Assert.isTrue(!isDisposed());
		
		if (getTab(part) != null) {
			return;
		}
		
		insertionIndex = Math.min(insertionIndex, tabFolder.getItemCount());
		
		AbstractTabItem item;
		
		ignoreSelectionChanges++;
		try {
			item = tabFolder.add(insertionIndex);
		} finally {
			ignoreSelectionChanges--;
		}
		
		item.setCloseable(getSite().isCloseable(part));
		item.getControl().setData(TAB_DATA, part);
			
		initTab(item, part);
		
		part.addPropertyListener(childPropertyChangeListener);
		item.getControl().addDisposeListener(tabDisposeListener);
	}
	
	private void doRemove(IPresentablePart toRemove) {
		ignoreSelectionChanges++;
		try {
			AbstractTabItem item = getTab(toRemove);
			item.getControl().dispose();
		} finally {
			ignoreSelectionChanges--;
		}
	}
	
	private void doSelect(IPresentablePart toSelect) {
		if (ignoreSelectionChanges > 0) {
			return;
		}
		
		AbstractTabItem selectedItem = getTab(toSelect);
		
		tabFolder.setSelection(selectedItem);
		
		if (selectedItem != null && !(selectedItem.getControl().isDisposed())) {
			// Determine if we need to un-bold this tab
			if (selectedItem.getControl().getData(BOLD_DATA) != null) {
				selectedItem.getControl().setData(BOLD_DATA, null);
				initTab(selectedItem, toSelect);
			}
			
			setToolbar(toSelect.getToolBar());
			
		} else {
			setToolbar(null);
		}
		
		layout(true);
	} 
	
	private void setToolbar(Control newToolbar) {
		Control oldToolbar = tabFolder.getToolbar();
		if (oldToolbar != null) {
			if (!oldToolbar.isDisposed()) {
				oldToolbar.removeDisposeListener(toolbarDisposeListener);
			}
		}
		
		if (newToolbar != null) {
			newToolbar.addDisposeListener(toolbarDisposeListener);
		}
		
		tabFolder.setToolbar(newToolbar);
	}
	
	/**
	 * Moves the given part to the given index. When this method returns,
	 * indexOf(part) will return newIndex.
	 * 
	 * @param part
	 * @param newIndex
	 */
	protected void doMove(IPresentablePart part, int newIndex) {
		int currentIndex = indexOf(part);
		
		if (currentIndex == newIndex) {
			return;
		}
		
		doRemove(part);
		doInsert(part, newIndex);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#removePart(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
		ignoreSelectionChanges++;
		try {
			tabs.remove(oldPart, tabOrderListener);
		} finally {
			ignoreSelectionChanges--;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#selectPart(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void selectPart(IPresentablePart toSelect) {
		initializing = false;
		
		tabs.select(toSelect, tabOrderListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
	 */
	public StackDropResult dragOver(Control currentControl, Point location) {
		return dragBehavior.dragOver(currentControl, location, dragStart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showPaneMenu()
	 */
	public void showPaneMenu() {
		showPaneMenu(tabFolder.getPaneMenuLocation());
	}
	
	public void showPaneMenu(Point location) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#getTabList(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public Control[] getTabList(IPresentablePart part) {
		ArrayList list = new ArrayList();
		if (tabFolder.getTabPosition() == SWT.BOTTOM) {
			if (part.getControl() != null) list.add(part.getControl());
		}
			
		Control[] tabFolderTabList = tabFolder.getTabList();
		for (int i = 0; i < tabFolderTabList.length; i++) {
			list.add(tabFolderTabList[i]);
		}
		
		if (tabFolder.getTabPosition() == SWT.TOP) {
			if (part.getControl() != null) list.add(part.getControl());
		}
		
		return (Control[]) list.toArray(new Control[list.size()]);
	}
}
