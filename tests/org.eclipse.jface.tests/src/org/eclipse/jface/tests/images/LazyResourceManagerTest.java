/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 490700
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LazyResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import junit.framework.TestCase;

public class LazyResourceManagerTest extends TestCase {
	private static class CachableTestDescriptor extends DeviceResourceDescriptor {
		CachableTestDescriptor() {
			super(true);
		}

		@Override
		public Object createResource(Device device) {
			return null;
		}

		@Override
		public void destroyResource(Object previouslyCreatedObject) {
		}
	}

	private static class UncachableTestDescriptor extends DeviceResourceDescriptor {
		UncachableTestDescriptor() {
			super(false);
		}

		@Override
		public Object createResource(Device device) {
			return null;
		}

		@Override
		public void destroyResource(Object previouslyCreatedObject) {
		}
	}

	private static class TestResourceManager extends ResourceManager {
		private Device device;
		private Map<DeviceResourceDescriptor, AtomicReference<DeviceResourceDescriptor>> objects = new HashMap<>();
		private Map<DeviceResourceDescriptor, Integer> refCount = new HashMap<>();
		private Image defaultImage;

		@Override
		public Device getDevice() {
			return device;
		}

		@Override
		public AtomicReference<DeviceResourceDescriptor> create(DeviceResourceDescriptor descriptor) {
			AtomicReference<DeviceResourceDescriptor> newInstance = new AtomicReference<>(descriptor);
			AtomicReference<DeviceResourceDescriptor> previous = objects.putIfAbsent(descriptor, newInstance);
			refCount.compute(descriptor, (k, refs) -> Integer.valueOf((refs == null ? 0 : refs.intValue()) + 1));
			return previous == null ? newInstance : previous;
		}

		@Override
		public void destroy(DeviceResourceDescriptor descriptor) {
			if (!refCount.containsKey(descriptor)) {
				throw new RuntimeException("not created");
			}
			int refs = refCount.get(descriptor).intValue();
			refs--;
			refCount.put(descriptor, Integer.valueOf(refs));
			if (refs == 0) {
				objects.remove(descriptor);
			}

		}

		@Override
		protected Image getDefaultImage() {
			return defaultImage;
		}

		@Override
		public AtomicReference<DeviceResourceDescriptor> find(DeviceResourceDescriptor descriptor) {
			return objects.get(descriptor);
		}

		public void setDevice(Device device) {
			this.device = device;
		}

		public void setDefaultImage(Image defaultImage) {
			this.defaultImage = defaultImage;
		}

	}

	public LazyResourceManagerTest(String name) {
		super(name);
	}

	public void testDefaultImage() {
		// note, we must touch the class to ensure the static initialer runs
		// so the image registry is up to date
		Dialog.getBlockedHandler();

		@SuppressWarnings("deprecation")
		String[] imageNames = new String[] { Dialog.DLG_IMG_ERROR, Dialog.DLG_IMG_INFO, Dialog.DLG_IMG_QUESTION,
				Dialog.DLG_IMG_WARNING, Dialog.DLG_IMG_MESSAGE_ERROR, Dialog.DLG_IMG_MESSAGE_INFO,
				Dialog.DLG_IMG_MESSAGE_WARNING };

		ImageRegistry reg = JFaceResources.getImageRegistry();

		TestResourceManager tst = new TestResourceManager();
		LazyResourceManager mgr = new LazyResourceManager(2, tst) {
			{
				// getDefaultImage() is not not public
				assertSame(tst.getDefaultImage(), getDefaultImage());
			}
		};
		for (String imageName : imageNames) {
			Image image = reg.get(imageName);
			tst.setDefaultImage(image);
			tst.setDevice(image.getDevice());
		}
		assertNotNull(tst.getDevice());
		assertSame(tst.getDevice(), mgr.getDevice());

	}

	public void testUncachable() {
		TestResourceManager tst = new TestResourceManager();
		LazyResourceManager mgr = new LazyResourceManager(2, tst);

		assertSame(tst.getDevice(), mgr.getDevice());
		DeviceResourceDescriptor descriptor1 = new UncachableTestDescriptor();
		{
			Object created1 = mgr.create(descriptor1);
			AtomicReference<DeviceResourceDescriptor> expected1 = tst.find(descriptor1);
			assertSame(expected1, created1);
			assertSame(expected1, mgr.find(descriptor1));
			mgr.destroy(descriptor1);
			assertDestroyed(expected1, mgr, tst, descriptor1);
		}
	}

