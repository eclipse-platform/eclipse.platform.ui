/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.r21.R21Colors;
import org.eclipse.ui.internal.presentations.r21.R21PresentationMessages;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabFolder;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabFolderEvent;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabFolderListener;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabItem;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * A stack presentation for editors using a widget set that is close to what was
 * provided in 2.1.
 * <p>
 * EXPERIMENTAL
 * </p>
 * 
 * @since 3.0
 */
public class R21EditorStackPresentation extends StackPresentation {

	/** the tab folder */
	private CTabFolder tabFolder;

	/** the drag listener */
	private Listener dragListener = new Listener() {

		public void handleEvent(Event event) {
			Point localPos = new Point(event.x, event.y);
			CTabItem tabUnderPointer = tabFolder.getItem(localPos);

			if (tabUnderPointer == null) {
				// drag the entire stack
				if (getSite().isStackMoveable()) {
					getSite().dragStart(tabFolder.toDisplay(localPos), false);
				}
				return;
			}

			IPresentablePart part = getPartForTab(tabUnderPointer);

			if (getSite().isPartMoveable(part)) {
				// drag the part
				getSite().dragStart(part, tabFolder.toDisplay(localPos), false);
			}
		}
	};

	/** the listener that will close the tab */
	private CTabFolderListener closeListener = new CTabFolderListener() {

		public void itemClosed(CTabFolderEvent e) {
			CTabItem item = (CTabItem) e.item;
			if (null != item) {
				e.doit = false; // otherwise tab is auto disposed on return
				getSite().close(new IPresentablePart[] { getPartForTab(item) });
			}
		}
	};

	/** the current part */
	private IPresentablePart current;

	/** the system menu */
	private MenuManager systemMenuManager = new MenuManager();

	/** the shared preference store */
	private static IPreferenceStore preferenceStore = WorkbenchPlugin
			.getDefault().getPreferenceStore();

	// don't reset this dynamically, so just keep the information static.
	// see bug:
	// 75422 [Presentations] Switching presentation to R21 switches immediately,
	// but only partially
	private static int tabPos = PlatformUI.getPreferenceStore()
			.getInt(IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION);

	/** the tab item property holding the part */
	private final static String TAB_DATA = R21EditorStackPresentation.class
			.getName()
			+ ".partId"; //$NON-NLS-1$

