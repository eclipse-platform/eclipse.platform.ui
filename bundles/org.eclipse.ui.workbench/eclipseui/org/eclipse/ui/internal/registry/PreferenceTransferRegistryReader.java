/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 208564
 *******************************************************************************/

package org.eclipse.ui.internal.registry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.PreferenceFilterEntry;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.PreferenceTransferElement;

/**
 * Preference Transfer registry reader to read extenders of the
 * preferenceTransfer schema.
 *
 * @since 3.1
 */
public class PreferenceTransferRegistryReader extends RegistryReader {
	private List<PreferenceTransferElement> preferenceTransfers;

	private String pluginPoint;

	/**
	 * Create an instance of this class.
	 *
	 * @param pluginPointId java.lang.String
	 */
	public PreferenceTransferRegistryReader(String pluginPointId) {
		pluginPoint = pluginPointId;
	}

	/**
	 * Returns a new PreferenceTransferElement configured according to the
	 * parameters contained in the passed element.
	 *
	 * @param element the configuration element
	 * @return the preference transfer element or <code>null</code> if there was not
	 *         enough information in the element
	 */
	protected PreferenceTransferElement createPreferenceTransferElement(IConfigurationElement element) {
		// PreferenceTransfers must have a class attribute
		if (element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_NAME);
			return null;
		}

		// must specify a mapping
		if (element.getChildren(IWorkbenchRegistryConstants.TAG_MAPPING) == null) {
			logMissingElement(element, IWorkbenchRegistryConstants.TAG_MAPPING);
			return null;
		}

		return new PreferenceTransferElement(element);
	}

	/**
	 * Returns a sorted list of preference transfers.
	 *
	 * @return an array of <code>IPreferenceTransfer</code> objects
	 */
	public PreferenceTransferElement[] getPreferenceTransfers() {
		readPreferenceTransfers();
		PreferenceTransferElement[] transfers = new PreferenceTransferElement[preferenceTransfers.size()];
		preferenceTransfers.sort((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();

			return Collator.getInstance().compare(name1, name2);
		});
		preferenceTransfers.toArray(transfers);
		return transfers;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(IWorkbenchRegistryConstants.TAG_TRANSFER)) {

			PreferenceTransferElement transfer = createPreferenceTransferElement(element);
			if (transfer != null)
				preferenceTransfers.add(transfer);
			return true;
		}

		// Allow settings transfers as well.

		return element.getName().equals(IWorkbenchRegistryConstants.TAG_SETTINGS_TRANSFER);
	}

	/**
	 * Reads the wizards in a registry.
	 */
	protected void readPreferenceTransfers() {
		preferenceTransfers = new ArrayList<>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		readRegistry(registry, WorkbenchPlugin.PI_WORKBENCH, pluginPoint);
	}

	/**
	 * Get the preference mappings.
	 *
	 * @return the child configuration elements
	 */
	public static IConfigurationElement[] getMappings(IConfigurationElement configElement) {
		IConfigurationElement[] children = configElement.getChildren(IWorkbenchRegistryConstants.TAG_MAPPING);
		if (children.length < 1) {
			logMissingElement(configElement, IWorkbenchRegistryConstants.TAG_MAPPING);
			return new IConfigurationElement[0];
		}
		return children;
	}

	/**
	 * @return the scope attribute for this element
	 */
	public static String getScope(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_SCOPE);
	}

	/**
	 * @param element the configuration element
	 * @return a map that maps nodes to keys for this element or <code>null</code>
	 *         for all nodes
	 */
	public static Map<String, PreferenceFilterEntry[]> getEntry(IConfigurationElement element) {
		IConfigurationElement[] entries = element.getChildren(IWorkbenchRegistryConstants.TAG_ENTRY);
		if (entries.length == 0) {
			return null;
		}
		Map<String, PreferenceFilterEntry[]> map = new HashMap<>(entries.length);
		for (IConfigurationElement entry : entries) {
			IConfigurationElement[] keys = entry.getChildren(IWorkbenchRegistryConstants.ATT_KEY);
			PreferenceFilterEntry[] prefFilters = null;
			if (keys.length > 0) {
				prefFilters = new PreferenceFilterEntry[keys.length];
				for (int j = 0; j < keys.length; j++) {
					IConfigurationElement keyElement = keys[j];
					prefFilters[j] = new PreferenceFilterEntry(
							keyElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME),
							keyElement.getAttribute(IWorkbenchRegistryConstants.ATT_MATCH_TYPE));
				}
			}
			map.put(entry.getAttribute(IWorkbenchRegistryConstants.ATT_NODE), prefFilters);
		}
		return map;
	}
}
