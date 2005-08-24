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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * This class manages SWT resources. It manages reference-counted instances of resources
 * such as Fonts, Images, and Colors, and allows them to be accessed using descriptors.
 * Everything allocated through the registry should also be disposed through the registry.
 * Since the resources are shared and reference counted, they should never be disposed
 * directly.
 * <p>
 * ResourceManager handles correct allocation and disposal of resources. It differs from 
 * the various JFace *Registry classes, which also map symbolic IDs onto resources. In 
 * general, you should use a *Registry class to map IDs onto descriptors, and use a 
 * ResourceManager to convert the descriptors into real Images/Fonts/etc.
 * </p>
 * 
 * @since 3.1
 */
public abstract class ResourceManager {
    
    private List disposeExecs = null;
    
    /**
     * Returns the Device for which this ResourceManager will create resources 
     * 
     * @since 3.1
     *
     * @return the Device associated with this ResourceManager
     */
    public abstract Device getDevice();
    
    /**
     * Returns the resource described by the given descriptor. If the resource already
     * exists, the reference count is incremented and the exiting resource is returned.
     * Otherwise, a new resource is allocated. Every call to create(...) should have
     * a corresponding call to dispose(...).  
     * 
     * @since 3.1 
     *
     * @param descriptor descriptor for the resource to allocate
     * @return the newly allocated resource (not null)
     * @throws DeviceResourceException if unable to allocate the resource
     */
    public abstract Object create(DeviceResourceDescriptor descriptor) throws DeviceResourceException;
    
    /**
     * Deallocates a resource previously allocated by create(...). Descriptors are compared
     * by equality, not identity. If the same resource was created multiple times, this may
     * decrement a reference count rather than disposing the actual resource.  
     * 
     * @since 3.1 
     *
     * @param descriptor identifier for the resource
     */
    public abstract void destroy(DeviceResourceDescriptor descriptor);
    
    /**
     * Creates an image, given an image descriptor. Images allocated in this manner must
     * be disposed by disposeImage, and never by calling Image.dispose().
     * 
     * @since 3.1 
     *
     * @param descriptor descriptor for the image to create
     * @return the Image described by this descriptor (possibly shared by other equivalent
     * ImageDescriptors)
     * @throws DeviceResourceException if unable to allocate the Image
     */
    public final Image createImage(ImageDescriptor descriptor) throws DeviceResourceException {
        return (Image)create(descriptor);
    }
    
    /**
     * Creates an image, given an image descriptor. Images allocated in this manner must
     * be disposed by disposeImage, and never by calling Image.dispose().
     * 
     * @since 3.1 
     *
     * @param descriptor descriptor for the image to create
     * @return the Image described by this descriptor (possibly shared by other equivalent
     * ImageDescriptors)
     */
    public final Image createImageWithDefault(ImageDescriptor descriptor) {
        if (descriptor == null) {
        	return getDefaultImage();
        }
        
        try {
			return (Image) create(descriptor);
		} catch (DeviceResourceException e) {
			Policy.getLog().log(
					new Status(IStatus.WARNING, "org.eclipse.jface", 0, //$NON-NLS-1$
							"The image could not be loaded: " + descriptor, //$NON-NLS-1$
							e));
			return getDefaultImage();
		} catch (SWTException e) {
			Policy.getLog().log(
					new Status(IStatus.WARNING, "org.eclipse.jface", 0, //$NON-NLS-1$
							"The image could not be loaded: " + descriptor, //$NON-NLS-1$
							e));
			return getDefaultImage();
		}
    }
    
    /**
     * Returns the default image that will be returned in the event that the intended
     * image is missing.
     * 
     * @since 3.1
     *
     * @return a default image that will be returned in the event that the intended
     * image is missing.
     */
    protected abstract Image getDefaultImage();

    /**
     * Undoes everything that was done by createImage(...).
     * 
     * @since 3.1 
     *
     * @param descriptor identifier for the image to dispose
     */
    public final void destroyImage(ImageDescriptor descriptor) {
        destroy(descriptor);
    }

