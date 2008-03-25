/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * When 'action' tag is found in the registry, an object of this
 * class is created. It creates the appropriate action object
 * and captures information that is later used to add this action
 * object into menu/tool bar. This class is reused for
 * global (workbench) menu/tool bar, popup menu actions,
 * as well as view's pulldown and local tool bar.
 */
public class ActionDescriptor implements IPluginContribution {
    private PluginAction action;

    private String toolbarId;

    private String menuPath;

    private String id;

    private String pluginId;

    private String menuGroup;

    private String toolbarGroupId;

	private int mode = 0;

    /**
     * Popup constant.  Value <code>0x1</code>.
     */
    public static final int T_POPUP = 0x1;

    /**
     * View constant.  Value <code>0x2</code>.
     */
    public static final int T_VIEW = 0x2;

    /**
     * Workbench constant.  Value <code>0x3</code>.
     */
    public static final int T_WORKBENCH = 0x3;

    /**
     * Editor constant.  Value <code>0x4</code>.
     */
    public static final int T_EDITOR = 0x4;

    /**
     * Workbench pulldown constant.  Value <code>0x5</code>.
     */
    public static final int T_WORKBENCH_PULLDOWN = 0x5;

    /**
     * Push style constant.  Value <code>push</code>.
     */
    public static final String STYLE_PUSH = "push"; //$NON-NLS-1$

    /**
     * Radio style constant.  Value <code>radio</code>.
     */
    public static final String STYLE_RADIO = "radio"; //$NON-NLS-1$

    /***
     * Toggle style constant.  Value <code>toggle</code>.
     */
    public static final String STYLE_TOGGLE = "toggle"; //$NON-NLS-1$

    /**
     * Pulldown style constant.  Value <code>pulldown</code>.
     */
    public static final String STYLE_PULLDOWN = "pulldown"; //$NON-NLS-1$

    /**
     * Creates a new descriptor with the specified target.
     * 
     * @param actionElement the configuration element
     * @param targetType the type of action
     */
    public ActionDescriptor(IConfigurationElement actionElement, int targetType) {
        this(actionElement, targetType, null);
    }

