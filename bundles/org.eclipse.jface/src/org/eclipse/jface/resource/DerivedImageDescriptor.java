/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * An image descriptor which creates images based on another ImageDescriptor, but with
 * additional SWT flags. Note that this is only intended for compatibility. 
 * 
 * @since 3.1
 */
final class DerivedImageDescriptor extends ImageDescriptor {

    private ImageDescriptor original;
    private int flags;
    
    public DerivedImageDescriptor(ImageDescriptor original, int swtFlags) {
        this.original = original;
        flags = swtFlags;
    }
    
    public Object createResource(Device device) throws DeviceResourceException {
        try {
            return internalCreateImage(device);
        } catch (SWTException e) {
            throw new DeviceResourceException(this, e);
        }
    }
    
    public Image createImage(Device device) {
        return internalCreateImage(device);
    }
    
    public int hashCode() {
        return original.hashCode() + flags;
    }
    
    public boolean equals(Object arg0) {
        if (arg0 instanceof DerivedImageDescriptor) {
            DerivedImageDescriptor desc = (DerivedImageDescriptor)arg0;
            
            return desc.original == original && flags == desc.flags;
        }
        
        return false;
    }
    
    /**
     * Creates a new Image on the given device. Note that we defined a new
     * method rather than overloading createImage since this needs to be
     * called by getImageData(), and we want to be absolutely certain not
     * to cause infinite recursion if the base class gets refactored. 
     *
     * @param device device to create the image on
     * @return a newly allocated Image. Must be disposed by calling image.dispose().
     */
    private final Image internalCreateImage(Device device) {
        Image originalImage = original.createImage(device);
        Image result = new Image(device, originalImage, flags);
        original.destroyResource(originalImage);
        return result;
    }
    
    public ImageData getImageData() {
        Image image = internalCreateImage(Display.getCurrent());
        ImageData result = image.getImageData();
        image.dispose();
        return result;
    }

}
