/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;


/**
 * A local registry that shares its resources with some global registry.
 * LocalResourceManager is typically used to safeguard against leaks. Clients
 * can use a nested registry to allocate and deallocate resources in the
 * global registry. Calling dispose() on the nested registry will deallocate
 * everything allocated for the nested registry without affecting the rest
 * of the global registry.
 * <p>
 * A nested registry can be used to manage the resources for, say, a dialog
 * box.
 * </p>
 * @since 3.1
 */
public final class LocalResourceManager extends AbstractResourceManager {

	private ResourceManager parentRegistry;

	/**
	 * Creates a local registry that delegates to the given global registry
	 * for all resource allocation and deallocation.
	 *
	 * @param parentRegistry global registry
	 */
	public LocalResourceManager(ResourceManager parentRegistry) {
		this.parentRegistry = parentRegistry;
	}

	/**
	 * Creates a local registry that wraps the given global registry. Anything
	 * allocated by this registry will be automatically cleaned up with the given
	 * control is disposed. Note that registries created in this way should not
	 * be used to allocate any resource that must outlive the given control.
	 *
	 * @param parentRegistry global registry that handles resource allocation
	 * @param owner control whose disposal will trigger cleanup of everything
	 * in the registry.
	 */
	public LocalResourceManager(ResourceManager parentRegistry, Control owner) {
		this(parentRegistry);

		owner.addDisposeListener(e -> LocalResourceManager.this.dispose());
	}

	@Override
	public Device getDevice() {
		return parentRegistry.getDevice();
	}

	@Override
	protected <R> R allocate(DeviceResourceDescriptor<R> descriptor) throws DeviceResourceException {
		return parentRegistry.create(descriptor);
	}

	@Override
	protected <R> void deallocate(Object resource, DeviceResourceDescriptor<R> descriptor) {
		parentRegistry.destroy(descriptor);
	}

	@Override
	protected Image getDefaultImage() {
		return parentRegistry.getDefaultImage();
	}
}
