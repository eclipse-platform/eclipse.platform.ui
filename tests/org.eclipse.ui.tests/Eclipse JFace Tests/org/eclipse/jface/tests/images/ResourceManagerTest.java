/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.DeviceResourceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.1
 */
public class ResourceManagerTest extends TestCase {

    private DeviceResourceDescriptor[] descriptors;
    private Image testImage;
    private Image testImage2;
    private Color testColor;
    private Color testColor2;
    private int numDupes;

    private DeviceResourceManager globalResourceManager;

    private static ImageDescriptor getImage(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.tests", path);
    }

    private static final class TestDescriptor extends DeviceResourceDescriptor {
        DeviceResourceDescriptor toWrap;
        public static int refCount = 0;

        public TestDescriptor(DeviceResourceDescriptor toWrap) {
            this.toWrap = toWrap;
        }

        @Override
		public Object createResource(Device device) throws DeviceResourceException {
            Object result = toWrap.createResource(device);
            refCount++;
            return result;
        }
        @Override
		public void destroyResource(Object previouslyCreatedObject) {
            refCount--;
            toWrap.destroyResource(previouslyCreatedObject);
        }

        @Override
		public boolean equals(Object arg0) {
            if (arg0 instanceof TestDescriptor) {
                TestDescriptor td = (TestDescriptor)arg0;

                return td.toWrap.equals(toWrap);
            }

            return false;
        }

        @Override
		public int hashCode() {
            return toWrap.hashCode();
        }
    }

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        TestDescriptor.refCount = 0;
        Display display = Display.getCurrent();
        globalResourceManager = new DeviceResourceManager(display);
        testImage = getImage("icons/anything.gif").createImage(display);
        testImage2 = getImage("icons/binary_co.gif").createImage(display);
        testColor = new Color(display, new RGB(10, 40, 20));
        testColor2 = new Color(display, new RGB(230, 100, 26));

        // Array of resource descriptors containing at least one duplicate of each type.
        // If you modify this array, be sure to adjust numDupes as well. Note that some
        // tests index the array directly, so it is a good idea to always add to this
        // array rather than remove from it.
        descriptors = new DeviceResourceDescriptor[] {
                new TestDescriptor(getImage("icons/anything.gif")),
                new TestDescriptor(getImage("icons/anything.gif")),
                new TestDescriptor(getImage("icons/binary_co.gif")),
                new TestDescriptor(getImage("icons/binary_co.gif")),
                new TestDescriptor(getImage("icons/mockeditorpart1.gif")),

                new TestDescriptor(getImage("icons/view.gif")), // 5
                new TestDescriptor(ImageDescriptor.createFromImage(testImage2)),
                new TestDescriptor(ImageDescriptor.createFromImage(testImage)),
                new TestDescriptor(ImageDescriptor.createFromImage(testImage)),
                new TestDescriptor(ImageDescriptor.createFromImage(testImage, display)),

                new TestDescriptor(ImageDescriptor.createFromImage(testImage, display)),  // 10
                new TestDescriptor(ImageDescriptor.createFromImage(testImage2, display)),
                new TestDescriptor(ColorDescriptor.createFrom(new RGB(10, 200, 54))),
                new TestDescriptor(ColorDescriptor.createFrom(new RGB(10, 200, 54))),
                new TestDescriptor(ColorDescriptor.createFrom(new RGB(200, 220, 54))),

                new TestDescriptor(ColorDescriptor.createFrom(testColor)), // 15
                new TestDescriptor(ColorDescriptor.createFrom(testColor)),
                new TestDescriptor(ColorDescriptor.createFrom(testColor2)),
                new TestDescriptor(ColorDescriptor.createFrom(testColor, display)),
                new TestDescriptor(ColorDescriptor.createFrom(testColor, display)),
                new TestDescriptor(ColorDescriptor.createFrom(testColor2, display))
                };

