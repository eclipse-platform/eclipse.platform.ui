/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The FiltersContentProvider provides the elements for use by the list dialog
 * for selecting the patterns to apply.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
/* package */class FiltersContentProvider implements IStructuredContentProvider {

	private static List<String> definedFilters;

	private static List<String> defaultFilters;

	private ResourcePatternFilter resourceFilter;

	/**
	 * Create a FiltersContentProvider using the selections from the supplied
	 * resource filter.
	 *
	 * @param filter the resource pattern filter
	 */
	public FiltersContentProvider(ResourcePatternFilter filter) {
		this.resourceFilter = filter;
	}

	@Override
	public void dispose() {
	}

	/**
	 * Returns the filters which are enabled by default.
	 *
	 * @return a list of strings
	 */
	public static List<String> getDefaultFilters() {
		if (defaultFilters == null) {
			readFilters();
		}
		return defaultFilters;
	}

	/**
	 * Returns the filters currently defined for the navigator.
	 *
	 * @return a list of strings
	 */
	public static List<String> getDefinedFilters() {
		if (definedFilters == null) {
			readFilters();
		}
		return definedFilters;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getDefinedFilters().toArray();
	}

	/**
	 * Return the initially selected elements.
	 *
	 * @return an array with the initial selections
	 */
	public String[] getInitialSelections() {
		return this.resourceFilter.getPatterns();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Reads the filters currently defined for the workbench.
	 */
	private static void readFilters() {
		definedFilters = new ArrayList<>();
		defaultFilters = new ArrayList<>();
		IExtensionPoint extension = Platform.getExtensionRegistry()
				.getExtensionPoint(IDEWorkbenchPlugin.IDE_WORKBENCH + '.' + ResourcePatternFilter.FILTERS_TAG);
		if (extension != null) {
			for (IExtension currentExtension : extension.getExtensions()) {
				IConfigurationElement[] configElements = currentExtension.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					String pattern = configElement.getAttribute("pattern");//$NON-NLS-1$
					if (pattern != null) {
						definedFilters.add(pattern);
					}
					String selected = configElement.getAttribute("selected");//$NON-NLS-1$
					if (selected != null && selected.equalsIgnoreCase("true")) { //$NON-NLS-1$
						defaultFilters.add(pattern);
					}
				}

			}
		}
	}
}
