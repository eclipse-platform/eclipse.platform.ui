/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * Manages SWT resources for a particular device. This is called a "global" registry
 * because there is typically only one instance of GlobalSWTRegistry per device.
 * The application should allocate and deallocate resources using a shared registry.
 * This allows different parts of the app to share the same instance of a common resource.
 * <p>
 * Note that it is possible to create multiple GlobalSWTRegistries on the same device,
 * but this would be wasteful since they wouldn't share the resources for identical descriptors.
 * If some object wants its own local registry in order to safeguard against leaks, it should
 * use NestedSWTRegistry instead.
 * </p>
 * 
 * @see LocalResourceManager
 * 
 * @since 3.1
 */
public final class DeviceResourceManager extends AbstractResourceManager {
    
    private Device device;
    private Image missingImage;
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ResourceManager#getDevice()
     */
    public Device getDevice() {
        return device;
    }
    
    /**
     * Creates a new registry for the given device
     * 
     * @param device device to manage
     */
    public DeviceResourceManager(Device device) {
        this.device = device;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.AbstractSwtRegistry#allocate(org.eclipse.jface.resource.SwtResourceDescriptor)
     */
    protected Object allocate(DeviceResourceDescriptor descriptor) throws DeviceResourceException {
        return descriptor.createResource(device);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.AbstractSwtRegistry#deallocate(java.lang.Object, org.eclipse.jface.resource.SwtResourceDescriptor)
     */
    protected void deallocate(Object resource, DeviceResourceDescriptor descriptor) {
        descriptor.destroyResource(resource);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ResourceManager#getDefaultImage()
     */
    protected Image getDefaultImage() {
        if (missingImage == null) {
            missingImage = ImageDescriptor.getMissingImageDescriptor().createImage();
        }
        return missingImage;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.AbstractResourceManager#dispose()
     */
    public void dispose() {
        super.dispose();
        if (missingImage != null) {
            missingImage.dispose();
            missingImage = null;
        }
    }
}