        // Let the tests know how many duplicates are in the array
        numDupes = 11;
    }

    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        globalResourceManager.dispose();
        Assert.assertEquals("Detected leaks", 0, TestDescriptor.refCount);
        testImage.dispose();
        testImage2.dispose();
    }

    protected void validateResource(Object resource) throws Exception {
        Assert.assertNotNull("Allocated resource was null", resource);
        if (resource instanceof Image) {
            Image image = (Image) resource;

            Assert.assertTrue("Image is disposed", !image.isDisposed());
            return;
        }
    }

    /**
     * Tests that the descriptors themselves w
     *
     * @throws Exception
     */
    public void testDescriptorAllocations() throws Exception {
        Display display = Display.getCurrent();

        // Allocate resources directly using the descriptors.
        Object[] resources = new Object[descriptors.length];

        for (int i = 0; i < descriptors.length; i++) {
            DeviceResourceDescriptor next = descriptors[i];

            Object resource = next.createResource(display);
            // Ensure that this resource was allocated correctly
            validateResource(resource);
            resources[i] = resource;
        }

        // Allocating resources without the manager will not reuse duplicates, so we
        // should get exactly one resource per descriptor.

        Assert.assertEquals("Expecting one resource to be allocated per descriptor",
                descriptors.length, TestDescriptor.refCount);

        // Deallocate resources directly using the descriptors
        for (int i = 0; i < descriptors.length; i++) {
            DeviceResourceDescriptor next = descriptors[i];

            next.destroyResource(resources[i]);
        }
    }

    public void testDeviceManagerAllocations() throws Exception {

        // Allocate resources directly using the descriptors.
        Object[] resources = new Object[descriptors.length];

        for (int i = 0; i < descriptors.length; i++) {
            DeviceResourceDescriptor next = descriptors[i];

            Object resource = globalResourceManager.create(next);
            // Ensure that this resource was allocated correctly
            validateResource(resource);
            resources[i] = resource;
        }

        Assert.assertEquals("Descriptors created from Images should not reallocate Images when the original can be reused",
                testImage, resources[9]);

        // Allocating resources without the manager will reuse duplicates, so the number
        // of resources should equal the number of unique descriptors.

        Assert.assertEquals("Duplicate descriptors should be reused",
                descriptors.length - numDupes,
                TestDescriptor.refCount);

        // Deallocate resources directly using the descriptors
        for (DeviceResourceDescriptor next : descriptors) {
            globalResourceManager.destroy(next);
        }
    }

    private void allocateResources(ResourceManager mgr, int[] toAllocate) throws Exception {
        for (int j : toAllocate) {
            validateResource(mgr.create(descriptors[j]));
        }
    }

    private void deallocateResources(ResourceManager mgr, int[] toDeallocate) {
        for (int j : toDeallocate) {
            mgr.destroy(descriptors[j]);
        }
    }

    public void testLocalManagerAllocations() throws Exception {
        // These arrays are indices into the descriptors array. For example, {0,1,7}
        // is a quick shorthand to indicate we should allocate resources 0, 1, and 7.
        int[] gResources = {0, 1, 5, 3, 7, 12, 13, 14};
        int[] lm1Resources = {0, 2, 3, 4, 11, 12, 13, 15};
        int[] lm2Resources = {0, 1, 6, 7, 8, 12, 14, 16};

        LocalResourceManager lm1 = new LocalResourceManager(globalResourceManager);
        LocalResourceManager lm2 = new LocalResourceManager(globalResourceManager);

        // Allocate a bunch of global resources
        allocateResources(globalResourceManager, gResources);

        // Remember how many global resources were allocated
        int initialCount = TestDescriptor.refCount;

        // Allocate a bunch of resources in lm1
        allocateResources(lm1, lm1Resources);

        // Remember how many global + lm1 resources there are
        int lm1Count = TestDescriptor.refCount;

        // Allocate and deallocate a bunch of resources in lm2
        // Ensure that all the lm2 resources were deallocated (we should be
        // back down to the count we had after we allocated lm1 resources)
        allocateResources(lm2, lm2Resources);
        deallocateResources(lm2, lm2Resources);
        Assert.assertEquals(lm1Count, TestDescriptor.refCount);

        // Dispose lm2 (shouldn't do anything since we already deallocated all of its resources)
        // Ensure that this doesn't change any refcounts
        lm2.dispose();
        Assert.assertEquals(lm1Count, TestDescriptor.refCount);

        // Dispose lm1 without first deallocating its resources. This should correctly detect
        // what needs to be deallocated from lm1 without affecting any global resources.
        // If everything works, we should be back to the resource count we had after allocating
        // the global resources.
        lm1.dispose();
        Assert.assertEquals(initialCount, TestDescriptor.refCount);

    }

    /*
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=135088
     */
    public void testResourceManagerFind() throws Exception {
    	DeviceResourceDescriptor descriptor = descriptors[0];
    	Object resource = globalResourceManager.find(descriptor);
    	assertNull("Resource should be null since it is not allocated", resource);
    	resource = globalResourceManager.create(descriptor);

        // Ensure that the resource was allocated correctly
        validateResource(resource);

        // Now the resource should be found
        Object foundResource = globalResourceManager.find(descriptor);
    	assertNotNull("Found resource should not be null", foundResource);
    	assertTrue("Found resource should be an image", foundResource instanceof Image);

    	// Destroy the resource we created
    	globalResourceManager.destroy(descriptor);
    }
}
