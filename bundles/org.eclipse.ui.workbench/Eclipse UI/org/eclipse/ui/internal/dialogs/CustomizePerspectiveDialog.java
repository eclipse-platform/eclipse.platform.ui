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
package org.eclipse.ui.internal.dialogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.IndentedTableViewer.IIndentedTableLabelProvider;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

// @issue add class doc
// @issue need to break this not to show menu specific page
public class CustomizePerspectiveDialog extends Dialog {
	private Perspective perspective;
	private WorkbenchWindow window;
	private TabFolder tabFolder;
	private CheckboxTableViewer actionSetsViewer;
	private IndentedTableViewer actionSetMenuViewer;	
	private IndentedTableViewer actionSetToolbarViewer;	
	private Combo menusCombo;
	private CheckboxTreeViewer menuCategoriesViewer;
	private CheckboxTableViewer menuItemsViewer;
	private CustomizeActionBars customizeWorkbenchActionBars;

	private static int lastSelectedTab = -1;
	private static int lastSelectedMenuIndex = 0;
	private static String lastSelectedActionSetId = null;
	private static int cursorSize = 15;
	
	private final static int TABLES_WIDTH = 600;
	private final static int TABLE_HEIGHT = 300;

	private final String shortcutMenuColumnHeaders[] = {
		WorkbenchMessages.getString("ActionSetSelection.menuColumnHeader"),//$NON-NLS-1$
		WorkbenchMessages.getString("ActionSetSelection.descriptionColumnHeader")};//$NON-NLS-1$
												
	private int[] shortcutMenuColumnWidths = {125, 300};

	private ActionSetDescriptor selectedActionSet = null;
	private ShortcutMenu selectedMenuCategory = null;
		
	private ImageDescriptor menuImageDescriptor = null;
	private ImageDescriptor toolbarImageDescriptor = null;
	
	private ShortcutMenu rootMenu; 
	private ArrayList actionSets = new ArrayList();
	private Hashtable actionSetStructures = new Hashtable();

	class ActionSetDisplayItem extends Object {
		/**
		 * Tree representation for action set menu and toolbar items.  
		 */
		private ArrayList children = new ArrayList();
		private ActionSetDisplayItem parent = null;
		private String id = null;
		private String text = ""; //$NON-NLS-1$
		private String description = "";  //$NON-NLS-1$
		private ImageDescriptor imageDescriptor;
		private int type = MENUITEM;
		private final static int MENUITEM = 0;
		private final static int TOOLITEM = 1;
		
		private ActionSetDisplayItem() {
		}
		private ActionSetDisplayItem(String id) {
			this(null, id, "", MENUITEM); //$NON-NLS-1$
		}
		private ActionSetDisplayItem(ActionSetDisplayItem parent, String id, String text, int type) {
			if (parent != null) {
				parent.children.add(this);
				this.parent = parent; 
			}
			this.id = id;
			this.type = type;
			this.text = removeShortcut(text);
			this.text = DialogUtil.removeAccel(this.text);
		}
		private ActionSetDisplayItem find(String id) {
			for (int i=0; i<children.size(); i++) {
				ActionSetDisplayItem child = (ActionSetDisplayItem)children.get(i);
				if (id.equals(child.id)) return child;
			}
			return null;
		}
		public void fillMenusFor(String actionSetId, IContributionItem item) {
			if (item instanceof ContributionManager) {
				ContributionManager mgr = (ContributionManager)item;
				IContributionItem[] items = mgr.getItems();
				for (int i=0; i<items.length; i++) {
					IContributionItem mgrItem = items[i];
					if (mgrItem instanceof ActionSetContributionItem) {
						ActionSetContributionItem actionSetItem = (ActionSetContributionItem)mgrItem;
						if (actionSetItem.getActionSetId().equals(actionSetId)) {
							IContributionItem innerItem = actionSetItem.getInnerItem();
							if (innerItem instanceof MenuManager) {
								MenuManager inner =(MenuManager)actionSetItem.getInnerItem();
								ActionSetDisplayItem node = new ActionSetDisplayItem(this, inner.getId(), inner.getMenuText(), MENUITEM);
								node.fillMenusFor(actionSetId, inner);
							} else if (innerItem instanceof ActionSetMenuManager) {
								ActionSetMenuManager inner =(ActionSetMenuManager)actionSetItem.getInnerItem();
								MenuManager parentMgr = (MenuManager)inner.getParent();
								ActionSetDisplayItem node = new ActionSetDisplayItem(this, inner.getId(), parentMgr.getMenuText(), MENUITEM);
								node.fillMenusFor(actionSetId, parentMgr);							
							} else if (innerItem instanceof PluginActionContributionItem) {
								PluginActionContributionItem inner =(PluginActionContributionItem)actionSetItem.getInnerItem();
								ActionSetDisplayItem node = new ActionSetDisplayItem(this, inner.getId(), inner.getAction().getText(), MENUITEM);
								IAction action = inner.getAction();
								if (action != null) {
									node.imageDescriptor = action.getImageDescriptor();
									node.description = action.getDescription();
								}
							}	
						}
					} else if (mgrItem instanceof MenuManager) {
						MenuManager menuMgr = (MenuManager)mgrItem;				
						boolean found = containsActionSet(menuMgr, actionSetId);
						if (found) {
							ActionSetDisplayItem node = new ActionSetDisplayItem(this, menuMgr.getId(), menuMgr.getMenuText(), MENUITEM);
							node.fillMenusFor(actionSetId, menuMgr);
						}
					}
				}
			}
		}
		public void fillToolsFor(String actionSetId, CoolBarManager mgr) {
			IContributionItem[] items = mgr.getItems();
			for (int i=0; i<items.length; i++) {
				CoolBarContributionItem cbItem = (CoolBarContributionItem)items[i];
				IContributionItem[] subItems = cbItem.getToolBarManager().getItems();
				for (int j=0; j<subItems.length; j++) {
					IContributionItem subItem = subItems[j];
					if (subItem instanceof PluginActionCoolBarContributionItem) {
						PluginActionCoolBarContributionItem actionItem = (PluginActionCoolBarContributionItem)subItem;
						if (actionItem.getActionSetId().equals(actionSetId)) {
							String toolbarId = cbItem.getId();
							ActionSetDisplayItem toolbar = find(toolbarId); 
							if (toolbar == null) {
								String toolbarText = window.getToolbarLabel(toolbarId);
								toolbar = new ActionSetDisplayItem(this, toolbarId, toolbarText, TOOLITEM);
							}
							IAction action = actionItem.getAction();
							String toolItemText = action.getToolTipText();
							if (toolItemText == null) toolItemText = action.getText();
							ActionSetDisplayItem toolItem = new ActionSetDisplayItem(toolbar, action.getId(), toolItemText, TOOLITEM);
							toolItem.imageDescriptor = action.getImageDescriptor();
							toolItem.description = action.getDescription();
						}
					}
				}
			}
		}
		private int getDepth() {
			if (parent == null) return 0;
			else return parent.getDepth() + 1;
		}
		private String getDisplayText() {
			if (type == MENUITEM) {
				if (children.size() > 0) {
					if (parent.id.equals("Root")) { //$NON-NLS-1$
						return WorkbenchMessages.format("ActionSetSelection.menubarLocation", new Object[] {text}); //$NON-NLS-1$
					} 
				}
				return text;
			} else {
				if (children.size() > 0) return WorkbenchMessages.format("ActionSetSelection.toolbarLocation", new Object[] {text}); //$NON-NLS-1$
				else return text;
			}
		}
		private ArrayList getElements() {
			ArrayList elements = new ArrayList();
			for (int i=0; i<children.size(); i++) {
				ActionSetDisplayItem child = (ActionSetDisplayItem)children.get(i);
				elements.add(child);
				elements.addAll(child.getElements());
			}
			return elements;
		}
	}
	public class CustomizeActionBars extends WWinActionBars {
		/**
		 * Fake action bars to use to build the menus and toolbar contributions for the
		 * workbench.  We cannot use the actual workbench action bars, since doing so would
		 * make the action set items visible.  
		 */
		MenuManager menuManager;
		CoolBarManager coolBarManager;
		
