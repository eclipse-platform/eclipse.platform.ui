package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;

import java.util.*;

/**
 * This class describes one element within the popup menu action registry.
 */
public class ObjectActionContributor extends PluginActionBuilder
	implements IObjectActionContributor 
{
	private IConfigurationElement config;
	private boolean configRead=false;
	private boolean adaptable = false;
	static final String ATT_NAME_FILTER="nameFilter";//$NON-NLS-1$
	static final String ATT_ADAPTABLE="adaptable";//$NON-NLS-1$
	static final String P_TRUE="true";//$NON-NLS-1$
	private ObjectFilterTest filterTest;
	private ActionExpression visibilityTest;
/**
 * The constructor.
 */
public ObjectActionContributor(IConfigurationElement config) {
	this.config = config;
	this.adaptable = P_TRUE.equalsIgnoreCase(config.getAttribute(ATT_ADAPTABLE));
}
/**
 * Contributes actions applicable for the current selection.
 */
public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager menu, 
	ISelectionProvider selProv) 
{
	// Parse config.
	if (!configRead)
		readConfigElement();
	if (cache == null)
		return false;

	// Get a structured selection.	
	ISelection sel = selProv.getSelection();
	if ((sel == null) || !(sel instanceof IStructuredSelection))
		return false;
	IStructuredSelection selection = (IStructuredSelection) sel;
	
	// Generate menu.
	boolean actualContributions = false;
	for (int i = 0; i < cache.size(); i++) {
		Object obj = cache.get(i);
		if (obj instanceof ActionDescriptor) {
			ActionDescriptor ad = (ActionDescriptor) obj;
			contributeMenuAction(ad, menu, true);
			// Update action for the current selection and part.
			if (ad.getAction() instanceof ObjectPluginAction) {
				ObjectPluginAction action = (ObjectPluginAction)ad.getAction();
				action.setActivePart(part);
				action.selectionChanged(selection);
			}
			actualContributions = true;
		}
	}
	return actualContributions;
}
/**
 * Contributes menus applicable for the current selection.
 */
public boolean contributeObjectMenus(IMenuManager menu, ISelectionProvider selProv) {
	// Parse config element.
	if (!configRead)
		readConfigElement();
	if (cache == null)
		return false;

	// Get a structured selection.	
	ISelection sel = selProv.getSelection();
	if ((sel == null) || !(sel instanceof IStructuredSelection))
		return false;
	IStructuredSelection selection = (IStructuredSelection) sel;
	
	// Generate menu.
	boolean actualContributions = false;
	for (int i = 0; i < cache.size(); i++) {
		Object obj = cache.get(i);
		if (obj instanceof IConfigurationElement) {
			IConfigurationElement menuElement = (IConfigurationElement) obj;
			contributeMenu(menuElement, menu, true);
			actualContributions = true;
		}
	}
	return actualContributions;
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	return new ActionDescriptor(element, ActionDescriptor.T_POPUP);
}
/**
 * Returns true if name filter is not specified for the contribution
 * or the current selection matches the filter.
 */
public boolean isApplicableTo(Object object) {
	// Parse config.
	if (!configRead)
		readConfigElement();
		
	// Test name.
	if (!testName(object))
		return false;

	// Test visibility filter.
	if (visibilityTest != null)
		return visibilityTest.isEnabledFor(object);
		
	// Test custom filter.
	if (filterTest != null)
		return filterTest.matches(object, true);
		
	return true;
}
/**
 * Reads the configuration element and all the children.
 * This creates an action descriptor for every action in the extension.
 */
private void readConfigElement() {
	if (!configRead) {
		readElementChildren(config);
		configRead = true;
	}
}
/**
 * Implements abstract method to handle the provided XML element
 * in the registry.
 */
protected boolean readElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(PluginActionBuilder.TAG_VISIBILITY)) {
		visibilityTest = new ActionExpression(element);
		return true;
	} else if (tag.equals(PluginActionBuilder.TAG_FILTER)) {
		if (filterTest == null)
			filterTest = new ObjectFilterTest();
		filterTest.addFilterElement(element);
		return true;
	} 
	
	return super.readElement(element);
}
/**
 * Returns whether the current selection matches the contribution name filter.
*/
private boolean testName(Object object) {
	String nameFilter = config.getAttribute(ATT_NAME_FILTER);
	if (nameFilter == null)
		return true;
	String objectName = null;
	if (object instanceof IAdaptable) {
		IAdaptable element = (IAdaptable) object;
		IWorkbenchAdapter de = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
		if (de != null)
			objectName = de.getLabel(element);
	}
	if (objectName == null) {
		objectName = object.toString();
	}
	return SelectionEnabler.verifyNameMatch(objectName, nameFilter);
}
	
/*
 * @see IObjectContributor#canAdapt()
 */
public boolean canAdapt() {
	return adaptable;
}

}
