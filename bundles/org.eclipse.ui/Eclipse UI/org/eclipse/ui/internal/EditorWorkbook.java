package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import java.util.*;
import java.util.List;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;


/**
 * Represents a tab folder of editors. This layout part
 * container only accepts EditorPane parts.
 */
public class EditorWorkbook extends LayoutPart
	implements ILayoutContainer, IPropertyListener
{
	private static final int INACTIVE = 0;
	private static final int ACTIVE_FOCUS = 1;
	private static final int ACTIVE_NOFOCUS = 2;

	private static int tabLocation = -1; // Initialized in constructor.
	
	private int activeState = INACTIVE;
	private boolean assignFocusOnSelection = true;
	private boolean ignoreTabFocusHide = false;
	private boolean handleTabSelection = true;
	private boolean mouseDownListenerAdded = false;

	private boolean isZoomed = false;
	private Composite parent;
	private CTabFolder tabFolder;
	private EditorArea editorArea;
	private EditorPane visibleEditor;
	private Map mapPartToDragMonitor = new HashMap();

	private Map mapTabToEditor = new HashMap();
	private List editors = new ArrayList();
/**
 * EditorWorkbook constructor comment.
 */
public EditorWorkbook(EditorArea editorArea) {
	super("editor workbook");//$NON-NLS-1$
	this.editorArea = editorArea;
	// Each workbook has a unique ID so
	// relative positioning is unambiguous.
	setID(this.toString());

	// Get tab location preference.
	if (tabLocation == -1)
		tabLocation = getPreferenceStore().getInt(
			IPreferenceConstants.EDITOR_TAB_POSITION);
}
/**
 * See ILayoutContainer::add
 *
 * Note: the workbook currently only accepts
 * editor parts.
 */
public void add(LayoutPart part) {
	if (part instanceof EditorPane) {
		EditorPane editorPart = (EditorPane) part;
		editors.add(editorPart);
		editorPart.setWorkbook(this);
		if (tabFolder != null) {
			createPage(editorPart);
			setVisibleEditor(editorPart);
		}
	}
}
/**
 * See ILayoutContainer::allowBorder
 *
 * There is already a border around the tab
 * folder so no need for one from the parts.
 */
public boolean allowsBorder() {
	return false;
}
public void becomeActiveWorkbook(boolean hasFocus) {
	EditorArea area = getEditorArea();
	if (area != null)
		area.setActiveWorkbook(this, hasFocus);
}
public void createControl(Composite parent) {

	if (tabFolder != null)
		return;

	this.parent = parent;
	tabFolder = new CTabFolder(parent, SWT.BORDER | tabLocation);

	// redirect drop request to the workbook
	tabFolder.setData((IPartDropTarget) this);

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
			setControlSize(visibleEditor);
		}
	});

	// listen for mouse down on tab area to set focus.
	tabFolder.addMouseListener(new MouseAdapter() {
		public void mouseDoubleClick(MouseEvent event) {
			doZoom();
		}

		public void mouseDown(MouseEvent e) {
			if (visibleEditor != null)
				visibleEditor.setFocus();
		}
	});

	// register the interested mouse down listener
	if (!mouseDownListenerAdded && getEditorArea() != null) {
		tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
		mouseDownListenerAdded = true;
	}

	// Enable drop target data
	enableDrop(this);

	// Create tabs.
	Iterator enum = editors.iterator();
	while (enum.hasNext()) {
		EditorPane part = (EditorPane) enum.next();
		createPage(part);
	}

	// Set active tab.
	if (visibleEditor != null)
		setVisibleEditor(visibleEditor);
	else
		if (getItemCount() > 0)
			setVisibleEditor((EditorPane) editors.get(0));
}
/**
 * Create a page and tab for an editor.
 */
private void createPage(EditorPane editorPane) {
	IEditorPart editorPart = editorPane.getEditorPart();
	createTab(editorPane);
	editorPane.createControl(parent);
	editorPane.setContainer(this);
	enableDrop(editorPane);
	editorPart.addPropertyListener(this);

	// When first editor added, also enable workbook for
	// D&D - this avoids dragging the initial empty workbook
	if (mapPartToDragMonitor.size() == 1)
		enableTabDrag(this, null);
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
	IEditorPart editorPart = editorPane.getEditorPart();
	CTabItem tab = new CTabItem(tabFolder, SWT.NONE, index);
	mapTabToEditor.put(tab, editorPane);
	enableTabDrag(editorPane, tab);
	updateEditorTab(editorPart);
	return tab;
}
private void disableTabDrag(LayoutPart part) {
	PartDragDrop partDragDrop = (PartDragDrop)mapPartToDragMonitor.get(part);
	if (partDragDrop != null) {
		partDragDrop.dispose();
		mapPartToDragMonitor.remove(part);
	}
}
/**
 * See LayoutPart#dispose
 */