    /**
     * Allocates a color, given a color descriptor. Any color allocated in this
     * manner must be disposed by calling disposeColor(...) and never by calling 
     * Color.dispose() directly.
     * 
     * @since 3.1 
     *
     * @param descriptor descriptor for the color to create
     * @return the Color described by the given ColorDescriptor (not null)
     * @throws DeviceResourceException if unable to create the color
     */
    public final Color createColor(ColorDescriptor descriptor) throws DeviceResourceException {
        return (Color)create(descriptor);
    }

    /**
     * Allocates a color, given its RGB values. Any color allocated in this
     * manner must be disposed by calling disposeColor(...) and never by calling 
     * Color.dispose() directly.
     * 
     * @since 3.1 
     *
     * @param descriptor descriptor for the color to create
     * @return the Color described by the given ColorDescriptor (not null)
     * @throws DeviceResourceException if unable to create the color
     */
    public final Color createColor(RGB descriptor) throws DeviceResourceException {
        return createColor(new RGBColorDescriptor(descriptor));
    }
    
    /**
     * Undoes everything that was done by a call to createColor(...).
     * 
     * @since 3.1 
     *
     * @param descriptor RGB value of the color to dispose
     */
    public final void destroyColor(RGB descriptor) {
        destroyColor(new RGBColorDescriptor(descriptor));
    }

    /**
     * Undoes everything that was done by a call to createColor(...).
     * 
     * @since 3.1 
     *
     * @param descriptor identifier for the color to dispose
     */
    public final void destroyColor(ColorDescriptor descriptor) {
        destroy(descriptor);
    }
    
    /**
     * Returns the Font described by the given FontDescriptor. Any Font
     * allocated in this manner must be deallocated by calling disposeFont(...) and
     * never by calling Font.dispose() directly.
     * 
     * @since 3.1 
     *
     * @param descriptor description of the font to create
     * @return the Font described by the given descriptor
     * @throws DeviceResourceException if unable to create the font
     */
    public final Font createFont(FontDescriptor descriptor) throws DeviceResourceException {
        return (Font)create(descriptor);
    }
    
    /**
     * Undoes everything that was done by a previous call to createFont().
     * 
     * @since 3.1 
     *
     * @param descriptor description of the font to destroy
     */
    public final void destroyFont(FontDescriptor descriptor) {
        destroy(descriptor);
    }
    
    /**
     * Disposes any remaining resources allocated by this manager. 
     */
    public void dispose() {
        if (disposeExecs == null) {
            return;
        }
        
        // If one of the runnables throws an exception, we need to propogate it.
        // However, this should not prevent the remaining runnables from being 
        // notified. If any runnables throw an exception, we remember one of them
        // here and throw it at the end of the method.
        RuntimeException foundException = null;
        
        Runnable[] execs = (Runnable[]) disposeExecs.toArray(new Runnable[disposeExecs.size()]);
        for (int i = 0; i < execs.length; i++) {
            Runnable exec = execs[i];            
            
            try {
                exec.run();
            } catch (RuntimeException e) {
                // Ensure that we propogate an exception, but don't stop notifying
                // the remaining runnables.
                foundException = e;
            }
        }
        
        if (foundException != null) {
            // If any runnables threw an exception, propogate one of them.
            throw foundException;
        }
    }
    
    /**
     * Returns a previously allocated resource associated with the given descriptor, or
     * null if none exists yet. 
     * 
     * @since 3.1
     *
     * @param descriptor descriptor to find
     * @return a previously allocated resource for the given descriptor or null if none.
     */
    public abstract Object find(DeviceResourceDescriptor descriptor);
    
    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked just before the receiver is disposed. The runnable
     * can be subsequently cancelled by a call to <code>cancelDisposeExec</code>.
     * 
     * @param r runnable to execute.
     */
    public void disposeExec(Runnable r) {
        Assert.isNotNull(r);
        
        if (disposeExecs == null) {
            disposeExecs = new ArrayList();
        }
        
        disposeExecs.add(r);
    }
    
    /**
     * Cancels a runnable that was previously scheduled with <code>disposeExec</code>.
     * Has no effect if the given runnable was not previously registered with
     * disposeExec.
     * 
     * @param r runnable to cancel
     */
    public void cancelDisposeExec(Runnable r) {
        Assert.isNotNull(r);
        
        if (disposeExecs == null) {
            return;
        }
        
        disposeExecs.remove(r);
        
        if (disposeExecs.isEmpty()) {
            disposeExecs = null;
        }
    }
}
