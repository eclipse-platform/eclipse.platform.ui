package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import java.util.*;

/**
 * This builder reads the actions for an action set from the registry.
 */
public class PluginActionSetBuilder extends PluginActionBuilder {
	public static final String TAG_ACTION_SET="actionSet";
	public static final String ATT_PULLDOWN="pulldown";
	
	private PluginActionSet actionSet;
	private IWorkbenchWindow window;
/**
 * Constructs a new builder.
 */
public PluginActionSetBuilder() {}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	String pulldown = element.getAttribute(ATT_PULLDOWN);
	ActionDescriptor desc = null;
	if (pulldown != null && pulldown.equals("true"))
		desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH_PULLDOWN, window);
	else
		desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH, window);
	WWinPluginAction action = (WWinPluginAction)desc.getAction();
	actionSet.addPluginAction(action);
	return desc;
}
/**
 * Read the actions within a config element.
 */
public void readActionExtensions(PluginActionSet set, IWorkbenchWindow window, 
	IActionBars bars) 
{
	this.actionSet = set;
	this.window = window;
	readElements(new IConfigurationElement[] {set.getConfigElement()});
	if (cache != null) {
		contribute(bars.getMenuManager(), bars.getToolBarManager(), true);
	} else {
		WorkbenchPlugin.log("Action Set is empty: " + set.getDesc().getId());
	}
}
/**
 * Implements abstract method to handle the provided XML element
 * in the registry.
 */
protected boolean readElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(TAG_ACTION_SET)) {
		readElementChildren(element);
		return true;
	}
	
	return super.readElement(element);
}
}
