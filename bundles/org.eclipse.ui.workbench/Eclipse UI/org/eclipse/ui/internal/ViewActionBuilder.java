package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import java.util.*;

/**
 *
 */
public class ViewActionBuilder extends PluginActionBuilder {
	private IViewPart targetPart;
	public static final String TAG_CONTRIBUTION_TYPE = "viewContribution";//$NON-NLS-1$
/**
 *
 */
public ViewActionBuilder() {}
/**
 * contributeToPart method comment.
 */
protected void contributeToPart(IViewPart part) {
	if (cache != null) {
		IActionBars bars = part.getViewSite().getActionBars();
		contribute(bars.getMenuManager(), bars.getToolBarManager(), true);
	}
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  It should be implemented by subclasses.
 */
protected ActionDescriptor createActionDescriptor(org.eclipse.core.runtime.IConfigurationElement element) {
	return new ActionDescriptor(element, ActionDescriptor.T_VIEW, targetPart);
}
/**
 * Return all extendedn actions. */
public ActionDescriptor[] getExtendedActions() {
	ArrayList result = new ArrayList(cache.size());
	for (Iterator iter = cache.iterator(); iter.hasNext();) {
		Object element = (Object) iter.next();
		if (element instanceof ActionDescriptor) {
			result.add(element);
		}
	}
	return (ActionDescriptor[])result.toArray(new ActionDescriptor[result.size()]);
}
/**
 *
 */
public void readActionExtensions(IViewPart viewPart) {
	targetPart = viewPart;
	readContributions(viewPart.getSite().getId(), TAG_CONTRIBUTION_TYPE, 
		IWorkbenchConstants.PL_VIEW_ACTIONS);
	contributeToPart(targetPart);
}
}