public void dispose() {
	if (tabFolder == null) 
		return;

	for (int i = 0; i < editors.size(); i++)
		removeListeners((EditorPane)editors.get(i));
	editors.clear();

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
/**
 * Zooms in on the active page in this workbook.
 */
private void doZoom() {
	if (visibleEditor == null)
		return;
	((WorkbenchPage)(getWorkbenchWindow().getActivePage())).toggleZoom(visibleEditor.getPart());
}
/**
 * enableDrop
 */
private void enableDrop(LayoutPart part) {
	Control control = part.getControl();
	if (control != null)
		control.setData((IPartDropTarget)this); // Use workbook as drop target, not part itself.
}
private void enableTabDrag(LayoutPart part, CTabItem tab) {
	CTabPartDragDrop dragSource = new CTabPartDragDrop(part, this.tabFolder, tab);
	mapPartToDragMonitor.put(part, dragSource);
	dragSource.addDropListener(getEditorArea().getPartDropListener());
}
/**
 * Gets the presentation bounds.
 */
public Rectangle getBounds() {
	if (tabFolder == null)
		return new Rectangle(0, 0, 0, 0);
	return tabFolder.getBounds();
}
/**
 * See ILayoutContainer::getChildren
 */
public LayoutPart[] getChildren() {
	int nSize = editors.size();
	LayoutPart [] children = new LayoutPart[nSize];
	editors.toArray(children);
	return children;
}
/**
 * Get the part control.  This method may return null.
 */
public Control getControl() {
	return tabFolder;
}
/**
 * Return the editor area to which this editor
 * workbook belongs to.
 */
public EditorArea getEditorArea() {
	return editorArea;
}
/**
 * Answer the number of children.
 */
public int getItemCount() {
	return editors.size();
}
/**
 * Return the composite used to parent all
 * editors within this workbook.
 */
public Composite getParent() {
	return this.parent;
}
/**
 * Returns the tab for a part.
 */
private CTabItem getTab(IEditorPart editorPart) {
	Iterator tabs = mapTabToEditor.keySet().iterator();
	while (tabs.hasNext()) {
		CTabItem tab = (CTabItem) tabs.next();
		EditorPane pane = (EditorPane) mapTabToEditor.get(tab);
		if (pane != null && pane.getEditorPart() == editorPart)
			return tab;
	}
	
	return null;
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
/**
 * Returns the visible child.
 */
public EditorPane getVisibleEditor() {
	return visibleEditor;
}
/**
 * Returns true if this editor workbook is the
 * active one within the editor area.
 */
public boolean isActiveWorkbook() {
	return getEditorArea().isActiveWorkbook(this);
}
/**
 *	Allow the layout part to determine if they are in
 * an acceptable state to start a drag & drop operation.
 */
public boolean isDragAllowed() {
	if (isZoomed)
		return false;
	else
		return true;
}
/**
 * Returns true if this part is visible.  A part is visible if it has a control.
 */
public boolean isVisible() {
	return (tabFolder != null);
}
/**
 * Listen for notifications from the editor part
 * that its title has change or it's dirty, and
 * update the corresponding tab
 *
 * @see IPropertyListener
 */
public void propertyChanged(Object source, int property) {
	if (property == IEditorPart.PROP_DIRTY || property == IWorkbenchPart.PROP_TITLE) {
		if (source instanceof IEditorPart) {
			updateEditorTab((IEditorPart)source);
		}
	}
}
/**
 * See ILayoutContainer::remove
 *
 * Note: workbook only handles editor parts.
 */
public void remove(LayoutPart child) {
	// Get editor position.
	int tabIndex = editors.indexOf(child);
	if (tabIndex < 0)
		return;

	// Dereference the old editor.  
	// This must be done before "show" to get accurate decorations.
	editors.remove(child);
	removeListeners((EditorPane)child);
	
	// Show new editor
	int maxIndex = editors.size() - 1;
	if (maxIndex >= 0) {
		tabIndex = Math.min(tabIndex, maxIndex);
		setVisibleEditor((EditorPane)editors.get(tabIndex));
	} else {
		setVisibleEditor(null);
	}

	// Dispose old editor.
	if (tabFolder != null) {
		removeTab(getTab(child));
		child.setContainer(null);
	}
}
/**
 * See IVisualContainer#remove
 */
public void removeAll() {
	// turn off tab selection handling so as
	// not to activate another editor when a
	// tab is disposed. See PR 1GBXAWZ
	handleTabSelection = false;
	
	// Show empty space.
	setVisibleEditor(null);

	// Dispose of all tabs.
	if (tabFolder != null) {
		Iterator tabs = mapTabToEditor.keySet().iterator();
		while (tabs.hasNext()) {
			CTabItem tab = (CTabItem) tabs.next();
			if (tab.getDisabledImage() != null)
				tab.getDisabledImage().dispose();
			tab.dispose();
			EditorPane child = (EditorPane)mapTabToEditor.get(tab);
			removeListeners(child);
			child.setContainer(null);
		}
	}

	// Clean up
	mapTabToEditor.clear();
	editors.clear();
	handleTabSelection = true;
}
private void removeListeners(EditorPane editor) {
	if (editor == null)
		return;

	disableTabDrag(editor);

	// When last editor removed, also disable workbook for
	// D&D - this avoids dragging the initial empty workbook
	if (mapPartToDragMonitor.size() == 1)
		disableTabDrag(this);

	IEditorPart editorPart = editor.getEditorPart();
	if (editorPart != null)
		editorPart.removePropertyListener(this);
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
/**
 * Reorder the tab representing the specified pane.
 * If a tab exists under the specified x,y location,
 * then move the tab before it, otherwise place it
 * as the last tab.
 */
public void reorderTab(EditorPane pane, int x, int y,boolean wasActive) {
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
			reorderTab(pane, sourceTab, -1,wasActive);
		
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

	reorderTab(pane, sourceTab, targetIndex,wasActive);
}
/**
 * Reorder the tab representing the specified pane.
 */
private void reorderTab(EditorPane pane, CTabItem sourceTab, int newIndex,boolean wasActive) {
	// Remove old tab.
	disableTabDrag(pane);
	removeTab(sourceTab);

	// Create the new tab at the specified index
	CTabItem newTab;
	if (newIndex < 0)
		newTab = createTab(pane);
	else
		newTab = createTab(pane, newIndex);
	CTabPartDragDrop partDragDrop = (CTabPartDragDrop)mapPartToDragMonitor.get(pane);
	partDragDrop.setTab(newTab);

	// update order of editors.
	editors.remove(pane);
	if (newIndex < 0)
		editors.add(pane);
	else
		editors.add(newIndex, pane);

	// update the new tab's visibility	
	if (wasActive) {
		tabFolder.setSelection(newTab);
		setVisibleEditor(pane);
		pane.setFocus();
	}
}
/**
 * See ILayoutContainer::replace
 *
 * Note: this is not currently supported
 */
public void replace(LayoutPart oldPart, LayoutPart newPart) {
}
/**
 * Sets the presentation bounds.
 */
public void setBounds(Rectangle r) {
	if (tabFolder != null) {
		tabFolder.setBounds(r);
		setControlSize(visibleEditor);
	}
}
/**
 * Sets the parent for this part.
 */
public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
	
	// register the interested mouse down listener
	if (!mouseDownListenerAdded && getEditorArea() != null && tabFolder != null) {
		tabFolder.addListener(SWT.MouseDown, getEditorArea().getMouseDownListener());
		mouseDownListenerAdded = true;
	}
}
/**
 * Set the size of a page in the folder.
 */
private void setControlSize(LayoutPart part) {
	if (part == null || tabFolder == null) 
		return;
	Rectangle bounds = PartTabFolder.calculatePageBounds(tabFolder);
	part.setBounds(bounds);
	part.moveAbove(tabFolder);
}
public void setVisibleEditor(EditorPane comp) {

	if (tabFolder == null) {
		visibleEditor = comp;
		return;
	}

	// Hide old part.
	if (visibleEditor != null && visibleEditor != comp){
		visibleEditor.getControl().setVisible(false);
	}

	// Show new part.
	visibleEditor = comp;
	if (visibleEditor != null) {
		CTabItem key = getTab(visibleEditor);
		if (key != null) {
			int index = tabFolder.indexOf(key);
			tabFolder.setSelection(index);
		}
		setControlSize(visibleEditor);
		visibleEditor.getControl().setVisible(true);
	}

	becomeActiveWorkbook(activeState == ACTIVE_FOCUS);
}
public void tabFocusHide() {
	if (tabFolder == null || ignoreTabFocusHide) 
		return;

	if (isActiveWorkbook()) {
		if (activeState != ACTIVE_NOFOCUS) {
			activeState = ACTIVE_NOFOCUS;
			tabFolder.setSelectionForeground(WorkbenchColors.getSystemColor(SWT.COLOR_BLACK));
			tabFolder.setSelectionBackground(WorkbenchColors.getActiveNoFocusEditorGradient(), WorkbenchColors.getActiveNoFocusEditorGradientPercents());
			tabFolder.update();
		}
	} else {
		if (activeState != INACTIVE) {
			activeState = INACTIVE;
			tabFolder.setSelectionForeground(WorkbenchColors.getSystemColor(SWT.COLOR_BLACK));
			tabFolder.setSelectionBackground(null, null);
			tabFolder.update();
		}
	}
}
public void tabFocusShow(boolean hasFocus) {
	if (tabFolder == null) 
		return;

	if (hasFocus) {
		if (activeState != ACTIVE_FOCUS) {
			activeState = ACTIVE_FOCUS;
			tabFolder.setSelectionForeground(WorkbenchColors.getSystemColor(SWT.COLOR_WHITE));
			tabFolder.setSelectionBackground(WorkbenchColors.getActiveEditorGradient(), WorkbenchColors.getActiveEditorGradientPercents());
			tabFolder.update();
		}
	}
	else {
		if (activeState != ACTIVE_NOFOCUS) {
			activeState = ACTIVE_NOFOCUS;
			tabFolder.setSelectionForeground(WorkbenchColors.getSystemColor(SWT.COLOR_BLACK));
			tabFolder.setSelectionBackground(WorkbenchColors.getActiveNoFocusEditorGradient(), WorkbenchColors.getActiveNoFocusEditorGradientPercents());
			tabFolder.update();
		}
	}
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	if (dragSource instanceof EditorPane || dragSource instanceof EditorWorkbook)
		return this;
	else
		return getEditorArea();
}
/**
 * Update the tab for an editor.  This is typically called
 * by a site when the tab title changes.
 */
public void updateEditorTab(IEditorPart editor) {
	// Get tab.
	CTabItem tab = getTab(editor);
	if(tab == null) return;
	
	// Update title.
	String title = editor.getTitle();
	if (editor.isDirty())
		title = "*" + title;//$NON-NLS-1$
	tab.setText(title);

	// Update the tab image
	Image image = editor.getTitleImage();
	if (image == null) {
		// Normal image.
		tab.setImage(null);
		// Disabled image.
		Image disableImage = tab.getDisabledImage();
		if (disableImage != null) {
			disableImage.dispose();
			tab.setDisabledImage(null);
		}
	} else if (!image.equals(tab.getImage())) {
		// Normal image.
		tab.setImage(image);
		// Disabled image.
		Image disableImage = tab.getDisabledImage();
		if (disableImage != null)
			disableImage.dispose();
		Display display = editor.getEditorSite().getShell().getDisplay();
		disableImage = new Image(display, image, SWT.IMAGE_DISABLE);
		tab.setDisabledImage(disableImage);
	}

	// Tool tip.
	tab.setToolTipText(editor.getTitleToolTip());
	tab.getParent().update();
}
/**
 * Zoom in on the active part.
 */
public void zoomIn() {
	if (isZoomed)
		return;
	isZoomed = true;
	
	// Remove each tab but the active.
	if (tabFolder != null) {
		// Get active editor.
		CTabItem activeTab = tabFolder.getSelection();
		if (activeTab == null)
			return;

		// Hide all inactive tabs.
		Object [] tabArray = mapTabToEditor.keySet().toArray();
		for (int nX = 0; nX < tabArray.length; nX ++) {
			CTabItem tab = (CTabItem)tabArray[nX];
			if (tab != activeTab)
				removeTab(tab);
		}
	}
}
/**
 * Zoom out and show all editors.
 */
public void zoomOut() {
	if (!isZoomed)
		return;
	isZoomed = false;
	
	// Create a tab for each inactive editor.
	if (tabFolder != null) {
		int count = 0;
		Iterator enum = editors.iterator();
		while (enum.hasNext()) {
			EditorPane part = (EditorPane) enum.next();
			if (part != visibleEditor)
				createTab(part, count);
			++ count;
		}
	}
}
}
