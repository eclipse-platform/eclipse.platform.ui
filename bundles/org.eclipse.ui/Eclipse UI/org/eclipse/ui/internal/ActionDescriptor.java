package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * When 'action' tag is found in the registry, an object of this
 * class is created. It creates the appropriate action object
 * and captures information that is later used to add this action
 * object into menu/tool bar. This class is reused for
 * global (workbench) menu/tool bar, popup menu actions,
 * as well as view's pulldown and local tool bar.
 */
public class ActionDescriptor {
	private PluginAction action;
	private String toolbarPath;
	private String menuPath;
	private String id;
	private String definitionId;
	private String menuGroup;
	private String toolbarGroup;
	
	public static final int T_POPUP=0x1;
	public static final int T_VIEW=0x2;
	public static final int T_WORKBENCH=0x3;
	public static final int T_EDITOR=0x4;
	public static final int T_WORKBENCH_PULLDOWN=0x5;
	
	public static final String ATT_ID = "id";//$NON-NLS-1$
	public static final String ATT_DEFINITION_ID = "definitionId";//$NON-NLS-1$
	public static final String ATT_HELP_CONTEXT_ID = "helpContextId";//$NON-NLS-1$
	public static final String ATT_LABEL = "label";//$NON-NLS-1$
	public static final String ATT_STATE = "state";//$NON-NLS-1$
	public static final String ATT_DESCRIPTION = "description";//$NON-NLS-1$
	public static final String ATT_TOOLTIP = "tooltip";//$NON-NLS-1$
	public static final String ATT_MENUBAR_PATH = "menubarPath";//$NON-NLS-1$
	public static final String ATT_TOOLBAR_PATH = "toolbarPath";//$NON-NLS-1$
	public static final String ATT_ICON = "icon";//$NON-NLS-1$
	public static final String ATT_HOVERICON = "hoverIcon";//$NON-NLS-1$
	public static final String ATT_DISABLEDICON = "disabledIcon";//$NON-NLS-1$
	public static final String ATT_CLASS = "class";//$NON-NLS-1$
	public static final String ATT_ACCELERATOR = "accelerator";//$NON-NLS-1$
/**
 * Creates a new descriptor with the specified target.
 */
public ActionDescriptor(IConfigurationElement actionElement, int targetType) {
	this(actionElement, targetType, null);
}
/**
 * Creates a new descriptor with the target and destination workbench part
 * it will go into.
 */
public ActionDescriptor(IConfigurationElement actionElement, int targetType, Object target) {
	// Load attributes.
	id = actionElement.getAttribute(ATT_ID);
	String label = actionElement.getAttribute(ATT_LABEL);
	String defId = actionElement.getAttribute(ATT_DEFINITION_ID);
	String tooltip = actionElement.getAttribute(ATT_TOOLTIP);
	String helpContextId = actionElement.getAttribute(ATT_HELP_CONTEXT_ID);
	String mpath = actionElement.getAttribute(ATT_MENUBAR_PATH);
	String tpath = actionElement.getAttribute(ATT_TOOLBAR_PATH);
	String state = actionElement.getAttribute(ATT_STATE);
	String icon = actionElement.getAttribute(ATT_ICON);
	String hoverIcon = actionElement.getAttribute(ATT_HOVERICON);
	String disabledIcon = actionElement.getAttribute(ATT_DISABLEDICON);
	String description = actionElement.getAttribute(ATT_DESCRIPTION);
	String accelerator = actionElement.getAttribute(ATT_ACCELERATOR);

	// Verify input.
	if (label == null) {
		WorkbenchPlugin.log("Invalid action declaration (label == null): " + id); //$NON-NLS-1$
		label = WorkbenchMessages.getString("ActionDescriptor.invalidLabel"); //$NON-NLS-1$
	}
	definitionId = defId;
//	if(defId == null)
//		WorkbenchPlugin.log("Invalid action declaration (definitionId == null): " + id);
//  }
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
	if (targetType == T_POPUP && mgroup == null)
		mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
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
	toolbarPath = tpath;
	toolbarGroup = tgroup;

	// Create action.
	action = createAction(targetType, actionElement, target);
	action.setText(label);
	action.setId(id);
	if (tooltip != null)
		action.setToolTipText(tooltip);
	if (helpContextId != null) {
		String fullID = helpContextId;
		if (helpContextId.indexOf(".") == -1) //$NON-NLS-1$
			// For backward compatibility we auto qualify the id if it is not qualified)
			fullID = actionElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier() + "." + helpContextId;//$NON-NLS-1$
		WorkbenchHelp.setHelp(action, fullID);
	}
	if (description != null)
		action.setDescription(description);
	if (state != null) {
		action.setChecked(state.equals("true"));//$NON-NLS-1$
	}
	if (icon != null) {
		action.setImageDescriptor(WorkbenchImages.getImageDescriptorFromExtension(actionElement.getDeclaringExtension(), icon));
	}
	if (hoverIcon != null) {
		action.setHoverImageDescriptor(WorkbenchImages.getImageDescriptorFromExtension(actionElement.getDeclaringExtension(), hoverIcon));
	}
	if (disabledIcon != null) {
		action.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptorFromExtension(actionElement.getDeclaringExtension(), disabledIcon));
	}
	
