package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * ActionSetDescriptor
 */
public class ActionSetDescriptor extends WorkbenchAdapter
	implements IActionSetDescriptor, IAdaptable
{
	private String id;
	private String label;
	private String category;
	private boolean visible;
	private String description;
	private IConfigurationElement configElement;
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_LABEL="label";//$NON-NLS-1$
	private static final String ATT_VISIBLE="visible";//$NON-NLS-1$
	private static final String ATT_DESC="description";//$NON-NLS-1$
/**
 * Create a descriptor from a config element.
 */
public ActionSetDescriptor(IConfigurationElement configElement)
	throws CoreException
{
	super();
	this.configElement = configElement;
	id = configElement.getAttribute(ATT_ID);
	label = configElement.getAttribute(ATT_LABEL);
	description = configElement.getAttribute(ATT_DESC);
	String str = configElement.getAttribute(ATT_VISIBLE);
	if (str != null && str.equals("true"))//$NON-NLS-1$
		visible = true;

	// Sanity check.
	if (label == null) {
		throw new CoreException(new Status(IStatus.ERROR,
			WorkbenchPlugin.PI_WORKBENCH, 0,
			"Invalid extension (missing label): " + id,//$NON-NLS-1$
			null));
	}
}
/**
 * Returns the action set for this descriptor.
 *
 * @return the action set
 */
public IActionSet createActionSet()
	throws CoreException
{
	return new PluginActionSet(this);
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) 
		return this;
	return null;
}
/**
 * Returns the category of this action set.
 *
 * @return a non-empty category name or <cod>null</code> if none specified
 */
public String getCategory() {
	return category;
}
/**
 * @see IWorkbenchAdapter#getChildren
 */
public Object[] getChildren(Object o) {
	if (o == this)
		return (new PluginActionSetReader()).readActionDescriptors(this);

	return NO_CHILDREN;
}
/**
 * Returns the config element
 */
public IConfigurationElement getConfigElement() {
	return configElement;
}
/**
 * Returns this action set's description. 
 * This is the value of its <code>"description"</code> attribute.
 *
 * @return the description
 */
public String getDescription() {
	return description;
}
/**
 * Returns this action set's id. 
 * This is the value of its <code>"id"</code> attribute.
 * <p>
 *
 * @return the action set id
 */
public String getId() {
	return id;
}
/**
 * Returns this action set's label. 
 * This is the value of its <code>"label"</code> attribute.
 *
 * @return the label
 */
public String getLabel() {
	return label;
}
/**
 * @see IWorkbenchAdapter#getLabel
 */
public String getLabel(Object o) {
	if (o == this)
		return getLabel();
	return "Unknown Label";//$NON-NLS-1$
}
/**
 * Returns whether this action set is initially visible.
 */
public boolean isInitiallyVisible() {
	return visible;
}
/**
 * Sets the category of this action set.
 *
 * @param cat a non-empty category id
 */
public void setCategory(String id) {
	category = id;
}
}
