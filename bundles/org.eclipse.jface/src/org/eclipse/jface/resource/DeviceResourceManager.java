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

/**
 * Manages SWT resources for a particular device.
 *
 * <p>
 * IMPORTANT: in most cases clients should use a <code>LocalResourceManager</code> instead of a
 * <code>DeviceResourceManager</code>. To create a resource manager on a particular display,
 * use <code>new LocalResourceManager(JFaceResources.getResources(myDisplay))</code>.
 * <code>DeviceResourceManager</code> should only be used directly when managing
 * resources for a device other than a Display (such as a printer).
 * </p>
 *
 * @see LocalResourceManager
 *
 * @since 3.1
 */
public final class DeviceResourceManager extends AbstractResourceManager {

	private final Device device;
	private Image missingImage;

	@Override
	public Device getDevice() {
		return device;
	}

	/**
	 * Creates a new registry for the given device.
	 *
	 * @param device device to manage
	 */
	public DeviceResourceManager(Device device) {
		this.device = device;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <R> R allocate(DeviceResourceDescriptor<R> descriptor) throws DeviceResourceException {
		return (R) descriptor.createResource(device);
	}

	@Override
	protected <R> void deallocate(Object resource, DeviceResourceDescriptor<R> descriptor) {
		descriptor.destroyResource(resource);
	}

	@Override
	protected Image getDefaultImage() {
		if (missingImage == null) {
			missingImage = ImageDescriptor.getMissingImageDescriptor().createImage();
		}
		return missingImage;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (missingImage != null) {
			missingImage.dispose();
			missingImage = null;
		}
	}
}
