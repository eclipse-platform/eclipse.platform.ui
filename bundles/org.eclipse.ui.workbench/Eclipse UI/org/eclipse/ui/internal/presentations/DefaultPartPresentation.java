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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Base class for StackPresentations that display IPresentableParts in a PaneFolder. 
 * 
 * @since 3.0
 */
public class DefaultPartPresentation extends StackPresentation {
	
	private PaneFolder tabFolder;
	private IPresentablePart current;
	private int activeState = StackPresentation.AS_INACTIVE;
	private MenuManager systemMenuManager = new MenuManager();
	private Label titleLabel;
	private Listener dragListener;
	private List activationList = new ArrayList(10);
	private boolean activationListChange = true;
	
	/**
	 * While we are dragging a tab from this folder, this holdes index of the tab
	 * being dragged. Set to -1 if we are not currently dragging a tab from this folder.
	 */
	private int dragStart = -1;
		
	private final static String TAB_DATA = DefaultPartPresentation.class.getName() + ".partId"; //$NON-NLS-1$
	
	private PaneFolderButtonListener buttonListener = new PaneFolderButtonListener() {
		public void stateButtonPressed(int buttonId) {
			getSite().setState(buttonId);
		}

		public void closeButtonPressed(CTabItem item) {
			IPresentablePart part = getPartForTab(item);
			
			getSite().close(new IPresentablePart[]{part});		
		}
		
        public void showList(CTabFolderEvent event) {
            event.doit = false;
            showPartList();
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
			if (ignoreSelectionChanges) {
				return;
			}
			
			IPresentablePart item = getPartForTab((CTabItem)e.item);
			
			if (item != null) {
				setSelection((CTabItem)e.item);
			}
		}
	};
	
	private IPropertyListener childPropertyChangeListener = new IPropertyListener() {
		public void propertyChanged(Object source, int property) {
			
			if(isDisposed())
				return;
			
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
	
	private DisposeListener folderDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			DefaultPartPresentation.this.widgetDisposed();
		}
	};
	
	private ToolBar viewToolBar;
	
	private boolean shellActive = true;
	
	private boolean ignoreSelectionChanges = false;
	
	private ShellListener shellListener = new ShellAdapter() {

        public void shellActivated(ShellEvent e) {
            shellActive = true;
            updateGradient();
        }


        public void shellDeactivated(ShellEvent e) {
            shellActive = false;
            updateGradient();            
        }
	};
	
