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
 
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class is used to read project capabilities and capability
 * categories from the platform's plugin registry and store the results in
 * a capability registry.
 */
public class CapabilityRegistryReader extends RegistryReader{	
	private static final String TAG_CAPABILITY = "capability"; //$NON-NLS-1$
	private static final String TAG_CATEGORY = "category"; //$NON-NLS-1$
	private static final String TAG_HANDLE_UI = "handleUI"; //$NON-NLS-1$
	private static final String TAG_PERSPECTIVE_CHOICE = "perspectiveChoice"; //$NON-NLS-1$
	
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	
	private CapabilityRegistry capabilityRegistry;
	private Capability currentCapability;
	
	/**
	 * Reads an element of the plugin registry and adds it to the capability
	 * registry if it is a capability or category (or a handleUI element, a
	 * child of a capability. A handleUI element represents another capability
	 * for which this capability controls the user interface).
	 */
	protected boolean readElement(IConfigurationElement element) {
		String name = element.getName();
		if (name.equals(TAG_CAPABILITY))
			return readCapability(element);
		if (name.equals(TAG_CATEGORY))
			return readCategory(element);
		if (name.equals(TAG_HANDLE_UI))
			return readHandleUI(element);
		if (name.equals(TAG_PERSPECTIVE_CHOICE))
			return readPerspectiveChoice(element);
		return false;
	}
	
	/**
	 * Reads a capability and adds it to the capability registry. Reads all
	 * children elements (which will be handleUI elements) as well and adds
	 * them to the capability.
	 */
	private boolean readCapability(IConfigurationElement element) {
		try {
			Capability capability = new Capability(element, this);
			capabilityRegistry.addCapability(capability);
			currentCapability = capability;
			readElementChildren(element);
			currentCapability = null;
			return true;
		} catch(WorkbenchException e) {
			currentCapability = null;
			return false;
		}
	}

	/**
	 * Reads a capability category and adds it to the capability registry.
	 */
	private boolean readCategory(IConfigurationElement element) {
		try {
			Category category = new Category(element);
			capabilityRegistry.addCategory(category);
		} catch (WorkbenchException e) {
			// log an error since its not safe to show a dialog here
			WorkbenchPlugin.log("Unable to create capability category. ", e.getStatus()); //$NON-NLS-1$
		}
		return true;		
	}
	
	/**
	 * Reads handleUI elements. These elements contain the ids of other
	 * capabilities. The capability that is the parent of the handleUI element
	 * controls the user interface for the capabilities whose ids are stored
	 * in the handleUI element.
	 */
	private boolean readHandleUI(IConfigurationElement element) {
		String capabilityId = element.getAttribute(ATT_ID);
		
		if (capabilityId == null) {
			logMissingAttribute(element, ATT_ID);
		}
		
		if (currentCapability != null)
			currentCapability.addHandleUI(capabilityId);		
		return true;	
	}
	
	/**
	 * Reads perspectiveChoice elements. These elements contain the ids of
	 * perspectives.
	 */
	private boolean readPerspectiveChoice(IConfigurationElement element) {
		String perspId = element.getAttribute(ATT_ID);
		
		if (perspId == null) {
			logMissingAttribute(element, ATT_ID);
		}
		
		if (currentCapability != null)
			currentCapability.addPerspectiveChoice(perspId);		
		return true;	
	}
	
	/**
	 * Reads project capabilities and capability categories from the provided
	 * plugin registry and stores them in the provided capability registry.
	 */
	public void read(IPluginRegistry registry, CapabilityRegistry out) {
		capabilityRegistry = out;
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_CAPABILITIES);
	}
}
