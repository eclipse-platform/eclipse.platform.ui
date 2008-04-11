/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.ActionSetActionBars;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.ActionSetMenuManager;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.PluginActionContributionItem;
import org.eclipse.ui.internal.PluginActionCoolBarContributionItem;
import org.eclipse.ui.internal.PluginActionSet;
import org.eclipse.ui.internal.PluginActionSetBuilder;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.registry.ActionSetDescriptor;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardCategory;

/**
 * Dialog to allow users the ability to customize the perspective. This includes
 * customizing menus and toolbars by adding, removing, or re-arranging commands
 * or groups of commands.
 * 
 */
// @issue need to break this not to show menu specific page
public class CustomizePerspectiveDialog extends TrayDialog {
	private DefaultToolTip actionSetMenuViewerTooltip;
	
	private DefaultToolTip actionSetToolbarViewerTooltip;
	
	private DefaultToolTip actionSetsViewerTooltip;
	
    private Perspective perspective;

    WorkbenchWindow window;

    private TabFolder tabFolder;

    private CheckboxTableViewer actionSetsViewer;

    private TreeViewer actionSetMenuViewer;

    private TreeViewer actionSetToolbarViewer;

    private Combo menusCombo;

    private CheckboxTreeViewer menuCategoriesViewer;

    private CheckboxTableViewer menuItemsViewer;

    private static int lastSelectedTab = -1;

    private static int lastSelectedMenuIndex = 0;

    private static String lastSelectedActionSetId = null;

    private final static int TAB_WIDTH_IN_DLUS = 490;

    private final static int TAB_HEIGHT_IN_DLUS = 230;

    private final String shortcutMenuColumnHeaders[] = {
            WorkbenchMessages.ActionSetSelection_menuColumnHeader,
            WorkbenchMessages.ActionSetSelection_descriptionColumnHeader };

    private int[] shortcutMenuColumnWidths = { 125, 300 };

    private ActionSetDescriptor selectedActionSet = null;

    private ShortcutMenu selectedMenuCategory = null;

    ImageDescriptor menuImageDescriptor = null;

    ImageDescriptor submenuImageDescriptor = null;

    ImageDescriptor toolbarImageDescriptor = null;

    ShortcutMenu rootMenu;

    private ArrayList actionSets = new ArrayList();

    private Hashtable actionSetStructures = new Hashtable();

	private IWorkbenchWindowConfigurer configurer;

    private List initiallyActive = new ArrayList();

    class ActionSetDisplayItem extends Object {
        /**
         * Tree representation for action set menu and toolbar items.  
         */
        ArrayList children = new ArrayList();

        ActionSetDisplayItem parent = null;

        private String id = null;

        private String text = ""; //$NON-NLS-1$

        String description = ""; //$NON-NLS-1$

        ImageDescriptor imageDescriptor;

        int type = MENUITEM;

        private final static int MENUITEM = 0;

        private final static int TOOLITEM = 1;

        ActionSetDisplayItem(String id) {
            this(null, id, "", MENUITEM); //$NON-NLS-1$
        }

        private ActionSetDisplayItem(ActionSetDisplayItem parent, String id,
                String text, int type) {
            if (parent != null) {
                parent.children.add(this);
                this.parent = parent;
            }
            this.id = id;
            this.type = type;
            this.text = removeShortcut(text);
            this.text = DialogUtil.removeAccel(this.text);
        }

        private ActionSetDisplayItem find(String itemId) {
            for (int i = 0; i < children.size(); i++) {
                ActionSetDisplayItem child = (ActionSetDisplayItem) children
                        .get(i);
                if (itemId.equals(child.id)) {
					return child;
				}
            }
            return null;
        }

        /**
         * @param actionSetId the action to fill
         * @param item the item to fill 
         */
        public void fillMenusFor(String actionSetId, IContributionItem item) {
            if (item instanceof ContributionManager) {
                ContributionManager mgr = (ContributionManager) item;
                IContributionItem[] items = mgr.getItems();
                for (int i = 0; i < items.length; i++) {
                    IContributionItem mgrItem = items[i];
                    if (mgrItem instanceof ActionSetContributionItem) {
                        ActionSetContributionItem actionSetItem = (ActionSetContributionItem) mgrItem;
                        if (actionSetItem.getActionSetId().equals(actionSetId)) {
                            IContributionItem innerItem = actionSetItem
                                    .getInnerItem();
                            if (innerItem instanceof MenuManager) {
                                MenuManager inner = (MenuManager) actionSetItem
                                        .getInnerItem();
                                ActionSetDisplayItem node = new ActionSetDisplayItem(
                                        this, inner.getId(), inner
                                                .getMenuText(), MENUITEM);
                                node.fillMenusFor(actionSetId, inner);
                            } else if (innerItem instanceof ActionSetMenuManager) {
                                ActionSetMenuManager inner = (ActionSetMenuManager) actionSetItem
                                        .getInnerItem();
                                MenuManager parentMgr = (MenuManager) inner
                                        .getParent();
                                ActionSetDisplayItem node = new ActionSetDisplayItem(
                                        this, inner.getId(), parentMgr
                                                .getMenuText(), MENUITEM);
                                node.fillMenusFor(actionSetId, parentMgr);
                            } else if (innerItem instanceof PluginActionContributionItem) {
                                PluginActionContributionItem inner = (PluginActionContributionItem) actionSetItem
                                        .getInnerItem();
                                ActionSetDisplayItem node = new ActionSetDisplayItem(
                                        this, inner.getId(), inner.getAction()
                                                .getText(), MENUITEM);
                                IAction action = inner.getAction();
                                if (action != null) {
                                    node.imageDescriptor = action
                                            .getImageDescriptor();
                                    node.description = action.getDescription();
                                }
                            }
                        }
                    } else if (mgrItem instanceof MenuManager) {
                        MenuManager menuMgr = (MenuManager) mgrItem;
                        boolean found = containsActionSet(menuMgr, actionSetId);
                        if (found) {
                            ActionSetDisplayItem node = new ActionSetDisplayItem(
                                    this, menuMgr.getId(), menuMgr
                                            .getMenuText(), MENUITEM);
                            node.fillMenusFor(actionSetId, menuMgr);
                        }
                    }
                }
            }
        }

