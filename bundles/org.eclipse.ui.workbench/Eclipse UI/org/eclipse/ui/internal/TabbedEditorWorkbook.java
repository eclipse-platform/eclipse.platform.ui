/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderAdapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IEditorReference;

public class TabbedEditorWorkbook extends EditorWorkbook {

	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
	private int tabLocation = -1; // Initialized in constructor.
	private CTabFolder tabFolder;
	private Map mapTabToEditor = new HashMap();
	private ToolBar pullDownBar;
	private ToolItem pullDownButton;
	private EditorList editorList;
	private ViewForm listComposite;
	private boolean assignFocusOnSelection = true;
	private boolean ignoreTabFocusHide = false;
	private boolean handleTabSelection = true;
	private boolean mouseDownListenerAdded = false;
	private boolean usePulldown = false;


public TabbedEditorWorkbook(EditorArea editorArea) {
	super(editorArea);
	
	// Get tab location preference.
	if (tabLocation == -1)
		tabLocation =
			preferenceStore.getInt(
				IPreferenceConstants.EDITOR_TAB_POSITION);
}

protected Object createItem(EditorPane editorPane) {
	return createTab(editorPane);
}

/**
 * Create a new tab for an item.
 */
private CTabItem createTab(EditorPane editorPane) {
	return createTab(editorPane, tabFolder.getItemCount());
}

/**
 * Create a new tab for an item at a particular index.
 */
private CTabItem createTab(EditorPane editorPane, int index) {
	CTabItem tab = new CTabItem(tabFolder, SWT.NONE, index);
	mapTabToEditor.put(tab, editorPane);
	enableDrag(editorPane);
	updateEditorTab((IEditorReference) editorPane.getPartReference());
	if (tabFolder.getItemCount() == 1) {
		if (tabFolder.getTopRight() != null) {
			pullDownBar.setVisible(true);
		}
	}
	return tab;
}

protected void setControlSize() {
	EditorPane visibleEditor = getVisibleEditor();
	if (visibleEditor == null || getControl() == null) 
		return;
	Rectangle bounds = PartTabFolder.calculatePageBounds(tabFolder);
	visibleEditor.setBounds(bounds);
	visibleEditor.moveAbove(tabFolder);
}

protected void createPresentation(Composite parent) {
	usePulldown = preferenceStore.getBoolean(IPreferenceConstants.EDITORLIST_PULLDOWN_ACTIVE);
	
	tabFolder = new CTabFolder(parent, SWT.BORDER | tabLocation);

	// prevent close button and scroll buttons from taking focus
	tabFolder.setTabList(new Control[0]);
	
	// redirect drop request to the workbook
	tabFolder.setData(this);

	// listener to close the editor
	tabFolder.addCTabFolderListener(new CTabFolderAdapter() {
		public void itemClosed(CTabFolderEvent e) {
			e.doit = false; // otherwise tab is auto disposed on return
			EditorPane pane = (EditorPane) mapTabToEditor.get(e.item);
			pane.doHide();
		}
	});

	// listener to switch between visible tabItems
	tabFolder.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (handleTabSelection) {
				EditorPane pane = (EditorPane) mapTabToEditor.get(e.item);
				// Pane can be null when tab is just created but not map yet.
				if (pane != null) {
					setVisibleEditor(pane);
					if (assignFocusOnSelection) {
						// If we get a tab focus hide request, it's from
						// the previous editor in this workbook which had focus.
						// Therefore ignore it to avoid paint flicker
						ignoreTabFocusHide = true;
						pane.setFocus();
						ignoreTabFocusHide = false;
					}
				}
			}
		}
	});

	// listener to resize visible components
	tabFolder.addListener(SWT.Resize, new Listener() {
		public void handleEvent(Event e) {
			setControlSize();
		}
	});

	// listen for mouse down on tab area to set focus.
	tabFolder.addMouseListener(new MouseAdapter() {
		public void mouseDoubleClick(MouseEvent event) {
			Rectangle clientArea = tabFolder.getClientArea();
			if((tabFolder.getStyle() & SWT.TOP) != 0) {
				if(event.y > clientArea.y)
					return;
			} else {
				if(event.y < clientArea.y + clientArea.height)
					return;
			}			
			doZoom();
		}

		public void mouseDown(MouseEvent e) {
			EditorPane visibleEditor = getVisibleEditor();
			if (visibleEditor != null) {
				// switch to the editor
				CTabItem item = getTab(visibleEditor);
				visibleEditor.setFocus();
				Rectangle bounds = item.getBounds();
				if (bounds.contains(e.x, e.y)) {
					if ((e.button == 1) && overImage(item, e.x))
						visibleEditor.showPaneMenu();
				}
			}
		}
	});

	// Listen for popup menu mouse event
	tabFolder.addListener(SWT.MenuDetect, new Listener() {
		public void handleEvent(Event event) {
			EditorPane visibleEditor = getVisibleEditor();
			if (event.type == SWT.MenuDetect && visibleEditor != null) {
				CTabItem item = getTab(visibleEditor);
				visibleEditor.setFocus();
				Rectangle bounds = item.getBounds();
				Point pt = tabFolder.toControl(event.x, event.y);
				if (bounds.contains(pt.x, pt.y)) {
					visibleEditor.showPaneMenu(tabFolder, new Point(event.x, event.y));
				}
			}
		}
	});
	
	// register the interested mouse down listener
	if (!mouseDownListenerAdded && getEditorArea() != null) {
		tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
		mouseDownListenerAdded = true;
	}

	// Create the pulldown menu on the CTabFolder
	editorList = new EditorList(getEditorArea().getWorkbenchWindow(), this);
	pullDownBar = new ToolBar(tabFolder, SWT.FLAT);
	pullDownButton = new ToolItem(pullDownBar, SWT.PUSH);
