/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PreferenceTransferRegistryReader;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Instances represent registered preference transfers.
 * 
 * @since 3.1
 */
public class PreferenceTransferElement extends WorkbenchAdapter implements
		IPluginContribution {
	private String id;

	private ImageDescriptor imageDescriptor;

	private IConfigurationElement configurationElement;

	private IPreferenceFilter filter;

	/**
	 * Create a new instance of this class
	 * 
	 * @param configurationElement
	 * 
	 */
	public PreferenceTransferElement(IConfigurationElement configurationElement) {
		this.configurationElement = configurationElement;
		id = configurationElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	/**
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	/**
	 * Answer the preference filter of this element.
	 * 
	 * @return a preference filter
	 * @throws CoreException
	 */
	public IPreferenceFilter getFilter() throws CoreException {
		if (filter == null) {
			IConfigurationElement[] mappingConfigurations = PreferenceTransferRegistryReader
					.getMappings(configurationElement);
			int size = mappingConfigurations.length;
			Set scopes = new HashSet(size);
			Map mappingsMap = new HashMap(size);
			for (int i = 0; i < size; i++) {
				String scope = PreferenceTransferRegistryReader
						.getScope(mappingConfigurations[i]);
				scopes.add(scope);

				Map mappings;
				if (!mappingsMap.containsKey(scope)) {
					mappings = new HashMap(size);
					mappingsMap.put(scope, mappings);
				} else {
					mappings = (Map) mappingsMap.get(scope);
					if (mappings == null) {
						continue;
					}
				}

				Map entries = PreferenceTransferRegistryReader
						.getEntry(mappingConfigurations[i]);
				if (entries == null) {
					mappingsMap.put(scope, null);
				} else {
					mappings.putAll(entries);
				}
			}
			filter = new PreferenceFilter((String[]) scopes
					.toArray(new String[scopes.size()]), mappingsMap);
		}
		return filter;
	}

	/**
	 * Answer the description parameter of this element
	 * 
	 * @return java.lang.String
	 */
	public String getDescription() {
		return RegistryReader.getDescription(configurationElement);
	}

	/**
	 * Answer the id as specified in the extension.
	 * 
	 * @return java.lang.String
	 */
	public String getID() {
		return id;
	}

	/**
	 * Returns the name of this preference transfer element.
	 * 
	 * @return the name of the element
	 */
	public String getName() {
		return configurationElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return (configurationElement != null) ? configurationElement
				.getContributor().getName() : null;
	}

	class PreferenceFilter implements IPreferenceFilter {

		private String[] scopes;
		private Map mappings;

		public PreferenceFilter(String[] scopes, Map mappings) {
			this.scopes = scopes;
			this.mappings = mappings;
		}

		public String[] getScopes() {
			return scopes;
		}

		public Map getMapping(String scope) {
			return (Map) mappings.get(scope);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object object) {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object
	 * )
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (imageDescriptor == null) {
			String iconName = configurationElement
					.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
			if (iconName == null) {
				return null;
			}
			imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
					getPluginId(), iconName);
		}
		return imageDescriptor;

	}
}
