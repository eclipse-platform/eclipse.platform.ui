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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of ResourceManager. Maintains reference counts for all previously
 * allocated SWT resources. Delegates to the abstract method allocate(...) the first time a resource
 * is referenced and delegates to the abstract method deallocate(...) the last time a reference is
 * removed.
 *
 * @since 3.1
 */
abstract class AbstractResourceManager extends ResourceManager {

	/**
	 * Map of ResourceDescriptor onto RefCount. (null when empty)
	 */
	private Map<DeviceResourceDescriptor<?>, RefCount<?>> map = null;

	/**
	 * Holds a reference count for a previously-allocated resource
	 */
	private static class RefCount<R> {
		final R resource;
		int count = 1;

		RefCount(R resource) {
			this.resource = resource;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <R> RefCount<R> getRefCount(DeviceResourceDescriptor<R> descriptor) {
		return (RefCount) map.get(descriptor);
	}

	/**
	 * Called the first time a resource is requested. Should allocate and return a resource
	 * of the correct type.
	 *
	 * @since 3.1
	 *
	 * @param descriptor identifier for the resource to allocate
	 * @return the newly allocated resource
	 * @throws DeviceResourceException Thrown when allocation of an SWT device resource fails
	 */
	protected abstract <R> R allocate(DeviceResourceDescriptor<R> descriptor) throws DeviceResourceException;

	/**
	 * Called the last time a resource is dereferenced. Should release any resources reserved by
	 * allocate(...).
	 *
	 * @since 3.1
	 *
	 * @param resource resource being deallocated
	 * @param descriptor identifier for the resource
	 */
	protected abstract <R> void deallocate(Object resource, DeviceResourceDescriptor<R> descriptor);

	@Override
	public final <R> R create(DeviceResourceDescriptor<R> descriptor) throws DeviceResourceException {

		// Lazily allocate the map
		if (map == null) {
			map = new HashMap<>();
		}

		// Get the current reference count
		RefCount<R> count = getRefCount(descriptor);
		if (count != null) {
			// If this resource already exists, increment the reference count and return
			// the existing resource.
			count.count++;
			return count.resource;
		}

		// Allocate and return a new resource (with ref count = 1)
		R resource = allocate(descriptor);

		count = new RefCount<>(resource);
		map.put(descriptor, count);

		return resource;
	}

	@Override
	public final <R> void destroy(DeviceResourceDescriptor<R> descriptor) {
		// If the map is empty (null) then there are no resources to dispose
		if (map == null) {
			return;
		}

		// Find the existing resource
		RefCount<R> count = getRefCount(descriptor);
		if (count != null) {
			// If the resource exists, decrement the reference count.
			count.count--;
			if (count.count == 0) {
				// If this was the last reference, deallocate it.
				deallocate(count.resource, descriptor);
				map.remove(descriptor);
			}
		}

		// Null out the map when empty to save a small amount of memory
		if (map.isEmpty()) {
			map = null;
		}
	}

	/**
	 * Deallocates any resources allocated by this registry that have not yet been
	 * deallocated.
	 *
	 * @since 3.1
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (map == null) {
			return;
		}
		map.forEach((key, val) -> deallocate(val.resource, key));
		map = null;
	}

	@Override
	public <R> R find(DeviceResourceDescriptor<R> descriptor) {
		if (map == null) {
			return null;
		}
		RefCount<R> refCount = getRefCount(descriptor);
		if (refCount == null) {
			return null;
		}
		return refCount.resource;
	}
}
