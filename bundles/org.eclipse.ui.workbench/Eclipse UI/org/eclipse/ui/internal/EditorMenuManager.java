/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.expressions.LegacyEditorContributionExpression;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.menus.CommandContributionItem;

/**
 * An <code>EditorMenuManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class EditorMenuManager extends SubMenuManager {
    private ArrayList wrappers;

    private boolean enabledAllowed = true;

    private class Overrides implements IContributionManagerOverrides {
        /**
         * Indicates that the items of this manager are allowed to enable;
         * <code>true</code> by default.
         */
        public void updateEnabledAllowed() {
            // update the items in the map
            IContributionItem[] items = EditorMenuManager.super.getItems();
            for (int i = 0; i < items.length; i++) {
                IContributionItem item = items[i];
                item.update(IContributionManagerOverrides.P_ENABLED);
            }
            // update the wrapped menus
            if (wrappers != null) {
                for (int i = 0; i < wrappers.size(); i++) {
                    EditorMenuManager manager = (EditorMenuManager) wrappers
                            .get(i);
                    manager.setEnabledAllowed(enabledAllowed);
                }
            }
        }

        public Boolean getEnabled(IContributionItem item) {
            if (((item instanceof ActionContributionItem) && (((ActionContributionItem) item)
                    .getAction() instanceof RetargetAction))
                    || enabledAllowed) {
				return null;
			} else {
				return Boolean.FALSE;
			}
        }

        public Integer getAccelerator(IContributionItem item) {
            if (getEnabled(item) == null) {
				return getParentMenuManager().getOverrides().getAccelerator(
                        item);
			} else {
				// no acclerator if the item is disabled
                return new Integer(0);
			}
        }

        public String getAcceleratorText(IContributionItem item) {
            return getParentMenuManager().getOverrides().getAcceleratorText(
                    item);
        }

        public String getText(IContributionItem item) {
            return getParentMenuManager().getOverrides().getText(item);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IContributionManagerOverrides#getVisible(org.eclipse.jface.action.IContributionItem)
         */
        public Boolean getVisible(IContributionItem item) {
        	return getParentMenuManager().getOverrides().getVisible(item);
        }
    }

	private Expression legacyActionBarExpression;

	private Expression getExpression() {
		if (legacyActionBarExpression == null) {
			legacyActionBarExpression = new LegacyEditorContributionExpression(editorId,
					workbenchWindow);
		}
		return legacyActionBarExpression;
	}

	private MExpression createExpression() {
		MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		exp.setCoreExpressionId("programmatic." + editorId); //$NON-NLS-1$
		exp.setCoreExpression(getExpression());
		return exp;
	}

    private Overrides overrides = new Overrides();

	private MApplication application;

	private IWorkbenchWindow workbenchWindow;

	private String editorId;

	private static HashMap<IWorkbenchWindow, Map<String, ArrayList<MMenuContribution>>> menuContributions = new HashMap<IWorkbenchWindow, Map<String, ArrayList<MMenuContribution>>>();
	private static HashMap<String, ArrayList<MenuManager>> managersToProcess = new HashMap<String, ArrayList<MenuManager>>();

    /**
     * Constructs a new editor manager.
     */
    public EditorMenuManager(MApplication application, IWorkbenchWindow workbenchWindow,
			String editorId, IMenuManager mgr) {
        super(mgr);
		this.application = application;
		this.workbenchWindow = workbenchWindow;
		this.editorId = editorId;
    }

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     */
    public IContributionItem[] getItems() {
        return getParentMenuManager().getItems();
    }

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     */
    public IContributionManagerOverrides getOverrides() {
        return overrides;
    }

	private void add(MMenuContribution mc, IContributionItem item) {
		if (item instanceof MenuManager) {
			MenuManager menuManager = (MenuManager) item;
			addManagerToProcess(menuManager);
			MMenu subMenu = MenuHelper.createMenu(menuManager);
			if (subMenu != null) {
				mc.getChildren().add(subMenu);
			}
		} else if (item instanceof CommandContributionItem) {
			CommandContributionItem cci = (CommandContributionItem) item;
			MMenuItem menuItem = MenuHelper.createItem(application, cci);
			if (menuItem != null) {
				mc.getChildren().add(menuItem);
			}
		} else if (item instanceof ActionContributionItem) {
			MMenuItem menuItem = MenuHelper.createItem(application, (ActionContributionItem) item);
			if (menuItem != null) {
				mc.getChildren().add(menuItem);
			}
		} else if (item instanceof AbstractGroupMarker) {
			MMenuSeparator separator = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
			separator.setToBeRendered(item.isVisible());
			separator.setElementId(item.getId());
			mc.getChildren().add(separator);
		} else if (!(item instanceof SubContributionItem) && !(item instanceof SubMenuManager)) {
			MRenderedMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createRenderedMenuItem();
			menuItem.setContributionItem(item);
			mc.getChildren().add(menuItem);
		}
	}

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     * Inserts the new item after any action set contributions which may
     * exist within the toolbar to ensure a consistent order for actions.
     */
	public void insertAfter(String id, IContributionItem item) {
		MMenuContribution mc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mc.setParentId(getId());
		if (id != null && !id.equals(item.getId())) {
			mc.setPositionInParent("after=" + id); //$NON-NLS-1$
		}
		mc.setVisibleWhen(createExpression());
		add(mc, item);
		addMenuContribution(mc, true);
		application.getMenuContributions().add(mc);
		super.insertAfter(id, item);
	}

    /* (non-Javadoc)
     * Method declared on IContributionManager.
     * Inserts the new item after any action set contributions which may
     * exist within the toolbar to ensure a consistent order for actions.
     */
    public void prependToGroup(String groupName, IContributionItem item) {
        insertAfter(groupName, item);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.SubContributionManager#insertBefore(java.lang
	 * .String, org.eclipse.jface.action.IContributionItem)
	 */
	@Override
	public void insertBefore(String id, IContributionItem item) {
		MMenuContribution mc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mc.setParentId(getId());
		mc.setVisibleWhen(createExpression());
		if (id != null && !id.equals(item.getId())) {
			mc.setPositionInParent("before=" + id); //$NON-NLS-1$
		}
		add(mc, item);
		addMenuContribution(mc, false);
		application.getMenuContributions().add(mc);
		super.insertBefore(id, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.SubContributionManager#add(org.eclipse.jface
	 * .action.IContributionItem)
	 */
	@Override
	public void add(IContributionItem item) {
		String pos = "after=additions"; //$NON-NLS-1$
		IContributionItem[] items = getItems();
		if (items.length > 0) {
			for (int i = items.length; i > 0; i--) {
				IContributionItem localItem = items[i - 1];
				if (localItem.getId() != null && !localItem.getId().equals(item.getId())) {
					pos = "after=" + localItem.getId(); //$NON-NLS-1$
					break;
				}
			}
		}
		MMenuContribution mc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mc.setParentId(getId());
		mc.setPositionInParent(pos);
		mc.setVisibleWhen(createExpression());
		add(mc, item);
		addMenuContribution(mc, false);
		application.getMenuContributions().add(mc);
		super.add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.SubContributionManager#appendToGroup(java.lang
	 * .String, org.eclipse.jface.action.IContributionItem)
	 */
	@Override
	public void appendToGroup(String groupName, IContributionItem item) {
		String pos = "after=" + groupName; //$NON-NLS-1$
		IContributionItem[] items = getItems();
		boolean nextGroup = false;
		for (int i = 0; i < items.length; i++) {
			IContributionItem localItem = items[i];
			if (groupName.equals(localItem.getId())) {
				nextGroup = true;
			}
			if ((localItem.isGroupMarker() || localItem.isSeparator()) && nextGroup
					&& localItem.getId() != null && !localItem.getId().equals(item.getId())) {
				pos = "before=" + localItem.getId(); //$NON-NLS-1$
				break;
			}
		}
		MMenuContribution mc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mc.setParentId(getId());
		mc.setPositionInParent(pos);
		mc.setVisibleWhen(createExpression());
		add(mc, item);
		addMenuContribution(mc, false);
		application.getMenuContributions().add(mc);
		super.appendToGroup(groupName, item);
	}

	/**
	 * @param mc
	 */
	private void addMenuContribution(MMenuContribution mc, boolean insert) {
		Map<String, ArrayList<MMenuContribution>> map = menuContributions.get(workbenchWindow);
		if (map == null) {
			map = new HashMap<String, ArrayList<MMenuContribution>>();
			menuContributions.put(workbenchWindow, map);
		}
		ArrayList<MMenuContribution> contributions = map.get(editorId);
		if (contributions == null) {
			contributions = new ArrayList<MMenuContribution>();
			map.put(editorId, contributions);
		}
		if (insert) {
			contributions.add(0, mc);
		} else {
			contributions.add(mc);
		}
	}

	private void addManagerToProcess(MenuManager manager) {
		ArrayList<MenuManager> contributions = managersToProcess.get(editorId);
		if (contributions == null) {
			contributions = new ArrayList<MenuManager>();
			managersToProcess.put(editorId, contributions);
		}
		contributions.add(manager);
	}

    /**
     * Sets the visibility of the manager. If the visibility is <code>true</code>
     * then each item within the manager appears within the parent manager.
     * Otherwise, the items are not visible.
     * <p>
     * If force visibility is <code>true</code>, or grayed out if force visibility is <code>false</code>
     * <p>
     * This is a workaround for the layout flashing when editors contribute
     * large amounts of items.</p>
     *
     * @param visible the new visibility
     * @param forceVisibility whether to change the visibility or just the
     * 		enablement state.
     */
    public void setVisible(boolean visible, boolean forceVisibility) {
        if (visible) {
            if (forceVisibility) {
                // Make the items visible 
                if (!enabledAllowed) {
					setEnabledAllowed(true);
				}
            } else {
                if (enabledAllowed) {
					setEnabledAllowed(false);
				}
            }
            if (!isVisible()) {
				setVisible(true);
			}
        } else {
            if (forceVisibility) {
				// Remove the editor menu items
                setVisible(false);
			} else {
				// Disable the editor menu items.
                setEnabledAllowed(false);
			}
        }
    }

    /**
     * Sets the enablement ability of all the items contributed by the editor.
     *
     * @param enabledAllowed <code>true</code> if the items may enable
     * @since 2.0
     */
    public void setEnabledAllowed(boolean enabledAllowed) {
        if (this.enabledAllowed == enabledAllowed) {
			return;
		}
        this.enabledAllowed = enabledAllowed;
        overrides.updateEnabledAllowed();
    }

    /* (non-Javadoc)
     * Method declared on SubMenuManager.
     */
    protected SubMenuManager wrapMenu(IMenuManager menu) {
        if (wrappers == null) {
			wrappers = new ArrayList();
		}
        EditorMenuManager manager = new EditorMenuManager(application, workbenchWindow, editorId,
				menu);
        wrappers.add(manager);
        return manager;
    }

    protected IAction[] getAllContributedActions() {
        HashSet set = new HashSet();
        getAllContributedActions(set);
        return (IAction[]) set.toArray(new IAction[set.size()]);
    }

    protected void getAllContributedActions(HashSet set) {
        IContributionItem[] items = super.getItems();
        for (int i = 0; i < items.length; i++) {
			getAllContributedActions(set, items[i]);
		}
        if (wrappers == null) {
			return;
		}
        for (Iterator iter = wrappers.iterator(); iter.hasNext();) {
            EditorMenuManager element = (EditorMenuManager) iter.next();
            element.getAllContributedActions(set);
        }
    }

    protected void getAllContributedActions(HashSet set, IContributionItem item) {
        if (item instanceof MenuManager) {
            IContributionItem subItems[] = ((MenuManager) item).getItems();
            for (int j = 0; j < subItems.length; j++) {
				getAllContributedActions(set, subItems[j]);
			}
        } else if (item instanceof ActionContributionItem) {
            set.add(((ActionContributionItem) item).getAction());
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.SubMenuManager#disposeManager()
	 */
	@Override
	public void disposeManager() {
		Map<String, ArrayList<MMenuContribution>> map = menuContributions.get(workbenchWindow);
		if (map != null) {
			ArrayList<MMenuContribution> contributions = map.remove(editorId);
			if (contributions != null) {
				application.getMenuContributions().removeAll(contributions);
			}
			
			if (map.isEmpty()) {
				menuContributions.remove(workbenchWindow);
			}
		}
		super.disposeManager();
	}

	/**
	 * Process the MenuManagers that were contributed through the
	 * EditorMenuManagers for this type of editor.
	 */
	public void processMenuManagers() {
		ArrayList<MenuManager> contributions = managersToProcess.remove(editorId);
		if (contributions == null) {
			return;
		}
		for (MenuManager manager : contributions) {
			processMenuManager(manager);
		}
		// throw away any MenuManagers that were added through processing
		managersToProcess.remove(editorId);
	}

	private void processMenuManager(MenuManager manager) {
		if (manager.getId() == null) {
			return;
		}
		IContributionItem[] items = manager.getItems();
		if (items.length == 0) {
			return;
		}
		MMenuContribution mc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mc.setParentId(manager.getId());
		mc.setPositionInParent("after=additions"); //$NON-NLS-1$
		mc.setVisibleWhen(createExpression());
		for (IContributionItem item : items) {
			add(mc, item);
			if (item instanceof MenuManager) {
				processMenuManager((MenuManager) item);
			}
		}
		addMenuContribution(mc, false);
		application.getMenuContributions().add(mc);
	}
}