        /**
         * @param actionSetId the action set to fill
         * @param mgr the manager to fill into
         */
        public void fillToolsFor(String actionSetId, CoolBarManager mgr) {
            IContributionItem[] items = mgr.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof IToolBarContributionItem) {
                	IToolBarContributionItem cbItem = (IToolBarContributionItem) items[i];
                    IContributionItem[] subItems = cbItem.getToolBarManager()
                            .getItems();
                    for (int j = 0; j < subItems.length; j++) {
                        IContributionItem subItem = subItems[j];
                        if (subItem instanceof PluginActionCoolBarContributionItem) {
                            PluginActionCoolBarContributionItem actionItem = (PluginActionCoolBarContributionItem) subItem;
                            if (actionItem.getActionSetId().equals(actionSetId)) {
                                String toolbarId = cbItem.getId();
                                ActionSetDisplayItem toolbar = find(toolbarId);
                                if (toolbar == null) {
                                    String toolbarText = window
                                            .getToolbarLabel(toolbarId);
                                    toolbar = new ActionSetDisplayItem(this,
                                            toolbarId, toolbarText, TOOLITEM);
                                }
                                IAction action = actionItem.getAction();
                                String toolItemText = action.getToolTipText();
                                if (toolItemText == null) {
									toolItemText = action.getText();
								}
                                ActionSetDisplayItem toolItem = new ActionSetDisplayItem(
                                        toolbar, action.getId(), toolItemText,
                                        TOOLITEM);
                                toolItem.imageDescriptor = action
                                        .getImageDescriptor();
                                toolItem.description = action.getDescription();
                            }
                        }
                    }
                }
            }
        }

        int getDepth() {
            if (parent == null) {
				return 0;
			}
            return parent.getDepth() + 1;
        }

        String getDisplayText() {
            if (type == MENUITEM) {
                if (children.size() > 0) {
                    if (parent.id.equals("Root")) { //$NON-NLS-1$
                        return NLS.bind(WorkbenchMessages.ActionSetSelection_menubarLocation, text );
                    }
                }
                return text;
            } 
            if (children.size() > 0) {
				return NLS.bind(WorkbenchMessages.ActionSetSelection_toolbarLocation, text );
			} 
            
            return text;
        }

        protected boolean isTopLevelMenu() {
            if (parent == null) {
				return false;
			}
            return parent.parent == null;
        }
        
        public String toString() {
        	StringBuffer sb = new StringBuffer();
        	for (int i = getDepth(); --i >= 0;) {
        		sb.append("  "); //$NON-NLS-1$
        	}
    		sb.append("id: "); //$NON-NLS-1$
    		sb.append(id);
    		sb.append("  text: "); //$NON-NLS-1$
    		sb.append(text);
        	sb.append('\n');
        	for (int i = 0; i < children.size(); ++i) {
        		sb.append(children.get(i));
        	}
        	return sb.toString();
        		
        }
    }

    /**
     * The proxy IActionBarConfigurer that gets passed to the application's
     * ActionBarAdvisor.  This is used to construct a representation of the
     * window's hardwired menus and toolbars in order to display their
     * structure properly in the preview panes.
     */
    public class CustomizeActionBars implements IActionBarConfigurer2,
			IActionBars2 {
        
        IWorkbenchWindowConfigurer configurer;
        
        /**
         * Fake action bars to use to build the menus and toolbar contributions for the
         * workbench.  We cannot use the actual workbench action bars, since doing so would
         * make the action set items visible.  
         */
        MenuManager menuManager = new MenuManager();
        CoolBarManager coolBarManager = new CoolBarManager();
        StatusLineManager statusLineManager = new StatusLineManager();
        
        /**
         * Create a new instance of this class.
         * 
         * @param configurer the configurer
         */
        public CustomizeActionBars(IWorkbenchWindowConfigurer configurer) {
            this.configurer = configurer;
        }

        
        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer#getWindowConfigurer()
         */
        public IWorkbenchWindowConfigurer getWindowConfigurer() {
            return configurer;
        }
        
        /*
         *  (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer#getMenuManager()
         */
        public IMenuManager getMenuManager() {
            return menuManager;
        }

        /*
         *  (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer#getStatusLineManager()
         */
        public IStatusLineManager getStatusLineManager() {
            return statusLineManager;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.AbstractActionBarConfigurer#getCoolBarManager()
         */
        public ICoolBarManager getCoolBarManager() {
            return coolBarManager;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getToolBarManager()
         */
        public IToolBarManager getToolBarManager() {
            return null;
        }

        public void setGlobalActionHandler(String actionID, IAction handler) {
        }

        public void updateActionBars() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#clearGlobalActionHandlers()
         */
        public void clearGlobalActionHandlers() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IActionBars#getGlobalActionHandler(java.lang.String)
         */
        public IAction getGlobalActionHandler(String actionId) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.application.IActionBarConfigurer#registerGlobalAction(org.eclipse.jface.action.IAction)
         */
        public void registerGlobalAction(IAction action) {
        }

        /**
         * Clean up the action bars.
         */
		public void dispose() {
			coolBarManager.dispose();
	        menuManager.dispose();	
	        statusLineManager.dispose();
	    }

		public final IServiceLocator getServiceLocator() {
			return configurer.getWindow();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer#createToolBarContributionItem(org.eclipse.jface.action.IToolBarManager, java.lang.String)
		 */
		public IToolBarContributionItem createToolBarContributionItem(IToolBarManager toolBarManager, String id) {
			return new ToolBarContributionItem2(toolBarManager, id);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.application.IActionBarConfigurer#createToolBarManager(int)
		 */
		public IToolBarManager createToolBarManager() {
			return new ToolBarManager();
		}
    }

    class ShortcutMenuItemContentProvider implements IStructuredContentProvider {

        ShortcutMenuItemContentProvider() {
        }

        public Object[] getElements(Object input) {
            if (input instanceof ShortcutMenu) {
                return ((ShortcutMenu) input).getItems().toArray();
            }
            return new Object[0];
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }
    }

    class ShortcutMenuItemLabelProvider extends LabelProvider implements
            ITableLabelProvider {
        private Map imageTable = new Hashtable();

        private final static int COLUMN_ID = 0;

        private final static int COLUMN_DESCRIPTION = 1;

        ShortcutMenuItemLabelProvider() {
        }

        public final void dispose() {
            for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            imageTable = null;
        }

        public Image getColumnImage(Object element, int index) {
            if (index != COLUMN_ID) {
				return null;
			}
            ImageDescriptor descriptor = null;
            if (element instanceof IPerspectiveDescriptor) {
                descriptor = ((IPerspectiveDescriptor) element)
                        .getImageDescriptor();
            } else if (element instanceof IViewDescriptor) {
                descriptor = ((IViewDescriptor) element).getImageDescriptor();
            } else if (element instanceof WorkbenchWizardElement) {
                descriptor = ((WorkbenchWizardElement) element)
                        .getImageDescriptor();
            }
            if (descriptor == null) {
				return null;
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

        public String getColumnText(Object element, int columnIndex) {
            String text = null;
            switch (columnIndex) {
            case COLUMN_ID:
                text = getText(element);
                break;
            case COLUMN_DESCRIPTION:
                if (element instanceof IPerspectiveDescriptor) {
                    text = ((IPerspectiveDescriptor) element).getDescription();
                } else if (element instanceof IViewDescriptor) {
                    text = ((IViewDescriptor) element).getDescription();
                } else if (element instanceof WorkbenchWizardElement) {
                    text = ((WorkbenchWizardElement) element).getDescription();
                }
                break;
            }
            if (text == null) {
				text = ""; //$NON-NLS-1$
			}
            return text;
        }

        public String getText(Object element) {
            String text = null;
            if (element instanceof IPerspectiveDescriptor) {
                text = ((IPerspectiveDescriptor) element).getLabel();
            } else if (element instanceof IViewDescriptor) {
                text = ((IViewDescriptor) element).getLabel();
            } else if (element instanceof WorkbenchWizardElement) {
                text = ((WorkbenchWizardElement) element).getLabel(element);
            }
            if (text == null) {
				text = ""; //$NON-NLS-1$
			}
            return text;
        }
    }

    class ShortcutMenu extends Object {
        /**
         * Tree representation for the shortcut items.  
         */
        private final static String ID_VIEW = "org.eclipse.ui.views"; //$NON-NLS-1$

        private final static String ID_WIZARD = "org.eclipse.ui.wizards"; //$NON-NLS-1$

        private final static String ID_PERSP = "org.eclipse.ui.perspectives"; //$NON-NLS-1$

        String id;

        String label;

        private ArrayList items = new ArrayList();

        private ArrayList checkedItems = new ArrayList();

        ArrayList children = new ArrayList();

        ShortcutMenu parent = null;

        ShortcutMenu(ShortcutMenu parent, String id, String label) {
            super();
            this.id = id;
            this.parent = parent;
            this.label = removeShortcut(label);
            this.label = DialogUtil.removeAccel(this.label);
            if (parent != null) {
				parent.children.add(this);
			}
        }

        void addItem(Object item) {
            items.add(item);
        }

        void addCheckedItem(Object item) {
            checkedItems.add(item);
        }

        public String toString() {
            return label;
        }

        ArrayList getCheckedItems() {
            return checkedItems;
        }

        ArrayList getCheckedItemIds() {
            ArrayList ids = new ArrayList();
            if (getMenuId() == ID_PERSP) {
                for (int i = 0; i < checkedItems.size(); i++) {
                    IPerspectiveDescriptor item = (IPerspectiveDescriptor) checkedItems
                            .get(i);
                    ids.add(item.getId());
                }
            } else if (getMenuId() == ID_VIEW) {
                for (int i = 0; i < checkedItems.size(); i++) {
                    IViewDescriptor item = (IViewDescriptor) checkedItems
                            .get(i);
                    ids.add(item.getId());
                }
            } else if (getMenuId() == ID_WIZARD) {
                for (int i = 0; i < checkedItems.size(); i++) {
                    WorkbenchWizardElement item = (WorkbenchWizardElement) checkedItems
                            .get(i);
                    ids.add(item.getId());
                }
            }
            for (int i = 0; i < children.size(); i++) {
                ShortcutMenu menu = (ShortcutMenu) children.get(i);
                ids.addAll(menu.getCheckedItemIds());
            }
            return ids;
        }

        ArrayList getChildren() {
            return children;
        }

        ArrayList getItems() {
            return items;
        }

        private String getMenuId() {
            if (parent == rootMenu) {
				return id;
			}

            return parent.getMenuId();
        }

        ArrayList getSubtreeItems() {
            ArrayList subtreeItems = new ArrayList();
            subtreeItems.add(this);
            for (int i = 0; i < children.size(); i++) {
                ShortcutMenu child = (ShortcutMenu) children.get(i);
                subtreeItems.addAll(child.getSubtreeItems());
            }
            return subtreeItems;
        }

        Object getItem(String menuItemId) {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String itemId = null;
                if (id == ID_PERSP) {
                    itemId = ((IPerspectiveDescriptor) item).getId();
                } else if (id == ID_VIEW) {
                    itemId = ((IViewDescriptor) item).getId();
                } else if (id == ID_WIZARD) {
                    itemId = ((WorkbenchWizardElement) item).getId();
                }
                if (menuItemId.equals(itemId)) {
					return item;
				}
            }
            return null;
        }

        boolean isFullyChecked() {
            if (getItems().size() != getCheckedItems().size()) {
				return false;
			}
            for (int i = 0; i < children.size(); i++) {
                ShortcutMenu child = (ShortcutMenu) children.get(i);
                if (!child.isFullyChecked()) {
					return false;
				}
            }
            return true;
        }

        boolean isFullyUnchecked() {
            if (getCheckedItems().size() != 0) {
				return false;
			}
            for (int i = 0; i < children.size(); i++) {
                ShortcutMenu child = (ShortcutMenu) children.get(i);
                if (!child.isFullyUnchecked()) {
					return false;
				}
            }
            return true;
        }

        void removeCheckedItem(Object item) {
            checkedItems.remove(item);
        }

        void checked(boolean checked) {
            checkedItems = new ArrayList();
            if (checked) {
                checkedItems.addAll(items);
            }
            for (int i = 0; i < children.size(); i++) {
                ShortcutMenu child = (ShortcutMenu) children.get(i);
                child.checked(checked);
            }
        }
    }

    class TreeContentProvider implements ITreeContentProvider {
        public void dispose() {
        }

        public Object[] getChildren(Object element) {
            if (element instanceof ActionSetDisplayItem) {
                ActionSetDisplayItem node = (ActionSetDisplayItem) element;
                return node.children.toArray();
            } else if (element instanceof ShortcutMenu) {
                ShortcutMenu node = (ShortcutMenu) element;
                return node.children.toArray();
            } else if (element instanceof ArrayList) {
            	return ((ArrayList) element).toArray();
            }
            return new Object[0];
        }

        public Object[] getElements(Object element) {
            return getChildren(element);
        }

        public Object getParent(Object element) {
            if (element instanceof ActionSetDisplayItem) {
                ActionSetDisplayItem node = (ActionSetDisplayItem) element;
                return node.parent;
            } else if (element instanceof ShortcutMenu) {
                ShortcutMenu node = (ShortcutMenu) element;
                return node.parent;
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof ActionSetDisplayItem) {
                ActionSetDisplayItem node = (ActionSetDisplayItem) element;
                return node.children.size() > 0;
            } else if (element instanceof ShortcutMenu) {
                ShortcutMenu node = (ShortcutMenu) element;
                return node.children.size() > 0;
            }
            return false;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    class ActionSetLabelProvider extends LabelProvider {
        private Map imageTable = new Hashtable();

        public void dispose() {
            for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            imageTable = null;
        }

        public Image getImage(Object element) {
            ActionSetDisplayItem item = (ActionSetDisplayItem) element;
            ImageDescriptor descriptor = item.imageDescriptor;
            if (descriptor == null) {
                if (item.type == ActionSetDisplayItem.MENUITEM) {
                    if (item.children.size() > 0) {
                        if (item.isTopLevelMenu()) {
							descriptor = menuImageDescriptor;
						} else {
							descriptor = submenuImageDescriptor;
						}
                    } else {
						return null;
					}
                } else if (item.type == ActionSetDisplayItem.TOOLITEM) {
                    if (item.children.size() > 0) {
						descriptor = toolbarImageDescriptor;
					} else {
						return null;
					}
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

        public String getText(Object element) {
            if (element instanceof ActionSetDisplayItem) {
                ActionSetDisplayItem item = (ActionSetDisplayItem) element;
                String text = item.getDisplayText();
                if ((item.type == ActionSetDisplayItem.MENUITEM)
                        && (item.children.size() > 0)) {
					text = text + "  >"; //$NON-NLS-1$ 
				}
                return text;
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Create an instance of this Dialog.
     * 
     * @param configurer the configurer
     * @param persp the perspective
     */
    public CustomizePerspectiveDialog(IWorkbenchWindowConfigurer configurer, Perspective persp) {
        super(configurer.getWindow().getShell());        
        this.configurer = configurer;
        perspective = persp;
        window = (WorkbenchWindow) configurer.getWindow();
        
        initializeActionSetInput();
        initializeShortcutMenuInput();
    }

    /**
     * Adds listeners for the Commands tab.
     * 
     * @since 3.0
     */
    private void addActionSetsListeners() {
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handleTabSelected(event);
            }
        });
        actionSetsViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        handleActionSetSelected(event);
                    }
                });
        actionSetsViewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                handleActionSetViewerKeyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        actionSetMenuViewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                handleActionSetMenuViewerKeyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        actionSetToolbarViewer.getControl().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                handleActionSetToolbarViewerKeyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * Adds listeners for the Shortcuts tab.
     * 
     * @since 3.0
     */
    private void addShortcutListeners() {
        menusCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }

            public void widgetSelected(SelectionEvent e) {
                handleMenuSelected();
            }
        });
        menusCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleMenuModified();
            }
        });
        menuCategoriesViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
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

    private PluginActionSet buildMenusAndToolbarsFor(CustomizeActionBars customizeActionBars, ActionSetDescriptor actionSetDesc) {
        String id = actionSetDesc.getId();
        ActionSetActionBars bars = new ActionSetActionBars(
        		customizeActionBars, window, customizeActionBars, id);
        PluginActionSetBuilder builder = new PluginActionSetBuilder();
        PluginActionSet actionSet = null;
        try {
            actionSet = (PluginActionSet) actionSetDesc.createActionSet();
            actionSet.init(null, bars);
        } catch (CoreException ex) {
            WorkbenchPlugin
                    .log("Unable to create action set " + actionSetDesc.getId(), ex); //$NON-NLS-1$
            return null;
        }
        builder.buildMenuAndToolBarStructure(actionSet, window);
        return actionSet;
    }

    private void checkInitialActionSetSelections() {
        // Check off the action sets that are active for the perspective.
        IActionSetDescriptor[] actionSetDescriptors = ((WorkbenchPage)window.getActivePage()).getActionSets();
        initiallyActive  = Arrays.asList(actionSetDescriptors);
        if (actionSets != null) {
            for (int i = 0; i < actionSetDescriptors.length; i++) {
				actionSetsViewer.setChecked(actionSetDescriptors[i], true);
			}
        }
    }

    private void checkInitialMenuCategorySelections(ShortcutMenu menu) {
        // Check off the shortcut categories that are active for the perspective.
        if (menu.children.size() == 0) {
			updateMenuCategoryCheckedState(menu);
		} else {
            for (int i = 0; i < menu.children.size(); i++) {
                ShortcutMenu child = (ShortcutMenu) menu.children.get(i);
                checkInitialMenuCategorySelections(child);
            }
        }
    }

    public boolean close() {
        if (showShortcutTab()) {
            lastSelectedMenuIndex = menusCombo.getSelectionIndex();
        }
        lastSelectedTab = tabFolder.getSelectionIndex();
        IStructuredSelection selection = (IStructuredSelection) actionSetsViewer
                .getSelection();
        if (selection.isEmpty()) {
			lastSelectedActionSetId = null;
		} else {
			lastSelectedActionSetId = ((ActionSetDescriptor) selection
                    .getFirstElement()).getId();
		}
        return super.close();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        String title=perspective.getDesc().getLabel();
        
        title = NLS.bind(WorkbenchMessages.ActionSetSelection_customize,title);
        shell.setText(title);
        window.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.ACTION_SET_SELECTION_DIALOG);
    }

    boolean containsActionSet(MenuManager mgr, String actionSetId) {
        // Return whether or not the given menuManager contains items for the
        // given actionSetId.
        IContributionItem[] menuItems = mgr.getItems();
        for (int j = 0; j < menuItems.length; j++) {
            IContributionItem menuItem = menuItems[j];
            if (menuItem instanceof ActionSetContributionItem) {
                ActionSetContributionItem actionSetItem = (ActionSetContributionItem) menuItem;
                if (actionSetItem.getActionSetId().equals(actionSetId)) {
                    return true;
                }
            } else if (menuItem instanceof MenuManager) {
                MenuManager childMgr = (MenuManager) menuItem;
                boolean found = containsActionSet(childMgr, actionSetId);
                if (found) {
					return true;
				}
            }
        }
        return false;
    }

    private Composite createActionSetsPage(Composite parent) {
    	GridData data;
    	
        Composite actionSetsComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        actionSetsComposite.setLayout(layout);

        // Select... label
        Label label = new Label(actionSetsComposite, SWT.WRAP);
        label
                .setText(NLS.bind(WorkbenchMessages.ActionSetSelection_selectActionSetsLabel,perspective.getDesc().getLabel() ));
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        label.setLayoutData(data);

        Label sep = new Label(actionSetsComposite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        SashForm sashComposite = new SashForm(actionSetsComposite,
                SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashComposite.setLayoutData(data);

        // Action Set List Composite
        Composite actionSetGroup = new Composite(sashComposite, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        actionSetGroup.setLayout(layout);
        actionSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        label = new Label(actionSetGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_availableActionSets);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        actionSetsViewer = CheckboxTableViewer.newCheckList(actionSetGroup,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        actionSetsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        actionSetsViewer.setLabelProvider(new WorkbenchLabelProvider());
        actionSetsViewer.setContentProvider(new ArrayContentProvider());
        actionSetsViewer.setComparator(new ActionSetComparator());
        actionSetsViewerTooltip = new DefaultToolTip(actionSetsViewer.getControl(),ToolTip.RECREATE,true){

			public Point getLocation(Point tipSize, Event event) {
				return getShell().getDisplay().getCursorLocation();
			}
        	
        };

        // Menu and toolbar composite
        Composite actionGroup = new Composite(sashComposite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        actionGroup.setLayout(layout);
        actionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite menubarGroup = new Composite(actionGroup, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        menubarGroup.setLayout(layout);
        menubarGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        label = new Label(menubarGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_menubarActions); 
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        actionSetMenuViewer = new TreeViewer(menubarGroup);
        actionSetMenuViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        actionSetMenuViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        actionSetMenuViewer.setLabelProvider(new ActionSetLabelProvider());
        actionSetMenuViewer.setContentProvider(new TreeContentProvider());
        actionSetMenuViewerTooltip = new DefaultToolTip(actionSetMenuViewer.getControl(),ToolTip.RECREATE, true) {

			public Point getLocation(Point tipSize, Event event) {
				return getShell().getDisplay().getCursorLocation();
			}
        	
        };

        Composite toolbarGroup = new Composite(actionGroup, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        toolbarGroup.setLayout(layout);
        toolbarGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        label = new Label(toolbarGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_toolbarActions);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        actionSetToolbarViewer = new TreeViewer(toolbarGroup);
        actionSetToolbarViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        actionSetToolbarViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        actionSetToolbarViewer
                .setLabelProvider(new ActionSetLabelProvider());
        actionSetToolbarViewer.setContentProvider(new TreeContentProvider());
        actionSetToolbarViewerTooltip = new DefaultToolTip(actionSetToolbarViewer.getControl(),ToolTip.RECREATE,true){

			public Point getLocation(Point tipSize, Event event) {
				return getShell().getDisplay().getCursorLocation();
			}
        	
        };

        sashComposite.setWeights(new int[] { 30, 70 });

        // Use F2... label
        label = new Label(actionSetsComposite, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_selectActionSetsHelp);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        return actionSetsComposite;
    }

    /**
     * Returns whether the shortcut tab should be shown.
     * 
     * @return <code>true</code> if the shortcut tab should be shown,
     * and <code>false</code> otherwise
     * @since 3.0
     */
    private boolean showShortcutTab() {
        return window.containsSubmenu(WorkbenchWindow.NEW_WIZARD_SUBMENU)
                || window
                        .containsSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU)
                || window.containsSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        // tab folder
        tabFolder = new TabFolder(composite, SWT.NONE);
        
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
        gd.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);
        tabFolder.setLayoutData(gd);

        // Shortcuts tab
        if (showShortcutTab()) {
            TabItem item1 = new TabItem(tabFolder, SWT.NONE);
            item1.setText(WorkbenchMessages.ActionSetSelection_menuTab);
            item1.setControl(createMenusPage(tabFolder));
            addShortcutListeners();
            ArrayList children = rootMenu.getChildren();
            String[] itemNames = new String[children.size()];
            for (int i = 0; i < children.size(); i++) {
                itemNames[i] = ((ShortcutMenu) children.get(i)).label;
            }
            menusCombo.setItems(itemNames);
        }

        // Commands tab
        TabItem item = new TabItem(tabFolder, SWT.NONE);
        item.setText(WorkbenchMessages.ActionSetSelection_actionSetsTab); 
        item.setControl(createActionSetsPage(tabFolder));
        applyDialogFont(tabFolder);
        addActionSetsListeners();
        actionSetsViewer.setInput(actionSets);
        checkInitialActionSetSelections();

        // now that both tabs are set up, initialize selections
        setInitialSelections();

        return composite;
    }

    private Composite createMenusPage(Composite parent) {
    	GridData data;

        Composite menusComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        menusComposite.setLayout(layout);

        // Select... label
        Label label = new Label(menusComposite, SWT.WRAP);
        label.setText(NLS.bind(WorkbenchMessages.ActionSetSelection_selectMenusLabel,  perspective.getDesc().getLabel() )); 
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        label.setLayoutData(data);

        Label sep = new Label(menusComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
        sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        SashForm sashComposite = new SashForm(menusComposite, SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashComposite.setLayoutData(data);

        // Menus List
        Composite menusGroup = new Composite(sashComposite, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        menusGroup.setLayout(layout);
        menusGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        label = new Label(menusGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_availableMenus); 
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        menusCombo = new Combo(menusGroup, SWT.READ_ONLY);
        menusCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Categories Tree
        label = new Label(menusGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_availableCategories);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        menuCategoriesViewer = new CheckboxTreeViewer(menusGroup);
        menuCategoriesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        menuCategoriesViewer.setLabelProvider(new LabelProvider());
        menuCategoriesViewer.setContentProvider(new TreeContentProvider());
        menuCategoriesViewer.setComparator(new WorkbenchViewerComparator());

        // Menu items list
        Composite menuItemsGroup = new Composite(sashComposite, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        menuItemsGroup.setLayout(layout);
        menuItemsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        label = new Label(menuItemsGroup, SWT.WRAP);
        label.setText(WorkbenchMessages.ActionSetSelection_menuItems);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        menuItemsViewer = CheckboxTableViewer.newCheckList(menuItemsGroup,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        Table menuTable = menuItemsViewer.getTable();
        menuTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        menuItemsViewer.setLabelProvider(new ShortcutMenuItemLabelProvider());
        menuItemsViewer
                .setContentProvider(new ShortcutMenuItemContentProvider());
        menuItemsViewer.setComparator(new WorkbenchViewerComparator());

        menuTable.setHeaderVisible(true);
        int[] columnWidths = new int[shortcutMenuColumnWidths.length];
        for (int i = 0; i < shortcutMenuColumnWidths.length; i++) {
            columnWidths[i] = convertHorizontalDLUsToPixels(shortcutMenuColumnWidths[i]);
        }
        for (int i = 0; i < shortcutMenuColumnHeaders.length; i++) {
            TableColumn tc = new TableColumn(menuTable, SWT.NONE, i);
            tc.setResizable(true);
            tc.setText(shortcutMenuColumnHeaders[i]);
            tc.setWidth(columnWidths[i]);
        }
        sashComposite.setWeights(new int[] { 30, 70 });
        return menusComposite;
    }

    void handleActionSetMenuViewerKeyPressed(KeyEvent event) {
        // popup the description for the selected action set menu item
        if (event.keyCode == SWT.F2 && event.stateMask == 0) {
            IStructuredSelection sel = (IStructuredSelection) actionSetMenuViewer
                    .getSelection();
            ActionSetDisplayItem element = (ActionSetDisplayItem) sel
                    .getFirstElement();
            if (element != null) {
                String desc = element.description;
                if (desc == null || desc.equals("")) { //$NON-NLS-1$
                    desc = WorkbenchMessages.ActionSetSelection_noDesc;
                }
                actionSetMenuViewerTooltip.setText(desc);
                // Passing 0,0 because tooltip is always opened on current cursor position
                actionSetMenuViewerTooltip.show(new Point(0,0));
            }
        }
    }

    void handleActionSetSelected(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        ActionSetDescriptor element = (ActionSetDescriptor) sel
                .getFirstElement();
        if (element == selectedActionSet) {
			return;
		}
        String actionSetId = element.getId();
        // Hash table is used to cache previous selections
        ArrayList structures = (ArrayList) actionSetStructures.get(actionSetId);
        ActionSetDisplayItem menubarStructure = null;
        ActionSetDisplayItem toolbarStructure = null;
        // If the actionset has never been selected then we need to populate the structures
        if (structures == null) {
            structures = new ArrayList(2);
            menubarStructure = new ActionSetDisplayItem("Menubar"); //$NON-NLS-1$
            toolbarStructure = new ActionSetDisplayItem("Toolbar"); //$NON-NLS-1$
            
            // Build the menus and toolbars for it using our fake action bars.
        	// Can't reuse customizeActionBars between action sets due to action set menu reuse.  See bug 94827 for details.
        	CustomizeActionBars customizeActionBars = new CustomizeActionBars(configurer);
            // Fill current actionBars in the "fake" workbench action bars
            window.fillActionBars(customizeActionBars,
                    ActionBarAdvisor.FILL_PROXY | ActionBarAdvisor.FILL_MENU_BAR
                            | ActionBarAdvisor.FILL_COOL_BAR);
            // Populate the action bars with the action set
            PluginActionSet actionSet = buildMenusAndToolbarsFor(customizeActionBars, element);
            // Build the representation to show
            menubarStructure.fillMenusFor(actionSetId,
            		customizeActionBars.menuManager);
            toolbarStructure.fillToolsFor(actionSetId,
            		customizeActionBars.coolBarManager);
            // Be sure to dispose the custom bars or we'll leak
            if (actionSet!=null) {
            	actionSet.dispose();
            }
            customizeActionBars.dispose();

            // Add menubarStructure and toolbarStructure to arrayList
            structures.add(menubarStructure);
            structures.add(toolbarStructure);
            // Add the structure to the hash table with key actionSetId
            actionSetStructures.put(actionSetId, structures);
        }
        // retrieve the actionsets from the arraylist
        if (menubarStructure == null) {
			menubarStructure = (ActionSetDisplayItem) structures.get(0);
		}
        if (toolbarStructure == null) {
			toolbarStructure = (ActionSetDisplayItem) structures.get(1);
		}

        // fill the menu structure table
        actionSetMenuViewer.setInput(menubarStructure);
        if (menubarStructure.children.size() > 0) {
            actionSetMenuViewer
                    .reveal(menubarStructure.children.get(0));
        }

        // fill the toolbar structure table
        actionSetToolbarViewer.setInput(toolbarStructure);
        if (toolbarStructure.children.size() > 0) {
            actionSetToolbarViewer.reveal(toolbarStructure.children
                    .get(0));
        }
        selectedActionSet = element;
    }

    void handleActionSetToolbarViewerKeyPressed(KeyEvent event) {
        // popup the description for the selected action set toolbar item
        if (event.keyCode == SWT.F2 && event.stateMask == 0) {
            IStructuredSelection sel = (IStructuredSelection) actionSetToolbarViewer
                    .getSelection();
            ActionSetDisplayItem element = (ActionSetDisplayItem) sel
                    .getFirstElement();
            if (element != null) {
                String desc = element.description;
                if (desc == null || desc.equals("")) { //$NON-NLS-1$
                    desc = WorkbenchMessages.ActionSetSelection_noDesc; 
                }
                actionSetToolbarViewerTooltip.setText(desc);
                // Passing 0,0 because tooltip is always opened on current cursor position
                actionSetToolbarViewerTooltip.show(new Point(0,0));
            }
        }
    }

    void handleActionSetViewerKeyPressed(KeyEvent event) {
        // popup the description for the selected action set
        if (event.keyCode == SWT.F2 && event.stateMask == 0) {
            IStructuredSelection sel = (IStructuredSelection) actionSetsViewer
                    .getSelection();
            ActionSetDescriptor element = (ActionSetDescriptor) sel
                    .getFirstElement();
            if (element != null) {
                String desc = element.getDescription();
                if (desc == null || desc.equals("")) { //$NON-NLS-1$
                    desc = WorkbenchMessages.ActionSetSelection_noDesc; 
                }
                actionSetsViewerTooltip.setText(desc);
                // Passing 0,0 because tooltip is always opened on current cursor position
                actionSetsViewerTooltip.show(new Point(0,0));
            }
        }
    }

    void handleMenuCategoryChecked(CheckStateChangedEvent event) {
        ShortcutMenu checkedCategory = (ShortcutMenu) event.getElement();
        boolean checked = event.getChecked();
        checkedCategory.checked(checked);
        // check/uncheck the element's category subtree
        menuCategoriesViewer.setSubtreeChecked(checkedCategory, checked);
        // set gray state of the element's category subtree, all items should
        // not be grayed
        ArrayList subtree = checkedCategory.getSubtreeItems();
        ShortcutMenu menuItemInput = (ShortcutMenu) menuItemsViewer.getInput();
        for (int i = 0; i < subtree.size(); i++) {
            Object child = subtree.get(i);
            menuCategoriesViewer.setGrayed(child, false);
            if (child == menuItemInput) {
				menuItemsViewer.setAllChecked(checked);
			}
        }
        menuCategoriesViewer.setGrayed(checkedCategory, false);
        updateMenuCategoryCheckedState(checkedCategory.parent);
    }

    void handleMenuCategorySelected(SelectionChangedEvent event) {
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        ShortcutMenu element = (ShortcutMenu) sel.getFirstElement();
        if (element == selectedMenuCategory) {
			return;
		}
        if (element != menuItemsViewer.getInput()) {
            menuItemsViewer.setInput(element);
        }
        if (element != null) {
            menuItemsViewer.setCheckedElements(element.getCheckedItems()
                    .toArray());
        }
    }

    void handleMenuItemChecked(CheckStateChangedEvent event) {
        ShortcutMenu selectedMenu = (ShortcutMenu) menuItemsViewer.getInput();
        boolean itemChecked = event.getChecked();
        Object item = event.getElement();
        if (itemChecked) {
            selectedMenu.addCheckedItem(item);
        } else {
            selectedMenu.removeCheckedItem(item);
        }
        updateMenuCategoryCheckedState(selectedMenu);
    }

    void handleMenuModified() {
        String text = menusCombo.getText();
        String[] items = menusCombo.getItems();
        int itemIndex = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(text)) {
                itemIndex = i;
                break;
            }
        }
        if (itemIndex == -1) {
			return;
		}
        ShortcutMenu element = (ShortcutMenu) rootMenu.children.get(itemIndex);
        handleMenuSelected(element);
    }

    void handleMenuSelected() {
        int i = menusCombo.getSelectionIndex();
        ShortcutMenu element = (ShortcutMenu) rootMenu.children.get(i);
        handleMenuSelected(element);
    }

    void handleMenuSelected(ShortcutMenu element) {
        if (element != menuCategoriesViewer.getInput()) {
            menuCategoriesViewer.setInput(element);
            menuCategoriesViewer.expandAll();
            if (element != null) {
                if (element.getChildren().size() > 0) {
                    StructuredSelection sel = new StructuredSelection(element
                            .getChildren().get(0));
                    menuCategoriesViewer.setSelection(sel, true);
                } else {
                    menuItemsViewer.setInput(element);
                    menuItemsViewer.setCheckedElements(element
                            .getCheckedItems().toArray());
                }
            } else {
                menuCategoriesViewer.setInput(element);
                menuItemsViewer.setInput(element);
            }
            checkInitialMenuCategorySelections(rootMenu);
        }
    }

    void handleTabSelected(SelectionEvent event) {
        TabItem item = (TabItem) event.item;
        Control control = item.getControl();
        if (control != null) {
			control.setFocus();
		}
    }

    private void initializeActionSetInput() {
        // Just get the action sets at this point.  Do not load the action set until it
        // is actually selected in the dialog.
        ActionSetRegistry reg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        IActionSetDescriptor[] sets = reg.getActionSets();
        for (int i = 0; i < sets.length; i++) {
            ActionSetDescriptor actionSetDesc = (ActionSetDescriptor) sets[i];
            if (WorkbenchActivityHelper.filterItem(actionSetDesc)) {
				continue;
			}
            actionSets.add(actionSetDesc);
        }
        String iconPath = "$nl$/icons/full/obj16/menu.gif";//$NON-NLS-1$
        URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
        menuImageDescriptor = ImageDescriptor.createFromURL(url);

        iconPath = "$nl$/icons/full/obj16/submenu.gif";//$NON-NLS-1$
        url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
        submenuImageDescriptor = ImageDescriptor.createFromURL(url);

        iconPath = "$nl$/icons/full/obj16/toolbar.gif";//$NON-NLS-1$
        url = BundleUtility.find(PlatformUI.PLUGIN_ID, iconPath);
        toolbarImageDescriptor = ImageDescriptor.createFromURL(url);
    }

    private void initializeShortCutMenu(ShortcutMenu menu,
    		IWizardCategory  element, List activeIds) {
        ShortcutMenu category = new ShortcutMenu(menu, element.getId(), element
                .getLabel());
        Object[] wizards = element.getWizards();
        for (int i = 0; i < wizards.length; i++) {
            WorkbenchWizardElement wizard = (WorkbenchWizardElement) wizards[i];
            category.addItem(wizard);
            if (activeIds.contains(wizard.getId())) {
				category.addCheckedItem(wizard);
			}
        }
        // @issue should not pass in null
        IWizardCategory [] children = element.getCategories();
        for (int i = 0; i < children.length; i++) {
            initializeShortCutMenu(category, children[i], activeIds);
        }
    }

    private void initializeShortcutMenuInput() {
        rootMenu = new ShortcutMenu(null, "Root", ""); //$NON-NLS-1$ //$NON-NLS-2$
        List activeIds;

        if (window.containsSubmenu(WorkbenchWindow.NEW_WIZARD_SUBMENU)) {
            ShortcutMenu wizardMenu = new ShortcutMenu(rootMenu,
                    ShortcutMenu.ID_WIZARD, WorkbenchMessages.ActionSetDialogInput_wizardCategory); 
            
            IWizardCategory wizardCollection = WorkbenchPlugin.getDefault().getNewWizardRegistry().getRootCategory();

            IWizardCategory [] wizardCategories = wizardCollection.getCategories();
            activeIds = Arrays.asList(perspective.getNewWizardShortcuts());
            for (int i = 0; i < wizardCategories.length; i++) {
                IWizardCategory element = wizardCategories[i];
                if (WorkbenchActivityHelper.filterItem(element)) {
					continue;
				}
                initializeShortCutMenu(wizardMenu, element, activeIds);
            }
        }

        if (window.containsSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU)) {
            ShortcutMenu perspMenu = new ShortcutMenu(
                    rootMenu,
                    ShortcutMenu.ID_PERSP,
                    WorkbenchMessages.ActionSetDialogInput_perspectiveCategory);
            IPerspectiveRegistry perspReg = WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry();
            IPerspectiveDescriptor[] persps = perspReg.getPerspectives();
            for (int i = 0; i < persps.length; i++) {
                if (WorkbenchActivityHelper.filterItem(persps[i])) {
					continue;
				}
                perspMenu.addItem(persps[i]);
            }
            activeIds = Arrays.asList(perspective.getPerspectiveShortcuts());
            for (int i = 0; i < activeIds.size(); i++) {
                String id = (String) activeIds.get(i);
                Object item = perspMenu.getItem(id);
                if (item != null) {
					perspMenu.addCheckedItem(item);
				}
            }
        }

        if (window.containsSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU)) {
            ShortcutMenu viewMenu = new ShortcutMenu(rootMenu,
                    ShortcutMenu.ID_VIEW, WorkbenchMessages.ActionSetDialogInput_viewCategory); 
            IViewRegistry viewReg = WorkbenchPlugin.getDefault()
                    .getViewRegistry();
            IViewCategory [] categories = viewReg.getCategories();
            activeIds = Arrays.asList(perspective.getShowViewShortcuts());
            for (int i = 0; i < categories.length; i++) {
                IViewCategory category = categories[i];
                if (WorkbenchActivityHelper.filterItem(category)) {
					continue;
				}
                ShortcutMenu viewCategory = new ShortcutMenu(viewMenu, category
                        .getId(), category.getLabel());
                IViewDescriptor [] views = category.getViews();
                if (views != null) {
                    for (int j = 0; j < views.length; j++) {
                        IViewDescriptor view = views[j];
                        if (view.getId().equals(IIntroConstants.INTRO_VIEW_ID)) {
							continue;
						}
                        if (WorkbenchActivityHelper.filterItem(view)) {
							continue;
						}
                        viewCategory.addItem(view);
                        if (activeIds.contains(view.getId())) {
							viewCategory.addCheckedItem(view);
						}
                    }
                }
            }
        }
    }

    protected void okPressed() {
        if (showShortcutTab()) {
            ArrayList menus = rootMenu.children;
            for (int i = 0; i < menus.size(); i++) {
                ShortcutMenu menu = (ShortcutMenu) menus.get(i);
                if (ShortcutMenu.ID_VIEW.equals(menu.id)) {
                    perspective.setShowViewActionIds(menu.getCheckedItemIds());
                } else if (ShortcutMenu.ID_PERSP.equals(menu.id)) {
                    perspective.setPerspectiveActionIds(menu
                            .getCheckedItemIds());
                } else if (ShortcutMenu.ID_WIZARD.equals(menu.id)) {
                    perspective.setNewWizardActionIds(menu.getCheckedItemIds());
                }
            }
        }

        ArrayList toAdd = new ArrayList();
        ArrayList toRemove = new ArrayList();
        toRemove.addAll(initiallyActive);
        
        Object[] selected = actionSetsViewer.getCheckedElements();
        for (int i = 0; i < selected.length; i++) {
            Object obj = selected[i];
            toRemove.remove(obj);
            if (!initiallyActive.contains(toRemove)) {
                toAdd.add(obj);
            }
        }
        
        perspective.turnOnActionSets((IActionSetDescriptor[]) toAdd.toArray(new IActionSetDescriptor[toAdd.size()]));
        perspective.turnOffActionSets((IActionSetDescriptor[]) toRemove.toArray(new IActionSetDescriptor[toAdd.size()]));

        super.okPressed();
    }
    
    String removeShortcut(String label) {
        if (label == null) {
			return label;
		}
        int end = label.lastIndexOf('@');
        if (end >= 0) {
			label = label.substring(0, end);
		}
        return label;
    }

    private void setInitialSelections() {
        Object item = actionSetsViewer.getElementAt(0);
        if (lastSelectedActionSetId != null) {
            for (int i = 0; i < actionSets.size(); i++) {
                ActionSetDescriptor actionSet = (ActionSetDescriptor) actionSets
                        .get(i);
                if (actionSet.getId().equals(lastSelectedActionSetId)) {
                    item = actionSet;
                    break;
                }
            }
        }
        if (item != null) {
        	StructuredSelection sel = new StructuredSelection(item);
        	actionSetsViewer.setSelection(sel, true);
        }

        if (showShortcutTab()) {
            menusCombo.select(lastSelectedMenuIndex);
        }

        if (lastSelectedTab != -1) {
            tabFolder.setSelection(lastSelectedTab);
        }

        if ((tabFolder.getSelectionIndex() == 0) && showShortcutTab()) {
            menusCombo.setFocus();
        } else {
            actionSetsViewer.getControl().setFocus();
        }
    }

    private void updateMenuCategoryCheckedState(ShortcutMenu menu) {
        if (menu == rootMenu) {
			return;
		}
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#applyDialogFont()
     */
    protected boolean applyDialogFont() {
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
