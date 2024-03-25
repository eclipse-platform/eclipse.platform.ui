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
 *     Hannes Wellmann - Parameterize DeviceResourceDescriptor with the described resource type
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;

/**
 * Instances of this class can allocate and dispose SWT resources. Each instance describes a
 * particular resource (such as a Color, Font, or Image) and can create and destroy that resource on
 * demand. DeviceResourceDescriptors are managed by a ResourceRegistry.
 * <p>
 * <strong>Note:</strong> It is recommended that subclasses implement <code>#equals</code> and
 * <code>#hashCode</code>, so that clients, like decoration managers, can recognize when they have
 * two equal descriptors at hand, e.g. decorating an identical object.
 * </p>
 *
 * @param <R> The resource's type described by this descriptor
 *
 * @see org.eclipse.jface.resource.ResourceManager
 *
 * @since 3.1
 */
public abstract class DeviceResourceDescriptor<R> {
	private final boolean shouldBeCached;

	/**
	 * default constructor with shouldBeCached=false
	 */
	public DeviceResourceDescriptor() {
		this(false);
	}

	/**
	 * @param shouldBeCached Indicates if the resource instance described by the
	 *                       descriptor should to be kept by {@link ResourceManager}
	 *                       even if all references to the resource are lost (due to
	 *                       {@link ResourceManager#destroy(DeviceResourceDescriptor)}).<br>
	 *                       Should return true for resources that are costly to
	 *                       create (for example by involving I/O Operation). Has
	 *                       only an effect if caching is enabled (see
	 *                       org.eclipse.jface.resource.JFaceResources#cacheSize).
	 *                       Caching relies on {@link #equals(Object)} and
	 *                       {@link #hashCode()}. For equal
	 *                       DeviceResourceDescriptors the same Resource instance is
	 *                       returned by the {@link ResourceManager} instance return
	 *                       by
	 *                       {@link org.eclipse.jface.resource.JFaceResources#getResources(org.eclipse.swt.widgets.Display)}
	 *                       as long as the cache is big enough to cache all
	 *                       resources.<br>
	 *                       Instances which equal (in terms of
	 *                       {@link #equals(Object)}) must have the same
	 *                       shouldBeCached mode.
	 * @see LazyResourceManager
	 * @since 3.24
	 */
	protected DeviceResourceDescriptor(boolean shouldBeCached) {
		this.shouldBeCached = shouldBeCached;

	}

	final boolean shouldBeCached() {
		return shouldBeCached;
	}

	/**
	 * Creates the resource described by this descriptor
	 *
	 * @since 3.1
	 *
	 * @param device the Device on which to allocate the resource
	 * @return the newly allocated resource (not null)
	 * @throws DeviceResourceException if unable to allocate the resource
	 */
	public abstract Object createResource(Device device);

	/**
	 * Undoes everything that was done by a previous call to create(...), given
	 * the object that was returned by create(...).
	 *
	 * @since 3.1
	 *
	 * @param previouslyCreatedObject an object that was returned by an equal
	 * descriptor in a previous call to createResource(...).
	 */
	public abstract void destroyResource(Object previouslyCreatedObject);
}