	/**
	 * property listener that listens for theme events and updates the tab folder
	 * accordingly
	 */
    private IPropertyChangeListener themeListener = new IPropertyChangeListener() {

        /* (non-Javadoc)
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event) {
            
        	if(isDisposed())
        		return;
        	
            String property = event.getProperty();
            if (property.equals(IThemeManager.CHANGE_CURRENT_THEME)) { 
                updateGradient();
                setTitleAttributes();
            }
            else if (property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START)
                    || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)
                    || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR)
                    || property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR)
					|| property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START)
					|| property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END)
                    || property.equals(IWorkbenchThemeConstants.TAB_TEXT_FONT)) {
                updateGradient();
            }
            else if (property.equals(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT)) {
                setTitleAttributes();
            }
        }	            
    };

	public DefaultPartPresentation(PaneFolder control, IStackPresentationSite stackSite) {
	    super(stackSite);
	    
	    shellActive = control.getControl().getShell().equals(control.getControl().getDisplay().getActiveShell());
	    
		tabFolder = control;
		// Add listener to check for premature disposal
		tabFolder.getControl().addDisposeListener(folderDisposeListener);
		
		tabFolder.setMinimizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MINIMIZED));
		tabFolder.setMaximizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
		
		tabFolder.getControl().getShell().addShellListener(shellListener);
		
		titleLabel = new Label(tabFolder.getControl(), SWT.NONE);
		titleLabel.setVisible(false);
		titleLabel.moveAbove(null);

		PlatformUI
        	.getWorkbench()
        	.getThemeManager()
        	.addPropertyChangeListener(themeListener);
		
		
		viewToolBar = new ToolBar(control.getControl(), SWT.FLAT);
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
				
				if (getSite().isPartMoveable(part)) {
					dragStart = tabFolder.indexOf(tabUnderPointer);
					getSite().dragStart(part, 
						tabFolder.getControl().toDisplay(localPos), false);
					dragStart = -1;
				}
			}
		};
		
		PresentationUtil.addDragListener(tabFolder.getControl(), dragListener);

		titleLabel.addMouseListener(mouseListener);
		
		{ // Initialize system menu
			systemMenuManager.add(new GroupMarker("misc")); //$NON-NLS-1$
			systemMenuManager.add(new GroupMarker("restore")); //$NON-NLS-1$
			systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuRestore(getSite())));
			
			systemMenuManager.add(new SystemMenuMove(getSite(), getPaneName()));
			systemMenuManager.add(new GroupMarker("size")); //$NON-NLS-1$
			systemMenuManager.add(new GroupMarker("state")); //$NON-NLS-1$
			systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuMinimize(getSite())));
			
			systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuMaximize(getSite())));
			systemMenuManager.add(new Separator("close")); //$NON-NLS-1$
			systemMenuManager.add(new UpdatingActionContributionItem(new SystemMenuClose(getSite())));
			
			getSite().addSystemActions(systemMenuManager);
		} // End of system menu initialization
	}

	/**
	 * Restores a presentation from a previously stored state
	 * 
	 * @param serializer (not null)
	 * @param savedState (not null)
	 */
	public void restoreState(IPresentationSerializer serializer, IMemento savedState) {
		IMemento[] parts = savedState.getChildren(IWorkbenchConstants.TAG_PART);
		
		for (int idx = 0; idx < parts.length; idx++) {
			String id = parts[idx].getString(IWorkbenchConstants.TAG_ID);
			
			if (id != null) {
				IPresentablePart part = serializer.getPart(id);
				
				if (part != null) {
					addPart(part, tabFolder.getItemCount());
				}
			} 
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#saveState(org.eclipse.ui.presentations.IPresentationSerializer, org.eclipse.ui.IMemento)
	 */
	public void saveState(IPresentationSerializer context, IMemento memento) {
		super.saveState(context, memento);
		
		List parts = getPresentableParts();
		
		Iterator iter = parts.iterator();
		while (iter.hasNext()) {
			IPresentablePart next = (IPresentablePart)iter.next();
			
			IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_PART);
			childMem.putString(IWorkbenchConstants.TAG_ID, context.getId(next));
		}
	}
	
	/**
	 * This method performs initialization that must be done after the object is created. Subclasses
	 * must call this method exactly once on the last line of any public constructor.
	 */
	public void init() {
		updateGradient();
		setTitleAttributes();
	}
	
    /**
     * Sets the font on the title of this stack.
     */
    protected void setTitleAttributes() {
    	Assert.isTrue(!isDisposed());
    	
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        Font messageFont = theme.getFontRegistry().get(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT);
        if (!messageFont.equals(titleLabel.getFont())) {
	        titleLabel.setFont(messageFont);
	        setControlSize();
        }
    }
    
    /**
     * Sets the colors of the tab to the inactive tab colors.
     */
    protected final void setInactiveTabColors() {
    	Assert.isTrue(!isDisposed());
    	
	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();	    
	    ColorRegistry colorRegistry = theme.getColorRegistry();

        drawGradient(
                colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR), 
                new Color [] {
                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START), 
                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)
                }, 
                new int [] {theme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT)},
                theme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL));	               
    }
    
    /**
     * Sets the colors of the tab to the active tab colors, taking into account 
     * shell focus.
     */
    protected final void setActiveTabColors() {
    	Assert.isTrue(!isDisposed());
    	
	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();	    
	    ColorRegistry colorRegistry = theme.getColorRegistry();
	    
        if (isShellActive()) {
	        drawGradient(
	                colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR), 
	                new Color [] {
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START), 
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END)
	                }, 
	                new int [] {theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT)},
	                theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL));
        }
        else {
	        drawGradient(
	                colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_TEXT_COLOR), 
	                new Color [] {
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_START), 
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_END)
	                }, 
	                new int [] {theme.getInt(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_PERCENT)},
	                theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_VERTICAL));
            
        }
    }
    
    /**
     * Update the folder colors and fonts based on the current active state.  
     * Subclasses should override, ensuring that they call
     * super after all color/font changes.
     */
    protected void updateGradient() {
    	Assert.isTrue(!isDisposed());
    	// do nothing
    }   
	
	/**
     * @return the required tab height for this folder.
     */
    protected int computeTabHeight() {
    	Assert.isTrue(!isDisposed());
    	
        GC gc = new GC(tabFolder.getControl());
        
		// Compute the tab height
		int tabHeight = Math.max(
		        viewToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, 
		        gc.getFontMetrics().getHeight());

		gc.dispose();
		
		return tabHeight;
    }
	
	protected String getPaneName() {
		return WorkbenchMessages.getString("DefaultPartPresentation.PaneName"); //$NON-NLS-1$
	}

	/**
	 * Displays the view menu as a popup
	 */
	public void showPaneMenu() {
    	Assert.isTrue(!isDisposed());
    	
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
    	Assert.isTrue(!isDisposed());
    	
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
	protected final int indexOf(IPresentablePart part) {
    	Assert.isTrue(!isDisposed());
    	
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
    	Assert.isTrue(!isDisposed());
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
		// If we're in the process of removing this part, it's possible that we might still receive
		// some events for it. If everything is working perfectly, this should never happen... however,
		// we check for this case just to be safe.
		if (tab == null) {
			return;
		}
		
		initTab(tab, part);
		
		switch (property) {
		 case IPresentablePart.PROP_BUSY:
			break;
	     case IPresentablePart.PROP_HIGHLIGHT_IF_BACK:
	     	FontRegistry registry = 
			    PlatformUI.getWorkbench().
			    	getThemeManager().getCurrentTheme().
			    		getFontRegistry();
	     	
	       	if(getCurrent() != part)//Set bold if it does currently have focus
				tab.setFont(registry.getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT));
	        break;
	     case IPresentablePart.PROP_CONTENT_DESCRIPTION:
		 case IPresentablePart.PROP_TOOLBAR:
		 case IPresentablePart.PROP_PANE_MENU:
		 case IPresentablePart.PROP_TITLE:
		 	if (getCurrent() == part) {
		 		setControlSize();
		 	}
		 	break;
		}
	}

	protected final IPresentablePart getPartForTab(CTabItem item) {
    	Assert.isTrue(!isDisposed());
    	
		IPresentablePart part = (IPresentablePart)item.getData(TAB_DATA);
		
		return part;
	}
	
	/**
	 * Returns the underlying tab folder for this presentation.
	 * 
	 * @return
	 */
	protected PaneFolder getTabFolder() {
    	Assert.isTrue(!isDisposed());
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
    	Assert.isTrue(!isDisposed());
    	
		tabFolder.setSelectionForeground(fgColor);
		tabFolder.setSelectionBackground(bgColors, percentages, vertical);	
	}
	
	public boolean isActive() {
    	Assert.isTrue(!isDisposed());
    	
		return activeState == StackPresentation.AS_ACTIVE_FOCUS;
	}
	
	public int getActive() {
    	Assert.isTrue(!isDisposed());
    	
	    return activeState;
	}	
	
	protected String getCurrentTitle() {
    	Assert.isTrue(!isDisposed());
    	
		if (current == null) {
			return ""; //$NON-NLS-1$
		} 
		
		String result = current.getTitleStatus();
		
		return result;
	}

	protected void layout(boolean changed) {
    	Assert.isTrue(!isDisposed());
    	
		if (changed) {
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
		}

		tabFolder.layout(changed);

		if (current != null) {
			Rectangle clientArea = tabFolder.getClientArea();
			Rectangle bounds = tabFolder.getControl().getBounds();
			clientArea.x += bounds.x;
			clientArea.y += bounds.y;
			
			current.setBounds(clientArea);
		}		
	}
	
	/**
	 * Set the size of a page in the folder.
	 */
	protected void setControlSize() {
    	Assert.isTrue(!isDisposed());
    	
		layout(true);
	}
	
	/**
	 * Returns the IPartMenu for the currently selected part, or null if the current
	 * part does not have a menu.
	 * 
	 * @return the IPartMenu for the currently selected part or null if none
	 */
	protected IPartMenu getPartMenu() {
    	Assert.isTrue(!isDisposed());
    	
		IPresentablePart part = getCurrentPart();		
		if (part == null) {
			return null;
		}

		return part.getMenu();
	}
		
	
	// This is final because subclasses should overload widgetDisposed() instead.
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#dispose()
	 */
	public final void dispose() {
		if (isDisposed()) {
			return;
		}
		// Dispose the tab folder. This will cause widgetDisposed (below) to be called,
		// which does the real cleanup. This pattern ensures that the disposal code
		// will run even if the widget was prematurely disposed (due to its parent
		// being destroyed).
		tabFolder.getControl().dispose();		
	}
	
	/**
	 * Called when the tabFolder is disposed. This does the real cleanup.
	 */
	protected void widgetDisposed() {
		tabFolder.getControl().getShell().removeShellListener(shellListener);
		PresentationUtil.removeDragListener(tabFolder.getControl(), dragListener);
		
		systemMenuManager.dispose();
		systemMenuManager.removeAll();
		
		tabFolder.getControl().removeDisposeListener(folderDisposeListener);
		tabFolder = null;
		
		titleLabel.dispose();
		titleLabel = null;
		
		viewToolBar.dispose();
		
        PlatformUI
        .getWorkbench()
        .getThemeManager()
        .removePropertyChangeListener(themeListener); 
        
        activationList = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
	 */
	public void setActive(int newState) {
    	Assert.isTrue(!isDisposed());
    	
	    activeState = newState;	   
	}
	
	private CTabItem createPartTab(IPresentablePart part, int tabIndex) {
		Assert.isTrue(!isDisposed());
		
		tabIndex = Math.min(tabIndex, tabFolder.getItemCount());
		
		CTabItem tabItem;

		int style = SWT.NONE;
		
		if (getSite().isCloseable(part)) {
			style |= SWT.CLOSE;
		}
		
		ignoreSelectionChanges = true;
		try {
			tabItem = tabFolder.createItem(style, tabIndex);			
		} finally {
			ignoreSelectionChanges = false;
		}
				
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
    	Assert.isTrue(!isDisposed());
    	
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
			tabItem.setFont(null);
		}			

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#addPart(org.eclipse.ui.internal.skins.IPresentablePart, org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void addPart(IPresentablePart newPart, Object cookie) {
		
		int idx;
		List lruList = null;
		
		if (cookie instanceof Integer) {
			idx = ((Integer)cookie).intValue();
			addPart(newPart, idx);
			return;
		} else {
			if (cookie != null && cookie instanceof List) {
				// When adding a part, the LRU list and index are being passed
				// from #selectPart(...).  In selectPart, when the item is invisible on 
				// the left hand side of the tab folder and the user selects it from the chevron, 
				// save the LRU list of visible items, get the position at which we should re-insert
				// the item, then remove the item and call addPart(...) with this information.
				// All this to support resize of the CTabFolder pushing tabs off the left and 
				// retaining the ability for them to come back from the left during resize, when
				// possible
				List list = (List)cookie;
				lruList = (List)list.get(0);
				idx = ((Integer)list.get(1)).intValue();
			}
			else {
				// just calling addPart(...) and the item is inserted in the default location
				idx = getItemInsertionIndex();
				lruList = getVisibleItemsLRUList();
			}
		}
		
		addPart(newPart, idx);
		
		// after adding, need to check if the part is showing, as there are cases where 
		// the part can be added at the given index and not appear (for example, when the list
		// becomes more constrained due to the addition of a chevron)
		if (tabFolder.getItemCount() > 1) {
			// get the tab for the item.  dont rely on the ideal insertion index as this may be altered
			CTabItem myItem = getTab(newPart);
			if (myItem == null) 
				return;
			// make sure the part is showing
			while (!myItem.isShowing()) {
				IPresentablePart lruPart = null;
				CTabItem lruItem = null;
				
				// get the least recently used item that is showing
				// and remove it
				for (int i = 0; i < lruList.size(); i++) {
					lruItem = (CTabItem)lruList.get(i);
					if (!myItem.equals(lruItem)) {
						lruItem = (CTabItem)lruList.remove(i);
						break;
					}
				}
				if (myItem.equals(lruItem) || lruItem == null)
					break;
				
				lruPart = getPartForTab(lruItem);
				// do not update the activation order of the part that will
				// be removed and added after the new item
				try {
					activationListChange = false;
					removePart(lruPart);
					// decrement the index of the item being added as the removal 
					// is always of an item with a lower index then the item being added on the right
					addPart(lruPart, --idx+1);
				}
				finally {
					activationListChange = true;
				}
			}
		}
	}
	
	/**
	 * @return a list containing the CTabItem items that are
	 * visible in the CTabFolder
	 */
	private List getVisibleItemsLRUList() {
		List lruList = new ArrayList(10);
		// save current visible items in the least recently used order
		
		// Sort the items whose tabs are showing based on activation order.
		// for each item in the CTabFolder, cross index that in the activation
		// list to come up with a LRU ordered list of CTabItems.
		// NOTE: the activation list is not always a complete list of all items 
		// for example on startup it can be empty
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem item = tabFolder.getItem(i);
			// only care about items that are showing
			if (item.isShowing()) {
				int lruListSize = lruList.size();
				if (lruListSize == 0)
					lruList.add(item);
				else {
					int j = 0;
					// any item that is NOT in the activation list, returns -1 as an
					// index hence the item will be inserted at the begining of the LRU 
					// list make it the least recently used 
					for (j = 0; j < lruListSize; j++) {
						if (activationList.indexOf(getPartForTab(item)) <
								activationList.indexOf(getPartForTab((CTabItem)lruList.get(j))))
							break;
					}
					lruList.add(j, item);
				}
			}
		}
		return lruList;
	}

	/**
	 * @return the index at which a new item
	 * should be inserted
	 */
	private int getItemInsertionIndex() {
		// Select a location for newly inserted parts
		// Insert after the last showing item on the right
		int idx = tabFolder.getItemCount();
		
		// add to the end if the bounds are 0 - the isShowing/getBounds logic
		// below does not work when opening an initial perspective because no 
		// items are showing (the folders bounds are set to 0)
		// Items on GTK wont have a width of 0 - so use 1
		if (!tabFolder.getControl().isVisible() || tabFolder.getControl().getBounds().width <= 1)
			return idx;
		for (int i = 0; i < idx; i++) {
			CTabItem item = tabFolder.getItem(i);
			// make sure the item is not showing and it is on the right
			if (!item.isShowing() && item.getBounds().x >= 0) {
				return i;
			}
		}
		return idx;
	}

	/**
	 * Adds the given presentable part to this presentation at the given index.
	 * Does nothing if a tab already exists for the given part. 
	 *
	 * @param newPart
	 * @param index
	 */
	public void addPart(IPresentablePart newPart, int index) { 
		// If we already have a tab for this part, do nothing
		if (getTab(newPart) != null) {
			return;
		}
		createPartTab(newPart, index);
		
		setControlSize();
		// in some cases we don't want addPart to affect the activation list
		// such as when we are moving an items location because it was 
		// the least recently used
		if (activationListChange)
			activationList.add(newPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#removePart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
	    if (current == oldPart) {
	    	current = null;
			// Remove any references to the old widget
			tabFolder.setTopLeft(null);
			tabFolder.setTopCenter(null);
			tabFolder.setTopRight(null);
	    }
	    
		CTabItem item = getTab(oldPart);
		if (item == null) {
			return;
		}
		oldPart.setVisible(false);		
		
		// in some cases we don't want removePart to affect the activation list
		// such as when we are moving an items location because it was 
		// the least recently used
		if (activationListChange)
			activationList.remove(oldPart);
		
		ignoreSelectionChanges = true;
		try {
			item.dispose();
		} finally {
			ignoreSelectionChanges = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#selectPart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void selectPart(IPresentablePart toSelect) {
    	Assert.isTrue(!isDisposed());
    	
		if (toSelect == current) {
			return;
		}
		
		IPresentablePart oldPart = current;
		
		current = toSelect;
		
		if (current != null) {
			CTabItem item = getTab(toSelect);
			if (item != null)
				// If the item is not currently visible, move it
				// to last visible position on the right
				// When loading the workbench, all tabs are invisible
				// we don't want to change tab ordering in this case
				if (isVisibleTabs()) {
					if (!item.isShowing() && tabFolder.getItemCount() > 1) {
						try {
						activationListChange = false;
						// Save a list of the visible items in LRU order
						List lruList = getVisibleItemsLRUList();
						int idx = getItemInsertionIndex();
						// If we remove an item from the left
						// decrement the index by 1
						if (item.getBounds().x < 0)
							idx--;
						
						removePart(toSelect);
						
						// pass the LRU list and insertion index
						// to addPart, as it was before the remove happened
						List cookie = new ArrayList(2);
						cookie.add(lruList);	
						cookie.add(new Integer(idx));
						
						addPart(toSelect, cookie);
						}
						finally {
							activationListChange = true;
						}
					}
				}
			current = toSelect;
			tabFolder.setSelection(indexOf(current));
			current.setVisible(true);
			// if the act of setting the part visible has caused it to be 
			// removed or the folder to be disposed (the case when restoring 
			// editors with non-existant input) then don't modify the state of 
			// this presentation any further - leave the last tab visible
			// see Bug 63721
			if (isDisposed())
				return;
			if (getTab(toSelect) == null) {
				selectPart(oldPart);
				return;
			}
			setControlSize();	
			// update the newly selected item in the activation order
			if (activationListChange) {
				activationList.remove(toSelect);
				activationList.add(toSelect);
			}
		}
		
		if (oldPart != null) {
			oldPart.setVisible(false);
		}
	}
	
	/**
	 * @return <code>true</code> if at least one tab is visible
	 * <code>false</code> otherwise
	 */
	private boolean isVisibleTabs() {
		boolean result = false;
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem item = tabFolder.getItem(i);
			if (item.isShowing()) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public IPresentablePart getCurrentPart() {
    	Assert.isTrue(!isDisposed());
    	
		return current;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
    	Assert.isTrue(!isDisposed());
    	
		tabFolder.aboutToResize();
		tabFolder.getControl().setBounds(bounds);
		layout(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		return tabFolder.computeMinimumSize();		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
    	Assert.isTrue(!isDisposed());
    	
		if (current != null) {
			current.setVisible(isVisible);
		}
		tabFolder.getControl().setVisible(isVisible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
	 */
	public void setState(int state) {
    	Assert.isTrue(!isDisposed());
    	
		tabFolder.setState(state);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#getSystemMenuManager()
	 */
	public IMenuManager getSystemMenuManager() {
    	Assert.isTrue(!isDisposed());
    	
		return systemMenuManager;
	}
	
	/**
	 * @param part
	 * @param point
	 */
	protected void showSystemMenu(Point point) {
    	Assert.isTrue(!isDisposed());
    	
		Menu aMenu = systemMenuManager.createContextMenu(tabFolder.getControl().getParent());
		systemMenuManager.update(true);
		aMenu.setLocation(point.x, point.y);
		aMenu.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#getControl()
	 */
	public Control getControl() {
    	Assert.isTrue(!isDisposed());
    	
		return tabFolder.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
	 */
	public StackDropResult dragOver(Control currentControl, Point location) {
    	Assert.isTrue(!isDisposed());
    	
		// Determine which tab we're currently dragging over
		Point localPos = tabFolder.getControl().toControl(location);
		
		CTabItem tabUnderPointer = tabFolder.getItem(localPos);

		// This drop target only deals with tabs... if we're not dragging over
		// a tab, exit.
		if (tabUnderPointer == null) {
			Rectangle titleArea = tabFolder.getTitleArea();
			
			// If we're dragging over the title area, treat this as a drop in the last
			// tab position.
			if (titleArea.contains(localPos)) {
				int dragOverIndex = tabFolder.getItemCount();
				CTabItem lastTab = tabFolder.getItem(dragOverIndex - 1);

				// Can't drag to end unless you can see the end
				if (!lastTab.isShowing()) {
					return null;
				}
				
				if (dragStart >= 0) {
					dragOverIndex--;

					return new StackDropResult(Geometry.toDisplay(tabFolder.getControl(), 
							lastTab.getBounds()), 
						new Integer(dragOverIndex));					
				}

				// Make the drag-over rectangle look like a tab at the end of the tab region.
				// We don't actually know how wide the tab will be when it's dropped, so just
				// make it 3 times wider than it is tall.
				Rectangle dropRectangle = Geometry.toDisplay(tabFolder.getControl(), titleArea);
		
				dropRectangle.width = 3 * dropRectangle.height;
				return new StackDropResult(dropRectangle, new Integer(dragOverIndex));
				
			} else {
				return null;
			}
		}
		
		if (!tabUnderPointer.isShowing()) {
			return null;
		}
		
		return new StackDropResult(Geometry.toDisplay(tabFolder.getControl(), tabUnderPointer.getBounds()), 
				new Integer(tabFolder.indexOf(tabUnderPointer)));
	}
		
	/**
	 * Returns the toolbar control for the currently selected part, or null if none (not 
	 * all parts have a toolbar).
	 * 
	 * @return the current toolbar or null if none
	 */
	protected Control getCurrentToolbar() {
    	Assert.isTrue(!isDisposed());
    	
		IPresentablePart part = getCurrentPart();		
		if (part == null) {
			return null;
		}

		return part.getToolBar();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
    	Assert.isTrue(!isDisposed());
    	
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
    	Assert.isTrue(!isDisposed());
    	
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
	
    protected void showList(Shell parentShell, int x, int y) {
    	Assert.isTrue(!isDisposed());
    	
        final PaneFolder tabFolder = getTabFolder();

        int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
        int tableStyle = SWT.V_SCROLL | SWT.H_SCROLL;
        final BasicStackList editorList = new BasicStackList(tabFolder.getControl().getShell(),
                shellStyle, tableStyle);
        editorList.setInput(this);
        Point size = editorList.computeSizeHint();
        
        Monitor mon = getTabFolder().getControl().getMonitor();
        Rectangle bounds = mon.getClientArea();
        if (x + size.x > bounds.x + bounds.width) x = bounds.x + bounds.width - size.x;
        if (y + size.y > bounds.y + bounds.height) y = bounds.y + bounds.height - size.y;
        editorList.setLocation(new Point(x, y));
        editorList.setVisible(true);
        editorList.setFocus();
        editorList.getTableViewer().getTable().getShell().addListener(
                SWT.Deactivate, new Listener() {

                    public void handleEvent(Event event) {
                        editorList.setVisible(false);
                    }
                });
    }
    
    /*
     * Shows the list of tabs at the top left corner of the editor
     */
    public void showPartList() {
    	Assert.isTrue(!isDisposed());
    	
    	PaneFolder tabFolder = getTabFolder();
    	Shell shell = tabFolder.getControl().getShell();
    	
    	// get the last visible item
    	int numItems = tabFolder.getItemCount();
    	CTabItem item = null, tempItem = null;
    	for (int i = 0; i < numItems; i++) {
    		tempItem = tabFolder.getItem(i);
			if (tempItem.isShowing())
				item = tempItem;
		}
		
		// if we have no visible tabs, abort.
		if (item == null)
			return;
    	
    	Rectangle itemBounds = item.getBounds();
    	int x = itemBounds.x+itemBounds.width;
    	int y = itemBounds.y + itemBounds.height;
    	Point location = item.getDisplay().map(tabFolder.getControl(), null, x, y); 
        showList(shell, location.x, location.y);
    }
    
	void setSelection(CTabItem tabItem) {
		getSite().selectPart(getPartForTab(tabItem));
    }

    void close(IPresentablePart[] presentablePart) {
        getSite().close(presentablePart);
    }
    
    /**
     * Returns the List of IPresentablePart currently in this presentation
     */
    List getPresentableParts() {
    	Assert.isTrue(!isDisposed());
    	
    	CTabItem[] items = tabFolder.getItems();
    	List result = new ArrayList(items.length);
    	
    	for (int idx = 0; idx < tabFolder.getItemCount(); idx++) {
    		result.add(getPartForTab(items[idx]));
    	}
    	
    	return result;
    }
    
    Image getLabelImage(IPresentablePart presentablePart) {
        return presentablePart.getTitleImage();
    }
    
    String getLabelText(IPresentablePart presentablePart,
            boolean includePath) {
        String title = presentablePart.getName();
        return title;
    }
    
    /**
     * Answers whether the shell containing this presentation is currently the active shell.
     */
    protected boolean isShellActive() {
    	Assert.isTrue(!isDisposed());
    	
        return shellActive;
    }
}
