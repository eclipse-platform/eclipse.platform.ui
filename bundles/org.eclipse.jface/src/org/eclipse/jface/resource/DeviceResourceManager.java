/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * Manages SWT resources for a particular device. Clients normally don't need to construct
 * this object themselves, since JFace provides global registries for each display. The
 * global registries can be accessed via JFaceResources.getResources(Display). These global
 * registries should be used wherever possible since creating multiple registries on the same
 * device is wasteful.
 * 
 * <p>
 * Clients would only need to construct a DeviceResourceManager if they are allocating resources
 * on a non-display (such as a printer), or if they specifically want multiple copies of certain
 * resources (which may be useful when debugging resource leaks). In any other circumstance, the
 * class LocalResourceManager should be used for a localized registry.
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
     * Creates a new registry for the given device.
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
