package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	private String toolbarId;
	private String menuPath;
	private String id;
	private String menuGroup;
	private String toolbarGroupId;
	private String beforeToolbarGroupId;
	
	public static final int T_POPUP=0x1;
	public static final int T_VIEW=0x2;
	public static final int T_WORKBENCH=0x3;
	public static final int T_EDITOR=0x4;
	public static final int T_WORKBENCH_PULLDOWN=0x5;
	
	public static final String ATT_ID = "id";//$NON-NLS-1$
	public static final String ATT_DEFINITION_ID = "definitionId";//$NON-NLS-1$
	public static final String ATT_HELP_CONTEXT_ID = "helpContextId";//$NON-NLS-1$
	public static final String ATT_LABEL = "label";//$NON-NLS-1$
	public static final String ATT_STYLE = "style";//$NON-NLS-1$
	public static final String ATT_STATE = "state";//$NON-NLS-1$
	public static final String ATT_DESCRIPTION = "description";//$NON-NLS-1$
	public static final String ATT_TOOLTIP = "tooltip";//$NON-NLS-1$
	public static final String ATT_MENUBAR_PATH = "menubarPath";//$NON-NLS-1$
	public static final String ATT_TOOLBAR_PATH = "toolbarPath";//$NON-NLS-1$
	public static final String ATT_ICON = "icon";//$NON-NLS-1$
	public static final String ATT_HOVERICON = "hoverIcon";//$NON-NLS-1$
	public static final String ATT_DISABLEDICON = "disabledIcon";//$NON-NLS-1$
	public static final String ATT_CLASS = "class";//$NON-NLS-1$
	public static final String ATT_RETARGET = "retarget";//$NON-NLS-1$
	public static final String ATT_ALLOW_LABEL_UPDATE = "allowLabelUpdate";//$NON-NLS-1$
	public static final String ATT_ACCELERATOR = "accelerator";//$NON-NLS-1$
	
	public static final String STYLE_PUSH = "push"; //$NON-NLS-1$
	public static final String STYLE_RADIO = "radio"; //$NON-NLS-1$
	public static final String STYLE_TOGGLE = "toggle"; //$NON-NLS-1$
	public static final String STYLE_PULLDOWN = "pulldown"; //$NON-NLS-1$
	
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
	String style = actionElement.getAttribute(ATT_STYLE);
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
	String tbeforegroup = null;
	if (tpath != null) {
		int loc = tpath.lastIndexOf('/');
		if (loc != -1) {
			tgroup = tpath.substring(loc + 1);
			tpath = tpath.substring(0, loc);
			loc = tpath.lastIndexOf('/');
			if (loc != -1) {
				tbeforegroup = tpath.substring(loc + 1);
				tpath = tpath.substring(0, loc);
			}
		} else {
			tgroup = tpath;
			tpath = null;
		}
	}
	menuPath = mpath;
	menuGroup = mgroup;
	toolbarId = tpath;
	toolbarGroupId = tgroup;
	beforeToolbarGroupId = tbeforegroup;

	// Create action.
	action = createAction(targetType, actionElement, target, defId, style);
	if (action.getText() == null) // may have been set by delegate
		action.setText(label);
	action.setId(id);
	if (action.getToolTipText() == null && tooltip != null) // may have been set by delegate
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
		
	if (style != null) {
		// Since 2.1, the "state" and "pulldown" attributes means something different
		// when the new "style" attribute has been set. See doc for more info.
		String state = actionElement.getAttribute(ATT_STATE);
		if (state != null) {
			if (style.equals(STYLE_RADIO) || style.equals(STYLE_TOGGLE))
				action.setChecked(state.equals("true"));//$NON-NLS-1$
		}
	} else {
		// Keep for backward compatibility for actions not using the
		// new style attribute.
		String state = actionElement.getAttribute(ATT_STATE);
		if (state != null) {
			action.setChecked(state.equals("true"));//$NON-NLS-1$
		}
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
private PluginAction createAction(int targetType, IConfigurationElement actionElement, Object target, String defId, String style) {
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
			return new ViewPluginAction(actionElement, ATT_CLASS, (IViewPart)target, defId, actionStyle);
		case T_EDITOR:
			return new EditorPluginAction(actionElement, ATT_CLASS, (IEditorPart)target, defId, actionStyle);
		case T_WORKBENCH:
			return new WWinPluginAction(actionElement, ATT_CLASS, (IWorkbenchWindow)target, defId, actionStyle);
		case T_WORKBENCH_PULLDOWN:
			actionStyle = IAction.AS_DROP_DOWN_MENU;
			return new WWinPluginPulldown(actionElement, ATT_CLASS, (IWorkbenchWindow)target, defId, actionStyle);
		case T_POPUP:
			return new ObjectPluginAction(actionElement, ATT_CLASS, defId, actionStyle);
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
 * Returns the named slot (group) in the tool bar where this
 * action's toolbar group should be before.
 */
public String getBeforeToolbarGroupId() {
	return beforeToolbarGroupId;
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

public String getToolbarGroupId() {
	return toolbarGroupId;
}
/**
 * Returns id of the tool bar where this action should be added.
 * If null, action will not be added to the tool bar.
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
		action.setAccelerator(Action.convertAccelerator(acceleratorText));
}
		
}