	/** the mouse listener for setting focus */
	private MouseListener mouseListener = new MouseAdapter() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Control) {
				Control ctrl = (Control) e.widget;

				Point globalPos = ctrl.toDisplay(new Point(e.x, e.y));

				CTabItem newItem = tabFolder.getItem(tabFolder
						.toControl(globalPos));
				if (newItem != null) {

					// show menu over icon
					if ((e.button == 1) && overImage(newItem, e.x)) {
						getSite().selectPart(getPartForTab(newItem));
						showSystemMenu();
					}

					// PR#1GDEZ25 - If selection will change in mouse up ignore
					// mouse down.
					CTabItem oldItem = tabFolder.getSelection();
					if (newItem != oldItem) {
						return;
					}
				}

				// set focus
				if (current != null) {
					current.setFocus();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			if (getSite().getState() == IStackPresentationSite.STATE_MAXIMIZED) {
				getSite().setState(IStackPresentationSite.STATE_RESTORED);
			} else {
				getSite().setState(IStackPresentationSite.STATE_MAXIMIZED);
			}
		}
	};

	/**
	 * Return true if <code>x</code> is over the tab item image.
	 * 
	 * @return true if <code>x</code> is over the tab item image
	 */
	static boolean overImage(CTabItem item, int x) {
		Rectangle imageBounds = item.getImage().getBounds();
		return x < (item.getBounds().x + imageBounds.x + imageBounds.width);
	}

	/** the menu listener for showing the menu */
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

	/** the selection listener */
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

	/** a property change listener for the parts */
	private IPropertyListener childPropertyChangeListener = new IPropertyListener() {

		public void propertyChanged(Object source, int property) {
			if (source instanceof IPresentablePart) {
				IPresentablePart part = (IPresentablePart) source;
				childPropertyChanged(part, property);
			}
		}
	};

	/** a dispose listener to do some cleanups when a tab is disposed */
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
			updateGradient();
		}

		public void shellDeactivated(ShellEvent event) {
			updateGradient();
		}
	};

	/**
	 * Create a new presentation stack.
	 * 
	 * @param parent
	 *            the parent widget
	 * @param stackSite
	 *            the site
	 */
	public R21EditorStackPresentation(Composite parent,
			IStackPresentationSite stackSite) {
		super(stackSite);

		// create the tab folder
		tabFolder = new CTabFolder(parent, tabPos | SWT.BORDER);

		// minimum tab width
		tabFolder.MIN_TAB_WIDTH = preferenceStore
				.getInt(IPreferenceConstants.EDITOR_TAB_WIDTH);

		// prevent close button and scroll buttons from taking focus
		tabFolder.setTabList(new Control[0]);

		// enable close button in tab folder
		tabFolder.addCTabFolderListener(closeListener);

		// listener to switch between visible tabItems
		tabFolder.addListener(SWT.Selection, selectionListener);

		// listener to resize visible components
		tabFolder.addListener(SWT.Resize, resizeListener);

		// listen for mouse down on tab to set focus, show system menu and
		// maximize/restore.
		tabFolder.addMouseListener(mouseListener);

		// the menu
		tabFolder.addListener(SWT.MenuDetect, menuListener);

		// register drag listener
		PresentationUtil.addDragListener(tabFolder, dragListener);

		// add the shell listener to track shell activations
		// TODO: check if workaround can be removed (see bug 55458)
		tabFolder.getShell().addShellListener(shellListener);

		// initialize system menu
		populateSystemMenu(systemMenuManager);
	}

	/**
	 * Initializes the specified menu manager.
	 * 
	 * @param menuManager
	 */
	private void populateSystemMenu(IMenuManager menuManager) {

		menuManager.add(new GroupMarker("misc")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("restore")); //$NON-NLS-1$
		menuManager.add(new UpdatingActionContributionItem(
				new SystemMenuRestore(getSite())));
		menuManager.add(new SystemMenuMove(getSite(), getPaneName()));
		menuManager.add(new GroupMarker("size")); //$NON-NLS-1$
		menuManager.add(new GroupMarker("state")); //$NON-NLS-1$
		// systemMenuManager.add(new UpdatingActionContributionItem(new
		// SystemMenuMinimize(getSite())));
		menuManager.add(new UpdatingActionContributionItem(
				new SystemMenuMaximize(getSite())));
		menuManager.add(new Separator("close")); //$NON-NLS-1$
		menuManager.add(new UpdatingActionContributionItem(new SystemMenuClose(
				getSite())));

		getSite().addSystemActions(menuManager);
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
			return tabFolder.getItemCount();
		}

		CTabItem[] items = tabFolder.getItems();
		for (int idx = 0; idx < items.length; idx++) {
			if (part == getPartForTab(items[idx])) {
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
		CTabItem[] items = tabFolder.getItems();
		int idx = indexOf(part);
		return idx < items.length ? items[idx] : null;
	}

	/**
	 * @param part
	 * @param property
	 */
	protected void childPropertyChanged(IPresentablePart part, int property) {
		initTab(getTab(part), part);
	}

	protected final IPresentablePart getPartForTab(CTabItem item) {
		return (IPresentablePart) item.getData(TAB_DATA);
	}

	protected CTabFolder getTabFolder() {
		return tabFolder;
	}

	/**
	 * Answer whether the receiver is disposed.
	 * 
	 * @return boolean <code>true</code> if disposed
	 */
	public boolean isDisposed() {
		return tabFolder == null || tabFolder.isDisposed();
	}

	/**
	 * Set the size of a page in the folder.
	 */
	private void setControlSize() {
		if (current != null && tabFolder != null) {
			current.setBounds(calculatePageBounds(tabFolder));
		}
	}

	/**
	 * Calculate the bounds of the client area inside the folder
	 * 
	 * @param folder
	 * @return Rectangle the bounds of the client
	 */
	public static Rectangle calculatePageBounds(CTabFolder folder) {
		if (folder == null) {
			return new Rectangle(0, 0, 0, 0);
		}

		Rectangle bounds = folder.getBounds();
		Rectangle offset = folder.getClientArea();
		bounds.x += offset.x;
		bounds.y += offset.y;
		bounds.width = offset.width;
		bounds.height = offset.height;
		return bounds;
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
		tabFolder.getShell().removeShellListener(shellListener);

		// remove close listener
		tabFolder.removeCTabFolderListener(closeListener);

		// remove drag listener
		PresentationUtil.removeDragListener(tabFolder, dragListener);

		// dispose system menu manager
		systemMenuManager.dispose();
		systemMenuManager.removeAll();

		// dispose tab folder
		tabFolder.dispose();
		tabFolder = null;
	}

	/** the active state */
	private int activeState = AS_INACTIVE;

	/**
	 * Update the tab folder's colours to match the current theme settings and
	 * active state
	 */
	private void updateGradient() {

		if (isDisposed()) {
			return;
		}

		Color fgColor;
		Color[] bgColors;
		int[] bgPercents;
		boolean vertical = false;
		if (activeState == AS_ACTIVE_FOCUS) {
			if (getShellActivated()) {
				fgColor = R21Colors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
				bgColors = R21Colors.getActiveEditorGradient();
				bgPercents = R21Colors.getActiveEditorGradientPercents();
			} else {
				fgColor = R21Colors
						.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
				bgColors = R21Colors.getDeactivatedEditorGradient();
				bgPercents = R21Colors.getDeactivatedEditorGradientPercents();
			}

		} else if (activeState == AS_ACTIVE_NOFOCUS) {
			fgColor = R21Colors.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
			bgColors = R21Colors.getActiveNoFocusEditorGradient();
			bgPercents = R21Colors.getActiveNoFocusEditorGradientPercents();
		} else {
			fgColor = null;
			bgColors = null;
			bgPercents = null;
		}

		drawGradient(fgColor, bgColors, bgPercents, vertical);
	}

	/**
	 * Sets the gradient for the selected tab
	 * 
	 * @param fgColor
	 * @param bgColors
	 * @param percentages
	 * @param vertical
	 */
	protected void drawGradient(Color fgColor, Color[] bgColors,
			int[] percentages, boolean vertical) {
		tabFolder.setSelectionForeground(fgColor);
		tabFolder.setSelectionBackground(bgColors, percentages);
		tabFolder.update();
	}

	/**
	 * Return whether the window's shell is activated
	 */
	/* package */boolean getShellActivated() {
		Window window = getWindow();
		if (window instanceof WorkbenchWindow) {
			return ((WorkbenchWindow) window).getShellActivated();
		}
		return false;
	}

	/**
	 * Returns the top level window.
	 * 
	 * @return Window the window for the receiver
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

	/**
	 * Creates the tab item for the specified part.
	 * 
	 * @param part
	 * @param tabIndex
	 * @return the tab item for the part
	 */
	private CTabItem createPartTab(IPresentablePart part, int tabIndex) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE, tabIndex);
		tabItem.setData(TAB_DATA, part);
		part.addPropertyListener(childPropertyChangeListener);
		tabItem.addDisposeListener(tabDisposeListener);
		initTab(tabItem, part);
		return tabItem;
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

		// set tab text and tooltip
		tabItem.setText(getLabelText(part, true, false));
		tabItem.setToolTipText(getLabelToolTipText(part));

		// set tab image
		tabItem.setImage(getLabelImage(part));

		// following code allows a disabled image
		// but the result was distracting: didn't see any disabled image

		// Image image = getLabelImage(part);
		// boolean useColorIcons = false; // should we use a preference setting?
		//
		// if (image == null || image.isDisposed()) {
		// // normal image
		// tabItem.setImage(null);
		// // disabled image
		// if (!useColorIcons) {
		// Image disableImage = tabItem.getDisabledImage();
		// if (disableImage != null) {
		// disableImage.dispose();
		// tabItem.setDisabledImage(null);
		// }
		// }
		// } else if (!image.equals(tabItem.getImage())) {
		// // normal image
		// tabItem.setImage(image);
		// // disabled image
		// if (!useColorIcons) {
		// Image disableImage = tabItem.getDisabledImage();
		// if (disableImage != null)
		// disableImage.dispose();
		// Display display = tabItem.getDisplay();
		// disableImage = new Image(display, image, SWT.IMAGE_DISABLE);
		// tabItem.setDisabledImage(disableImage);
		// }
		// }

	}

	/**
	 * Returns the label text that should be used for the tab item for the
	 * specified part
	 * 
	 * @param presentablePart
	 * @param dirtyLeft
	 * @param includePath
	 * @return a formated label text
	 */
	String getLabelText(IPresentablePart presentablePart, boolean dirtyLeft,
			boolean includePath) {
		String title = presentablePart.getName().trim();
		String text = title;

		if (includePath) {
			String titleTooltip = presentablePart.getTitleToolTip().trim();

			if (titleTooltip.endsWith(title)) {
				titleTooltip = titleTooltip.substring(0,
						titleTooltip.lastIndexOf(title)).trim();
			}

			if (titleTooltip.endsWith("\\")) { //$NON-NLS-1$
				titleTooltip = titleTooltip.substring(0,
						titleTooltip.lastIndexOf("\\")).trim(); //$NON-NLS-1$
			}

			if (titleTooltip.endsWith("/")) { //$NON-NLS-1$
				titleTooltip = titleTooltip.substring(0,
						titleTooltip.lastIndexOf("/")).trim(); //$NON-NLS-1$
			}

			if (titleTooltip.length() >= 1) {
				text += " - " + titleTooltip; //$NON-NLS-1$
			}
		}

		if (presentablePart.isDirty()) {
			if (dirtyLeft) {
				text = "* " + text; //$NON-NLS-1$
			} else {
				text = text + " *"; //$NON-NLS-1$
			}
		}

		return text;
	}

	/**
	 * Returns the image used for the tab item
	 * 
	 * @param presentablePart
	 * @return an image
	 */
	Image getLabelImage(IPresentablePart presentablePart) {
		return presentablePart.getTitleImage();
	}

	/**
	 * Returns the tool tip text used for the tab item
	 * 
	 * @param presentablePart
	 * @return a tool tip text
	 */
	String getLabelToolTipText(IPresentablePart presentablePart) {
		return presentablePart.getTitleToolTip();
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
			idx = tabFolder.getItemCount();
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

		// setControlSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#removePart(org.eclipse.ui.internal.skins.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
		if (current == oldPart) {
			current = null;
		}

		CTabItem item = getTab(oldPart);
		if (item == null) {
			return;
		}
		oldPart.setVisible(false);

		item.dispose();
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
			tabFolder.setSelection(indexOf(current));
			current.setVisible(true);
			setControlSize();

		}

		if (oldPart != null) {
			oldPart.setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		tabFolder.setBounds(bounds);
		setControlSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		return Geometry.getSize(tabFolder.computeTrim(0, 0, 0, 0));
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

		getTabFolder().setVisible(isVisible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.Presentation#setState(int)
	 */
	public void setState(int state) {
		// tabFolder.setMinimized(state == IPresentationSite.STATE_MINIMIZED);
		// tabFolder.setMaximized(state == IPresentationSite.STATE_MAXIMIZED);
	}

	/**
	 * Returns the system menu manager.
	 * 
	 * @return the system menu manager
	 */
	public IMenuManager getSystemMenuManager() {
		return systemMenuManager;
	}

	/**
	 * Shows the system context menu at the specified location
	 * 
	 * @param point
	 */
	protected void showSystemMenu(Point point) {
		Menu aMenu = systemMenuManager.createContextMenu(tabFolder.getParent());
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
		return tabFolder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.skins.StackPresentation#dragOver(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.swt.graphics.Point)
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

		// workaround when left tab is dragged over next
		int dragOverIndex = tabFolder.indexOf(tabUnderPointer);

		return new StackDropResult(Geometry.toDisplay(tabFolder,
				tabUnderPointer.getBounds()), new Integer(dragOverIndex));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
		if (null != current) {
			// switch to the editor
			CTabItem item = getTab(current);
			getSite().selectPart(getCurrentPart());
			Rectangle bounds = item.getBounds();
			int y = bounds.height;
			if (getTabFolder().getTabPosition() == SWT.BOTTOM) {
				y += bounds.y;
			}
			showSystemMenu(getTabFolder().toDisplay(bounds.x, y));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#showPaneMenu()
	 */
	public void showPaneMenu() {
		IPartMenu menu = getPartMenu();

		if (null != menu) {
			CTabItem tab = getTab(getCurrentPart());

			if (null != tab && null != tab.getControl()) {
				Rectangle bounds = DragUtil.getDisplayBounds(tab.getControl());
				menu.showMenu(new Point(bounds.x, bounds.y + bounds.height));
			}
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
	 * @see org.eclipse.ui.presentations.StackPresentation#getTabList(IPresentablePart)
	 */
	public Control[] getTabList(IPresentablePart part) {
		ArrayList list = new ArrayList();
		if (getControl() != null) {
			list.add(getControl());
		}
		if (part.getToolBar() != null) {
			list.add(part.getToolBar());
		}
		if (part.getControl() != null) {
			list.add(part.getControl());
		}
		return (Control[]) list.toArray(new Control[list.size()]);
	}

	/**
	 * @return the current part
	 */
	public IPresentablePart getCurrentPart() {
		return current;
	}

	protected String getPaneName() {
		return R21PresentationMessages.getString("EditorPane.moveEditor"); //$NON-NLS-1$ 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
	 */
	public void setActive(int newState) {
		activeState = newState;
		updateGradient();
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
     * Returns the List of IPresentablePart currently in this presentation
     */
    private List getPresentableParts() {
        Assert.isTrue(!isDisposed());
        
        CTabItem[] items = tabFolder.getItems();
        List result = new ArrayList(items.length);
        
        for (int idx = 0; idx < tabFolder.getItemCount(); idx++) {
            result.add(getPartForTab(items[idx]));
        }
        
        return result;
    }
    }
