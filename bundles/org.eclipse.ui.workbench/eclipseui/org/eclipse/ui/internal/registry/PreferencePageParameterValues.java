/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.registry;

import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * Provides the parameter values for the show preferences command.
 * </p>
 * <p>
 * To disambiguate preference pages with the same local label, names are
 * constructed incorporating the full path of preference page labels. For
 * instance names like <code>General &gt; Appearance</code> and
 * <code>Java &gt; Appearance</code> avoid the problem of trying to put two
 * <code>Appearance</code> keys into the parameter values map.
 * </p>
 * <p>
 * This is only intended for use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public final class PreferencePageParameterValues implements IParameterValues {

	public PreferencePageParameterValues() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> {
			if (event.getExtensionDeltas(PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_PREFERENCES).length > 0) {
				preferenceMap = null;
			}
		});
	}

	private Map preferenceMap;

	/**
	 * Iterate through the preference page and build the map of preference page
	 * names to ids.
	 *
	 * @param values          The Map being populated with parameter values.
	 * @param preferenceNodes An array of <code>IPreferenceNode</code> to process.
	 * @param namePrefix      A string incorporating the names of each parent
	 *                        <code>IPreferenceNode</code> up to the root of the
	 *                        preference page tree. This will be <code>null</code>
	 *                        for the root level preference page nodes.
	 */
	private void collectParameterValues(final Map values, final IPreferenceNode[] preferenceNodes,
			final String namePrefix) {

		for (final IPreferenceNode preferenceNode : preferenceNodes) {
			final String name;
			if (namePrefix == null) {
				name = preferenceNode.getLabelText();
			} else {
				name = namePrefix + WorkbenchMessages.PreferencePageParameterValues_pageLabelSeparator
						+ preferenceNode.getLabelText();
			}
			final String value = preferenceNode.getId();
			values.put(name, value);

			collectParameterValues(values, preferenceNode.getSubNodes(), name);
		}
	}

	@Override
	public Map getParameterValues() {
		if (preferenceMap == null) {
			preferenceMap = new TreeMap();

			final PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
			collectParameterValues(preferenceMap, preferenceManager.getRootSubNodes(), null);
		}

		return preferenceMap;
	}

}
