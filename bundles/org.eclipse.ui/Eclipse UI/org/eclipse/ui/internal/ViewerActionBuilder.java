package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.*;
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
 *
 */
public class ViewerActionBuilder extends PluginActionBuilder {
	private ISelectionProvider provider;
	private IWorkbenchPart part;
	public static final String TAG_CONTRIBUTION_TYPE = "viewerContribution";
/**
 *
 */
public ViewerActionBuilder() {}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  It should be implemented by subclasses.
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	ActionDescriptor desc = null;
	if (part instanceof IViewPart)
		desc = new ActionDescriptor(element, ActionDescriptor.T_VIEW, part);
	else
		desc = new ActionDescriptor(element, ActionDescriptor.T_EDITOR, part);
	if (provider != null) {
		PluginAction action = desc.getAction();
		provider.addSelectionChangedListener(action);
	}
	return desc;
}
/**
 * Returns a vector of viewer contributions for a selection provider.
 */
public List readViewerContributions(String id, ISelectionProvider prov, IWorkbenchPart part) {
	provider = prov;
	this.part = part;
	readContributions(id, TAG_CONTRIBUTION_TYPE, IWorkbenchConstants.PL_POPUP_MENU);
	return cache;
}
}
