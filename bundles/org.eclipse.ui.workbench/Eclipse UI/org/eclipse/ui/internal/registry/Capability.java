/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;

import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A capability is the user interface aspect of a project's nature. There is
 * a 1-1 relationship between a capability and a nature. It is possible for
 * a nature to not have a capability, but the opposite is not true - that is,
 * a capability must represent a nature.
 * <p>
 * A capability can take control of the user interface of other capabilities. It
 * is then responsible for collecting the necessary information and adding the
 * natures represented by these capabilites.
 * </p>
 */
public class Capability extends WorkbenchAdapter implements IAdaptable {
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_NATURE_ID = "natureId"; //$NON-NLS-1$
	private static final String ATT_CATEGORY = "category"; //$NON-NLS-1$
	private static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATT_INSTALL_WIZARD = "installWizard"; //$NON-NLS-1$
	private static final String ATT_INSTALL_DETAILS = "installDetails"; //$NON-NLS-1$
	private static final String ATT_UNINSTALL_WIZARD = "uninstallWizard"; //$NON-NLS-1$
	private static final String ATT_UNINSTALL_DETAILS = "uninstallDetails"; //$NON-NLS-1$
	
	private String id;
	private String natureId;
	private IProjectNatureDescriptor natureDescriptor;
	private ImageDescriptor icon;
	private IConfigurationElement element;
	private ArrayList handleUIs;
	private ArrayList perspectiveChoices;
	
	/**
	 * Creates an instance of <code>Capability</code> using the
	 * information provided by the configuration element.
	 * 
	 * @param configElement the <code>IConfigurationElement<code> containing
	 * 		the attributes
	 * @param reader the <code>CapabilityRegistryReader<code> used to log missing attributes
	 * @throws a <code>WorkbenchException</code> if the ID, nature, or wizard is <code>null</code>
	 */
	public Capability(IConfigurationElement configElement, CapabilityRegistryReader reader)
		throws WorkbenchException
	{
		super();
		
		boolean missingAttribute = false;
		String attr_id = configElement.getAttribute(ATT_ID);
		String attr_nature = configElement.getAttribute(ATT_NATURE_ID);
			
		if (attr_id == null) {
			reader.logMissingAttribute(configElement, ATT_ID);
			missingAttribute = true;
		}
		if (attr_nature == null) {
			reader.logMissingAttribute(configElement, ATT_NATURE_ID);
			missingAttribute = true;
		}
		if (configElement.getAttribute(ATT_INSTALL_WIZARD) == null) {
			reader.logMissingAttribute(configElement, ATT_INSTALL_WIZARD);
			missingAttribute = true;
		}
		
		if (missingAttribute)
			throw new WorkbenchException("Capability missing required attributes."); //$NON-NLS-1$

		id = attr_id;
		natureId = attr_nature;
		element = configElement;
		natureDescriptor = ResourcesPlugin.getWorkspace().getNatureDescriptor(natureId);
	}
	
	/**
	 * Creates an instance of <code>Capability</code> as an unknown one
	 * for a given nature id.
	 * 
	 * @param natureId the nature id for the unknown capbility
	 */
	public Capability(String natureId) {
		super();
		this.id = natureId;
		this.natureId = natureId;
	}
	
	/**
	 * Adds the id of a capability for which this capability handles
	 * the user interface.
	 */
	public void addHandleUI(String capabilityId) {
		if (handleUIs == null)
			handleUIs = new ArrayList(4);
		handleUIs.add(capabilityId);	
	}
	
	/**
	 * Adds the id of a perspective for which this capability
	 * wants to present as a choice in the user interface.
	 */
	public void addPerspectiveChoice(String perspId) {
		if (perspectiveChoices == null)
			perspectiveChoices = new ArrayList(4);
		perspectiveChoices.add(perspId);	
	}
	
	public String getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return getIconDescriptor();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWorkbenchAdapter.
	 */
	public String getLabel(Object o) {
		return getName();
	}
	
	public String getName() {
		if (isValid())
			return natureDescriptor.getLabel();
		else
			return WorkbenchMessages.format("Capability.nameMissing", new Object[] {id}); //$NON-NLS-1$
	}
	
	public ImageDescriptor getIconDescriptor() {
		if (icon == null && isValid()) {
			IExtension extension = element.getDeclaringExtension();
			String location = element.getAttribute(ATT_ICON);
			if (location != null && location.length() > 0)
				icon = WorkbenchImages.getImageDescriptorFromExtension(extension, location);
		}
		return icon;
	}
	
	/**
	 * Returns the nature descriptor or <code>null</code> if
	 * none exist.
	 */
	public IProjectNatureDescriptor getNatureDescriptor() {
		return natureDescriptor;
	}
	
	public String getNatureId() {
		return natureId;
	}
	
	/* (non-Javadoc)
	 * Method declared on IAdaptable.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) 
			return this;
		else
			return null;
	}
	
	public String getCategoryPath() {
		if (element == null)
			return ""; //$NON-NLS-1$;
		else
			return element.getAttribute(ATT_CATEGORY);
	}
	
	/**
	 * Returns a new instance of the capability install
	 * wizard. Caller is responsible for calling the init
	 * method. If the wizard cannot be created, <code>null</code>
	 * is returned.
	 * 
	 * @return the non-initialized capability wizard or
	 * 		<code>null</code> if the wizard cannot be created.
	 */
	public ICapabilityInstallWizard getInstallWizard() {
		if (!isValid())
			return null;
			
		try {
			return (ICapabilityInstallWizard)element.createExecutableExtension(ATT_INSTALL_WIZARD);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Could not create capability install wizard.", e.getStatus()); //$NON-NLS-1$
			return null;
		}
	}
	
	/**
	 * Returns the description for the install wizard
	 * or <code>null</code> if none supplied.
	 */
	public String getInstallDetails() {
		if (!isValid())
			return null;
		return element.getAttribute(ATT_INSTALL_DETAILS);
	}
	
	/**
	 * Returns a new instance of the capability uninstall
	 * wizard. Caller is responsible for calling the init
	 * method. If the wizard cannot be created, <code>null</code>
	 * is returned.
	 * 
	 * @return the non-initialized capability wizard or
	 * 		<code>null</code> if the wizard cannot be created.
	 */
	public ICapabilityUninstallWizard getUninstallWizard() {
		if (!isValid())
			return null;
			
		try {
			return (ICapabilityUninstallWizard)element.createExecutableExtension(ATT_UNINSTALL_WIZARD);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Could not create capability uninstall wizard.", e.getStatus()); //$NON-NLS-1$
			return null;
		}
	}
	
	/**
	 * Returns the description for the uninstall wizard
	 * or <code>null</code> if none supplied.
	 */
	public String getUninstallDetails() {
		if (!isValid())
			return null;
		return element.getAttribute(ATT_UNINSTALL_DETAILS);
	}
	
	public String getDescription() {
		if (!isValid())
			return ""; //$NON-NLS-1$;
		String description = element.getAttribute(ATT_DESCRIPTION);
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}
	
	/**
	 * Returns a list of ids of other capabilities for which this 
	 * capability handles the user interface, or <code>null</code>
	 * if not applicable.
	 */
	public ArrayList getHandleUIs() {
		return handleUIs;	
	}
	
	/**
	 * Returns a list of ids of perspectives for which this 
	 * capability wants to present as choices, or <code>null</code>
	 * if not applicable.
	 */
	public ArrayList getPerspectiveChoices() {
		return perspectiveChoices;	
	}
	
	/**
	 * Returns whether this capability is valid
	 */
	public boolean isValid() {
		return natureDescriptor != null;
	}
}