    /**
     * Creates a new descriptor with the target and destination workbench part
     * it will go into.
     * 
     * @param actionElement the configuration element
     * @param targetType the type of action
     * @param target the target object
     */
    public ActionDescriptor(IConfigurationElement actionElement,
            int targetType, Object target) {
        // Load attributes.
        id = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        pluginId = actionElement.getNamespace();
        String label = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
        String tooltip = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
        String helpContextId = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_HELP_CONTEXT_ID);
        String mpath = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_MENUBAR_PATH);
        String tpath = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLBAR_PATH);
        String style = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
        String icon = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
        String hoverIcon = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_HOVERICON);
        String disabledIcon = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_DISABLEDICON);
        String description = actionElement.getAttribute(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
        String accelerator = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_ACCELERATOR);
        if ("FORCE_TEXT".equals(actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_MODE))) { //$NON-NLS-1$
        	mode  = ActionContributionItem.MODE_FORCE_TEXT;
        }

        // Verify input.
        if (label == null) {
            WorkbenchPlugin
                    .log("Invalid action declaration (label == null): " + id); //$NON-NLS-1$
            label = WorkbenchMessages.ActionDescriptor_invalidLabel; 
        }

        // Calculate menu and toolbar paths.
        String mgroup = null;
        String tgroup = null;
        if (mpath != null) {
            int loc = mpath.lastIndexOf('/');
            if (loc != -1) {
                mgroup = mpath.substring(loc + 1);
                mpath = mpath.substring(0, loc);
            } else {
                mgroup = mpath;
                mpath = null;
            }
        }
        if (targetType == T_POPUP && mgroup == null) {
			mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
		}
        if (tpath != null) {
            int loc = tpath.lastIndexOf('/');
            if (loc != -1) {
                tgroup = tpath.substring(loc + 1);
                tpath = tpath.substring(0, loc);
            } else {
                tgroup = tpath;
                tpath = null;
            }
        }
        menuPath = mpath;
        menuGroup = mgroup;
        if ((tpath != null) && tpath.equals("Normal")) { //$NON-NLS-1$
			tpath = ""; //$NON-NLS-1$
		}
        toolbarId = tpath;
        toolbarGroupId = tgroup;

        // Create action.
        action = createAction(targetType, actionElement, target, style);
        if (action.getText() == null) {
			action.setText(label);
		}
        if (action.getToolTipText() == null && tooltip != null) {
			action.setToolTipText(tooltip);
		}
        if (helpContextId != null) {
            String fullID = helpContextId;
            if (helpContextId.indexOf(".") == -1) { //$NON-NLS-1$
				// For backward compatibility we auto qualify the id if it is not
                // qualified)
                fullID = actionElement.getNamespace()
                        + "." + helpContextId;//$NON-NLS-1$
			}
            PlatformUI.getWorkbench().getHelpSystem().setHelp(action, fullID);
        }
        if (description != null) {
			action.setDescription(description);
		}

        if (style != null) {
            // Since 2.1, the "state" and "pulldown" attributes means something different
            // when the new "style" attribute has been set. See doc for more info.
            String state = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_STATE);
            if (state != null) {
                if (style.equals(STYLE_RADIO) || style.equals(STYLE_TOGGLE)) {
					action.setChecked(state.equals("true"));//$NON-NLS-1$
				}
            }
        } else {
            // Keep for backward compatibility for actions not using the
            // new style attribute.
            String state = actionElement.getAttribute(IWorkbenchRegistryConstants.ATT_STATE);
            if (state != null) {
                action.setChecked(state.equals("true"));//$NON-NLS-1$
            }
        }

        String extendingPluginId = actionElement.getDeclaringExtension()
                .getNamespace();

        if (icon != null) {
            action.setImageDescriptor(AbstractUIPlugin
                    .imageDescriptorFromPlugin(extendingPluginId, icon));
        }
        if (hoverIcon != null) {
            action.setHoverImageDescriptor(AbstractUIPlugin
                    .imageDescriptorFromPlugin(extendingPluginId, hoverIcon));
        }
        if (disabledIcon != null) {
            action
                    .setDisabledImageDescriptor(AbstractUIPlugin
                            .imageDescriptorFromPlugin(extendingPluginId,
                                    disabledIcon));
        }

        if (accelerator != null) {
			processAccelerator(action, accelerator);
		}
    }

    /**
     * Creates an instance of PluginAction. Depending on the target part,
     * subclasses of this class may be created.
     */
    private PluginAction createAction(int targetType,
            IConfigurationElement actionElement, Object target, String style) {
        int actionStyle = IAction.AS_UNSPECIFIED;
        if (style != null) {
            if (style.equals(STYLE_RADIO)) {
                actionStyle = IAction.AS_RADIO_BUTTON;
            } else if (style.equals(STYLE_TOGGLE)) {
                actionStyle = IAction.AS_CHECK_BOX;
            } else if (style.equals(STYLE_PULLDOWN)) {
                actionStyle = IAction.AS_DROP_DOWN_MENU;
            } else if (style.equals(STYLE_PUSH)) {
                actionStyle = IAction.AS_PUSH_BUTTON;
            }
        }

        switch (targetType) {
        case T_VIEW:
            return new ViewPluginAction(actionElement, (IViewPart) target, id,
                    actionStyle);
        case T_EDITOR:
            return new EditorPluginAction(actionElement, (IEditorPart) target,
                    id, actionStyle);
        case T_WORKBENCH:
            return new WWinPluginAction(actionElement,
                    (IWorkbenchWindow) target, id, actionStyle);
        case T_WORKBENCH_PULLDOWN:
            actionStyle = IAction.AS_DROP_DOWN_MENU;
            return new WWinPluginPulldown(actionElement,
                    (IWorkbenchWindow) target, id, actionStyle);
        case T_POPUP:
            return new ObjectPluginAction(actionElement, id, actionStyle);
        default:
            WorkbenchPlugin.log("Unknown Action Type: " + targetType);//$NON-NLS-1$
            return null;
        }
    }

    /**
     * Returns the action object held in this descriptor.
     * 
     * @return the action
     */
    public PluginAction getAction() {
        return action;
    }

    /**
     * Returns action's id as defined in the registry.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns named slot (group) in the menu where this action
     * should be added.
     * 
     * @return the menu group
     */
    public String getMenuGroup() {
        return menuGroup;
    }

    /**
     * Returns menu path where this action should be added. If null,
     * the action will not be added into the menu.
     * 
     * @return the menubar path
     */
    public String getMenuPath() {
        return menuPath;
    }

    /**
     * Returns the named slot (group) in the tool bar where this
     * action should be added.
     * 
     * @return the toolbar group id
     */
    public String getToolbarGroupId() {
        return toolbarGroupId;
    }

    /**
     * Returns id of the tool bar where this action should be added.
     * If null, action will not be added to the tool bar.
     * 
     * @return the toolbar id
     */
    public String getToolbarId() {
        return toolbarId;
    }

    /**
     * For debugging only.
     */
    public String toString() {
        return "ActionDescriptor(" + id + ")";//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Process the accelerator definition. If it is a number
     * then process the code directly - if not then parse it
     * and create the code
     */
    private void processAccelerator(IAction action, String acceleratorText) {

        if (acceleratorText.length() == 0) {
			return;
		}

        //Is it a numeric definition?
        if (Character.isDigit(acceleratorText.charAt(0))) {
            try {
                action.setAccelerator(Integer.valueOf(acceleratorText)
                        .intValue());
            } catch (NumberFormatException e) {
                WorkbenchPlugin.log("Invalid accelerator declaration for action: " + id, e); //$NON-NLS-1$
            }
        } else {
			action.setAccelerator(Action.convertAccelerator(acceleratorText));
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return pluginId;
    }
    
    public int getMode() {
    	return mode;
    }
}