		public CustomizeActionBars() {
			super(null);
		}
		public CustomizeActionBars(MenuManager menuManager, CoolBarManager coolBarManager) {
			super(null);
			this.menuManager = menuManager;
			this.coolBarManager = coolBarManager;
		}
		public void clearGlobalActionHandlers() {
		}
		public CoolBarManager getCoolBarManager() {
			return coolBarManager;
		}
		public IAction getGlobalActionHandler(String actionID) {
			return null;
		}
		public IMenuManager getMenuManager() {
			return menuManager;
		}
		public IStatusLineManager getStatusLineManager() {
			return null;
		}
		public IToolBarManager getToolBarManager() {
			return coolBarManager;
		}
		public void setGlobalActionHandler(String actionID, IAction handler) {
		}
		public void updateActionBars() {
		}
	}
	class ShortcutMenuItemContentProvider implements IStructuredContentProvider {
	
		private ShortcutMenuItemContentProvider() {
		}
		public Object[] getElements(Object input) {
			if (input instanceof ShortcutMenu) {
				return ((ShortcutMenu)input).getItems().toArray();
			}
			return new Object[0];
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
	}
	class ShortcutMenuItemLabelProvider  extends LabelProvider implements ITableLabelProvider {
		private Map imageTable = new Hashtable();
		private final static int COLUMN_ID = 0;
		private final static int COLUMN_DESCRIPTION = 1;
	
		private ShortcutMenuItemLabelProvider() {
		}
		public final void dispose() {
			for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
				((Image) i.next()).dispose();
			}
			imageTable = null;
		}
		public Image getColumnImage(Object element, int index) {
				if (index != COLUMN_ID) return null;
				ImageDescriptor descriptor = null;
				if (element instanceof IPerspectiveDescriptor) {
					descriptor = ((IPerspectiveDescriptor)element).getImageDescriptor();
				} else if (element instanceof IViewDescriptor) {
					descriptor = ((IViewDescriptor)element).getImageDescriptor();
				} else if (element instanceof WorkbenchWizardElement) {
					descriptor = ((WorkbenchWizardElement)element).getImageDescriptor();
				}
				if (descriptor == null)
					return null;
				//obtain the cached image corresponding to the descriptor
				if (imageTable == null) {
					imageTable = new Hashtable(40);
				}
				Image image = (Image) imageTable.get(descriptor);
				if (image == null) {
					image = descriptor.createImage();
					imageTable.put(descriptor, image);
				}
				return image;
		}
		public String getColumnText(Object element, int columnIndex) {
			String text = null;
			switch (columnIndex) {
				case COLUMN_ID :
					text = getText(element);
					break;
				case COLUMN_DESCRIPTION :
					if (element instanceof IPerspectiveDescriptor) {
						text = ((IPerspectiveDescriptor)element).getDescription();
					} else if (element instanceof IViewDescriptor) {
						text = ((IViewDescriptor)element).getDescription();
					} else if (element instanceof WorkbenchWizardElement) {
						text = ((WorkbenchWizardElement)element).getDescription();
					}
					break;
				}
			if (text == null) text = ""; //$NON-NLS-1$
			return text; 
		}
		public String getText(Object element) {
			String text = null;
			if (element instanceof IPerspectiveDescriptor) {
				text =  ((IPerspectiveDescriptor)element).getLabel();
			} else if (element instanceof IViewDescriptor) {
				text =  ((IViewDescriptor)element).getLabel();
			} else if (element instanceof WorkbenchWizardElement) {
				text = ((WorkbenchWizardElement)element).getLabel(element);
			}
			if (text == null) text = ""; //$NON-NLS-1$
			return text; 
		}
	}
	class ShortcutMenu extends Object  {
		/**
		 * Tree representation for the shortcut items.  
		 */
		private final static String ID_VIEW = "org.eclipse.ui.views"; //$NON-NLS-1$
		private final static String ID_WIZARD = "org.eclipse.ui.wizards"; //$NON-NLS-1$
		private final static String ID_PERSP = "org.eclipse.ui.perspectives"; //$NON-NLS-1$
		private String id;
		private String label;
		private ArrayList items = new ArrayList();
		private ArrayList checkedItems = new ArrayList();
		private ArrayList children = new ArrayList();
		private ShortcutMenu parent = null;

