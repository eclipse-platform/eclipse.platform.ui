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
package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * This class contains shared functionality for reading action contributions
 * from plugins into workbench parts (both editors and views).
 */
public abstract class PluginActionBuilder extends RegistryReader {
    protected String targetID;

    protected String targetContributionTag;

    protected BasicContribution currentContribution;

    protected ArrayList cache;

    /**
     * The default constructor.
     */
    public PluginActionBuilder() {
    }

    /**
     * Contributes submenus and/or actions into the provided menu and tool bar
     * managers.
     * 
     * @param menu the menu to contribute to
     * @param toolbar the toolbar to contribute to 
     * @param appendIfMissing append containers if missing
     */
    public final void contribute(IMenuManager menu, IToolBarManager toolbar,
            boolean appendIfMissing) {
        if (cache == null) {
			return;
		}

        for (int i = 0; i < cache.size(); i++) {
            BasicContribution contribution = (BasicContribution) cache.get(i);
            contribution.contribute(menu, appendIfMissing, toolbar,
                    appendIfMissing);
        }
    }

    /**
     * This factory method returns a new ActionDescriptor for the
     * configuration element.  It should be implemented by subclasses.
     */
    protected abstract ActionDescriptor createActionDescriptor(
            IConfigurationElement element);

    /**
     * Factory method to create the helper contribution class that will hold
     * onto the menus and actions contributed.
     */
    protected BasicContribution createContribution() {
        return new BasicContribution();
    }

    /**
     * Returns the name of the part ID attribute that is expected
     * in the target extension.
     */
    protected String getTargetID(IConfigurationElement element) {
        String value = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
        return value != null ? value : "???"; //$NON-NLS-1$
    }
    
    /**
     * Returns the id of this contributions.
     */
    protected String getID(IConfigurationElement element) {
        String value = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        return value != null ? value : "???"; //$NON-NLS-1$
    }

    /**
     * Reads the contributions from the registry for the provided workbench
     * part and the provided extension point ID.
     */
    protected void readContributions(String id, String tag,
            String extensionPoint) {
        cache = null;
        currentContribution = null;
        targetID = id;
        targetContributionTag = tag;
        readRegistry(Platform.getExtensionRegistry(), PlatformUI.PLUGIN_ID,
                extensionPoint);
    }

    /**
     * Implements abstract method to handle the provided XML element
     * in the registry.
     */
    protected boolean readElement(IConfigurationElement element) {
        String tag = element.getName();

        // Ignore all object contributions element as these
        // are handled by the ObjectActionContributorReader.
        if (tag.equals(IWorkbenchRegistryConstants.TAG_OBJECT_CONTRIBUTION)) {
            return true;
        }

        // Found top level contribution element		
        if (tag.equals(targetContributionTag)) {
            if (targetID != null) {
                // Ignore contributions not matching target id
                String id = getTargetID(element);
                if (id == null || !id.equals(targetID)) {
					return true;
				}
            }

            // Read its sub-elements
            currentContribution = createContribution();
            readElementChildren(element);
            if (cache == null) {
				cache = new ArrayList(4);
			}
            cache.add(currentContribution);
            currentContribution = null;
            return true;
        }

        // Found menu contribution sub-element		
        if (tag.equals(IWorkbenchRegistryConstants.TAG_MENU)) {
            currentContribution.addMenu(element);
            return true;
        }

        // Found action contribution sub-element
        if (tag.equals(IWorkbenchRegistryConstants.TAG_ACTION)) {
            currentContribution.addAction(createActionDescriptor(element));
            return true;
        }

        return false;
    }

    /**
     * Helper class to collect the menus and actions defined within a
     * contribution element.
     */
    protected static class BasicContribution {
        protected ArrayList menus;

        protected ArrayList actions;

        /**
         * Add a menu. 
         * 
         * @param element the element to base the menu on
         */
        public void addMenu(IConfigurationElement element) {
            if (menus == null) {
				menus = new ArrayList(1);
			}
            menus.add(element);
        }

        /**
         * Add an action.
         * 
         * @param desc the descriptor
         */
        public void addAction(ActionDescriptor desc) {
            if (actions == null) {
				actions = new ArrayList(3);
			}
            actions.add(desc);
        }