//	Image image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
	Image hotImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
	pullDownButton.setDisabledImage(hotImage);
	pullDownButton.setImage(hotImage);
//	pullDownButton.setHotImage(hotImage);
	pullDownButton.setToolTipText(WorkbenchMessages.getString("EditorList.button.toolTip")); //$NON-NLS-1$
	
	pullDownButton.addSelectionListener(new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			openEditorList(); 

		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	});

	// Present the editorList pull-down if requested
	if (usePulldown) {
		tabFolder.setTopRight(pullDownBar);
		pullDownBar.setVisible(true);
	} else {
		pullDownBar.setVisible(false);
		tabFolder.setTopRight(null);
	}
	
	// Set the tab width
	tabFolder.MIN_TAB_WIDTH = preferenceStore.getInt(IPreferenceConstants.EDITOR_TAB_WIDTH);			
}

/**
 * Returns the tab for a part.
 */
private CTabItem getTab(LayoutPart child) {
	Iterator tabs = mapTabToEditor.keySet().iterator();
	while (tabs.hasNext()) {
		CTabItem tab = (CTabItem) tabs.next();
		if (mapTabToEditor.get(tab) == child)
			return tab;
	}
	
	return null;
}

public boolean isDragAllowed(EditorPane pane, Point p) {
	CTabItem tab = getTab(pane);
	return tab != null && overImage(tab, p.x);
}

/**
 * Returns true if <code>x</code> is over the label image.
 */
private boolean overImage(CTabItem item, int x) {
	if (item.getImage() == null) {
		return false;
	} else {
		Rectangle imageBounds = item.getImage().getBounds();
		return x < (item.getBounds().x + imageBounds.x + imageBounds.width);
	}
}		

private void closeEditorList() {
	editorList.destroyControl();
}

public void openEditorList() {
	// don't think this check is necessary, need to verify
	if (listComposite != null) {
		return;
	}
	Shell parent = getEditorArea().getWorkbenchWindow().getShell();
	
	listComposite = new ViewForm(parent, SWT.BORDER);
	listComposite.setVisible(false);
	listComposite.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			listComposite = null;
		}
	});
	parent.addControlListener(new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			if (listComposite != null) {
				closeEditorList();
			}
		}
	});

	Control editorListControl = editorList.createControl(listComposite);
	editorListControl.setVisible(false);
	Table editorsTable = ((Table)editorListControl);
	TableItem[] items = editorsTable.getItems();
	if (items.length == 0) {
		listComposite.dispose();
		return;
	}

	listComposite.setContent(editorListControl);
	listComposite.pack();

	setEditorListBounds(parent);

	listComposite.setVisible(true);
	listComposite.moveAbove(null);
	editorListControl.setVisible(true);
	editorListControl.setFocus();
	editorsTable.showSelection();

	editorListControl.addListener(SWT.Deactivate, new Listener() {
		public void handleEvent(Event event) {
			if (listComposite != null) {
				closeEditorList();
			}			
		}
	});
}

private void setEditorListBounds(Shell parent) {
	final int MAX_ITEMS = 20;
	
	Rectangle r = listComposite.getBounds();
	
	int width = r.width;
	int height = Math.min(r.height, MAX_ITEMS * ((Table)editorList.getControl()).getItemHeight());
	Rectangle bounds = tabFolder.getClientArea();
	Point point = new Point(bounds.x + bounds.width - width, bounds.y);
	if (tabLocation == SWT.BOTTOM) {
		point.y = bounds.y + bounds.height - height - 1;
	}
	point = tabFolder.toDisplay(point);
	point = parent.toControl(point);
	listComposite.setBounds(listComposite.computeTrim(point.x, point.y, width, height));
}