	if(accelerator != null)
		processAccelerator(action,accelerator);
}
/**
 * Creates an instance of PluginAction. Depending on the target part,
 * subclasses of this class may be created.
 */
private PluginAction createAction(int targetType, IConfigurationElement actionElement, Object target) {
	switch (targetType) {
		case T_VIEW:
			return new ViewPluginAction(actionElement, ATT_CLASS, (IViewPart)target);
		case T_EDITOR:
			return new EditorPluginAction(actionElement, ATT_CLASS, (IEditorPart)target);
		case T_WORKBENCH:
			return new WWinPluginAction(actionElement, ATT_CLASS, (IWorkbenchWindow)target,definitionId);
		case T_WORKBENCH_PULLDOWN:
			return new WWinPluginPulldown(actionElement, ATT_CLASS, (IWorkbenchWindow)target);
		case T_POPUP:
			return new ObjectPluginAction(actionElement, ATT_CLASS);
		default:
			WorkbenchPlugin.log("Unknown Action Type: " + targetType);//$NON-NLS-1$
			return null;
	}
}
/**
 * Returns the action object held in this descriptor.
 */
public PluginAction getAction() {
	return action;
}
/**
 * Returns action's id as defined in the registry.
 */
public String getId() {
	return id;
}
/**
 * Returns named slot (group) in the menu where this action
 * should be added.
 */
public String getMenuGroup() {
	return menuGroup;
}
/**
 * Returns menu path where this action should be added. If null,
 * the action will not be added into the menu.
 */

public String getMenuPath() {
	return menuPath;
}
/**
 * Returns the named slot (group) in the tool bar where this
 * action should be added.
 */

public String getToolbarGroup() {
	return toolbarGroup;
}
/**
 * Returns path in the tool bar where this action should be added.
 * If null, action will not be added to the tool bar.
 */
public String getToolbarPath() {
	return toolbarPath;
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
private void processAccelerator(IAction action, String acceleratorText){
	
	if(acceleratorText.length() == 0)
		return;
	
	//Is it a numeric definition?
	if(Character.isDigit(acceleratorText.charAt(0))){
		try{
			action.setAccelerator(Integer.valueOf(acceleratorText).intValue());
		}
		catch (NumberFormatException exception){
			WorkbenchPlugin.log("Invalid accelerator declaration: " + id); //$NON-NLS-1$
		}
	}
	else
		action.setAccelerator(convertAccelerator(acceleratorText));
}
		
/**
 * Parses the given accelerator text, and converts it to an accelerator key code.
 *
 * @param acceleratorText the accelerator text
 * @result the SWT key code, or 0 if there is no accelerator
 */
private int convertAccelerator(String acceleratorText) {
	int accelerator = 0;
	StringTokenizer stok = new StringTokenizer(acceleratorText, "+");    //$NON-NLS-1$

	int keyCode = -1;

	boolean hasMoreTokens = stok.hasMoreTokens();
	while (hasMoreTokens) {
		System.out.println(getClass().getName());
		String token = stok.nextToken();
		hasMoreTokens = stok.hasMoreTokens();
		// Every token except the last must be one of the modifiers
		// Ctrl, Shift, or Alt.
		if (hasMoreTokens) {
			int modifier = Action.findModifier(token);
			if (modifier != 0) {
				accelerator |= modifier;
			} else {//Leave if there are none
				return 0;
			}
		} else {
			keyCode = Action.findKeyCode(token);
		}
	}
	if (keyCode != -1) {
		accelerator |= keyCode;
	}
	return accelerator;
}
}