		private ShortcutMenu(ShortcutMenu parent, String id, String label) {
			super();
			this.id = id;
			this.parent = parent;
			this.label = removeShortcut(label);
			this.label = DialogUtil.removeAccel(this.label);
			if (parent != null) parent.children.add(this);
		}
		private void addItem(Object item) {
			items.add(item);
		}
		private void addCheckedItem(Object item) {
			checkedItems.add(item);
		}
		public String toString() {
			return label;
		}
		private ArrayList getCheckedItems() {
			return checkedItems;
		}
		private ArrayList getCheckedItemIds() {
			ArrayList ids = new ArrayList();
			if (getMenuId() == ID_PERSP) {
				for (int i=0; i < checkedItems.size(); i++) {
					IPerspectiveDescriptor item = (IPerspectiveDescriptor)checkedItems.get(i);
					ids.add(item.getId());
				}	
			} else if (getMenuId() == ID_VIEW) {
				for (int i=0; i < checkedItems.size(); i++) {
					IViewDescriptor item = (IViewDescriptor)checkedItems.get(i);
					ids.add(item.getId());
				}	
			} else if (getMenuId() == ID_WIZARD) {
				for (int i=0; i < checkedItems.size(); i++) {
					WorkbenchWizardElement item = (WorkbenchWizardElement)checkedItems.get(i);
					ids.add(item.getID());
				}	
			}
			for (int i=0; i < children.size(); i++) {
				ShortcutMenu menu = (ShortcutMenu)children.get(i);
				ids.addAll(menu.getCheckedItemIds());
			}
			return ids;
		}
		private ArrayList getChildren() {
			return children;
		}
		private ArrayList getItems() {
			return items;
		}
		private String getMenuId() {
			if (parent == rootMenu) return id;
			else return parent.getMenuId();
		}
		private ArrayList getSubtreeItems() {
			ArrayList subtreeItems = new ArrayList();
			subtreeItems.add(this);
			for (int i=0; i<children.size(); i++) { 
				ShortcutMenu child = (ShortcutMenu)children.get(i);
				subtreeItems.addAll(child.getSubtreeItems());
			}
			return subtreeItems;
		}
		private Object getItem(String menuItemId) {
			for (int i=0; i<items.size(); i++) {
				Object item = items.get(i);
				String itemId = null;
				if (id == ID_PERSP) {
					itemId = ((IPerspectiveDescriptor)item).getId();
				} else if (id == ID_VIEW) {
					itemId = ((IViewDescriptor)item).getId();
				} else if (id == ID_WIZARD) {
					itemId = ((WorkbenchWizardElement)item).getID();
				}
				if (menuItemId.equals(itemId)) return item;
			}
			return null;
		}
		private boolean isFullyChecked() {
			if (getItems().size() != getCheckedItems().size()) return false;
			for (int i=0; i<children.size(); i++) {
				ShortcutMenu child = (ShortcutMenu)children.get(i);
				if (!child.isFullyChecked()) return false;
			}
			return true;
		}
		private boolean isFullyUnchecked() {
			if (getCheckedItems().size() != 0) return false;
			for (int i=0; i<children.size(); i++) {
				ShortcutMenu child = (ShortcutMenu)children.get(i);
				if (!child.isFullyUnchecked()) return false;
			}
			return true;
		}
		private void removeCheckedItem(Object item) {
			checkedItems.remove(item);
		}
		private void checked(boolean checked) {
			checkedItems = new ArrayList();
			if (checked) {
				checkedItems.addAll(items);
			} 
			for (int i=0; i<children.size(); i++) {
				ShortcutMenu child = (ShortcutMenu)children.get(i);
				child.checked(checked);
			}
		}
	}
	class TreeContentProvider implements ITreeContentProvider  {
		public void dispose() {
		}
		public Object[] getChildren(Object element) {
			if (element instanceof ActionSetDisplayItem) {
				ActionSetDisplayItem node = (ActionSetDisplayItem)element;
				return node.children.toArray();
			} else if (element instanceof ShortcutMenu) {
				ShortcutMenu node = (ShortcutMenu)element;
				return node.children.toArray();
			}
			return null;
		}
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
		public Object getParent(Object element) {
			if (element instanceof ActionSetDisplayItem) {
				ActionSetDisplayItem node = (ActionSetDisplayItem)element;
				return node.parent;
			} else if (element instanceof ShortcutMenu) {
				ShortcutMenu node = (ShortcutMenu)element;
				return node.parent;
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			if (element instanceof ActionSetDisplayItem) {
				ActionSetDisplayItem node = (ActionSetDisplayItem)element;
				return node.children.size() > 0;
			} else if (element instanceof ShortcutMenu) {
				ShortcutMenu node = (ShortcutMenu)element;
				return node.children.size() > 0;
			}
			return false;
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class IndentedTableLabelProvider extends LabelProvider implements IIndentedTableLabelProvider  {
		private Map imageTable = new Hashtable();
		public void dispose() {
			for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
				((Image) i.next()).dispose();
			}
			imageTable = null;
		}
		public Image getColumnImage(Object element, int column) {
			ActionSetDisplayItem item = (ActionSetDisplayItem)element;
			ImageDescriptor descriptor = item.imageDescriptor;
			if (descriptor == null) {
				if (item.type == ActionSetDisplayItem.MENUITEM) {
					if (item.children.size() > 0) descriptor = menuImageDescriptor;
					else return null;
				} else if (item.type == ActionSetDisplayItem.TOOLITEM) {
					if (item.children.size() > 0) descriptor = toolbarImageDescriptor;
					else return null;
				} else {
					return null;
				}
			}
			//obtain the cached image corresponding to the descriptor
			if (imageTable == null) {
				imageTable = new Hashtable(40);
			}
			Image image = (Image) imageTable.get(descriptor);
			if (image == null) {
				image = descriptor.createImage();
				imageTable.put(descriptor, image);
			}
			return image;
		}
		public String getColumnText(Object element, int column) {
			if (element instanceof ActionSetDisplayItem) {
				ActionSetDisplayItem item = (ActionSetDisplayItem)element;
				String text = item.getDisplayText();
				return text;
			}
			return ""; //$NON-NLS-1$
		}
		public int getIndent(Object element) {
			if (element instanceof ActionSetDisplayItem) {
				int depth = ((ActionSetDisplayItem)element).getDepth();
				depth = depth - 1;
				return depth;
			}
			return 0;
		}
	}
public CustomizePerspectiveDialog(Shell parentShell, Perspective persp) {
	super(parentShell);
	perspective = persp;
	window = (WorkbenchWindow)((Workbench)WorkbenchPlugin.getDefault().getWorkbench()).getActiveWorkbenchWindow();
	// build a structure for the menuitems and toolitems that the workbench contributes
	customizeWorkbenchActionBars = new CustomizeActionBars(new MenuManager(), new CoolBarManager());
	window.fillActionBars(customizeWorkbenchActionBars);
	initializeActionSetInput();
	initializeShortcutMenuInput();
}
private void addListeners() {
	tabFolder.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleTabSelected(event);
		}
	});
	actionSetsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			handleActionSetSelected(event);
		}
	});
	actionSetsViewer.getControl().addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e)  {
			handleActionSetViewerKeyPressed(e);
		}
		public void keyReleased(KeyEvent e) {}
	});
	actionSetMenuViewer.getControl().addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e)  {
			handleActionSetMenuViewerKeyPressed(e);
		}
		public void keyReleased(KeyEvent e) {}
	});
	actionSetToolbarViewer.getControl().addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e)  {
			handleActionSetToolbarViewerKeyPressed(e);
		}
		public void keyReleased(KeyEvent e) {}
	});
	menusCombo.addSelectionListener(new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
			// do nothing
		}		
		public void widgetSelected(SelectionEvent e) {
			handleMenuSelected(e);
		}
	});
	menusCombo.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			handleMenuModified(e);
		}		
	});
	menuCategoriesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			handleMenuCategorySelected(event);
		}
	});
	menuCategoriesViewer.addCheckStateListener(new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleMenuCategoryChecked(event);
		}
	});
	menuItemsViewer.addCheckStateListener(new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleMenuItemChecked(event);
		}
	});
}
private void buildMenusAndToolbarsFor (ActionSetDescriptor actionSetDesc) {
	String id = actionSetDesc.getId();
	ActionSetActionBars bars = new ActionSetActionBars(customizeWorkbenchActionBars, id);
	PluginActionSetBuilder builder = new PluginActionSetBuilder();
	PluginActionSet actionSet = null;
	try {
		actionSet = (PluginActionSet)actionSetDesc.createActionSet();
		actionSet.init(null, bars);
	} catch (CoreException ex) {
		WorkbenchPlugin.log("Unable to create action set " + actionSetDesc.getId()); //$NON-NLS-1$
		return;
	}
	builder.buildMenuAndToolBarStructure(actionSet, window);
}
private void checkInitialActionSetSelections() {
	// Check off the action sets that are active for the perspective.
	IActionSetDescriptor [] actionSets = perspective.getActionSets();
	if (actionSets != null) {
		for (int i = 0; i < actionSets.length; i++)
			actionSetsViewer.setChecked(actionSets[i],true);
	}
}
private void checkInitialMenuCategorySelections(ShortcutMenu menu) {
	// Check off the shortcut categories that are active for the perspective.
	if (menu.children.size() == 0) updateMenuCategoryCheckedState(menu);
	else {
		for (int i=0; i<menu.children.size(); i++) {
			ShortcutMenu child = (ShortcutMenu)menu.children.get(i);
			checkInitialMenuCategorySelections(child);
		}
	}
}
public boolean close() {
	lastSelectedMenuIndex = menusCombo.getSelectionIndex();
	lastSelectedTab = tabFolder.getSelectionIndex();
	StructuredSelection selection = (StructuredSelection)actionSetsViewer.getSelection();
	if (selection.isEmpty()) lastSelectedActionSetId = null;
	else lastSelectedActionSetId = ((ActionSetDescriptor)selection.getFirstElement()).getId();
	((CoolBarManager)customizeWorkbenchActionBars.getToolBarManager()).dispose();
	customizeWorkbenchActionBars.getMenuManager().dispose();
	return super.close();
}
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("ActionSetSelection.customize")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, IHelpContextIds.ACTION_SET_SELECTION_DIALOG);
}
private boolean containsActionSet(MenuManager mgr, String actionSetId) {
	// Return whether or not the given menuManager contains items for the
	// given actionSetId.
	IContributionItem[] menuItems = mgr.getItems();
	for (int j=0; j<menuItems.length; j++) {
		IContributionItem menuItem = menuItems[j];
		if (menuItem instanceof ActionSetContributionItem) {
			ActionSetContributionItem actionSetItem = (ActionSetContributionItem)menuItem;
			if (actionSetItem.getActionSetId().equals(actionSetId)) {
				return true;
			}
		} else if (menuItem instanceof MenuManager) {
			MenuManager childMgr = (MenuManager)menuItem;
			boolean found = containsActionSet(childMgr, actionSetId);
			if (found) return true;
		}
	}
	return false;
}
private Composite createActionSetsPage(Composite parent) {
	GridData data;
	Font font = parent.getFont();

	Composite actionSetsComposite= new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	actionSetsComposite.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionSetsComposite.setLayoutData(data);

	// Select... label
	Label label = new Label(actionSetsComposite, SWT.WRAP);
	label.setText(WorkbenchMessages.format("ActionSetSelection.selectActionSetsLabel", new Object[] {perspective.getDesc().getLabel()})); //$NON-NLS-1$
	label.setFont(font);
	data = new GridData(GridData.FILL_BOTH);
	data.widthHint = TABLES_WIDTH;
	label.setLayoutData(data);

	Label sep = new Label(actionSetsComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
	data = new GridData(GridData.FILL_HORIZONTAL);
	sep.setLayoutData(data);

	SashForm sashComposite= new SashForm(actionSetsComposite, SWT.HORIZONTAL);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = TABLE_HEIGHT;
	data.widthHint = TABLES_WIDTH;
	sashComposite.setLayoutData(data);
				
	// Action Set List Composite
	Composite actionSetGroup = new Composite(sashComposite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	actionSetGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionSetGroup.setLayoutData(data);
	actionSetGroup.setFont(font);

	label = new Label(actionSetGroup,SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.availableActionSets")); //$NON-NLS-1$
	label.setFont(font);

	actionSetsViewer = CheckboxTableViewer.newCheckList(actionSetGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	actionSetsViewer.getTable().setLayoutData(data);
	actionSetsViewer.getTable().setFont(font);
	actionSetsViewer.setLabelProvider(new WorkbenchLabelProvider());
	actionSetsViewer.setContentProvider(new ListContentProvider());
	actionSetsViewer.setSorter(new ActionSetSorter());

	// Action list Group
	Composite actionItemGroup = new Composite(sashComposite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.horizontalSpacing = 5;
	actionItemGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionItemGroup.setLayoutData(data);

	Composite actionGroup = new Composite(actionItemGroup, SWT.NULL);
	layout = new GridLayout();
	layout.numColumns = 2;
	layout.makeColumnsEqualWidth = true;
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.horizontalSpacing = 0;
	actionGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionGroup.setLayoutData(data);

	label = new Label(actionGroup, SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.menubarActions")); //$NON-NLS-1$
	label.setFont(font);

	label = new Label(actionGroup, SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.toolbarActions")); //$NON-NLS-1$
	label.setFont(font);

	actionSetMenuViewer = new IndentedTableViewer(actionGroup);
	data = new GridData(GridData.FILL_BOTH);
	actionSetMenuViewer.getControl().setLayoutData(data);
	actionSetMenuViewer.setLabelProvider(new IndentedTableLabelProvider());
	actionSetMenuViewer.setContentProvider(new ListContentProvider());

	actionSetToolbarViewer = new IndentedTableViewer(actionGroup);
	data = new GridData(GridData.FILL_BOTH);
	actionSetToolbarViewer.getControl().setLayoutData(data);
	actionSetToolbarViewer.setLabelProvider(new IndentedTableLabelProvider());
	actionSetToolbarViewer.setContentProvider(new ListContentProvider());

	sashComposite.setWeights(new int[]{30, 70});

	// Use F2... label
	label = new Label(actionSetsComposite, SWT.WRAP);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.selectActionSetsHelp")); //$NON-NLS-1$
	label.setFont(font);
	data = new GridData();
	data.widthHint = TABLES_WIDTH;
	label.setLayoutData(data);

	return actionSetsComposite;
}
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite)super.createDialogArea(parent);
	
	tabFolder = new TabFolder(composite, SWT.NONE);
	GridData gd = new GridData(GridData.FILL_BOTH);
	tabFolder.setLayoutData(gd);
	
	TabItem item = new TabItem(tabFolder, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ActionSetSelection.menuTab")); //$NON-NLS-1$
	item.setControl(createMenusPage(tabFolder));

	item = new TabItem(tabFolder, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ActionSetSelection.actionSetsTab")); //$NON-NLS-1$
	item.setControl(createActionSetsPage(tabFolder));
		
	addListeners();
	actionSetsViewer.setInput(actionSets);
	checkInitialActionSetSelections();
	ArrayList children = rootMenu.getChildren();
	String[] itemNames = new String[children.size()];
	for (int i=0; i<children.size(); i++) {
		itemNames[i]=((ShortcutMenu)children.get(i)).label;
	}
	menusCombo.setItems(itemNames);
	setInitialSelections();

	return composite;
}
private Composite createMenusPage(Composite parent) {
	GridData data;
	Font font = parent.getFont();

	Composite menusComposite= new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	menusComposite.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	menusComposite.setLayoutData(data);

	// Select... label
	Label label = new Label(menusComposite, SWT.WRAP);
	label.setText(WorkbenchMessages.format("ActionSetSelection.selectMenusLabel", new Object[] {perspective.getDesc().getLabel()})); //$NON-NLS-1$
	label.setFont(font);
	data = new GridData();
	data.widthHint = TABLES_WIDTH;
	label.setLayoutData(data);

	Label sep = new Label(menusComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
	data = new GridData(GridData.FILL_HORIZONTAL);
	sep.setLayoutData(data);

	SashForm sashComposite= new SashForm(menusComposite, SWT.HORIZONTAL);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = TABLE_HEIGHT;
	data.widthHint = TABLES_WIDTH;
	sashComposite.setLayoutData(data);

	// Menus List
	Composite menusGroup = new Composite(sashComposite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	menusGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	menusGroup.setLayoutData(data);
	menusGroup.setFont(font);

	label = new Label(menusGroup,SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.availableMenus")); //$NON-NLS-1$
	label.setFont(font);

	menusCombo = new Combo(menusGroup, SWT.READ_ONLY);
	menusCombo.setFont(font);
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	menusCombo.setLayoutData(gridData);

	// Categories Tree
	label = new Label(menusGroup,SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.availableCategories")); //$NON-NLS-1$
	label.setFont(font);

	menuCategoriesViewer = new CheckboxTreeViewer(menusGroup);
	data = new GridData(GridData.FILL_BOTH);
	menuCategoriesViewer.getControl().setLayoutData(data);
	menuCategoriesViewer.setLabelProvider(new LabelProvider());
	menuCategoriesViewer.setContentProvider(new TreeContentProvider());
	menuCategoriesViewer.setSorter(new WorkbenchViewerSorter());

	// Menu items list
	Composite menuItemsGroup = new Composite(sashComposite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	menuItemsGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	menuItemsGroup.setLayoutData(data);
	menuItemsGroup.setFont(font);

	label = new Label(menuItemsGroup, SWT.NONE);
	label.setText(WorkbenchMessages.getString("ActionSetSelection.menuItems")); //$NON-NLS-1$
	label.setFont(font);

	menuItemsViewer = CheckboxTableViewer.newCheckList(menuItemsGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	Table menuTable = menuItemsViewer.getTable();
	menuTable.setLayoutData(data);
	menuTable.setFont(font);
	menuItemsViewer.setLabelProvider(new ShortcutMenuItemLabelProvider());
	menuItemsViewer.setContentProvider(new ShortcutMenuItemContentProvider());
	menuItemsViewer.setSorter(new WorkbenchViewerSorter());

	menuTable.setHeaderVisible(true);
	int[] columnWidths = new int[shortcutMenuColumnWidths.length];
	for (int i = 0; i < shortcutMenuColumnWidths.length; i++) {
		columnWidths[i] = convertHorizontalDLUsToPixels(shortcutMenuColumnWidths[i]);
	}
	for (int i = 0; i < shortcutMenuColumnHeaders.length; i++) {
		TableColumn tc = new TableColumn(menuTable, SWT.NONE,i);
		tc.setResizable(true);
		tc.setText(shortcutMenuColumnHeaders[i]);
		tc.setWidth(columnWidths[i]);
	}
	sashComposite.setWeights(new int[]{30, 70});
	return menusComposite;
}
private void handleActionSetMenuViewerKeyPressed(KeyEvent event) {
	// popup the description for the selected action set menu item
	if (event.keyCode == SWT.F2 && event.stateMask == 0) {
		IStructuredSelection sel = (IStructuredSelection)actionSetMenuViewer.getSelection();
		ActionSetDisplayItem element = (ActionSetDisplayItem)sel.getFirstElement();
		if (element != null) {
			String desc = element.description;
			if (desc == null || desc.equals("")) { //$NON-NLS-1$
				desc = WorkbenchMessages.getString("ActionSetSelection.noDesc"); //$NON-NLS-1$
			}
			popUp(desc);
		}
	}
}
private void handleActionSetSelected(SelectionChangedEvent event) {
	IStructuredSelection sel = (IStructuredSelection)event.getSelection();
	ActionSetDescriptor element = (ActionSetDescriptor)sel.getFirstElement();
	if (element == selectedActionSet) return;
	String actionSetId = element.getId();
	ArrayList structures = (ArrayList)actionSetStructures.get(actionSetId);
	ActionSetDisplayItem menubarStructure = null;
	ActionSetDisplayItem toolbarStructure = null;
	if (structures == null) {
		structures = new ArrayList(2);
		menubarStructure = new ActionSetDisplayItem("Menubar"); //$NON-NLS-1$
		toolbarStructure = new ActionSetDisplayItem("Toolbar"); //$NON-NLS-1$
		MenuManager windowMenuMgr = window.getMenuManager();
		CoolBarManager windowCoolBarManager = window.getCoolBarManager();
		if (containsActionSet(windowMenuMgr, actionSetId)) {
			// if the action set is active, we can use the workbench menu and coolbar managers
			// to figure out the action set structure.
			menubarStructure.fillMenusFor(actionSetId, windowMenuMgr);
			toolbarStructure.fillToolsFor(actionSetId, windowCoolBarManager);
		} else {
			// The action set is not active, so build the menus and toolbars for it using
			// our fake action bars.
			buildMenusAndToolbarsFor(element);
			menubarStructure.fillMenusFor(actionSetId, customizeWorkbenchActionBars.menuManager);
			toolbarStructure.fillToolsFor(actionSetId, (CoolBarManager)customizeWorkbenchActionBars.coolBarManager);
		}
		structures.add(menubarStructure);
		structures.add(toolbarStructure);			
		actionSetStructures.put(actionSetId, structures);
	}
	if (menubarStructure == null) menubarStructure = (ActionSetDisplayItem)structures.get(0);
	if (toolbarStructure == null) toolbarStructure = (ActionSetDisplayItem)structures.get(1);
	// fill the menu structure table
	if (element != actionSetMenuViewer.getInput()) {
		try {
			actionSetMenuViewer.getControl().setRedraw(false);
			actionSetMenuViewer.setInput(menubarStructure.getElements());
			if (menubarStructure.children.size() > 0) {
				actionSetMenuViewer.reveal(menubarStructure.children.get(0));
			}
		} finally {
			actionSetMenuViewer.getControl().setRedraw(true);
		}
	}
	// fill the toolbar structure table
	if (element != actionSetToolbarViewer.getInput()) {
		try {
			actionSetToolbarViewer.getControl().setRedraw(false);
			actionSetToolbarViewer.setInput(toolbarStructure.getElements());
			if (toolbarStructure.children.size() > 0) {
				actionSetToolbarViewer.reveal(toolbarStructure.children.get(0));
			}
		} finally {
			actionSetToolbarViewer.getControl().setRedraw(true);
		}
	}
	selectedActionSet = element;
}
private void handleActionSetToolbarViewerKeyPressed(KeyEvent event) {
	// popup the description for the selected action set toolbar item
	if (event.keyCode == SWT.F2 && event.stateMask == 0) {
		IStructuredSelection sel = (IStructuredSelection)actionSetToolbarViewer.getSelection();
		ActionSetDisplayItem element = (ActionSetDisplayItem)sel.getFirstElement();
		if (element != null) {
			String desc = element.description;
			if (desc == null || desc.equals("")) { //$NON-NLS-1$
				desc = WorkbenchMessages.getString("ActionSetSelection.noDesc"); //$NON-NLS-1$
			}
			popUp(desc);
		}
	}
}
private void handleActionSetViewerKeyPressed(KeyEvent event) {
	// popup the description for the selected action set
	if (event.keyCode == SWT.F2 && event.stateMask == 0) {
		IStructuredSelection sel = (IStructuredSelection)actionSetsViewer.getSelection();
		ActionSetDescriptor element = (ActionSetDescriptor)sel.getFirstElement();
		if (element != null) {
			String desc = element.getDescription();
			if (desc == null || desc.equals("")) { //$NON-NLS-1$
				desc = WorkbenchMessages.getString("ActionSetSelection.noDesc"); //$NON-NLS-1$
			}
			popUp(desc);
		}
	}
}
private void handleMenuCategoryChecked(CheckStateChangedEvent event) {
	ShortcutMenu checkedCategory = (ShortcutMenu)event.getElement();
	boolean checked = event.getChecked();
	checkedCategory.checked(checked);
	// check/uncheck the element's category subtree
	menuCategoriesViewer.setSubtreeChecked(checkedCategory, checked);
	// set gray state of the element's category subtree, all items should
	// not be grayed
	ArrayList subtree = checkedCategory.getSubtreeItems();
	ShortcutMenu menuItemInput = (ShortcutMenu)menuItemsViewer.getInput();
	for (int i=0; i<subtree.size(); i++) {
		Object child = subtree.get(i);
		menuCategoriesViewer.setGrayed(child, false);
		if (child == menuItemInput) menuItemsViewer.setAllChecked(checked);
	}
	menuCategoriesViewer.setGrayed(checkedCategory, false);
	updateMenuCategoryCheckedState(checkedCategory.parent);
}
private void handleMenuCategorySelected(SelectionChangedEvent event) {
	IStructuredSelection sel = (IStructuredSelection)event.getSelection();
	ShortcutMenu element = (ShortcutMenu)sel.getFirstElement();
	if (element == selectedMenuCategory) return;
	if (element != menuItemsViewer.getInput()) {
		menuItemsViewer.setInput(element);
	}
	if (element != null) {
		menuItemsViewer.setCheckedElements(element.getCheckedItems().toArray());
	}
}
private void handleMenuItemChecked(CheckStateChangedEvent event) {
	ShortcutMenu selectedMenu = (ShortcutMenu)menuItemsViewer.getInput();
	boolean itemChecked = event.getChecked();
	Object item = event.getElement();
	if (itemChecked) {
		selectedMenu.addCheckedItem(item);
	} else {
		selectedMenu.removeCheckedItem(item);
	}
	updateMenuCategoryCheckedState(selectedMenu);
}
private void handleMenuModified(ModifyEvent event) {
	String text = menusCombo.getText();
	String[] items = menusCombo.getItems();
	int itemIndex = -1;
	for (int i=0; i<items.length; i++) {
		if (items[i].equals(text)) {
			itemIndex = i;
			break;
		}
	}
	if (itemIndex == -1) return;
	ShortcutMenu element = (ShortcutMenu)rootMenu.children.get(itemIndex);
	handleMenuSelected(element);
}
private void handleMenuSelected(SelectionEvent event) {
	int i = menusCombo.getSelectionIndex();
	ShortcutMenu element = (ShortcutMenu)rootMenu.children.get(i);
	handleMenuSelected(element);
}
private void handleMenuSelected(ShortcutMenu element) {
	if (element != menuCategoriesViewer.getInput()) {
		menuCategoriesViewer.setInput(element);
		menuCategoriesViewer.expandAll();
		if (element != null) {
			if (element.getChildren().size() > 0) {
				StructuredSelection sel = new StructuredSelection(element.getChildren().get(0));
				menuCategoriesViewer.setSelection(sel, true);
			} else {
				menuItemsViewer.setInput(element);
				menuItemsViewer.setCheckedElements(element.getCheckedItems().toArray());
			}
		} else {
			menuCategoriesViewer.setInput(element);
			menuItemsViewer.setInput(element);
		}
		checkInitialMenuCategorySelections(rootMenu);			
	}
}
private void handleTabSelected(SelectionEvent event) {
	TabItem item = (TabItem)event.item;
	Control control = item.getControl();
	if (control != null) control.setFocus();	
}
private void initializeActionSetInput() {
	// Just get the action sets at this point.  Do not load the action set until it
	// is actually selected in the dialog.
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	IActionSetDescriptor [] sets = reg.getActionSets();
	for (int i = 0; i < sets.length; i ++) {
		ActionSetDescriptor actionSetDesc = (ActionSetDescriptor)sets[i];
		actionSets.add(actionSetDesc);
	}
	String iconPath = "icons/full/obj16/menu.gif";//$NON-NLS-1$
	try {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath);
		menuImageDescriptor = ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		// Should not happen
	}
	iconPath = "icons/full/obj16/toolbar.gif";//$NON-NLS-1$
	try {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath);
		toolbarImageDescriptor = ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		// Should not happen
	}
}
private void initializeShortCutMenu(ShortcutMenu menu, WizardCollectionElement element, ArrayList activeIds) {
	ShortcutMenu category = new ShortcutMenu(menu, element.getId(), element.getLabel(element));
	Object[] wizards = element.getWizards();
	for (int i = 0; i < wizards.length; i++) {
		WorkbenchWizardElement wizard = (WorkbenchWizardElement)wizards[i];
		category.addItem(wizard);
		if (activeIds.contains(wizard.getID())) category.addCheckedItem(wizard);
	}
	Object[] children = element.getChildren();
	for (int i = 0; i < children.length; i++) {
		initializeShortCutMenu(category, (WizardCollectionElement)children[i], activeIds);
	}
}
private void initializeShortcutMenuInput() {
	rootMenu = new ShortcutMenu(null, "Root", ""); //$NON-NLS-1$ //$NON-NLS-2$
	ShortcutMenu wizardMenu = new ShortcutMenu(rootMenu, ShortcutMenu.ID_WIZARD, WorkbenchMessages.getString("ActionSetDialogInput.wizardCategory")); //$NON-NLS-1$
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader();
	WizardCollectionElement wizardCollection = (WizardCollectionElement)rdr.getWizards();
	Object [] wizardCategories = wizardCollection.getChildren();
	ArrayList activeIds = perspective.getNewWizardActionIds();
	for (int i = 0; i < wizardCategories.length; i ++) {
		WizardCollectionElement element = (WizardCollectionElement)wizardCategories[i];
		initializeShortCutMenu(wizardMenu, element, activeIds);
	}

	ShortcutMenu perspMenu = new ShortcutMenu(rootMenu, ShortcutMenu.ID_PERSP, WorkbenchMessages.getString("ActionSetDialogInput.perspectiveCategory")); //$NON-NLS-1$
	IPerspectiveRegistry perspReg = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	IPerspectiveDescriptor [] persps = perspReg.getPerspectives();
	for (int i = 0; i < persps.length; i ++) {
		perspMenu.addItem(persps[i]);
	}
	activeIds = perspective.getPerspectiveActionIds();
	for (int i = 0; i < activeIds.size(); i++) {
		String id = (String)activeIds.get(i);
		Object item = perspMenu.getItem(id);
		if (item != null) perspMenu.addCheckedItem(item);
	}

	ShortcutMenu viewMenu = new ShortcutMenu(rootMenu, ShortcutMenu.ID_VIEW, WorkbenchMessages.getString("ActionSetDialogInput.viewCategory")); //$NON-NLS-1$
	IViewRegistry viewReg = WorkbenchPlugin.getDefault().getViewRegistry();
	ICategory[] categories = viewReg.getCategories();
	activeIds = perspective.getShowViewActionIds();
	for (int i=0; i<categories.length; i++) {
		ICategory category = categories[i];
		ShortcutMenu viewCategory = new ShortcutMenu(viewMenu, category.getId(), category.getLabel());
		ArrayList views = category.getElements();
		if (views != null) {
			for (int j=0; j<views.size(); j++) {
				IViewDescriptor view = (IViewDescriptor)views.get(j);
				viewCategory.addItem(view);
				if (activeIds.contains(view.getId())) viewCategory.addCheckedItem(view);
			}
		}
	}
}
protected void okPressed() {
	ArrayList menus = rootMenu.children;
	for (int i=0; i < menus.size(); i++) {
		ShortcutMenu menu = (ShortcutMenu)menus.get(i);
		if (menu.id == ShortcutMenu.ID_VIEW) {
			perspective.setShowViewActionIds(menu.getCheckedItemIds());
		} else if (menu.id == ShortcutMenu.ID_PERSP) {
			perspective.setPerspectiveActionIds(menu.getCheckedItemIds());
		} else if (menu.id == ShortcutMenu.ID_WIZARD) {
			perspective.setNewWizardActionIds(menu.getCheckedItemIds());
		}
	}
	ArrayList actionSets = new ArrayList();
	Object[] selected = actionSetsViewer.getCheckedElements();
	for (int i = 0; i < selected.length; i ++) {
		Object obj = selected[i];
		actionSets.add(obj);
	}
	IActionSetDescriptor [] actionSetArray = new IActionSetDescriptor[actionSets.size()];
	actionSetArray = (IActionSetDescriptor [])actionSets.toArray(actionSetArray);
	perspective.setActionSets(actionSetArray);
	
	super.okPressed();
}
private void popUp(String description) {
	Display display = getShell().getDisplay();
	final Shell descShell = new Shell(getShell(), SWT.ON_TOP | SWT.NO_TRIM);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 1;
	layout.marginWidth = 1;
	descShell.setLayout(layout);
	descShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

	Composite insetComposite = new Composite(descShell, SWT.NULL);
	insetComposite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	layout = new GridLayout();
	layout.marginHeight = 2;
	layout.marginWidth = 2;
	insetComposite.setLayout(layout);
	GridData data = new GridData(GridData.FILL_BOTH);
	insetComposite.setLayoutData(data);	
	insetComposite.addFocusListener(new FocusListener() {
		public void focusLost(FocusEvent e)  {
			descShell.dispose();
		}
		public void focusGained(FocusEvent e) {}
	});
	insetComposite.addKeyListener(new KeyListener() {
		public void keyPressed(KeyEvent e)  {
			descShell.dispose();
		}
		public void keyReleased(KeyEvent e) {}
	});
		
	StyledText descText = new StyledText(insetComposite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
	descText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	descText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	data = new GridData(GridData.FILL_BOTH);
	data.widthHint = 200;
	descText.setLayoutData(data);
	descText.setText(description);
	descText.setEnabled(false);

	descShell.pack();
	Rectangle displayBounds= display.getClientArea();
	Rectangle bounds = descShell.getBounds();
	Point point = display.getCursorLocation();
	Point location = new Point(point.x + cursorSize, point.y + cursorSize);
	if (location.x + bounds.width > displayBounds.x + displayBounds.width) {
		location.x = displayBounds.x + displayBounds.width - bounds.width;
	}
	if (location.y + bounds.height > displayBounds.x + displayBounds.height) {
		location.y = displayBounds.y + displayBounds.height - bounds.height;
	}
	descShell.setLocation(location);
	descShell.setVisible(true);
	descShell.setFocus();
}
private String removeShortcut(String label) {
	if (label == null) return label;
	int end = label.lastIndexOf('@');
	if (end >= 0) label = label.substring(0, end);
	return label;
}
private void setInitialSelections() {
	Object item = null;
	if (lastSelectedActionSetId == null) {
		item = actionSetsViewer.getElementAt(0);
	} else {
		for (int i=0; i<actionSets.size(); i++) {
			ActionSetDescriptor actionSet = (ActionSetDescriptor)actionSets.get(i);
			if (actionSet.getId().equals(lastSelectedActionSetId)) {
				item = actionSet;
				break;
			}
		}
	}		
	StructuredSelection sel = new StructuredSelection(item);
	actionSetsViewer.setSelection(sel, true);
	
	menusCombo.select(lastSelectedMenuIndex);

	if (lastSelectedTab != -1) {
		tabFolder.setSelection(lastSelectedTab);
	}
	if (tabFolder.getSelectionIndex() == 0) {
		menusCombo.setFocus();
	} else {
		actionSetsViewer.getControl().setFocus();
	}
}
private void updateMenuCategoryCheckedState(ShortcutMenu menu) {
	if (menu == rootMenu) return;
	if (menu.isFullyChecked()) {
		menuCategoriesViewer.setParentsGrayed(menu, false);
		menuCategoriesViewer.setChecked(menu, true);
	} else if (menu.isFullyUnchecked()) {
		menuCategoriesViewer.setParentsGrayed(menu, false);
		menuCategoriesViewer.setChecked(menu, false);
	} else {
		menuCategoriesViewer.setParentsGrayed(menu, true);
		menuCategoriesViewer.setChecked(menu, true);
	}
	updateMenuCategoryCheckedState(menu.parent);
}
}
