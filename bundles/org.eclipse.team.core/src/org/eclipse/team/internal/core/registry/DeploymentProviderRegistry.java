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
package org.eclipse.team.internal.core.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.core.TeamPlugin;

public class DeploymentProviderRegistry extends RegistryReader {
	
	private final static String PT_TEAMPROVIDER = "deployment"; //$NON-NLS-1$
	private Map providers = new HashMap();
	private String extensionId;
	
	public DeploymentProviderRegistry() {
		super();
		this.extensionId = PT_TEAMPROVIDER;
		readRegistry(Platform.getPluginRegistry(), TeamPlugin.ID, PT_TEAMPROVIDER);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(extensionId)) {
			String descText = getDescription(element);
			DeploymentProviderDescriptor desc;
			try {
				desc = new DeploymentProviderDescriptor(element, descText);
				providers.put(desc.getId(), desc);
			} catch (CoreException e) {
				TeamPlugin.log(e);
			}
			return true;
		}
		return false;
	}
	
	public DeploymentProviderDescriptor[] getTeamProviderDescriptors() {
		return (DeploymentProviderDescriptor[])providers.values().toArray(new DeploymentProviderDescriptor[providers.size()]);
	}
	
	public DeploymentProviderDescriptor find(String id) {
		return (DeploymentProviderDescriptor)providers.get(id);
	}
}
