/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * PerspectiveDescriptor.
 * <p>
 * A PerspectiveDesciptor has 3 states:
 * </p>
 * <ol>
 * <li>It <code>isPredefined()</code>, in which case it was defined from an
 * extension point.</li>
 * <li>It <code>isPredefined()</code> and <code>hasCustomFile</code>, in which 
 * case the user has customized a predefined perspective.</li>
 * <li>It <code>hasCustomFile</code>, in which case the user created a
 * new perspective.</li>
 * </ol>
 */
public class PerspectiveDescriptor implements IPerspectiveDescriptor, IPluginContribution {    
	private String id;
    private String pluginId;
	private String originalId;
	private String label;
	private String className;
	private String description;
	private boolean singleton;
	private boolean fixed;
	private ImageDescriptor image;
	private IConfigurationElement configElement;
	
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_DEFAULT = "default";//$NON-NLS-1$
	private static final String ATT_NAME="name";//$NON-NLS-1$
	private static final String ATT_ICON="icon";//$NON-NLS-1$
	private static final String ATT_CLASS="class";//$NON-NLS-1$
	private static final String ATT_SINGLETON="singleton";//$NON-NLS-1$
	private static final String ATT_FIXED="fixed";//$NON-NLS-1$
	
/**
 * Create a new empty descriptor.
 */
public PerspectiveDescriptor(String id, String label,PerspectiveDescriptor originalDescriptor) {
	super();
	this.id = id;
	this.label = label;
	if(originalDescriptor != null) {
		this.originalId = originalDescriptor.getOriginalId();
		this.image = originalDescriptor.image;
	}
}
/**
 * Create a descriptor from a config element.
 */
public PerspectiveDescriptor(IConfigurationElement configElement, String desc)
	throws CoreException
{
	super();
	this.configElement = configElement;
	id = configElement.getAttribute(ATT_ID);
    pluginId = configElement.getDeclaringExtension().getNamespace();
	label = configElement.getAttribute(ATT_NAME);
	className = configElement.getAttribute(ATT_CLASS);
	singleton = (configElement.getAttributeAsIs(ATT_SINGLETON) != null);
	
	String str = configElement.getAttribute(ATT_FIXED);
	if (str != null && str.equalsIgnoreCase("true"))//$NON-NLS-1$
		fixed = true;
	
	description = desc;

	// Sanity check.
	if ((label == null) || (className == null)) {
		throw new CoreException(new Status(IStatus.ERROR,
			WorkbenchPlugin.PI_WORKBENCH, 0,
			"Invalid extension (missing label or class name): " + id,//$NON-NLS-1$
			null));
	}

	// Load icon.
	String icon = configElement.getAttribute(ATT_ICON);
	if (icon != null) {
		IExtension extension = configElement.getDeclaringExtension();
		String extendingPluginId = extension.getNamespace();
		image = AbstractUIPlugin.imageDescriptorFromPlugin(
			extendingPluginId, icon);
	}
}
/**
 * Creates a factory for a predefined perspective.  If the perspective
 * is not predefined return null.
 *
 * @throws a CoreException if the object could not be instantiated.
 */
public IPerspectiveFactory createFactory() throws CoreException {
	IConfigurationElement element = configElement;
	if (originalId != null) {
		IPerspectiveDescriptor desc = ((PerspectiveRegistry)WorkbenchPlugin.getDefault().
				getPerspectiveRegistry()).findPerspectiveWithId(originalId);
		if (desc != null)
			element = ((PerspectiveDescriptor)desc).configElement;
		if (element == null)
			return null;
	}
	else if (className == null || element == null)
		return null;
	Object obj = WorkbenchPlugin.createExtension(element, ATT_CLASS);
	return (IPerspectiveFactory) obj;
}
/**
 * Deletes the custom definition for a perspective..
 */
public void deleteCustomDefinition() {
	((PerspectiveRegistry)WorkbenchPlugin.getDefault().
		getPerspectiveRegistry()).deleteCustomDefinition(this);
}
/**
 * Returns this perspective's description. 
 * This is the value of its <code>"description"</code> attribute.
 *
 * @return the description
 */
public String getDescription() {
	return description;
}
/**
 * Returns whether or not this perspective
 * is fixed.
 */
public boolean getFixed() {
	return fixed;
}
/**
 * Returns the ID.
 */
public String getId() {
	return id;
}
/**
 * Returns the descriptor of the image for this perspective.
 *
 * @return the descriptor of the image to display next to this perspective
 */
public ImageDescriptor getImageDescriptor() {
	return image;
}
/**
 * Returns the label.
 */
public String getLabel() {
	return label;
}
public String getOriginalId() {
	if(originalId == null)
		return id;
	return originalId;
}
/**
 * Returns true if this perspective has a custom file.
 */
public boolean hasCustomDefinition() {
	return ((PerspectiveRegistry)WorkbenchPlugin.getDefault().
		getPerspectiveRegistry()).hasCustomDefinition(this);
}
/**
 * Returns true if this perspective wants to be default.
 */
public boolean hasDefaultFlag() {
	if (configElement == null)
		return false;
	String str = configElement.getAttribute(ATT_DEFAULT);
	if (str == null)
		return false;
	return str.equals("true");//$NON-NLS-1$
}
/**
 * Returns true if this perspective is predefined by an extension.
 */
public boolean isPredefined() {
	return (className != null);
}
/**
 * Returns true if this perspective is a singleton.
 */
public boolean isSingleton() {
	return singleton;
}
/**
 * @see IPersistable
 */
public IStatus restoreState(IMemento memento) {
	IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_DESCRIPTOR);
	if(childMem != null) {
		id = childMem.getString(IWorkbenchConstants.TAG_ID);
		originalId = childMem.getString(IWorkbenchConstants.TAG_DESCRIPTOR);
		label = childMem.getString(IWorkbenchConstants.TAG_LABEL);
		className = childMem.getString(IWorkbenchConstants.TAG_CLASS);
		singleton = (childMem.getInteger(IWorkbenchConstants.TAG_SINGLETON) != null);		
	
		//Find a descriptor in the registry.
		PerspectiveDescriptor descriptor = (PerspectiveDescriptor)WorkbenchPlugin.getDefault().
			getPerspectiveRegistry().findPerspectiveWithId(getOriginalId());
	
		if(descriptor != null)
			//Copy the state from the registred descriptor.	
			image = descriptor.image;
	}
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null);		 //$NON-NLS-1$
}
/**
 * Revert to the predefined extension template.
 * Does nothing if this descriptor is user defined.
 */
public void revertToPredefined() {
	if (isPredefined())
		deleteCustomDefinition();
}
/**
 * @see IPersistable
 */
public IStatus saveState(IMemento memento) {
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_DESCRIPTOR);
	childMem.putString(IWorkbenchConstants.TAG_ID,id);
	if(originalId != null)
		childMem.putString(IWorkbenchConstants.TAG_DESCRIPTOR,originalId);
	childMem.putString(IWorkbenchConstants.TAG_LABEL,label);
	childMem.putString(IWorkbenchConstants.TAG_CLASS,className);
	if (singleton)
		childMem.putInteger(IWorkbenchConstants.TAG_SINGLETON, 1);
	return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
}

    /**
     * @return the configuration element used to create this perspective, if one was used.
     * @since 3.0
     */
    public IConfigurationElement getConfigElement() {
        return configElement;
    }  

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#fromPlugin()
	 */
	public boolean fromPlugin() {
		return pluginId != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return pluginId;
	}
}
