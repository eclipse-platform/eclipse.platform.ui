package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.ColorSchemeService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder2;
import org.eclipse.swt.custom.CTabFolderCloseAdapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabFolderListener;
import org.eclipse.swt.custom.CTabFolderMinMaxAdapter;
import org.eclipse.swt.custom.CTabItem2;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dnd.AbstractDragSource;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.themes.ITabThemeDescriptor;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.internal.themes.TabThemeDescriptor;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager;
import org.eclipse.ui.progress.UIJob;

public class PartTabFolder extends LayoutPart implements ILayoutContainer, IPropertyListener, IWorkbenchDragSource {
	
	private static final int STATE_MINIMIZED = 0;
	private static final int STATE_RESTORED = 1;
	private static final int STATE_MAXIMIZED = 2;
	
	private static int tabLocation = -1; // Initialized in constructor.

	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();

	private CTabFolder2 tabFolder;
	private Map mapTabToPart = new HashMap();
	private LayoutPart current;
	private boolean assignFocusOnSelection = true;
	private int viewState = STATE_RESTORED;
	private WorkbenchPage page;

	// inactiveCurrent is only used when restoring the persisted state of
	// perspective on startup.
	private LayoutPart inactiveCurrent;
	private Composite parent;
	private boolean active = false;

	/**
	 * Makes changes in the minimize state be exposed in the tab folder 
	 */
	IChangeListener stateListener = new IChangeListener() {
		public void update(boolean changed) {
		}
	};
	
	/**
	 * Sets the minimized state based on the state of the tab folder
	 */
	CTabFolderMinMaxAdapter expandListener = new CTabFolderMinMaxAdapter() {
		
		public void minimize(CTabFolderEvent event) {
			setState(STATE_MINIMIZED);	
		}
		
		public void restore(CTabFolderEvent event) {
			setState(STATE_RESTORED);
		}
		
		public void maximize(CTabFolderEvent event) {
			setState(STATE_MAXIMIZED);
		}
	};
	
	// listen for mouse down on tab to set focus.
	private MouseListener mouseListener = new MouseAdapter() {

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
//
//			if (current instanceof PartPane) {
//				WorkbenchPage page = ((PartPane) current).getPage();
//				if (current instanceof ViewPane) {
//					if (page.isZoomed()) {
//						setState(STATE_RESTORED);
//					} else {
//						setState(STATE_MAXIMIZED);
//					}
//				}
//			}

		}