public void resizeEditorList() {
	Shell parent = getEditorArea().getWorkbenchWindow().getShell();
	listComposite.pack();
	setEditorListBounds(parent);
}


public void showPaneMenu() {
	EditorPane visibleEditor = getVisibleEditor();
	if (visibleEditor != null) {
		CTabItem item = getTab(visibleEditor);
		Rectangle bounds = item.getBounds();
		visibleEditor.showPaneMenu(tabFolder,tabFolder.toDisplay(new Point(bounds.x, bounds.height)));
	}
}

protected void checkEnableDrag() {
	// When first editor added, also enable workbook for
	// D&D - this avoids dragging the initial empty workbook
	if (tabFolder != null && tabFolder.getItemCount() == 1)
		enableDrag(this);
}

protected void disposePresentation() {
	// dispose of disabled images
	for(int i = 0; i < tabFolder.getItemCount(); i++) {
		CTabItem tab = tabFolder.getItem(i);
		if (tab.getDisabledImage() != null)
			tab.getDisabledImage().dispose();
	}

	tabFolder.dispose();
	tabFolder = null;
	mouseDownListenerAdded = false;

	mapTabToEditor.clear();
}

protected void drawGradient(Color fgColor, Color[] bgColors, int[] bgPercents) {
	tabFolder.setSelectionForeground(fgColor);
	tabFolder.setSelectionBackground(bgColors, bgPercents);
	tabFolder.update();
}

// getMinimumHeight() added by cagatayk@acm.org 
/**
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	if (tabFolder != null && !tabFolder.isDisposed() && getItemCount() > 0)
		/* Subtract 1 for divider line, bottom border is enough
		 * for editor tabs. 
		 */
		return tabFolder.computeTrim(0, 0, 0, 0).height - 1;
 	else
 		return super.getMinimumHeight();
}

public Control getControl() {
	return tabFolder;
}

public Control[] getTabList() {
	if (tabFolder == null) {
		return new Control[0];
	}
	EditorPane visibleEditor = getVisibleEditor();
	if (visibleEditor == null) {
		return new Control[] { tabFolder };
	}
	if ((tabFolder.getStyle() & SWT.TOP) != 0) {
		return new Control[] { tabFolder, visibleEditor.getControl() };
	}
	else {
		return new Control[] { visibleEditor.getControl(), tabFolder };
	}
}

public void showVisibleEditor() {
	if (tabFolder != null) {
		tabFolder.showSelection();
	}
}

protected void disposeItem(EditorPane editorPane) {
	removeTab(getTab(editorPane));
	if (tabFolder.getItemCount() == 0) {
		pullDownBar.setVisible(false);			
	}
}

/**
 * Remove the tab item from the tab folder
 */
private void removeTab(CTabItem tab) {
	if (tabFolder != null) {
		if (tab != null) {
			mapTabToEditor.remove(tab);
			if (tab.getDisabledImage() != null)
				tab.getDisabledImage().dispose();
			// Note, that disposing of the tab causes the
			// tab folder to select another tab and fires
			// a selection event. In this situation, do
			// not assign focus.
			assignFocusOnSelection = false;
			tab.dispose();
			assignFocusOnSelection = true;
		}
	}
}

public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
	// register the interested mouse down listener
	if (!mouseDownListenerAdded && getEditorArea() != null && tabFolder != null) {
		tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
		mouseDownListenerAdded = true;
	}
}

protected void setVisibleItem(EditorPane editorPane) {
	CTabItem key = getTab(editorPane);
	if (key != null) {
		int index = tabFolder.indexOf(key);
		tabFolder.setSelection(index);
	}
}

public void tabFocusHide() {
	if (!ignoreTabFocusHide) {
		super.tabFocusHide();
	}
}