	void assertCached(AtomicReference<DeviceResourceDescriptor> previousInstance, LazyResourceManager mgr,
			TestResourceManager tst, DeviceResourceDescriptor rd) {
		assertNotNull(previousInstance);
		assertNull(mgr.find(rd)); // destroyed but
		assertNotNull(tst.find(rd)); // cached
//		would be best to ask mgr to creat a new resource, but that would influence the LRU order:
//		Object created = mgr.create(rd);
//		mgr.destroy(rd);
		// assume mgr would create the instance given by parent tst:
		AtomicReference<DeviceResourceDescriptor> created = tst.find(rd);
		assertSame(previousInstance, created);
	}

	void assertAlife(AtomicReference<DeviceResourceDescriptor> previousInstance, LazyResourceManager mgr,
			TestResourceManager tst, DeviceResourceDescriptor rd) {
		assertNotNull(previousInstance);
		assertSame(previousInstance, mgr.find(rd));
		assertSame(previousInstance, tst.find(rd));
	}

	void assertDestroyed(AtomicReference<DeviceResourceDescriptor> previousInstance, LazyResourceManager mgr,
			TestResourceManager tst, DeviceResourceDescriptor rd) {
		assertNotNull(previousInstance);
		assertNull(mgr.find(rd)); // destroyed and
		assertNull(tst.find(rd)); // cached
//		would be best to ask mgr to creat a new resource, but that would influence the LRU order:
//		Object created = mgr.create(rd);
//		mgr.destroy(rd);
//		assertNotSame(previousInstance, created);
		// assume mgr would create the instance given by parent tst:
		AtomicReference<DeviceResourceDescriptor> created = tst.find(rd);
		assertNotSame(previousInstance, created);
	}

	/**
	 * Creates multiple resources for 2 Descriptors. Only 1 of them can be cached
	 **/
	@SuppressWarnings("unchecked")
	public void testLazyResourceManagerRefCounting() {
		TestResourceManager tst = new TestResourceManager();
		LazyResourceManager mgr = new LazyResourceManager(1, tst);

		assertSame(tst.getDevice(), mgr.getDevice());
		DeviceResourceDescriptor descriptor1 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected1;
		{
			expected1 = (AtomicReference<DeviceResourceDescriptor>) mgr.create(descriptor1); // create first ref
			assertAlife(expected1, mgr, tst, descriptor1);
			mgr.create(descriptor1); // create second ref
			assertAlife(expected1, mgr, tst, descriptor1);
			mgr.destroy(descriptor1); // destroy second
			assertAlife(expected1, mgr, tst, descriptor1);
			mgr.destroy(descriptor1); // destroy first
		}
		assertCached(expected1, mgr, tst, descriptor1);
		DeviceResourceDescriptor descriptor2 = new CachableTestDescriptor();
		{
			// exceeded cache capacity:
			mgr.create(descriptor2);
			mgr.destroy(descriptor2);
		}
		assertDestroyed(expected1, mgr, tst, descriptor1);
		{
			Object created1_ = mgr.create(descriptor1);
			AtomicReference<DeviceResourceDescriptor> expected1_ = tst.find(descriptor1); // == descriptor1
			assertNotSame(expected1, expected1_);
			assertSame((expected1).get(), (expected1_).get()); // same descriptor
			assertSame(expected1_, created1_);
			assertAlife(expected1_, mgr, tst, descriptor1);
			mgr.destroy(descriptor1);
			assertCached(expected1_, mgr, tst, descriptor1);
		}
	}