        /**
         * Contributes submenus and/or actions into the provided menu and tool bar
         * managers.
         * 
         * The elements added are filtered based on activity enablement.
         * @param menu the menu to contribute to
         * @param menuAppendIfMissing whether to append missing groups to menus
         * @param toolbar the toolbar to contribute to
         * @param toolAppendIfMissing whether to append missing groups to toolbars
         */
        public void contribute(IMenuManager menu, boolean menuAppendIfMissing,
                IToolBarManager toolbar, boolean toolAppendIfMissing) {
            if (menus != null && menu != null) {
                for (int i = 0; i < menus.size(); i++) {
                    IConfigurationElement menuElement = (IConfigurationElement) menus
                            .get(i);
                    contributeMenu(menuElement, menu, menuAppendIfMissing);
                }
            }

            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    ActionDescriptor ad = (ActionDescriptor) actions.get(i);
                    if (menu != null) {
						contributeMenuAction(ad, menu, menuAppendIfMissing);
					}
                    if (toolbar != null) {
						contributeToolbarAction(ad, toolbar,
                                toolAppendIfMissing);
					}
                }
            }
        }

        /**
         * Creates a menu from the information in the menu configuration element and
         * adds it into the provided menu manager. If 'appendIfMissing' is true, and
         * menu path slot is not found, it will be created and menu will be added
         * into it. Otherwise, add operation will fail.
         */
        protected void contributeMenu(IConfigurationElement menuElement,
                IMenuManager mng, boolean appendIfMissing) {
            // Get config data.
            String id = menuElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
            String label = menuElement.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
            String path = menuElement.getAttribute(IWorkbenchRegistryConstants.ATT_PATH);
            if (label == null) {
				WorkbenchPlugin.log("Plugin \'" //$NON-NLS-1$
						+ menuElement.getContributor().getName()
						+ "\' invalid Menu Extension (label == null): " + id); //$NON-NLS-1$
				return;
			}

            // Calculate menu path and group.
            String group = null;
            if (path != null) {
                int loc = path.lastIndexOf('/');
                if (loc != -1) {
                    group = path.substring(loc + 1);
                    path = path.substring(0, loc);
                } else {
                    // assume that path represents a slot
                    // so actual path portion should be null
                    group = path;
                    path = null;
                }
            }

            // Find parent menu.
            IMenuManager parent = mng;
            if (path != null) {
                parent = mng.findMenuUsingPath(path);
                if (parent == null) {
					ideLog("Plugin \'" //$NON-NLS-1$
									+ menuElement.getContributor().getName()
									+ "\' invalid Menu Extension (Path \'"  //$NON-NLS-1$
									+ path + "\' is invalid): " + id); //$NON-NLS-1$
					return;
				}
            }

            // Find reference group.
            if (group == null) {
				group = IWorkbenchActionConstants.MB_ADDITIONS;
			}
            IContributionItem sep = parent.find(group);
            if (sep == null) {
                if (appendIfMissing) {
					addGroup(parent, group);
				} else {
                    WorkbenchPlugin
                            .log("Plugin \'" //$NON-NLS-1$
									+ menuElement.getContributor().getName()
									+ "\' invalid Menu Extension (Group \'"  //$NON-NLS-1$
									+ group + "\' is invalid): " + id); //$NON-NLS-1$
                    return;
                }
            }

            // If the menu does not exist create it.
            IMenuManager newMenu = parent.findMenuUsingPath(id);
            if (newMenu == null) {
				newMenu = new MenuManager(label, id);
			}

            // Add the menu
            try {
                insertAfter(parent, group, newMenu);
            } catch (IllegalArgumentException e) {
                WorkbenchPlugin
                        .log("Plugin \'" //$NON-NLS-1$
								+ menuElement.getContributor().getName()
								+ "\' invalid Menu Extension (Group \'"  //$NON-NLS-1$
								+ group + "\' is missing): " + id); //$NON-NLS-1$
            }

            // Get the menu again as it may be wrapped, otherwise adding
            // the separators and group markers below will not be wrapped
            // properly if the menu was just created.
            newMenu = parent.findMenuUsingPath(id);
            if (newMenu == null) {
				WorkbenchPlugin.log("Could not find new menu: " + id); //$NON-NLS-1$
			}

            // Create separators.
            IConfigurationElement[] children = menuElement.getChildren();
            for (int i = 0; i < children.length; i++) {
                String childName = children[i].getName();
                if (childName.equals(IWorkbenchRegistryConstants.TAG_SEPARATOR)) {
                    contributeSeparator(newMenu, children[i]);
                } else if (childName.equals(IWorkbenchRegistryConstants.TAG_GROUP_MARKER)) {
                    contributeGroupMarker(newMenu, children[i]);
                }
            }
        }

        /**
         * Contributes action from action descriptor into the provided menu manager.
         */
        protected void contributeMenuAction(ActionDescriptor ad,
                IMenuManager menu, boolean appendIfMissing) {
            // Get config data.
            String mpath = ad.getMenuPath();
            String mgroup = ad.getMenuGroup();
            if (mpath == null && mgroup == null) {
				return;
			}
            // Find parent menu.
            IMenuManager parent = menu;
            if (mpath != null) {
                parent = parent.findMenuUsingPath(mpath);
                if (parent == null) {
                    ideLog("Plug-in '" + ad.getPluginId() + "' contributed an invalid Menu Extension (Path: '" + mpath + "' is invalid): " + ad.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
            }

            // Find reference group.
            if (mgroup == null) {
				mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
			}
            IContributionItem sep = parent.find(mgroup);
            if (sep == null) {
                if (appendIfMissing) {
					addGroup(parent, mgroup);
				} else {
                    WorkbenchPlugin
                            .log("Plug-in '" + ad.getPluginId() + "' contributed an invalid Menu Extension (Group: '" + mgroup + "' is invalid): " + ad.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
            }

            // Add action.
            try {
                insertAfter(parent, mgroup, ad);
            } catch (IllegalArgumentException e) {
                WorkbenchPlugin
                        .log("Plug-in '" + ad.getPluginId() + "' contributed an invalid Menu Extension (Group: '" + mgroup + "' is missing): " + ad.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }

        /**
         * Creates a named menu separator from the information in the configuration element.
         * If the separator already exists do not create a second.
         */
        protected void contributeSeparator(IMenuManager menu,
                IConfigurationElement element) {
            String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
            if (id == null || id.length() <= 0) {
				return;
			}
            IContributionItem sep = menu.find(id);
            if (sep != null) {
				return;
			}
            insertMenuGroup(menu, new Separator(id));
        }

        /**
         * Creates a named menu group marker from the information in the configuration element.
         * If the marker already exists do not create a second.
         */
        protected void contributeGroupMarker(IMenuManager menu,
                IConfigurationElement element) {
            String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
            if (id == null || id.length() <= 0) {
				return;
			}
            IContributionItem marker = menu.find(id);
            if (marker != null) {
				return;
			}
            insertMenuGroup(menu, new GroupMarker(id));
        }

        /**
         * Contributes action from the action descriptor into the provided tool bar manager.
         */
        protected void contributeToolbarAction(ActionDescriptor ad,
                IToolBarManager toolbar, boolean appendIfMissing) {
            // Get config data.
            String tId = ad.getToolbarId();
            String tgroup = ad.getToolbarGroupId();
            if (tId == null && tgroup == null) {
				return;
			}

            // Find reference group.
            if (tgroup == null) {
				tgroup = IWorkbenchActionConstants.MB_ADDITIONS;
			}
            IContributionItem sep = null;
            sep = toolbar.find(tgroup);
            if (sep == null) {
                if (appendIfMissing) {
                    addGroup(toolbar, tgroup);
                } else {
                    WorkbenchPlugin
                            .log("Plug-in '" + ad.getPluginId()  //$NON-NLS-1$
                            		+ "' invalid Toolbar Extension (Group \'" //$NON-NLS-1$
                            		+ tgroup + "\' is invalid): " + ad.getId()); //$NON-NLS-1$
                    return;
                }
            }
            // Add action to tool bar.
            try {
                insertAfter(toolbar, tgroup, ad);
            } catch (IllegalArgumentException e) {
                WorkbenchPlugin
                        .log("Plug-in '" + ad.getPluginId()  //$NON-NLS-1$
                        		+ "' invalid Toolbar Extension (Group \'" //$NON-NLS-1$
                        		+ tgroup + "\' is missing): " + ad.getId()); //$NON-NLS-1$
            }
        }

        /**
         * Inserts the separator or group marker into the menu. Subclasses may override.
         */
        protected void insertMenuGroup(IMenuManager menu,
                AbstractGroupMarker marker) {
            menu.add(marker);
        }

        /**
         * Inserts an action after another named contribution item.
         * Subclasses may override.
         */
        protected void insertAfter(IContributionManager mgr, String refId,
                ActionDescriptor desc) {
            final PluginActionContributionItem item = new PluginActionContributionItem(desc.getAction());
            item.setMode(desc.getMode());
			insertAfter(mgr, refId, item);
        }

        /**
         * Inserts a contribution item after another named contribution item.
         * Subclasses may override.
         */
        protected void insertAfter(IContributionManager mgr, String refId,
                IContributionItem item) {
            mgr.insertAfter(refId, item);
        }

        /**
         * Adds a group to a contribution manager.
         * Subclasses may override.
         */
        protected void addGroup(IContributionManager mgr, String name) {
            mgr.add(new Separator(name));
        }

		/**
		 * Disposes this contribution. 
		 * 
		 * @since 3.1
		 */
		public void dispose() {
			// do nothing
		}
		
		/**
		 * Disposes the actions.
		 *
		 * @since 3.1
		 */
		protected void disposeActions() {
            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    PluginAction proxy = ((ActionDescriptor) actions.get(i))
                            .getAction();
					proxy.dispose();
                }
				actions = null;
            }
		}
    }
    
    private static boolean allowIdeLogging = false;
    
    /**
	 * If set to <code>false</code>, some of the logs that can be caused by
	 * use IDE plugins from an RCP app will be ignored.
	 * 
	 * @param b
	 *            Log the errors or not.
	 * @since 3.3
	 */
    public static void setAllowIdeLogging(boolean b) {
    	allowIdeLogging = b;
    }
    
    /**
	 * These are log messages that should be ignored by RCP apps when using the
	 * IDE plugins.
	 * 
	 * @param msg
	 * @since 3.3
	 */
    private static void ideLog(String msg) {
    	if (allowIdeLogging) {
    		WorkbenchPlugin.log(msg);
    	}
    }
}
