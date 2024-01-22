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
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.views.IStickyViewDescriptor;

/**
 * @since 3.0
 */
public class StickyViewDescriptor implements IStickyViewDescriptor, IPluginContribution {

	private IConfigurationElement configurationElement;

	private String id;

	/**
	 * Folder constant for right sticky views.
	 */
	public static final String STICKY_FOLDER_RIGHT = "stickyFolderRight"; //$NON-NLS-1$


	public StickyViewDescriptor(IConfigurationElement element) throws CoreException {
		this.configurationElement = element;
		id = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id == null) {
			throw new CoreException(new Status(IStatus.ERROR, element.getContributor().getName(), 0,
					"Invalid extension (missing id) ", null));//$NON-NLS-1$
		}
	}

	/**
	 * Return the configuration element.
	 *
	 * @return the configuration element
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	@Override
	public int getLocation() {
		int direction = IPageLayout.RIGHT;

		String location = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_LOCATION);
		if (location != null) {
			if (location.equalsIgnoreCase("left")) { //$NON-NLS-1$
				direction = IPageLayout.LEFT;
			} else if (location.equalsIgnoreCase("top")) { //$NON-NLS-1$
				direction = IPageLayout.TOP;
			} else if (location.equalsIgnoreCase("bottom")) { //$NON-NLS-1$
				direction = IPageLayout.BOTTOM;
				// no else for right - it is the default value;
			}
		}
		return direction;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLocalId() {
		return id;
	}

	@Override
	public String getPluginId() {
		return configurationElement.getContributor().getName();
	}

	@Override
	public boolean isCloseable() {
		boolean closeable = true;
		String closeableString = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_CLOSEABLE);
		if (closeableString != null) {
			closeable = !closeableString.equals("false"); //$NON-NLS-1$
		}
		return closeable;
	}

	@Override
	public boolean isMoveable() {
		boolean moveable = true;
		String moveableString = configurationElement.getAttribute(IWorkbenchRegistryConstants.ATT_MOVEABLE);
		if (moveableString != null) {
			moveable = !moveableString.equals("false"); //$NON-NLS-1$
		}
		return moveable;
	}
}
