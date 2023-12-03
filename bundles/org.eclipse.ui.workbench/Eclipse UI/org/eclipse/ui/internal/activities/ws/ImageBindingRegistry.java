/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.activities.Persistence;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.1
 */
public class ImageBindingRegistry implements IExtensionChangeHandler {
	private String tag;
	private ImageRegistry registry = new ImageRegistry();

	public ImageBindingRegistry(String tag) {
		super();
		this.tag = tag;
		IExtension[] extensions = getExtensionPointFilter().getExtensions();
		for (IExtension extension : extensions) {
			addExtension(PlatformUI.getWorkbench().getExtensionTracker(), extension);
		}
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			if (element.getName().equals(tag)) {
				String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				String file = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (file == null || id == null) {
					Persistence.log(element, Persistence.ACTIVITY_IMAGE_BINDING_DESC,
							"definition must contain icon and ID"); //$NON-NLS-1$
					continue; // ignore - malformed
				}
				if (registry.getDescriptor(id) == null) { // first come, first serve
					ResourceLocator.imageDescriptorFromBundle(element.getContributor().getName(), file).ifPresent(d -> {
						registry.put(id, d);
						tracker.registerObject(extension, id, IExtensionTracker.REF_WEAK);
					});
				}
			}
		}

	}

	/**
	 * Return the activity support extension point that this registry is interested
	 * in.
	 *
	 * @return the extension point
	 */
	public IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT);
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof String) {
				registry.remove((String) object);
			}
		}
	}

	/**
	 * Get the ImageDescriptor for the given id.
	 *
	 * @param id the id
	 * @return the descriptor
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return registry.getDescriptor(id);
	}

	/**
	 * Dispose of this registry.
	 */
	void dispose() {
		registry.dispose();
	}

}