	/** Creates resources for 3 Descriptors. Only 2 of them can be cached **/
	public void testLazyResourceManager() {
		TestResourceManager tst = new TestResourceManager();
		LazyResourceManager mgr = new LazyResourceManager(2, tst);

		assertSame(tst.getDevice(), mgr.getDevice());
		DeviceResourceDescriptor descriptor1 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected1;
		{
			Object created1 = mgr.create(descriptor1);
			expected1 = tst.find(descriptor1);
			assertSame(expected1, created1);
			assertSame(expected1, mgr.find(descriptor1));
			mgr.destroy(descriptor1);
		}
		assertCached(expected1, mgr, tst, descriptor1);
		DeviceResourceDescriptor descriptor2 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected2;
		{
			Object created = mgr.create(descriptor2);
			expected2 = tst.find(descriptor2);
			assertSame(expected2, created);
			assertSame(expected2, mgr.find(descriptor2));
			mgr.destroy(descriptor2);
			assertCached(expected2, mgr, tst, descriptor2);
			mgr.create(descriptor2);
			mgr.destroy(descriptor2);
			assertCached(expected2, mgr, tst, descriptor2);
			mgr.create(descriptor2);
			mgr.destroy(descriptor2);
			assertCached(expected2, mgr, tst, descriptor2);
		}
		assertCached(expected1, mgr, tst, descriptor1);
		DeviceResourceDescriptor descriptor3 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected3;
		{
			Object created = mgr.create(descriptor3);
			expected3 = tst.find(descriptor3);
			assertSame(expected3, created);
			assertSame(expected3, mgr.find(descriptor3));
			mgr.destroy(descriptor3);
			assertCached(expected3, mgr, tst, descriptor3);
		}
		assertDestroyed(expected1, mgr, tst, descriptor1); // lru size exceeded: not cached anymore
		assertCached(expected2, mgr, tst, descriptor2);
		assertCached(expected3, mgr, tst, descriptor3);
		{
			Object created1_ = mgr.create(descriptor1);
			AtomicReference<DeviceResourceDescriptor> expected1_ = tst.find(descriptor1); // == descriptor1
			assertNotSame(expected1, expected1_); // different resources
			assertSame(((AtomicReference<?>) expected1).get(), ((AtomicReference<?>) expected1_).get()); // same
																											// descriptor
			assertSame(expected1_, created1_);
			assertSame(expected1_, mgr.find(descriptor1));
			mgr.destroy(descriptor1);
			assertCached(expected1_, mgr, tst, descriptor1);// cached again
		}
		assertCached(expected3, mgr, tst, descriptor3);
		assertDestroyed(expected2, mgr, tst, descriptor2); // lru size exceeded: not cached anymore
	}

	/**
	 * Creates resources for 3 Descriptors. Only the 2 last recently used should be
	 * cached
	 **/
	public void testLazyResourceManagerLRU() {
		TestResourceManager tst = new TestResourceManager();
		LazyResourceManager mgr = new LazyResourceManager(2, tst);

		assertSame(tst.getDevice(), mgr.getDevice());
		DeviceResourceDescriptor descriptor1 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected1;
		{
			Object created1 = mgr.create(descriptor1);
			expected1 = tst.find(descriptor1);
			assertSame(expected1, created1);
			assertSame(expected1, mgr.find(descriptor1));
			mgr.destroy(descriptor1);
		}
		assertCached(expected1, mgr, tst, descriptor1);
		DeviceResourceDescriptor descriptor2 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected2;
		{
			Object created = mgr.create(descriptor2);
			expected2 = tst.find(descriptor2);
			assertSame(expected2, created);
			assertSame(expected2, mgr.find(descriptor2));
			mgr.destroy(descriptor2);
			assertCached(expected2, mgr, tst, descriptor2);
		}
		assertCached(expected1, mgr, tst, descriptor1);
		DeviceResourceDescriptor descriptor3 = new CachableTestDescriptor();
		AtomicReference<DeviceResourceDescriptor> expected3;
		{
			Object created = mgr.create(descriptor3);
			expected3 = tst.find(descriptor3);
			assertSame(expected3, created);
			assertSame(expected3, mgr.find(descriptor3));
			mgr.destroy(descriptor3);
			assertCached(expected3, mgr, tst, descriptor3);
		}
		{ // *use* 2 again
			Object created = mgr.create(descriptor2);
			AtomicReference<DeviceResourceDescriptor> expected = tst.find(descriptor2);
			assertSame(expected, created);
			assertSame(expected, mgr.find(descriptor2));
			mgr.destroy(descriptor2);
			assertCached(expected, mgr, tst, descriptor2);
		}
		assertDestroyed(expected1, mgr, tst, descriptor1); // lru size exceeded: not cached anymore
		assertCached(expected2, mgr, tst, descriptor2);
		assertCached(expected3, mgr, tst, descriptor3);
		{
			Object created1_ = mgr.create(descriptor1);
			AtomicReference<DeviceResourceDescriptor> expected1_ = tst.find(descriptor1); // == descriptor1
			assertSame(expected1_, created1_);
			assertSame(expected1_, mgr.find(descriptor1));
			mgr.destroy(descriptor1);
			assertCached(expected1_, mgr, tst, descriptor1);
		}
		assertDestroyed(expected3, mgr, tst, descriptor3); // lru size exceeded: not cached anymore
		assertCached(expected2, mgr, tst, descriptor2); // 2 still cached, because recently used
	}
}
