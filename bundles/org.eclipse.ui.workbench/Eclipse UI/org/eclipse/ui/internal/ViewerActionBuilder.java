package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 *
 */
public class ViewerActionBuilder extends PluginActionBuilder {
	private ISelectionProvider provider;
	private IWorkbenchPart part;
	public static final String TAG_CONTRIBUTION_TYPE = "viewerContribution";//$NON-NLS-1$
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
 * Reads the contributions for a viewer menu.
 * This method is typically used in conjunction with <code>contribute</code> to read
 * and then insert actions for a particular viewer menu.
 *
 * @param id the menu id
 * @param prov the selection provider for the control containing the menu
 * @param part the part containing the menu.
 * @return <code>true</code> if 1 or more items were read.  
 */
public boolean readViewerContributions(String id, ISelectionProvider prov, IWorkbenchPart part) {
	provider = prov;
	this.part = part;
	readContributions(id, TAG_CONTRIBUTION_TYPE, IWorkbenchConstants.PL_POPUP_MENU);
	return (cache != null);
}
}