		public void mouseDown(MouseEvent e) {
			// PR#1GDEZ25 - If selection will change in mouse up ignore mouse down.
			// Else, set focus.
			CTabItem2 newItem = tabFolder.getItem(new Point(e.x, e.y));
			if (newItem != null) {
				CTabItem2 oldItem = tabFolder.getSelection();
				if (newItem != oldItem)
					return;
			}
			if (PartTabFolder.this.current != null) {
				PartTabFolder.this.current.setFocus();
				//tabFolder.setBorderVisible(true);
			}
		}

	};

	private class TabInfo {
		private String tabText;
		private LayoutPart part;
		private Image partImage;
		private boolean fixed;
	}

	private TabInfo[] invisibleChildren;

	// Themes
	private String themeid;
	private ITabThemeDescriptor tabThemeDescriptor;
	private int tabPosition = -1;

	/**
	 * PartTabFolder constructor comment.
	 */
	public PartTabFolder(WorkbenchPage page) {
		super("PartTabFolder"); //$NON-NLS-1$
		setID(this.toString());
		// Each folder has a unique ID so relative positioning is unambiguous.
		
		// save off a ref to the page
		//@issue is it okay to do this??
		//I think so since a PartTabFolder is
		//not used on more than one page.
		this.page = page;

		// Get the location of the tabs from the preferences
		if (tabLocation == -1)
			tabLocation =
				WorkbenchPlugin.getDefault().getPreferenceStore().getInt(
					IPreferenceConstants.VIEW_TAB_POSITION);

	}
	/**
	 * Add a part at an index.
	 */
	public void add(String name, int index, LayoutPart part) {		
		if (active && !(part instanceof PartPlaceholder)) {
			CTabItem2 tab = createPartTab(part, name, null, index);
			index = tabFolder.indexOf(tab);
			setSelection(index);
		} else {
			TabInfo info = new TabInfo();
			if (part instanceof PartPane) {
				WorkbenchPartReference ref =
					(WorkbenchPartReference) ((PartPane) part).getPartReference();
				info.partImage = ref.getTitleImage();
			}
			info.tabText = name;
			info.part = part;
			invisibleChildren = arrayAdd(invisibleChildren, info, index);
			if (active)
				part.setContainer(this);
		}
	}
	/**
	 * Add a part at an index. Also use Image
	 */
	public void add(String name, int index, LayoutPart part, Image partImage) {
		if (active && !(part instanceof PartPlaceholder)) {
			CTabItem2 tab = createPartTab(part, name, partImage, index);
			index = tabFolder.indexOf(tab);
			setSelection(index);
		} else {
			TabInfo info = new TabInfo();
			if (partImage != null)
				info.partImage = partImage;
			else if (part instanceof PartPane) {
				WorkbenchPartReference ref =
					(WorkbenchPartReference) ((PartPane) part).getPartReference();
				info.partImage = ref.getTitleImage();
			}
			info.tabText = name;
			info.part = part;
			invisibleChildren = arrayAdd(invisibleChildren, info, index);
			if (active)
				part.setContainer(this);
		}
	}

	/**
	 * See IVisualContainer#add
	 */
	public void add(LayoutPart child) {
		int index = getItemCount();
		String label = ""; //$NON-NLS-1$
		Image partimage = null;
		if (child instanceof PartPane) {
			WorkbenchPartReference ref =
				(WorkbenchPartReference) ((PartPane) child).getPartReference();
			label = ref.getRegisteredName();
			partimage = ref.getTitleImage();
		}
		add(label, index, child, partimage);
	}
	/**
	 * See ILayoutContainer::allowBorder
	 *
	 * There is already a border around the tab
	 * folder so no need for one from the parts.
	 */
	public boolean allowsBorder() {
		// @issue need to support old look even if a theme is set (i.e. show border
		//   even when only one item) -- separate theme attribute, or derive this
		//   from existing attributes?
		// @issue this says to show the border only if there are no items, but 
		//   in this case the folder should not be visible anyway
//		if (tabThemeDescriptor != null)
//			return (mapTabToPart.size() < 1);
//		return mapTabToPart.size() <= 1;
		return false;
	}
	private TabInfo[] arrayAdd(TabInfo[] array, TabInfo item, int index) {

		if (item == null)
			return array;

		TabInfo[] result = null;

		if (array == null) {
			result = new TabInfo[1];
			result[0] = item;
		} else {
			if (index >= array.length)
				index = array.length;
			result = new TabInfo[array.length + 1];
			System.arraycopy(array, 0, result, 0, index);
			result[index] = item;
			System.arraycopy(array, index, result, index + 1, array.length - index);
		}

		return result;
	}
	private TabInfo[] arrayRemove(TabInfo[] array, LayoutPart item) {

		if (item == null)
			return array;

		TabInfo[] result = null;

		int index = -1;
		for (int i = 0, length = array.length; i < length; i++) {
			if (item == array[i].part) {
				index = i;
				break;
			}
		}
		if (index == -1)
			return array;

		if (array.length > 1) {
			result = new TabInfo[array.length - 1];
			System.arraycopy(array, 0, result, 0, index);
			System.arraycopy(array, index + 1, result, index, result.length - index);
		}
		return result;
	}
	/**
	 * Set the default bounds of a page in a CTabFolder.
	 */
	protected static Rectangle calculatePageBounds(CTabFolder2 folder) {
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
	
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
			if (IPreferenceConstants.VIEW_TAB_POSITION.equals(propertyChangeEvent.getProperty()) && tabFolder != null) {
				int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
				int style = SWT.CLOSE | SWT.BORDER | tabLocation;
				tabFolder.setStyle(style);
			}
		}
	};	
	
	public void createControl(Composite parent) {

		if (tabFolder != null)
			return;

		// Create control.	
		this.parent = parent;
		
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
		int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
		
		// TODO probably won't work, given the code above..
		if (tabPosition == -1) {
			if (tabThemeDescriptor != null)
				tabPosition = tabThemeDescriptor.getTabPosition();
			else
				tabPosition = tabLocation;
		}
		
		tabFolder = new CTabFolder2(parent, tabLocation | SWT.BORDER | SWT.CLOSE);
		//tabFolder.setBorderVisible(true);
		// set basic colors
		ColorSchemeService.setTabColors(tabFolder);
		// draw a gradient for the selected tab
		drawGradient(false);
		
		if (tabThemeDescriptor != null) {
			useThemeInfo();
		}

		// listener to switch between visible tabItems
		tabFolder.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LayoutPart item = (LayoutPart) mapTabToPart.get(e.item);
				// Item can be null when tab is just created but not map yet.
				if (item != null) {
					setSelection(item);
					if (assignFocusOnSelection)
						item.setFocus();
				}
			}
		});

		// listener to resize visible components
		tabFolder.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				setControlSize();
			}
		});

		// listen for mouse down on tab to set focus.
		tabFolder.addMouseListener(this.mouseListener);

		tabFolder.addListener(SWT.MenuDetect, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				LayoutPart part = PartTabFolder.this.current;
				if (part instanceof PartPane) {
					((PartPane) part).showPaneMenu(tabFolder, new Point(event.x, event.y));
				}

			}
		});

		tabFolder.addCTabFolderCloseListener(new CTabFolderCloseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.custom.CTabFolderCloseAdapter#itemClosed(org.eclipse.swt.custom.CTabFolderEvent)
			 */
			public void itemClosed(CTabFolderEvent event) {
				LayoutPart item = (LayoutPart) mapTabToPart.get(event.item);
				if (item instanceof ViewPane)
					 ((ViewPane) item).doHide();
				//remove(item);
			}
		});
				
		DragUtil.addDragSource(tabFolder, new AbstractDragSource() {

			public Object getDraggedItem(Point position) {
				Point localPos = tabFolder.toControl(position);
				CTabItem2 tabUnderPointer = tabFolder.getItem(localPos);
				
				if (tabUnderPointer == null) {
					return null;
				}
				
				return mapTabToPart.get(tabUnderPointer);
			}

			public Rectangle getDragRectangle(Object draggedItem) {
				return DragUtil.getDisplayBounds(tabFolder);
			}
			
		});
		
		DragUtil.addDragTarget(tabFolder, new IDragOverListener() {

			public IDropTarget drag(Control currentControl, final Object draggedObject, 
					Point position, Rectangle dragRectangle) {
				
				if (!(draggedObject instanceof ViewPane)) {
					return null;
				}
				
				final ViewPane pane = (ViewPane)draggedObject;
				
				Point localPos = tabFolder.toControl(position);
				final CTabItem2 tabUnderPointer = tabFolder.getItem(localPos);
				
				if (tabUnderPointer == null || mapTabToPart.get(tabUnderPointer) == draggedObject) {
					return null;
				}
				
				return new IDropTarget() {

					public void drop() {
						if (pane.getContainer() != PartTabFolder.this) { 
							page.removeFastView(pane.getViewReference());
							page.getActivePerspective().getPresentation().derefPart(pane);
							pane.reparent(getParent());
							add(pane);
							setSelection(pane);	
							pane.setFocus();
						}
						
						reorderTab(pane, getTab(pane), tabFolder.indexOf(tabUnderPointer));

						getParent().setRedraw(true);
					}

					public Cursor getCursor() {
						return DragCursors.getCursor(DragCursors.CENTER);
					}

					public Rectangle getSnapRectangle() {
						if (tabUnderPointer == null) {
							return Geometry.toDisplay(tabFolder.getParent(), tabFolder.getBounds());
						}
						
						return Geometry.toDisplay(tabFolder, tabUnderPointer.getBounds());
					}
				};
			}
			
		});
		
		// enable for drag & drop target
		tabFolder.setData(this);

		// Create pages.
		if (invisibleChildren != null) {
			TabInfo[] stillInactive = new TabInfo[0];
			int tabCount = 0;
			for (int i = 0, length = invisibleChildren.length; i < length; i++) {
				if (invisibleChildren[i].part instanceof PartPlaceholder) {
					invisibleChildren[i].part.setContainer(this);
					TabInfo[] newStillInactive = new TabInfo[stillInactive.length + 1];
					System.arraycopy(stillInactive, 0, newStillInactive, 0, stillInactive.length);
					newStillInactive[stillInactive.length] = invisibleChildren[i];
					stillInactive = newStillInactive;
				} else {
					if (invisibleChildren[i].partImage == null) {
						if (invisibleChildren[i].part instanceof PartPane) {
							WorkbenchPartReference ref =
								(WorkbenchPartReference) ((PartPane) invisibleChildren[i].part)
									.getPartReference();
							invisibleChildren[i].partImage = ref.getTitleImage();
						}
					}
					createPartTab(
						invisibleChildren[i].part,
						invisibleChildren[i].tabText,
						invisibleChildren[i].partImage,
						tabCount);
					++tabCount;
				}
			}
			invisibleChildren = stillInactive;
		}

		active = true;

		// Set current page.
		if (getItemCount() > 0) {
			int newPage = 0;
			if (current != null)
				newPage = indexOf(current);
			setSelection(newPage);
		}
		
		tabFolder.addCTabFolderMinMaxListener(expandListener);
		showMinMaxButtons(getContainer() != null);		
		updateControlState();
	}
		
	private CTabItem2 createPartTab(LayoutPart part, String tabName, Image tabImage, int tabIndex) {
		CTabItem2 tabItem;

		if (tabIndex < 0)
			tabItem = new CTabItem2(this.tabFolder, SWT.NONE);
		else
			tabItem = new CTabItem2(this.tabFolder, SWT.NONE, tabIndex);
		
		if (tabThemeDescriptor != null) {
			int showInTab = tabThemeDescriptor.getShowInTab();

			//		tabItem.setItemMargins(tabThemeDescriptor.getItemMargins());
			if (tabThemeDescriptor.isShowTooltip())
				tabItem.setToolTipText(tabName);

			if ((tabImage != null) && (showInTab != TabThemeDescriptor.TABLOOKSHOWTEXTONLY))
				tabItem.setImage(tabImage);
			if (showInTab != TabThemeDescriptor.TABLOOKSHOWICONSONLY)
				tabItem.setText(tabName);
			
			// @issue not sure of exact API on CTabItem
			//if (part instanceof ViewPane) {
			//	tabItem.setShowClose(!((ViewPane)part).isFixedView());
			//}
			
		} else {
			tabItem.setText(tabName);
		}
		
		if (part instanceof PartPane) {
			tabItem.setImage(((PartPane) part).getPartReference().getTitleImage());
		}

		mapTabToPart.put(tabItem, part);
		
		part.createControl(this.parent);
		part.setContainer(this);
		
		// Because the container's allow border api
		// is dependent on the number of tabs it has,
		// reset the container so the parts can update.
		if (mapTabToPart.size() == 2) {
			Iterator parts = mapTabToPart.values().iterator();
			((LayoutPart) parts.next()).setContainer(this);
			((LayoutPart) parts.next()).setContainer(this);
		}
				
		if (part instanceof PartPane) {
			WorkbenchPartReference ref =
				(WorkbenchPartReference) ((PartPane) part).getPartReference();
			ref.addPropertyListener(this);
		}

		updateControlBounds();
		return tabItem;
	}

	/**
	 * See LayoutPart#dispose
	 */
	public void dispose() {

		if (!active)
			return;

		// combine active and inactive entries into one
		TabInfo[] newInvisibleChildren = new TabInfo[mapTabToPart.size()];

		if (invisibleChildren != null) {
			// tack the inactive ones on at the end
			newInvisibleChildren =
				new TabInfo[newInvisibleChildren.length + invisibleChildren.length];
			System.arraycopy(
				invisibleChildren,
				0,
				newInvisibleChildren,
				mapTabToPart.size(),
				invisibleChildren.length);
		}

		Iterator keys = mapTabToPart.keySet().iterator();
		while (keys.hasNext()) {
			CTabItem2 item = (CTabItem2) keys.next();
			LayoutPart part = (LayoutPart) mapTabToPart.get(item);
			TabInfo info = new TabInfo();
			info.tabText = item.getText();
			info.part = part;
			newInvisibleChildren[tabFolder.indexOf(item)] = info;
		}

		invisibleChildren = newInvisibleChildren;

		if (invisibleChildren != null) {
			for (int i = 0, length = invisibleChildren.length; i < length; i++) {
				invisibleChildren[i].part.setContainer(null);
			}
		}

		mapTabToPart.clear();

		if (tabFolder != null) {
			tabFolder.removeCTabFolderMinMaxListener(expandListener);
			
			tabFolder.dispose();
		}

		tabFolder = null;

		active = false;
	}

	/**
	 * Open the tracker to allow the user to move
	 * the specified part using keyboard.
	 */
	public void openTracker(LayoutPart part) {
		DragUtil.performDrag(part, DragUtil.getDisplayBounds(part.getControl()));
	}
	/**
	 * Gets the presentation bounds.
	 */
	public Rectangle getBounds() {
		if (tabFolder == null) {
			return new Rectangle(0,0,0,0);
		}
		
		Rectangle result = tabFolder.getBounds();
				
		return result;
	}

	// getMinimumHeight() added by cagatayk@acm.org 
	/**
	 * @see LayoutPart#getMinimumHeight()
	 */
	public int getMinimumHeight() {
		//return 20;
		
		if (current == null || tabFolder == null || tabFolder.isDisposed())
			return super.getMinimumHeight();
		
		if (getItemCount() > 0) {
			int clientHeight = isMinimized() ? 0 : current.getMinimumHeight();
			
			Rectangle trim = tabFolder.computeTrim(0, 0, 0, clientHeight);
			return trim.height;
		} else
			return current.getMinimumHeight();
	}
	
	/**
	 * Sanity-checks this object, to verify that it is in a valid state.
	 */
	private void checkPart() {
		if (tabFolder != null && !tabFolder.isDisposed()) {
			Assert.isTrue(tabFolder.getMaximized() == (viewState == STATE_MAXIMIZED));
			Assert.isTrue(tabFolder.getMinimized() == isMinimized());
			if (isMinimized()) {
				Assert.isTrue(getBounds().height == getMinimumHeight());
			}
		}
		
		if (current != null) {
			//Assert.isTrue(isMinimized() == current.isVisible());
		}
	}

	/**
	 * See IVisualContainer#getChildren
	 */
	public LayoutPart[] getChildren() {
		LayoutPart[] children = new LayoutPart[0];

		if (invisibleChildren != null) {
			children = new LayoutPart[invisibleChildren.length];
			for (int i = 0, length = invisibleChildren.length; i < length; i++) {
				children[i] = invisibleChildren[i].part;
			}
		}

		int count = mapTabToPart.size();
		if (count > 0) {
			int index = children.length;
			LayoutPart[] newChildren = new LayoutPart[children.length + count];
			System.arraycopy(children, 0, newChildren, 0, children.length);
			children = newChildren;
			for (int nX = 0; nX < count; nX++) {
				CTabItem2 tabItem = tabFolder.getItem(nX);
				children[index] = (LayoutPart) mapTabToPart.get(tabItem);
				index++;
			}
		}

		return children;
	}
	public Control getControl() {
		return tabFolder;
	}
	/**
	 * Answer the number of children.
	 */
	public int getItemCount() {
		if (active)
			return tabFolder.getItemCount();
		else if (invisibleChildren != null)
			return invisibleChildren.length;
		else
			return 0;

	}
	/**
	 * Get the parent control.
	 */
	public Composite getParent() {
		return tabFolder.getParent();
	}
	public int getSelection() {
		if (!active)
			return 0;
		return tabFolder.getSelectionIndex();
	}
	/**
	 * Returns the tab for a part.
	 */
	private CTabItem2 getTab(LayoutPart child) {
		Iterator tabs = mapTabToPart.keySet().iterator();
		while (tabs.hasNext()) {
			CTabItem2 tab = (CTabItem2) tabs.next();
			if (mapTabToPart.get(tab) == child)
				return tab;
		}

		return null;
	}
	/**
	 * Returns the visible child.
	 */
	public LayoutPart getVisiblePart() {
		if (current == null)
			return inactiveCurrent;
		return current;
	}
	public int indexOf(LayoutPart item) {

		Iterator keys = mapTabToPart.keySet().iterator();
		while (keys.hasNext()) {
			CTabItem2 tab = (CTabItem2) keys.next();
			LayoutPart part = (LayoutPart) mapTabToPart.get(tab);
			if (part.equals(item))
				return tabFolder.indexOf(tab);
		}

		return 0;
	}
	/**
	 * See IVisualContainer#remove
	 */
	public void remove(LayoutPart child) {		
		if (active && !(child instanceof PartPlaceholder)) {

			Iterator keys = mapTabToPart.keySet().iterator();
			while (keys.hasNext()) {
				CTabItem2 key = (CTabItem2) keys.next();
				if (mapTabToPart.get(key).equals(child)) {
					removeTab(key);
					break;
				}
			}
		} else if (invisibleChildren != null) {
			invisibleChildren = arrayRemove(invisibleChildren, child);
		}

		if (active) {
			child.setVisible(false);
			child.setContainer(null);
		}
	}
	private void removeTab(CTabItem2 tab) {

		// remove the tab now
		// Note, that disposing of the tab causes the
		// tab folder to select the next tab and fires
		// a selection event. In this situation, do
		// not assign focus.
		assignFocusOnSelection = false;
		mapTabToPart.remove(tab);
		tab.dispose();
		assignFocusOnSelection = true;

		// Because the container's allow border api
		// is dependent on the number of tabs it has,
		// reset the container so the parts can update.
		if (mapTabToPart.size() == 1) {
			Iterator parts = mapTabToPart.values().iterator();
			((LayoutPart) parts.next()).setContainer(this);
		}
	}
	/**
	 * Reorder the tab representing the specified pane.
	 * If a tab exists under the specified x,y location,
	 * then move the tab before it, otherwise place it
	 * as the last tab.
	 */
	public void reorderTab(ViewPane pane, int x, int y) {
		CTabItem2 sourceTab = getTab(pane);
		if (sourceTab == null)
			return;

		// adjust the y coordinate to fall within the tab area
		Point location = new Point(1, 1);
		if ((tabFolder.getStyle() & SWT.BOTTOM) != 0)
			location.y = tabFolder.getSize().y - 4; // account for 3 pixel border

		// adjust the x coordinate to be within the tab area
		if (x > location.x)
			location.x = x;

		// find the tab under the adjusted location.
		CTabItem2 targetTab = tabFolder.getItem(location);

		// no tab under location so move view's tab to end
		if (targetTab == null) {
			// do nothing if already at the end
			if (tabFolder.indexOf(sourceTab) != tabFolder.getItemCount() - 1)
				reorderTab(pane, sourceTab, -1);

			return;
		}

		// do nothing if over view's own tab
		if (targetTab == sourceTab)
			return;

		// do nothing if already before target tab
		int sourceIndex = tabFolder.indexOf(sourceTab);
		int targetIndex = tabFolder.indexOf(targetTab);
		if (sourceIndex == targetIndex - 1)
			return;

		reorderTab(pane, sourceTab, targetIndex);
	}
	/**
	 * Reorder the tab representing the specified pane.
	 */
	private void reorderTab(ViewPane pane, CTabItem2 sourceTab, int newIndex) {
		
		// remember if the source tab was the visible one
		boolean wasVisible = (tabFolder.getSelection() == sourceTab);

		// create the new tab at the specified index
		CTabItem2 newTab;
		if (newIndex < 0) {
			newTab = new CTabItem2(tabFolder, SWT.NONE);
		} else {
			int sourceIdx = indexOf(pane);
			if (sourceIdx < newIndex) {
				newIndex += 1;
			}
			newTab = new CTabItem2(tabFolder, SWT.NONE, newIndex);
		}

		// map it now before events start coming in...	
		mapTabToPart.put(newTab, pane);

		// dispose of the old tab and remove it
		String sourceLabel = sourceTab.getText();
		Image partImage = sourceTab.getImage();
		newTab.setImage(partImage);

		mapTabToPart.remove(sourceTab);
		assignFocusOnSelection = false;
		sourceTab.dispose();
		assignFocusOnSelection = true;
		
		// update the new tab's title and visibility
		if (tabThemeDescriptor != null) {
			int showInTab = tabThemeDescriptor.getShowInTab();

			if ((partImage != null) && (showInTab != TabThemeDescriptor.TABLOOKSHOWTEXTONLY))
				newTab.setImage(partImage);
			if (showInTab != TabThemeDescriptor.TABLOOKSHOWICONSONLY)
				newTab.setText(sourceLabel);
		} else {
			newTab.setText(sourceLabel);
		}
		
		if (wasVisible) {
			tabFolder.setSelection(newTab);
			setSelection(pane);
			pane.setFocus();
		}
	}
	/**
	 * Reparent a part. Also reparent visible children...
	 */
	public void reparent(Composite newParent) {
		if (!newParent.isReparentable())
			return;

		Control control = getControl();
		if ((control == null) || (control.getParent() == newParent))
			return;

		super.reparent(newParent);

		// reparent also the visible children.
		Iterator enum = mapTabToPart.values().iterator();
		while (enum.hasNext())
			 ((LayoutPart) enum.next()).reparent(newParent);
	}
	/**
	 * See IVisualContainer#replace
	 */
	public void replace(LayoutPart oldChild, LayoutPart newChild) {

		if ((oldChild instanceof PartPlaceholder) && !(newChild instanceof PartPlaceholder)) {
			replaceChild((PartPlaceholder) oldChild, newChild);
			return;
		}

		if (!(oldChild instanceof PartPlaceholder) && (newChild instanceof PartPlaceholder)) {
			replaceChild(oldChild, (PartPlaceholder) newChild);
			return;
		}

	}
	private void replaceChild(LayoutPart oldChild, PartPlaceholder newChild) {

		// remove old child from display
		if (active) {
			Iterator keys = mapTabToPart.keySet().iterator();
			while (keys.hasNext()) {
				CTabItem2 key = (CTabItem2) keys.next();
				LayoutPart part = (LayoutPart) mapTabToPart.get(key);
				if (part == oldChild) {
					boolean partIsActive = (current == oldChild);
					TabInfo info = new TabInfo();
					info.part = newChild;
					info.tabText = key.getText();
					removeTab(key);
					int index = 0;
					if (invisibleChildren != null)
						index = invisibleChildren.length;
					invisibleChildren = arrayAdd(invisibleChildren, info, index);
					oldChild.setVisible(false);
					oldChild.setContainer(null);
					newChild.setContainer(this);
					if (tabFolder.getItemCount() > 0 && !partIsActive) {
						setControlSize();
					}
					break;
				}
			}
		} else if (invisibleChildren != null) {
			for (int i = 0, length = invisibleChildren.length; i < length; i++) {
				if (invisibleChildren[i].part == oldChild) {
					invisibleChildren[i].part = newChild;
				}
			}
		}

	}
	private void replaceChild(PartPlaceholder oldChild, LayoutPart newChild) {
		if (invisibleChildren == null)
			return;

		for (int i = 0, length = invisibleChildren.length; i < length; i++) {
			if (invisibleChildren[i].part == oldChild) {
				if (active) {
					TabInfo info = invisibleChildren[i];
					invisibleChildren = arrayRemove(invisibleChildren, oldChild);
					oldChild.setContainer(null);

					if (newChild instanceof PartPane) {
						WorkbenchPartReference ref =
							(WorkbenchPartReference) ((PartPane) newChild).getPartReference();
						info.tabText = ref.getRegisteredName();
					}
					CTabItem2 oldItem = tabFolder.getSelection();
					CTabItem2 item = createPartTab(newChild, info.tabText, info.partImage, -1);
					if (oldItem == null) 
						oldItem = item;
					int index = tabFolder.indexOf(oldItem);
					
					setSelection(index);
				} else {
					invisibleChildren[i].part = newChild;
					// On restore, all views are initially represented by placeholders and then 
					// they are replaced with the real views.  The following code is used to preserve the active 
					// tab when a prespective is restored from its persisted state.
					if (inactiveCurrent != null && inactiveCurrent == oldChild) {
						current = newChild;
						inactiveCurrent = null;
					}
				}
				break;
			}
		}
	}
	/**
	 * @see IPersistable
	 */
	public IStatus restoreState(IMemento memento) {
		// Read the active tab.
		String activeTabID = memento.getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);
		
		// Read the page elements.
		IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
		if (children != null) {
			// Loop through the page elements.
			for (int i = 0; i < children.length; i++) {
				// Get the info details.
				IMemento childMem = children[i];
				String partID = childMem.getString(IWorkbenchConstants.TAG_CONTENT);
				String tabText = childMem.getString(IWorkbenchConstants.TAG_LABEL);

				IViewDescriptor descriptor = (IViewDescriptor)WorkbenchPlugin.getDefault().
					getViewRegistry().find(partID);
			
				if (descriptor != null) {
					if (descriptor.getId().equals(IIntroConstants.INTRO_VIEW_ID))
						continue; // ignore the intro view
					tabText = descriptor.getLabel();
				}

				// Create the part.
				LayoutPart part = new PartPlaceholder(partID);
				add(tabText, i, part);
				//1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
				part.setContainer(this);
				if (partID.equals(activeTabID)) {
					// Mark this as the active part.
					inactiveCurrent = part;
				}
			}
		}
		
		Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
		setState((expanded == null || expanded.intValue() != STATE_MINIMIZED) ? STATE_RESTORED : STATE_MINIMIZED);
		
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}
	/**
	 * @see IPersistable
	 */
	public IStatus saveState(IMemento memento) {

		// Save the active tab.
		if (current != null)
			memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID, current.getID());

		if (mapTabToPart.size() == 0) {
			// Loop through the invisible children.
			if (invisibleChildren != null) {
				for (int i = 0; i < invisibleChildren.length; i++) {
					// Save the info.
					// Fields in TabInfo ..
					//		private String tabText;
					//		private LayoutPart part;
					TabInfo info = invisibleChildren[i];
					IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
					childMem.putString(IWorkbenchConstants.TAG_LABEL, info.tabText);
					childMem.putString(IWorkbenchConstants.TAG_CONTENT, info.part.getID());
				}
			}
		} else {
			LayoutPart[] children = getChildren();
			CTabItem2 keys[] = new CTabItem2[mapTabToPart.size()];
			mapTabToPart.keySet().toArray(keys);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
					childMem.putString(IWorkbenchConstants.TAG_CONTENT, children[i].getID());
					boolean found = false;
					for (int j = 0; j < keys.length; j++) {
						if (mapTabToPart.get(keys[j]) == children[i]) {
							childMem.putString(IWorkbenchConstants.TAG_LABEL, keys[j].getText());
							found = true;
							break;
						}
					}
					if (!found) {
						for (int j = 0; j < invisibleChildren.length; j++) {
							if (invisibleChildren[j].part == children[i]) {
								childMem.putString(
									IWorkbenchConstants.TAG_LABEL,
									invisibleChildren[j].tabText);
								found = true;
								break;
							}
						}
					}
					if (!found) {
						childMem.putString(IWorkbenchConstants.TAG_LABEL, "LabelNotFound"); //$NON-NLS-1$
					}
				}
			}
		}
		
		memento.putInteger(IWorkbenchConstants.TAG_EXPANDED, (viewState == STATE_MINIMIZED) ? STATE_MINIMIZED : STATE_RESTORED);
		
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}
	/**
	 * Sets the presentation bounds.
	 */
	public void setBounds(Rectangle r) {
		if (tabFolder != null)
			tabFolder.setBounds(r);
		setControlSize();
	}
	/**
	 * Set the size of a page in the folder.
	 */
	private void setControlSize() {
		if (current == null || tabFolder == null)
			return;
		Rectangle bounds;
		// @issue as above, the mere presence of a theme should not change the behaviour
//		if ((mapTabToPart.size() > 1)
//			|| ((tabThemeDescriptor != null) && (mapTabToPart.size() >= 1)))
//			bounds = calculatePageBounds(tabFolder);
//		else
//			bounds = tabFolder.getBounds();
		current.setBounds(calculatePageBounds(tabFolder));
		current.moveAbove(tabFolder);
	}

	public boolean isMinimized() {
		return viewState == STATE_MINIMIZED;
	}
	
	public void setSelection(int index) {
		if (!active)
			return;

		if (mapTabToPart.size() == 0) {
			setSelection(null);
			return;
		}

		// make sure the index is in the right range
		if (index < 0)
			index = 0;
		if (index > mapTabToPart.size() - 1)
			index = mapTabToPart.size() - 1;
		tabFolder.setSelection(index);

		CTabItem2 item = tabFolder.getItem(index);
		LayoutPart part = (LayoutPart) mapTabToPart.get(item);
		setSelection(part);
	}
	private void setSelection(LayoutPart part) {

		if (!active)
			return;
		if (part instanceof PartPlaceholder)
			return;

		// Deactivate old / Activate new.
		if (current != null && current != part) {
			current.setVisible(false);
		}
		current = part;
		if (current != null) {
			setControlSize();
			current.setVisible(!isMinimized());
		}

		// set the title of the detached window to reflect the active tab
		Window window = getWindow();
		if (window instanceof DetachedWindow) {
			if (current == null || !(current instanceof PartPane))
				window.getShell().setText("");//$NON-NLS-1$
			else
				window.getShell().setText(((PartPane) current).getPartReference().getTitle());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#addDropTargets(java.util.Collection)
	 */
	public void addDropTargets(Collection result) {
		addDropTargets(result, this);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDragSource#getType()
	 */
	public int getType() {
		return VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDragSource#isDragAllowed(org.eclipse.swt.graphics.Point)
	 */
	public boolean isDragAllowed(Point point) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWorkbenchDropTarget#targetPartFor(org.eclipse.ui.internal.IWorkbenchDragSource)
	 */
	public LayoutPart targetPartFor(IWorkbenchDragSource dragSource) {
		return this;
	}

	/**
	 * Set the active appearence on the tab folder.
	 * @param active
	 */
	public void setActive(boolean activeState) {
		drawGradient(activeState);
	}
	
	void drawGradient(boolean activeState) {
		Color fgColor;
		Color[] bgColors;
		int[] bgPercents;

		if (activeState){
			fgColor = WorkbenchColors.getActiveViewForeground();
			bgColors = WorkbenchColors.getActiveViewGradient();
			bgPercents = WorkbenchColors.getActiveViewGradientPercents();
		} else {
			fgColor = WorkbenchColors.getActiveViewForeground();
			bgColors = WorkbenchColors.getActiveNoFocusViewGradient();
			bgPercents = WorkbenchColors.getActiveNoFocusViewGradientPercents();
		}		
		
		drawGradient(fgColor, bgColors, bgPercents, activeState);	
	}
	
	protected void drawGradient(Color fgColor, Color[] bgColors, int[] bgPercents, boolean active) {
		tabFolder.setSelectionForeground(fgColor);
		//tabFolder.setBorderVisible(active);
		if (bgPercents == null)
			tabFolder.setSelectionBackground(bgColors[0]);
		else
			tabFolder.setSelectionBackground(bgColors, bgPercents, true);
		tabFolder.update();		
	}
	
	
	
	private void useThemeInfo() {

		//	if (tabThemeDescriptor.getSelectedImageDesc() != null)
		//		tabFolder.setSelectedTabImage(tabThemeDescriptor.getSelectedImageDesc().createImage());
		//	if (tabThemeDescriptor.getUnselectedImageDesc() != null)
		//		tabFolder.setUnselectedTabImage(tabThemeDescriptor.getUnselectedImageDesc().createImage());

		if (tabThemeDescriptor.getTabMarginSize(SWT.DEFAULT) != -1) {
			//		tabFolder.setUseSameMarginAllSides(true);		
			//		tabFolder.setMarginHeight(tabThemeDescriptor.getTabMarginSize(SWT.DEFAULT));
			//		tabFolder.setBorderMarginHeightColor(tabThemeDescriptor.getTabMarginColor(SWT.DEFAULT));
		} else if (tabThemeDescriptor.getTabMarginSize(tabPosition) != -1) {
			//		tabFolder.setMarginHeight(tabThemeDescriptor.getTabMarginSize(tabPosition));
			//		tabFolder.setBorderMarginHeightColor(tabThemeDescriptor.getTabMarginColor(tabPosition));
		}

		if (tabThemeDescriptor.getTabFixedHeight() > 0) {
			tabFolder.setTabHeight(tabThemeDescriptor.getTabFixedHeight());
		}
		if (tabThemeDescriptor.getTabFixedWidth() > 0) {
			//		tabFolder.setTabWidth(tabThemeDescriptor.getTabFixedWidth());
		}
		if (tabThemeDescriptor.getBorderStyle() == SWT.NONE) {
			tabFolder.setBorderVisible(false);
		}

		//	setTabDragInFolder(tabThemeDescriptor.isDragInFolder());

		/* get the font */
		if (themeid != null) {
			Font tabfont =
				WorkbenchThemeManager.getInstance().getTabFont(
					themeid,
					IThemeDescriptor.TAB_TITLE_FONT);
			tabFolder.setFont(tabfont);

			//		tabFolder.setHoverForeground(WorkbenchThemeManager.getInstance().
			//					getTabColor(themeid, IThemeDescriptor.TAB_TITLE_TEXT_COLOR_HOVER));
			tabFolder.setSelectionForeground(
				WorkbenchThemeManager.getInstance().getTabColor(
					themeid,
					IThemeDescriptor.TAB_TITLE_TEXT_COLOR_ACTIVE));
			//		tabFolder.setInactiveForeground(WorkbenchThemeManager.getInstance().
			//					getTabColor(themeid, IThemeDescriptor.TAB_TITLE_TEXT_COLOR_DEACTIVATED));										
		}
		if (tabThemeDescriptor.isShowClose()) {

			//		if (tabThemeDescriptor.getCloseActiveImageDesc() != null)
			//			tabFolder.setCloseActiveImage(tabThemeDescriptor.getCloseActiveImageDesc().createImage());
			//		if (tabThemeDescriptor.getCloseInactiveImageDesc() != null)
			//			tabFolder.setCloseInactiveImage(tabThemeDescriptor.getCloseInactiveImageDesc().createImage());

			// listener to close the view
			tabFolder.addCTabFolderListener(new CTabFolderListener() {
				public void itemClosed(CTabFolderEvent e) {
					LayoutPart item = (LayoutPart) mapTabToPart.get(e.item);
					// Item can be null when tab is just created but not map yet.
					if (item != null) {
						if (item instanceof ViewPane) {
							ViewPane pane = (ViewPane) item;
							pane.doHide();
						} else
							remove(item);
					}
					//			e.doit = false; // otherwise tab is auto disposed on return
				}
			});

			// listener to close the view
			//		tabFolder.addCTabFolderThemeListener(new CTabFolderThemeListener() {
			//			public boolean showClosebar(CTabFolderEvent e) {
			//				LayoutPart item = (LayoutPart)mapTabToPart.get(e.item);
			//				boolean showClosebar = true;
			//				// Item can be null when tab is just created but not map yet.
			//				if (item != null) {
			//					if (item instanceof PartPane) {
			//						WorkbenchPartReference ref = (WorkbenchPartReference)((PartPane)item).getPartReference();
			//						if (ref instanceof IViewReference) {
			//							IViewReference viewref = (IViewReference)ref;
			//							if (viewref.getHideCloseButton())
			//								showClosebar = false;
			//						}
			//					}
			//	
			//				}
			//	//			e.doit = false; // otherwise tab is auto disposed on return
			//				return showClosebar;
			//			}		
			//		});
		}
	}

	/**
	 * Listen for notifications from the view part
	 * that its title has change or it's dirty, and
	 * update the corresponding tab
	 *
	 * @see IPropertyListener
	 */
	public void propertyChanged(Object source, int property) {
		if (property == IWorkbenchPart.PROP_TITLE) {
			if (source instanceof IViewPart) {
				IViewPart part = (IViewPart) source;
				PartPane pane = ((ViewSite) part.getSite()).getPane();
				CTabItem2 sourceTab = getTab(pane);
				String title = part.getTitle();
				Image newImage = part.getTitleImage();

				// @issue need to handle backwards compatibility: tab text is always
				//   registry name -- for now, only update tab if there's a theme set
				if (tabThemeDescriptor != null) {
					// @issue need to take theme settings into account - may not
					//   want to show text or image
					if ((title != null) && (title.length() != 0))
						sourceTab.setText(title);

					if (newImage != sourceTab.getImage())
						sourceTab.setImage(newImage);

					// @issue what about tooltip?
				}
			}
		}
	}

	/**
	 * Returns the theme id.
	 *
	 * @return the theme id.
	 */
	public String getTheme() {
		return themeid;
	}

	/**
	 * Sets the theme id.
	 *
	 * @param theme the theme id to set.
	 */
	public void setTheme(String theme) {
		if ((theme != null) && (theme.length() > 0)) {
			this.themeid = theme;
			tabThemeDescriptor = WorkbenchThemeManager.getInstance().getTabThemeDescriptor(theme);
		}
	}
	
	/**
	 * Replace the image on the tab with the supplied image.
	 * @param part PartPane
	 * @param image Image
	 */
	private void updateImage(final PartPane part, final Image image){
		final CTabItem2 item = getTab(part);
		if(item != null){
			UIJob updateJob = new UIJob("Tab Update"){ //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public IStatus runInUIThread(IProgressMonitor monitor) {
					part.setImage(item,image);
					return Status.OK_STATUS;
				}
			};
			updateJob.setSystem(true);
			updateJob.schedule();
		}
	}
	
	
	
	/**
	 * Indicate busy state in the supplied partPane.
	 * @param partPane PartPane.
	 */
	public void showBusy(PartPane partPane) {
		updateTab(
			partPane,
			JFaceResources.getImage(ProgressManager.BUSY_OVERLAY_KEY));
	}
	
	/**
	 * Restore the part to the default.
	 * @param partPane PartPane
	 */
	public void clearBusy(PartPane partPane) {
		updateTab(partPane,partPane.getPartReference().getTitleImage());
	}
	
	/**
	 * Replace the image on the tab with the supplied image.
	 * @param part PartPane
	 * @param image Image
	 */
	private void updateTab(PartPane part, final Image image){
		final CTabItem2 item = getTab(part);
		if(item != null){
			UIJob updateJob = new UIJob("Tab Update"){ //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public IStatus runInUIThread(IProgressMonitor monitor) {
					item.setImage(image);
					return Status.OK_STATUS;
				}
			};
			updateJob.setSystem(true);
			updateJob.schedule();
		}
			
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#setContainer(org.eclipse.ui.internal.ILayoutContainer)
	 */
	public void setContainer(ILayoutContainer container) {
		
		super.setContainer(container);

		if (tabFolder != null) {			
			showMinMaxButtons(container != null);
			
			if (current != null && viewState == STATE_MAXIMIZED) {
				WorkbenchPage page = ((PartPane) current).getPage();
				if (!page.isZoomed()) {
					setState(STATE_RESTORED);
				}
			}
		}
	}
	/**
	 * @param b
	 */
	private void showMinMaxButtons(boolean b) {
		tabFolder.setMinimizeVisible(b);
		tabFolder.setMaximizeVisible(b);
	}

	private void setState(int newState) {
		if (newState == viewState) {
			return;
		}
		
		int oldState = viewState;
		
		viewState = newState;
		updateControlState();
		
		if (current != null) {
			if (viewState == STATE_MAXIMIZED) {
				((PartPane) current).doZoom();
			} else {
				WorkbenchPage page = ((PartPane) current).getPage();
				if (page.isZoomed()) {
					page.zoomOut();
				}
			
				updateControlBounds();
				
				if (oldState == STATE_MINIMIZED) {
					forceLayout();
				}
			}
		}
	}
	
	private void updateControlBounds() {
		Rectangle bounds = tabFolder.getBounds();
		int minimumHeight = getMinimumHeight();
		
		if (viewState == STATE_MINIMIZED && bounds.height != minimumHeight) {
			tabFolder.setSize(getMinimumWidth(), minimumHeight);	
			
			forceLayout();
		}
	}
	
	/**
	 * Forces the layout to be recomputed for all parts
	 */
	private void forceLayout() {
		PartSashContainer cont = (PartSashContainer) getContainer();
		if (cont != null) {
			cont.getLayoutTree().setBounds(PartTabFolder.this.parent.getClientArea());
		}
	}
	
	/**
	 * Updates this tab folder's widgetry to make it reflect its current minimized/maximized/restored state. 
	 * This should be called when the controls are first created, and every time the state changes.
	 *
	 */
	private void updateControlState() {
		if (tabFolder != null && !tabFolder.isDisposed()) {
			boolean minimized = (viewState == STATE_MINIMIZED);
			boolean maximized = (viewState == STATE_MAXIMIZED);
			
			tabFolder.setMinimized(minimized);
			tabFolder.setMaximized(maximized);
			
			if (current != null) {
				current.setVisible(!minimized);
			}
			
			updateControlBounds();
		}
	}

	public void findSashes(LayoutPart part, ViewPane.Sashes sashes) {
		ILayoutContainer container = getContainer();
		
		if (container != null) {
			container.findSashes(this, sashes);
		}
	}	
}

