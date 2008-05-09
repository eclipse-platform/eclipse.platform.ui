/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - some contributions (bug fixes and enhancements)
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.r21.R21Colors;
import org.eclipse.ui.internal.presentations.r21.R21PresentationMessages;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabItem;
import org.eclipse.ui.internal.presentations.r21.widgets.R21PaneFolder;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Base class for StackPresentations that display IPresentableParts in a
 * CTabFolder.
 * 
 * @since 3.0
 */
public class R21BasicStackPresentation extends StackPresentation {

	private R21PaneFolder paneFolder;

	private IPresentablePart current;

	private boolean activeState = false;

	private MenuManager systemMenuManager = new MenuManager();

	private CLabel titleLabel;

	private boolean shellActive = true;

	private final static String TAB_DATA = R21BasicStackPresentation.class
			.getName()
			+ ".partId"; //$NON-NLS-1$

	// private PaneFolderButtonListener buttonListener = new
	// PaneFolderButtonListener() {
	// public void stateButtonPressed(int buttonId) {
	// getSite().setState(buttonId);
	// }
	//
	// public void closeButtonPressed(CTabItem item) {
	// IPresentablePart part = getPartForTab(item);
	//			
	// getSite().close(part);
	// }
	// };
	//	
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Control) {
				Control ctrl = (Control) e.widget;
				Point globalPos = ctrl.toDisplay(new Point(e.x, e.y));

				// PR#1GDEZ25 - If selection will change in mouse up ignore
				// mouse down.
				// Else, set focus.
				CTabItem newItem = paneFolder.getItem(paneFolder.getControl()
						.toControl(globalPos));
				if (newItem != null) {
					CTabItem oldItem = paneFolder.getSelection();
					if (newItem != oldItem) {
						return;
					}
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

	private MouseListener titleMouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Control) {
				Control ctrl = (Control) e.widget;
				Point globalPos = ctrl.toDisplay(new Point(0, titleLabel
						.getBounds().height));

				if ((e.button == 1) && overImage(e.x)) {
					showSystemMenu(globalPos);
				}
			}
		}
	};

	private Listener menuListener = new Listener() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			Point pos = new Point(event.x, event.y);

			showSystemMenu(pos);
		}
	};

	private Listener dragListener = new Listener() {
		public void handleEvent(Event event) {

			Point localPos = new Point(event.x, event.y);
			CTabItem tabUnderPointer = paneFolder.getItem(localPos);

			// Drags on the title area drag the selected part only
			if (tabUnderPointer == null) {
				if (paneFolder.getTabPosition() == SWT.BOTTOM
						&& localPos.y < paneFolder.getControl().getBounds().height
								- paneFolder.getTabHeight()) {
					tabUnderPointer = paneFolder.getSelection();
				} else if (paneFolder.getTabPosition() == SWT.TOP
						&& localPos.y > paneFolder.getTabHeight()) {
					tabUnderPointer = paneFolder.getSelection();
				}
			}

			// Not in a tab, not in a title area, must be dragging the whole
			// stack
			if (tabUnderPointer == null) {
				getSite().dragStart(
						paneFolder.getControl().toDisplay(localPos), false);
				return;
			}

			IPresentablePart part = getPartForTab(tabUnderPointer);

			if (getSite().isPartMoveable(part)) {
				getSite().dragStart(part,
						paneFolder.getControl().toDisplay(localPos), false);
			}
		}
	};

	private Listener selectionListener = new Listener() {
		public void handleEvent(Event e) {
			IPresentablePart item = getPartForTab((CTabItem) e.item);

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
				CTabItem item = (CTabItem) e.widget;

				IPresentablePart part = getPartForTab(item);

				part.removePropertyListener(childPropertyChangeListener);
			}
		}
	};

	/** the shell listener for upgrading the gradient */
	private ShellAdapter shellListener = new ShellAdapter() {

		public void shellActivated(ShellEvent event) {
			shellActive = true;
			updateGradient();
		}

		public void shellDeactivated(ShellEvent event) {
			shellActive = false;
			updateGradient();
		}
	};

	private ToolBar viewToolBar;

	private ToolItem pullDownButton;

	private ToolItem closeButton;

	/**
	 * @param control
	 * @param stackSite
	 */
	public R21BasicStackPresentation(R21PaneFolder control,
			IStackPresentationSite stackSite) {
		super(stackSite);
		paneFolder = control;

		shellActive = paneFolder.getControl().getShell().equals(
				control.getControl().getDisplay().getActiveShell());

		// tabFolder.setMinimizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MINIMIZED));
		// tabFolder.setMaximizeVisible(stackSite.supportsState(IStackPresentationSite.STATE_MAXIMIZED));
		//				
		titleLabel = new CLabel(paneFolder.getControl(), SWT.SHADOW_NONE);
		titleLabel.setVisible(false);
		titleLabel.moveAbove(null);
		titleLabel.addMouseListener(titleMouseListener);
		titleLabel.addMouseListener(mouseListener);
		titleLabel.addListener(SWT.MenuDetect, menuListener);
		PresentationUtil.addDragListener(titleLabel, dragListener);

		// ColorSchemeService.setViewTitleFont(this, titleLabel);

		viewToolBar = new ToolBar(control.getControl(), SWT.HORIZONTAL
				| SWT.FLAT);
		viewToolBar.moveAbove(null);

		pullDownButton = new ToolItem(viewToolBar, SWT.PUSH);
		// Image img =
		// WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		Image hoverImage = WorkbenchImages
				.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		pullDownButton.setDisabledImage(null); // TODO: comment this out?
		// PR#1GE56QT - Avoid creation of unnecessary image.
		pullDownButton.setImage(hoverImage);
		pullDownButton.setToolTipText(R21PresentationMessages
				.getString("BasicStackPresentation.menu.tooltip")); //$NON-NLS-1$
		pullDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showPaneMenu();
			}
		});

		// listener to switch between visible tabItems
		paneFolder.getControl().addListener(SWT.Selection, selectionListener);

		// listener to resize visible components
		paneFolder.getControl().addListener(SWT.Resize, resizeListener);

		// listen for mouse down on tab to set focus.
		paneFolder.getControl().addMouseListener(mouseListener);

		paneFolder.getControl().addListener(SWT.MenuDetect, menuListener);

		// tabFolder.addButtonListener(buttonListener);

		PresentationUtil.addDragListener(paneFolder.getControl(), dragListener);

		// add the shell listener to track shell activations
		// TODO: check if workaround can be removed (see bug 55458)
		paneFolder.getControl().getShell().addShellListener(shellListener);

		// Uncomment to allow dragging from the title label
		// PresentationUtil.addDragListener(titleLabel, new Listener() {
		// public void handleEvent(Event event) {
		// if (layout.isTrimOnTop()) {
		// Point localPos = new Point(event.x, event.y);
		// getSite().dragStart(titleLabel.toDisplay(localPos), false);
		// }
		// }
		// });

		// // Compute the tab height
		// int tabHeight = viewToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		//
		// // Enforce a minimum tab height
		// if (tabHeight < 20) {
		// tabHeight = 20;
		// }
		// paneFolder.setTabHeight(tabHeight);
		//		
		populateSystemMenu(systemMenuManager);
	}

	/*
	 * Return true if <code>x</code> is over the label image.
	 */
	private boolean overImage(int x) {
		return x < titleLabel.getImage().getBounds().width;
	}

	/**
	 * @param systemMenuManager
	 */
	private void populateSystemMenu(IMenuManager systemMenuManager) {

		systemMenuManager.add(new GroupMarker("misc")); //$NON-NLS-1$
		systemMenuManager.add(new GroupMarker("restore")); //$NON-NLS-1$
		systemMenuManager.add(new UpdatingActionContributionItem(
				new SystemMenuRestore(getSite())));

		systemMenuManager.add(new SystemMenuMove(getSite(), getPaneName()));
		systemMenuManager.add(new GroupMarker("size")); //$NON-NLS-1$
		systemMenuManager.add(new GroupMarker("state")); //$NON-NLS-1$
		systemMenuManager.add(new UpdatingActionContributionItem(
				new SystemMenuMinimize(getSite())));

		systemMenuManager.add(new UpdatingActionContributionItem(
				new SystemMenuMaximize(getSite())));
		systemMenuManager.add(new Separator("close")); //$NON-NLS-1$
		systemMenuManager.add(new UpdatingActionContributionItem(
				new SystemMenuClose(getSite())));

		getSite().addSystemActions(systemMenuManager);
	}

	protected String getPaneName() {
		return R21PresentationMessages.getString("BasicStackPresentation.pane"); //$NON-NLS-1$
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
	 * Returns the index of the tab for the given part, or returns
	 * tabFolder.getItemCount() if there is no such tab.
	 * 
	 * @param part
	 *            part being searched for
	 * @return the index of the tab for the given part, or the number of tabs if
	 *         there is no such tab
	 */
	private final int indexOf(IPresentablePart part) {
		if (part == null) {
			return paneFolder.getItemCount();
		}

		CTabItem[] items = paneFolder.getItems();

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
	 * @param part
	 *            the part being searched for
	 * @return the tab for the given part, or null if there is no such tab
	 */
	protected final CTabItem getTab(IPresentablePart part) {
		CTabItem[] items = paneFolder.getItems();

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
			// FontRegistry registry =
			// PlatformUI.getWorkbench().
			// getThemeManager().getCurrentTheme().
			// getFontRegistry();
			//	     	
			// if(!getCurrent().equals(part))//Set bold if it does currently
			// have focus
			// tab.setFont(registry.getBold(IWorkbenchThemeConstants.TAB_TEXT_FONT));
			// break;
		case IPresentablePart.PROP_TOOLBAR:
		case IPresentablePart.PROP_PANE_MENU:
		case IPresentablePart.PROP_TITLE:
			setControlSize();
			break;
		}
	}

	protected final IPresentablePart getPartForTab(CTabItem item) {
		IPresentablePart part = (IPresentablePart) item.getData(TAB_DATA);

		return part;
	}

	/**
	 * Returns the underlying tab folder for this presentation.
	 * 
	 * @return the tab folder
	 */
	protected R21PaneFolder getPaneFolder() {
		return paneFolder;
	}

	/**
	 * Returns true iff the underlying tab folder has been disposed.
	 * 
	 * @return true if disposed
	 */
	public boolean isDisposed() {
		return paneFolder == null || paneFolder.isDisposed();
	}

	/**
	 * Update the tab folder's colours to match the current theme settings and
	 * active state
	 */
	protected void updateGradient() {

		if (isDisposed()) {
			return;
		}

		Color fgColor;
		Color[] bgColors;
		int[] bgPercents;
		boolean vertical = false;
		if (isActive()) {
			if (getShellActivated()) {
				fgColor = R21Colors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
				bgColors = R21Colors.getActiveViewGradient();
				bgPercents = R21Colors.getActiveViewGradientPercents();
			} else {
				fgColor = R21Colors
						.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
				bgColors = R21Colors.getDeactivatedViewGradient();
				bgPercents = R21Colors.getDeactivatedViewGradientPercents();
			}

		} else {
			fgColor = R21Colors.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
			bgColors = null;
			bgPercents = null;
		}

		drawGradient(fgColor, bgColors, bgPercents, vertical);

		// Color fgColor;
		// ITheme currentTheme =
		// PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		// FontRegistry fontRegistry = currentTheme.getFontRegistry();
		// ColorRegistry colorRegistry = currentTheme.getColorRegistry();
		// Color [] bgColors = new Color[2];
		// int [] percent = new int[1];
		// boolean vertical;

		// if (isActive()){
		//        	
		// CTabItem item = getPaneFolder().getSelection();
		// if(item != null && !getPartForTab(item).isBusy()){
		// Font tabFont =
		// fontRegistry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
		// // item.setFont(tabFont);
		// }
		//            
		// fgColor =
		// colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR);
		// bgColors[0] =
		// colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
		// bgColors[1] =
		// colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
		// percent[0] =
		// currentTheme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
		// vertical =
		// currentTheme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);
		// } else {
		// fgColor =
		// colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR);
		// bgColors[0] =
		// colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
		// bgColors[1] =
		// colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
		// percent[0] =
		// currentTheme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT);
		// vertical =
		// currentTheme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL);
		// }
		//      
		//		
		// drawGradient(fgColor, bgColors, bgPercents, false);
	}

	/**
	 * Draws the applicable gradient on the title area
	 * 
	 * @param fgColor
	 * @param bgColors
	 * @param percentages
	 * @param vertical
	 */
	public void drawGradient(Color fgColor, Color[] bgColors,
			int[] percentages, boolean vertical) {
		// paneFolder.setSelectionForeground(fgColor);
		// paneFolder.setSelectionBackground(bgColors, percentages, vertical);

		if (titleLabel == null || viewToolBar == null) {
			return;
		}

		titleLabel.setBackground(bgColors, percentages, vertical);
		titleLabel.setForeground(fgColor);

		titleLabel.update();
	}

	/**
	 * @return true if active
	 */
	public boolean isActive() {
		return activeState;
	}

	/**
	 * Set the size of a page in the folder.
	 * 
	 * TODO: Kim here...I had to make this public so that the when the font was
	 * updated via the color scheme service it could relayout the
	 * presentation... calling control.getLayout() doesn't do the trick.
	 */
	public void setControlSize() {
		// Set up the top-right controls
		// List topRight = new ArrayList(3);

		if (current != null) {
			paneFolder.setTopLeft(titleLabel);
			titleLabel.setText(current.getTitle());
			titleLabel.setImage(current.getTitleImage());
			titleLabel.setVisible(true);

			// set tooltip (https://bugs.eclipse.org/bugs/show_bug.cgi?id=67513)
			String toolTipText = current.getTitleToolTip();
			titleLabel.setToolTipText(toolTipText
					.equals(Util.ZERO_LENGTH_STRING) ? null : toolTipText);
			
		}

		Control currentToolbar = getCurrentToolbar();
		paneFolder.setTopCenter(currentToolbar);

		IPartMenu partMenu = getPartMenu();

		if (partMenu != null) {
			pullDownButton.setEnabled(true);
		} else {
			pullDownButton.setEnabled(false);
		}
		paneFolder.setTopRight(viewToolBar);
		viewToolBar.setVisible(true);

		paneFolder.layout(true);

		if (current != null) {
			Rectangle clientArea = paneFolder.getClientArea();
			Rectangle bounds = paneFolder.getControl().getBounds();
			clientArea.x += bounds.x;
			clientArea.y += bounds.y;

			current.setBounds(clientArea);
		}

	}

	/**
	 * Returns the IPartMenu for the currently selected part, or null if the
	 * current part does not have a menu.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#dispose()
	 */
	public void dispose() {
		if (isDisposed()) {
			return;
		}

		// remove shell listener
		paneFolder.getControl().getShell().removeShellListener(shellListener);

		PresentationUtil.removeDragListener(paneFolder.getControl(),
				dragListener);
		PresentationUtil.removeDragListener(titleLabel, dragListener);

		systemMenuManager.dispose();
		systemMenuManager.removeAll();
		paneFolder.getControl().dispose();
		paneFolder = null;

		titleLabel.dispose();
		titleLabel = null;

		viewToolBar.dispose();
	}

	/**
	 * @param isActive
	 */
	public void setActive(boolean isActive) {
		activeState = isActive;
		updateGradient();
	}

	/**
	 * Return whether the window's shell is activated
	 */
	/* package */boolean getShellActivated() {
		return shellActive;
	}

	/**
	 * Returns the top level window.
	 * @return the window
	 */
	public Window getWindow() {
		Control ctrl = getControl();
		if (ctrl != null) {
			Object data = ctrl.getShell().getData();
			if (data instanceof Window) {
				return (Window) data;
			}
		}
		return null;
	}

	private CTabItem createPartTab(IPresentablePart part, int tabIndex) {
		CTabItem tabItem;

		int style = SWT.NONE;

		if (getSite().isCloseable(part)) {
			style |= SWT.CLOSE;
		}

		tabItem = paneFolder.createItem(style, tabIndex);

		tabItem.setData(TAB_DATA, part);

		part.addPropertyListener(childPropertyChangeListener);
		tabItem.addDisposeListener(tabDisposeListener);

		initTab(tabItem, part);

		return tabItem;
	}

	// Create a close button in the title bar for the argument part (if needed).
	private void updateCloseButton() {
		// remove the close button if needed
		if (current == null || !getSite().isCloseable(current)) {
			if (closeButton != null) {
				closeButton.dispose();
				closeButton = null;

				paneFolder.flush();
			}
			return;
		}

		// a close button is needed, so if its already there, we're done
		if (closeButton != null) {
			return;
		}

		// otherwise create it
		closeButton = new ToolItem(viewToolBar, SWT.PUSH);
		closeButton.setDisabledImage(null);
		closeButton.setImage(WorkbenchImages
				.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW));
		closeButton.setToolTipText(R21PresentationMessages
				.getString("BasicStackPresentation.close.tooltip")); //$NON-NLS-1$
		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close(getCurrent());
			}
		});

		paneFolder.flush();
	}

	/**
	 * Initializes a tab for the given part. Sets the text, icon, tool tip, etc.
	 * This will also be called whenever a relevant property changes in the part
	 * to reflect those changes in the tab. Subclasses may override to change
	 * the appearance of tabs for a particular part.
	 * 
	 * @param tabItem
	 *            tab for the part
	 * @param part
	 *            the part being displayed
	 */
	protected void initTab(CTabItem tabItem, IPresentablePart part) {
		tabItem.setText(part.getName());

		// tabItem.setImage(part.getTitleImage());

		// String toolTipText = part.getTitleToolTip();
		// if (!toolTipText.equals(Util.ZERO_LENGTH_STRING)) {
		// tabItem.setToolTipText(toolTipText);
		// }

		// FontRegistry registry =
		// PlatformUI.getWorkbench().
		// getThemeManager().getCurrentTheme().
		// getFontRegistry();
		//		
		// if(part.isBusy())
		// tabItem.setFont(registry.getItalic(IWorkbenchThemeConstants.TAB_TEXT_FONT));
		// else{
		// tabItem.setFont(registry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
		// }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#addPart(org.eclipse.ui.internal.skins.IPresentablePart,
	 *      org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void addPart(IPresentablePart newPart, Object cookie) {

		int idx;

		if (cookie instanceof Integer) {
			idx = ((Integer) cookie).intValue();
		} else {
			// Select a location for newly inserted parts
			idx = paneFolder.getItemCount();
		}

		addPart(newPart, idx);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#removePart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
		if (current == oldPart) {
			titleLabel.setImage(null);
			current = null;
		}

		CTabItem item = getTab(oldPart);
		if (item == null) {
			return;
		}
		oldPart.setVisible(false);

		item.dispose();

		// Layout the folder again in case there is only one item
		setControlSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#selectPart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void selectPart(IPresentablePart toSelect) {
		if (toSelect == current) {
			return;
		}

		IPresentablePart oldPart = current;

		current = toSelect;

		if (current != null) {
			paneFolder.setSelection(indexOf(current));
			current.setVisible(true);
			updateCloseButton();
			setControlSize();
		}

		if (oldPart != null) {
			oldPart.setVisible(false);
		}
	}

	/**
	 * @return the current part
	 */
	public IPresentablePart getCurrentPart() {
		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		if (getSite().getState() == IStackPresentationSite.STATE_MINIMIZED) {
			bounds = Geometry.copy(bounds);
			bounds.height = computePreferredSize(false, Integer.MAX_VALUE,
					bounds.width, Integer.MAX_VALUE);
		}

		paneFolder.getControl().setBounds(bounds);
		setControlSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		Point result = Geometry.getSize(paneFolder.computeTrim(0, 0, 0, 0));

		result.x += 100;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
		if (current != null) {
			current.setVisible(isVisible);
		}
		paneFolder.getControl().setVisible(isVisible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
	 */
	public void setState(int state) {
		// tabFolder.setState(state);
	}

	/**
	 * @return the menu manager
	 */
	public IMenuManager getSystemMenuManager() {
		return systemMenuManager;
	}

	/**
	 * @param point
	 */
	protected void showSystemMenu(Point point) {
		Menu aMenu = systemMenuManager.createContextMenu(paneFolder
				.getControl().getParent());
		systemMenuManager.update(true);
		aMenu.setLocation(point.x, point.y);
		aMenu.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#getControl()
	 */
	public Control getControl() {
		return paneFolder.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#dragOver(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.swt.graphics.Point)
	 */
	public StackDropResult dragOver(Control currentControl, Point location) {

		// Determine which tab we're currently dragging over
		Point localPos = paneFolder.getControl().toControl(location);
		final CTabItem tabUnderPointer = paneFolder.getItem(localPos);

		// This drop target only deals with tabs... if we're not dragging over
		// a tab, exit.
		if (tabUnderPointer == null) {
			return null;
		}

		// workaround when left tab is dragged over next
		int dragOverIndex = paneFolder.indexOf(tabUnderPointer);

		return new StackDropResult(Geometry.toDisplay(paneFolder.getControl(),
				tabUnderPointer.getBounds()), new Integer(dragOverIndex));
	}

	/**
	 * Returns the toolbar control for the currently selected part, or null if
	 * none (not all parts have a toolbar).
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
		IPresentablePart part = getCurrentPart();
		if (part != null) {
			Rectangle bounds = DragUtil.getDisplayBounds(paneFolder
					.getControl());

			int idx = paneFolder.getSelectionIndex();
			if (idx > -1) {
				CTabItem item = paneFolder.getItem(idx);
				Rectangle itemBounds = item.getBounds();

				bounds.x += itemBounds.x;
				bounds.y += itemBounds.y;
			}

			Point location = new Point(bounds.x, bounds.y
					+ paneFolder.getTabHeight());
			showSystemMenu(location);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#getTabList(IPresentablePart)
	 */
	public Control[] getTabList(IPresentablePart part) {
		ArrayList list = new ArrayList();
		if (paneFolder.getTabPosition() == SWT.BOTTOM) {
			if (part.getToolBar() != null) {
				list.add(part.getToolBar());
			}
			if (part.getControl() != null) {
				list.add(part.getControl());
			}
			if (getPaneFolder() != null) {
				list.add(getPaneFolder().getControl());
			}
		} else {
			if (getPaneFolder() != null) {
				list.add(getPaneFolder().getControl());
			}
			if (part.getToolBar() != null) {
				list.add(part.getToolBar());
			}
			if (part.getControl() != null) {
				list.add(part.getControl());
			}
		}
		return (Control[]) list.toArray(new Control[list.size()]);
	}

	/**
	 * @param parentShell 
	 * @param x 
	 * @param y 
	 */
	protected void showList(Shell parentShell, int x, int y) {
		// final R21PaneFolder tabFolder = getTabFolder();
		//
		// int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
		// int tableStyle = SWT.V_SCROLL | SWT.H_SCROLL;
		// final BasicStackList editorList = new
		// BasicStackList(tabFolder.getControl().getShell(),
		// shellStyle, tableStyle);
		// editorList.setInput(this);
		// Point size = editorList.computeSizeHint();
		//        
		// Rectangle bounds = Display.getCurrent().getBounds();
		// if (x + size.x > bounds.width) x = bounds.width - size.x;
		// if (y + size.y > bounds.height) y = bounds.height - size.y;
		// editorList.setLocation(new Point(x, y));
		// editorList.setVisible(true);
		// editorList.setFocus();
		// editorList.getTableViewer().getTable().getShell().addListener(
		// SWT.Deactivate, new Listener() {
		//
		// public void handleEvent(Event event) {
		// editorList.setVisible(false);
		// }
		// });
	}

	/*
	 * Shows the list of tabs at the top left corner of the editor
	 */
	protected void showListDefaultLocation() {
		R21PaneFolder tabFolder = getPaneFolder();
		Shell shell = tabFolder.getControl().getShell();
		Rectangle clientArea = tabFolder.getClientArea();
		Point location = tabFolder.getControl().getDisplay().map(
				tabFolder.getControl(), null, clientArea.x, clientArea.y);
		showList(shell, location.x, location.y);
	}

	void setSelection(CTabItem tabItem) {
		getSite().selectPart(getPartForTab(tabItem));
	}

	void close(IPresentablePart presentablePart) {
		getSite().close(new IPresentablePart[] { presentablePart });
	}

	Image getLabelImage(IPresentablePart presentablePart) {
		return presentablePart.getTitleImage();
	}

	/**
	 * @param presentablePart
	 * @param includePath  
	 */
	String getLabelText(IPresentablePart presentablePart, boolean includePath) {
		String title = presentablePart.getTitle().trim();
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
	 */
	public void setActive(int newState) {
		setActive(newState == AS_ACTIVE_FOCUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#restoreState(org.eclipse.ui.presentations.IPresentationSerializer,
	 *      org.eclipse.ui.IMemento)
	 */
	public void restoreState(IPresentationSerializer serializer,
			IMemento savedState) {
		IMemento[] parts = savedState.getChildren(IWorkbenchConstants.TAG_PART);

		for (int idx = 0; idx < parts.length; idx++) {
			String id = parts[idx].getString(IWorkbenchConstants.TAG_ID);

			if (id != null) {
				IPresentablePart part = serializer.getPart(id);

				if (part != null) {
					addPart(part, getPaneFolder().getItemCount());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#saveState(org.eclipse.ui.presentations.IPresentationSerializer,
	 *      org.eclipse.ui.IMemento)
	 */
	public void saveState(IPresentationSerializer context, IMemento memento) {
		super.saveState(context, memento);

		List parts = getPresentableParts();

		Iterator iter = parts.iterator();
		while (iter.hasNext()) {
			IPresentablePart next = (IPresentablePart) iter.next();

			IMemento childMem = memento
					.createChild(IWorkbenchConstants.TAG_PART);
			childMem.putString(IWorkbenchConstants.TAG_ID, context.getId(next));
		}
	}

	/**
	 * Returns the List of IPresentablePart currently in this presentation
	 */
	private List getPresentableParts() {
		Assert.isTrue(!isDisposed());

		CTabItem[] items = getPaneFolder().getItems();
		List result = new ArrayList(items.length);

		for (int idx = 0; idx < getPaneFolder().getItemCount(); idx++) {
			result.add(getPartForTab(items[idx]));
		}

		return result;
	}
}
