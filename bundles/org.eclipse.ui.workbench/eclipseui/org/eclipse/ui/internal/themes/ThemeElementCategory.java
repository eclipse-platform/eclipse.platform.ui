/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.themes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.themes.IThemePreview;

/**
 * @since 3.0
 */
public class ThemeElementCategory implements IPluginContribution, IThemeElementDefinition {

	private String description;

	private IConfigurationElement element;

	private String id;

	private String parentId;

	private String label;

	private String pluginId;

	public ThemeElementCategory(String label, String id, String parentId, String description, String pluginId,
			IConfigurationElement element) {

		this.label = label;
		this.id = id;
		this.parentId = parentId;
		this.description = description;
		this.pluginId = pluginId;
		this.element = element;
	}

	/**
	 * @return Returns the <code>IColorExample</code> for this category. If one is
	 *         not available, <code>null</code> is returned.
	 * @throws CoreException thrown if there is a problem instantiating the preview
	 */
	public IThemePreview createPreview() throws CoreException {
		String classString = element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
		if (classString == null || "".equals(classString)) { //$NON-NLS-1$
			return null;
		}
		return (IThemePreview) WorkbenchPlugin.createExtension(element, IWorkbenchRegistryConstants.ATT_CLASS);
	}

	/**
	 * @return Returns the description.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the element.
	 */
	public IConfigurationElement getElement() {
		return element;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return label;
	}

	@Override
	public String getLocalId() {
		return id;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return Returns the parentId. May be <code>null</code>.
	 */
	public String getParentId() {
		return parentId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ThemeElementCategory) {
			return getId().equals(((ThemeElementCategory) obj).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