protected void updateItem(EditorPane editorPane) {
	// Get tab.
	CTabItem tab = getTab(editorPane);
	if(tab == null) return;
	
	IEditorReference ref = editorPane.getEditorReference();
	
	// Update title.
	String title = ref.getTitle();
	if (ref.isDirty())
		title = "*" + title; //$NON-NLS-1$
	tab.setText(title);

	boolean useColorIcons = ActionContributionItem.getUseColorIconsInToolbars();

	Image image = ref.getTitleImage();	
	// Update the tab image
	if (image == null || image.isDisposed()) {
		// Normal image.
		tab.setImage(null);
		// Disabled image.
		if (!useColorIcons) {
			Image disableImage = tab.getDisabledImage();
			if (disableImage != null) {
				disableImage.dispose();
				tab.setDisabledImage(null);
			}
		}
	} else if (!image.equals(tab.getImage())) {
		// Normal image.
		tab.setImage(image);
		// Disabled image.
		if (!useColorIcons) {
			Image disableImage = tab.getDisabledImage();
			if (disableImage != null)
				disableImage.dispose();
			disableImage = new Image(tab.getDisplay(), image, SWT.IMAGE_DISABLE);
			tab.setDisabledImage(disableImage);
		}
	}

	// Tool tip.
	String toolTip = ref.getTitleToolTip();
	tab.setToolTipText(toolTip);
	tab.getParent().update();
}

protected void disposeAllItems() {
	// turn off tab selection handling so as
	// not to activate another editor when a
	// tab is disposed. See PR 1GBXAWZ
	handleTabSelection = false;

	Iterator tabs = mapTabToEditor.keySet().iterator();
	while (tabs.hasNext()) {
		CTabItem tab = (CTabItem) tabs.next();
		if (tab.getDisabledImage() != null)
			tab.getDisabledImage().dispose();
		tab.dispose();
	}
	// disable the pulldown menu
	pullDownBar.setVisible(false);

	// Clean up
	mapTabToEditor.clear();
	handleTabSelection = true;
}

public void reorderTab(EditorPane pane, int x, int y) {
	CTabItem sourceTab = getTab(pane);
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
	CTabItem targetTab = tabFolder.getItem(location);

	// no tab under location so move editor's tab to end
	if (targetTab == null) {
		// do nothing if already at the end
		if (tabFolder.indexOf(sourceTab) != tabFolder.getItemCount() - 1)
			reorderTab(pane, sourceTab, -1);
		
		return;
	}

	// do nothing if over editor's own tab
	if (targetTab == sourceTab)
		return;

	// do nothing if already before target tab
	int sourceIndex = tabFolder.indexOf(sourceTab);
	int targetIndex = tabFolder.indexOf(targetTab);
	
	if (sourceIndex == targetIndex - 1)
		return;
		
	//Source is going to be dispose so the target index will change.
	if (sourceIndex < targetIndex)
		targetIndex--;
		
	reorderTab(pane, sourceTab, targetIndex);
}
/**
 * Move the specified editor to the a new position. 
 * Move to the end if <code>newIndex</code> is less then
 * zero.
 */
public void reorderTab(EditorPane pane, int newIndex) {
	reorderTab(pane, getTab(pane), newIndex);
}

/**
 * Reorder the tab representing the specified pane.
 */
private void reorderTab(EditorPane pane, CTabItem sourceTab, int newIndex) {
	int oldIndex = tabFolder.indexOf(sourceTab);
	if(newIndex < 0)
		if(oldIndex == tabFolder.getItemCount() - 1)
			return;
	else if(oldIndex == newIndex)
		return;
	
	// remember if the source tab was the visible one
	boolean wasVisible = (tabFolder.getSelection() == sourceTab);

	// Remove old tab.
	disableDrag(pane);
	removeTab(sourceTab);

	// Create the new tab at the specified index
	CTabItem newTab;
	if (newIndex < 0)
		newTab = createTab(pane);
	else
		newTab = createTab(pane, newIndex);
	CTabPartDragDrop partDragDrop = (CTabPartDragDrop) getDragSource(pane);
	partDragDrop.setTab(newTab);

	// update order of editors.
	
	List editors = getEditorList();
	editors.remove(pane);
	if (newIndex < 0)
		editors.add(pane);
	else
		editors.add(newIndex, pane);

	// update the new tab's visibility but do
	// not set focus...caller's responsibility.
	// Note, if the pane already had focus, it
	// will still have it after the tab order change.
	if (wasVisible) {
		tabFolder.setSelection(newTab);
		setVisibleEditor(pane);
	}
}

/* (non-Javadoc)
 * @see org.eclipse.ui.internal.EditorWorkbook#createDragSource(org.eclipse.ui.internal.EditorPane)
 */
protected PartDragDrop createDragSource(LayoutPart part) {
	if (part instanceof EditorPane) {
		CTabItem tab = getTab(part);
		if (tab != null) {
			return new CTabPartDragDrop(part, this.tabFolder, tab);
		}
	}
	else if (part instanceof EditorWorkbook) {
		return new CTabPartDragDrop(part, this.tabFolder, null);
	}
	return null;
}

}
