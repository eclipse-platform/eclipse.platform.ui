/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.PreferenceFilterEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PreferenceTransferRegistryReader;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Instances represent registered preference transfers.
 *
 * @since 3.1
 */
public class PreferenceTransferElement extends WorkbenchAdapter implements IPluginContribution {
	private String id;

	private ImageDescriptor imageDescriptor;

	private IConfigurationElement configurationElement;

	private IPreferenceFilter filter;

	/**
	 * Create a new instance of this class
	 */
	public PreferenceTransferElement(IConfigurationElement configurationElement) {
		this.configurationElement = configurationElement;
		id = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
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
	 */
	public IPreferenceFilter getFilter() {
		if (filter == null) {
			IConfigurationElement[] mappingConfigurations = PreferenceTransferRegistryReader
					.getMappings(configurationElement);
			int size = mappingConfigurations.length;
			Set<String> scopes = new HashSet<>(size);
			Map<String, Map<String, PreferenceFilterEntry[]>> mappingsMap = new HashMap<>(size);
			for (int i = 0; i < size; i++) {
				String scope = PreferenceTransferRegistryReader.getScope(mappingConfigurations[i]);
				scopes.add(scope);

				Map<String, PreferenceFilterEntry[]> mappings;
				if (!mappingsMap.containsKey(scope)) {
					mappings = new HashMap<>(size);
					mappingsMap.put(scope, mappings);
				} else {
					mappings = mappingsMap.get(scope);
					if (mappings == null) {
						continue;
					}
				}

				Map<String, PreferenceFilterEntry[]> entries = PreferenceTransferRegistryReader.getEntry(mappingConfigurations[i]);
				if (entries == null) {
					mappingsMap.put(scope, null);
				} else {
					mappings.putAll(entries);
				}
			}
			filter = new PreferenceFilter(scopes.toArray(new String[scopes.size()]), mappingsMap);
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
		return configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	@Override
	public String getLocalId() {
		return getID();
	}

	@Override
	public String getPluginId() {
		return (configurationElement != null) ? configurationElement.getContributor().getName() : null;
	}

	static class PreferenceFilter implements IPreferenceFilter {

		private String[] scopes;
		private Map<String, Map<String, PreferenceFilterEntry[]>> mappings;

		public PreferenceFilter(String[] scopes, Map<String, Map<String, PreferenceFilterEntry[]>> mappings) {
			this.scopes = scopes;
			this.mappings = mappings;
		}

		@Override
		public String[] getScopes() {
			return scopes;
		}

		@Override
		public Map<String, PreferenceFilterEntry[]> getMapping(String scope) {
			return mappings.get(scope);
		}

	}

	@Override
	public String getLabel(Object object) {
		return getName();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		if (imageDescriptor == null) {
			String iconName = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
			if (iconName == null) {
				return null;
			}
			imageDescriptor = ResourceLocator.imageDescriptorFromBundle(getPluginId(), iconName).orElse(null);
		}
		return imageDescriptor;

	}
}
