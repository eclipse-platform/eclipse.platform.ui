/*******************************************************************************
 * Copyright (c) 2021 Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.pde.api.tools.annotations.NoReference;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * A LRU based ResourceManager Wrapper. Not to be used by clients.
 */
@NoReference
public class LazyResourceManager extends ResourceManager {
	/**
	 * This LRU Map only holds the DeviceResourceDescriptors which are not
	 * referenced otherwise anymore. The Resources itself are only cached by the
	 * parent ResourceManager.
	 */
	private static class LruMap extends LinkedHashMap<DeviceResourceDescriptor, ResourceManager> {
		private static final long serialVersionUID = 1L;
		int cacheSize;

		LruMap(int cacheSize) {
			super(cacheSize, 0.75f, true); // last access-order
			this.cacheSize = cacheSize;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<DeviceResourceDescriptor, ResourceManager> eldest) {
			boolean remove = size() > cacheSize;
			if (remove) {
				// destroy resource which was not used recently:
				eldest.getValue().destroy(eldest.getKey());
			}
			return remove;
		}
	}

	private final ResourceManager parent;
	private final LruMap unreferenced;
	private final Map<DeviceResourceDescriptor, Integer> refCount;

	/**
	 * @param cacheSize the lru cache size
	 * @param parent    ResourceManager
	 */
	public LazyResourceManager(int cacheSize, ResourceManager parent) {
		this.parent = parent;
		this.unreferenced = new LruMap(cacheSize);
		this.refCount = new HashMap<>();
	}

	@Override
	public Device getDevice() {
		return parent.getDevice();
	}

	/**
	 * @param descriptor
	 * @return if a resource based on this descriptor should be cached.
	 */
	private boolean shouldBeCached(DeviceResourceDescriptor descriptor) {
		return descriptor != null && descriptor.shouldBeCached();
	}

	@Override
	protected Image getDefaultImage() {
		return parent.getDefaultImage();
	}

	private static Integer createOrIncrease(@SuppressWarnings("unused") DeviceResourceDescriptor key, Integer refs) {
		return Integer.valueOf(refs == null ? 1 : refs.intValue() + 1);
	}

	private static Integer decreaseOrRemove(@SuppressWarnings("unused") DeviceResourceDescriptor key, Integer refs) {
		return refs.intValue() == 1 ? null : Integer.valueOf(refs.intValue() - 1);

	}
	@Override
	public Object create(DeviceResourceDescriptor descriptor) {
		if (!shouldBeCached(descriptor)) {
			return parent.create(descriptor);
		}
		int updatedRefs = refCount.compute(descriptor, LazyResourceManager::createOrIncrease).intValue();
		if (updatedRefs == 1) {
			ResourceManager cached = unreferenced.remove(descriptor);
			if (cached == null) {
				return parent.create(descriptor);
			}
			// referenced again
		} else {
			assert !unreferenced.containsKey(descriptor);
		}
		return parent.find(descriptor);
	}

	@Override
	public void destroy(DeviceResourceDescriptor descriptor) {
		if (!shouldBeCached(descriptor)) {
			parent.destroy(descriptor);
			return;
		}
		Integer refsLeft = refCount.computeIfPresent(descriptor, LazyResourceManager::decreaseOrRemove);
		if (refsLeft == null) {
			// defer destroy:
			ResourceManager old = unreferenced.put(descriptor, parent);
			assert old == null;
		}
	}

	@Override
	public Object find(DeviceResourceDescriptor descriptor) {
		if (!shouldBeCached(descriptor)) {
			return parent.find(descriptor);
		}
		if (refCount.containsKey(descriptor)) {
			return parent.find(descriptor);
		}
		return null;
	}

}
