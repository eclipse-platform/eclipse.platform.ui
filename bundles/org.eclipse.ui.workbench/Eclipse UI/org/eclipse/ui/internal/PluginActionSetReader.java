package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.decorators.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * Read the actions for an plugin action set.
 *
 * [Issue: There is some overlap with the class
 *		PluginActionSetBuilder which should be reviewed
 *		at a later time and maybe merged together]
 */
public class PluginActionSetReader extends RegistryReader {
	private List cache = new ArrayList();
/**
 * PluginActionSetReader constructor comment.
 */
public PluginActionSetReader() {
	super();
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  
 */
protected LightweightActionDescriptor createActionDescriptor(IConfigurationElement element) {
	return new LightweightActionDescriptor(element);
}
/**
 * Return all the action descriptor within the set.
 */
public LightweightActionDescriptor[] readActionDescriptors(ActionSetDescriptor actionSet) {
	readElements(new IConfigurationElement[] {actionSet.getConfigElement()});
	LightweightActionDescriptor[] actions = new LightweightActionDescriptor[cache.size()];
	cache.toArray(actions);
	return actions;
}
/**
 * @see RegistryReader
 */
protected boolean readElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(PluginActionSetBuilder.TAG_ACTION_SET)) {
		readElementChildren(element);
		return true;
	}
	if (tag.equals(ObjectActionContributorReader.TAG_OBJECT_CONTRIBUTION)) {
		// This builder is sometimes used to read the popup menu
		// extension point.  Ignore all object contributions.
		return true;
	}
	if (tag.equals(PluginActionSetBuilder.TAG_MENU)) {
		return true; // just cache the element - don't go into it
	}
	if (tag.equals(PluginActionSetBuilder.TAG_ACTION)) {
		cache.add(createActionDescriptor(element));
		return true; // just cache the action - don't go into
	}
	
	return false;
}
}
